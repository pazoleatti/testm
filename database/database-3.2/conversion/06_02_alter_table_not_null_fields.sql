--https://jira.aplana.com/browse/SBRFNDFL-5837 Конвертировать данные реестра ФЛ
declare 
	v_run_condition number(1):=0;
	v_task_name varchar2(128):='alter_tables block #1 - alter table NDFL_REFERENCES PERSON_ID nullable=''N'' (SBRFNDFL-5837)';  
begin
	select decode(count(*),0,1,0) into v_run_condition from NDFL_REFERENCES where PERSON_ID is null;
	IF v_run_condition=1 THEN
		select count(*) into v_run_condition from user_tab_columns where table_name='NDFL_REFERENCES' and column_name='PERSON_ID' and nullable='Y';
		IF v_run_condition=1 THEN
			EXECUTE IMMEDIATE 'alter table NDFL_REFERENCES modify PERSON_ID not null';
			dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
		ELSE
			dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
		END IF; 
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' PERSON_ID contains empty values');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
end;
/
	
--https://jira.aplana.com/browse/SBRFNDFL-5837 Конвертировать данные реестра ФЛ
declare 
	v_run_condition number(1):=0;
	v_task_name varchar2(128):='alter_tables block #2 - alter table REF_BOOK_ID_TAX_PAYER PERSON_ID nullable=''N'' (SBRFNDFL-5837)';  
begin
	select decode(count(*),0,1,0) into v_run_condition from REF_BOOK_ID_TAX_PAYER where PERSON_ID is null;
	IF v_run_condition=1 THEN
		select count(*) into v_run_condition from user_tab_columns where table_name='REF_BOOK_ID_TAX_PAYER' and column_name='PERSON_ID' and nullable='Y';
		IF v_run_condition=1 THEN
			EXECUTE IMMEDIATE 'alter table REF_BOOK_ID_TAX_PAYER modify PERSON_ID not null';
			dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
		ELSE
			dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
		END IF; 
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' PERSON_ID contains empty values');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
end;
/

--https://jira.aplana.com/browse/SBRFNDFL-5837 Конвертировать данные реестра ФЛ
declare 
	v_run_condition number(1):=0;
	v_task_name varchar2(128):='alter_tables block #3 - alter table REF_BOOK_PERSON_TB PERSON_ID nullable=''N'' (SBRFNDFL-5837)';  
begin
	select decode(count(*),0,1,0) into v_run_condition from REF_BOOK_PERSON_TB where PERSON_ID is null;
	IF v_run_condition=1 THEN
		select count(*) into v_run_condition from user_tab_columns where table_name='REF_BOOK_PERSON_TB' and column_name='PERSON_ID' and nullable='Y';
		IF v_run_condition=1 THEN
			EXECUTE IMMEDIATE 'alter table REF_BOOK_PERSON_TB modify PERSON_ID not null';
			dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
		ELSE
			dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
		END IF; 
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' PERSON_ID contains empty values');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
end;
/

--https://jira.aplana.com/browse/SBRFNDFL-5837 Конвертировать данные реестра ФЛ
declare 
	v_run_condition number(1):=0;
	v_task_name varchar2(128):='alter_tables block #4 - alter table REF_BOOK_PERSON START_DATE nullable=''N'' (SBRFNDFL-5837)';  
begin
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='start_date';
	IF v_run_condition=1 THEN
		select decode(count(*),0,1,0) into v_run_condition from REF_BOOK_PERSON where START_DATE is null;
		IF v_run_condition=1 THEN
			select count(*) into v_run_condition from user_tab_columns where table_name='REF_BOOK_PERSON' and column_name='START_DATE' and nullable='Y';
			IF v_run_condition=1 THEN
				EXECUTE IMMEDIATE 'alter table REF_BOOK_PERSON modify START_DATE not null';
				dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
			ELSE
				dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
			END IF; 
		ELSE
			dbms_output.put_line(v_task_name||'[WARNING]:'||' START_DATE contains empty values');
		END IF; 
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' column ref_book_person.start_date not found');
	END IF;
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
end;
/
