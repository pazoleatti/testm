set feedback off;
set verify off;
set serveroutput on;
spool &1;

WHENEVER SQLERROR EXIT SQL.SQLCODE

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

PROMPT Fill tables
PROMPT ======================

PROMPT Fill service tables...
HOST "&2\sqlldr" &3 control=templates/ldr/refbook/blob_data.ldr log=&4/02_01_refbook_blob_data.txt bad=&5/02_01_refbook_blob_data.bad

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
@02_01_fill_templates.sql

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
exception when others then
   dbms_output.put_line(sqlerrm);
   raise_application_error(-20999,'Error drop service tables');   
end;
/


spool off;

exit;