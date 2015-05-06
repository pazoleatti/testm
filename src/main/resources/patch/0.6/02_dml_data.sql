--http://jira.aplana.com/browse/SBRFACCTAX-10831
insert into configuration_lock (key, timeout) values ('CONFIGURATION_PARAMS', 86400000);

--http://jira.aplana.com/browse/SBRFACCTAX-11050: 0.6 Новые асинхронные задачи(загрузка ТФ)
INSERT INTO configuration_lock (key, timeout) VALUES ('LOAD_TRANSPORT_DATA', 86400000);
INSERT INTO ASYNC_TASK_TYPE (ID, NAME, HANDLER_JNDI) VALUES (12, 'Загрузка ТФ с локального компьютера', 'ejb/taxaccounting/async-task.jar/UploadTransportDataAsyncTask#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote');
INSERT INTO ASYNC_TASK_TYPE (ID, NAME, HANDLER_JNDI) VALUES (13, 'Загрузка ТФ из каталога загрузки', 'ejb/taxaccounting/async-task.jar/LoadAllTransportDataAsyncTask#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote');

--http://jira.aplana.com/browse/SBRFACCTAX-10965: Ограничить время XSD-валидации
INSERT INTO configuration_lock (key, timeout) VALUES ('XSD_VALIDATION', 3600000);

-----------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-10770: Очистка данных (несоответствие типа графы графы с типом данных)

-- SET NULLS instead of invalid data
update data_cell dc
set nvalue = null
where nvalue is not null and dc.column_id in (select id from form_column fc where fc.type in ('D', 'S', 'A')); 

update data_cell dc
set nvalue = null
where nvalue is not null and dc.column_id in (select id from form_column fc where fc.type = 'R' and fc.parent_column_id is not null); 

update data_cell dc
set svalue = null
where svalue is not null and dc.column_id in (select id from form_column fc where fc.type <> 'S'); 

update data_cell dc
set dvalue = null
where dvalue is not null and dc.column_id in (select id from form_column fc where fc.type <> 'D'); 

-- Impose martial law
delete from data_cell 
where svalue is null 
	and dvalue is null 
	and nvalue is null 
	and style_id is null 
	and colspan is null 
	and rowspan is null 
	and editable is null;

-----------------------------------------------------------------------

commit;
exit;

