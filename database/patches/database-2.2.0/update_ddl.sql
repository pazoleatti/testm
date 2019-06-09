set serveroutput on;

-- create tables
prompt create_tmp
@@create_tmp.sql;

--alter tables
prompt alter tables
@@alter_tables.sql;

--drop tables
prompt drop tables
@@drop_tables.sql;

--alter materialized views
prompt alter materialized views
@@alter_mat_views.sql;

prompt compile person_pkg
@@person_pkg.sql;

prompt compile fias_pkg
@@fias_pkg.sql;

exit;
