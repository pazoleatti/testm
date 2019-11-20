-- 3.11-adudenko-01

DECLARE            
	v_task_name varchar2(128):='create sequences block #1 ';
	cnt number;
BEGIN
	select count(*) into cnt from user_sequences where sequence_name='SEQ_NDFL_APP2';
        if (cnt =0) then
	        execute immediate 'CREATE SEQUENCE SEQ_NDFL_APP2
					     MINVALUE 1 MAXVALUE 9999999999999999999999999999 
		                            INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE'; 
		dbms_output.put_line(v_task_name||'[INFO (sequences)]:'||' Success: SEQ_NDFL_APP2');
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

DECLARE            
	v_task_name varchar2(128):='create sequences block #2 ';
	cnt number;
BEGIN
	select count(*) into cnt from user_sequences where sequence_name='SEQ_NDFL_APP2_INCOME';
        if (cnt = 0) then
	        execute immediate 'CREATE SEQUENCE SEQ_NDFL_APP2_INCOME
					     MINVALUE 1 MAXVALUE 9999999999999999999999999999 
		                            INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE'; 
		dbms_output.put_line(v_task_name||'[INFO (sequences)]:'||' Success: SEQ_NDFL_APP2_INCOME');
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


