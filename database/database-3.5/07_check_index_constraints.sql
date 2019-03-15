set serveroutput on;

spool &1
prompt check_index
@@07_01_check_index.sql;

spool &2
prompt check_constraints
@@07_02_check_constraints.sql;

exit;
