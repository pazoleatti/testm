--https://jira.aplana.com/browse/SBRFNDFL-6233 Поменять размерность поля "phone" в настройках подразделений
declare
	v_task_name varchar2(128):='update ref_book_ndfl_detail.phone';  
begin	
	
	update ref_book_ndfl_detail  
	set phone = substr(phone, 1, 20)
	where phone is not null and length(phone)>20;

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

