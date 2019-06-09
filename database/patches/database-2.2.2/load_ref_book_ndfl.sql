set feedback off;
set verify off;
set serveroutput on;
spool &1;

WHENEVER SQLERROR EXIT SQL.SQLCODE

PROMPT Fill ref_book_oktmo
PROMPT ======================
@fill_ref_book_oktmo.sql

PROMPT alter table REF_BOOK_NDFL_DETAIL...
declare
	v_count number;
begin
	select count(1) into v_count from user_tab_columns where table_name='REF_BOOK_NDFL_DETAIL' and column_name='ROW_ORD' and data_type='NUMBER' and data_precision<9;
	if v_count>0 then
		dbms_output.put_line('alter table REF_BOOK_NDFL_DETAIL');
		execute	immediate 'alter table ref_book_ndfl_detail modify row_ord number(9)';
		select count(1) into v_count from user_tab_columns where table_name='REF_BOOK_NDFL_DETAIL' and column_name='ROW_ORD' and data_type='NUMBER' and data_precision=9;
		if v_count>0 then
			dbms_output.put_line('Table REF_BOOK_NDFL_DETAIL altered');
		end if;
	end if;
end;
/

PROMPT Create service tables...

declare 
	v_count number;
begin
	select count(1) into v_count from user_tables where table_name='TMP_DEPART';
	dbms_output.put_line('Create table TMP_DEPART...');
	if v_count = 0 then execute immediate 
		'create table tmp_depart
(
  code varchar2(4 char),
  name varchar2(100 char),
  dep_id number
)
 ';
		dbms_output.put_line('Table TMP_DEPART created.');	
	else
		dbms_output.put_line('Table TMP_DEPART already exists.');
		execute immediate 'delete from TMP_DEPART';
		commit;
		dbms_output.put_line('Table TMP_DEPART cleared.');
	end if;	
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
	if v_count = 0 then execute immediate 
		'create table tmp_dep_params
(
  depcode varchar2(100 char),
  inn varchar2(100 char),
  tax_end varchar2(100 char),
  kpp varchar2(100 char),
  place varchar2(100 char),
  titname varchar2(255 char),
  oktmo varchar2(100 char),
  phone varchar2(100 char),
  sign varchar2(100 char),
  surname varchar2(100 char),
  name varchar2(100 char),
  lastname varchar2(100 char),
  docname varchar2(100 char),
  orgname varchar2(100 char),
  reorgcode varchar2(100 char),
  row_num number,
  tax_organ_code_mid varchar2(100 char),
  okved number(18),
  region number(18),
  obligation number(18),
  reorg_inn varchar2(12 char),
  reorg_kpp varchar2(9 char)
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
@fill_tmp_departs.sql

PROMPT Fill service tables...
HOST "&2\sqlldr" &3 control=tmp_dep_params.ctl log=&4/tmp_dep_params.txt bad=&5/tmp_dep_params.bad

PROMPT Fill tables...
@fill_ref_dep_params.sql

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
@checks.sql 

exit;