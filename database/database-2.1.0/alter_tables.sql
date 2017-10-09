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
end;
/