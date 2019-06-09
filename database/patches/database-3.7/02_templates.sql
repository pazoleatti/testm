--ÂÐÅÌÅÍÍÎ ÒÎËÜÊÎ â 3.7
--ÈÑÏÐÀÂËÅÍÈÅ ÎØÈÁÎÊ Â ÄÀÍÍÛÕ
--ÏÎÑËÅ 3.7 ÓÁÐÀÒÜ!

merge into blob_data dst using
(
	select '7833e689-c60b-4a1b-98be-b181079d0c29' as id, 'report.xlsx' as name,
		  to_date ('19.04.2019','dd.mm.yyyy') as creation_date from dual) src
on (src.id=dst.id)
when not matched then
insert (id, name, creation_date, data) values (src.id, src.name, src.creation_date, 
	(select data from blob_data where id='4b85f92c-7fd0-4d67-834d-e61g34684336'));

update declaration_template_file set blob_data_id='7833e689-c60b-4a1b-98be-b181079d0c29'
	where declaration_template_id=101 and blob_data_id='4b85f92c-7fd0-4d67-834d-e61g34684336';
commit;
                                        
PROMPT Create service tables...

declare 
	v_count number;
begin
	select count(1) into v_count from user_tables where table_name='BLOB_NAMES_TMP';
	dbms_output.put_line('Create table BLOB_NAMES_TMP...');
	if v_count = 0 then execute immediate 
		'CREATE TABLE BLOB_NAMES_TMP 
   (ID NUMBER(9,0),	
	BLOB_DATA_ID VARCHAR2(36 BYTE) NOT NULL, 
    NAME VARCHAR2(530 BYTE)
   ) 
 ';
		dbms_output.put_line('Table BLOB_NAMES_TMP created.');	
	else
		dbms_output.put_line('Table BLOB_NAMES_TMP already exists.');
		execute immediate 'delete from BLOB_NAMES_TMP';
		commit;
		dbms_output.put_line('Table BLOB_NAMES_TMP cleared.');
	end if;	
exception when others then
   dbms_output.put_line(sqlerrm);
   raise_application_error(-20999,'Error create table BLOB_NAMES_TMP.');   
end;
/

declare 
	v_count number;
begin
	select count(1) into v_count from user_tables where table_name='BLOB_ID_TMP';
	dbms_output.put_line('Create table BLOB_ID_TMP...');
	if v_count = 0 then execute immediate 
		'CREATE TABLE BLOB_ID_TMP 
   (
	BLOB_DATA_ID VARCHAR2(36 BYTE) NOT NULL,
	UPDATED NUMBER(1,0),
	BLOB_DATA_ID_OLD VARCHAR2(36 BYTE)
   ) 
 ';
		dbms_output.put_line('Table BLOB_ID_TMP created.');	
	else
		dbms_output.put_line('Table BLOB_ID_TMP already exists.');
		execute immediate 'delete from BLOB_ID_TMP';
		commit;
		dbms_output.put_line('Table BLOB_ID_TMP cleared.');
	end if;	
exception when others then
   dbms_output.put_line(sqlerrm);
   raise_application_error(-20999,'Error create table BLOB_ID_TMP.');   
end;
/

declare 
	v_count number;
begin
	select count(1) into v_count from user_tables where table_name='BLOB_DATA_TMP';
	dbms_output.put_line('Create table BLOB_DATA_TMP...');
	if v_count = 0 then execute immediate 
		'CREATE TABLE BLOB_DATA_TMP 
   (	ID VARCHAR2(36 BYTE) NOT NULL, 
    NAME VARCHAR2(530 BYTE), 
	DATA BLOB,
	CREATION_DATE DATE,
	DECLARATION_TEMPLATE_ID NUMBER(9,0)
   ) 
 ';
		dbms_output.put_line('Table BLOB_DATA_TMP created.');	
	else
		dbms_output.put_line('Table BLOB_DATA_TMP already exists.');
		execute immediate 'delete from BLOB_DATA_TMP';
		commit;
		dbms_output.put_line('Table BLOB_DATA_TMP cleared.');
	end if;	
exception when others then
   dbms_output.put_line(sqlerrm);
   raise_application_error(-20999,'Error create table BLOB_DATA_TMP.');   
end;
/


declare 
	v_count number;
begin
	select count(1) into v_count from user_tables where table_name='DECLARATION_TEMPLATE_TMP';
	dbms_output.put_line('Create table DECLARATION_TEMPLATE_TMP...');
	if v_count = 0 then execute immediate 
		'CREATE TABLE DECLARATION_TEMPLATE_TMP 
   (	ID NUMBER(9,0) NOT NULL, 
	STATUS NUMBER(1,0),
	VERSION DATE,
	NAME VARCHAR2(512 CHAR),
	CREATE_SCRIPT CLOB,
	JRXML VARCHAR2(36 BYTE),
	DECLARATION_TYPE_ID NUMBER(18,0),
	XSD VARCHAR2(36 BYTE),
	FORM_KIND NUMBER(18,0),
	FORM_TYPE NUMBER(18,0)
   ) 
 ';
		dbms_output.put_line('Table DECLARATION_TEMPLATE_TMP created.');	
	else
		dbms_output.put_line('Table DECLARATION_TEMPLATE_TMP already exists.');
		execute immediate 'delete from DECLARATION_TEMPLATE_TMP';
		commit;
		dbms_output.put_line('Table DECLARATION_TEMPLATE_TMP cleared.');
	end if;	
exception when others then
   dbms_output.put_line(sqlerrm);
   raise_application_error(-20999,'Error create table DECLARATION_TEMPLATE_TMP.');   
end;
/

declare 
	v_count number;
begin
	select count(1) into v_count from user_tables where table_name='DECL_TEMPLATE_EVENT_SCRIPT_TMP';
	dbms_output.put_line('Create table DECL_TEMPLATE_EVENT_SCRIPT_TMP...');
	if v_count = 0 then execute immediate 
		'CREATE TABLE DECL_TEMPLATE_EVENT_SCRIPT_TMP 
   (	ID NUMBER(19,0) NOT NULL, 
    DECLARATION_TEMPLATE_ID NUMBER(19,0),
	EVENT_ID NUMBER(19,0),
	SCRIPT CLOB
   ) 
 ';
		dbms_output.put_line('Table DECL_TEMPLATE_EVENT_SCRIPT_TMP created.');	
	else
		dbms_output.put_line('Table DECL_TEMPLATE_EVENT_SCRIPT_TMP already exists.');
		execute immediate 'delete from DECL_TEMPLATE_EVENT_SCRIPT_TMP';
		commit;
		dbms_output.put_line('Table DECL_TEMPLATE_EVENT_SCRIPT_TMP cleared.');
	end if;	
exception when others then
   dbms_output.put_line(sqlerrm);
   raise_application_error(-20999,'Error create table DECL_TEMPLATE_EVENT_SCRIPT_TMP.');   
end;
/

PROMPT Fill tables
PROMPT ======================

PROMPT Fill service tables...
--3.6-ytrofimov-4, 3.7-ytrofimov-6, 3.7-ytrofimov-8, 3.7-dnovikov-1
HOST "&2\sqlldr" &3 control=database-3.7/templates/ldr/refbook/blob_data.ldr log=&4/02_01_refbook_blob_data.txt bad=&5/02_01_refbook_blob_data.bad

--3.6-ytrofimov-6, 3.6-ytrofimov-7, 3.6-ytrofimov-8, 3.6-ytrofimov-9, 3.7-ishevchuk-2, 3.7-ishevchuk-1, 3.7-snazin-4, 
--3.7-dnovikov-29
HOST "&2\sqlldr" &3 control=database-3.7/templates/ldr/ndfl/blob_data.ldr log=&4/02_02_ndfl_blob_data.txt bad=&5/02_02_ndfl_blob_data.bad

--3.6-ytrofimov-5, 3.7-ytrofimov-5, 3.7-ytrofimov-7, 3.7-dnovikov-5
HOST "&2\sqlldr" &3 control=database-3.7/templates/ldr/ndfl/template.ldr log=&4/02_03_ndfl_template.txt bad=&5/02_03_ndfl_template.bad

--3.6-ytrofimov-1, 3.7-ytrofimov-1, 3.6-ytrofimov-2, 3.7-ytrofimov-2, 3.6-ytrofimov-5, 3.7-dnovikov-6, 3.6-ytrofimov-5, 3.7-dnovikov-2,
--3.6-ytrofimov-5, 3.7-dnovikov-3, 3.6-ytrofimov-5, 3.7-dnovikov-4, 3.7-dnovikov-23
HOST "&2\sqlldr" &3 control=database-3.7/templates/ldr/ndfl/template_script.ldr log=&4/02_04_ndfl_template_script.txt bad=&5/02_04_ndfl_template_script.bad

BEGIN
	insert into blob_names_tmp(id, blob_data_id, name)
	select dt.id,bd.id blob_data_id, bd.name from declaration_template dt join  blob_data bd on dt.xsd=bd.id
	union 
	select dt.id,bd.id blob_data_id, bd.name from declaration_template dt join  blob_data bd on dt.jrxml=bd.id
	union 
	select dt.id,bd.id blob_data_id, bd.name from declaration_template dt join declaration_template_file dtf on dt.id=dtf.declaration_template_id
	join blob_data bd on dtf.blob_data_id=bd.id;
	commit;
END;
/

PROMPT Fill tables...
@@02_01_fill_templates.sql

PROMPT Fill complete
PROMPT ======================

set feedback off;
set verify off;
set serveroutput on;


commit;
   
PROMPT drop service tables

declare 
	v_count number;
begin
	select count(1) into v_count from user_tables where table_name='BLOB_NAMES_TMP';
	dbms_output.put_line('Drop table BLOB_NAMES_TMP...');
	if v_count > 0 then 
		execute immediate 'drop table BLOB_NAMES_TMP';
		dbms_output.put_line('Table BLOB_NAMES_TMP was dropped.');	
	end if;	
	
	select count(1) into v_count from user_tables where table_name='BLOB_ID_TMP';
	dbms_output.put_line('Drop table BLOB_ID_TMP...');
	if v_count > 0 then 
		execute immediate 'drop table BLOB_ID_TMP';
		dbms_output.put_line('Table BLOB_ID_TMP was dropped.');	
	end if;	

	select count(1) into v_count from user_tables where table_name='BLOB_DATA_TMP';
	dbms_output.put_line('Drop table BLOB_DATA_TMP...');
	if v_count > 0 then 
		execute immediate 'drop table BLOB_DATA_TMP';
		dbms_output.put_line('Table BLOB_DATA_TMP was dropped.');	
	end if;	
	
	select count(1) into v_count from user_tables where table_name='DECLARATION_TEMPLATE_TMP';
	dbms_output.put_line('drop table DECLARATION_TEMPLATE_TMP...');
	if v_count > 0 then 
		execute immediate 'drop table DECLARATION_TEMPLATE_TMP';
		dbms_output.put_line('Table DECLARATION_TEMPLATE_TMP was dropped.');	
	end if;	
	
	select count(1) into v_count from user_tables where table_name='DECL_TEMPLATE_EVENT_SCRIPT_TMP';
	dbms_output.put_line('drop table DECL_TEMPLATE_EVENT_SCRIPT_TMP...');
	if v_count > 0 then 
		execute immediate 'drop table DECL_TEMPLATE_EVENT_SCRIPT_TMP';
		dbms_output.put_line('Table DECL_TEMPLATE_EVENT_SCRIPT_TMP was dropped.');	
	end if;	
exception when others then
   dbms_output.put_line(sqlerrm);
   raise_application_error(-20999,'Error drop service tables');   
end;

/


