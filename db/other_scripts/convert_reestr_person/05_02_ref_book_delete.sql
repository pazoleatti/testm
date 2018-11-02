declare
	v_task_name varchar2(128):='delete block #1 - delete from ref_book_id_doc status <> 0';  
begin		
	--https://jira.aplana.com/browse/SBRFNDFL-5837 Конвертировать данные реестра ФЛ
	delete from ref_book_id_doc where status <> 0;

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

declare
	v_task_name varchar2(128):='delete block #2 - delete from ref_book_id_tax_payer status <> 0';  
begin		
	--https://jira.aplana.com/browse/SBRFNDFL-5837 Конвертировать данные реестра ФЛ
	delete from ref_book_id_tax_payer where status <> 0;

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

declare
	v_task_name varchar2(128):='delete block #3 - delete from ref_book_person_tb status <> 0';  
begin		
	--https://jira.aplana.com/browse/SBRFNDFL-5837 Конвертировать данные реестра ФЛ
	delete from ref_book_person_tb where status <> 0;

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

declare
	v_task_name varchar2(128):='delete block #4 - delete from ref_book_person status <> 0';  
begin		
	--https://jira.aplana.com/browse/SBRFNDFL-5837 Конвертировать данные реестра ФЛ
	delete from ref_book_person where status in (-1, 1);

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

declare
	v_task_name varchar2(128):='delete block #5 - delete from ref_book_id_doc person_id is null';  
begin		
	--https://jira.aplana.com/browse/SBRFNDFL-5837 Конвертировать данные реестра ФЛ
	delete from ref_book_id_doc where person_id is null;

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

declare
	v_task_name varchar2(128):='delete block #6 - delete from ref_book_id_tax_payer person_id is null';  
begin		
	--https://jira.aplana.com/browse/SBRFNDFL-5837 Конвертировать данные реестра ФЛ
	delete from ref_book_id_tax_payer where person_id is null;

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

declare
	v_task_name varchar2(128):='delete block #7 - delete from ref_book_person_tb person_id is null';  
begin		
	--https://jira.aplana.com/browse/SBRFNDFL-5837 Конвертировать данные реестра ФЛ
	delete from ref_book_person_tb where person_id is null;

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
