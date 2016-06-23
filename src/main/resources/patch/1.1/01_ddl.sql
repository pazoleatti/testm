set serveroutput on size 1000000;

-----------------------------------------------------------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-15969: 1.1 БД. Логирование изменения скриптов справочников
declare 
	l_task_name varchar2(128) := 'DDL Block #1 (SBRFACCTAX-15969)';
	l_rerun_condition decimal(1) := 0;
begin
	select count(*) into l_rerun_condition from user_tab_columns where table_name = 'TEMPLATE_CHANGES' and column_name = 'REF_BOOK_ID';
	
	if l_rerun_condition = 0 then 
		execute immediate 'alter table template_changes add ref_book_id number(9)';
		execute immediate 'comment on column template_changes.ref_book_id is ''Идентификатор справочника''';
		
		execute immediate 'alter table template_changes drop constraint template_changes_chk_template';
		execute immediate 'alter table template_changes add constraint template_changes_chk_template check ((form_template_id is not null and declaration_template_id is null and ref_book_id is null) or (form_template_id is null and declaration_template_id is not null and ref_book_id is null) or (form_template_id is null and declaration_template_id is null and ref_book_id is not null))';
		
		dbms_output.put_line(l_task_name||'[INFO]:'||' table TEMPLATE_CHANGES was successfully modified');
	else
		dbms_output.put_line(l_task_name||'[ERROR]:'||' changes to the table TEMPLATE_CHANGES had already been implemented');
	end if;
EXCEPTION
	when OTHERS then
		dbms_output.put_line(l_task_name||'[FATAL]:'||sqlerrm);
end;
/

-----------------------------------------------------------------------------------------------------------------------------

;

COMMIT;
EXIT;