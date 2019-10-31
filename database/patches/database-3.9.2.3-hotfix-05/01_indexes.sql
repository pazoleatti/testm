
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='indexes block #1  - SRCH_REFB_TAX_PAYER_INP_ASNU';  
BEGIN
	select count(*) into v_run_condition from user_indexes where INDEX_NAME='SRCH_REFB_TAX_PAYER_INP_ASNU';
	IF v_run_condition=1 THEN
		execute immediate 'DROP INDEX SRCH_REFB_TAX_PAYER_INP_ASNU';
	end if;
        execute immediate 'CREATE INDEX SRCH_REFB_TAX_PAYER_INP_ASNU ON REF_BOOK_ID_TAX_PAYER (INP, AS_NU )';
	dbms_output.put_line(v_task_name||'[INFO ]:'||' Success');

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
commit;

