PROMPT Fill DECLARATION_TEMPLATE_FILE... 


-----------------------------------------------------------------------------------------------------------------------------
--> 

DECLARE
  l_task_name varchar2(128) := 'DECLARATION_TEMPLATE_FILE Block #1';
  l_run_condition decimal(1) := 0;
BEGIN
  
    
  SELECT CASE WHEN count(*) = 1 THEN 0 ELSE 1 END INTO l_run_condition FROM declaration_template_file dtf WHERE dtf.declaration_template_id = 104 AND dtf.blob_data_id = 'efde6350-c417-4d12-aca1-bc268d9490fb';

  
  IF l_run_condition = 1 THEN 
    INSERT INTO declaration_template_file (declaration_template_id, blob_data_id)
    VALUES (104, 'efde6350-c417-4d12-aca1-bc268d9490fb');

    dbms_output.put_line(l_task_name||'[INFO]:'||' Success ');
  ELSE
    dbms_output.put_line(l_task_name||'[WARNING]:'||' changes had already been implemented');
  END IF;

EXCEPTION
  when OTHERS then
    dbms_output.put_line(l_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
END;
/
COMMIT;

-----------------------------------------------------------------------------------------------------------------------------

PROMPT OK.
