set feedback off;
set verify off;
set serveroutput on;
spool &2

var v_exists_user number;
var v_cnt number;
var start_time varchar2(30);
var version varchar2(15);
var STATUS VARCHAR2(3000);
var USER_NDFL VARCHAR2(50);

PROMPT Install patch DB
PROMPT ======================

WHENEVER SQLERROR EXIT SQL.SQLCODE

begin  
   :USER_NDFL := upper('&1');
   select to_char(systimestamp,'yyyy.mm.dd hh24:mi:ss:FF3') into :start_time from dual;	
   :version := '03.010.00.fx1';
   :STATUS := 'OK';   
end;
/
 
begin 
	select count(1) into :v_cnt from version_history where status='OK' and version='03.010.00';
	if :v_cnt = 0 then
		dbms_output.put_line('Error: 03.010.00 not installed. Check log-files.');
		raise_application_error(-20999,'Error: 03.010.00 not installed. Check log-files.');
	end if;

  	select count(1) into :v_cnt from version_history where status='OK' and version='03.010.00.fx.01';
	if :v_cnt > 0 then
		dbms_output.put_line('Hotfix 03.010.00.hotfix.01 already installed.');
	end if; 
end;
/  


PROMPT ## Beginning Installing Hotfix

PROMPT ## 02_templates
@02_templates.sql "_log/3.9.2_hotfix_01_templates.txt" "&3" "&4" "_log" "_bad"

insert into version_history (version, scrname, status, start_time, end_time) values (:version, 'patch_03_010_00_hotfix_01.sql', :status, to_timestamp(:start_time,'yyyy.mm.dd hh24:mi:ss:FF3'), systimestamp);
commit;

PROMPT Installation complete
PROMPT ======================

spool off;

exit;



