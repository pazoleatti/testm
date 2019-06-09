DECLARE
	v_id number;
BEGIN
	merge into configuration a using
	(select 'ENABLE_IMPORT_PERSON' as code, 0 as department_id, 1 as value from dual) b
	on (a.code=b.code and a.department_id=b.department_id)
	when not matched then
		insert (code, department_id, value)
		values (b.code, b.department_id, b.value);
		
	
END;
/