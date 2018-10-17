set feedback off;
set verify off;
set serveroutput on;
spool &1;

WHENEVER SQLERROR EXIT SQL.SQLCODE

PROMPT Create service tables...

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
HOST "&2\sqlldr" &3 control=templates/ldr/ndfl/template_script.ldr log=&4/02_ndfl_template_script.txt bad=&5/02_ndfl_template_script.bad

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


spool off;

exit;