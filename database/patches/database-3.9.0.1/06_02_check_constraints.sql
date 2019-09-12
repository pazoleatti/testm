BEGIN
	FOR c1 IN (
					select uc.TABLE_NAME, uc.CONSTRAINT_NAME from user_constraints uc where uc.status='DISABLED' and uc.CONSTRAINT_NAME not like '%FIAS%'
          )
    LOOP
		execute immediate 'alter table ' || c1.TABLE_NAME || ' enable constraint ' || c1.CONSTRAINT_NAME;
		dbms_output.put_line('Constraint: ' || c1.TABLE_NAME || '.' || c1.CONSTRAINT_NAME || ' enabled');
    END LOOP;
EXCEPTION
	when OTHERS then
		dbms_output.put_line('Alter table enable constraint [FATAL]:'||sqlerrm);	
END;
/
