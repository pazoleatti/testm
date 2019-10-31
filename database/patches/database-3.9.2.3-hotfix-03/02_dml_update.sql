
alter trigger REF_BOOK_ID_DOC_BEFORE_INS_UPD disable;
/
alter trigger REF_BOOK_PERSON_BEFORE_INS_UPD disable;
/

declare 
	v_cnt number(10);
begin
	select count(*)  into v_cnt  from user_triggers where trigger_name='NDFL_PERSON_BEFORE_INS_UPD' and status='ENABLED';
	if (v_cnt)>0 then
	   execute immediate 'alter trigger NDFL_PERSON_BEFORE_INS_UPD disable';
	   dbms_output.put_line('Triggers disabled');
	end if;	
EXCEPTION
  when OTHERS then 
    dbms_output.put_line('Triggers error[FATAL]:'||sqlerrm);		
end;
/

declare 
	  v_task_name varchar2(128):='insert_update_delete block #1 - update NDFL_PERSON';  
begin
	update ndfl_person set search_doc_number=regexp_replace(id_doc_number,'[^0-9A-Za-zА-Яа-я]',''),
		search_LAST_NAME = replace(nvl(last_name,'empty'),' ',''),
		search_FIRST_NAME = replace(nvl(FIRST_name,'empty'),' ',''),
		search_MIDDLE_NAME = replace(nvl(MIDDLE_name,'empty'),' ',''),
                search_INN = replace(nvl(inn_np,'empty'),' ',''),
		search_INN_FOREIGN = replace(nvl(inn_foreign,'empty'),' ',''),
		search_SNILS = replace(replace(nvl(snils,'empty'),' ',''),'-',''); 

	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');

EXCEPTION
  when OTHERS then 
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;


declare 
  v_task_name varchar2(128):='insert_update_delete block #2 - update ref_book_id_doc';  
begin
	update ref_book_id_doc set search_doc_number=regexp_replace(doc_number,'[^0-9A-Za-zА-Яа-я]','');	
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
	update ref_book_person set search_LAST_NAME = replace(nvl(last_name,'empty'),' ',''),
		search_FIRST_NAME = replace(nvl(FIRST_name,'empty'),' ',''),
		search_MIDDLE_NAME = replace(nvl(MIDDLE_name,'empty'),' ',''),
                search_INN = replace(nvl(inn,'empty'),' ',''),
		search_INN_FOREIGN = replace(nvl(inn_foreign,'empty'),' ',''),
		search_SNILS = replace(replace(nvl(snils,'empty'),' ',''),'-',''); 

	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');

EXCEPTION
  when OTHERS then 
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

alter trigger REF_BOOK_ID_DOC_BEFORE_INS_UPD enable;
/
alter trigger REF_BOOK_PERSON_BEFORE_INS_UPD enable;
/



