create or replace TRIGGER REF_BOOK_ID_DOC_BEFORE_INS_UPD
  before insert or update on ref_book_id_doc
  for each row
begin
        :new.search_doc_number := regexp_replace(:new.doc_number,'[^0-9A-Za-zА-Яа-я]','');        
end REF_BOOK_ID_DOC_BEFORE_INS_UPD;
/

create or replace TRIGGER REF_BOOK_PERSON_BEFORE_INS_UPD
  before insert or update on ref_book_person
  for each row
begin
        :new.search_LAST_NAME := replace(nvl(:new.last_name,'empty'),' ','');        

        :new.search_first_NAME := replace(nvl(:new.first_name,'empty'),' ','');        

        :new.search_middle_NAME := replace(nvl(:new.middle_name,'empty'),' ','');        

        :new.search_inn := replace(nvl(:new.inn,'empty'),' ','');        

        :new.search_inn_foreign := replace(nvl(:new.inn_foreign,'empty'),' ','');        

        :new.search_snils := replace(replace(nvl(:new.snils,'empty'),' ',''),'-','');
    
end REF_BOOK_PERSON_BEFORE_INS_UPD;
/


create or replace TRIGGER ndfl_PERSON_BEFORE_INS_UPD
  before insert or update on ndfl_person
  for each row
begin
        :new.search_LAST_NAME := replace(nvl(:new.last_name,'empty'),' ','');        

        :new.search_first_NAME := replace(nvl(:new.first_name,'empty'),' ','');        

        :new.search_middle_NAME := replace(nvl(:new.middle_name,'empty'),' ','');        

        :new.search_inn := replace(nvl(:new.inn_np,'empty'),' ','');        

        :new.search_inn_foreign := replace(nvl(:new.inn_foreign,'empty'),' ','');        

        :new.search_snils := replace(replace(nvl(:new.snils,'empty'),' ',''),'-','');

        :new.search_doc_number := regexp_replace(:new.id_doc_number,'[^0-9A-Za-zА-Яа-я]','');        
    
end ndfl_PERSON_BEFORE_INS_UPD;
/
commit;


DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='indexes block #1 - idx_ref_book_id_doc_srch_doc';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME='IDX_REF_BOOK_ID_DOC_SRCH_DOC';
	IF v_run_condition=1 THEN
	        execute immediate 'create index idx_ref_book_id_doc_srch_doc on ref_book_id_doc (search_doc_number asc)';
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
	v_task_name varchar2(128):='indexes block #2 - idx_ref_book_person_srch_fio';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME='IDX_REF_BOOK_PERSON_SRCH_FIO';
	IF v_run_condition=1 THEN
	        execute immediate 'create index idx_ref_book_person_srch_fio  on ref_book_person (search_last_name asc, search_first_name asc, search_middle_name asc, birth_date asc)';
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
	v_task_name varchar2(128):='indexes block #3 - idx_ref_book_person_srch_inn';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME='IDX_REF_BOOK_PERSON_SRCH_INN';
	IF v_run_condition=1 THEN
	        execute immediate 'create index idx_ref_book_person_srch_inn on ref_book_person (search_inn asc)';
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
	v_task_name varchar2(128):='indexes block #4 - idx_ref_book_person_srch_innf';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME='IDX_REF_BOOK_PERSON_SRCH_INNF';
	IF v_run_condition=1 THEN
	        execute immediate 'create index idx_ref_book_person_srch_innf on ref_book_person (search_inn_foreign asc)';
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
	v_task_name varchar2(128):='indexes block #5 - idx_ref_book_person_srch_snils';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME='IDX_REF_BOOK_PERSON_SRCH_SNILS';
	IF v_run_condition=1 THEN
	        execute immediate 'create index idx_ref_book_person_srch_snils on ref_book_person (search_snils asc)';
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
	v_task_name varchar2(128):='indexes block #6 - SRCH_FULL_REF_PERS_DUBLE';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME='SRCH_FULL_REF_PERS_DUBLE';
	IF v_run_condition=1 THEN
	        execute immediate 'CREATE INDEX SRCH_FULL_REF_PERS_DUBLE ON REF_BOOK_PERSON 
					(REPLACE(LOWER(NVL(LAST_NAME,''empty'')),'' '',''''), 
					REPLACE(LOWER(NVL(FIRST_NAME,''empty'')),'' '',''''), 
					REPLACE(LOWER(NVL(MIDDLE_NAME,''empty'')),'' '',''''), 
					BIRTH_DATE, 
					REPLACE(REPLACE(NVL(SNILS,''empty''),'' '',''''),''-'',''''), 
					REPLACE(NVL(INN,''empty''),'' '',''''), 
					REPLACE(NVL(INN_FOREIGN,''empty''),'' '',''''))';
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
	v_task_name varchar2(128):='indexes block #7 - SRCH_REF_BOOK_PERSON_INN';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME='SRCH_REF_BOOK_PERSON_INN';
	IF v_run_condition=1 THEN
	        execute immediate 'CREATE INDEX SRCH_REF_BOOK_PERSON_INN ON REF_BOOK_PERSON (REPLACE(INN,'' '',''''))';
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
	v_task_name varchar2(128):='indexes block #8 - SRCH_REF_BOOK_PERSON_INN_F';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME='SRCH_REF_BOOK_PERSON_INN_F';
	IF v_run_condition=1 THEN
	        execute immediate 'CREATE INDEX SRCH_REF_BOOK_PERSON_INN_F ON REF_BOOK_PERSON (REPLACE(INN_FOREIGN,'' '',''''))';
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
	v_task_name varchar2(128):='indexes block #9 - SRCH_REF_BOOK_PERSON_SNILS';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME='SRCH_REF_BOOK_PERSON_INN_F';
	IF v_run_condition=1 THEN
	        execute immediate 'CREATE INDEX SRCH_REF_BOOK_PERSON_SNILS ON REF_BOOK_PERSON (REPLACE(REPLACE(SNILS,'' '',''''),''-'',''''))';
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
	v_task_name varchar2(128):='indexes block #10 - SRCH_REF_PERSON_NAME_BRTHD';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME='SRCH_REF_PERSON_NAME_BRTHD';
	IF v_run_condition=1 THEN
	        execute immediate 'CREATE INDEX SRCH_REF_PERSON_NAME_BRTHD ON REF_BOOK_PERSON 
				  	(REPLACE(LOWER(LAST_NAME),'' '',''''), 
					REPLACE(LOWER(FIRST_NAME),'' '',''''), 
					REPLACE(LOWER(MIDDLE_NAME),'' '',''''), BIRTH_DATE) ';
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
	v_task_name varchar2(128):='indexes block #11 - SRCH_REF_PERSON_NAME_BRTHD';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME='SRCH_REF_PERSON_NAME_BRTHD';
	IF v_run_condition=1 THEN
	        execute immediate 'CREATE INDEX SRCH_REF_PERSON_NAME_BRTHD ON REF_BOOK_PERSON 
				  	(REPLACE(LOWER(LAST_NAME),'' '',''''), 
					REPLACE(LOWER(FIRST_NAME),'' '',''''), 
					REPLACE(LOWER(MIDDLE_NAME),'' '',''''), BIRTH_DATE) ';
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
	v_task_name varchar2(128):='indexes block #12 - SRCH_REF_BOOK_ID_DOC_TP_NUM';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME='SRCH_REF_BOOK_ID_DOC_TP_NUM';
	IF v_run_condition=1 THEN
	        execute immediate ' CREATE INDEX SRCH_REF_BOOK_ID_DOC_TP_NUM ON REF_BOOK_ID_DOC 
					(DOC_ID, REPLACE(LOWER(DOC_NUMBER),'' '',''''))';
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
	v_task_name varchar2(128):='indexes block #13 - SRCH_REFB_TAX_PAYER_INP_ASNU';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME='SRCH_REFB_TAX_PAYER_INP_ASNU';
	IF v_run_condition=1 THEN
	        execute immediate 'CREATE INDEX SRCH_REFB_TAX_PAYER_INP_ASNU ON REF_BOOK_ID_TAX_PAYER (AS_NU, LOWER(INP))';
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
	v_task_name varchar2(128):='indexes block #14 - IDX_NDFL_PERSON_SEARCH_DOC';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME='IDX_NDFL_PERSON_SEARCH_DOC';
	IF v_run_condition=1 THEN
	        execute immediate 'create index idx_ndfl_person_search_doc on ndfl_person 
			(declaration_data_id asc, person_id asc, id, search_doc_number asc) 
		PCTFREE 50 INITRANS 50 COMPUTE STATISTICS';
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
	v_task_name varchar2(128):='indexes block #15 - IDX_NDFL_PERSON_SEARCH_FIO';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME='IDX_NDFL_PERSON_SEARCH_FIO';
	IF v_run_condition=1 THEN
	        execute immediate 'create index idx_ndfl_person_search_fio on ndfl_person 
			(declaration_data_id asc, person_id asc, id, search_last_name asc, 
			search_first_name asc, search_middle_name asc, birth_day asc) 
		PCTFREE 50 INITRANS 50 COMPUTE STATISTICS';
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
	v_task_name varchar2(128):='indexes block #16 - IDX_NDFL_PERSON_SEARCH_INN';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME='IDX_NDFL_PERSON_SEARCH_INN';
	IF v_run_condition=1 THEN
	        execute immediate 'create index idx_ndfl_person_search_inn on ndfl_person 
			(declaration_data_id asc, person_id asc, id, search_inn asc) 
		PCTFREE 50 INITRANS 50 COMPUTE STATISTICS';
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
	v_task_name varchar2(128):='indexes block #17 - IDX_NDFL_PERSON_SEARCH_INNF';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME='IDX_NDFL_PERSON_SEARCH_INNF';
	IF v_run_condition=1 THEN
	        execute immediate 'create index idx_ndfl_person_search_innf on ndfl_person 
			(declaration_data_id asc, person_id asc, id, search_inn_foreign asc) 
		PCTFREE 50 INITRANS 50 COMPUTE STATISTICS';
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
	v_task_name varchar2(128):='indexes block #18 - IDX_NDFL_PERSON_SEARCH_SNILS';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME='IDX_NDFL_PERSON_SEARCH_SNILS';
	IF v_run_condition=1 THEN
	        execute immediate 'create index idx_ndfl_person_search_snils on ndfl_person 
			(declaration_data_id, person_id, id, search_snils) 
		PCTFREE 50 INITRANS 50 COMPUTE STATISTICS';
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
	v_task_name varchar2(128):='indexes block #18 - idx_ref_book_id_doc_srch_doc';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME='IDX_REF_BOOK_ID_DOC_SRCH_DOC';
	IF v_run_condition=1 THEN
	        execute immediate 'create index idx_ref_book_id_doc_srch_doc on ref_book_id_doc (search_doc_number asc)';
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
	v_task_name varchar2(128):='indexes block #20 - idx_ref_book_person_srch_fio';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME='IDX_REF_BOOK_PERSON_SRCH_FIO';
	IF v_run_condition=1 THEN
	        execute immediate 'create index idx_ref_book_person_srch_fio  on ref_book_person (search_last_name asc, search_first_name asc, search_middle_name asc, birth_date asc)';
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
	v_task_name varchar2(128):='indexes block #21 - idx_ref_book_person_srch_inn';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME='IDX_REF_BOOK_PERSON_SRCH_INN';
	IF v_run_condition=1 THEN
	        execute immediate 'create index idx_ref_book_person_srch_inn on ref_book_person (search_inn asc)';
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
	v_task_name varchar2(128):='indexes block #22 - idx_ref_book_person_srch_innf';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME='IDX_REF_BOOK_PERSON_SRCH_INNF';
	IF v_run_condition=1 THEN
	        execute immediate 'create index idx_ref_book_person_srch_innf on ref_book_person (search_inn_foreign asc)';
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
	v_task_name varchar2(128):='indexes block #23 - idx_ref_book_person_srch_snil1';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME='IDX_REF_BOOK_PERSON_SRCH_SNIL1';
	IF v_run_condition=1 THEN
	        execute immediate 'create index IDX_REF_BOOK_PERSON_SRCH_SNIL1 on ref_book_person (search_snils asc, start_date asc, end_date asc, id asc, record_id asc)';
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

commit;


DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='indexes block #24  - SRCH_REFB_TAX_PAYER_INP_ASNU';  
BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME='SRCH_REFB_TAX_PAYER_INP_ASNU';
	IF v_run_condition=1 THEN
		execute immediate 'DROP INDEX SRCH_REFB_TAX_PAYER_INP_ASNU';
	end if;
        execute immediate 'CREATE INDEX SRCH_REFB_TAX_PAYER_INP_ASNU ON REF_BOOK_ID_TAX_PAYER (INP, AS_NU )';
	dbms_output.put_line(v_task_name||'[INFO ]:'||' Success');

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
commit;

--создание новых индексов
DECLARE
	v_index_name varchar(30) :='IDX_REF_BOOK_PERSON_REPORT_DOC';
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #25 - create index '||v_index_name;  
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
	v_task_name varchar2(128):='alter_tables block #26 - create index '||v_index_name;  

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
	v_task_name varchar2(128):='alter_tables block #27 - create index '||v_index_name;  

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
	v_task_name varchar2(128):='alter_tables block #28 - create index '||v_index_name;  

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
	v_task_name varchar2(128):='alter_tables block #29 - create index '||v_index_name;  

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

DECLARE
	v_index_name varchar(30) :='IDX_LOG_BUSINESS_LOG_ID' ;
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #30 - create index '||v_index_name;  

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

DECLARE
	v_index_name varchar(30) :='IDX_REF_BOOK_CAL_DATETYPE' ;
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #31 - create index '||v_index_name;  

BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition=1 THEN
		execute immediate 'create index '|| v_index_name ||' on REF_BOOK_CALENDAR (cdate asc, ctype asc)
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
	v_index_name varchar(30) :='IDX_REF_BOOK_OKTMO_ID_CODE' ;
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #32 - create index '||v_index_name;  

BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition=1 THEN
		execute immediate 'create index '|| v_index_name ||' on REF_BOOK_OKTMO (id asc, code asc)
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
	v_index_name varchar(30) :='IDX_DD_PERSON_PERSON' ;
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #33 - create index '||v_index_name;  

BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition=1 THEN
		execute immediate 'create index '|| v_index_name ||' on DECLARATION_DATA_PERSON (person_id asc)
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
	v_index_name varchar(30) :='IDX_TMESS_BLOB_ID' ;
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #34 - create index '||v_index_name;  

BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition=1 THEN
		execute immediate 'create index '|| v_index_name ||' on TRANSPORT_MESSAGE (blob_id asc)
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
	v_index_name varchar(30) :='IDX_TMESS_DECLARATION_ID' ;
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #35 - create index '||v_index_name;  

BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition=1 THEN
		execute immediate 'create index '|| v_index_name ||' on TRANSPORT_MESSAGE (declaration_id asc)
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
	v_index_name varchar(30) :='IDX_NDFL_PERSON_INCOME_OPER_ID' ;
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #36 - create index '||v_index_name;  

BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition=1 THEN
		execute immediate 'create index '|| v_index_name ||' on NDFL_PERSON_INCOME (operation_id asc)
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
	v_index_name varchar(30) :='IDX_NDFL_PERSON_PP_OPER_ID' ;
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #37 - create index '||v_index_name;  

BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition=1 THEN
		execute immediate 'create index '|| v_index_name ||' on NDFL_PERSON_PREPAYMENT (operation_id asc)
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
	v_index_name varchar(30) :='IDX_NDFL_PERSON_DEDUCT_OPER_ID' ;
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #38 - create index '||v_index_name;  

BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME=v_index_name ;
	IF v_run_condition=1 THEN
		execute immediate 'create index '|| v_index_name ||' on NDFL_PERSON_DEDUCTION (operation_id asc)
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
	v_task_name varchar2(128):='indexes change block #1 - PK_REF_BOOK_PERSON'; 
	v_run_condition number(1); 
BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME='PK_REF_BOOK_PERSON';
	IF v_run_condition>0 THEN
	        execute immediate 'alter index PK_REF_BOOK_PERSON REBUILD REVERSE COMPUTE STATISTICS';
		dbms_output.put_line(v_task_name||'[INFO ]:'||' Success');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
DECLARE
	v_task_name varchar2(128):='indexes change block #2 - PK_REF_BOOK_ID_TAX_PAYER';  
	v_run_condition number(1);
BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME='PK_REF_BOOK_ID_TAX_PAYER';
	IF v_run_condition>0 THEN
	        execute immediate 'alter index PK_REF_BOOK_ID_TAX_PAYER REBUILD REVERSE COMPUTE STATISTICS';
		dbms_output.put_line(v_task_name||'[INFO ]:'||' Success');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

DECLARE
	v_task_name varchar2(128):='indexes change block #3 - PK_REF_BOOK_ID_DOC';  
	v_run_condition number(1);
BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME='PK_REF_BOOK_ID_DOC';
	IF v_run_condition>0 THEN
	        execute immediate 'alter index PK_REF_BOOK_ID_DOC REBUILD REVERSE COMPUTE STATISTICS';
		dbms_output.put_line(v_task_name||'[INFO ]:'||' Success');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
DECLARE
	v_task_name varchar2(128):='indexes change block #4 - DECLARATION_DATA_PK';  
	v_run_condition number(1);
BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME='DECLARATION_DATA_PK';
	IF v_run_condition>0 THEN
	        execute immediate 'alter index DECLARATION_DATA_PK REBUILD REVERSE COMPUTE STATISTICS';
		dbms_output.put_line(v_task_name||'[INFO ]:'||' Success');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

DECLARE
	v_task_name varchar2(128):='indexes change block #5 - NDFL_PP_PK';  
	v_run_condition number(1);
BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME='NDFL_PP_PK';
	IF v_run_condition>0 THEN
	        execute immediate 'alter index NDFL_PP_PK REBUILD REVERSE COMPUTE STATISTICS';
		dbms_output.put_line(v_task_name||'[INFO ]:'||' Success');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
DECLARE
	v_task_name varchar2(128):='indexes change block #6 - NDFL_PERSON_PK';  
	v_run_condition number(1);
BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME='NDFL_PERSON_PK';
	IF v_run_condition>0 THEN
	        execute immediate 'alter index NDFL_PERSON_PK REBUILD REVERSE COMPUTE STATISTICS';
		dbms_output.put_line(v_task_name||'[INFO ]:'||' Success');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

DECLARE
	v_task_name varchar2(128):='indexes change block #7 - NDFL_PERSON_I_PK';  
	v_run_condition number(1);
BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME='NDFL_PERSON_I_PK';
	IF v_run_condition>0 THEN
	        execute immediate 'alter index NDFL_PERSON_I_PK REBUILD REVERSE COMPUTE STATISTICS';
		dbms_output.put_line(v_task_name||'[INFO ]:'||' Success');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

DECLARE
	v_task_name varchar2(128):='indexes change block #8 - NDFL_PD_PK';  
	v_run_condition number(1);
BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME='NDFL_PD_PK';
	IF v_run_condition>0 THEN
	        execute immediate 'alter index NDFL_PD_PK REBUILD REVERSE COMPUTE STATISTICS';
		dbms_output.put_line(v_task_name||'[INFO ]:'||' Success');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

DECLARE
	v_task_name varchar2(128):='indexes change block #9 - LOG_BUSINESS_PK';  
	v_run_condition number(1);
BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME='LOG_BUSINESS_PK';
	IF v_run_condition>0 THEN
	        execute immediate 'alter index LOG_BUSINESS_PK REBUILD REVERSE COMPUTE STATISTICS';
		dbms_output.put_line(v_task_name||'[INFO ]:'||' Success');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

DECLARE
	v_task_name varchar2(128):='indexes change block #10 - TRANSPORT_MESSAGE_PK';  
	v_run_condition number(1);
BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME='TRANSPORT_MESSAGE_PK';
	IF v_run_condition>0 THEN
	        execute immediate 'alter index TRANSPORT_MESSAGE_PK REBUILD REVERSE COMPUTE STATISTICS';
		dbms_output.put_line(v_task_name||'[INFO ]:'||' Success');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

DECLARE
	v_task_name varchar2(128):='indexes change block #11 - PK_NDFL_REFERENCES';  
	v_run_condition number(1);
BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME='PK_NDFL_REFERENCES';
	IF v_run_condition>0 THEN
	        execute immediate 'alter index PK_NDFL_REFERENCES REBUILD REVERSE COMPUTE STATISTICS';
		dbms_output.put_line(v_task_name||'[INFO ]:'||' Success');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

DECLARE
	v_task_name varchar2(128):='indexes change block #12 - NDFL_PERS_INC_KPP_OKTMO';  
	v_run_condition number(1);
BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME='NDFL_PERS_INC_KPP_OKTMO';
	IF v_run_condition>0 THEN
	        execute immediate 'alter index NDFL_PERS_INC_KPP_OKTMO REBUILD PCTFREE 50 INITRANS 50 COMPUTE STATISTICS';
		dbms_output.put_line(v_task_name||'[INFO ]:'||' Success');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

DECLARE
	v_task_name varchar2(128):='indexes change block #13 - FK_LOG_ENTRY_LOG';  
	v_run_condition number(1);
BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME='FK_LOG_ENTRY_LOG';
	IF v_run_condition>0 THEN
	        execute immediate 'alter index FK_LOG_ENTRY_LOG REBUILD PCTFREE 50 INITRANS 10 COMPUTE STATISTICS';
		dbms_output.put_line(v_task_name||'[INFO ]:'||' Success');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

DECLARE
	v_task_name varchar2(128):='indexes change block #14 - IDX_LOG_ENTRY_DATE';  
	v_run_condition number(1);
BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME='IDX_LOG_ENTRY_DATE';
	IF v_run_condition>0 THEN
	        execute immediate 'alter index IDX_LOG_ENTRY_DATE REBUILD PCTFREE 50 INITRANS 10 COMPUTE STATISTICS';
		dbms_output.put_line(v_task_name||'[INFO ]:'||' Success');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

DECLARE
	v_task_name varchar2(128):='indexes change block #15 - PK_LOG_ENTRY';  
	v_run_condition number(1);
BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME='PK_LOG_ENTRY';
	IF v_run_condition>0 THEN
	        execute immediate 'alter index PK_LOG_ENTRY REBUILD PCTFREE 50 INITRANS 10	 COMPUTE STATISTICS';
		dbms_output.put_line(v_task_name||'[INFO ]:'||' Success');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

DECLARE
	v_task_name varchar2(128):='indexes change block #16 - IDX_NDFL_PERSON_INC_PAYMDT';  
	v_run_condition number(1);
BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME='IDX_NDFL_PERSON_INC_PAYMDT';
	IF v_run_condition>0 THEN
	        execute immediate 'alter index IDX_NDFL_PERSON_INC_PAYMDT REBUILD INITRANS 50 COMPUTE STATISTICS';
		dbms_output.put_line(v_task_name||'[INFO ]:'||' Success');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

DECLARE
	v_task_name varchar2(128):='indexes change block #17 - IDX_NDFL_PERSON_INC_TAXDT';  
	v_run_condition number(1);
BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME='IDX_NDFL_PERSON_INC_TAXDT';
	IF v_run_condition>0 THEN
	        execute immediate 'alter index IDX_NDFL_PERSON_INC_TAXDT REBUILD PCTFREE 50 INITRANS 50 COMPUTE STATISTICS';
		dbms_output.put_line(v_task_name||'[INFO ]:'||' Success');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

DECLARE
	v_task_name varchar2(128):='indexes change block #18 - IDX_NDFL_PERSON_DECL_DATA_ID';  
	v_run_condition number(1);
BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME='IDX_NDFL_PERSON_DECL_DATA_ID';
	IF v_run_condition>0 THEN
	        execute immediate 'alter index IDX_NDFL_PERSON_DECL_DATA_ID REBUILD PCTFREE 50 INITRANS 50	 COMPUTE STATISTICS';
		dbms_output.put_line(v_task_name||'[INFO ]:'||' Success');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/


BEGIN
	FOR c1 IN (
		select ui.index_name from user_indexes ui where table_name in ('REF_BOOK_PERSON','REF_BOOK_ID_DOC', 'REF_BOOK_ID_TAX_PAYER') 
          )
    LOOP
		execute immediate 'alter index ' || c1.index_name || ' rebuild compute statistics';
		dbms_output.put_line('Index: ' || c1.index_name || ' rebuild');
    END LOOP;
EXCEPTION
	when OTHERS then
		dbms_output.put_line('Alter index [FATAL]:'||sqlerrm);	
END;
/
