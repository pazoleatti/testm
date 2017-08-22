prompt drop constraint LOG_SYSTEM_FK_USER_LOGIN...
declare 
  v_count number;
BEGIN
  select count(1) into v_count from user_constraints where constraint_name='LOG_SYSTEM_FK_USER_LOGIN';
  if v_count>0 then
    execute immediate 'alter table LOG_SYSTEM drop constraint LOG_SYSTEM_FK_USER_LOGIN';
    dbms_output.put_line('Constraint LOG_SYSTEM_FK_USER_LOGIN droped.');	
	else
		dbms_output.put_line('Constraint LOG_SYSTEM_FK_USER_LOGIN not exists');
	end if;	
END;
/