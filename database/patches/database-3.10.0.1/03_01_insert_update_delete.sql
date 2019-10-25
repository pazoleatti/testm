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
  v_task_name varchar2(128):='insert_update_delete block #1 - update ref_book_id_doc';  
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
  v_task_name varchar2(128):='insert_update_delete block #2 - update ref_book_person';  
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

declare 
	  v_task_name varchar2(128):='insert_update_delete block #3 - update NDFL_PERSON';  
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
  v_task_name varchar2(128):='insert_update_delete block #4 - add new task';  
begin
	merge into async_task_type dst using
	(select 46 as id, 'Удаление строк налоговой формы' as name, 
		'DeleteSelectedDeclarationRowsAsyncTask' as handler_bean,
		'3000' as SHORT_QUEUE_LIMIT, '1000000' as task_limit, 
		'количество строк для удаления' as limit_kind from dual
	) src
	on (src.id=dst.id)
	when not matched then
		insert (id, name, handler_bean, SHORT_QUEUE_LIMIT, TASK_LIMIT, LIMIT_KIND)
		values (src.id, src.name, src.handler_bean, src.SHORT_QUEUE_LIMIT, src.TASK_LIMIT, src.LIMIT_KIND);
	
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
