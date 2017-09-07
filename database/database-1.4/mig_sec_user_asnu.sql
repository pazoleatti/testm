declare 
  v_count number;
BEGIN
	select count(1) into v_count from user_tables where table_name='SEC_USER_ASNU';
	if v_count>0 then
		execute immediate '
		merge into sec_user_role a using
		(select sua.user_id, rba.role_alias as role_id from sec_user_asnu sua JOIN ref_book_asnu rba on sua.asnu_id=rba.id) b
		on (a.user_id=b.user_id and a.role_id=b.role_id)
		when not matched then
			insert (user_id, role_id)
			values (b.user_id, b.role_id)';

		execute immediate 'drop table sec_user_asnu';
	end if;
END;
/