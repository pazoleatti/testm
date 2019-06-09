-- https://jira.aplana.com/browse/SBRFNDFL-5184 Реализовать возможность хранения для ФЛ списка тербанков и заполнять этот список ТБ при первичной загрузке ФЛ
declare 
  v_task_name varchar2(128):='insert_ref_book block #1 - merge into ref_book (SBRFNDFL-5184)';  
begin
	merge into ref_book a using
	(select 907 as id, 'Тербанки для ФЛ при первичной загрузке' as name, '10d898c7-8db0-4df2-a4f2-3df135deab40' as script_id, 1 as visible, 0 as type, 0 as read_only,'REF_BOOK_TB_PERSON' as table_name,0 as is_versioned from dual
	union all
	select 908 as id, 'Список тербанков назначенных ФЛ' as name, '9538d273-51dd-4331-9c1c-510468b5ebee' as script_id, 1 as visible, 0 as type, 0 as read_only,'REF_BOOK_PERSON_TB' as table_name,0 as is_versioned from dual
	) b
	on (a.id=b.id)
	when not matched then
		insert (id, name, script_id, visible, type, read_only, table_name, is_versioned)
		values (b.id, b.name, b.script_id, b.visible, b.type, b.read_only, b.table_name, b.is_versioned);

	CASE SQL%ROWCOUNT 
	WHEN 2 THEN dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	ELSE dbms_output.put_line(v_task_name||'[ERROR]:'||' changes had already been partly implemented');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
