-- https://jira.aplana.com/browse/SBRFNDFL-5184 Реализовать возможность хранения для ФЛ списка тербанков и заполнять этот список ТБ при первичной загрузке ФЛ
declare 
  v_run_condition number(1);
  v_task_name varchar2(128):='create_tables block #1 - create table ref_book_tb_person (SBRFNDFL-5184)';  
begin
	select decode(count(*),0,1,0) into v_run_condition from user_tables where lower(table_name)='ref_book_tb_person';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'create table ref_book_tb_person (
		id                number(18)           not null,
		record_id		  number(18)           not null,
		version           date                 not null,
		status            number(1)            not null,
		guid              varchar2(500 char)   not null,
		tb_department_id  number(18)           not null
		)';

		EXECUTE IMMEDIATE 'comment on table ref_book_tb_person is ''Справочник Тербанки для ФЛ при первичной загрузке''';
		EXECUTE IMMEDIATE 'comment on column ref_book_tb_person.id is ''Уникальный идентификатор''';
		EXECUTE IMMEDIATE 'comment on column ref_book_tb_person.record_id is ''Идентификатор строки. Может повторяться у разных версий''';
		EXECUTE IMMEDIATE 'comment on column ref_book_tb_person.version is ''Версия. Дата актуальности записи''';
		EXECUTE IMMEDIATE 'comment on column ref_book_tb_person.status is ''Статус записи (0 - обычная запись, -1 - удаленная, 1 - черновик, 2 - фиктивная)''';
		EXECUTE IMMEDIATE 'comment on column ref_book_tb_person.guid is ''Значение GUID''';
		EXECUTE IMMEDIATE 'comment on column ref_book_tb_person.tb_department_id is ''Ссылка на запись Справочник Подразделения''';
		
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
	
    v_task_name:='create_tables block #2 - create table ref_book_person_tb (SBRFNDFL-5184)';  
	select decode(count(*),0,1,0) into v_run_condition from user_tables where lower(table_name)='ref_book_person_tb';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'create table ref_book_person_tb(
		id                number(18)           not null,
		record_id		  number(18)           not null,
		version           date                 not null,
		status            number(1)            not null,
		person_id		  number(18)           not null,    
		tb_department_id  number(18)           not null,
		import_date       timestamp            default null
		)';

		EXECUTE IMMEDIATE 'comment on table ref_book_person_tb is ''Список тербанков назначенных ФЛ''';
		EXECUTE IMMEDIATE 'comment on column ref_book_person_tb.id is ''Уникальный идентификатор''';
		EXECUTE IMMEDIATE 'comment on column ref_book_person_tb.record_id is ''Идентификатор строки. Может повторяться у разных версий''';
		EXECUTE IMMEDIATE 'comment on column ref_book_person_tb.version is ''Версия. Дата актуальности записи''';
		EXECUTE IMMEDIATE 'comment on column ref_book_person_tb.status is ''Статус записи (0 - обычная запись, -1 - удаленная, 1 - черновик, 2 - фиктивная)''';
		EXECUTE IMMEDIATE 'comment on column ref_book_person_tb.person_id is ''Ссылка на запись справочника ФЛ''';
		EXECUTE IMMEDIATE 'comment on column ref_book_person_tb.tb_department_id is ''Ссылка на тербанк''';
		EXECUTE IMMEDIATE 'comment on column ref_book_person_tb.import_date is ''Время выгрузки файла для загрузки справочника ФЛ''';        

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
end;
/
