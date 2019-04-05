set serveroutput on;

declare 
	v_count number;
begin
	select count(1) into v_count from user_tables where table_name='TMP_DELETE_RB_PERSON';
	if v_count > 0 then 
		execute immediate 'drop table TMP_DELETE_RB_PERSON';
		dbms_output.put_line('Table TMP_DELETE_RB_PERSON was dropped.');	
	end if;	
end;
/

exit;