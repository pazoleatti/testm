DECLARE
	v_id number;
BEGIN
	merge into async_task_type a using
	(select 34 as id, 'Обновление данных ФЛ в КНФ' as name, 'UpdatePersonsDataAsyncTask' as handler_bean, 3000 as short_queue_limit, 'Количество ФЛ в НФ' as limit_kind from dual) b
	on (a.id=b.id)
	when not matched then
		insert (id, name, handler_bean, short_queue_limit, limit_kind)
		values (b.id, b.name, b.handler_bean, b.short_queue_limit, b.limit_kind);
		
	
END;
/