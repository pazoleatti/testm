set serveroutput on;

spool &1
prompt check_index
@@04_01_check_index.sql;

spool &2
prompt check_constraints
@@04_02_check_constraints.sql;

exit;
