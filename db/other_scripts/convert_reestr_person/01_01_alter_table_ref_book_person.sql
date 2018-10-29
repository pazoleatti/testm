--https://jira.aplana.com/browse/SBRFNDFL-5837 Конвертировать данные реестра ФЛ
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #1 - alter table ref_book_person add START_DATE (SBRFNDFL-5837)';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='start_date';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_person add start_date DATE';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
	v_task_name:='alter_tables block #2 - comment on column ref_book_person.start_date (SBRFNDFL-5837)';  	
	EXECUTE IMMEDIATE 'comment on column ref_book_person.start_date is ''Дата начала действия''';		
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

--https://jira.aplana.com/browse/SBRFNDFL-5837 Конвертировать данные реестра ФЛ
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #3 - alter table ref_book_person add END_DATE (SBRFNDFL-5837)';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='end_date';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_person add end_date DATE';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
	v_task_name:='alter_tables block #4 - comment on column ref_book_person.end_date (SBRFNDFL-5837)';  	
	EXECUTE IMMEDIATE 'comment on column ref_book_person.end_date is ''Дата окончания действия''';		
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

--https://jira.aplana.com/browse/SBRFNDFL-5837 Конвертировать данные реестра ФЛ
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #5 - alter table ref_book_person add REGION_CODE (SBRFNDFL-5837)';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='region_code';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_person add region_code VARCHAR2(2 CHAR)';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
	v_task_name:='alter_tables block #6 - comment on column ref_book_person.region_code (SBRFNDFL-5837)';  	
	EXECUTE IMMEDIATE 'comment on column ref_book_person.region_code is ''Код региона''';		
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

--https://jira.aplana.com/browse/SBRFNDFL-5837 Конвертировать данные реестра ФЛ
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #7 - alter table ref_book_person add postal_code (SBRFNDFL-5837)';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='postal_code';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_person add postal_code VARCHAR2(6 CHAR)';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
	v_task_name:='alter_tables block #8 - comment on column ref_book_person.postal_code (SBRFNDFL-5837)';  	
	EXECUTE IMMEDIATE 'comment on column ref_book_person.postal_code is ''Почтовый индекс''';		
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

--https://jira.aplana.com/browse/SBRFNDFL-5837 Конвертировать данные реестра ФЛ
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #9 - alter table ref_book_person add district (SBRFNDFL-5837)';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='district';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_person add district VARCHAR2(500 CHAR)';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
	v_task_name:='alter_tables block #10 - comment on column ref_book_person.district (SBRFNDFL-5837)';  	
	EXECUTE IMMEDIATE 'comment on column ref_book_person.district is ''Район''';		
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

--https://jira.aplana.com/browse/SBRFNDFL-5837 Конвертировать данные реестра ФЛ
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #11 - alter table ref_book_person add city (SBRFNDFL-5837)';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='city';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_person add city VARCHAR2(500 CHAR)';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
	v_task_name:='alter_tables block #12 - comment on column ref_book_person.city (SBRFNDFL-5837)';  	
	EXECUTE IMMEDIATE 'comment on column ref_book_person.city is ''Город''';		
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

--https://jira.aplana.com/browse/SBRFNDFL-5837 Конвертировать данные реестра ФЛ
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #13 - alter table ref_book_person add locality (SBRFNDFL-5837)';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='locality';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_person add locality VARCHAR2(500 CHAR)';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
	v_task_name:='alter_tables block #14 - comment on column ref_book_person.locality (SBRFNDFL-5837)';  	
	EXECUTE IMMEDIATE 'comment on column ref_book_person.locality is ''Населенный пункт (село, поселок)''';		
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

--https://jira.aplana.com/browse/SBRFNDFL-5837 Конвертировать данные реестра ФЛ
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #15 - alter table ref_book_person add street (SBRFNDFL-5837)';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='street';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_person add street VARCHAR2(500 CHAR)';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
	v_task_name:='alter_tables block #16 - comment on column ref_book_person.street (SBRFNDFL-5837)';  	
	EXECUTE IMMEDIATE 'comment on column ref_book_person.street is ''Улица (проспект, переулок)''';		
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

--https://jira.aplana.com/browse/SBRFNDFL-5837 Конвертировать данные реестра ФЛ
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #17 - alter table ref_book_person add house (SBRFNDFL-5837)';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='house';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_person add house VARCHAR2(20 CHAR)';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
	v_task_name:='alter_tables block #18 - comment on column ref_book_person.house (SBRFNDFL-5837)';  	
	EXECUTE IMMEDIATE 'comment on column ref_book_person.house is ''Номер дома (владения)''';		
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

--https://jira.aplana.com/browse/SBRFNDFL-5837 Конвертировать данные реестра ФЛ
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #19 - alter table ref_book_person add build (SBRFNDFL-5837)';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='build';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_person add build VARCHAR2(20 CHAR)';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
	v_task_name:='alter_tables block #20 - comment on column ref_book_person.build (SBRFNDFL-5837)';  	
	EXECUTE IMMEDIATE 'comment on column ref_book_person.build is ''Номер корпуса (строения)''';		
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

--https://jira.aplana.com/browse/SBRFNDFL-5837 Конвертировать данные реестра ФЛ
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #21 - alter table ref_book_person add appartment (SBRFNDFL-5837)';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='appartment';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_person add appartment VARCHAR2(20 CHAR)';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
	v_task_name:='alter_tables block #22 - comment on column ref_book_person.appartment (SBRFNDFL-5837)';  	
	EXECUTE IMMEDIATE 'comment on column ref_book_person.appartment is ''Номер квартиры''';		
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

--https://jira.aplana.com/browse/SBRFNDFL-5837 Конвертировать данные реестра ФЛ
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #23 - alter table ref_book_person add country_id (SBRFNDFL-5837)';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='country_id';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_person add country_id NUMBER(18,0)';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
	v_task_name:='alter_tables block #24 - comment on column ref_book_person.country_id (SBRFNDFL-5837)';  	
	EXECUTE IMMEDIATE 'comment on column ref_book_person.country_id is ''Страна проживания''';		
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

--https://jira.aplana.com/browse/SBRFNDFL-5837 Конвертировать данные реестра ФЛ
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #25 - alter table ref_book_person add address_foreign (SBRFNDFL-5837)';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='address_foreign';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_person add address_foreign VARCHAR2(255 CHAR)';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
	v_task_name:='alter_tables block #26 - comment on column ref_book_person.address_foreign (SBRFNDFL-5837)';  	
	EXECUTE IMMEDIATE 'comment on column ref_book_person.address_foreign is ''Адрес за пределами Российской Федерации''';		
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

--https://jira.aplana.com/browse/SBRFNDFL-5837 Конвертировать данные реестра ФЛ
declare 
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #28 - alter table NDFL_REFERENCES PERSON_ID nullable=''Y'' (SBRFNDFL-5837)';  
begin
	select count(*) into v_run_condition from user_tab_columns where table_name='NDFL_REFERENCES' and column_name='PERSON_ID' and nullable='N';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table NDFL_REFERENCES modify PERSON_ID null';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
end;
/
	
--https://jira.aplana.com/browse/SBRFNDFL-5837 Конвертировать данные реестра ФЛ
declare 
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #29 - alter table REF_BOOK_ID_TAX_PAYER PERSON_ID nullable=''Y'' (SBRFNDFL-5837)';  
begin
	select count(*) into v_run_condition from user_tab_columns where table_name='REF_BOOK_ID_TAX_PAYER' and column_name='PERSON_ID' and nullable='N';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table REF_BOOK_ID_TAX_PAYER modify PERSON_ID null';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
end;
/

--https://jira.aplana.com/browse/SBRFNDFL-5837 Конвертировать данные реестра ФЛ
declare 
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #30 - alter table REF_BOOK_PERSON_TB PERSON_ID nullable=''Y'' (SBRFNDFL-5837)';  
begin
	select count(*) into v_run_condition from user_tab_columns where table_name='REF_BOOK_PERSON_TB' and column_name='PERSON_ID' and nullable='N';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table REF_BOOK_PERSON_TB modify PERSON_ID null';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
end;
/
