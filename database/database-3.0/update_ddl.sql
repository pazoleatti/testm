set serveroutput on;

-- create tables
prompt create_tmp
@@create_tmp.sql;

prompt alter tables
@@alter_tables.sql;

prompt create procedure DeleteRefBookPerson
@@deleterefbookperson.sql;


exit;
