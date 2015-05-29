--http://jira.aplana.com/browse/SBRFACCTAX-10831
insert into configuration_lock (key, timeout) values ('CONFIGURATION_PARAMS', 86400000);

--http://jira.aplana.com/browse/SBRFACCTAX-11050: 0.6 ����� ����������� ������(�������� ��)
INSERT INTO configuration_lock (key, timeout) VALUES ('LOAD_TRANSPORT_DATA', 86400000);
INSERT INTO ASYNC_TASK_TYPE (ID, NAME, HANDLER_JNDI) VALUES (12, '�������� �� � ���������� ����������', 'ejb/taxaccounting/async-task.jar/UploadTransportDataAsyncTask#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote');
INSERT INTO ASYNC_TASK_TYPE (ID, NAME, HANDLER_JNDI) VALUES (13, '�������� �� �� �������� ��������', 'ejb/taxaccounting/async-task.jar/LoadAllTransportDataAsyncTask#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote');

--http://jira.aplana.com/browse/SBRFACCTAX-10965: ���������� ����� XSD-���������
INSERT INTO configuration_lock (key, timeout) VALUES ('XSD_VALIDATION', 3600000);

-----------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-10770: ������� ������ (�������������� ���� ����� ����� � ����� ������)

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
-- http://jira.aplana.com/browse/SBRFACCTAX-11238: 0.6 �������� � �� ������ �� ����������� ������������ ��� ��
insert into async_task_type (id, name, handler_jndi) 
  values (10, '��������� ������ ��', 'ejb/taxaccounting/async-task.jar/CsvAuditArchiveGeneratorAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote');
insert into async_task_type (id, name, handler_jndi)
  values (110, '��������� ������ ��', 'CsvAuditArchiveGeneratorAsyncTaskSpring');
insert into async_task_type (id, name, handler_jndi)
  values (11, '��������� ������ ��', 'ejb/taxaccounting/async-task.jar/CsvAuditGeneratorAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote');
insert into async_task_type (id, name, handler_jndi)
  values (111, '��������� ������ ��', 'CsvAuditGeneratorAsyncTaskSpring');

-----------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-11235: ���������� ������ � ASYNC_TASK_TYPE
update async_task_type set handler_jndi = 'ejb/taxaccounting/async-task.jar/XlsmGeneratorAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote' where id = 3;
update async_task_type set handler_jndi = 'ejb/taxaccounting/async-task.jar/CsvGeneratorAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote' where id = 4;
update async_task_type set handler_jndi = 'ejb/taxaccounting/async-task.jar/XlsxGeneratorAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote' where id = 5;
update async_task_type set handler_jndi = 'ejb/taxaccounting/async-task.jar/XmlGeneratorAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote' where id = 6;
update async_task_type set handler_jndi = 'ejb/taxaccounting/async-task.jar/PdfGeneratorAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote' where id = 7;
update async_task_type set handler_jndi = 'ejb/taxaccounting/async-task.jar/IfrsGeneratorAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote' where id = 9;
update async_task_type set handler_jndi = 'ejb/taxaccounting/async-task.jar/UploadTransportDataAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote' where id = 12;
update async_task_type set handler_jndi = 'ejb/taxaccounting/async-task.jar/LoadAllTransportDataAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote' where id = 13;

-----------------------------------------------------------------------

commit;
exit;

