declare 
	v_run_condition number(1);
	v_task_name varchar2(128):='01_01_drop_create_tables block #1 - drop table ref_book_value ';  
begin
	select count(*) into v_run_condition from user_tables where lower(table_name)=lower('ref_book_value');
	if v_run_condition > 0 then 
		execute immediate 'drop table ref_book_value CASCADE CONSTRAINTS';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	end if;	
	
exception when others then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
end;
/

declare 
	v_run_condition number(1);
	v_task_name varchar2(128):='01_01_drop_create_tables block #2 - drop table ref_book_record ';  
begin
	select count(*) into v_run_condition from user_tables where lower(table_name)=lower('ref_book_record');
	if v_run_condition > 0 then 
		execute immediate 'drop table ref_book_record CASCADE CONSTRAINTS';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	end if;	
	
exception when others then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
end;
/

-- https://jira.aplana.com/browse/SBRFNDFL-5080 новые виды КНФ
declare 
  v_run_condition number(1);
  v_task_name varchar2(128):='01_01_drop_create_tables block #3 - create table declaration_data_kpp';  
begin
	select decode(count(*),0,1,0) into v_run_condition from user_tables where lower(table_name)='declaration_data_kpp';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'CREATE TABLE declaration_data_kpp (
                declaration_data_id NUMBER(18) NOT NULL,
                kpp                 VARCHAR2(9 CHAR) NOT NULL,
                CONSTRAINT declaration_data_kpp_pk PRIMARY KEY (declaration_data_id, kpp)
            ) organization INDEX';

		EXECUTE IMMEDIATE 'comment on table declaration_data_kpp is ''Включаемые в КНФ КПП''';
		EXECUTE IMMEDIATE 'comment on column declaration_data_kpp.declaration_data_id is ''Ид КНФ''';
		EXECUTE IMMEDIATE 'comment on column declaration_data_kpp.kpp is ''КПП''';
		
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
	
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
end;
/

-- https://jira.aplana.com/browse/SBRFNDFL-5080 новые виды КНФ
declare 
  v_run_condition number(1);
  v_task_name varchar2(128):='01_01_drop_create_tables block #5 - create table ref_book_knf_type';  
begin
	select decode(count(*),0,1,0) into v_run_condition from user_tables where lower(table_name)='ref_book_knf_type';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'CREATE TABLE ref_book_knf_type (
                id   NUMBER(9) NOT NULL,
                name VARCHAR2(2000 CHAR) NOT NULL,
                status NUMBER(1,0) default 0,
                CONSTRAINT ref_book_knf_type_pk PRIMARY KEY (id))';

		EXECUTE IMMEDIATE 'comment on table ref_book_knf_type is ''Типы КНФ''';
		EXECUTE IMMEDIATE 'comment on column ref_book_knf_type.id is ''Код''';
		EXECUTE IMMEDIATE 'comment on column ref_book_knf_type.name is ''Наименование типа КНФ''';
		
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
	
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
end;
/
COMMIT;


-- https://jira.aplana.com/browse/SBRFNDFL-5080 новые виды КНФ
declare 
  v_task_name varchar2(128):='01_01_drop_create_tables block #6 - merge into ref_book_knf_type ';  
begin
	merge into ref_book_knf_type a using
	(select 1 as id, 'КНФ по всем данным' as name from dual
	 union all
	 select 2 as id, 'КНФ по неудержанному налогу' as name from dual
	 union all
	 select 3 as id, 'КНФ по обособленному подразделению' as name from dual
	 union all
	 select 4 as id, 'КНФ по ФЛ' as name from dual
	 union all
	 select 5 as id, 'КНФ для Приложения 2' as name from dual
	) b
	on (a.id=b.id)
	when not matched then
		insert (id, name)
		values (b.id, b.name);

	CASE SQL%ROWCOUNT 
	WHEN 5 THEN dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	ELSE dbms_output.put_line(v_task_name||'[ERROR]:'||' changes had already been partly implemented');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;
