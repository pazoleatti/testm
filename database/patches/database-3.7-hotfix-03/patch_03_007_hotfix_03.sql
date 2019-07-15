set feedback off;
set verify off;
set serveroutput on;
spool &2

var v_exists_user number;
var v_cnt number;
var start_time varchar2(30);
var version varchar2(10);
var STATUS VARCHAR2(3000);
var USER_NDFL VARCHAR2(50);
.
PROMPT Install patch DB
PROMPT ======================

WHENEVER SQLERROR EXIT SQL.SQLCODE

begin  
   :USER_NDFL := upper('&1');
   select to_char(systimestamp,'yyyy.mm.dd hh24:mi:ss:FF3') into :start_time from dual;	
   :version := '03.007.fx3';
   :STATUS := 'OK';   
end;
/
 
begin 
	select count(1) into :v_cnt from version_history where status='OK' and version='03.007.00';
	if :v_cnt = 0 then
		dbms_output.put_line('Error: 03.007.00 not installed. Check log-files.');
		raise_application_error(-20999,'Error: 03.007.00 not installed. Check log-files.');
	end if;

  	select count(1) into :v_cnt from version_history where status='OK' and version='03.007.fx.03';
	if :v_cnt > 0 then
		dbms_output.put_line('Hotfix 03.007.hotfix.03 already installed.');
	end if; 
end;
/  


PROMPT ## Beginning Installing Hotfix


PROMPT ## 03_update_dml.sql
@03_update_dml.sql

insert into version_history (version, scrname, status, start_time, end_time) values (:version, 'patch_03_007_hotfix_03.sql', :status, to_timestamp(:start_time,'yyyy.mm.dd hh24:mi:ss:FF3'), systimestamp);
commit;

PROMPT Installation complete
PROMPT ======================

spool off;

exit;



