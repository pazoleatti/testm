--3.10.1-adudenko-02
declare 
  v_task_name varchar2(128):='insert_update_delete block #1 - merge into ref_book_form_type';  
begin

	merge into ref_book_form_type dst using
	(select 3 as id, '2-НДФЛ (1)' as code, '2-НДФЛ с признаком 1' as name from dual union
	 select 4, '2-НДФЛ (2)', '2-НДФЛ с признаком 2' from dual union
	 select 5, '6-НДФЛ', '6-НДФЛ' from dual union
	 select 6, '2-НДФЛ (ФЛ)', '2-НДФЛ для выдачи ФЛ' from dual 
	) src
	on (src.id=dst.id)
	when not matched then
		insert (id, code, name)
		values (src.id, src.code, src.name)
	when matched then
		update set dst.name=src.name, dst.code=src.code;
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 

EXCEPTION
  when OTHERS then 
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

--отключение триггеров, если они есть
declare 
	v_cnt number(10);
begin
	select count(*)  into v_cnt  from user_triggers where trigger_name='NDFL_PERSON_BEFORE_INS_UPD' and status='ENABLED';
	if (v_cnt)>0 then
	   execute immediate 'alter trigger NDFL_PERSON_BEFORE_INS_UPD disable';
	   dbms_output.put_line('Trigger NDFL_PERSON_BEFORE_INS_UPD disabled');
	end if;	
EXCEPTION
  when OTHERS then 
    dbms_output.put_line('Triggers error[FATAL]:'||sqlerrm);		
end;
/

declare 
	v_cnt number(10);
begin
	select count(*)  into v_cnt  from user_triggers where trigger_name='REF_BOOK_ID_DOC_BEFORE_INS_UPD' and status='ENABLED';
	if (v_cnt)>0 then
	   execute immediate 'alter trigger REF_BOOK_ID_DOC_BEFORE_INS_UPD disable';
	   dbms_output.put_line('Trigger REF_BOOK_ID_DOC_BEFORE_INS_UPD disabled');
	end if;	
EXCEPTION
  when OTHERS then 
    dbms_output.put_line('Triggers error[FATAL]:'||sqlerrm);		
end;
/

declare 
	v_cnt number(10);
begin
	select count(*)  into v_cnt  from user_triggers where trigger_name='REF_BOOK_PERSON_BEFORE_INS_UPD' and status='ENABLED';
	if (v_cnt)>0 then
	   execute immediate 'alter trigger REF_BOOK_PERSON_BEFORE_INS_UPD disable';
	   dbms_output.put_line('Trigger REF_BOOK_PERSON_BEFORE_INS_UPD disabled');
	end if;	
EXCEPTION
  when OTHERS then 
    dbms_output.put_line('Triggers error[FATAL]:'||sqlerrm);		
end;
/

--обновление поисковых полей в реестре
declare 
  v_task_name varchar2(128):='insert_update_delete block #2 - update ref_book_id_doc';  
begin
	update ref_book_id_doc set 
		doc_number = upper (doc_number),
		search_doc_number=regexp_replace(upper(doc_number),'[^0-9A-Za-zА-Яа-я]','')
		where upper(doc_number)<>doc_number;	
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');

EXCEPTION
  when OTHERS then 
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

declare 
  v_task_name varchar2(128):='insert_update_delete block #3 - update ref_book_person';  
begin
	update ref_book_person set 
		LAST_NAME = upper(LAST_NAME),
		FIRST_NAME = upper(FIRST_NAME),
		MIDDLE_NAME = upper(MIDDLE_NAME),
		INN = upper(INN),
		INN_FOREIGN = upper(INN_FOREIGN),
		SNILS = upper(SNILS),
		search_LAST_NAME = replace(nvl(upper(last_name),'empty'),' ',''),
		search_FIRST_NAME = replace(nvl(upper(FIRST_name),'empty'),' ',''),
		search_MIDDLE_NAME = replace(nvl(upper(MIDDLE_name),'empty'),' ',''),
                search_INN = replace(nvl(upper(inn),'empty'),' ',''),
		search_INN_FOREIGN = replace(nvl(upper(inn_foreign),'empty'),' ',''),
		search_SNILS = replace(replace(nvl(upper(snils),'empty'),' ',''),'-',''); 

	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');

EXCEPTION
  when OTHERS then 
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

declare 
	  v_task_name varchar2(128):='insert_update_delete block #4 - update NDFL_PERSON';  
begin
	update ndfl_person set 
		id_doc_number = upper(id_doc_number),
		LAST_NAME = upper(LAST_NAME),
		FIRST_NAME = upper(FIRST_NAME),
		MIDDLE_NAME = upper(MIDDLE_NAME),
		INN_NP = upper(INN_NP),
		INN_FOREIGN = upper(INN_FOREIGN),
		SNILS = upper(SNILS),
		search_doc_number=regexp_replace(upper(id_doc_number),'[^0-9A-Za-zА-Яа-я]',''),
		search_LAST_NAME = replace(nvl(upper(last_name),'empty'),' ',''),
		search_FIRST_NAME = replace(nvl(upper(FIRST_name),'empty'),' ',''),
		search_MIDDLE_NAME = replace(nvl(upper(MIDDLE_name),'empty'),' ',''),
                search_INN = replace(nvl(upper(inn_np),'empty'),' ',''),
		search_INN_FOREIGN = replace(nvl(upper(inn_foreign),'empty'),' ',''),
		search_SNILS = replace(replace(nvl(upper(snils),'empty'),' ',''),'-',''); 
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');

EXCEPTION
  when OTHERS then 
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

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
