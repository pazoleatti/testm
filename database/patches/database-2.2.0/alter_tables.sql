DECLARE
	v_count number;
BEGIN
	dbms_output.put_line('alter table ASYNC_TASK');
	execute	immediate 'alter table ASYNC_TASK modify (DESCRIPTION VARCHAR2(400 CHAR))';
	select count(1) into v_count from user_tab_columns where table_name='ASYNC_TASK' and column_name='DESCRIPTION' and data_type='VARCHAR2' and char_length=400;
	if v_count>0 then
		dbms_output.put_line('Table ASYNC_TASK altered');
	end if;
	
	-- https://jira.aplana.com/browse/SBRFNDFL-3689 Редактирование данных НФ через загрузку ТФ (Excel). Правки по БД РНУ НДФЛ.
	select count(1) into v_count from user_tab_columns where lower(table_name)='ndfl_person' and lower(column_name)='modified_date';
	IF v_count=0 THEN
		dbms_output.put_line('add column MODIFIED_DATE to table NDFL_PERSON');
		EXECUTE IMMEDIATE 'alter table ndfl_person add modified_date date';
		
		select count(1) into v_count from user_tab_columns where lower(table_name)='ndfl_person' and lower(column_name)='modified_date';
		IF v_count>0 THEN
			dbms_output.put_line('Column MODIFIED_DATE was added to table NDFL_PERSON');
			execute immediate 'comment on column ndfl_person.modified_date is ''Дата и время редактирования. Заполняется при редактировании данных НФ через загрузку Excel файла''';
		END IF; 
	END IF; 
	
	select count(1) into v_count from user_tab_columns where lower(table_name)='ndfl_person' and lower(column_name)='modified_by';
	IF v_count=0 THEN
		dbms_output.put_line('add column MODIFIED_BY to table NDFL_PERSON');
		EXECUTE IMMEDIATE 'alter table ndfl_person add modified_by varchar2(255 char)';
		
		select count(1) into v_count from user_tab_columns where lower(table_name)='ndfl_person' and lower(column_name)='modified_by';
		IF v_count>0 THEN
			dbms_output.put_line('Column MODIFIED_BY was added to table NDFL_PERSON');
			execute immediate 'comment on column ndfl_person.modified_by is ''Значение имени пользователя и логин из Справочника пользователей системы. Заполняется при редактировании данных НФ через загрузку Excel файла''';
		END IF; 
	END IF; 
	
	select count(1) into v_count from user_tab_columns where lower(table_name)='ndfl_person_income' and lower(column_name)='modified_date';
	IF v_count=0 THEN
		dbms_output.put_line('add column MODIFIED_DATE to table NDFL_PERSON_INCOME');
		EXECUTE IMMEDIATE 'alter table ndfl_person_income add modified_date date';
		
		select count(1) into v_count from user_tab_columns where lower(table_name)='ndfl_person_income' and lower(column_name)='modified_date';
		IF v_count>0 THEN
			dbms_output.put_line('Column MODIFIED_DATE was added to table NDFL_PERSON_INCOME');
			execute immediate 'comment on column ndfl_person_income.modified_date is ''Дата и время редактирования. Заполняется при редактировании данных НФ через загрузку Excel файла''';
		END IF; 
	END IF; 
	
	select count(1) into v_count from user_tab_columns where lower(table_name)='ndfl_person_income' and lower(column_name)='modified_by';
	IF v_count=0 THEN
		dbms_output.put_line('add column MODIFIED_BY to table NDFL_PERSON_INCOME');
		EXECUTE IMMEDIATE 'alter table ndfl_person_income add modified_by varchar2(255 char)';
		
		select count(1) into v_count from user_tab_columns where lower(table_name)='ndfl_person_income' and lower(column_name)='modified_by';
		IF v_count>0 THEN
			dbms_output.put_line('Column MODIFIED_BY was added to table NDFL_PERSON_INCOME');
			execute immediate 'comment on column ndfl_person_income.modified_by is ''Значение имени пользователя и логин из Справочника пользователей системы. Заполняется при редактировании данных НФ через загрузку Excel файла''';
		END IF; 
	END IF; 
	
	select count(1) into v_count from user_tab_columns where lower(table_name)='ndfl_person_deduction' and lower(column_name)='modified_date';
	IF v_count=0 THEN
		dbms_output.put_line('add column MODIFIED_DATE to table NDFL_PERSON_DEDUCTION');
		EXECUTE IMMEDIATE 'alter table ndfl_person_deduction add modified_date date';
		
		select count(1) into v_count from user_tab_columns where lower(table_name)='ndfl_person_deduction' and lower(column_name)='modified_date';
		IF v_count>0 THEN
			dbms_output.put_line('Column MODIFIED_DATE was added to table NDFL_PERSON_DEDUCTION');
			execute immediate 'comment on column ndfl_person_deduction.modified_date is ''Дата и время редактирования. Заполняется при редактировании данных НФ через загрузку Excel файла''';
		END IF; 
	END IF; 
	
	select count(1) into v_count from user_tab_columns where lower(table_name)='ndfl_person_deduction' and lower(column_name)='modified_by';
	IF v_count=0 THEN
		dbms_output.put_line('add column MODIFIED_BY to table NDFL_PERSON_DEDUCTION');
		EXECUTE IMMEDIATE 'alter table ndfl_person_deduction add modified_by varchar2(255 char)';
		
		select count(1) into v_count from user_tab_columns where lower(table_name)='ndfl_person_deduction' and lower(column_name)='modified_by';
		IF v_count>0 THEN
			dbms_output.put_line('Column MODIFIED_BY was added to table NDFL_PERSON_DEDUCTION');
			execute immediate 'comment on column ndfl_person_deduction.modified_by is ''Значение имени пользователя и логин из Справочника пользователей системы. Заполняется при редактировании данных НФ через загрузку Excel файла''';
		END IF; 
	END IF; 
	
	select count(1) into v_count from user_tab_columns where lower(table_name)='ndfl_person_prepayment' and lower(column_name)='modified_date';
	IF v_count=0 THEN
		dbms_output.put_line('add column MODIFIED_DATE to table NDFL_PERSON_PREPAYMENT');
		EXECUTE IMMEDIATE 'alter table ndfl_person_prepayment add modified_date date';
		
		select count(1) into v_count from user_tab_columns where lower(table_name)='ndfl_person_prepayment' and lower(column_name)='modified_date';
		IF v_count>0 THEN
			dbms_output.put_line('Column MODIFIED_DATE was added to table NDFL_PERSON_PREPAYMENT');
			execute immediate 'comment on column ndfl_person_prepayment.modified_date is ''Дата и время редактирования. Заполняется при редактировании данных НФ через загрузку Excel файла''';
		END IF; 
	END IF; 
	
	select count(1) into v_count from user_tab_columns where lower(table_name)='ndfl_person_prepayment' and lower(column_name)='modified_by';
	IF v_count=0 THEN
		dbms_output.put_line('add column MODIFIED_BY to table NDFL_PERSON_PREPAYMENT');
		EXECUTE IMMEDIATE 'alter table ndfl_person_prepayment add modified_by varchar2(255 char)';
		
		select count(1) into v_count from user_tab_columns where lower(table_name)='ndfl_person_prepayment' and lower(column_name)='modified_by';
		IF v_count>0 THEN
			dbms_output.put_line('Column MODIFIED_BY was added to table NDFL_PERSON_PREPAYMENT');
			execute immediate 'comment on column ndfl_person_prepayment.modified_by is ''Значение имени пользователя и логин из Справочника пользователей системы. Заполняется при редактировании данных НФ через загрузку Excel файла''';
		END IF; 
	END IF; 
	
	
	--https://jira.aplana.com/browse/SBRFNDFL-3380 DECLARATION_TYPE. Удалить IS_IFRS, IFRS_NAME изменения по БД
	select count(1) into v_count from user_tab_columns where lower(table_name)='declaration_type' and lower(column_name)='is_ifrs';
	IF v_count>0 THEN
		dbms_output.put_line('drop column IS_IFRS from table DECLARATION_TYPE');
		EXECUTE IMMEDIATE 'alter table declaration_type drop column is_ifrs';
		
		select count(1) into v_count from user_tab_columns where lower(table_name)='declaration_type' and lower(column_name)='is_ifrs';
		IF v_count=0 THEN
			dbms_output.put_line('Column IS_IFRS was dropped from table DECLARATION_TYPE');
		END IF; 
	END IF; 
	
	select count(1) into v_count from user_tab_columns where lower(table_name)='declaration_type' and lower(column_name)='ifrs_name';
	IF v_count>0 THEN
		dbms_output.put_line('drop column IFRS_NAME from table DECLARATION_TYPE');
		EXECUTE IMMEDIATE 'alter table declaration_type drop column ifrs_name';
		
		select count(1) into v_count from user_tab_columns where lower(table_name)='declaration_type' and lower(column_name)='ifrs_name';
		IF v_count=0 THEN
			dbms_output.put_line('Column IFRS_NAME was dropped from table DECLARATION_TYPE');
		END IF; 
	END IF; 
	
	EXECUTE IMMEDIATE 'comment on table declaration_type is ''Виды деклараций''';
	
	--https://jira.aplana.com/browse/SBRFNDFL-3765 Увеличить размеры полей: "Район", "Город", "Населенный пункт", "Улица" в справочнике ФЛ
	dbms_output.put_line('modify column REF_BOOK_ADDRESS.DISTRICT');
	execute	immediate 'alter table ref_book_address modify (district VARCHAR2(100 CHAR))';
	select count(1) into v_count from user_tab_columns where lower(table_name)='ref_book_address' and lower(column_name)='district' and data_type='VARCHAR2' and char_length=100;
	if v_count>0 then
		dbms_output.put_line('Column REF_BOOK_ADDRESS.DISTRICT modified');
	end if;
	
	dbms_output.put_line('modify column REF_BOOK_ADDRESS.CITY');
	execute	immediate 'alter table ref_book_address modify (city VARCHAR2(100 CHAR))';
	select count(1) into v_count from user_tab_columns where lower(table_name)='ref_book_address' and lower(column_name)='city' and data_type='VARCHAR2' and char_length=100;
	if v_count>0 then
		dbms_output.put_line('Column REF_BOOK_ADDRESS.CITY modified');
	end if;
	
	dbms_output.put_line('modify column REF_BOOK_ADDRESS.LOCALITY');
	execute	immediate 'alter table ref_book_address modify (locality VARCHAR2(100 CHAR))';
	select count(1) into v_count from user_tab_columns where lower(table_name)='ref_book_address' and lower(column_name)='locality' and data_type='VARCHAR2' and char_length=100;
	if v_count>0 then
		dbms_output.put_line('Column REF_BOOK_ADDRESS.LOCALITY modified');
	end if;
	
	dbms_output.put_line('modify column REF_BOOK_ADDRESS.STREET');
	execute	immediate 'alter table ref_book_address modify (street VARCHAR2(100 CHAR))';
	select count(1) into v_count from user_tab_columns where lower(table_name)='ref_book_address' and lower(column_name)='street' and data_type='VARCHAR2' and char_length=100;
	if v_count>0 then
		dbms_output.put_line('Column REF_BOOK_ADDRESS.STREET modified');
	end if;
		
	--https://jira.aplana.com/browse/SBRFNDFL-3818
	select count(1) into v_count from user_tab_columns where lower(table_name)='ref_book_income_kind' and lower(column_name)='version';
	IF v_count=0 THEN
		dbms_output.put_line('add column version to table ref_book_income_kind');
		EXECUTE IMMEDIATE 'alter table ref_book_income_kind add version date default to_date(''01.01.0001'',''DD.MM.YYYY'') not null';
		
		select count(1) into v_count from user_tab_columns where lower(table_name)='ref_book_income_kind' and lower(column_name)='version';
		IF v_count>0 THEN
			dbms_output.put_line('Column version was added to table ref_book_income_kind');
		END IF; 
	END IF; 
	
	select count(1) into v_count from user_tab_columns where lower(table_name)='ref_book_income_kind' and lower(column_name)='status';
	IF v_count=0 THEN
		dbms_output.put_line('add column status to table ref_book_income_kind');
		EXECUTE IMMEDIATE 'alter table ref_book_income_kind add status number(1) default 0 not null';
		
		select count(1) into v_count from user_tab_columns where lower(table_name)='ref_book_income_kind' and lower(column_name)='status';
		IF v_count>0 THEN
			dbms_output.put_line('Column status was added to table ref_book_income_kind');
		END IF; 
	END IF; 
	
	select count(1) into v_count from user_tab_columns where lower(table_name)='ref_book_income_kind' and lower(column_name)='record_id';
	IF v_count=0 THEN
		dbms_output.put_line('add column record_id to table ref_book_income_kind');
		EXECUTE IMMEDIATE 'alter table ref_book_income_kind add record_id number(18) null';
		
		select count(1) into v_count from user_tab_columns where lower(table_name)='ref_book_income_kind' and lower(column_name)='record_id';
		IF v_count>0 THEN
			dbms_output.put_line('Column record_id was added to table ref_book_income_kind');
		END IF; 
	END IF; 
	
	execute immediate 'update REF_BOOK_INCOME_KIND set record_id = id';
	commit;
	
	select count(1) into v_count from user_tab_columns where table_name='REF_BOOK_INCOME_KIND' and column_name='RECORD_ID' and data_type='NUMBER'and nullable='Y';
	if v_count>0 then
		dbms_output.put_line('alter table REF_BOOK_INCOME_KIND');
		execute	immediate 'alter table REF_BOOK_INCOME_KIND modify record_id number(18) not null';
		select count(1) into v_count from user_tab_columns where table_name='REF_BOOK_INCOME_KIND' and column_name='RECORD_ID' and data_type='NUMBER' and data_precision=18 and nullable='N';
		if v_count>0 then
			dbms_output.put_line('Table REF_BOOK_INCOME_KIND altered');
		end if;
	end if;
	
	select count(1) into v_count from user_tab_columns where lower(table_name)='report_period_type' and lower(column_name)='version';
	IF v_count=0 THEN
		dbms_output.put_line('add column version to table report_period_type');
		EXECUTE IMMEDIATE 'alter table report_period_type add version date default to_date(''01.01.0001'',''DD.MM.YYYY'') not null';
		
		select count(1) into v_count from user_tab_columns where lower(table_name)='report_period_type' and lower(column_name)='version';
		IF v_count>0 THEN
			dbms_output.put_line('Column version was added to table report_period_type');
		END IF; 
	END IF; 
	
	select count(1) into v_count from user_tab_columns where lower(table_name)='report_period_type' and lower(column_name)='status';
	IF v_count=0 THEN
		dbms_output.put_line('add column status to table report_period_type');
		EXECUTE IMMEDIATE 'alter table report_period_type add status number(1) default 0 not null';
		
		select count(1) into v_count from user_tab_columns where lower(table_name)='report_period_type' and lower(column_name)='status';
		IF v_count>0 THEN
			dbms_output.put_line('Column status was added to table report_period_type');
		END IF; 
	END IF; 
	
	select count(1) into v_count from user_tab_columns where lower(table_name)='report_period_type' and lower(column_name)='record_id';
	IF v_count=0 THEN
		dbms_output.put_line('add column record_id to table report_period_type');
		EXECUTE IMMEDIATE 'alter table report_period_type add record_id number(18) null';
		
		select count(1) into v_count from user_tab_columns where lower(table_name)='report_period_type' and lower(column_name)='record_id';
		IF v_count>0 THEN
			dbms_output.put_line('Column record_id was added to table report_period_type');
		END IF; 
	END IF; 
	
	execute immediate 'update REPORT_PERIOD_TYPE set record_id = id';
	commit;
	
	select count(1) into v_count from user_tab_columns where table_name='REPORT_PERIOD_TYPE' and column_name='RECORD_ID' and data_type='NUMBER'and nullable='Y';
	if v_count>0 then
		dbms_output.put_line('alter table REPORT_PERIOD_TYPE');
		execute	immediate 'alter table REPORT_PERIOD_TYPE modify record_id number(18) not null';
		select count(1) into v_count from user_tab_columns where table_name='REPORT_PERIOD_TYPE' and column_name='RECORD_ID' and data_type='NUMBER' and data_precision=18 and nullable='N';
		if v_count>0 then
			dbms_output.put_line('Table REPORT_PERIOD_TYPE altered');
		end if;
	end if;
END;
/