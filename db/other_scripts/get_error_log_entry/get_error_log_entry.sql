set long 32767
set pagesize 0
set linesize 4000
set feedback off
set echo off
set verify off
set trims on
set heading off
--set termout off
set serveroutput on size 1000000;
spool &1;

select * from log_entry where creation_date between to_date ('20.08.2019 00:00:00','dd.mm.yyyy hh24:mi:ss') and to_date ('20.08.2019 23:59:59','dd.mm.yyyy hh24:mi:ss')
and log_level=2;

exit;
