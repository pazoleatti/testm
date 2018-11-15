declare 
	v_task_name varchar2(128):='update_wrong_end_date block #1 - update REF_BOOK_PERSON end_date = null';  
	v_exist_status number(1);
	v_exist_start_date number(1);
	v_exist_end_date number(1);
begin
	select count(*) into v_exist_status from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='status';
	select count(*) into v_exist_start_date from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='start_date';
	select count(*) into v_exist_end_date from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='end_date';
	IF v_exist_status=1 AND v_exist_start_date=1 AND v_exist_end_date=1 THEN
		EXECUTE IMMEDIATE 'update REF_BOOK_PERSON p
		set p.end_date = null
		where 
		p.status=0 and 
		p.start_date is not null and
		p.end_date is not null and
		p.start_date > p.end_date';
		
		CASE SQL%ROWCOUNT 
		WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
		ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success updated '||SQL%ROWCOUNT||' rows');
		END CASE; 
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||CASE WHEN v_exist_status=0 THEN ' Column "status" not found' ELSE '' END
		||CASE WHEN v_exist_start_date=0 THEN ' Column "start_date" not found' ELSE '' END
		||CASE WHEN v_exist_end_date=0 THEN ' Column "end_date" not found' ELSE '' END);
	END IF;
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;
