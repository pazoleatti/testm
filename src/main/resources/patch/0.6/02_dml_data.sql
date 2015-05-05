--http://jira.aplana.com/browse/SBRFACCTAX-10831
insert into configuration_lock (key, timeout) values ('CONFIGURATION_PARAMS', 86400000);

--http://jira.aplana.com/browse/SBRFACCTAX-11050: 0.6 ����� ����������� ������(�������� ��)
INSERT INTO configuration_lock (key, timeout) VALUES ('LOAD_TRANSPORT_DATA', 86400000);
INSERT INTO ASYNC_TASK_TYPE (ID, NAME, HANDLER_JNDI) VALUES (12, '�������� �� � ���������� ����������', 'ejb/taxaccounting/async-task.jar/UploadTransportDataAsyncTask#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote');
INSERT INTO ASYNC_TASK_TYPE (ID, NAME, HANDLER_JNDI) VALUES (13, '�������� �� �� �������� ��������', 'ejb/taxaccounting/async-task.jar/LoadAllTransportDataAsyncTask#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote');

--http://jira.aplana.com/browse/SBRFACCTAX-10965: ���������� ����� XSD-���������
INSERT INTO configuration_lock (key, timeout) VALUES ('XSD_VALIDATION', 3600000);

-----------------------------------------------------------------------
commit;
exit;

