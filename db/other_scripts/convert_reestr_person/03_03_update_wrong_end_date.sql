declare 
	v_task_name varchar2(128):='update_wrong_end_date block #1 - update REF_BOOK_PERSON end_date = null';  
begin
	update REF_BOOK_PERSON p
	set p.end_date = null
	where 
	p.status=0 and 
	p.start_date is not null and
	p.end_date is not null and
	p.start_date > p.end_date;
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success updated '||SQL%ROWCOUNT||' rows');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;
