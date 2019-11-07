BEGIN
	FOR c1 IN (
		select ui.index_name from user_indexes ui where table_name in ('REF_BOOK_PERSON','REF_BOOK_ID_DOC', 'REF_BOOK_ID_TAX_PAYER') 
          )
    LOOP
		execute immediate 'alter index ' || c1.index_name || ' rebuild compute statistics';
		dbms_output.put_line('Index: ' || c1.index_name || ' rebuild');
    END LOOP;
EXCEPTION
	when OTHERS then
		dbms_output.put_line('Alter index [FATAL]:'||sqlerrm);	
END;
/
