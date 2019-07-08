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
  v_task_name varchar2(128):='insert_update_delete block #5 - merge into declaration_template';  
begin
	merge into declaration_template dst using
	(select 105 as id, to_date ('01.01.2016','dd.mm.yyyy') as version, '2 НДФЛ (ФЛ)' as name, 
105 as declaration_type_id, 8 as form_kind, 6 as form_type  from dual 
	) src
	on (src.id=dst.id)
	when not matched then
		insert (id, version, name,declaration_type_id, form_kind, form_type)
		values (src.id, src.version, src.name, src.declaration_type_id, src.form_kind, src.form_type);
	
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
  v_task_name varchar2(128):='insert_update_delete block #6 - merge into state';  
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
