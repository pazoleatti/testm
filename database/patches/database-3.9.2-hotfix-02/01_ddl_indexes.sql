--создание новых индексов
DECLARE
	v_index_name varchar(30) :='IDX_REF_BOOK_PERSON_REPORT_DOC';
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #1 - create index '||v_index_name;  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition=1 THEN
		execute immediate 'create index '|| v_index_name ||' on REF_BOOK_PERSON (REPORT_DOC ASC)
					PCTFREE 20 INITRANS 5 COMPUTE STATISTICS ONLINE';
		dbms_output.put_line(v_task_name||'[INFO ('||v_index_name||')]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING ('||v_index_name||')]:'||' changes had already been implemented');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
commit;
/


DECLARE
	v_index_name varchar(30) :='IDX_DECLARATION_DATA_PERSON_ID';
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #2 - create index '||v_index_name;  

BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition=1 THEN
		execute immediate 'create index '|| v_index_name ||' on DECLARATION_DATA (PERSON_ID ASC)
				   COMPUTE STATISTICS ONLINE';
		dbms_output.put_line(v_task_name||'[INFO ('||v_index_name||')]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING ('||v_index_name||')]:'||' changes had already been implemented');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
commit
/

DECLARE
	v_index_name varchar(30) :='IDX_NDFL_PERSON_PERSON_ID';
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #3 - create index '||v_index_name;  

BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition=1 THEN
		execute immediate 'create index '|| v_index_name ||' on NDFL_PERSON (PERSON_ID ASC)
				   PCTFREE 50 INITRANS 5 COMPUTE STATISTICS ONLINE';
		dbms_output.put_line(v_task_name||'[INFO ('||v_index_name||')]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING ('||v_index_name||')]:'||' changes had already been implemented');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
commit
/

DECLARE
	v_index_name varchar(30) :='IDX_NDFL_REFERENCES_PERSON_ID';
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #4 - create index '||v_index_name;  

BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition=1 THEN
		execute immediate 'create index '|| v_index_name ||' on NDFL_REFERENCES (PERSON_ID ASC)
				   PCTFREE 50 INITRANS 5 COMPUTE STATISTICS  ONLINE';
		dbms_output.put_line(v_task_name||'[INFO ('||v_index_name||')]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING ('||v_index_name||')]:'||' changes had already been implemented');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
commit
/

DECLARE
	v_index_name varchar(30) :='IDX_LOG_BUSINESS_PERSON_ID' ;
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #5 - create index '||v_index_name;  

BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition=1 THEN
		execute immediate 'create index '|| v_index_name ||' on LOG_BUSINESS (PERSON_ID ASC)
				   INITRANS 5 COMPUTE STATISTICS ONLINE';
		dbms_output.put_line(v_task_name||'[INFO ('||v_index_name||')]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING ('||v_index_name||')]:'||' changes had already been implemented');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
commit
/

