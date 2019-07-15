-- 3.8-dnovikov-1 

declare 
  v_task_name varchar2(128):='insert_update_delete block #1 - merge into ref_book_doc_state';  
begin
	merge into ref_book_doc_state dst using
	(select 8 as id, 'Отправка в ЭДО' as name from dual 
	) src
	on (src.id=dst.id)
	when not matched then
		insert (id, name)
		values (src.id, src.name)
	when matched then
		update set dst.name=src.name;
	
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

-- 3.8-dnovikov-3 
declare 
  v_task_name varchar2(128):='insert_update_delete block #2 - merge into ref_book_form_type';  
begin
	merge into ref_book_form_type dst using
	(select 6 as id, '2 НДФЛ (ФЛ)' as code, '2-НДФЛ для выдачи ФЛ' as name from dual 
	) src
	on (src.id=dst.id)
	when not matched then
		insert (id, code, name)
		values (src.id, src.code, src.name);
	
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

declare 
  v_task_name varchar2(128):='insert_update_delete block #3 - merge into declaration_kind';  
begin
	merge into declaration_kind dst using
	(select 8 as id, 'Отчетная ФЛ' as name from dual 
	) src
	on (src.id=dst.id)
	when not matched then
		insert (id, name)
		values (src.id, src.name);
	
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

declare 
  v_task_name varchar2(128):='insert_update_delete block #4 - merge into declaration_type';  
begin
	merge into declaration_type dst using
	(select 105 as id, '2 НДФЛ (ФЛ)' as name from dual 
	) src
	on (src.id=dst.id)
	when not matched then
		insert (id, name)
		values (src.id, src.name);
	
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

declare 
  v_task_name varchar2(128):='insert_update_delete block #5 - merge into state';  
begin
	merge into state dst using
	(select 4 as id, 'Выдан' as name from dual 
	) src
	on (src.id=dst.id)
	when not matched then
		insert (id, name)
		values (src.id, src.name);
	
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

--3.8-dnovikov-5
declare 
  v_task_name varchar2(128):='insert_update_delete block #6 - merge into async_task_type';  
begin
	merge into async_task_type dst using
	(select 44 as id, 'Формирование ОНФ 2-НДФЛ(ФЛ)' as name, 'Create2NdflFLAsyncTask' as handler_bean from dual 
	) src
	on (src.id=dst.id)
	when not matched then
		insert (id, name, handler_bean)
		values (src.id, src.name, src.handler_bean);
	
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
--3.8-ytrofimov-4
declare 
  v_task_name varchar2(128):='insert_update_delete block #7 - merge into declaration_template_file';  
begin

	merge into declaration_template_file dst using
	(select 105 as declaration_template_id, '6466aad0-90c5-4101-8002-fb97b0e32d16' as blob_data_id from dual  union
         select 105 as declaration_template_id, '5f3eb319-56cb-4dd8-b06e-b97ea3df0cf1' as blob_data_id from dual  
	) src
	on (src.declaration_template_id=dst.declaration_template_id and src.blob_data_id=dst.blob_data_id)
	when not matched then
		insert (declaration_template_id, blob_data_id)
		values (src.declaration_template_id, src.blob_data_id);
	
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
/

