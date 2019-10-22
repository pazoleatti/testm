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
	v_task_name varchar2(128):='indexes block #1 - IDX_NDFL_PERSON_SEARCH_DOC';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME='IDX_NDFL_PERSON_SEARCH_DOC';
	IF v_run_condition=1 THEN
	        execute immediate '		create index idx_ndfl_person_search_doc on ndfl_person 
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
	v_task_name varchar2(128):='indexes block #2 - IDX_NDFL_PERSON_SEARCH_FIO';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME='IDX_NDFL_PERSON_SEARCH_FIO';
	IF v_run_condition=1 THEN
	        execute immediate '		create index idx_ndfl_person_search_fio on ndfl_person 
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
	v_task_name varchar2(128):='indexes block #3 - IDX_NDFL_PERSON_SEARCH_INN';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME='IDX_NDFL_PERSON_SEARCH_INN';
	IF v_run_condition=1 THEN
	        execute immediate '		create index idx_ndfl_person_search_inn on ndfl_person 
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
	v_task_name varchar2(128):='indexes block #4 - IDX_NDFL_PERSON_SEARCH_INNF';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME='IDX_NDFL_PERSON_SEARCH_INNF';
	IF v_run_condition=1 THEN
	        execute immediate '		create index idx_ndfl_person_search_innf on ndfl_person 
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
	v_task_name varchar2(128):='indexes block #5 - IDX_NDFL_PERSON_SEARCH_SNILS';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where INDEX_NAME='IDX_NDFL_PERSON_SEARCH_SNILS';
	IF v_run_condition=1 THEN
	        execute immediate '		create index idx_ndfl_person_search_snils on ndfl_person 
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
	v_task_name varchar2(128):='indexes block #6 - idx_ref_book_id_doc_srch_doc';  
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
	v_task_name varchar2(128):='indexes block #7 - idx_ref_book_person_srch_fio';  
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
	v_task_name varchar2(128):='indexes block #8 - idx_ref_book_person_srch_inn';  
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
	v_task_name varchar2(128):='indexes block #9 - idx_ref_book_person_srch_innf';  
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
	v_task_name varchar2(128):='indexes block #10 - idx_ref_book_person_srch_snil1';  
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
