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
--https://jira.aplana.com/browse/SBRFACCTAX-15555: SQLException при удалении подразделения, если существуют(существовали) ссылки в настройках подразделений
CREATE OR REPLACE TRIGGER DEPARTMENT_BEFORE_DELETE for delete on department
COMPOUND TRIGGER

    TYPE t_change_tab IS TABLE OF DEPARTMENT%ROWTYPE;
    g_change_tab  t_change_tab := t_change_tab();

  BEFORE EACH ROW IS
    vCurrentDepartmentID number(9) := :old.id;
    vCurrentDepartmentType number(9) := :old.type;
  BEGIN
    g_change_tab.extend;
    g_change_tab(g_change_tab.last).id      := :old.id;

    --Подразделение с типом "Банк"(type=1) не может быть удалено
		if vCurrentDepartmentType=1 then
		   raise_application_error(-20001, 'Подразделение с типом "Банк" не может быть удалено');
		end if;

  END BEFORE EACH ROW;

  AFTER STATEMENT IS
  vHasLinks number(9) := -1;
  vHasDescendant number(9) := -1;
  BEGIN

  FOR i IN 1 .. g_change_tab.count LOOP
    --Существуют дочерние подразделения
		select count(*) into vHasDescendant
		from department
		start with parent_id = g_change_tab(i).id
		connect by parent_id = prior id;

		if vHasDescendant != 0 then
		   raise_application_error(-20002, 'Подразделение, имеющее дочерние подразделения, не может быть удалено');
		end if;

    --Ссылочная целостность
		--FORM_DATA
		select count(*) into  vHasLinks from form_data fd
		join department_report_period drp on (drp.id = fd.department_report_period_id or drp.id = fd.comparative_dep_rep_per_id) and drp.department_id = g_change_tab(i).id;

		if vHasLinks !=0 then
		   raise_application_error(-20003, 'Подразделение не может быть удалено, если на него существует ссылка в FORM_DATA');
		end if;

		--DECLARATION_DATA
		select count(*) into  vHasLinks from declaration_data dd
		join department_report_period drp on drp.id = dd.department_report_period_id and drp.department_id = g_change_tab(i).id;

		if vHasLinks !=0 then
		   raise_application_error(-20004, 'Подразделение не может быть удалено, если на него существует ссылка в DECLARATION_DATA');
		end if;

		--SEC_USER
		select count(*) into  vHasLinks from sec_user where department_id = g_change_tab(i).id;

		if vHasLinks !=0 then
		   raise_application_error(-20005, 'Подразделение не может быть удалено, если на него существует ссылка в SEC_USER');
		end if;

     --REF_BOOK_VALUE
		select count(*) into vHasLinks from ref_book_value rbv
		join ref_book_attribute rba on rba.id = rbv.attribute_id and rba.reference_id = 30
		where rbv.reference_value = g_change_tab(i).id;

		if vHasLinks !=0 then
		   raise_application_error(-20006, 'Подразделение не может быть удалено, если на него существует ссылка в REF_BOOK_VALUE');
		end if;

	  --FORM_DATA_REF_BOOK
		select count(*) into vHasLinks
		from form_data_ref_book
		where ref_book_id = 30 and record_id = g_change_tab(i).id;

		if vHasLinks !=0 then
		   raise_application_error(-20006, 'Подразделение не может быть удалено, если на него существует ссылка в FORM_DATA_REF_BOOK');
		end if;

  END LOOP;

  g_change_tab.delete;
  END AFTER STATEMENT;
end DEPARTMENT_BEFORE_DELETE;
/

declare 
	l_task_name varchar2(128) := 'DDL Block #2 (SBRFACCTAX-15555)';
	l_rerun_condition decimal(1) := 0;
begin
	select count(*) into l_rerun_condition from user_indexes where index_name = 'I_FORM_DATA_COM_DEP_REP_ID';
	
	if l_rerun_condition = 0 then 
		execute immediate 'create index I_FORM_DATA_COM_DEP_REP_ID on FORM_DATA (COMPARATIVE_DEP_REP_PER_ID)';
		dbms_output.put_line(l_task_name||'[INFO]:'||' index I_FORM_DATA_COM_DEP_REP_ID on FORM_DATA created');
	else
		dbms_output.put_line(l_task_name||'[ERROR]:'||' index I_FORM_DATA_COM_DEP_REP_ID already exists');
	end if;
EXCEPTION
	when OTHERS then
		dbms_output.put_line(l_task_name||'[FATAL]:'||sqlerrm);
end;
/
-----------------------------------------------------------------------------------------------------------------------------


COMMIT;
EXIT;