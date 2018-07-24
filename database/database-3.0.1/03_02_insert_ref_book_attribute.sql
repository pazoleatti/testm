-- https://jira.aplana.com/browse/SBRFNDFL-5184 Реализовать возможность хранения для ФЛ списка тербанков и заполнять этот список ТБ при первичной загрузке ФЛ
declare 
  v_task_name varchar2(128):='insert_ref_book_attribute block #1 - merge into ref_book_attribute (SBRFNDFL-5184)';  
begin
	merge into ref_book_attribute a using
	(select 9071 as id, 907 as ref_book_id, 'Значение GUID' as name, 'GUID' as alias, 1 as type, 1 as ord,null as reference_id, null as attribute_id, 1 as visible, null as precision, 15 as width, 1 as required, 1 as is_unique, null as sort_order, null as format, 0 as read_only, 500 as max_length from dual
	union all
	select 9072 as id, 907 as ref_book_id, 'Тербанк' as name, 'TB_DEPARTMENT_ID' as alias, 4 as type, 2 as ord,30 as reference_id, 161 as attribute_id, 1 as visible, null as precision, 15 as width, 1 as required, 0 as is_unique, null as sort_order, null as format, 0 as read_only, null as max_length from dual
	union all
	select 9081 as id, 908 as ref_book_id, 'Идентификатор версии ФЛ' as name, 'PERSON_ID' as alias, 4 as type, 1 as ord,904 as reference_id, 9041 as attribute_id, 1 as visible, null as precision, 15 as width, 1 as required, 0 as is_unique, null as sort_order, null as format, 0 as read_only, null as max_length from dual
	union all
	select 9082 as id, 908 as ref_book_id, 'Тербанк' as name, 'TB_DEPARTMENT_ID' as alias, 4 as type, 2 as ord,30 as reference_id, 161 as attribute_id, 1 as visible, null as precision, 15 as width, 1 as required, 0 as is_unique, null as sort_order, null as format, 0 as read_only, null as max_length from dual
	union all
	select 9083 as id, 908 as ref_book_id, 'Дата' as name, 'IMPORT_DATE' as alias, 3 as type, 3 as ord,null as reference_id, null as attribute_id, 1 as visible, null as precision, 15 as width, 0 as required, 0 as is_unique, null as sort_order, 1 as format, 0 as read_only, null as max_length from dual
	) b
	on (a.id=b.id)
	when not matched then
		insert (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length)
		values (b.id, b.ref_book_id, b.name, b.alias, b.type, b.ord, b.reference_id, b.attribute_id, b.visible, b.precision, b.width, b.required, b.is_unique, b.sort_order, b.format, b.read_only, b.max_length);

	CASE SQL%ROWCOUNT 
	WHEN 5 THEN dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	ELSE dbms_output.put_line(v_task_name||'[ERROR]:'||' changes had already been partly implemented');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
