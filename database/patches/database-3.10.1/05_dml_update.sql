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
