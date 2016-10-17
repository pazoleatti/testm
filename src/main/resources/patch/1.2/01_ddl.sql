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
	
		dbms_output.put_line(l_task_name||'[INFO]:'||' SUCCESS');
	else
		dbms_output.put_line(l_task_name||'[ERROR]:'||' column DEPARTMENT.CODE had already been modified');
	end if;
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(l_task_name||'[FATAL]:'||sqlerrm);
end;
/
-----------------------------------------------------------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-17019: 1.2 БД. Добавить событие "удаление блокировки"
--https://jira.aplana.com/browse/SBRFACCTAX-17268: 1.2 БД. Добавить событие "Действия пользователя в ФП СУНР"
declare 
	l_task_name varchar2(128) := 'DDL Block #2 - New event codes (SBRFACCTAX-17019 / SBRFACCTAX-17268)';
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
		
	dbms_output.put_line(l_task_name||'[INFO]:'||' SUCCESS');
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(l_task_name||'[FATAL]:'||sqlerrm);
end;
/
-----------------------------------------------------------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-16521: 1.2 Хранение промежуточных результатов поиска по большим НФ
declare 
	l_task_name varchar2(128) := 'DDL Block #3 - FORM_DATA.SEARCH() (SBRFACCTAX-16521)';
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
	
		dbms_output.put_line(l_task_name||'[INFO]:'||' SUCCESS');
	else
		dbms_output.put_line(l_task_name||'[ERROR]:'||' tables for FORM_DATA.SEARCH() had already existed');
	end if;
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(l_task_name||'[FATAL]:'||sqlerrm);
end;
/
-----------------------------------------------------------------------------------------------------------------------------
COMMIT;
EXIT;