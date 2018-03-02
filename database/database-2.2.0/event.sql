DECLARE
	v_id number;
BEGIN
	
	merge into event a using
	(select 10 as id, 'Изменение данных' as name from dual) b
	on (a.id=b.id)
	when not matched then
		insert (id, name)
		values (b.id, b.name);
	
END;
/