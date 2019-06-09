-- https://jira.aplana.com/browse/SBRFNDFL-5066 Изучить причины высокого потребления памяти СУБД при консолидации НФ -->
declare 
	v_run_condition number(1);
	v_task_name varchar2(128):='01_03_create_tables block #1 - create table tmp_cons_data (SBRFNDFL-5066)';  
begin
	select decode(count(*),0,1,0) into v_run_condition from user_tables where lower(table_name)='tmp_cons_data';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'create global temporary table tmp_cons_data(
			operation_id varchar2(100 char),
			asnu_id number(18),
			inp varchar2(25 char),
			year number(4),
			period_code varchar2(2 char),
			correction_date date)
			on commit delete rows';			
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
	COMMIT;
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
end;
/