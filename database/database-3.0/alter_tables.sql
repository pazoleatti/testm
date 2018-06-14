DECLARE
	v_count number;
BEGIN

	select count(1) into v_count from user_tab_columns where lower(table_name)='declaration_data' and lower(column_name)='adjust_negative_values';
	IF v_count=0 THEN
		dbms_output.put_line('add column ADJUST_NEGATIVE_VALUES to table DECLARATION_DATA');
		EXECUTE IMMEDIATE 'alter table declaration_data add adjust_negative_values number(1) default 0 not null';
		
		select count(1) into v_count from user_tab_columns where lower(table_name)='declaration_data' and lower(column_name)='adjust_negative_values';
		IF v_count>0 THEN
			dbms_output.put_line('Column ADJUST_NEGATIVE_VALUES was added to table DECLARATION_DATA');
			execute immediate 'comment on column declaration_data.adjust_negative_values is ''Признак, показывающий необходимость корректировки отрицательных значений''';
		END IF; 
	END IF; 

END;
/