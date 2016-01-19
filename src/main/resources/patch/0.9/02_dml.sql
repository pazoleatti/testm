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

-----------------------------------------------------------------------------------------

COMMIT;
EXIT;