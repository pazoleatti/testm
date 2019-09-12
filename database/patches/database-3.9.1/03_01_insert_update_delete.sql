-- 3.9.1-adudenko-1

declare 
  v_task_name varchar2(128):='insert_update_delete block #1 - update async_task_type';  
begin
        update async_task_type set limit_kind = 'Количество строк в любом разделе НФ', 
				   task_limit = 1000000 
				where id = 26;
	
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

