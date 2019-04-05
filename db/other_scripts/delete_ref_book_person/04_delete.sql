set heading off;
set serveroutput on;

alter session set skip_unusable_indexes = true;

spool &1

select 'Start Delete' from dual;

declare 
	v_count number;
begin

	select count(1) into v_count from TMP_DELETE_RB_PERSON;
	IF v_count > 0 THEN
	
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
		
	END IF;
exception
 when others then
   rollback;
   dbms_output.put_line('Delete from REF_BOOK_PERSON [FATAL]:'||sqlerrm);	
end;
/

spool off

exit;
