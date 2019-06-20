-- 3.7-skononova-6 https://jira.aplana.com/browse/SBRFNDFL-7852 Добавить прочерк в поле Наименование для вновь добавленных статей дохода
declare 
  v_task_name varchar2(128):='insert_update_delete block #1 - merge into ref_book_income_kind';  
begin
	merge into ref_book_income_kind a using
	 (select (select id from (select id,version from ref_book_income_type where code='2520' and status=0 order by version desc) where rownum=1) as income_type_id, 
	        '00' as mark, '-' as name, to_date('01.01.2016','DD.MM.YYYY') as version from dual
	 union
	 select (select id from (select id,version from ref_book_income_type where code='2720' and status=0 order by version desc) where rownum=1) as income_type_id, 
	        '00' as mark, '-' as name, to_date('01.01.2016','DD.MM.YYYY') as version from dual
	 union
	 select (select id from (select id,version from ref_book_income_type where code='2740' and status=0 order by version desc) where rownum=1) as income_type_id, 
	        '00' as mark, '-' as name, to_date('01.01.2016','DD.MM.YYYY') as version from dual
	 union
	 select (select id from (select id,version from ref_book_income_type where code='2750' and status=0 order by version desc) where rownum=1) as income_type_id, 
	        '00' as mark, '-' as name, to_date('01.01.2016','DD.MM.YYYY') as version from dual
	 union
	 select (select id from (select id,version from ref_book_income_type where code='2790' and status=0 order by version desc) where rownum=1) as income_type_id, 
	        '00' as mark, '-' as name, to_date('01.01.2016','DD.MM.YYYY') as version from dual
	) b
	on (a.income_type_id=b.income_type_id and a.mark=b.mark and a.version=b.version)
	when not matched then
		insert (id,record_id,income_type_id,mark,name,version)
		values (seq_ref_book_record.nextval,seq_ref_book_record_row_id.nextval,b.income_type_id,b.mark,b.name,b.version)
	when matched then
		update set a.name = b.name where a.name is null;

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


