set feedback off;
set verify off;
set serveroutput on;
spool &2

var v_exists_user number;
var v_cnt number;
var start_time varchar2(30);
var version varchar2(50);
var STATUS VARCHAR2(3000);
var USER_NDFL VARCHAR2(50);
var NSI_USER VARCHAR2(50);
var TAXREC_USER VARCHAR2(50);

PROMPT Install patch DB
PROMPT ======================

WHENEVER SQLERROR EXIT SQL.SQLCODE

begin  
   :USER_NDFL := upper('&1');
   :NSI_USER := upper('&3');
   :TAXREC_USER := upper('&6');	
   select to_char(systimestamp,'yyyy.mm.dd hh24:mi:ss:FF3') into :start_time from dual;	
   :version := '03.010.00.01';
   :STATUS := 'OK';   
end;
/
 
begin 

	select count(1) into :v_cnt from version_history where status='OK' and version='03.010.00';
	if :v_cnt = 0 then
		dbms_output.put_line('Error: 03.010.00 not installed. Check log-files.');
		raise_application_error(-20999,'Error: 03.010.00 not installed. Check log-files.');
	end if;

  	select count(1) into :v_cnt from version_history where status='OK' and version='03.010.00.01';
	if :v_cnt > 0 then
		dbms_output.put_line('Version 03.010.00.01 already installed.');
		raise_application_error(-20999,'Version 03.010.00.01 already installed.');
	end if; 
end;
/  


PROMPT ## Beginning Installing Patch


PROMPT ## 01_ddl_alter_tables
@database-3.10.0.1/01_01_alter_tables.sql 

PROMPT ## 02_templates
@database-3.10.0.1/02_templates.sql "_log/3.10_03_templates.txt" "&4" "&5" "../_log" "../_bad"

PROMPT ## 03_dml_update
@database-3.10.0.1/03_update_dml.sql 

PROMPT ## 04_package
@database-3.10.0.1/04_package.sql

PROMPT ## 04_ddl_views
@database-3.10.0.1/04_ddl_views.sql &1 &6

PROMPT ## 05_indexes_triggers
@database-3.10.0.1/05_indexes_triggers.sql
 
PROMPT ## 06_check_index_constraints
@database-3.10.0.1/06_check_index_constraints.sql

PROMPT ## get statistics
@database-3.10.0.1/08_get_statistics.sql

insert into version_history (version, scrname, status, start_time, end_time) values (:version, 'patch_03_010_00_01.sql', :status, to_timestamp(:start_time,'yyyy.mm.dd hh24:mi:ss:FF3'), systimestamp);
commit;

PROMPT Installation complete
PROMPT ======================

spool off;

exit;



