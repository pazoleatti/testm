
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='indexes block #1  - idx_ref_book_cal_datetype';  
BEGIN
	select decode(count(*),1,0,1) into v_run_condition from user_indexes where INDEX_NAME='IDX_REF_BOOKCAL_DATETYPE';
        execute immediate 'create unique index idx_ref_book_cal_datetype on ref_book_calendar (cdate asc, ctype asc)';
	dbms_output.put_line(v_task_name||'[INFO ]:'||' Success');

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
commit;

