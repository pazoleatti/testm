-- 3.5-dnovikov-16, 3.5-ytrofimov-18 https://jira.aplana.com/browse/SBRFNDFL-7269 Изменить размерность поля "Наименование" в Справочнике Виды дохода, добавить новые виды доходов в справочник
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #1 - ref_book_income_kind modify name';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_income_kind' and lower(column_name)='name';
	IF v_run_condition=1 THEN
		select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_income_kind' and lower(column_name)='name' and data_type = 'VARCHAR2' and char_length < 2000;
		IF v_run_condition=1 THEN
			EXECUTE IMMEDIATE 'alter table ref_book_income_kind modify name VARCHAR2(2000 CHAR)';
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
