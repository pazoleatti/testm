PROMPT Fill DECLARATION_TEMPLATE_FILE... 


-----------------------------------------------------------------------------------------------------------------------------
--> 

DECLARE
  l_task_name varchar2(128) := 'DECLARATION_TEMPLATE_FILE Block #1 (SBRFNDFL-5088)';
  l_run_condition decimal(1) := 0;
BEGIN
  
  -- https://jira.aplana.com/browse/SBRFNDFL-5088 Реализовать изменения в форматах чисел в шаблонах файлов Excel-->    
  SELECT CASE WHEN count(*) = 1 THEN 0 ELSE 1 END INTO l_run_condition FROM declaration_template_file dtf WHERE dtf.declaration_template_id = 100 AND dtf.blob_data_id = '4b85f92c-7fd0-4d67-834d-e61g34684336';
  
  IF l_run_condition = 1 THEN 
    INSERT INTO declaration_template_file (declaration_template_id, blob_data_id)
    VALUES (100, '4b85f92c-7fd0-4d67-834d-e61g34684336');

    dbms_output.put_line(l_task_name||'[INFO]:'||' Success ');
  ELSE
    dbms_output.put_line(l_task_name||'[WARNING]:'||' changes had already been implemented');
  END IF;

  -- https://jira.aplana.com/browse/SBRFNDFL-5088 Реализовать изменения в форматах чисел в шаблонах файлов Excel-->    
  l_task_name := 'DECLARATION_TEMPLATE_FILE Block #2 (SBRFNDFL-5088)';
  SELECT CASE WHEN count(*) = 1 THEN 0 ELSE 1 END INTO l_run_condition FROM declaration_template_file dtf WHERE dtf.declaration_template_id = 101 AND dtf.blob_data_id = '4b85f92c-7fd0-4d67-834d-e61g34684336';
  
  IF l_run_condition = 1 THEN 
    INSERT INTO declaration_template_file (declaration_template_id, blob_data_id)
    VALUES (101, '4b85f92c-7fd0-4d67-834d-e61g34684336');

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
