-- alter tables
prompt alter table notification.id
alter table notification modify id number(18);

--create tables
prompt create tables
@@create_tables.sql;

--create views
prompt create views
@@create_views.sql;

--create constraints
prompt create constraints
@@create_constraints.sql;

--alter tables
prompt alter tables
@@alter_tables.sql;

--drop tables
prompt drop tables
@@drop_tables.sql;

--create sequences
prompt create sequences
@@create_sequences.sql;

-- compile source
prompt compile DEP_REP_PER_BEFORE_DELETE
@@DEP_REP_PER_BEFORE_DELETE.sql;

-- compile source
prompt compile DEP_REP_PER_BEFORE_INS_UPD
@@DEP_REP_PER_BEFORE_INS_UPD.sql;

-- compile source
prompt compile person_pkg
@@person_pkg.sql;

exit;
