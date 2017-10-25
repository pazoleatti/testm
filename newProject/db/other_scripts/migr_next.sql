  INSERT INTO tax.department(id, name, parent_id, type, shortname, tb_index, sbrf_code, region_id, is_active, code, garant_use, sunr_use)
  SELECT id, name, parent_id, type, shortname, tb_index, sbrf_code, region_id, is_active, code, garant_use, sunr_use
  FROM ndfl_next.department where id not in (select id from tax.department) AND id!=-1;
  
  insert into tax.sec_user(id, login, name, department_id, is_active, email)
  SELECT id, login, name, department_id, is_active, email from ndfl_next.sec_user where id not in (SELECT id from tax.sec_user);

  merge into tax.sec_user_role a using
		(select sua.user_id, 
  case when code='0000' then 35 
       when code='1000' then 20 
       when code='2000' then 21 
       when code='3000' then 22 
       when code='4000' then 23 
       when code='5000' then 24 
       when code='6000' then 25 
       when code='6001' then 26 
       when code='7000' then 27 
       when code='6002' then 28 
       when code='6003' then 29 
       when code='6004' then 30 
       when code='6005' then 31 
       when code='1001' then 32 
       when code='8000' then 33 
       when code='9000' then 34
  end as role_id 
  from ndfl_next.sec_user_asnu sua JOIN ndfl_next.ref_book_asnu rba on sua.asnu_id=rba.id) b
		on (a.user_id=b.user_id and a.role_id=b.role_id)
		when not matched then
			insert (user_id, role_id)
			values (b.user_id, b.role_id);

    merge into TAX.sec_user_role a USING
    (select user_id, CASE WHEN c.ROLE_ID=14 THEN 5 ELSE c.ROLE_ID END AS role_id FROM ndfl_next.sec_user_role c) b
    ON (a.user_id=b.user_id and a.role_id=b.role_id)
    when not matched then
			insert (user_id, role_id)
			values (b.user_id, b.role_id);

