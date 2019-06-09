set feedback off;
set verify off;
set serveroutput on;
spool &1;

WHENEVER SQLERROR EXIT SQL.SQLCODE

PROMPT Create service tables...

declare 
	v_count number;
begin
	select count(1) into v_count from user_tables where table_name='TMP_DEPART';
	dbms_output.put_line('Create table TMP_DEPART...');
	if v_count = 1 then 
		execute immediate 'drop table tmp_depart';
	end if;
	execute immediate 'create table tmp_depart(name varchar2(510 char),dep_id number(9))';
	dbms_output.put_line('Table TMP_DEPART created.');	
exception when others then
   dbms_output.put_line(sqlerrm);
   raise_application_error(-20999,'Error create table TMP_DEPART.');   
end;
/

declare 
	v_count number;
begin
	select count(1) into v_count from user_tables where table_name='TMP_DEP_PARAMS';
	dbms_output.put_line('Create table TMP_DEP_PARAMS...');
	if v_count = 0 then 
		execute immediate 'create table tmp_dep_params
							(
							row_num integer,
							titname varchar2(510 char),
							kpp varchar2(100 char),
							oktmo varchar2(100 char),
							tax_end varchar2(100 char),
							start_date varchar2(10 char),
							end_date varchar2(10 char),
							place varchar2(100 char),
							phone varchar2(100 char),
							sign varchar2(100 char),
							surname varchar2(100 char),
							name varchar2(100 char),
							lastname varchar2(100 char),
							docname varchar2(100 char),
							record_id number(9)
							)
 ';
		dbms_output.put_line('Table TMP_DEP_PARAMS created.');	
	else
		dbms_output.put_line('Table TMP_DEP_PARAMS already exists.');
		execute immediate 'delete from TMP_DEP_PARAMS';
		commit;
		dbms_output.put_line('Table TMP_DEP_PARAMS cleared.');
	end if;	
exception when others then
   dbms_output.put_line(sqlerrm);
   raise_application_error(-20999,'Error create table TMP_DEP_PARAMS.');   
end;
/


PROMPT Fill tables
PROMPT ======================
@00_01_fill_tmp_departs.sql

PROMPT Fill service tables...
HOST "&2\sqlldr" &3 control=00_02_tmp_dep_params.ctl log=&4/tmp_dep_params.txt bad=&5/tmp_dep_params.bad

PROMPT Fill tables...
@00_03_fill_ref_dep_params.sql

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
	select count(1) into v_count from user_tables where table_name='TMP_DEPART';
	dbms_output.put_line('Drop table TMP_DEPART...');
	if v_count > 0 then 
		execute immediate 'drop table TMP_DEPART';
		dbms_output.put_line('Table TMP_DEPART was dropped.');	
	end if;	
	
	select count(1) into v_count from user_tables where table_name='TMP_DEP_PARAMS';
	dbms_output.put_line('Drop table TMP_DEP_PARAMS...');
	if v_count > 0 then 
		execute immediate 'drop table TMP_DEP_PARAMS';
		dbms_output.put_line('Table TMP_DEP_PARAMS was dropped.');	
	end if;	

exception when others then
   dbms_output.put_line(sqlerrm);
   raise_application_error(-20999,'Error drop service tables');   
end;
/

spool off;

PROMPT Checks
@00_04_checks_load_ref_book_ndfl.sql 

exit;