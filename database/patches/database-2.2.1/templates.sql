set feedback off;
set verify off;
set serveroutput on;
spool &1;

WHENEVER SQLERROR EXIT SQL.SQLCODE

PROMPT Create service tables...

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
	CREATION_DATE DATE
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
HOST "&2\sqlldr" &3 control=templates/ldr/ndfl/blob_data.ldr log=&4/00_ndfl_blob_data.txt bad=&5/00_ndfl_blob_data.bad
HOST "&2\sqlldr" &3 control=templates/ldr/ndfl/template.ldr log=&4/01_ndfl_template.txt bad=&5/01_ndfl_template.bad
HOST "&2\sqlldr" &3 control=templates/ldr/ndfl/template_script.ldr log=&4/02_ndfl_template_script.txt bad=&5/02_ndfl_template_script.bad
HOST "&2\sqlldr" &3 control=templates/ldr/refbook/blob_data.ldr log=&4/03_refbook_blob_data.txt bad=&5/03_refbook_blob_data.bad

PROMPT Fill tables...
@fill_templates.sql

PROMPT Fill complete
PROMPT ======================


set feedback off;
set verify off;
set serveroutput on;


commit;
   
PROMPT templates updated
PROMPT ======================

PROMPT drop service tables

declare 
	v_count number;
begin
	select count(1) into v_count from user_tables where table_name='BLOB_DATA_TMP';
	dbms_output.put_line('Create table BLOB_DATA_TMP...');
	if v_count > 0 then 
		execute immediate 'drop table BLOB_DATA_TMP';
		dbms_output.put_line('Table BLOB_DATA_TMP was dropped.');	
	end if;	
	
	select count(1) into v_count from user_tables where table_name='DECLARATION_TEMPLATE_TMP';
	dbms_output.put_line('Create table DECLARATION_TEMPLATE_TMP...');
	if v_count > 0 then 
		execute immediate 'drop table DECLARATION_TEMPLATE_TMP';
		dbms_output.put_line('Table DECLARATION_TEMPLATE_TMP was dropped.');	
	end if;	
	
	select count(1) into v_count from user_tables where table_name='DECL_TEMPLATE_EVENT_SCRIPT_TMP';
	dbms_output.put_line('Create table DECL_TEMPLATE_EVENT_SCRIPT_TMP...');
	if v_count > 0 then 
		execute immediate 'drop table DECL_TEMPLATE_EVENT_SCRIPT_TMP';
		dbms_output.put_line('Table DECL_TEMPLATE_EVENT_SCRIPT_TMP was dropped.');	
	end if;	
exception when others then
   dbms_output.put_line(sqlerrm);
   raise_application_error(-20999,'Error drop service tables');   
end;
/


spool off;

exit;