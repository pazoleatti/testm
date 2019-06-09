--https://jira.aplana.com/browse/SBRFNDFL-6233 Поменять размерность поля "phone" в настройках подразделений
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #1 - ref_book_ndfl_detail modify phone';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_ndfl_detail' and lower(column_name)='phone';
	IF v_run_condition=1 THEN
		select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_ndfl_detail' and lower(column_name)='phone' and data_type = 'VARCHAR2' and char_length > 20;
		IF v_run_condition=1 THEN
			EXECUTE IMMEDIATE 'alter table ref_book_ndfl_detail modify phone varchar2(20 char)';
			dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
		ELSE
			dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
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

-- https://jira.aplana.com/browse/SBRFNDFL-6247 Каскадное удаление для записи ФЛ
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #2 - alter table ref_book_id_doc drop constraint fk_ref_book_id_doc_person';  
BEGIN
	select count(*) into v_run_condition from user_constraints where lower(table_name)='ref_book_id_doc' and lower(constraint_name)='fk_ref_book_id_doc_person';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_id_doc drop constraint fk_ref_book_id_doc_person';
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

-- https://jira.aplana.com/browse/SBRFNDFL-6247 Каскадное удаление для записи ФЛ
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #3 - alter table ref_book_id_doc add constraint fk_ref_book_id_doc_person';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_constraints where lower(table_name)='ref_book_id_doc' and lower(constraint_name)='fk_ref_book_id_doc_person';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_id_doc add constraint fk_ref_book_id_doc_person foreign key (person_id) references ref_book_person (id) on delete cascade';
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

-- https://jira.aplana.com/browse/SBRFNDFL-6247 Каскадное удаление для записи ФЛ
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #4 - alter table ref_book_id_tax_payer drop constraint fk_ref_book_id_tax_payer_pers';  
BEGIN
	select count(*) into v_run_condition from user_constraints where lower(table_name)='ref_book_id_tax_payer' and lower(constraint_name)='fk_ref_book_id_tax_payer_pers';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_id_tax_payer drop constraint fk_ref_book_id_tax_payer_pers';
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

-- https://jira.aplana.com/browse/SBRFNDFL-6247 Каскадное удаление для записи ФЛ
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #5- alter table ref_book_id_tax_payer add constraint fk_ref_book_id_tax_payer_pers';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_constraints where lower(table_name)='ref_book_id_tax_payer' and lower(constraint_name)='fk_ref_book_id_tax_payer_pers';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_id_tax_payer add constraint fk_ref_book_id_tax_payer_pers foreign key (person_id) references ref_book_person (id) on delete cascade';
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

--https://jira.aplana.com/browse/SBRFNDFL-6298 - Обязательность полей таблиц связанных с ФЛ
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #6 - ref_book_id_doc modify person_id not null';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_id_doc' and lower(column_name)='person_id';
	IF v_run_condition=1 THEN
		select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_id_doc' and lower(column_name)='person_id' and lower(nullable)='y';
		IF v_run_condition=1 THEN
			select decode(count(*),0,1,0) into v_run_condition from ref_book_id_doc where person_id is null;
			IF v_run_condition=1 THEN
				EXECUTE IMMEDIATE 'alter table ref_book_id_doc modify person_id not null';
				dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
			ELSE
				dbms_output.put_line(v_task_name||'[WARNING]:'||' PERSON_ID contains empty values');
			END IF; 
		ELSE
			dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
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

--https://jira.aplana.com/browse/SBRFNDFL-6298 - Обязательность полей таблиц связанных с ФЛ
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #7 - ref_book_id_doc modify doc_id not null';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_id_doc' and lower(column_name)='doc_id';
	IF v_run_condition=1 THEN
		select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_id_doc' and lower(column_name)='doc_id' and lower(nullable)='y';
		IF v_run_condition=1 THEN
			select decode(count(*),0,1,0) into v_run_condition from ref_book_id_doc where doc_id is null;
			IF v_run_condition=1 THEN
				EXECUTE IMMEDIATE 'alter table ref_book_id_doc modify doc_id not null';
				dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
			ELSE
				dbms_output.put_line(v_task_name||'[WARNING]:'||' DOC_ID contains empty values');
			END IF; 
		ELSE
			dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
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

--https://jira.aplana.com/browse/SBRFNDFL-6298 - Обязательность полей таблиц связанных с ФЛ
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #8 - ref_book_id_doc modify doc_number not null';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_id_doc' and lower(column_name)='doc_number';
	IF v_run_condition=1 THEN
		select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_id_doc' and lower(column_name)='doc_number' and lower(nullable)='y';
		IF v_run_condition=1 THEN
			select decode(count(*),0,1,0) into v_run_condition from ref_book_id_doc where doc_number is null;
			IF v_run_condition=1 THEN
				EXECUTE IMMEDIATE 'alter table ref_book_id_doc modify doc_number not null';
				dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
			ELSE
				dbms_output.put_line(v_task_name||'[WARNING]:'||' DOC_NUMBER contains empty values');
			END IF; 
		ELSE
			dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
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

--https://jira.aplana.com/browse/SBRFNDFL-6298 - Обязательность полей таблиц связанных с ФЛ
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #9 - ref_book_person modify old_id not null';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='old_id';
	IF v_run_condition=1 THEN
		select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='old_id' and lower(nullable)='y';
		IF v_run_condition=1 THEN
			select decode(count(*),0,1,0) into v_run_condition from ref_book_person where old_id is null;
			IF v_run_condition=1 THEN
				EXECUTE IMMEDIATE 'alter table ref_book_person modify old_id not null';
				dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
			ELSE
				dbms_output.put_line(v_task_name||'[WARNING]:'||' OLD_ID contains empty values');
			END IF; 
		ELSE
			dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
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

--https://jira.aplana.com/browse/SBRFNDFL-6290 - Удалить поле ref_book_person.address
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #10 - alter table ref_book_person drop column address ';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='address';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_person drop column address';
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

--https://jira.aplana.com/browse/SBRFNDFL-5489 изменения истории изменений
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #11- alter table log_business drop constraint log_business_chk_event_id';  
BEGIN
	select count(*) into v_run_condition from user_constraints where lower(table_name)='log_business' and lower(constraint_name)='log_business_chk_event_id';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table log_business drop constraint log_business_chk_event_id';
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

--https://jira.aplana.com/browse/SBRFNDFL-5489 изменения истории изменений
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #12- alter table log_business drop constraint log_business_chk_frm_dcl_ev';  
BEGIN
	select count(*) into v_run_condition from user_constraints where lower(table_name)='log_business' and lower(constraint_name)='log_business_chk_frm_dcl_ev';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table log_business drop constraint log_business_chk_frm_dcl_ev';
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

--https://jira.aplana.com/browse/SBRFNDFL-5489 изменения истории изменений
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #13 - alter table log_business drop column form_data_id ';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='log_business' and lower(column_name)='form_data_id';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table log_business drop column form_data_id';
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

--https://jira.aplana.com/browse/SBRFNDFL-5489 изменения истории изменений
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #14 - log_business modify note';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='log_business' and lower(column_name)='note';
	IF v_run_condition=1 THEN
		select count(*) into v_run_condition from user_tab_columns where lower(table_name)='log_business' and lower(column_name)='note' and data_type = 'VARCHAR2' and char_length < 4000;
		IF v_run_condition=1 THEN
			EXECUTE IMMEDIATE 'alter table log_business modify note varchar2(4000 byte)';
			dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
		ELSE
			dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
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

--https://jira.aplana.com/browse/SBRFNDFL-5489 изменения истории изменений
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #15 - log_business add person_id';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where lower(table_name)='log_business' and lower(column_name)='person_id';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table log_business add person_id number(18,0)';
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

--https://jira.aplana.com/browse/SBRFNDFL-5489 изменения истории изменений
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #16 - alter table log_business add constraint log_business_fk_person';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_constraints where lower(table_name)='log_business' and lower(constraint_name)='log_business_fk_person';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table log_business add constraint log_business_fk_person foreign key (person_id) references ref_book_person(id) on delete cascade';
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

--https://jira.aplana.com/browse/SBRFNDFL-5489 изменения истории изменений
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #17 - alter table log_business add constraint log_business_chk_obj_id';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_constraints where lower(table_name)='log_business' and lower(constraint_name)='log_business_chk_obj_id';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table log_business add constraint log_business_chk_obj_id check(declaration_data_id is not null and person_id is null or declaration_data_id is null and person_id is not null)';
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

--https://jira.aplana.com/browse/SBRFNDFL-5489 изменения истории изменений
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #18 - comment on columns log_business';  
BEGIN
	EXECUTE IMMEDIATE 'comment on column log_business.declaration_data_id is ''Идентификатор формы''';
	EXECUTE IMMEDIATE 'comment on column log_business.person_id is ''Идентификатор ФЛ''';
	EXECUTE IMMEDIATE 'comment on column log_business.event_id is ''Идентификатор события''';
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
COMMIT;

DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #19 - declaration_data add correction_num';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where lower(table_name)='declaration_data' and lower(column_name)='correction_num';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table declaration_data add correction_num number(3)';
		EXECUTE IMMEDIATE 'comment on column declaration_data.correction_num is ''Номер корректировки''';
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

--https://jira.aplana.com/browse/SBRFNDFL-5466 добавить атрибут дял формы "Показывать возвращенный налог"
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #20 - declaration_data add tax_refund_reflection_mode';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where lower(table_name)='declaration_data' and lower(column_name)='tax_refund_reflection_mode';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table declaration_data add tax_refund_reflection_mode number(1)';
		EXECUTE IMMEDIATE 'comment on column declaration_data.tax_refund_reflection_mode is ''Показывать возвращенный налог (1 - "Показывать в строке 090 Раздела 1", 2 - "Учитывать возврат как отрицательное удержание в Разделе 2", 3 - Не учитывать)''';
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

--https://jira.aplana.com/browse/SBRFNDFL-5489 изменения истории изменений
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #21 - log_business modify event_id';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='log_business' and lower(column_name)='event_id';
	IF v_run_condition=1 THEN
		select count(*) into v_run_condition from user_tab_columns where lower(table_name)='log_business' and lower(column_name)='event_id' and data_type = 'NUMBER' and data_precision = 3;
		IF v_run_condition=1 THEN
			EXECUTE IMMEDIATE 'alter table log_business modify event_id NUMBER(9,0)';
			dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
		ELSE
			dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
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
