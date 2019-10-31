
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='indexes block #1  - idx_ref_book_cal_datetype';  
BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME='IDX_REF_BOOK_CAL_DATETYPE';
	if (v_run_condition =0) then
        execute immediate 'create unique index idx_ref_book_cal_datetype on ref_book_calendar (cdate asc, ctype asc)';
	dbms_output.put_line(v_task_name||'[INFO ]:'||' Success');
	end if;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
commit;

