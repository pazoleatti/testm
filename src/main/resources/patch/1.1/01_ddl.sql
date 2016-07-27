set serveroutput on size 1000000;

-----------------------------------------------------------------------------------------------------------------------------
declare 
	l_task_name varchar2(128) := 'DDL Block #0 - 1.0 rerun ';
	l_rerun_condition decimal(1) := 0;
begin
	select count(*) into l_rerun_condition from user_indexes where index_name='FORM_TYPE_UNIQ_CODE';
	
	--part1: https://jira.aplana.com/browse/SBRFACCTAX-14930
	if l_rerun_condition = 1 then 
		execute immediate 'drop index form_type_uniq_code';
	
		dbms_output.put_line(l_task_name||'[INFO]:'||' index FORM_TYPE_UNIQ_CODE dropped');
	else
		dbms_output.put_line(l_task_name||'[INFO]:'||' index FORM_TYPE_UNIQ_CODE no longer exists');
	end if;
	
	select count(*) into l_rerun_condition from user_cons_columns cc join user_constraints c on c.constraint_name = cc.constraint_name join user_tab_columns tc on tc.TABLE_NAME = cc.table_name and tc.COLUMN_NAME=cc.column_name where cc.table_name='FORM_DATA_REPORT' and cc.column_name='TYPE' and c.constraint_type = 'C';
	
	--part2: http://jira.aplana.com/browse/SBRFACCTAX-13912
	if l_rerun_condition = 0 then 
		execute immediate 'alter table form_data_report modify type not null';
	
		dbms_output.put_line(l_task_name||'[INFO]:'||' column FORM_DATA_REPORT.TYPE made explicitly not null');
	else
		dbms_output.put_line(l_task_name||'[INFO]:'||' column FORM_DATA_REPORT.TYPE was already explicitly not null');
	end if;
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(l_task_name||'[FATAL]:'||sqlerrm);
end;
/
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

--https://jira.aplana.com/browse/SBRFACCTAX-16253: 1.1 БД. Изменения в ЖА. Добавить столбец "Тип форм" 
declare 
	l_task_name varchar2(128) := 'DDL Block #3 (SBRFACCTAX-16253)';
	l_rerun_condition decimal(1) := 0;
	l_sql_query varchar2(4000) := '';
begin
	select count(*) into l_rerun_condition from user_tables where table_name = 'AUDIT_FORM_TYPE';
	
	if l_rerun_condition = 0 then 
		execute immediate 'create table audit_form_type (id number(9,0) not null, name varchar2(1000) not null)';
		execute immediate 'alter table audit_form_type add constraint audit_form_type_pk primary key (id)';
		execute immediate 'comment on table audit_form_type is ''Типы форм для журнала аудита''';
		execute immediate 'comment on column audit_form_type.id is ''Код записи''';
		execute immediate 'comment on column audit_form_type.name is ''Наименование типа''';
		
		execute immediate 'alter table log_system add audit_form_type_id number(9,0)';
		execute immediate 'comment on column log_system.audit_form_type_id is ''Тип формы''';
		execute immediate 'alter table log_system add constraint log_system_fk_audit_form_type foreign key (audit_form_type_id) references audit_form_type (id)';
		
		execute immediate 'insert into audit_form_type (id, name) select 1, ''Налоговая форма'' from dual union select 2, ''Декларация'' from dual union select 3, ''Версия макета нф'' from dual union select 4, ''Версия макета декларации'' from dual union select 5, ''Форма 101'' from dual union select 6, ''Форма 102'' from dual';	
			  
		l_sql_query := 'update log_system ls 
						set audit_form_type_id = 
						  case 
							when ls.form_type_name is not null 
								then ( 
									  case 
										when (instr(ls.note, ''Импорт бухгалтерской отчётности: '') > 0 AND (instr(ls.form_type_name, ''Вид бух. отчетности - Форма 101'') > 0 OR instr(ls.form_type_name, ''Вид бух. отчетности - Форма 102'')   > 0)) 
											then (case when instr(ls.form_type_name, ''Вид бух. отчетности - Форма 101'') > 0 then 5 else 6 end)
										when ls.department_name is not null then 1 else 3 
										end 
									) 
							when ls.declaration_type_name is not null 
								then (case when ls.department_name is not null then 2 else 4 end) 
						  end 
						where ls.form_type_name is not null or ls.declaration_type_name is not null';
		
		execute immediate l_sql_query;	
		execute immediate 'update log_system set form_type_name = null where audit_form_type_id in (5,6)';	
		execute immediate 'alter table log_system add constraint log_system_chk_aft check (audit_form_type_id = 1 and not event_id in (701,702,703,704,705,904) and form_type_name is not null and department_name is not null or audit_form_type_id = 2 and not event_id in (701,702,703,704,705,904) and declaration_type_name is not null and department_name is not null or audit_form_type_id = 3 and event_id in (701,702,703,704,705,904) and form_type_name is not null and department_name is null or audit_form_type_id = 4 and event_id in (701,702,703,704,705,904) and declaration_type_name is not null and department_name is null or audit_form_type_id in (5,6) and event_id in (7) and form_type_name is null and declaration_type_name is null or audit_form_type_id is null)';
		dbms_output.put_line(l_task_name||'[INFO]:'||' table AUDIT_FORM_TYPE created');
	else
		dbms_output.put_line(l_task_name||'[ERROR]:'||' table AUDIT_FORM_TYPE already exists');
	end if;
EXCEPTION
	when OTHERS then
		dbms_output.put_line(l_task_name||'[FATAL]:'||sqlerrm);
end;
/

-----------------------------------------------------------------------------------------------------------------------------

COMMIT;
EXIT;