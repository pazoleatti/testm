set serveroutput on size 1000000;
set linesize 128;

-----------------------------------------------------------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-16791: 1.2. Справочники. Ошибка при изменении кода подразделения
declare 
	l_task_name varchar2(128) := 'DDL Block #1 - Department''s code extension (SBRFACCTAX-16791)';
	l_rerun_condition decimal(1) := 0;
begin
	select case when data_precision < 15 then 1 else 0 end into l_rerun_condition from user_tab_columns where table_name = 'DEPARTMENT' and column_name = 'CODE';
	
	--part1: https://jira.aplana.com/browse/SBRFACCTAX-14930
	if l_rerun_condition = 1 then 
		execute immediate 'alter table department modify code number(15)';
	
		dbms_output.put_line(l_task_name||'[INFO]:'||' SUCCESS');
	else
		dbms_output.put_line(l_task_name||'[INFO]:'||' column DEPARTMENT.CODE had already been modified');
	end if;
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(l_task_name||'[FATAL]:'||sqlerrm);
end;
/
-----------------------------------------------------------------------------------------------------------------------------
COMMIT;
EXIT;