create or replace PROCEDURE RESERVE_TASK 
(
--приоритетная нода для запуска
  PRIOR_NODE IN VARCHAR2 
--текущая нода, на которой будет запущена задача
, CURRENT_NODE IN VARCHAR2
--Тип очереди, в которую помещена задача. 1 - короткие, 2 - длинные  
, QUEUE_TYPE IN NUMBER 
--таймаут для выполнения задачи
, TIMEOUT_HOURS IN NUMBER 
--максимальное количество задач на ноде
, MAX_TASKS_PER_NODE IN NUMBER 
--тип запроса для выбора следующей задачи для исполнения: 1 - с учетом task_group, 2 - без учета
, SELECT_QUERY_TYPE IN NUMBER 
--выходной параметр - ID задачи
, TASK_ID OUT NUMBER 
)  IS PRAGMA AUTONOMOUS_TRANSACTION;

st_cursor SYS_REFCURSOR;
BEGIN

    if (SELECT_QUERY_TYPE = 1) then
        open st_cursor for
        select id  from async_task
        where ((PRIOR_NODE is null and priority_node is null) or (PRIOR_NODE is not null and priority_node = PRIOR_NODE)) and 
        queue = QUEUE_TYPE and (node is null or current_timestamp > start_process_date + TIMEOUT_HOURS * (interval '1' hour)) and 
        (task_group is null or (task_group is not null and task_group not in (select task_group from async_task where start_process_date is not null and task_group is not null))) 
        order by create_date
        for update skip locked;
    elsif (SELECT_QUERY_TYPE = 2) then
        open st_cursor for select id from  async_task 
        where ((PRIOR_NODE is null and priority_node is null) or (PRIOR_NODE is not null and priority_node = PRIOR_NODE)) and 
        queue = QUEUE_TYPE and (node is null or current_timestamp > start_process_date + TIMEOUT_HOURS * (interval '1' hour))
        order by create_date
        for update skip locked;
    else
        TASK_ID :=null;
        return;
    end if; 
    fetch st_cursor into TASK_ID;
    close st_cursor;
    if TASK_ID is not null then    
        UPDATE async_task SET node = CURRENT_NODE, state_date = current_timestamp, start_process_date = current_timestamp 
        WHERE (SELECT count(*) FROM async_task WHERE node = CURRENT_NODE AND queue = QUEUE_TYPE) < MAX_TASKS_PER_NODE AND id = TASK_ID;
        if (sql%rowcount=0) then
            TASK_ID :=null;
        end if;    
    end if;
    COMMIT;
EXCEPTION
    WHEN others then
        TASK_ID := null;
        commit;    
END RESERVE_TASK;
