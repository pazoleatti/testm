set serveroutput on;

prompt create_tables
@@00_01_create_tables.sql;

prompt check_conversion
@@00_02_check_conversion.sql;

exit;
