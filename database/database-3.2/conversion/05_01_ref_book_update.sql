declare 
	v_task_name varchar2(128):='ref_book_update block #1 - update REF_BOOK_ID_DOC person_id = null';  
begin
	update REF_BOOK_ID_DOC a 
	set a.person_id = null
	where 
	exists (select 1 from REF_BOOK_PERSON p where p.status in (-1,1) and p.id=a.person_id);
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare 
	v_task_name varchar2(128):='ref_book_update block #2 - update DECLARATION_DATA_PERSON person_id = null';  
begin
	update DECLARATION_DATA_PERSON b 
	set b.person_id = null
	where 
	exists (select 1 from REF_BOOK_PERSON p where p.status in (-1,1) and p.id=b.person_id);
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare 
	v_task_name varchar2(128):='ref_book_update block #3 - update NDFL_PERSON person_id = null';  
begin
	update NDFL_PERSON c
	set c.person_id = null
	where 
	exists (select 1 from REF_BOOK_PERSON p where p.status in (-1,1) and p.id=c.person_id);
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare 
	v_task_name varchar2(128):='ref_book_update block #4 - update NDFL_REFERENCES person_id = null';  
begin
	update NDFL_REFERENCES d  
	set d.person_id = null
	where 
	exists (select 1 from REF_BOOK_PERSON p where p.status in (-1,1) and p.id=d.person_id);
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare 
	v_task_name varchar2(128):='ref_book_update block #5 - update REF_BOOK_ID_TAX_PAYER person_id = null';  
begin
	update REF_BOOK_ID_TAX_PAYER e 
	set e.person_id = null
	where 
	exists (select 1 from REF_BOOK_PERSON p where p.status in (-1,1) and p.id=e.person_id);
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare 
	v_task_name varchar2(128):='ref_book_update block #6 - update REF_BOOK_PERSON_TB person_id = null';  
begin
	update REF_BOOK_PERSON_TB f 
	set f.person_id = null
	where 
	exists (select 1 from REF_BOOK_PERSON p where p.status in (-1,1) and p.id=f.person_id);
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare 
	v_task_name varchar2(128):='ref_book_update block #7 - update REF_BOOK_PERSON report_doc = null';  
begin
	update REF_BOOK_PERSON p 
	set p.report_doc = null
	where 
	exists (select 1 from REF_BOOK_ID_DOC g where g.status<>0 and g.id=p.report_doc);
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;
