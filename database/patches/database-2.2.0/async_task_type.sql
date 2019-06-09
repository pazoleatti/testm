DECLARE
	v_id number;
BEGIN
	merge into async_task_type a using
	(select 8 as id, 'Идентификация ФЛ налоговой формы' as name, 'IdentifyAsyncTask' as handler_bean, 3000 as short_queue_limit, 'Количество ФЛ в НФ' as limit_kind from dual
	union all
	select 9 as id, 'Консолидация налоговой формы' as name, 'ConsolidateAsyncTask' as handler_bean, 3000 as short_queue_limit, 'Количество ФЛ в НФ' as limit_kind from dual
	union all
	select 33 as id, 'Загрузка данных xml в справочник' as name, 'ImportRefBookXmlAsyncTask' as handler_bean, 10000 as short_queue_limit, 'Размер файла (Кбайт)' as limit_kind from dual) b
	on (a.id=b.id)
	when not matched then
		insert (id, name, handler_bean, short_queue_limit, limit_kind)
		values (b.id, b.name, b.handler_bean, b.short_queue_limit, b.limit_kind);
		
	
END;
/