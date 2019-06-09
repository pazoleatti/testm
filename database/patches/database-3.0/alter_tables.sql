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

-----------------------------------------------------------------------------------------------------------------------------
--> https://jira.aplana.com/browse/SBRFNDFL-5012 - Реализовать увеличение размерности параметров Раздела 1 РНУ
DECLARE 
  l_task_name varchar2(128) := 'ALTER_TABLES Block #1 - ALTER NDFL_PERSON (SBRFNDFL-5012)';
  l_run_condition decimal(1) := 0;
  l_sql_query varchar2(4000) := ''; 
BEGIN
  
  SELECT CASE WHEN count(*) = 1 THEN 0 ELSE 1 END INTO l_run_condition FROM user_tab_columns utcl WHERE utcl.table_name = 'NDFL_PERSON' AND utcl.column_name = 'LAST_NAME' AND utcl.CHAR_LENGTH = '60';

  IF l_run_condition = 1 THEN 
    --DDL

    EXECUTE IMMEDIATE 'ALTER TABLE NDFL_PERSON MODIFY (last_name VARCHAR2(60 CHAR), first_name VARCHAR2(60 CHAR), middle_name VARCHAR2(60 CHAR), flat VARCHAR2(20 CHAR))';
   
    dbms_output.put_line(l_task_name||'[INFO]:'||' Success');
  ELSE
    dbms_output.put_line(l_task_name||'[WARNING]:'||' changes had already been implemented');
  END IF;
  
EXCEPTION
  when OTHERS then
    dbms_output.put_line(l_task_name||'[FATAL]:'||sqlerrm);
end;
/

-----------------------------------------------------------------------------------------------------------------------------
