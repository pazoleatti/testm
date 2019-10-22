create or replace TRIGGER REF_BOOK_ID_DOC_BEFORE_INS_UPD
  before insert or update on ref_book_id_doc
  for each row
begin
        :new.search_doc_number := regexp_replace(:new.doc_number,'[^0-9A-Za-zР-пр-џ]','');        
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

        :new.search_doc_number := regexp_replace(:new.id_doc_number,'[^0-9A-Za-zР-пр-џ]','');        
    
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
