set serveroutput on;

prompt create tables
@@01_01_create_tables.sql;

prompt add constraints
@@01_02_add_constraints.sql;

exit;
