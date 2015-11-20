update ref_book_attribute set is_unique = 1 where id in (3305,3304);

--http://jira.aplana.com/browse/SBRFACCTAX-13269: Удаление всех текущих блокировок
delete from lock_data;

--http://jira.aplana.com/browse/SBRFACCTAX-12605: 0.8 Реализовать возможность создания форм типа "Расчетная"
INSERT INTO form_kind (id, name) VALUES (6, 'Расчетная');

--http://jira.aplana.com/browse/SBRFACCTAX-12866: справочники для табличных частей настроек  - неверсионными
update ref_book set IS_VERSIONED = 1 where id in (206, 310, 330);

--http://jira.aplana.com/browse/SBRFACCTAX-13181: Асинхронная задача Обновление формы
insert into async_task_type (id, name, handler_jndi, limit_kind) 
  values (21, 'Обновление формы', 'ejb/taxaccounting/async-task.jar/RefreshFormDataAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote', 'Количество ячеек таблицы формы = Количество строк * Количество граф');

--http://jira.aplana.com/browse/SBRFACCTAX-13356: Переименование событий в таблице событий
update event set name = 'Архивация журнала аудита' where id = 601;
update event set name = 'Версия макета создана' where id = 701;
update event set name = 'Версия макета изменена' where id = 702;
update event set name = 'Версия макета введена в действие' where id = 703;
update event set name = 'Версия макета выведена из действия' where id = 704;
update event set name = 'Версия макета удалена' where id = 705;

--Период Год для ТЦО 46->34
set serveroutput on size 30000;
begin
merge into report_period tgt
using (
  select rp.id, rp.calendar_start_date, 
                case when extract(month from calendar_start_date) = 1 then add_months(rp.calendar_start_date, 9) else rp.calendar_start_date end as shift_calendar_start_date,
         r46_id, r34_id 
  from report_period rp 
  join tax_period tp on tp.ID = rp.TAX_PERIOD_ID and tp.tax_type = 'D'
  join (select r.id as r46_id, v34.record_id as r34_id from ref_book b
    join ref_book_record r on r.ref_book_id = b.ID
    join ref_book_value v on v.RECORD_ID = r.id
    join ref_book_attribute a on a.id = v.ATTRIBUTE_ID and b.id = 8 and a.ID = 25 and v.STRING_VALUE = '46'
    join ref_book_value v34 on v34.attribute_id = 25 and v34.STRING_VALUE = '34'
    ) dict on dict.r46_id = rp.DICT_TAX_PERIOD_ID 
   where rp.name = 'год') src
on (tgt.id = src.id)  
when matched then 
  update set tgt.calendar_start_date = src.shift_calendar_start_date, tgt.dict_tax_period_id = src.r34_id;
  dbms_output.put_line(''||sql%rowcount||' rows merged.');
end;
/  

--------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-13274: Актуализация FORM_DATA_REF_BOOK
create or replace procedure rebuild_form_data_ref_book
is
    type msg_record is record (err_level varchar2(32), txt varchar2(512));
    type msg is table of msg_record;
    msg_list msg := msg();

    TYPE t_change_tab IS TABLE OF FORM_DATA_REF_BOOK%ROWTYPE;
    g_change_tab  t_change_tab := t_change_tab();
    g_change_tab_temp t_change_tab;
    g_initial_tab t_change_tab := t_change_tab();

    v_str_query varchar2(512) := '';
    v_cursor  SYS_REFCURSOR;
    v_count_deleted number(9) := 0;
    v_count_inserted number(9) := 0;
    v_flag number(1) := 0;
begin
    --Loop by form_template / user_tables
    for x in (select ft.id as ft_id,
                     ft.name as ft_name,
                     ft.version as ft_version,
                     ft.status as ft_status,
                     ut.table_name as table_name
              from form_template ft
              left join user_tables ut on ut.table_name = 'FORM_DATA_'||ft.id
              where exists (select 1 from form_column fc where fc.form_template_id = ft.id and fc.type='R' and fc.parent_column_id is null) order by ut.table_name nulls first) loop
        if (x.table_name is null) then
            msg_list.extend();
            msg_list(msg_list.last).err_level := 'ERROR';
            msg_list(msg_list.last).txt := 'Table not found (Template_ID = '|| x.ft_id ||') ' || x.ft_name || ' (' ||x.ft_version||'), status = '||x.ft_status;
        else
           --loop by form_column
            for y in (select fc.id, fc.alias, fc.attribute_id, rba.ref_book_id, utc.COLUMN_NAME, utc.DATA_TYPE
                      from form_column fc
                      join ref_book_attribute rba on rba.id = fc.attribute_id
                      left join user_tab_columns utc on utc.TABLE_NAME = 'FORM_DATA_'||fc.form_template_id and 'C'||fc.id = utc.COLUMN_NAME
                      where fc.type='R' and fc.parent_column_id is null and fc.form_template_id = x.ft_id
                      order by utc.column_name nulls first) loop

                if (y.column_name is null) then
                    msg_list.extend();
                    msg_list(msg_list.last).err_level := 'ERROR';
                    msg_list(msg_list.last).txt := 'Column not found (Template_ID = '|| x.ft_id ||') column = C' || y.id || ' (' ||y.alias||').';

                elsif (y.data_type <> 'NUMBER') then
                    msg_list.extend();
                    msg_list(msg_list.last).err_level := 'ERROR';
                    msg_list(msg_list.last).txt := 'Wrong datatype for a reference column (Template_ID = '|| x.ft_id ||') column = C' || y.id || ' (' ||y.alias||'): '||y.data_type;

                else

                    v_str_query := 'select distinct form_data_id, '||y.ref_book_id|| ' as ref_book_id, '||y.column_name||' as record_id from form_data_'||x.ft_id||' where '||y.column_name||' is not null ';

                    OPEN v_cursor FOR v_str_query;
                    FETCH v_cursor
                    BULK COLLECT INTO g_change_tab_temp;
                    CLOSE v_cursor;

                    g_change_tab := g_change_tab multiset union g_change_tab_temp;

               end if;
            end loop;
        end if;
    end loop;

  OPEN v_cursor FOR 'SELECT * FROM FORM_DATA_REF_BOOK';
  FETCH v_cursor
  BULK COLLECT INTO g_initial_tab;
  CLOSE v_cursor;

  msg_list.extend();
  msg_list(msg_list.last).err_level := 'INFO ';
  msg_list(msg_list.last).txt := 'Before merge: initial tab ['||g_initial_tab.count||'], new collection (not unique items) ['||g_change_tab.count||']';


  --new records
  for i in 1..g_change_tab.count loop
	  merge into form_data_ref_book tgt
      using (select g_change_tab(i).form_data_id as form_data_id, g_change_tab(i).ref_book_id as ref_book_id, g_change_tab(i).record_id as record_id from dual) src
      on (src.form_data_id = tgt.form_data_id and src.ref_book_id = tgt.ref_book_id and src.record_id = tgt.record_id)
      when not matched then
           insert (tgt.form_data_id, tgt.ref_book_id, tgt.record_id) values (src.form_data_id, src.ref_book_id, src.record_id);

      v_count_inserted := v_count_inserted + sql%rowcount;

  end loop;

  --delete records
  for i in 1..g_initial_tab.count loop
      v_flag := 0;
      for j in 1..g_change_tab.count loop
          if (g_initial_tab(i).form_data_id = g_change_tab(j).form_data_id and g_initial_tab(i).ref_book_id = g_change_tab(j).ref_book_id and g_initial_tab(i).record_id = g_change_tab(j).record_id) then
             v_flag := 1;
          end if;
      end loop;

      if v_flag = 0 then --Not found
         delete from form_data_ref_book where form_data_id = g_initial_tab(i).form_data_id and ref_book_id = g_initial_tab(i).ref_book_id and record_id = g_initial_tab(i).record_id;
         v_count_deleted := v_count_deleted + sql%rowcount;
      end if;

  end loop;

  msg_list.extend();
  msg_list(msg_list.last).err_level := 'INFO ';
  msg_list(msg_list.last).txt := 'Merge: ['||v_count_inserted||'] rows inserted, ['||v_count_deleted||'] rows deleted';


  --Print all generated messages
  for i in 1..msg_list.count loop
    dbms_output.put_line('[' || msg_list(i).err_level || ']:   ' ||msg_list(i).txt);
  end loop;
end;
/

set serveroutput on size 1000000;

--Удаление из FORM_DATA_NNN если нет записи в FORM_DATA
declare 
	cnt_form_data number(9) := 0;
begin
for x in (select table_name from user_tables ut join form_template ft on ut.table_name = 'FORM_DATA_'||ft.id) loop
	execute immediate 'select count(distinct form_data_id) from '||x.table_name||' t where not exists (select 1 from form_data fd where fd.id = t.form_data_id)' into cnt_form_data;
    execute immediate 'delete from '||x.table_name||' t where not exists (select 1 from form_data fd where fd.id = t.form_data_id)';
    if sql%rowcount <> 0 then dbms_output.put_line('['||x.table_name||']: '||sql%rowcount||' rows deleted ('||cnt_form_data||' unique form_data_id)'); end if;
end loop;
end;
/

exec rebuild_form_data_ref_book;

COMMIT;
EXIT;