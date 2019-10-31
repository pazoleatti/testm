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

begin
    dbms_output.put_line ('Drop functional indexes...');
    for ind in (select index_name from user_indexes where index_name in ('SRCH_FULL_REF_PERS_DUBLE','SRCH_REF_BOOK_ID_DOC_TP_NUM','SRCH_REFB_TAX_PAYER_INP_ASNU',
                'SRCH_REF_PERSON_NAME_BRTHD','SRCH_REF_BOOK_PERSON_SNILS','SRCH_REF_BOOK_PERSON_INN_F','SRCH_REF_BOOK_PERSON_INN'))
    loop
        execute immediate 'DROP INDEX '|| ind.index_name;
    end loop;
end;
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

begin
	dbms_output.put_line('Create other indexes...');
end;
/
commit;

  CREATE INDEX SRCH_FULL_REF_PERS_DUBLE ON REF_BOOK_PERSON (REPLACE(LOWER(NVL(LAST_NAME,'empty')),' ',''), REPLACE(LOWER(NVL(FIRST_NAME,'empty')),' ',''), REPLACE(LOWER(NVL(MIDDLE_NAME,'empty')),' ',''), BIRTH_DATE, REPLACE(REPLACE(NVL(SNILS,'empty'),' ',''),'-',''), REPLACE(NVL(INN,'empty'),' ',''), REPLACE(NVL(INN_FOREIGN,'empty'),' ','')); 

  CREATE INDEX SRCH_REF_BOOK_PERSON_INN ON REF_BOOK_PERSON (REPLACE(INN,' ',''))  ;

  CREATE INDEX SRCH_REF_BOOK_PERSON_INN_F ON REF_BOOK_PERSON (REPLACE(INN_FOREIGN,' ','')) ;

  CREATE INDEX SRCH_REF_BOOK_PERSON_SNILS ON REF_BOOK_PERSON (REPLACE(REPLACE(SNILS,' ',''),'-','')) ;

  CREATE INDEX SRCH_REF_PERSON_NAME_BRTHD ON REF_BOOK_PERSON (REPLACE(LOWER(LAST_NAME),' ',''), REPLACE(LOWER(FIRST_NAME),' ',''), REPLACE(LOWER(MIDDLE_NAME),' ',''), BIRTH_DATE) ;

  CREATE INDEX SRCH_REF_BOOK_ID_DOC_TP_NUM ON REF_BOOK_ID_DOC (DOC_ID, REPLACE(LOWER(DOC_NUMBER),' ','')) ;

  CREATE INDEX SRCH_REFB_TAX_PAYER_INP_ASNU ON REF_BOOK_ID_TAX_PAYER (AS_NU, LOWER(INP)); 
commit;