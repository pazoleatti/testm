-- 3.9-adudenko-2

declare 
  v_task_name varchar2(128):='insert_update_delete block #1 - merge into ref_book_doc_state';  
begin
	merge into ref_book_doc_state dst using
	(select 9 as id, 'Отправлен в ЭДО' as name from dual 
	) src
	on (src.id=dst.id)
	when not matched then
		insert (id, name)
		values (src.id, src.name)
	when matched then
		update set dst.name=src.name;
	
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

-- 3.9-adudenko-5

declare 
  v_task_name varchar2(128):='insert_update_delete block #2 - merge into ref_book';  
begin
	update ref_book set xsd_id='448c3350-d00e-45ff-afda-2d2388d43808' where id=207;
	
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
