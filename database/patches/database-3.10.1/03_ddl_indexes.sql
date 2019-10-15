--создание новых индексов
DECLARE
	v_index_name varchar(30) :='IDX_REF_BOOK_PERSON_REPORT_DOC';
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #1 - create index '||v_index_name;  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition=1 THEN
		execute immediate 'create index '|| v_index_name ||' on REF_BOOK_PERSON (REPORT_DOC ASC)
					PCTFREE 20 INITRANS 5 COMPUTE STATISTICS';
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
				   COMPUTE STATISTICS';
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
				   PCTFREE 50 INITRANS 5 COMPUTE STATISTICS';
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
				   PCTFREE 50 INITRANS 5 COMPUTE STATISTICS';
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
				   INITRANS 5 COMPUTE STATISTICS';
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
	v_index_name varchar(30) :='IDX_LOG_BUSINESS_LOG_ID';
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #6 - create index '||v_index_name;  

BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition=1 THEN
		execute immediate 'create index '|| v_index_name ||' on LOG_BUSINESS (LOG_ID ASC)
				   PCTFREE 50 INITRANS 5 COMPUTE STATISTICS';
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

--изменение и перестройка старых

DECLARE
	v_index_name varchar(30) :='PK_REF_BOOK_PERSON'      ;
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #7 - alter index '||v_index_name;  

BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition>=1 THEN
		execute immediate 'alter index '|| v_index_name ||' REBUILD REVERSE COMPUTE STATISTICS';
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
	v_index_name varchar(30) :='PK_REF_BOOK_ID_TAX_PAYER' ;
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #8 - alter index '||v_index_name;  

BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition>=1 THEN
		execute immediate 'alter index '|| v_index_name ||' REBUILD REVERSE COMPUTE STATISTICS';
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
	v_index_name varchar(30) :='PK_REF_BOOK_ID_DOC'    ;
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #9 - alter index '||v_index_name;  

BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition>=1 THEN
		execute immediate 'alter index '|| v_index_name ||' REBUILD REVERSE COMPUTE STATISTICS';
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
	v_index_name varchar(30) :='DECLARATION_DATA_PK'  ;
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #10 - alter index '||v_index_name;  

BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition>=1 THEN
		execute immediate 'alter index '|| v_index_name ||' REBUILD REVERSE COMPUTE STATISTICS';
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
	v_index_name varchar(30) :='NDFL_PP_PK'     ;
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #11 - alter index '||v_index_name;  

BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition>=1 THEN
		execute immediate 'alter index '|| v_index_name ||' REBUILD REVERSE COMPUTE STATISTICS';
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
	v_index_name varchar(30) :='NDFL_PERSON_PK'    ;
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #12 - alter index '||v_index_name;  

BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition>=1 THEN
		execute immediate 'alter index '|| v_index_name ||' REBUILD REVERSE COMPUTE STATISTICS';
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
	v_index_name varchar(30) :='NDFL_PERSON_I_PK' ;
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #13 - alter index '||v_index_name;  

BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition>=1 THEN
		execute immediate 'alter index '|| v_index_name ||' REBUILD REVERSE COMPUTE STATISTICS';
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
	v_index_name varchar(30) :='NDFL_PD_PK'   ;
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #14 - alter index '||v_index_name;  

BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition>=1 THEN
		execute immediate 'alter index '|| v_index_name ||' REBUILD REVERSE COMPUTE STATISTICS';
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
	v_index_name varchar(30) :='LOG_BUSINESS_PK' ;
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #15 - alter index '||v_index_name;  

BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition>=1 THEN
		execute immediate 'alter index '|| v_index_name ||' REBUILD REVERSE COMPUTE STATISTICS';
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
	v_index_name varchar(30) :='TRANSPORT_MESSAGE_PK'  ;
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #16 - alter index '||v_index_name;  

BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition>=1 THEN
		execute immediate 'alter index '|| v_index_name ||' REBUILD REVERSE COMPUTE STATISTICS';
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
	v_index_name varchar(30) :='PK_NDFL_REFERENCES'   ;
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #17 - alter index '||v_index_name;  

BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition>=1 THEN
		execute immediate 'alter index '|| v_index_name ||' REBUILD REVERSE COMPUTE STATISTICS';
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
	v_index_name varchar(30) :='NDFL_PERS_INC_KPP_OKTMO' ;
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #18 - alter index '||v_index_name;  

BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition>=1 THEN
		execute immediate 'alter index '|| v_index_name ||' REBUILD
				 PCTFREE 50 INITRANS 50	 COMPUTE STATISTICS';
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
	v_index_name varchar(30) :='FK_LOG_ENTRY_LOG'  ;
	v_run_condition number(1);                     
	v_task_name varchar2(128):='alter_tables block #19 - alter index '||v_index_name;  

BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition>=1 THEN
		execute immediate 'alter index '|| v_index_name ||' REBUILD
				 PCTFREE 50 INITRANS 10	 COMPUTE STATISTICS';
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
	v_index_name varchar(30) :='IDX_LOG_ENTRY_DATE'  ;
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #20 - alter index '||v_index_name;  

BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition>=1 THEN
		execute immediate 'alter index '|| v_index_name ||' REBUILD
				 PCTFREE 50 INITRANS 10	 COMPUTE STATISTICS';
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
	v_index_name varchar(30) :='PK_LOG_ENTRY'     ;
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #21 - alter index '||v_index_name;  

BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition>=1 THEN
		execute immediate 'alter index '|| v_index_name ||' REBUILD
				 PCTFREE 50 INITRANS 10	 COMPUTE STATISTICS';
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
	v_index_name varchar(30) :='IDX_NDFL_PERSON_INC_PAYMDT'  ;
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #22 - alter index '||v_index_name;  

BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition>=1 THEN
		execute immediate 'alter index '|| v_index_name ||' REBUILD
				 INITRANS 50	 COMPUTE STATISTICS';
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
	v_index_name varchar(30) :='IDX_NDFL_PERSON_INC_TAXDT' ;
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #23 - alter index '||v_index_name;  

BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition>=1 THEN
		execute immediate 'alter index '|| v_index_name ||' REBUILD
				 PCTFREE 50 INITRANS 50	 COMPUTE STATISTICS';
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
	v_index_name varchar(30) :='IDX_NDFL_PERSON_DECL_DATA_ID';
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #24 - alter index '||v_index_name;  

BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition>=1 THEN
		execute immediate 'alter index '|| v_index_name ||' REBUILD
				 PCTFREE 50 INITRANS 50	 COMPUTE STATISTICS';
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
