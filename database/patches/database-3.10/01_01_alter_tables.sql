-- 3.10-adudenko-01, 3.9.2-adudenko-02
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #1 - alter table report_period';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where TABLE_NAME='REPORT_PERIOD' and COLUMN_NAME='FORM_TYPE_ID';
	IF v_run_condition=1 THEN
           	execute immediate 'alter table report_period add form_type_id number(1)';
                execute immediate 'comment on column report_period.form_type_id is ''Вид налоговой формы''';
		execute immediate 'alter table report_period add constraint rp_fk_ref_book_form_type_id
					foreign key (form_type_id) references ref_book_form_type (id) on delete cascade';
		dbms_output.put_line(v_task_name||'[INFO (form_type)]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING (form_type)]:'||' changes had already been implemented');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
commit;
--3.10-mchernyakov-03

DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #2 - alter table log_entry';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where 
			TABLE_NAME='LOG_ENTRY' and COLUMN_NAME='PERIOD';
	IF v_run_condition=1 THEN
           	execute immediate 'ALTER TABLE LOG_ENTRY ADD (PERIOD VARCHAR2(50 CHAR))';
                execute immediate 'COMMENT ON COLUMN LOG_ENTRY.PERIOD IS ''Период''';
		dbms_output.put_line(v_task_name||'[INFO (period)]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING (period)]:'||' changes had already been implemented');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
commit;
/