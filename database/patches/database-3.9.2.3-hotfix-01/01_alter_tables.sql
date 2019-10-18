DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #1 - alter table ref_book_id_doc';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where TABLE_NAME='REF_BOOK_ID_DOC' and COLUMN_NAME='SEARCH_DOC_NUMBER';
	IF v_run_condition=1 THEN
	        execute immediate 'alter table ref_book_id_doc add  SEARCH_DOC_NUMBER VARCHAR2(25 CHAR)';
		dbms_output.put_line(v_task_name||'[INFO ]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
commit;
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #2 - alter table ref_book_person';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where TABLE_NAME='REF_BOOK_PERSON' and COLUMN_NAME='SEARCH_LAST_NAME';
	IF v_run_condition=1 THEN
	        execute immediate 'alter table ref_book_person add SEARCH_LAST_NAME VARCHAR2(60 CHAR)';
		dbms_output.put_line(v_task_name||'[INFO ]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
commit;
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #3 - alter table ref_book_person';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where TABLE_NAME='REF_BOOK_PERSON' and COLUMN_NAME='SEARCH_FIRST_NAME';
	IF v_run_condition=1 THEN
	        execute immediate 'alter table ref_book_person add SEARCH_FIRST_NAME VARCHAR2(60 CHAR)';
		dbms_output.put_line(v_task_name||'[INFO ]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
commit;		
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #4 - alter table ref_book_person';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where TABLE_NAME='REF_BOOK_PERSON' and COLUMN_NAME='SEARCH_MIDDLE_NAME';
	IF v_run_condition=1 THEN
	        execute immediate 'alter table ref_book_person add SEARCH_MIDDLE_NAME VARCHAR2(60 CHAR)';
		dbms_output.put_line(v_task_name||'[INFO ]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/                                      
commit;

DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #5 - alter table ref_book_person';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where TABLE_NAME='REF_BOOK_PERSON' and COLUMN_NAME='SEARCH_INN';
	IF v_run_condition=1 THEN
	        execute immediate 'alter table ref_book_person add SEARCH_INN VARCHAR2(12 CHAR)';
		dbms_output.put_line(v_task_name||'[INFO ]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
commit;

DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #6 - alter table ref_book_person';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where TABLE_NAME='REF_BOOK_PERSON' and COLUMN_NAME='SEARCH_INN_FOREIGN';
	IF v_run_condition=1 THEN
	        execute immediate 'alter table ref_book_person add SEARCH_INN_FOREIGN VARCHAR2(50 CHAR)';
		dbms_output.put_line(v_task_name||'[INFO ]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
commit;

		
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #7 - alter table ref_book_person';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where TABLE_NAME='REF_BOOK_PERSON' and COLUMN_NAME='SEARCH_SNILS';
	IF v_run_condition=1 THEN
	        execute immediate 'alter table ref_book_person add SEARCH_SNILS VARCHAR2(14 CHAR)';
		dbms_output.put_line(v_task_name||'[INFO ]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
commit;

