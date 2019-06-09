
-----------------------------------------------------------------------------------------------------------------------------
--> 

DECLARE
  l_task_name varchar2(128) := 'ASYNC_TASK_TYPE Block #1 ADD id = 35';
  l_run_condition decimal(1) := 0;
BEGIN
  
  SELECT CASE WHEN count(*) = 1 THEN 0 ELSE 1 END INTO l_run_condition FROM async_task_type WHERE id = 35;
  
  IF l_run_condition = 1 THEN 
    
    INSERT INTO async_task_type (id, name, handler_bean, short_queue_limit, task_limit, limit_kind)
    VALUES (35, 'Формирования уведомления о задолженности', 'DeptNoticeDocAsyncTask', NULL, NULL, 'Количество ФЛ в НФ');

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
  l_task_name varchar2(128) := 'ASYNC_TASK_TYPE Block #2 ADD id = 36';
  l_run_condition decimal(1) := 0;
BEGIN
  
  SELECT CASE WHEN count(*) = 1 THEN 0 ELSE 1 END INTO l_run_condition FROM async_task_type WHERE id = 36;
  
  IF l_run_condition = 1 THEN 
    
    INSERT INTO async_task_type (id, name, handler_bean, short_queue_limit, task_limit, limit_kind)
    VALUES (36, 'Создание Приложения 2 к декларации НП', 'CreateApplication2AsyncTask', NULL, NULL, NULL);

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
