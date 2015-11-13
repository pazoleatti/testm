update ref_book_attribute set is_unique = 1 where id in (3305,3304);

--http://jira.aplana.com/browse/SBRFACCTAX-13269: Удаление всех текущих блокировок
delete from lock_data;

--http://jira.aplana.com/browse/SBRFACCTAX-12605: 0.8 Реализовать возможность создания форм типа "Расчетная"
INSERT INTO form_kind (id, name) VALUES (6, 'Расчетная');

--http://jira.aplana.com/browse/SBRFACCTAX-12866: справочники для табличных частей настроек  - неверсионными
update ref_book set IS_VERSIONED = 0 where id in (206, 310, 330);

--http://jira.aplana.com/browse/SBRFACCTAX-13181: Асинхронная задача Обновление формы
insert into async_task_type (id, name, handler_jndi, limit_kind) 
  values (21, 'Обновление формы', 'ejb/taxaccounting/async-task.jar/RefreshFormDataAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote', 'Количество ячеек таблицы формы = Количество строк * Количество граф');
  
--http://jira.aplana.com/browse/SBRFACCTAX-13378: Исправить настройки граф согласно ограничениям на размерность
update form_column set max_length = 17 + precision where max_length - precision > 17 and type = 'N';

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

COMMIT;
EXIT;