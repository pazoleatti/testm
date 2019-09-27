--3.10-adudenko-01
declare 
  v_task_name varchar2(128):='insert_update_delete block #1 - merge into ref_book_form_type';  
begin

	merge into ref_book_form_type dst using
	(select 7 as id, 'Приложение 2' as code, 'Приложение 2' as name from dual 
	) src
	on (src.id=dst.id)
	when not matched then
		insert (id, code, name)
		values (src.id, src.code, src.name)
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
