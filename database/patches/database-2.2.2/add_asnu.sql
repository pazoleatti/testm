declare
	v_count1 number;
	v_count2 number;
	v_id1 number;
	v_id2 number;
begin
	select count(1) into v_count1 from sec_role where alias='N_ROLE_OPER_1002';
	select count(1) into v_count2 from sec_role where alias='N_ROLE_OPER_9001';
	if v_count1>0 and v_count2>0 then
		select id into v_id1 from sec_role where alias='N_ROLE_OPER_1002';
		select id into v_id2 from sec_role where alias='N_ROLE_OPER_9001';
		merge into ref_book_asnu a using
		(select 16 as id, '1002' as code, 'Выплаты арендной платы физлицу_АС «SAP»' as name, 
		'Выплаты арендной платы физическому лицу' as type, v_id1 as role_alias, v_id1 as role_name, 900 as priority from dual
		union all
		select 17 as id, '9001' as code, 'Выплаты клиентам по заключениям ЦЗК_АС «ЕКС»' as name, 
		'Выплаты клиентам по заключениям Центра заботы о клиентах' as type, v_id2 as role_alias, v_id2 as role_name, 100 as priority from dual) b
		on (a.id=b.id)
		when not matched then
		insert(id, code, name, type, role_alias, role_name, priority)
		values(b.id, b.code, b.name, b.type, b.role_alias, b.role_name, b.priority);
		commit;
	else
		if v_count1=0 then 
			dbms_output.put_line('Role N_ROLE_OPER_1002 not exists');
		end if;
		if v_count2=0 then 
			dbms_output.put_line('Role N_ROLE_OPER_9001 not exists');
		end if;
	end if;
end;
/
