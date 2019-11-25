-- 3.11-avoynov-1 3.11-avoynov-5
declare 
  v_task_name varchar2(128):='insert_update_delete block #3 - merge into configuration';  
begin
	merge into configuration dst using
	(select 'DEPARTMENT_FOR_APP_2' as code, 0 as department_id, 113 as value from dual 
	) src
	on (src.code = dst.code and src.department_id = dst.department_id)
	when not matched then
		insert (code, department_id, value)
		values (src.code, src.department_id, src.value);

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


-- 3.11-adudenko-02

declare 
  v_task_name varchar2(128):='insert_update_delete block #4 - merge into ref_book_doc_state';  
begin
	merge into ref_book_doc_state dst using
	(select 10 as id, 'Не отправлен в НП' as name from dual union
	 select 11, 'Выгружен для отправки в НП' from dual union
	 select 12, 'Загружен в НП' from dual	
	) src
	on (src.id=dst.id)
	when not matched then
		insert (ID, NAME)
		values (src.ID, src.NAME)
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

