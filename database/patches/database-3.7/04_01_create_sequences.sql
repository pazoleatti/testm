--3.6-snazin-3  https://jira.aplana.com/browse/SBRFNDFL-7096 Реализовать работу с журналом обмена с ЭДО 
--     https://conf.aplana.com/pages/viewpage.action?pageId=47141084 "Транспортное сообщение" 

DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='create_sequences block #1 - seq_transport_message';  
BEGIN
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_TRANSPORT_MESSAGE';

	IF v_run_condition=0 THEN
		EXECUTE IMMEDIATE 'create sequence seq_transport_message MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/


--3.7-dnovikov-9 https://jira.aplana.com/browse/SBRFNDFL-5679 - рефакторинг настроек подразделений, сиквенс-->

DECLARE
	v_run_condition number(1);
	max_id number(19);
	v_task_name varchar2(128):='create_sequence block #2 - seq_department_config';  
BEGIN
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_DEPARTMENT_CONFIG';

	IF v_run_condition=0 THEN
		select max(to_number(id)) into max_id from department_config;
		EXECUTE IMMEDIATE 'create sequence seq_department_config MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH '||to_char(max_id+1)||' CACHE 20 NOORDER  NOCYCLE ';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/


