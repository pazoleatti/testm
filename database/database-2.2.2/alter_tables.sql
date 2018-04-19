DECLARE
	v_count number;
BEGIN

	select count(1) into v_count from user_tab_columns where lower(table_name)='ref_book' and lower(column_name)='xsd_id';
	IF v_count=0 THEN
		dbms_output.put_line('add column XSD_ID to table REF_BOOK');
		EXECUTE IMMEDIATE 'alter table ref_book add xsd_id varchar2(36 byte)';
		
		select count(1) into v_count from user_tab_columns where lower(table_name)='ref_book' and lower(column_name)='xsd_id';
		IF v_count>0 THEN
			dbms_output.put_line('Column XSD_ID was added to table REF_BOOK');
			execute immediate 'comment on column ref_book.xsd_id is ''Идентификатор связанного XSD файла''';
		END IF; 
	END IF; 
	
	select count(1) into v_count from user_tab_columns where lower(table_name)='async_task' and lower(column_name)='task_group';
	IF v_count=0 THEN
		dbms_output.put_line('add column TASK_GROUP to table ASYNC_TASK');
		EXECUTE IMMEDIATE 'alter table async_task add task_group number(3)';
		
		select count(1) into v_count from user_tab_columns where lower(table_name)='async_task' and lower(column_name)='task_group';
		IF v_count>0 THEN
			dbms_output.put_line('Column TASK_GROUP was added to table ASYNC_TASK');
			execute immediate 'comment on column async_task.task_group is ''Группа асинхронной задачи''';
		END IF; 
	END IF; 
	

END;
/