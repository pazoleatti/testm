set serveroutput on;
set verify off;
set termout off;
spool &1;
declare
	v_task_name varchar2(128):='update DECL_TEMPLATE_CHECKS set IS_FATAL = 0 for CHECK_CODE = "003-0001-00002"';  
begin	
	
    update DECL_TEMPLATE_CHECKS 
	set IS_FATAL = 0
	where CHECK_CODE = '003-0001-00002'
	  and DECLARATION_TEMPLATE_ID is not null
	  and IS_FATAL <> 0;

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success updated ' || to_char(SQL%ROWCOUNT) || ' rows');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;
exit;
