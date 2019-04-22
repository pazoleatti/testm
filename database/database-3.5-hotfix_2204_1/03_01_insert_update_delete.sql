-- 3.5-dnovikov-18
declare
	v_task_name varchar2(128):='insert_update_delete block #1 - update declaration_template_file';  
begin	
	
    update declaration_template_file set BLOB_DATA_ID = '7833e689-c60b-4a1b-98be-b181079d0c29'
			where DECLARATION_TEMPLATE_ID=101 and BLOB_DATA_ID='4b85f92c-7fd0-4d67-834d-e61g34684336';

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
