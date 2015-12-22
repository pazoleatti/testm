--http://jira.aplana.com/browse/SBRFACCTAX-13954: Добавить новую асинхронную задачу "Формирование специфичного отчета НФ"
insert into async_task_type (id, name, handler_jndi, limit_kind, dev_mode) values (22, 'Формирование специфичного отчета налоговой формы', 'ejb/taxaccounting/async-task.jar/SpecificReportFormDataAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote', 'Количество ячеек таблицы формы = Количество строк * Количество граф', 0);



COMMIT;
EXIT;