
-- 3.6-amandzyak-1 https://jira.aplana.com/browse/SBRFNDFL-7132 - Добавление нового Справочника Подсистем
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='Create synonyms for subsystem';  
BEGIN
	EXECUTE IMMEDIATE 'CREATE OR REPLACE SYNONYM SUBSYSTEM_SYN FOR '||:NSI_USER||'.SUBSYSTEM';
	EXECUTE IMMEDIATE 'CREATE OR REPLACE SYNONYM VW_SUBSYSTEM_SYN FOR '||:NSI_USER||'.VW_SUBSYSTEM';
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');	
	
EXCEPTION
	WHEN OTHERS THEN 
		dbms_output.put_line(v_task_name||'[Error]:'||' Can''t create synonyms for SUBSYSTEM');
END;
/
