declare 
  v_count number;
BEGIN
	select count(1) into v_count from user_constraints where constraint_name='FK_DEPARTMENT_REGION_ID' and table_name='DEPARTMENT';
	if v_count>0 then
		execute immediate 'alter table DEPARTMENT drop constraint FK_DEPARTMENT_REGION_ID';
	end if;	
END;
/