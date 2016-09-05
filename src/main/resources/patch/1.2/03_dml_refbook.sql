set serveroutput on size 1000000;
set linesize 128;
---------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-16816: 1.2 ЗемНалог. Изменить справочник "Коды налоговых льгот земельного налога"
declare 
	l_task_name varchar2(128) := 'RefBook Block #1 (SBRFACCTAX-16816 - Tax concession codes(L): hierarchical to lineal)';
	l_rerun_condition decimal(1) := 0;
begin
	select type into l_rerun_condition from ref_book where id=704;
	
	if l_rerun_condition = 1 then --still hierarchical
			delete from ref_book_value where attribute_id = 7044;
			delete from ref_book_attribute where id = 7044;
			update ref_book set type=0 where id = 704;	
	
	else
		dbms_output.put_line(l_task_name||'[INFO]:'||' changes to the ref_book had already been made');
	end if;
	
	dbms_output.put_line(l_task_name||'[INFO]: Success');	
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(l_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;
---------------------------------------------------------------------------
COMMIT;
EXIT;