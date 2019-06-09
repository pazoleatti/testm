DECLARE
	v_id number;
BEGIN
	
	merge into event a using
	(select 11 as id, 'Обновление данных ФЛ в КНФ' as name from dual) b
	on (a.id=b.id)
	when not matched then
		insert (id, name)
		values (b.id, b.name);
	
END;
/