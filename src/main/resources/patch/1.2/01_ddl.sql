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
		dbms_output.put_line(l_task_name||'[ERROR]:'||' column DEPARTMENT.CODE had already been modified');
	end if;
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(l_task_name||'[FATAL]:'||sqlerrm);
end;
/
-----------------------------------------------------------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-17019: 1.2 БД. Добавить событие "удаление блокировки"
declare 
	l_task_name varchar2(128) := 'DDL Block #2 - New event code for locks'' deletion (SBRFACCTAX-17019)';
	l_rerun_condition decimal(1) := 0;
begin
	select count(*) into l_rerun_condition from event where id=960;
	
	if l_rerun_condition = 1 then 
		insert into event (id, name) values(960, 'Удаление блокировки');
		
		execute immediate 'alter table log_system drop constraint log_system_chk_rp';
		execute immediate 'alter table log_system add constraint log_system_chk_rp check (event_id in (7, 11, 401, 402, 501, 502, 503, 601, 650, 901, 902, 903, 810, 811, 812, 813, 820, 821, 830, 831, 832, 840, 841, 842, 850, 860, 701, 702, 703, 704, 705, 904, 951, 960) or report_period_name is not null) enable';
		execute immediate 'alter table log_system drop constraint log_system_chk_dcl_form';
		execute immediate 'alter table log_system add constraint log_system_chk_dcl_form check (event_id in (7, 11, 401, 402, 501, 502, 503, 601, 650, 901, 902, 903, 810, 811, 812, 813, 820, 821, 830, 831, 832, 840, 841, 842, 850, 860, 701, 702, 703, 704, 705, 904, 951, 960) or declaration_type_name is not null or (form_type_name is not null and form_kind_id is not null)) enable';
		
		dbms_output.put_line(l_task_name||'[INFO]:'||' SUCCESS');
	else
		dbms_output.put_line(l_task_name||'[ERROR]:'||' event.id had already been added');
	end if;
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(l_task_name||'[FATAL]:'||sqlerrm);
end;
/
-----------------------------------------------------------------------------------------------------------------------------
COMMIT;
EXIT;