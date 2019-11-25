--3.10-adudenko-03
declare 
  v_task_name varchar2(128):='insert_update_delete block #1 - merge into declaration_type';  
begin

	merge into declaration_type dst using
	(select 106 as id, 'Приложение 2' as name from dual 
	) src
	on (src.id=dst.id)
	when not matched then
		insert (id,  name)
		values (src.id,  src.name)
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


declare 
  v_task_name varchar2(128):='insert_update_delete block #2 - merge into declaration_type';  
begin

	merge into declaration_template dst using
	(select 106 as id, to_date ('01.01.2016','dd.mm.yyyy') as version, 'Приложение 2' as name,
	106 as declaration_type_id, 7 as form_kind, 7 as form_type from dual 
	) src
	on (src.id=dst.id)
	when not matched then
		insert (id,  version, name, declaration_type_id, form_kind, form_type)
		values (src.id, src.version, src.name, src.declaration_type_id, src.form_kind, src.form_type)
	when matched then
		update set dst.name=src.name, dst.version=src.version, dst.declaration_type_id = src.declaration_type_id, 
			   dst.form_kind = src.form_kind, dst.form_type = src.form_type;
	
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
