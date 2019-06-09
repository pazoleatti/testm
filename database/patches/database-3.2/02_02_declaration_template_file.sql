PROMPT Fill DECLARATION_TEMPLATE_FILE... 

DECLARE
  l_task_name varchar2(128) := 'DECLARATION_TEMPLATE_FILE Block #1 (SBRFNDFL-5779)';
  l_run_condition decimal(1) := 0;
BEGIN
  
  -- https://jira.aplana.com/browse/SBRFNDFL-5779 Реализовать формирование уведомлений о неудержанном налоге-->    
  SELECT CASE WHEN count(*) = 1 THEN 0 ELSE 1 END INTO l_run_condition FROM declaration_template_file dtf 
  WHERE dtf.declaration_template_id = 101 AND dtf.blob_data_id = 'c1e2aa59-98f8-4234-b3ea-09defcc9e52b';
  
  IF l_run_condition = 1 THEN 
    INSERT INTO declaration_template_file (declaration_template_id, blob_data_id)
    VALUES (101, 'c1e2aa59-98f8-4234-b3ea-09defcc9e52b');

    dbms_output.put_line(l_task_name||'[INFO]:'||' Success ');
  ELSE
    dbms_output.put_line(l_task_name||'[WARNING]:'||' changes had already been implemented');
  END IF;

  -- https://jira.aplana.com/browse/SBRFNDFL-5779 Реализовать формирование уведомлений о неудержанном налоге-->    
  l_task_name := 'DECLARATION_TEMPLATE_FILE Block #2 (SBRFNDFL-5779)';
  SELECT CASE WHEN count(*) = 1 THEN 0 ELSE 1 END INTO l_run_condition FROM declaration_template_file dtf 
  WHERE dtf.declaration_template_id = 101 AND dtf.blob_data_id = '7eb666b4-0ad7-44a1-9288-f27d0200388d';
  
  IF l_run_condition = 1 THEN 
    INSERT INTO declaration_template_file (declaration_template_id, blob_data_id)
    VALUES (101, '7eb666b4-0ad7-44a1-9288-f27d0200388d');

    dbms_output.put_line(l_task_name||'[INFO]:'||' Success ');
  ELSE
    dbms_output.put_line(l_task_name||'[WARNING]:'||' changes had already been implemented');
  END IF;

  -- https://jira.aplana.com/browse/SBRFNDFL-5779 Реализовать формирование уведомлений о неудержанном налоге-->    
  l_task_name := 'DECLARATION_TEMPLATE_FILE Block #3 (SBRFNDFL-5779)';
  SELECT CASE WHEN count(*) = 1 THEN 0 ELSE 1 END INTO l_run_condition FROM declaration_template_file dtf 
  WHERE dtf.declaration_template_id = 101 AND dtf.blob_data_id = 'f103841c-b850-428b-a6af-3b4c38f90c81';
  
  IF l_run_condition = 1 THEN 
    INSERT INTO declaration_template_file (declaration_template_id, blob_data_id)
    VALUES (101, 'f103841c-b850-428b-a6af-3b4c38f90c81');

    dbms_output.put_line(l_task_name||'[INFO]:'||' Success ');
  ELSE
    dbms_output.put_line(l_task_name||'[WARNING]:'||' changes had already been implemented');
  END IF;

  -- https://jira.aplana.com/browse/SBRFNDFL-5779 Реализовать формирование уведомлений о неудержанном налоге-->    
  l_task_name := 'DECLARATION_TEMPLATE_FILE Block #4 (SBRFNDFL-5779)';
  SELECT CASE WHEN count(*) = 1 THEN 0 ELSE 1 END INTO l_run_condition FROM declaration_template_file dtf 
  WHERE dtf.declaration_template_id = 101 AND dtf.blob_data_id = '576112c6-5d87-42c4-8073-9e6c414af82f';
  
  IF l_run_condition = 1 THEN 
    INSERT INTO declaration_template_file (declaration_template_id, blob_data_id)
    VALUES (101, '576112c6-5d87-42c4-8073-9e6c414af82f');

    dbms_output.put_line(l_task_name||'[INFO]:'||' Success ');
  ELSE
    dbms_output.put_line(l_task_name||'[WARNING]:'||' changes had already been implemented');
  END IF;

  -- https://jira.aplana.com/browse/SBRFNDFL-5779 Реализовать формирование уведомлений о неудержанном налоге-->    
  l_task_name := 'DECLARATION_TEMPLATE_FILE Block #5 (SBRFNDFL-5779)';
  SELECT CASE WHEN count(*) = 1 THEN 0 ELSE 1 END INTO l_run_condition FROM declaration_template_file dtf 
  WHERE dtf.declaration_template_id = 101 AND dtf.blob_data_id = '20f4a5ca-0312-4f95-971f-9909f4ec6236';
  
  IF l_run_condition = 1 THEN 
    INSERT INTO declaration_template_file (declaration_template_id, blob_data_id)
    VALUES (101, '20f4a5ca-0312-4f95-971f-9909f4ec6236');

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

PROMPT OK.
