create or replace function BLOB_TO_CLOB(v_blob_in in blob)
return clob is

v_file_clob clob;
v_file_size integer := 1024;
v_dest_offset integer := 1;
v_src_offset integer := 1;
v_blob_csid number :=  nls_charset_id('CL8MSWIN1251'); --dbms_lob.default_csid;
v_lang_context number := dbms_lob.default_lang_ctx;
v_warning integer;
v_size integer := 0;

begin

dbms_lob.createtemporary(v_file_clob, true);

select dbms_lob.getlength(v_blob_in) into v_size from dual;

if (v_size <= v_file_size) then v_file_size := v_size; end if;

dbms_lob.converttoclob(v_file_clob,
v_blob_in,
v_file_size,
v_dest_offset,
v_src_offset,
v_blob_csid,
v_lang_context,
v_warning);

return v_file_clob;

end;
/
---------------------------------------------------------------------------------------

create or replace procedure CREATE_FORM_DATA_NNN (FT_ID number)
is
       v_table_name varchar2(512) := '';
       query_str varchar2(1024);
       x_column_type varchar2(64);
       chk_table number(1) :=0;
       p_nnn number(18) := FT_ID;
     v_session_id number(18) := 0;
begin
		--Получить идентификатор текущей сессии для логирования
	    select seq_log_query_session.nextval into v_session_id from dual;

	   for t in (select id as form_template_id, translate(fullname, '''', ' ') as table_template_fullname from form_template where id in (P_NNN) order by id) loop

         v_table_name := 'FORM_DATA_'||t.form_template_id;

         --Если таблица уже существует, предварительно удалить ее
         select count(*) into chk_table from user_tables ut where ut.table_name = v_table_name;
         if chk_table <> 0 then
            query_str := 'DROP TABLE '||v_table_name;
                      insert into log_clob_query (id, form_template_id, sql_mode, text_query, session_id) values(seq_log_query.nextval, t.form_template_id, 'DDL', query_str, v_session_id);
            EXECUTE IMMEDIATE query_str;
         end if;

         -- Фиксированная "шапка" создания таблицы
         query_str := 'create table '||v_table_name||' ('
         || 'ID    NUMBER(18) not null,'
         || 'FORM_DATA_ID    NUMBER(18) not null,'
         || 'TEMPORARY       NUMBER(1) not null,'
         || 'MANUAL          NUMBER(1) not null,'
         || 'ORD             NUMBER(14) not null,'
         || 'ALIAS           VARCHAR2(20)'
         || ')';

         insert into log_clob_query (id, form_template_id, sql_mode, text_query, session_id) values(seq_log_query.nextval, t.form_template_id, 'DDL', query_str, v_session_id);
         EXECUTE IMMEDIATE query_str;

        --Цикл по form_column, относящимся к заданному form_template.id
           for x in (select id, alias || ' - ' || name as column_comment, type, max_length, precision, attribute_id, ord from form_column where form_template_id = t.form_template_id order by ord) loop

               --Определить тип данных для значения (form_column.type)
              select DECODE(x.type, 'S', 'VARCHAR2(4000 BYTE)',
                          'N', 'DECIMAL(38, 19)',
                          'R', 'DECIMAL(18)',
                          'D', 'DATE',
                          'A', 'DECIMAL(18)') into x_column_type from dual;

              -- Создать столбец, содержащий значение определенного типа
              query_str := 'ALTER TABLE '||v_table_name||' ADD c'||x.Id ||' '|| x_column_type;
                        insert into log_clob_query (id, form_template_id, sql_mode, text_query, session_id) values(seq_log_query.nextval, t.form_template_id, 'DDL', query_str, v_session_id);
              execute immediate query_str;

              -- Комментарий = form_column.alias + ' - ' + form_column.name
              query_str := 'COMMENT ON COLUMN '||v_table_name ||'.c'||x.Id ||' is '''||translate(x.column_comment, '''', ' ') ||'''';
                        insert into log_clob_query (id, form_template_id, sql_mode, text_query, session_id) values(seq_log_query.nextval, t.form_template_id, 'DDL', query_str, v_session_id);
              execute immediate query_str;

              -- Создание столбцов 'STYLE_ID', 'EDITABLE', 'COLSPAN', 'ROWSPAN' с фиксированными метаданными
             query_str := 'ALTER TABLE '||v_table_name||' ADD (c'||x.Id||'_STYLE_ID NUMBER(9), c'
                          || x.Id||'_EDITABLE NUMBER(1), c'
                          || x.Id||'_COLSPAN NUMBER(3), c'
                          || x.Id||'_ROWSPAN NUMBER(3))';
                      insert into log_clob_query (id, form_template_id, sql_mode, text_query, session_id) values(seq_log_query.nextval, t.form_template_id, 'DDL', query_str, v_session_id);
             execute immediate query_str;
           end loop;

         --Фиксированный первичный ключ + уникальный индекс
          query_str := 'alter table '|| v_table_name ||' add constraint '||v_table_name||'_PK primary key (ID)';
               insert into log_clob_query (id, form_template_id, sql_mode, text_query, session_id) values(seq_log_query.nextval, t.form_template_id, 'DDL', query_str, v_session_id);
          execute immediate query_str;

          query_str := 'create unique index i_'|| v_table_name ||'_unq on '||v_table_name||' (FORM_DATA_ID, TEMPORARY, MANUAL, ORD)';
               insert into log_clob_query (id, form_template_id, sql_mode, text_query, session_id) values(seq_log_query.nextval, t.form_template_id, 'DDL', query_str, v_session_id);
          execute immediate query_str;

          --Индекс на FORM_DATA
          query_str := 'create index i_'|| v_table_name||' on '|| v_table_name ||' (form_data_id)';
               insert into log_clob_query (id, form_template_id, sql_mode, text_query, session_id) values(seq_log_query.nextval, t.form_template_id, 'DDL', query_str, v_session_id);
          execute immediate query_str;


          --Комментарий к таблице из названия шаблона
           query_str := 'comment on table '|| v_table_name || ' is ''' || coalesce(t.table_template_fullname, '') ||'''' ;
                insert into log_clob_query (id, form_template_id, sql_mode, text_query, session_id) values(seq_log_query.nextval, t.form_template_id, 'DDL', query_str, v_session_id);
          execute immediate query_str;

         -------------------------------------------------------------------------------------
      end loop;
end CREATE_FORM_DATA_NNN;
/ 

--Диагностика корректности структуры и метаданных для FORM_DATA_NNN
CREATE OR REPLACE PACKAGE form_data_nnn IS
  TYPE t_message IS RECORD(
		form_template_id NUMBER(9),
		form_template_name VARCHAR(1024),
		message VARCHAR2(1024));
  TYPE t_message_box IS TABLE OF t_message;
  FUNCTION check_structure RETURN t_message_box PIPELINED;
  FUNCTION check_template (p_ft_id number) RETURN t_message_box PIPELINED;
  END form_data_nnn;
/

CREATE OR REPLACE PACKAGE BODY form_data_nnn AS
FUNCTION check_structure RETURN t_message_box PIPELINED IS
         l_row t_message;
BEGIN
         for x in (select id from form_template order by id) loop
             for xdata in (select * from table(check_template(x.id))) loop
                 l_row.form_template_id := xdata.form_template_id;
                 l_row.form_template_name := xdata.form_template_name;
                 l_row.message := xdata.message;
                 PIPE ROW(l_row);
             end loop;
         end loop;

         for abandoned_tables in (select cast(substr(ut.table_name, 11) as number(9)) as form_template_id, utc.comments,  'Не найдена соответствующая запись в FORM_TEMPLATE для таблицы '||ut.table_name as message
                                  from user_tables ut
                                  join user_tab_comments utc on ut.table_name = utc.table_name
                                  WHERE REGEXP_LIKE(ut.table_name, '^FORM_DATA_[0-9]{1,}$')
                                        AND not exists (select 1 from form_template where id = cast(substr(ut.table_name, 11) as number(9)))
                                  ) loop
          l_row.form_template_id := abandoned_tables.form_template_id;
          l_row.form_template_name := abandoned_tables.comments;
          l_row.message := abandoned_tables.message;
          PIPE ROW(l_row);
         end loop
         RETURN;
      END;
FUNCTION check_template (p_ft_id number) RETURN t_message_box PIPELINED IS
         l_row t_message;
         l_counter number(18) := 0;
         l_form_column_counter number(9) := 0;
         l_tab_column_counter number(9) := 0;
BEGIN
         for x in (select id, fullname from form_template where id = p_ft_id) loop

          select count(*) into l_counter from user_tables where upper(table_name) = upper('FORM_DATA_'||x.id);
          if (l_counter = 0) then --If the table does not even exist
            l_row.form_template_id := x.id;
            l_row.form_template_name := x.fullname;
            l_row.message := 'Таблица FORM_DATA_'||x.id||' не найдена';
            PIPE ROW(l_row);

          else --If exists, compare columns
            select count(*)*5+6 into l_form_column_counter from form_column where form_template_id = x.id;
            select count(*) into l_tab_column_counter from user_tab_columns where upper(table_name) = upper('FORM_DATA_'||x.id);

            if (l_form_column_counter <> l_tab_column_counter) then
               l_row.form_template_id := x.id;
               l_row.form_template_name := x.fullname;
               l_row.message := 'Количество столбцов в шаблоне и соответствующей таблице не совпадает';
               PIPE ROW(l_row);
            end if;

            for col_check in (
                    with t as (
                        select '' as postfix, 1 as ord from dual
                        union all select '_STYLE_ID' as postfix, 2 as ord from dual
                        union all select '_EDITABLE' as postfix, 3 as ord  from dual
                        union all select '_COLSPAN' as postfix, 4 as ord  from dual
                        union all select '_ROWSPAN' as postfix, 5 as ord from dual),
                      tdata as (
                        select fc.form_template_id, fc.id, fc.id||t.postfix as form_column_name, fc.type, utc.TABLE_NAME, utc.COLUMN_NAME, utc.DATA_TYPE
                        from t
                            cross join form_column fc
                            full outer join user_tab_columns utc
                                 on upper(utc.table_name) = upper('FORM_DATA_'||fc.form_template_id) and upper(utc.COLUMN_NAME) = upper('C'||fc.id||t.postfix)
                        where (table_name = 'FORM_DATA_'||x.id or fc.form_template_id = x.id)
                        and (utc.COLUMN_NAME is null or utc.column_name not in ('ID', 'FORM_DATA_ID', 'TEMPORARY', 'MANUAL', 'ORD', 'ALIAS'))),
                      t_msg as (
                         select coalesce(form_template_id, cast(substr(table_name, 11) as number(9))) as form_template_id,
                           case when form_template_id is not null and table_name is null and REGEXP_LIKE(form_column_name, '^[0-9]{1,}$') then 'В таблице FORM_DATA_'||form_template_id||' отсутствует ожидаемое поле '||'C'||form_column_name || ' и иже с ним'
                                when form_template_id is null and table_name is not null and column_name not like 'C%\_%' ESCAPE '\' then 'Не найдена соответствующая запись в FORM_COLUMN для столбца '||table_name||'.'||column_name
                                when form_template_id is not null and table_name is not null and column_name not like 'C%\_%' ESCAPE '\' and DECODE(type, 'S', 'VARCHAR2', 'N', 'NUMBER', 'R', 'NUMBER', 'D', 'DATE', 'A', 'NUMBER') <> data_type then 'Расхождение по типам данных между '||table_name||'.'||column_name||'('||data_type||') и form_column (id='||id||', тип='||type||')'
                                end message
                                from tdata)
                    select form_template_id, message from t_msg where message is not null) loop
                     l_row.form_template_id := x.id;
                     l_row.form_template_name := x.fullname;
                     l_row.message := col_check.message;
                     PIPE ROW(l_row);
               end loop;
          end if;

         end loop;
         RETURN;
      END;
   END form_data_nnn;
/

create or replace procedure DELETE_FORM_TEMPLATE(FT_STR varchar) is
       query_str varchar2(1024);
       v_session_id number(18) := 0;
begin
  --Получить идентификатор текущей сессии для логирования
      select seq_log_query_session.nextval into v_session_id from dual;        
       
  --Всё монопольно заблокировать    
  for x in (select id from (
                  select trim(regexp_substr(FT_STR,'[^,]+', 1, level)) as id
                  from dual
                  connect by regexp_substr(FT_STR, '[^,]+', 1, level) is not null
                  ) t 
                  join user_tables ut on upper('FORM_DATA_'||t.id) = upper(ut.table_name)) loop
      query_str := 'LOCK TABLE FORM_DATA_'||x.id||' IN EXCLUSIVE MODE WAIT 300';
      insert into log_clob_query (id, form_template_id, sql_mode, text_query, session_id) 
         values(seq_log_query.nextval, x.id, 'DDL', query_str, v_session_id);          
      execute immediate query_str;
  end loop;
  
  --Удалить таблицы
  for x in (select id from (
                  select trim(regexp_substr(FT_STR,'[^,]+', 1, level)) as id
                  from dual
                  connect by regexp_substr(FT_STR, '[^,]+', 1, level) is not null
                  ) t 
                  join user_tables ut on upper('FORM_DATA_'||t.id) = upper(ut.table_name)) loop
      query_str := 'DROP TABLE FORM_DATA_'||x.id;
      insert into log_clob_query (id, form_template_id, sql_mode, text_query, session_id) 
         values(seq_log_query.nextval, x.id, 'DDL', query_str, v_session_id);          
      execute immediate query_str;
    
  end loop;
  
end DELETE_FORM_TEMPLATE;
/

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
