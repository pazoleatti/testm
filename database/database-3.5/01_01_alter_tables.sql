-- 3.5-ytrofimov-2 https://jira.aplana.com/browse/SBRFNDFL-6552 Сортировка РНУ реализована не по постановке
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #1 - ndfl_person_income add operation_date';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where lower(table_name)='ndfl_person_income' and lower(column_name)='operation_date';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ndfl_person_income add operation_date date';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
COMMIT;

-- 3.5-ytrofimov-2 https://jira.aplana.com/browse/SBRFNDFL-6552 Сортировка РНУ реализована не по постановке
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #2 - ndfl_person_income add action_date';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where lower(table_name)='ndfl_person_income' and lower(column_name)='action_date';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ndfl_person_income add action_date date';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
COMMIT;

-- 3.5-ytrofimov-2 https://jira.aplana.com/browse/SBRFNDFL-6552 Сортировка РНУ реализована не по постановке
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #3 - ndfl_person_income add row_type';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where lower(table_name)='ndfl_person_income' and lower(column_name)='row_type';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ndfl_person_income add row_type number(3,0)';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
COMMIT;

-- 3.5-amandzyak-2 https://jira.aplana.com/browse/SBRFNDFL-6552 Сортировка РНУ реализована не по постановке
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #4 - ndfl_person_income add constraint ndfl_person_inc_chk_row_type';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_constraints where lower(constraint_name)='ndfl_person_inc_chk_row_type' and lower(table_name)='ndfl_person_income';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ndfl_person_income add constraint ndfl_person_inc_chk_row_type check(row_type in (100, 200, 300))';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
COMMIT;

-- 3.5-ytrofimov-3 https://jira.aplana.com/browse/SBRFNDFL-6552 Сортировка РНУ реализована не по постановке
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #5 - comment on columns ndfl_person_income';  
BEGIN
	execute immediate 'comment on column ndfl_person_income.operation_date is ''Поле, используемое для сортировки Раздела 2  Хранит дату первого начисления в рамках этой операции (набора строк формы с одинаковым ID операции)''';
	execute immediate 'comment on column ndfl_person_income.action_date is ''Поле, используемое для сортировки Раздела 2  Показывает дату действия, отражаемого в этой строке.''';
	execute immediate 'comment on column ndfl_person_income.row_type is ''Поле, используемое для сортировки Раздела 2''';
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
COMMIT;

-- 3.5-dnovikov-4 https://jira.aplana.com/browse/SBRFNDFL-6513 новые атрибуты формы для нераспределенных отрицательных сумм
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #6 - declaration_data add negative_income';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where lower(table_name)='declaration_data' and lower(column_name)='negative_income';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table declaration_data add negative_income number(20,2)';
		execute immediate 'comment on column declaration_data.negative_income is ''Нераспределенный отрицательный Доход''';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
COMMIT;

-- 3.5-dnovikov-5 https://jira.aplana.com/browse/SBRFNDFL-6513 новые атрибуты формы для нераспределенных отрицательных сумм
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #7 - declaration_data add negative_tax';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where lower(table_name)='declaration_data' and lower(column_name)='negative_tax';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table declaration_data add negative_tax number(20,2)';
		execute immediate 'comment on column declaration_data.negative_tax is ''Нераспределенный отрицательный Налог''';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
COMMIT;

-- 3.5-dnovikov-6 https://jira.aplana.com/browse/SBRFNDFL-6513 новые атрибуты формы для нераспределенных отрицательных сумм
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #8 - declaration_data add negative_sums_sign';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where lower(table_name)='declaration_data' and lower(column_name)='negative_sums_sign';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table declaration_data add negative_sums_sign number(1)';
		execute immediate 'comment on column declaration_data.negative_sums_sign is ''Признак нераспределенных сумм (0 - из текущей формы, 1 - из предыдущей формы)''';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
COMMIT;

-- 3.5-dnovikov-12 https://jira.aplana.com/browse/SBRFNDFL-6408 Корректировка описания лица, подписавшего документ
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #9 - ref_book_signatory_mark modify name';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_signatory_mark' and lower(column_name)='name';
	IF v_run_condition=1 THEN
		select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_signatory_mark' and lower(column_name)='name' and data_type = 'VARCHAR2' and char_length < 100;
		IF v_run_condition=1 THEN
			EXECUTE IMMEDIATE 'alter table ref_book_signatory_mark modify name VARCHAR2(100 CHAR)';
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

-- 3.5-snazin-5 https://jira.aplana.com/browse/SBRFNDFL-6759 В БД под хранение параметра Сумма платежного поручения в раздела 2 РНУ отведено не 20 чисел, а 10
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #10 - ndfl_person_income modify tax_summ';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ndfl_person_income' and lower(column_name)='tax_summ';
	IF v_run_condition=1 THEN
		select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ndfl_person_income' and lower(column_name)='tax_summ' and data_type = 'NUMBER' and data_precision < 20;
		IF v_run_condition=1 THEN
			EXECUTE IMMEDIATE 'alter table ndfl_person_income modify tax_summ number(20)';
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

-- 3.5-ytrofimov-11 https://jira.aplana.com/browse/SBRFNDFL-6678 2 узла берут в работу одну асинхронную задачу
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #11 - alter table async_task drop column task_group ';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='async_task' and lower(column_name)='task_group';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table async_task drop column task_group';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
END;
/
COMMIT;
