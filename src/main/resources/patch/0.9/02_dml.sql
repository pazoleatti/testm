--http://jira.aplana.com/browse/SBRFACCTAX-13954: Добавить новую асинхронную задачу "Формирование специфичного отчета НФ"
INSERT INTO async_task_type (id, name, handler_jndi, limit_kind, dev_mode) values (22, 'Формирование специфичного отчета налоговой формы', 'ejb/taxaccounting/async-task.jar/SpecificReportFormDataAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote', 'Количество ячеек таблицы формы = Количество строк * Количество граф', 0);

--http://jira.aplana.com/browse/SBRFACCTAX-14002: Асинхронные задачи для формирования отчетов
INSERT INTO async_task_type (id, name, handler_jndi, limit_kind, dev_mode) values (23, 'Формирование XLSX-отчета справочника', 'ejb/taxaccounting/async-task.jar/ExcelReportRefBookAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote', 'Количество выгружаемых ячеек = Количество строк * Количество столбцов', 0);
INSERT INTO async_task_type (id, name, handler_jndi, limit_kind, dev_mode) values (24, 'Формирование CSV-отчета справочника', 'ejb/taxaccounting/async-task.jar/CsvReportRefBookAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote', 'Количество выгружаемых ячеек = Количество строк * Количество столбцов', 0);
INSERT INTO async_task_type (id, name, handler_jndi, limit_kind, dev_mode) values (25, 'Формирование специфичного отчета справочника', 'ejb/taxaccounting/async-task.jar/SpecificReportRefBookAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote', 'Количество выгружаемых ячеек = Количество строк * Количество столбцов', 0);

COMMIT;

-----------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-14070: 0.9. ТЦО. Справочник "Признаки физической поставки". Добавить запись "Поставочная сделка"
SET serveroutput ON SIZE 10000;
BEGIN
INSERT ALL
	INTO ref_book_record (id, ref_book_id, record_id, status, version) VALUES (seq_ref_book_record.nextval, 18, 3, 0, to_date('01.01.2012', 'dd.mm.yyyy'))
	INTO ref_book_value (record_id, attribute_id, number_value) VALUES (seq_ref_book_record.currval, 43, 3)
	INTO ref_book_value (record_id, attribute_id, string_value) VALUES (seq_ref_book_record.currval, 44, 'Поставочная сделка')
SELECT * FROM dual;

dbms_output.put_line(SQL%ROWCOUNT || ' rows inserted (SBRFACCTAX-14070)');

EXCEPTION
	WHEN OTHERS THEN
		dbms_output.put_line('Insert failed (' || SQLERRM || ' ). Block will be rollbacked. (SBRFACCTAX-14070)');
		ROLLBACK;
END;
/	
COMMIT;

--http://jira.aplana.com/browse/SBRFACCTAX-14314: Переименовать названия асинхронных задач
begin
merge into async_task_type att
using (
  select 3 as id, 'Формирование отчета налоговой формы в XLSM-формате' as name, 'Ячейки = строки * графы' as limit_kind from dual union all 
  select 4 as id, 'Формирование отчета налоговой формы в CSV-формате' as name, 'Ячейки = строки * графы' as limit_kind from dual union all 
  select 5 as id, 'Формирование отчета декларации/уведомления в XLSX-формате ' as name, 'Размер XML-файла декларации/уведомления (Кбайт)' as limit_kind from dual union all 
  select 6 as id, 'Расчет декларации/уведомления (формирование XML-файла)' as name, 'Сумма по всем формам-источникам. Ячейки = строки * графы' as limit_kind from dual union all 
  select 7 as id, 'Создание формы предварительного просмотра декларации/уведомления' as name, 'Размер XML-файла декларации/уведомления (Кбайт)' as limit_kind from dual union all 
  select 9 as id, 'Формирование отчетности для МСФО' as name, 'Общий размер файлов форм МСФО (налоговая форма - XLSM, декларация/уведомление - XLSX' as limit_kind from dual union all 
  select 10 as id, 'Архивация ЖА' as name, '' as limit_kind from dual union all 
  select 11 as id, 'Генерация отчета ЖА' as name, '' as limit_kind from dual union all 
  select 13 as id, 'Обработка ТФ налоговой формы/справочника из каталога загрузки' as name, 'Размер файла (Кбайт)' as limit_kind from dual union all 
  select 14 as id, 'Проверка декларации/уведомления' as name, 'Размер XML-файла декларации/уведомления (Кбайт)' as limit_kind from dual union all 
  select 15 as id, 'Принятие декларации/уведомления' as name, 'Размер XML-файла декларации/уведомления (Кбайт)' as limit_kind from dual union all 
  select 16 as id, 'Консолидация в налоговую форму' as name, 'Сумма по всем формам-источникам. Ячейки = строки * графы' as limit_kind from dual union all 
  select 17 as id, 'Расчет налоговой формы' as name, 'Ячейки = строки * графы' as limit_kind from dual union all 
  select 18 as id, 'Загрузка XLSM-файла с формы экземпляра налоговой формы' as name, 'Размер файла (Кбайт)' as limit_kind from dual union all 
  select 19 as id, 'Проверка налоговой формы' as name, 'Ячейки = строки * графы' as limit_kind from dual union all 
  select 20 as id, 'Подготовка/утверждение/принятие налоговой формы' as name, 'Ячейки = строки * графы' as limit_kind from dual union all 
  select 21 as id, 'Обновление формы' as name, 'Ячейки = строки * графы' as limit_kind from dual union all 
  select 22 as id, 'Формирование специфического отчета налоговой формы' as name, 'Ячейки = строки * графы' as limit_kind from dual union all 
  select 23 as id, 'Формирование отчета справочника в XLSM-формате' as name, 'Ячейки = строки * столбцы' as limit_kind from dual union all 
  select 24 as id, 'Формирование отчета справочника в CSV-формате ' as name, 'Ячейки = строки * столбцы' as limit_kind from dual union all 
  select 25 as id, 'Формирование специфического отчета справочника' as name, 'Ячейки = строки * столбцы' as limit_kind from dual) t
on (att.id = t.id)  
when matched then
     update set att.name = t.name, att.limit_kind = t.limit_kind where att.name <> t.name or att.limit_kind <> t.limit_kind;

dbms_output.put_line('ASYNC_TASK_TYPE: '||sql%rowcount||' rows merged.');          
end;
/     
commit;
-----------------------------------------------------------------------------------------

COMMIT;
EXIT;