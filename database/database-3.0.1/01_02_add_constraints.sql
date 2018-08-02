-- https://jira.aplana.com/browse/SBRFNDFL-5184 Реализовать возможность хранения для ФЛ списка тербанков и заполнять этот список ТБ при первичной загрузке ФЛ
declare 
  v_run_condition number(1);
  v_task_name varchar2(128):='add_constraints block #1 - add constraint ref_book_tb_person_pk (SBRFNDFL-5184)';  
begin
	select decode(count(*),0,1,0) into v_run_condition from user_constraints where lower(constraint_name)='ref_book_tb_person_pk' AND lower(table_name)='ref_book_tb_person';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_tb_person add constraint ref_book_tb_person_pk primary key (id)';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
	
	v_task_name:='add_constraints block #2 - add constraint ref_book_person_tb_pk (SBRFNDFL-5184)';  
	select decode(count(*),0,1,0) into v_run_condition from user_constraints where lower(constraint_name)='ref_book_person_tb_pk' AND lower(table_name)='ref_book_person_tb';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_person_tb add constraint ref_book_person_tb_pk primary key (id)';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
	
	v_task_name:='add_constraints block #3 - add constraint tb_person_fk_department (SBRFNDFL-5184)';  
	select decode(count(*),0,1,0) into v_run_condition from user_constraints where lower(constraint_name)='tb_person_fk_department' AND lower(table_name)='ref_book_tb_person';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_tb_person add constraint tb_person_fk_department foreign key (tb_department_id) references department(id)';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
	
	v_task_name:='add_constraints block #4 - add constraint person_tb_fk_person (SBRFNDFL-5184)';  
	select decode(count(*),0,1,0) into v_run_condition from user_constraints where lower(constraint_name)='person_tb_fk_person' AND lower(table_name)='ref_book_person_tb';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_person_tb add constraint person_tb_fk_person foreign key (person_id) references ref_book_person(id) on delete cascade';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
	
	v_task_name:='add_constraints block #5 - add constraint person_tb_fk_department (SBRFNDFL-5184)';  
	select decode(count(*),0,1,0) into v_run_condition from user_constraints where lower(constraint_name)='person_tb_fk_department' AND lower(table_name)='ref_book_person_tb';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_person_tb add constraint person_tb_fk_department foreign key (tb_department_id) references department(id) on delete cascade';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
	
	v_task_name:='add_constraints block #6 - add constraint chk_ref_book_tb_person_status (SBRFNDFL-5184)';  
	select decode(count(*),0,1,0) into v_run_condition from user_constraints where lower(constraint_name)='chk_ref_book_tb_person_status' AND lower(table_name)='ref_book_tb_person';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_tb_person add constraint chk_ref_book_tb_person_status check (status in (-1, 0, 1, 2))';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
	
	v_task_name:='add_constraints block #7 - add constraint chk_ref_book_person_tb_status (SBRFNDFL-5184)';  
	select decode(count(*),0,1,0) into v_run_condition from user_constraints where lower(constraint_name)='chk_ref_book_person_tb_status' AND lower(table_name)='ref_book_person_tb';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table ref_book_person_tb add constraint chk_ref_book_person_tb_status check (status in (-1, 0, 1, 2))';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
