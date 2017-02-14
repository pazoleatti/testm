insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('3','Формирование отчета налоговой формы в XLSM-формате','ejb/ndfl/async-task.jar/XlsmGeneratorAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote','30000','2000000','Ячейки = строки * графы','0');
insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('4','Формирование отчета налоговой формы в CSV-формате','ejb/ndfl/async-task.jar/CsvGeneratorAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote',null,'15000000','Ячейки = строки * графы','0');
insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('5','Формирование отчета налоговой формы в XLSX-формате ','ejb/ndfl/async-task.jar/XlsxGeneratorAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote','20','3000','Размер XML-файла декларации/уведомления (Кбайт)','0');
insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('6','Расчет налоговой формы (формирование XML-файла)','ejb/ndfl/async-task.jar/XmlGeneratorAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote','1',null,'Сумма по всем формам-источникам. Ячейки = строки * графы','0');
insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('7','Создание формы предварительного просмотра налоговой формы','ejb/ndfl/async-task.jar/PdfGeneratorAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote','15','7000','Размер XML-файла декларации/уведомления (Кбайт)','0');
insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('13','Обработка ТФ налоговой формы/справочника из каталога загрузки','ejb/ndfl/async-task.jar/LoadAllTransportDataAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote','10000',null,'Размер файла (Кбайт)','0');
insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('112','Загрузка ТФ с локального компьютера','UploadTransportDataAsyncTaskSpring','0','0',null,'1');
insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('113','Загрузка ТФ из каталога загрузки','LoadAllTransportDataAsyncTaskSpring','0','0',null,'1');
insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('106','Генерация xml-файла','TestXmlGeneratorAsyncTaskSpring','0','0',null,'1');
insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('114','Проверка налоговой формы','CheckDeclarationAsyncTaskSpring','0','0',null,'1');
insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('14','Проверка налоговой формы','ejb/ndfl/async-task.jar/CheckDeclarationAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote','1',null,'Размер XML-файла декларации/уведомления (Кбайт)','0');
insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('15','Принятие налоговой формы','ejb/ndfl/async-task.jar/AcceptDeclarationAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote','1',null,'Размер XML-файла декларации/уведомления (Кбайт)','0');
insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('10','Архивация ЖА','ejb/ndfl/async-task.jar/CsvAuditArchiveGeneratorAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote','1',null,null,'0');
insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('110','Генерация архива ЖА','CsvAuditArchiveGeneratorAsyncTaskSpring','0','0',null,'1');
insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('11','Генерация отчета ЖА','ejb/ndfl/async-task.jar/CsvAuditGeneratorAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote','1',null,null,'0');
insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('111','Генерация отчета ЖА','CsvAuditGeneratorAsyncTaskSpring','0','0',null,'1');
insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('103','Генерация xlsm-файла','TestXlsmGeneratorAsyncTaskSpring','0','0',null,'1');
insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('104','Генерация csv-файла','TestCsvGeneratorAsyncTaskSpring','0','0',null,'1');
insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('105','Генерация xlsx-файла','TestXlsxGeneratorAsyncTaskSpring','0','0',null,'1');
insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('107','Генерация pdf-файла','TestPdfGeneratorAsyncTaskSpring','0','0',null,'1');
insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('115','Принятие налоговой формы','AcceptDeclarationAsyncTaskSpring','0','0',null,'1');
insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('26','Формирование специфического отчета налоговой формы','ejb/ndfl/async-task.jar/SpecificReportDeclarationDataAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote',null,null,'Сумма по всем формам-источникам. Ячейки = строки * графы','0');
insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('27','Загрузка данных из файла в справочник','ejb/ndfl/async-task.jar/UploadRefBookAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote',null,null,'Размер файла (Кбайт)','0');
insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('23','Формирование отчета справочника в XLSM-формате','ejb/ndfl/async-task.jar/ExcelReportRefBookAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote',null,null,'Ячейки = строки * столбцы','0');
insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('24','Формирование отчета справочника в CSV-формате ','ejb/ndfl/async-task.jar/CsvReportRefBookAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote',null,null,'Ячейки = строки * столбцы','0');
insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('25','Формирование специфического отчета справочника','ejb/ndfl/async-task.jar/SpecificReportRefBookAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote',null,null,'Ячейки = строки * столбцы','0');
insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('123','Формирование XLSX-отчета справочника','ExcelReportRefBookAsyncTaskSpring',null,null,'Количество выгружаемых ячеек = Количество строк * Количество столбцов','1');
insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('124','Формирование CSV-отчета справочника','CsvReportRefBookAsyncTaskSpring',null,null,'Количество выгружаемых ячеек = Количество строк * Количество столбцов','1');
insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('125','Формирование специфичного отчета справочника','SpecificReportRefBookAsyncTaskSpring',null,null,'Количество выгружаемых ячеек = Количество строк * Количество столбцов','1');
insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('126','Формирование специфического отчета налоговой формы','SpecificReportDeclarationDataAsyncTaskSpring',null,null,'Сумма по всем формам-источникам. Ячейки = строки * графы','1');
insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('127','Загрузка данных из файла в справочник','UploadRefBookAsyncTaskSpring',null,null,'Размер файла (Кбайт)','1');
insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('29','Формирование отчетности','ejb/ndfl/async-task.jar/CreateReportsAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote',null,null,null,'0');
insert into async_task_type (id,name,handler_jndi,short_queue_limit,task_limit,limit_kind,dev_mode) values ('129','Формирование отчетности','CreateReportsAsyncTaskSpring',null,null,null,'1');
