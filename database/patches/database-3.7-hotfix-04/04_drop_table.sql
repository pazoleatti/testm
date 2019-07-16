--3.7.1-skononova-6
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='drop_table block #1';  
BEGIN
	select count(*) into v_run_condition from user_tables where table_name = 'KNO_TEMP';
	IF v_run_condition=1 THEN
		execute immediate 'drop table KNO_TEMP ';
		dbms_output.put_line(v_task_name||'[INFO ]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

COMMIT;

