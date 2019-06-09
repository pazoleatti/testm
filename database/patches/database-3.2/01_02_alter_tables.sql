--https://jira.aplana.com/browse/SBRFNDFL-5080 новые виды КНФ
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #1 - alter table declaration_data add knf_type_id ';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where lower(table_name)='declaration_data' and lower(column_name)='knf_type_id';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'ALTER TABLE declaration_data ADD knf_type_id NUMBER(9)';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
	v_task_name:='alter_tables block #2 - comment on column declaration_data.knf_type_id';  	
	EXECUTE IMMEDIATE 'COMMENT ON column declaration_data.knf_type_id IS ''Тип КНФ''';		
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
COMMIT;

-- https://jira.aplana.com/browse/SBRFNDFL-5080 новые виды КНФ
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #2 - alter table declaration_data add constraint declaration_data_knf_type';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_constraints where lower(table_name)='declaration_data' and lower(constraint_name)='declaration_data_knf_type';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'ALTER TABLE declaration_data ADD CONSTRAINT declaration_data_knf_type FOREIGN KEY(knf_type_id) REFERENCES ref_book_knf_type(id)';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
COMMIT;

declare
	v_task_name varchar2(128):='alter_tables block block #3 - update declaration_data';  
begin	
	-- https://jira.aplana.com/browse/SBRFNDFL-5080 новые виды КНФ
    UPDATE declaration_data SET knf_type_id = 1 WHERE declaration_template_id = 101;

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

-- https://jira.aplana.com/browse/SBRFNDFL-5080 новые виды КНФ
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #4 - alter table declaration_data add constraint declaration_data_knf_type_ck';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_constraints where lower(table_name)='declaration_data' and lower(constraint_name)='declaration_data_knf_type_ck';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'ALTER TABLE declaration_data ADD CONSTRAINT declaration_data_knf_type_ck CHECK (declaration_template_id = 101 AND knf_type_id IS NOT NULL OR declaration_template_id != 101 AND knf_type_id is null)';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
COMMIT;

--https://jira.aplana.com/browse/SBRFNDFL-5080 новые виды КНФ
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #5 - alter table ref_book_income_type add app2_include ';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_income_type' and lower(column_name)='app2_include';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'ALTER TABLE ref_book_income_type ADD app2_include NUMBER(1)';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
	v_task_name:='alter_tables block #5 - comment on column ref_book_income_type.app2_include';  	
	EXECUTE IMMEDIATE 'COMMENT ON column ref_book_income_type.app2_include IS ''Включается в Приложение 2''';		
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
COMMIT;

--https://jira.aplana.com/browse/SBRFNDFL-5080 новые виды КНФ
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #6 - update ref_book_income_type set app2_include = 1';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_income_type' and lower(column_name)='app2_include';
	IF v_run_condition=1 THEN
		update ref_book_income_type set app2_include = 1;

		CASE SQL%ROWCOUNT 
		WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
		ELSE 
			dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
		END CASE; 
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' column not found');
	END IF;
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
COMMIT;

--https://jira.aplana.com/browse/SBRFNDFL-5080 новые виды КНФ
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #7 - ref_book_income_type MODIFY app2_include NOT NULL';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_income_type' and lower(column_name)='app2_include';
	IF v_run_condition=1 THEN
		select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_income_type' and lower(column_name)='app2_include' and lower(nullable)='y';
		IF v_run_condition=1 THEN
			EXECUTE IMMEDIATE 'ALTER TABLE ref_book_income_type MODIFY app2_include NOT NULL';
			dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
		ELSE
			dbms_output.put_line(v_task_name||'[WARNING]:'||' column is already NOT NULL');
		END IF;
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' column not found');
	END IF;
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
COMMIT;

-- https://jira.aplana.com/browse/SBRFNDFL-5080 новые виды КНФ
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #8 - alter table declaration_data_kpp add constraint dd_kpp_dd_fk';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_constraints where lower(table_name)='declaration_data_kpp' and lower(constraint_name)='dd_kpp_dd_fk';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table declaration_data_kpp add constraint dd_kpp_dd_fk foreign key (declaration_data_id) references declaration_data (id) on delete cascade';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
COMMIT;

-- https://jira.aplana.com/browse/SBRFNDFL-5080 новые виды КНФ
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #9 - alter table declaration_data_person add constraint dd_person_dd_fk';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_constraints where lower(table_name)='declaration_data_person' and lower(constraint_name)='dd_person_dd_fk';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table declaration_data_person add constraint dd_person_dd_fk foreign key (declaration_data_id) references declaration_data (id) on delete cascade';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
COMMIT;

-- https://jira.aplana.com/browse/SBRFNDFL-5080 новые виды КНФ
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #10 - alter table declaration_data_person add constraint dd_person_person_fk';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_constraints where lower(table_name)='declaration_data_person' and lower(constraint_name)='dd_person_person_fk';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table declaration_data_person add constraint dd_person_person_fk foreign key (person_id) references ref_book_person (id) on delete cascade';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
COMMIT;

--https://jira.aplana.com/browse/SBRFNDFL-5610
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #11 - alter table ref_book_id_doc drop column inc_rep ';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_id_doc' and lower(column_name)='inc_rep';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_id_doc drop column inc_rep';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
END;
/
COMMIT;

--https://jira.aplana.com/browse/SBRFNDFL-5967 Исправить коментарии у таблиц и колонок реестра ФЛ
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #12 - comment on table REF_BOOK_PERSON';  
BEGIN
	EXECUTE IMMEDIATE 'comment on table REF_BOOK_PERSON is ''Реестр физических лиц''';		
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	v_task_name:='alter_tables block #13 - comment on column REF_BOOK_PERSON.record_id';  	
	EXECUTE IMMEDIATE 'comment on column REF_BOOK_PERSON.record_id is ''Идентификатор ФЛ''';		
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');

	v_task_name:='alter_tables block #14 - comment on table REF_BOOK_PERSON_TB';  	
	EXECUTE IMMEDIATE 'comment on table REF_BOOK_PERSON_TB is ''Тербанк назначенный ФЛ''';		
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	v_task_name:='alter_tables block #15 - comment on column REF_BOOK_PERSON_TB.person_id';  	
	EXECUTE IMMEDIATE 'comment on column REF_BOOK_PERSON_TB.person_id is ''Ссылка на запись реестра ФЛ''';		
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	
	v_task_name:='alter_tables block #16 - comment on table REF_BOOK_ID_DOC';  	
	EXECUTE IMMEDIATE 'comment on table REF_BOOK_ID_DOC is ''Документ, удостоверяющий личность ФЛ''';		
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	v_task_name:='alter_tables block #17 - comment on column REF_BOOK_ID_DOC.person_id';  	
	EXECUTE IMMEDIATE 'comment on column REF_BOOK_ID_DOC.person_id is ''Ссылка на запись реестра ФЛ''';		
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	
	v_task_name:='alter_tables block #18 - comment on table REF_BOOK_ID_TAX_PAYER';  	
	EXECUTE IMMEDIATE 'comment on table REF_BOOK_ID_TAX_PAYER is ''Идентификатор налогоплательщика назначенный ФЛ в АСНУ''';		
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	v_task_name:='alter_tables block #19 - comment on column REF_BOOK_ID_TAX_PAYER.person_id';  	
	EXECUTE IMMEDIATE 'comment on column REF_BOOK_ID_TAX_PAYER.person_id is ''Ссылка на запись реестра ФЛ''';		
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
COMMIT;

--https://jira.aplana.com/browse/SBRFNDFL-5979
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #20 - alter table tmp_cons_data drop column inp';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='tmp_cons_data' and lower(column_name)='inp';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table tmp_cons_data drop column inp';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
END;
/
COMMIT;

--https://jira.aplana.com/browse/SBRFNDFL-5979
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #21 - alter table tmp_cons_data drop column year';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='tmp_cons_data' and lower(column_name)='year';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table tmp_cons_data drop column year';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
END;
/
COMMIT;

--https://jira.aplana.com/browse/SBRFNDFL-5979
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #22 - alter table tmp_cons_data drop column period_code';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='tmp_cons_data' and lower(column_name)='period_code';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table tmp_cons_data drop column period_code';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
END;
/
COMMIT;

--https://jira.aplana.com/browse/SBRFNDFL-5979
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #23 - alter table tmp_cons_data drop column correction_date';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='tmp_cons_data' and lower(column_name)='correction_date';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table tmp_cons_data drop column correction_date';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
END;
/
COMMIT;
