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
--3.10-skononova-1
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #2 - alter table transport_message';  
BEGIN
	select count(*) into v_run_condition from user_constraints where 
		TABLE_NAME='TRANSPORT_MESSAGE' and CONSTRAINT_NAME='TMESS_DECLARATION_ID_FK';
	IF v_run_condition>=1 THEN
           	execute immediate 'ALTER TABLE transport_message DROP  CONSTRAINT tmess_declaration_id_fk';
	END IF;
	execute immediate ' ALTER TABLE transport_message ADD  CONSTRAINT tmess_declaration_id_fk FOREIGN KEY (declaration_id)
        	REFERENCES declaration_data (id) ON DELETE CASCADE ENABLE';
		dbms_output.put_line(v_task_name||'[INFO (transport_message)]:'||' Success');

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

commit;
--3.10-mchernyakov-03

DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #3 - alter table log_entry';  
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