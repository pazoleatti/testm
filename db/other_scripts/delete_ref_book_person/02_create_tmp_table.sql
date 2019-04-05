set heading off;
set serveroutput on;

spool &1

declare 
	v_count number;
begin
	select count(1) into v_count from user_tables where table_name='TMP_DELETE_RB_PERSON';
	if v_count = 0 then 
	   execute immediate 'CREATE TABLE TMP_DELETE_RB_PERSON (ID NUMBER NOT NULL)';
		dbms_output.put_line('Table TMP_DELETE_RB_PERSON created.');	
	else
		dbms_output.put_line('Table TMP_DELETE_RB_PERSON already exists.');
		execute immediate 'delete from TMP_DELETE_RB_PERSON';
		commit;
		dbms_output.put_line('Table TMP_DELETE_RB_PERSON cleared.');
	end if;	
end;
/

variable v_limit number;

exec :v_limit := &2;


insert into TMP_DELETE_RB_PERSON 
select id from REF_BOOK_PERSON r where 
not exists(select * from NDFL_REFERENCES where person_id=r.id)
and
not exists(select * from NDFL_PERSON where person_id=r.id)
and 
not exists(select * from DECLARATION_DATA_PERSON where person_id=r.id)
and not exists
(select 1 from 
(
select id from REF_BOOK_ID_DOC 
where person_id in
(select a.id from REF_BOOK_PERSON a, NDFL_REFERENCES b where b.person_id=a.id)
union
select id from REF_BOOK_ID_DOC 
where person_id in
(select a.id from REF_BOOK_PERSON a, NDFL_PERSON b where b.person_id=a.id)
union
select id from REF_BOOK_ID_DOC 
where person_id in
(select a.id from REF_BOOK_PERSON a, DECLARATION_DATA_PERSON b where b.person_id=a.id)
) where id = r.report_doc
) 
and rownum <=:v_limit;

spool off

exit;