set feedback off;
set verify off;
set serveroutput on;
spool &2

var v_exists_user number;
var v_cnt number;
var start_time varchar2(30);
var version varchar2(20);
var STATUS VARCHAR2(3000);
var USER_NDFL VARCHAR2(50);

PROMPT Install patch DB
PROMPT ======================

WHENEVER SQLERROR EXIT SQL.SQLCODE

begin  
   :USER_NDFL := upper('&1');
   select to_char(systimestamp,'yyyy.mm.dd hh24:mi:ss:FF3') into :start_time from dual;	
   :version := '03.009.02.03.fx2';
   :STATUS := 'OK';   
end;
/
 
PROMPT ## Beginning Installing Hotfix

PROMPT ## 04_package
@04_package.sql

PROMPT ##Statistics
--@tech/gather_statistics.sql &1

insert into version_history (version, scrname, status, start_time, end_time) values (:version, 'patch_03_009_02_03_hotfix_02.sql', :status, to_timestamp(:start_time,'yyyy.mm.dd hh24:mi:ss:FF3'), systimestamp);
commit;

PROMPT Installation complete
PROMPT ======================

spool off;

exit;



