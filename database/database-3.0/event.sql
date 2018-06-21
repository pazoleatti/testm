-----------------------------------------------------------------------------------------------------------------------------
--> 

DECLARE
  l_task_name varchar2(128) := 'EVENT Block #1 ADD id = 30';
  l_run_condition decimal(1) := 0;
BEGIN
  
  SELECT CASE WHEN count(*) = 1 THEN 0 ELSE 1 END INTO l_run_condition FROM event WHERE id = 30;
  
  IF l_run_condition = 1 THEN 
    
    INSERT INTO event (id, name)
    VALUES (30, 'Формирование документа');
  
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
--> 

DECLARE
  l_task_name varchar2(128) := 'EVENT Block #2 ADD id = 31';
  l_run_condition decimal(1) := 0;
BEGIN
    
  SELECT CASE WHEN count(*) = 1 THEN 0 ELSE 1 END INTO l_run_condition FROM event WHERE id = 31;
  
  IF l_run_condition = 1 THEN 
    
    INSERT INTO event (id, name)
    VALUES (31, 'Создание Приложения 2 к декларации НП');
  
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
