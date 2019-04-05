set serveroutput on;

spool &1
prompt check_index
@@08_01_check_index.sql;

spool &2
prompt check_constraints
@@08_02_check_constraints.sql;

exit;
