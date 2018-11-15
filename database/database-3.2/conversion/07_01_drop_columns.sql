DECLARE
	v_run_condition number(1):=0;
	v_task_name varchar2(128):='drop_columns block #1 - alter table REF_BOOK_ID_DOC drop column RECORD_ID (SBRFNDFL-5837)';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_id_doc' and lower(column_name)='record_id';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_id_doc drop column record_id';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
END;
/

DECLARE
	v_run_condition number(1):=0;
	v_task_name varchar2(128):='drop_columns block #2 - alter table REF_BOOK_ID_DOC drop column version (SBRFNDFL-5837)';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_id_doc' and lower(column_name)='version';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_id_doc drop column version';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
END;
/

DECLARE
	v_run_condition number(1):=0;
	v_task_name varchar2(128):='drop_columns block #3 - alter table REF_BOOK_ID_DOC drop constraint RB_ID_DOC_CHK_STATUS (SBRFNDFL-5837)';  
BEGIN
	select count(*) into v_run_condition from user_constraints where lower(table_name)='ref_book_id_doc' and lower(constraint_name)=lower('RB_ID_DOC_CHK_STATUS');
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_id_doc drop constraint RB_ID_DOC_CHK_STATUS';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 

	v_task_name :='drop_columns block #3 - alter table REF_BOOK_ID_DOC drop column status (SBRFNDFL-5837)';  
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_id_doc' and lower(column_name)='status';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_id_doc drop column status';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
END;
/

DECLARE
	v_run_condition number(1):=0;
	v_task_name varchar2(128):='drop_columns block #4 - alter table REF_BOOK_ID_DOC drop column duplicate_record_id (SBRFNDFL-5837)';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_id_doc' and lower(column_name)='duplicate_record_id';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_id_doc drop column duplicate_record_id';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
END;
/

DECLARE
	v_run_condition number(1):=0;
	v_task_name varchar2(128):='drop_columns block #5 - alter table ref_book_id_tax_payer drop column RECORD_ID (SBRFNDFL-5837)';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_id_tax_payer' and lower(column_name)='record_id';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_id_tax_payer drop column record_id';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
END;
/

DECLARE
	v_run_condition number(1):=0;
	v_task_name varchar2(128):='drop_columns block #6 - alter table ref_book_id_tax_payer drop column version (SBRFNDFL-5837)';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_id_tax_payer' and lower(column_name)='version';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_id_tax_payer drop column version';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
END;
/

DECLARE
	v_run_condition number(1):=0;
	v_task_name varchar2(128):='drop_columns block #7 - alter table ref_book_id_tax_payer drop constraint RB_TAX_PAYER_CHK_STATUS (SBRFNDFL-5837)';  
BEGIN
	select count(*) into v_run_condition from user_constraints where lower(table_name)='ref_book_id_tax_payer' and lower(constraint_name)=lower('RB_TAX_PAYER_CHK_STATUS');
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_id_tax_payer drop constraint RB_TAX_PAYER_CHK_STATUS';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 

	v_task_name :='drop_columns block #7 - alter table ref_book_id_tax_payer drop column status (SBRFNDFL-5837)';  
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_id_tax_payer' and lower(column_name)='status';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_id_tax_payer drop column status';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
END;
/

DECLARE
	v_run_condition number(1):=0;
	v_task_name varchar2(128):='drop_columns block #8 - alter table ref_book_person_tb drop column RECORD_ID (SBRFNDFL-5837)';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_person_tb' and lower(column_name)='record_id';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_person_tb drop column record_id';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
END;
/

DECLARE
	v_run_condition number(1):=0;
	v_task_name varchar2(128):='drop_columns block #9 - alter table ref_book_person_tb drop column version (SBRFNDFL-5837)';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_person_tb' and lower(column_name)='version';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_person_tb drop column version';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
END;
/

DECLARE
	v_run_condition number(1):=0;
	v_task_name varchar2(128):='drop_columns block #10 - alter table ref_book_person_tb drop constraint CHK_REF_BOOK_PERSON_TB_STATUS (SBRFNDFL-5837)';  
BEGIN
	select count(*) into v_run_condition from user_constraints where lower(table_name)='ref_book_person_tb' and lower(constraint_name)=lower('CHK_REF_BOOK_PERSON_TB_STATUS');
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_person_tb drop constraint CHK_REF_BOOK_PERSON_TB_STATUS';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 

	v_task_name :='drop_columns block #10 - alter table ref_book_person_tb drop column status (SBRFNDFL-5837)';  
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_person_tb' and lower(column_name)='status';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_person_tb drop column status';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
END;
/

DECLARE
	v_run_condition number(1):=0;
	v_run_condition2 number(1):=0;
	v_task_name varchar2(128):='drop_columns block #11 - alter table ref_book_person drop column version (SBRFNDFL-5837)';  
	v_exist_status number(1):=0;
	v_exist_start_date number(1):=0;
BEGIN
	select count(*) into v_exist_status from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='status';
	select count(*) into v_exist_start_date from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='start_date';
	IF v_exist_status=1 AND v_exist_start_date=1 THEN
		select decode(count(*),0,1,0) into v_run_condition from ref_book_person where start_date is null;
		EXECUTE IMMEDIATE 'select decode(count(*),0,1,0) from ref_book_person where status<>0' INTO v_run_condition2;
		IF v_run_condition=1 AND v_run_condition2=1 THEN
			v_task_name :='drop_columns block #11 - alter table ref_book_person drop index IDX_REF_PERSON_ST_VER_REC (SBRFNDFL-5837)';  
			select count(*) into v_run_condition from user_indexes where lower(table_name)='ref_book_person' and lower(index_name)=lower('IDX_REF_PERSON_ST_VER_REC');
			IF v_run_condition=1 THEN
				EXECUTE IMMEDIATE 'drop index IDX_REF_PERSON_ST_VER_REC';
				dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
			ELSE
				dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
			END IF; 

			v_task_name:='drop_columns block #11 - alter table ref_book_person drop column version (SBRFNDFL-5837)';  
			v_run_condition := 0;
			select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='version';
			IF v_run_condition=1 THEN
				EXECUTE IMMEDIATE 'alter table ref_book_person drop column version';
				dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
			ELSE
				dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
			END IF; 
		ELSE
			dbms_output.put_line(v_task_name||'[WARNING]:'||CASE WHEN v_run_condition=0 THEN ' Filed. Start_Date is null' ELSE '' END||CASE WHEN v_run_condition2=0 THEN ' Filed. Status<>0 found' ELSE '' END);
		END IF; 
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||CASE WHEN v_exist_status=0 THEN ' Column "status" not found' ELSE '' END
		||CASE WHEN v_exist_start_date=0 THEN ' Column "start_date" not found' ELSE '' END);
	END IF;
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
END;
/

DECLARE
	v_run_condition number(1):=0;
	v_run_condition2 number(1):=0;
	v_task_name varchar2(128):='drop_columns block #12 - alter table ref_book_person drop column status (SBRFNDFL-5837)';  
	v_exist_status number(1):=0;
	v_exist_start_date number(1):=0;
BEGIN
	select count(*) into v_exist_status from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='status';
	select count(*) into v_exist_start_date from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='start_date';
	IF v_exist_status=1 AND v_exist_start_date=1 THEN
		select decode(count(*),0,1,0) into v_run_condition from ref_book_person where start_date is null;
		EXECUTE IMMEDIATE 'select decode(count(*),0,1,0) from ref_book_person where status<>0' INTO v_run_condition2;
		IF v_run_condition=1 AND v_run_condition2=1 THEN
			v_task_name :='drop_columns block #12 - alter table ref_book_person drop constraint CHK_REF_BOOK_PERSON_STATUS (SBRFNDFL-5837)';  
			select count(*) into v_run_condition from user_constraints where lower(table_name)='ref_book_person' and lower(constraint_name)=lower('CHK_REF_BOOK_PERSON_STATUS');
			IF v_run_condition=1 THEN
				EXECUTE IMMEDIATE 'alter table ref_book_person drop constraint CHK_REF_BOOK_PERSON_STATUS';
				dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
			ELSE
				dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
			END IF; 

			v_task_name :='drop_columns block #12 - alter table ref_book_person drop index IDX_REF_PERSON_STATUS (SBRFNDFL-5837)';  
			select count(*) into v_run_condition from user_indexes where lower(table_name)='ref_book_person' and lower(index_name)=lower('IDX_REF_PERSON_STATUS');
			IF v_run_condition=1 THEN
				EXECUTE IMMEDIATE 'drop index IDX_REF_PERSON_STATUS';
				dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
			ELSE
				dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
			END IF; 

			v_task_name :='drop_columns block #12 - alter table ref_book_person drop column status (SBRFNDFL-5837)';  
			select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='status';
			IF v_run_condition=1 THEN
				EXECUTE IMMEDIATE 'alter table ref_book_person drop column status';
				dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
			ELSE
				dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
			END IF; 
		ELSE
			dbms_output.put_line(v_task_name||'[WARNING]:'||CASE WHEN v_run_condition=0 THEN ' Filed. Start_Date is null' ELSE '' END||CASE WHEN v_run_condition2=0 THEN ' Filed. Status<>0 found' ELSE '' END);
		END IF; 
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||CASE WHEN v_exist_status=0 THEN ' Column "status" not found' ELSE '' END
		||CASE WHEN v_exist_start_date=0 THEN ' Column "start_date" not found' ELSE '' END);
	END IF;
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
END;
/
