--http://jira.aplana.com/browse/SBRFACCTAX-14449: Дублирование текста в значении справочника "Статус по НДС"
update ref_book_value
set string_value = 'Организация, не признаваемая налогоплательщиком по НДС, или организация, освобожденная от обязанностей налогоплательщика'
where attribute_id = 5102 and record_id in (
	select id from ref_book_record rbr
	join ref_book_value rbv on rbr.id = rbv.record_id and rbv.attribute_id = 5101 and rbv.number_value = 1
	where rbr.ref_book_id = 510);		
	
--https://jira.aplana.com/browse/SBRFACCTAX-14735: Формирование специфического отчета декларации
INSERT INTO async_task_type (id, name, handler_jndi, limit_kind, dev_mode) values (26, 'Формирование специфического отчета декларации', 'ejb/taxaccounting/async-task.jar/SpecificReportDeclarationDataAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote', '', 0);
	
commit;
exit;	