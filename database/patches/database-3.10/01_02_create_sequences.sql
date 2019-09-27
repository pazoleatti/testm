-- 3.10-skononova2

DECLARE
	cnt number;
	v_task_name varchar2(128):='create sequences block #1 ';
	cnt_all number;  
BEGIN
	cnt_all :=0;

		for tax_year in (select year from tax_period)
		loop
		    select count(*) into cnt from user_sequences where sequence_name='SEQ_NDFL_REFERENCES_'||
			to_char(tax_year.year);
		    if (cnt =0) then
		        execute immediate 'CREATE SEQUENCE SEQ_NDFL_REFERENCES_'||to_char(tax_year.year)||
					    ' MINVALUE 1 MAXVALUE 9999999999999999999999999999 
		                            INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE'; 
			cnt_all :=cnt_all + 1;   
		    end if;
		end loop;
	if cnt_all >0 then
		dbms_output.put_line(v_task_name||'[INFO (sequences)]:'||' Success: create '|| to_char(cnt_all) || ' sequences');
	else
		dbms_output.put_line(v_task_name||'[WARNING (sequences)]:'||' changes had already been implemented');
	end if;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
commit;
/
