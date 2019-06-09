BEGIN
	FOR c1 IN (
					select ui.index_name from user_indexes ui where status ='UNUSABLE' and  lower(ui.uniqueness)='nonunique'
          )
    LOOP
		execute immediate 'alter index ' || c1.index_name || ' rebuild';
		dbms_output.put_line('Index: ' || c1.index_name || ' rebuild');
    END LOOP;
EXCEPTION
	when OTHERS then
		dbms_output.put_line('Alter index [FATAL]:'||sqlerrm);	
END;
/
