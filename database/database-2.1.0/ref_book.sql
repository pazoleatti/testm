DECLARE
	v_id number;
BEGIN
	delete from ref_book_attribute where ref_book_id in (select id from ref_book where table_name in ('REF_BOOK_FOND','REF_BOOK_FOND_DETAIL'));
	delete from ref_book where table_name in ('REF_BOOK_FOND','REF_BOOK_FOND_DETAIL');

	--SBRFNDFL-2142
	update ref_book set is_versioned=0 where table_name='REPORT_PERIOD_TYPE';

	update ref_book_attribute set attribute_id = (select id from ref_book_attribute where ref_book_id = 904 and alias = 'RECORD_ID') where ref_book_id = 905 and alias = 'PERSON_ID';

	select min(record_id) into v_id from ref_book_income_type where code='2791';
	update ref_book_income_type set record_id=v_id where code='2791';
	
	update ref_book_income_type set version=version+1 where status=2;
	
	
	merge into ref_book_attribute a using
	(select 9007 as id, 900 as ref_book_id, 'Приоритет АСНУ' as name, 'PRIORITY' as alias, 2 as type, 5 as ord, 1 as visible, 20 as width,
		1 as required, 0 as is_unique, 0 as read_only, 0 as precision, 4 as max_length from dual) b
	on (a.id=b.id)
	when not matched then
		insert (id, ref_book_id, name, alias, type, ord, visible, width, required, is_unique, read_only, precision, max_length)
		values (b.id, b.ref_book_id, b.name, b.alias, b.type, b.ord, b.visible, b.width, b.required, b.is_unique, b.read_only, b.precision, b.max_length);
		
	update ref_book set table_name='SEC_ROLE_NDFL' where id=95;
	
	update ref_book_asnu set priority=case when code='1000' then 900
												   when code='2000' then 800
												   when code='3000' then 600
												   when code='4000' then 500
												   when code='5000' then 700
												   when code='6000' then 400
												   when code='6001' then 400
												   when code='7000' then 300
												   when code='6002' then 400
												   when code='6003' then 400
												   when code='6004' then 400
												   when code='6005' then 400
												   when code='1001' then 900
												   when code='8000' then 200
												   when code='9000' then 100
												   else 999 end;
END;
/