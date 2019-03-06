set serveroutput on;
spool &1;

begin
	update ref_book_person d
	set record_id = old_id
	where record_id != old_id and not exists (select 1 from ref_book_person p where p.record_id = d.record_id and p.record_id = p.old_id);

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line('Update hanging duplicates [WARNING]: No changes was done');
	ELSE dbms_output.put_line('Updated '||to_char(SQL%ROWCOUNT)||' rows');
	END CASE; 
  
end;
/
exit;