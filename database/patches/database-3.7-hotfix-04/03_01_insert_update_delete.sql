declare 
  v_task_name varchar2(128):='insert_update_delete block #1 - update ref_book';  
begin

	update ref_book set visible=1 where upper(table_name)='REF_BOOK_TAX_INSPECTION';

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
	

EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

declare 
  v_task_name varchar2(128):='insert_update_delete block #2 - merge into ref_book_tax_inspection';  
begin
	merge into ref_book_tax_inspection dst using 
	    ( select code, name from kno_temp
	    )  src
	    on (dst.code=src.code )
	    when not matched then insert (id, code, name)
	    values (seq_ref_book_record.nextval, src.code, src.name)
	    when matched then update set dst.name=src.name where dst.name<>src.name;

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
	

EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

declare 
  v_task_name varchar2(128):='insert_update_delete block #3 - update ref_book_tax_inspection';  
begin

	delete from ref_book_tax_inspection where id=-1;

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
	

EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;
