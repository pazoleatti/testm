set serveroutput on size 1000000;
set linesize 128;

--https://jira.aplana.com/browse/SBRFACCTAX-17229: 1.2 Поправить данные в справочнике "Виды обязательств"
declare l_task_name varchar2(128) := 'DML Block #1 (SBRFACCTAX-17229))';
begin
	update (select r.id, v.string_value
			 from   ref_book_value v
			 join   ref_book_record r on r.id = v.record_id
			 join   ref_book_attribute a on a.id = v.attribute_id
			 where  r.ref_book_id = 609 and
					(
					-- имеются пробелы в начале
					regexp_like(v.string_value, '^\s{1,}.*$')
					-- имеются пробелы в конце
					or regexp_like(v.string_value, '^.*\s{1,}$')
					-- имеются двойные и более пробелы
					or regexp_like(v.string_value, '\s{2,}')
					-- имеются кавычки отличные от " и '
					or regexp_like(v.string_value, '[«»„“”`‘’]'))) t
	set    t.string_value = TRIM(regexp_replace(regexp_replace(regexp_replace(t.string_value, '\s{2,}', ' '), '[«»„“”]', '"'), '[`‘’]', ''''));
	dbms_output.put_line(l_task_name||'[INFO]: '||sql%rowcount||' row(s) updated in ref_book_value');		
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(l_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
commit;	
-----------------------------------------------------------------------------------------------------------------------------

COMMIT;
EXIT;