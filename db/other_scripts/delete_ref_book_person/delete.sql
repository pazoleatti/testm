set heading off;
set serveroutput on;
alter session set skip_unusable_indexes = true;

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
and rownum <=:v_limit;

select 'Start Delete' from dual;

declare 
	v_count number;
begin

	select count(1) into v_count from TMP_DELETE_RB_PERSON;
	IF v_count > 0 THEN
	
		FOR c1 IN (
					select ui.index_name from user_indexes ui where lower(ui.table_name)='ref_book_person' and lower(ui.uniqueness)='nonunique'
				  )
		LOOP
			execute immediate 'alter index ' || c1.index_name || ' unusable';
		END LOOP;
		
		execute immediate 'alter table REF_BOOK_PERSON disable constraint FK_REF_BOOK_PERSON_REPORT_DOC';

		DBMS_OUTPUT.PUT_LINE('Indexes set to unusable, constraint disabled');
		
		delete from REF_BOOK_ID_DOC where person_id in (select id from TMP_DELETE_RB_PERSON);
		
		DBMS_OUTPUT.PUT_LINE('Deleted ' || SQL%ROWCOUNT || ' rows from REF_BOOK_ID_DOC.');

		delete from REF_BOOK_PERSON_TB where person_id in (select id from TMP_DELETE_RB_PERSON);
		
		DBMS_OUTPUT.PUT_LINE('Deleted ' || SQL%ROWCOUNT || ' rows from REF_BOOK_PERSON_TB.');

		delete from LOG_BUSINESS where person_id in (select id from TMP_DELETE_RB_PERSON);
		
		DBMS_OUTPUT.PUT_LINE('Deleted ' || SQL%ROWCOUNT || ' rows from LOG_BUSINESS.');

		delete from REF_BOOK_ID_TAX_PAYER where person_id in (select id from TMP_DELETE_RB_PERSON);
		
		DBMS_OUTPUT.PUT_LINE('Deleted ' || SQL%ROWCOUNT || ' rows from REF_BOOK_ID_TAX_PAYER.');

		delete from REF_BOOK_PERSON where id in (select id from TMP_DELETE_RB_PERSON);
		
		DBMS_OUTPUT.PUT_LINE('Deleted ' || SQL%ROWCOUNT || ' rows from REF_BOOK_PERSON.');

		commit;
		
		FOR c1 IN (
					select ui.index_name from user_indexes ui where lower(ui.table_name)='ref_book_person' and lower(ui.uniqueness)='nonunique'
				  )
		LOOP
			execute immediate 'alter index ' || c1.index_name || ' rebuild';
		END LOOP;
		
		execute immediate 'alter table REF_BOOK_PERSON enable constraint FK_REF_BOOK_PERSON_REPORT_DOC';
		
		DBMS_OUTPUT.PUT_LINE('Indexes rebuilded, constraint enabled');	
	
	END IF;
end;
/

declare 
	v_count number;
begin
	select count(1) into v_count from user_tables where table_name='TMP_DELETE_RB_PERSON';
	if v_count > 0 then 
		execute immediate 'drop table TMP_DELETE_RB_PERSON';
		dbms_output.put_line('Table TMP_DELETE_RB_PERSON was dropped.');	
	end if;	
end;
/

spool off

exit;
