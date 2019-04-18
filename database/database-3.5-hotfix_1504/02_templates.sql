set feedback off;
set verify off;
set serveroutput on;
spool &1;

WHENEVER SQLERROR EXIT SQL.SQLCODE

PROMPT Create service tables...

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

PROMPT Fill tables
PROMPT ======================

PROMPT Fill service tables...
HOST "&2\sqlldr" &3 control=templates/ldr/ndfl/template.ldr log=&4/02_02_ndfl_template.txt bad=&5/02_02_ndfl_template.bad

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
  select count(1) into v_count from user_tables where table_name='DECLARATION_TEMPLATE_TMP';
	dbms_output.put_line('drop table DECLARATION_TEMPLATE_TMP...');
	if v_count > 0 then
		execute immediate 'drop table DECLARATION_TEMPLATE_TMP';
		dbms_output.put_line('Table DECLARATION_TEMPLATE_TMP was dropped.');
	end if;
exception when others then
   dbms_output.put_line(sqlerrm);
   raise_application_error(-20999,'Error drop service tables');   
end;
/


spool off;

exit;