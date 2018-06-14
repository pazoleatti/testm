set serveroutput on;

prompt alter tables
@@alter_tables.sql;

prompt create procedure DeleteRefBookPerson
@@deleterefbookperson.sql;


exit;
