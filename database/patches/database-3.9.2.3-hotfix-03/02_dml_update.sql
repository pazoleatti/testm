
declare 
  v_task_name varchar2(128):='insert_update_delete block #1 - update NDFL_PERSON';  
begin
	update ndfl_person set search_doc_number=regexp_replace(doc_number,'[^0-9A-Za-zА-Яа-я]',''),
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

