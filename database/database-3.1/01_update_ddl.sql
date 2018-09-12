set serveroutput on;

--alter tables
prompt alter tables
@@01_01_alter_tables.sql;

--drop sequences
prompt drop sequences
@@01_02_drop_sequences.sql;

--create tables
prompt create tables
@@01_03_create_tables.sql;

exit;
