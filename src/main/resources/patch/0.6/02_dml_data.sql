--http://jira.aplana.com/browse/SBRFACCTAX-10831
insert into configuration_lock (key, timeout) values ('CONFIGURATION_PARAMS', 86400000);

--http://jira.aplana.com/browse/SBRFACCTAX-11050: 0.6 Новые асинхронные задачи(загрузка ТФ)
INSERT INTO configuration_lock (key, timeout) VALUES ('LOAD_TRANSPORT_DATA', 86400000);
INSERT INTO ASYNC_TASK_TYPE (ID, NAME, HANDLER_JNDI) VALUES (12, 'Загрузка ТФ с локального компьютера', 'ejb/taxaccounting/async-task.jar/UploadTransportDataAsyncTask#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote');
INSERT INTO ASYNC_TASK_TYPE (ID, NAME, HANDLER_JNDI) VALUES (13, 'Загрузка ТФ из каталога загрузки', 'ejb/taxaccounting/async-task.jar/LoadAllTransportDataAsyncTask#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote');

--http://jira.aplana.com/browse/SBRFACCTAX-10965: Ограничить время XSD-валидации
INSERT INTO configuration_lock (key, timeout) VALUES ('XSD_VALIDATION', 3600000);

INSERT INTO configuration_lock (key, timeout) VALUES ('LOG_SYSTEM_CSV', 3600000);

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
-- http://jira.aplana.com/browse/SBRFACCTAX-11238: 0.6 Добавить в бд записи об асинхронном формировании для ЖА
insert into async_task_type (id, name, handler_jndi) 
  values (10, 'Генерация архива ЖА', 'ejb/taxaccounting/async-task.jar/CsvAuditArchiveGeneratorAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote');
insert into async_task_type (id, name, handler_jndi)
  values (110, 'Генерация архива ЖА', 'CsvAuditArchiveGeneratorAsyncTaskSpring');
insert into async_task_type (id, name, handler_jndi)
  values (11, 'Генерация отчета ЖА', 'ejb/taxaccounting/async-task.jar/CsvAuditGeneratorAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote');
insert into async_task_type (id, name, handler_jndi)
  values (111, 'Генерация отчета ЖА', 'CsvAuditGeneratorAsyncTaskSpring');

-----------------------------------------------------------------------
----http://jira.aplana.com/browse/SBRFACCTAX-11812: Эталонное заполнение ASYNC_TASK_TYPE
TRUNCATE TABLE async_task_type;

INSERT INTO async_task_type(ID, NAME, HANDLER_JNDI, SHORT_QUEUE_LIMIT, TASK_LIMIT, LIMIT_KIND, DEV_MODE) VALUES (3,'Формирование XLSM-файла налоговой формы','ejb/taxaccounting/async-task.jar/XlsmGeneratorAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote',null,null,'Количество ячеек таблицы формы = Количество строк * Количество граф',0);
INSERT INTO async_task_type(ID, NAME, HANDLER_JNDI, SHORT_QUEUE_LIMIT, TASK_LIMIT, LIMIT_KIND, DEV_MODE) VALUES (4,'Формирование CSV-файла налоговой формы','ejb/taxaccounting/async-task.jar/CsvGeneratorAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote',null,null,'Количество ячеек таблицы формы = Количество строк * Количество граф',0);
INSERT INTO async_task_type(ID, NAME, HANDLER_JNDI, SHORT_QUEUE_LIMIT, TASK_LIMIT, LIMIT_KIND, DEV_MODE) VALUES (5,'Формирование XLSX-файла декларации/уведомления','ejb/taxaccounting/async-task.jar/XlsxGeneratorAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote',null,null,'Размер XML-файла декларации/уведомления (Кбайт)',0);
INSERT INTO async_task_type(ID, NAME, HANDLER_JNDI, SHORT_QUEUE_LIMIT, TASK_LIMIT, LIMIT_KIND, DEV_MODE) VALUES (6,'Расчет декларации/уведомления (формирование XML-файла)','ejb/taxaccounting/async-task.jar/XmlGeneratorAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote',null,null,'Сумма количества ячеек таблицы формы по всем формам источникам. Количество ячеек таблицы формы источника = Количество строк * Количество граф',0);
INSERT INTO async_task_type(ID, NAME, HANDLER_JNDI, SHORT_QUEUE_LIMIT, TASK_LIMIT, LIMIT_KIND, DEV_MODE) VALUES (7,'Формирование формы предварительного просмотра декларации/уведомления','ejb/taxaccounting/async-task.jar/PdfGeneratorAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote',null,null,'Размер XML-файла декларации/уведомления (Кбайт)',0);
INSERT INTO async_task_type(ID, NAME, HANDLER_JNDI, SHORT_QUEUE_LIMIT, TASK_LIMIT, LIMIT_KIND, DEV_MODE) VALUES (9,'Формирование отчетности для МСФО','ejb/taxaccounting/async-task.jar/IfrsGeneratorAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote',null,null,'Общий размер файлов форм МСФО (налоговая форма - XLSM, декларация/уведомление - XLSX',0);
INSERT INTO async_task_type(ID, NAME, HANDLER_JNDI, SHORT_QUEUE_LIMIT, TASK_LIMIT, LIMIT_KIND, DEV_MODE) VALUES (10,'Архивация ЖА','ejb/taxaccounting/async-task.jar/CsvAuditArchiveGeneratorAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote',null,null,null,0);
INSERT INTO async_task_type(ID, NAME, HANDLER_JNDI, SHORT_QUEUE_LIMIT, TASK_LIMIT, LIMIT_KIND, DEV_MODE) VALUES (11,'Генерация отчета ЖА','ejb/taxaccounting/async-task.jar/CsvAuditGeneratorAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote',null,null,null,0);
INSERT INTO async_task_type(ID, NAME, HANDLER_JNDI, SHORT_QUEUE_LIMIT, TASK_LIMIT, LIMIT_KIND, DEV_MODE) VALUES (12,'Загрузка ТФ налоговой формы с локального компьютера','ejb/taxaccounting/async-task.jar/UploadTransportDataAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote',null,null,'Размер ТФ (Кбайт)',0);
INSERT INTO async_task_type(ID, NAME, HANDLER_JNDI, SHORT_QUEUE_LIMIT, TASK_LIMIT, LIMIT_KIND, DEV_MODE) VALUES (13,'Загрузка ТФ налоговой формы из каталога загрузки','ejb/taxaccounting/async-task.jar/LoadAllTransportDataAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote',null,null,'Размер ТФ (Кбайт)',0);
INSERT INTO async_task_type(ID, NAME, HANDLER_JNDI, SHORT_QUEUE_LIMIT, TASK_LIMIT, LIMIT_KIND, DEV_MODE) VALUES (14,'Проверка декларации/уведомления','ejb/taxaccounting/async-task.jar/CheckDeclarationAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote',null,null,'Размер XML-файла декларации/уведомления (Кбайт)',0);
INSERT INTO async_task_type(ID, NAME, HANDLER_JNDI, SHORT_QUEUE_LIMIT, TASK_LIMIT, LIMIT_KIND, DEV_MODE) VALUES (15,'Принятие декларации/уведомления','ejb/taxaccounting/async-task.jar/AcceptDeclarationAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote',null,null,'Размер XML-файла декларации/уведомления (Кбайт)',0);
INSERT INTO async_task_type(ID, NAME, HANDLER_JNDI, SHORT_QUEUE_LIMIT, TASK_LIMIT, LIMIT_KIND, DEV_MODE) VALUES (16,'Консолидация в налоговую форму','ejb/taxaccounting/async-task.jar/ConsolidateFormDataAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote',null,null,'Сумма количества ячеек таблицы формы по всем формам источникам. Количество ячеек таблицы формы источника = Количество строк * Количество граф',0);
INSERT INTO async_task_type(ID, NAME, HANDLER_JNDI, SHORT_QUEUE_LIMIT, TASK_LIMIT, LIMIT_KIND, DEV_MODE) VALUES (17,'Расчет налоговой формы','ejb/taxaccounting/async-task.jar/CalculateFormDataAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote',null,null,'Количество ячеек таблицы формы = Количество строк * Количество граф',0);
INSERT INTO async_task_type(ID, NAME, HANDLER_JNDI, SHORT_QUEUE_LIMIT, TASK_LIMIT, LIMIT_KIND, DEV_MODE) VALUES (18,'Загрузка XLSM-файла с формы экземпляра налоговой формы','ejb/taxaccounting/async-task.jar/UploadFormDataAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote',null,null,'Размер файла (Кбайт)',0);
INSERT INTO async_task_type(ID, NAME, HANDLER_JNDI, SHORT_QUEUE_LIMIT, TASK_LIMIT, LIMIT_KIND, DEV_MODE) VALUES (19,'Проверка налоговой формы','ejb/taxaccounting/async-task.jar/CheckFormDataAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote',null,null,'Количество ячеек таблицы формы = Количество строк * Количество граф',0);
INSERT INTO async_task_type(ID, NAME, HANDLER_JNDI, SHORT_QUEUE_LIMIT, TASK_LIMIT, LIMIT_KIND, DEV_MODE) VALUES (20,'Подготовка/утверждение/принятие налоговой формы','ejb/taxaccounting/async-task.jar/MoveFormDataAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote',null,null,'Количество ячеек таблицы формы = Количество строк * Количество граф',0);

-----------------------------------------------------------------------
-- чистка блобов
delete from blob_data bd where id not in 
(select distinct id from 
(select script_id id from ref_book 
union select xsd from declaration_template 
union select jrxml from declaration_template 
union select blob_data_id from log_system 
union select blob_data_id from declaration_report 
union select blob_data_id from form_data_report 
union select blob_data_id from notification 
union select blob_data_id from ifrs_data) where id is not null);
-----------------------------------------------------------------------

commit;
exit;

