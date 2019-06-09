-- https://jira.aplana.com/browse/SBRFNDFL-5080 новые виды КНФ
declare 
  v_run_condition number(1);
  v_task_name varchar2(128):='00_01_create_tables block #1 - create table declaration_data_person';  
begin
	select decode(count(*),0,1,0) into v_run_condition from user_tables where lower(table_name)='declaration_data_person';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'CREATE TABLE declaration_data_person (
                declaration_data_id NUMBER(18) NOT NULL,
                person_id           NUMBER(18) NOT NULL,
                CONSTRAINT declaration_data_person_pk PRIMARY KEY (declaration_data_id, person_id)
            ) organization INDEX';

		EXECUTE IMMEDIATE 'comment on table declaration_data_person is ''Включаемые в КНФ ФЛ''';
		EXECUTE IMMEDIATE 'comment on column declaration_data_person.declaration_data_id is ''Ид КНФ''';
		EXECUTE IMMEDIATE 'comment on column declaration_data_person.person_id is ''Ид ФЛ''';
		
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
	
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
end;
/
