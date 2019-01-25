--https://jira.aplana.com/browse/SBRFNDFL-6298 - Обязательность полей таблиц связанных с ФЛ
declare
	v_task_name varchar2(128):='update null ref_book_person.old_id';  
begin	
	
	update ref_book_person  
	set old_id = record_id
	where old_id is null;

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

