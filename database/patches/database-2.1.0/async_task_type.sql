delete from async_task_type where id in (103, 104, 105, 106, 107, 110, 111, 112, 113, 114, 115, 123, 124, 125, 126, 127, 128, 129);

update async_task_type set handler_bean = 'XlsxGeneratorAsyncTask' where id = 5;
update async_task_type set handler_bean = 'XmlGeneratorAsyncTask' where id = 6;
update async_task_type set handler_bean = 'PdfGeneratorAsyncTask' where id = 7;
update async_task_type set handler_bean = 'CsvAuditArchiveGeneratorAsyncTask' where id = 10;
update async_task_type set handler_bean = 'CsvAuditGeneratorAsyncTask' where id = 11;
update async_task_type set handler_bean = 'LoadAllTransportDataAsyncTask' where id = 13;
update async_task_type set handler_bean = 'CheckDeclarationAsyncTask' where id = 14;
update async_task_type set handler_bean = 'AcceptDeclarationAsyncTask' where id = 15;
update async_task_type set handler_bean = 'ExcelReportRefBookAsyncTask' where id = 23;
update async_task_type set handler_bean = 'CsvReportRefBookAsyncTask' where id = 24;
update async_task_type set handler_bean = 'SpecificReportRefBookAsyncTask' where id = 25;
update async_task_type set handler_bean = 'SpecificReportDeclarationDataAsyncTask' where id = 26;
update async_task_type set handler_bean = 'UploadRefBookAsyncTask' where id = 27;
update async_task_type set handler_bean = 'CreateFormsAsyncTask' where id = 28;
update async_task_type set handler_bean = 'CreateReportsAsyncTask' where id = 29;

update async_task_type set name = 'Обработка справочника из каталога загрузки' where id=13;

merge into async_task_type a using
(select 12 as id, 'Обработка ТФ налоговой формы' as name, 'LoadTransportFileAsyncTask' as handler_bean, 10000 as short_queue_limit, 'Размер файла (Кбайт)' as limit_kind from dual
union all
select 30 as id, 'Загрузка данных РНУ НДФЛ из Excel-файла в ПНФ' as name, 'ImportExcelFileAsyncTask' as handler_bean, 10000 as short_queue_limit, 'Размер файла (Кбайт)' as limit_kind from dual
union all
select 31 as id, 'Выгрузка данных налоговой формы в виде шаблона ТФ (Excel)' as name, 'CreateExcelTemplateAsyncTask' as handler_bean, null as short_queue_limit, 'Количество ФЛ в НФ' as limit_kind from dual
union all
select 32 as id, 'Удаление налоговой формы' as name, 'DeleteDeclarationAsyncTask' as handler_bean, 10000 as short_queue_limit, 'Размер файла (Кбайт)' as limit_kind from dual) b
on (a.id=b.id)
when not matched then
	insert (id, name, handler_bean, short_queue_limit, limit_kind)
	values (b.id, b.name, b.handler_bean, b.short_queue_limit, b.limit_kind);