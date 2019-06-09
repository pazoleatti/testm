--https://jira.aplana.com/browse/SBRFNDFL-5245 Изменить возможные значения поля format из справочника ref_book_attribute
declare 
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #1 - alter table ref_book_attribute drop constraint REF_BOOK_ATTRIBUTE_CHK_FORMAT (SBRFNDFL-5184)';  
begin
	select count(*) into v_run_condition from user_constraints where lower(constraint_name)='ref_book_attribute_chk_format';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_attribute drop constraint REF_BOOK_ATTRIBUTE_CHK_FORMAT';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
	
    v_task_name:='alter_tables block #2 - alter table ref_book_attribute add constraint REF_BOOK_ATTRIBUTE_CHK_FORMAT (SBRFNDFL-5184)';  
	--без проверки, т.к. на предыдущем шаге создаваемое ограничение должно было удалиться
	EXECUTE IMMEDIATE 'alter table ref_book_attribute add constraint REF_BOOK_ATTRIBUTE_CHK_FORMAT CHECK (format in (0,1,2,3,4,5,6,7)) ENABLE';
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
		
    v_task_name:='alter_tables block #3 - comment on column ref_book_attribute.format (SBRFNDFL-5184)';  	
	EXECUTE IMMEDIATE 'comment on column ref_book_attribute.format is ''Формат. (Для дат: 0 - "", 1 - "dd.MM.yyyy", 2 - "MM.yyyy", 3 - "MMMM yyyy", 4 - "yyyy", 5 - "dd.MM", 7 - "dd.MM.yyyy HH:mm:ss"; Для чисел: 6 - чекбокс)''';
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
end;
/

--https://jira.aplana.com/browse/SBRFNDFL-5299 Реализовать добавление в РНУ колонки с АСНУ и фильтра по этой колонке
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #4 - alter table ndfl_person add asnu_id (SBRFNDFL-5299)';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where lower(table_name)='ndfl_person' and lower(column_name)='asnu_id';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ndfl_person add asnu_id number(18)';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
	v_task_name:='alter_tables block #5 - comment on column ndfl_person.asnu_id (SBRFNDFL-5199)';  	
	EXECUTE IMMEDIATE 'comment on column ndfl_person.asnu_id is ''Идентификатор АСНУ''';		
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

--https://jira.aplana.com/browse/SBRFNDFL-5299 Реализовать добавление в РНУ колонки с АСНУ и фильтра по этой колонке
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #6 - alter table ndfl_person_deduction add asnu_id (SBRFNDFL-5299)';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where lower(table_name)='ndfl_person_deduction' and lower(column_name)='asnu_id';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ndfl_person_deduction add asnu_id number(18)';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
	
	v_task_name:='alter_tables block #7 - comment on column ndfl_person_deduction.asnu_id (SBRFNDFL-5199)';  	
	EXECUTE IMMEDIATE 'comment on column ndfl_person_deduction.asnu_id is ''Идентификатор АСНУ''';		
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

--https://jira.aplana.com/browse/SBRFNDFL-5299 Реализовать добавление в РНУ колонки с АСНУ и фильтра по этой колонке
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #8 - alter table ndfl_person_income add asnu_id (SBRFNDFL-5299)';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where lower(table_name)='ndfl_person_income' and lower(column_name)='asnu_id';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ndfl_person_income add asnu_id number(18)';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
	
	v_task_name:='alter_tables block #9 - comment on column ndfl_person_income.asnu_id (SBRFNDFL-5199)';  	
	EXECUTE IMMEDIATE 'comment on column ndfl_person_income.asnu_id is ''Идентификатор АСНУ''';		
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

--https://jira.aplana.com/browse/SBRFNDFL-5299 Реализовать добавление в РНУ колонки с АСНУ и фильтра по этой колонке
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #10 - alter table ndfl_person_prepayment add asnu_id (SBRFNDFL-5299)';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where lower(table_name)='ndfl_person_prepayment' and lower(column_name)='asnu_id';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ndfl_person_prepayment add asnu_id number(18)';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
	
	v_task_name:='alter_tables block #11 - comment on column ndfl_person_prepayment.asnu_id (SBRFNDFL-5199)';  	
	EXECUTE IMMEDIATE 'comment on column ndfl_person_prepayment.asnu_id is ''Идентификатор АСНУ''';		
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
END;
/

--https://jira.aplana.com/browse/SBRFNDFL-5415 Реализовать изменения в алгоритме идентификации по хранимым процедурам
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #12 - alter table ref_book_person drop column employee (SBRFNDFL-5415)';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='employee';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_person drop column employee';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
END;
/

-- https://jira.aplana.com/browse/SBRFNDFL-5346 Добавление новых полей к справочнику физ. лиц
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #13 - alter table ref_book_person add column report_doc (SBRFNDFL-5346)';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='report_doc';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_person add report_doc number(18)';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 		
	
	v_task_name:='alter_tables block #14 - comment on column ref_book_person.report_doc (SBRFNDFL-5346)';  	
	EXECUTE IMMEDIATE 'comment on column ref_book_person.report_doc is ''Ссылка на ДУЛ, который должен включаться в отчетность''';		
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');

	v_task_name:='alter_tables block #15 - alter table ref_book_person add constraint fk_ref_book_person_report_doc (SBRFNDFL-5346)';  	
	select decode(count(*),0,1,0) into v_run_condition from user_constraints where lower(constraint_name)='fk_ref_book_person_report_doc';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_person add constraint fk_ref_book_person_report_doc foreign key (report_doc) references ref_book_id_doc(id)';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

-- https://jira.aplana.com/browse/SBRFNDFL-5346 Добавление новых полей к справочнику физ. лиц
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #16 - alter table ref_book_person add column vip (SBRFNDFL-5346)';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='vip';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_person add vip number(1) default 0 not null';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 					

	v_task_name:='alter_tables block #17 - comment on column ref_book_person.vip (SBRFNDFL-5346)';  	
	EXECUTE IMMEDIATE 'comment on column ref_book_person.vip is ''Признак, показывающий, является ли ФЛ VIP-ом (0 - Не VIP, 1 - VIP)''';		
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	
	v_task_name:='alter_tables block #18 - alter table ref_book_person add constraint chk_ref_book_person_vip (SBRFNDFL-5346)';  	
	select decode(count(*),0,1,0) into v_run_condition from user_constraints where lower(constraint_name)='chk_ref_book_person_vip';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_person add constraint chk_ref_book_person_vip check (vip in (0, 1)) ENABLE';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

-- https://conf.aplana.com/pages/viewpage.action?pageId=40019337 "На что обратить внимание", пункт 2.b
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #19 - alter table ref_book_id_doc add constraint chk_ref_book_id_doc_inc_rep';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_constraints where lower(constraint_name)='chk_ref_book_id_doc_inc_rep';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_id_doc add constraint chk_ref_book_id_doc_inc_rep check (inc_rep in (0, 1)) ENABLE';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

--https://jira.aplana.com/browse/SBRFNDFL-2158 Реализовать работу с обновленными настройками подразделений
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #20 - alter table ref_book_ndfl_detail drop column ref_book_ndfl_id (SBRFNDFL-2158)';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_ndfl_detail' and lower(column_name)='ref_book_ndfl_id';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_ndfl_detail drop column ref_book_ndfl_id';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
END;
/

--https://jira.aplana.com/browse/SBRFNDFL-2158 Реализовать работу с обновленными настройками подразделений
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #21 - alter table ref_book_ndfl_detail drop column row_ord (SBRFNDFL-2158)';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_ndfl_detail' and lower(column_name)='row_ord';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_ndfl_detail drop column row_ord';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
END;
/

--https://jira.aplana.com/browse/SBRFNDFL-2158 Реализовать работу с обновленными настройками подразделений
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #22 - alter table ref_book_ndfl_detail drop column obligation (SBRFNDFL-2158)';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_ndfl_detail' and lower(column_name)='obligation';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_ndfl_detail drop column obligation';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
END;
/

--https://jira.aplana.com/browse/SBRFNDFL-2158 Реализовать работу с обновленными настройками подразделений
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #23 - alter table ref_book_ndfl_detail drop column okved (SBRFNDFL-2158)';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_ndfl_detail' and lower(column_name)='okved';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_ndfl_detail drop column okved';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
END;
/

--https://jira.aplana.com/browse/SBRFNDFL-2158 Реализовать работу с обновленными настройками подразделений
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #24 - alter table ref_book_ndfl_detail drop column region (SBRFNDFL-2158)';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_ndfl_detail' and lower(column_name)='region';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_ndfl_detail drop column region';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
END;
/

--https://jira.aplana.com/browse/SBRFNDFL-2158 Реализовать работу с обновленными настройками подразделений
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #25 - alter table ref_book_ndfl_detail drop column type (SBRFNDFL-2158)';  
BEGIN
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_ndfl_detail' and lower(column_name)='type';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_ndfl_detail drop column type';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
END;
/

--https://jira.aplana.com/browse/SBRFNDFL-2158 Реализовать работу с обновленными настройками подразделений
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #26 - alter table ref_book_ndfl rename to ref_book_ndfl_old (SBRFNDFL-2158)';  
BEGIN
	select count(*) into v_run_condition from user_tables where lower(table_name)='ref_book_ndfl';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_ndfl rename to ref_book_ndfl_old';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
END;
/

--https://jira.aplana.com/browse/SBRFNDFL-5570 Идентификация формы 25609 идет более 30 минут -->
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #27 - CREATE INDEX idx_ref_book_person_tb_pers (SBRFNDFL-5570)';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where lower(index_name)='idx_ref_book_person_tb_pers';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'CREATE INDEX idx_ref_book_person_tb_pers ON REF_BOOK_PERSON_TB (PERSON_ID)';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
END;
/
