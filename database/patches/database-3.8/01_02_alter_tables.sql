-- 3.8-ytrofimov-3
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #1 - alter table declaration_data';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where TABLE_NAME='DECLARATION_DATA' and COLUMN_NAME='PERSON_ID';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table declaration_data add person_id number(18) ';
		EXECUTE IMMEDIATE 'alter table declaration_data 
					add constraint fk_declaration_data_person 
						foreign key(person_id) references ref_book_person (id)';
		EXECUTE IMMEDIATE 'comment on column declaration_data.person_id is ''Физ. лицо''';
		dbms_output.put_line(v_task_name||'[INFO (created date)]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING (person_id)]:'||' changes had already been implemented');
	END IF;

	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where TABLE_NAME='DECLARATION_DATA' and COLUMN_NAME='SIGNATORY';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table declaration_data add signatory  varchar(60 char)';
		EXECUTE IMMEDIATE 'comment on column declaration_data.signatory is ''Подписант''';
		dbms_output.put_line(v_task_name||'[INFO (signatory)]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING (signatory)]:'||' changes had already been implemented');
	END IF;

	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

COMMIT;


