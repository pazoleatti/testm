declare 
  v_count number;
begin
	select count(1) into v_count from user_tab_columns where lower(table_name)='async_task_type' and lower(column_name)='dev_mode';
	IF v_count>0 THEN
		EXECUTE IMMEDIATE 'alter table async_task_type drop column dev_mode';
	END IF; 
	
	select count(1) into v_count from user_tab_columns where lower(table_name)='async_task_type' and lower(column_name)='handler_jndi';
	IF v_count>0 THEN
		EXECUTE IMMEDIATE 'alter table async_task_type rename column handler_jndi to handler_bean';
		EXECUTE IMMEDIATE 'comment on column async_task_type.handler_bean is ''Имя spring бина-обработчика задачи''';
	END IF; 
	
	EXECUTE IMMEDIATE 'alter table configuration add value2 varchar2(2048 char) null';
	EXECUTE IMMEDIATE 'update configuration set value2=value';
	EXECUTE IMMEDIATE 'alter table configuration drop column value';
	EXECUTE IMMEDIATE 'alter table configuration rename column value2 to value';
	
	EXECUTE IMMEDIATE 'alter table declaration_type modify id number(18,0)';
	EXECUTE IMMEDIATE 'alter table department_declaration_type modify DECLARATION_TYPE_ID number(18,0)';
	EXECUTE IMMEDIATE 'alter table declaration_template modify DECLARATION_TYPE_ID number(18,0)';
	EXECUTE IMMEDIATE 'alter table ref_book_asnu modify id number(18,0)';
	
	select count(1) into v_count from user_tab_columns where lower(table_name)='department_report_period' and lower(column_name)='is_balance_period';
	IF v_count>0 THEN
		EXECUTE IMMEDIATE 'alter table department_report_period drop column is_balance_period';
	END IF; 
	
	select count(1) into v_count from user_tab_columns where lower(table_name)='declaration_data' and lower(column_name)='manually_created';
	IF v_count=0 THEN
		EXECUTE IMMEDIATE 'alter table declaration_data add manually_created number(1) default 0 not null';
		EXECUTE IMMEDIATE 'comment on column declaration_data.manually_created is ''Создана вручную (0-нет, 1-да)''';
	END IF; 
	
	select count(1) into v_count from user_tab_columns where lower(table_name)='ref_book_asnu' and lower(column_name)='priority';
	IF v_count=0 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_asnu add priority number(3)';
		EXECUTE IMMEDIATE 'comment on column ref_book_asnu.priority is ''Приоритет АСНУ. Определяет, нужно ли обновлять запись справочника "ФЛ" при идентификации''';
		EXECUTE IMMEDIATE 'comment on column ref_book_asnu.ROLE_ALIAS is ''Значение поля "Код роли" справочника "Системные роли" АСУН''';
		EXECUTE IMMEDIATE 'comment on column ref_book_asnu.ROLE_NAME is ''Значение поля "Наименование" справочника "Системные роли" АСУН''';
	END IF; 
	
	EXECUTE IMMEDIATE 'alter table declaration_report modify TYPE number(2,0)';
	
	select count(1) into v_count from user_tab_columns where lower(table_name)='declaration_data' and lower(column_name)='last_data_modified';
	IF v_count=0 THEN
		EXECUTE IMMEDIATE 'alter table declaration_data add last_data_modified date';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN DECLARATION_DATA.last_data_modified IS ''Дата последних изменений данных формы''';
		EXECUTE IMMEDIATE 'update DECLARATION_DATA set last_data_modified=sysdate';
	END IF;
end;
/