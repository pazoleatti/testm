declare 
	v_task_name varchar2(128):='ref_book_person_start_date block #1 - update ref_book_person start_date from version';  
	v_run_condition1 number(1) := 0;
	v_run_condition2 number(1) := 0;
begin
	select count(*) into v_run_condition1 from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='start_date';
	select count(*) into v_run_condition2 from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='version';
	IF v_run_condition1=1 AND v_run_condition2=1 THEN
		EXECUTE IMMEDIATE 'update ref_book_person set start_date=version where start_date is null and version is not null';
		
		CASE SQL%ROWCOUNT 
		WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
		ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
		END CASE; 
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||CASE WHEN v_run_condition1=0 THEN ' Column "start_date" not found' ELSE '' END
		||CASE WHEN v_run_condition2=0 THEN ' Column "version" not found' ELSE '' END);
	END IF;
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;
