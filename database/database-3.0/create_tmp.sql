-----------------------------------------------------------------------------------------------------------------------------
DECLARE 
  l_task_name varchar2(128) := 'create_tmp #1 - ADD TEMPORARY TABLE TMP_STRING_PAIRS';
  l_run_condition decimal(1) := 0;
  l_sql_query varchar2(4000) := ''; 
BEGIN
  
  SELECT CASE WHEN count(*) = 1 THEN 0 ELSE 1 END INTO l_run_condition FROM user_tables ut WHERE ut.table_name='TMP_STRING_PAIRS';

  IF l_run_condition = 1 THEN 
    --DDL

    EXECUTE IMMEDIATE ' CREATE GLOBAL TEMPORARY TABLE TMP_STRING_PAIRS(
                            STRING1 VARCHAR2(4000 BYTE),
                            STRING2 VARCHAR2(4000 BYTE)
                        ) ON COMMIT DELETE ROWS';
                      
    dbms_output.put_line(l_task_name||'[INFO]:'||' Success');
  ELSE
    dbms_output.put_line(l_task_name||'[WARNING]:'||' changes had already been implemented');
  END IF;

  
EXCEPTION
  when OTHERS then
    dbms_output.put_line(l_task_name||'[FATAL]:'||sqlerrm);
end;
/
-----------------------------------------------------------------------------------------------------------------------------

