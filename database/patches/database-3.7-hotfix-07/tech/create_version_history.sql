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

PROMPT Install DB
PROMPT ======================

WHENEVER SQLERROR EXIT SQL.SQLCODE

begin  
   :USER_NDFL := upper('&1');
   select to_char(systimestamp,'yyyy.mm.dd hh24:mi:ss:FF3') into :start_time from dual;	
   :version := '01.000.00';
   :STATUS := 'OK';
   /* select count(1) into :v_exists_user from all_users where username = :USER_NDFL;
   if :v_exists_user <> 1 then
		raise_application_error(-20999,'User '||'&1'||' not exists.');
   else execute immediate 'ALTER SESSION SET CURRENT_SCHEMA = &1';
   end if;   */
   
end;
/

PROMPT Create service tables...

begin
	select count(1) into :v_cnt from all_tables where table_name='VERSION_HISTORY' and OWNER = :USER_NDFL;
	dbms_output.put_line('Create table VERSION_HISTORY...');
	if :v_cnt = 0 then execute immediate 
		'CREATE TABLE VERSION_HISTORY ( VERSION VARCHAR2(255),
										SCRNAME VARCHAR2(50 CHAR),
										STATUS VARCHAR2(4000),					
										START_TIME TIMESTAMP,
										END_TIME TIMESTAMP)';
		dbms_output.put_line('Table VERSION_HISTORY created.');	
	else
		dbms_output.put_line('Table VERSION_HISTORY already exists.');
	end if;	
exception when others then
	:status := sqlerrm;
	dbms_output.put_line(sqlerrm);
	raise_application_error(-20999,'Error create table VERSION_HISTORY.');	
end;
/


spool off;

exit;