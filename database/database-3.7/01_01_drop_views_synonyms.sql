-- 3.7-dnovikov-15
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='drop synonym department_config';  
BEGIN
	select count(*) into v_run_condition from user_synonyms where synonym_name='DEPARTMENT_CONFIG';
	IF v_run_condition>0 THEN
		EXECUTE IMMEDIATE 'drop synonym department_config';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
-- 3.7-dnovikov-16

DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='drop view vw_department_config';  
BEGIN
	select count(*) into v_run_condition from user_views where view_name='VW_DEPARTMENT_CONFIG';
	IF v_run_condition>0 THEN
		EXECUTE IMMEDIATE 'drop view vw_department_config';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/


COMMIT;

