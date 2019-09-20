--3.9.2-skononova-1
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='grants block #1 - revoke privs from NSI';  
BEGIN
	select count(*) into v_run_condition from user_tab_privs where table_name='VW_DEPARTMENT_CONFIG'
			and grantee = '&1';
	IF v_run_condition>0 THEN
           	execute immediate 'revoke select on vw_department_config from &1 ';
		dbms_output.put_line(v_task_name||'[INFO (revoke)]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING (revoke)]:'||' changes had already been implemented');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='grants block #2 add grants to TAXREC';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_privs where table_name='VW_DEPARTMENT_CONFIG'
			and grantee = '&2';

	IF v_run_condition=1 THEN
           	execute immediate 'grant select on vw_department_config to &2';
		dbms_output.put_line(v_task_name||'[INFO (grants)]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING (grants)]:'||' changes had already been implemented');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

commit;
/