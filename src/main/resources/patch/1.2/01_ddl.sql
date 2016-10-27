set serveroutput on size 1000000;
set linesize 128;

-----------------------------------------------------------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-16791: 1.2. Справочники. Ошибка при изменении кода подразделения
declare 
	l_task_name varchar2(128) := 'DDL Block #1 - Department''s code extension (SBRFACCTAX-16791)';
	l_rerun_condition decimal(1) := 0;
begin
	select case when data_precision < 15 then 1 else 0 end into l_rerun_condition from user_tab_columns where table_name = 'DEPARTMENT' and column_name = 'CODE';
	
	--part1: https://jira.aplana.com/browse/SBRFACCTAX-14930
	if l_rerun_condition = 1 then 
		execute immediate 'alter table department modify code number(15)';
	
		dbms_output.put_line(l_task_name||'[INFO]:'||' Success');
	else
		dbms_output.put_line(l_task_name||'[ERROR]:'||' column DEPARTMENT.CODE had already been modified');
	end if;
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(l_task_name||'[FATAL]:'||sqlerrm);
end;
/
-----------------------------------------------------------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-17135: 1.2 Добавить атрибут "Используется в СУНР" в справочник подразделений
declare 
	l_task_name varchar2(128) := 'DDL Block #2 - SUNR usage attributes (SBRFACCTAX-17135)';
	l_rerun_condition decimal(1) := 0;
begin
	select count(*) into l_rerun_condition from user_tab_columns where table_name = 'DEPARTMENT' and column_name = 'SUNR_USE';
	
	if l_rerun_condition = 0 then 
		execute immediate 'alter table department add sunr_use number(1) default 0 not null';
		execute immediate 'comment on column department.sunr_use is ''Признак, что используется в АС СУНР''';
		execute immediate 'alter table department add constraint department_chk_sunr_use check (sunr_use in (0, 1))';
		
		insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (168,30,'Используется в АС СУНР','SUNR_USE',2,10,null,null,1,0,15,0,0,null,6,0,1);
	
		dbms_output.put_line(l_task_name||'[INFO]:'||' Success');
	else
		dbms_output.put_line(l_task_name||'[ERROR]:'||' column DEPARTMENT.SUNR_USE had already been modified');
	end if;
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(l_task_name||'[FATAL]:'||sqlerrm);
end;
/
commit;
-----------------------------------------------------------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-17019: 1.2 БД. Добавить событие "удаление блокировки"
--https://jira.aplana.com/browse/SBRFACCTAX-17268: 1.2 БД. Добавить событие "Действия пользователя в ФП СУНР"
declare 
	l_task_name varchar2(128) := 'DDL Block #3 - New event codes (SBRFACCTAX-17019 / SBRFACCTAX-17268)';
	l_rerun_condition decimal(1) := 0;
begin
	select count(*) into l_rerun_condition from event where id=960;	
	if l_rerun_condition = 0 then 
		insert into event (id, name) values(960, 'Удаление блокировки');
		dbms_output.put_line(l_task_name||'[INFO]:'||' Event.id = 960 added');
	end if;	
	
	select count(*) into l_rerun_condition from event where id=504;	
	if l_rerun_condition = 0 then 
		insert into event(id, name) values(504, 'Действия пользователя в ФП СУНР');
		dbms_output.put_line(l_task_name||'[INFO]:'||' Event.id = 504 added');
	end if;
		
	--Common block for both events		
	execute immediate 'alter table log_system drop constraint log_system_chk_rp';
	execute immediate 'alter table log_system add constraint log_system_chk_rp check (event_id in (7, 11, 401, 402, 501, 502, 503, 504, 601, 650, 901, 902, 903, 810, 811, 812, 813, 820, 821, 830, 831, 832, 840, 841, 842, 850, 860, 701, 702, 703, 704, 705, 904, 951, 960) or report_period_name is not null) enable';
	execute immediate 'alter table log_system drop constraint log_system_chk_dcl_form';
	execute immediate 'alter table log_system add constraint log_system_chk_dcl_form check (event_id in (7, 11, 401, 402, 501, 502, 503, 504, 601, 650, 901, 902, 903, 810, 811, 812, 813, 820, 821, 830, 831, 832, 840, 841, 842, 850, 860, 701, 702, 703, 704, 705, 904, 951, 960) or declaration_type_name is not null or (form_type_name is not null and form_kind_id is not null)) enable';
		
	dbms_output.put_line(l_task_name||'[INFO]:'||' Success');
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(l_task_name||'[FATAL]:'||sqlerrm);
end;
/
-----------------------------------------------------------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-16521: 1.2 Хранение промежуточных результатов поиска по большим НФ
declare 
	l_task_name varchar2(128) := 'DDL Block #4 - FORM_DATA.SEARCH() (SBRFACCTAX-16521)';
	l_rerun_condition decimal(1) := 0;
begin
	select count(*) into l_rerun_condition from user_tables where table_name = 'FORM_SEARCH_DATA_RESULT';
	
	if l_rerun_condition = 0 then 
		execute immediate 'CREATE TABLE FORM_SEARCH_DATA_RESULT (SESSION_ID  NUMBER(10,0), ID  NUMBER(9,0),  ROW_INDEX  NUMBER(9,0),  COLUMN_INDEX NUMBER(9,0),  RAW_VALUE  VARCHAR2(4000 BYTE),  ORD   NUMBER(9,0) ) PARTITION BY LIST (SESSION_ID) (PARTITION P0 VALUES (0))';
		execute immediate 'COMMENT ON COLUMN FORM_SEARCH_DATA_RESULT.ID IS ''Идентификатор результата поиска''';
		execute immediate 'COMMENT ON COLUMN FORM_SEARCH_DATA_RESULT.ROW_INDEX  IS ''Номер строки в форме''';
		execute immediate 'COMMENT ON COLUMN FORM_SEARCH_DATA_RESULT.COLUMN_INDEX  IS ''Номер столбца в форме''';
		execute immediate 'COMMENT ON COLUMN FORM_SEARCH_DATA_RESULT.RAW_VALUE  IS ''Значение в ячейке формы''';
		execute immediate 'COMMENT ON COLUMN FORM_SEARCH_DATA_RESULT.ORD  IS ''Порядковый номер''';
		execute immediate 'COMMENT ON COLUMN FORM_SEARCH_DATA_RESULT.SESSION_ID IS ''Идентификатор сессии в которой выполнялся поиск''';
		execute immediate 'CREATE UNIQUE INDEX I_SEARCH_DATA_RESULT ON FORM_SEARCH_DATA_RESULT ( SESSION_ID, ID , ORD ) LOCAL';
		
		execute immediate 'CREATE TABLE FORM_SEARCH_RESULT  (ID  NUMBER(9,0) PRIMARY KEY, SESSION_ID  NUMBER(10,0), FORM_DATA_ID  NUMBER(18,0),  "DATE"  DATE, KEY VARCHAR2(4000 BYTE),  ROWS_COUNT  NUMBER(9,0))';
		execute immediate 'COMMENT ON COLUMN FORM_SEARCH_RESULT.ID  IS ''Идентификатор результата поиска''';
		execute immediate 'COMMENT ON COLUMN FORM_SEARCH_RESULT.SESSION_ID  IS ''Идентификатор сессии в которой выполнялся поиск''';
		execute immediate 'COMMENT ON COLUMN FORM_SEARCH_RESULT.FORM_DATA_ID  IS ''Идентификатор формы в которой выполнялся поиск''';
		execute immediate 'COMMENT ON COLUMN FORM_SEARCH_RESULT."DATE"  IS ''Дата выполнения поиска''';
		execute immediate 'COMMENT ON COLUMN FORM_SEARCH_RESULT.KEY  IS ''Строка поиска''';
		
		execute immediate 'ALTER TABLE FORM_SEARCH_RESULT ADD CONSTRAINT FORM_SEARCH_RESULT_FK_FORMDATA FOREIGN KEY (FORM_DATA_ID) REFERENCES FORM_DATA(ID) ON DELETE CASCADE';
		execute immediate 'CREATE INDEX I_FORM_SEARCH_RESULT_FORMDATA ON FORM_SEARCH_RESULT(FORM_DATA_ID)';
		execute immediate 'CREATE SEQUENCE SEQ_FORM_SEARCH_RESULT START WITH 1';
		
		execute immediate 'CREATE GLOBAL TEMPORARY TABLE FORM_SEARCH_DATA_RESULT_TMP(ROW_INDEX NUMBER(9,0), COLUMN_INDEX NUMBER(9,0), RAW_VALUE VARCHAR2(4000 BYTE)) ON COMMIT DELETE ROWS ';
	
		dbms_output.put_line(l_task_name||'[INFO]:'||' Success');
	else
		dbms_output.put_line(l_task_name||'[ERROR]:'||' tables for FORM_DATA.SEARCH() had already existed');
	end if;
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(l_task_name||'[FATAL]:'||sqlerrm);
end;
/
-----------------------------------------------------------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-17282: 1.2 БД. Поправить Check constraint в LOG_SYSTEM
declare 
	l_task_name varchar2(128) := 'DDL Block #5 - Alter constraint LOG_SYSTEM_CHK_AFT (SBRFACCTAX-17282)';
begin
	execute immediate 'alter table log_system drop constraint log_system_chk_aft';
	execute immediate 'alter table log_system add constraint log_system_chk_aft check (audit_form_type_id = 1 and not event_id in (701,702,703,704,705,904) and form_type_name is not null and department_name is not null or audit_form_type_id = 2 and not event_id in (701,702,703,704,705,904) and declaration_type_name is not null and department_name is not null or audit_form_type_id = 3 and event_id in (701,702,703,704,705,904) and form_type_name is not null and department_name is null or audit_form_type_id = 4 and event_id in (701,702,703,704,705,904) and declaration_type_name is not null and department_name is null or audit_form_type_id in (5,6) and event_id in (7) and form_type_name is null and declaration_type_name is null or audit_form_type_id is null or event_id in (402))';
		
	dbms_output.put_line(l_task_name||'[INFO]:'||' Success');
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(l_task_name||'[FATAL]:'||sqlerrm);
end;
/
-----------------------------------------------------------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-17000: 1.2 Создать таблицу для хранения не переданных изменений.
declare 
	l_task_name varchar2(128) := 'DDL Block #6 - DEPARTMENT_CHANGE (SBRFACCTAX-17000)';
	l_rerun_condition decimal(1) := 0;
begin
	select count(*) into l_rerun_condition from user_tables where table_name = 'DEPARTMENT_CHANGE';
	
	if l_rerun_condition = 0 then 
		execute immediate 'create table department_change (  department_id number(9) not null,  log_date date not null,  operationType number(9) not null,  hier_level number(9),  name varchar2(510),  parent_id number(9),  type number(9),  shortname varchar2(510),  tb_index varchar2(3),  sbrf_code varchar2(255),  region varchar2(510),  is_active number(1),  code number(15),  garant_use number(1),  sunr_use number(1))';
		
		execute immediate 'comment on table department_change is ''Изменения справочника "Подразделения"''';
		execute immediate 'comment on column department_change.code is ''Код подразделения''';
		execute immediate 'comment on column department_change.department_id is ''Идентификатор подразделения''';
		execute immediate 'comment on column department_change.garant_use is ''Признак, что используется в модуле Гарантий (0 - не используется, 1 - используется)''';
		execute immediate 'comment on column department_change.hier_level is ''Уровень записи в иерархии''';
		execute immediate 'comment on column department_change.is_active is ''Действующее подразделение (0 - не действующее, 1 - действующее)''';
		execute immediate 'comment on column department_change.log_date is ''Дата/время изменения данных''';
		execute immediate 'comment on column department_change.name is ''Наименование подразделения''';
		execute immediate 'comment on column department_change.operationtype is ''Тип операции (0 - создание, 1 - изменение, 2 - удаление)''';
		execute immediate 'comment on column department_change.parent_id is ''Идентификатор родительского подразделения''';
		execute immediate 'comment on column department_change.region is ''Регион''';
		execute immediate 'comment on column department_change.sbrf_code is ''Код подразделения в нотации Сбербанка''';
		execute immediate 'comment on column department_change.shortname is ''Сокращенное наименование подразделения''';
		execute immediate 'comment on column department_change.sunr_use is ''Признак, что используется в АС СУНР (0 - не используется, 1 - используется)''';
		execute immediate 'comment on column department_change.tb_index is ''Индекс территориального банка''';
		execute immediate 'comment on column department_change.type is ''Тип подразделения (1 - Банк, 2 - ТБ, 3 - ЦСКО, ПЦП, 4 - Управление, 5 - Не передается в СУДИР)''';
		
		execute immediate 'alter table department_change add constraint dep_change_pk primary key (department_id, log_date)';
		execute immediate 'alter table department_change add constraint dep_change_chk_op_type check ((operationtype in (0,1) and hier_level is not null and name is not null and type is not null and is_active is not null and garant_use is not null and sunr_use is not null and code is not null) or (operationtype = 2 and hier_level is null and name is null and type is null and is_active is null and garant_use is null and sunr_use is null and code is null))';
		execute immediate 'alter table department_change add constraint dep_change_chk_is_active check (is_active in (0, 1))';
		execute immediate 'alter table department_change add constraint dep_change_chk_garant_use check (garant_use in (0, 1))';
		execute immediate 'alter table department_change add constraint dep_change_chk_sunr_use check (sunr_use in (0, 1))';
		
		--https://jira.aplana.com/browse/SBRFACCTAX-17311: передачу первоначального наполнения справочника "Подразделения" в СУНР
		INSERT INTO department_change
		SELECT dep.ID AS department_id,  (SYSDATE + LEVEL/24/60/60) AS log_date, 0 AS operationtype, LEVEL AS hier_level, dep.NAME, dep.parent_id, dep.TYPE, dep.shortname, dep.tb_index, dep.sbrf_code, 
		  rbv.string_value AS region, dep.is_active, dep.code, dep.garant_use, dep.sunr_use
		FROM department dep
		LEFT JOIN ref_book_value rbv ON rbv.record_id = dep.region_id AND rbv.attribute_id = 10
		START WITH dep.parent_id IS NULL 
		CONNECT BY PRIOR dep.ID = dep.parent_id;
		dbms_output.put_line(l_task_name||'[INFO]: '||sql%rowcount||' row(s) added in department_change');
	
		dbms_output.put_line(l_task_name||'[INFO]:'||' Success');
	else
		dbms_output.put_line(l_task_name||'[ERROR]:'||' table DEPARTMENT_CHANGE had already existed');
	end if;
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(l_task_name||'[FATAL]:'||sqlerrm);
end;
/
-----------------------------------------------------------------------------------------------------------------------------
COMMIT;
EXIT;