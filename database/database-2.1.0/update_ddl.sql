-- alter tables
prompt alter table notification.id
alter table notification modify id number(18);

--create synonym
prompt create synonyms
@@create_synonyms.sql;

--create tables
prompt create tables
@@create_tables.sql;

--create views
prompt create views
@@create_views.sql;

--alter tables
prompt alter tables
@@alter_tables.sql;

--create constraints
prompt create constraints
@@create_constraints.sql;

--drop tables
prompt drop tables
@@drop_tables.sql;

--create sequences
prompt create sequences
@@create_sequences.sql;

--create indexes
prompt create indexes
@@create_indexes.sql;

--alter jobs
prompt alter jobs
@@alter_jobs.sql;

--alter materialized views
prompt alter materialized views
@@alter_mat_views.sql;

-- compile source
prompt compile DEP_REP_PER_BEFORE_DELETE
@@DEP_REP_PER_BEFORE_DELETE.sql;

-- compile source
prompt compile DEP_REP_PER_BEFORE_INS_UPD
@@DEP_REP_PER_BEFORE_INS_UPD.sql;

-- compile source
prompt compile person_pkg
@@person_pkg.sql;

-- compile source
prompt compile fias_pkg
@@fias_pkg.sql;

prompt async_task
@@async_task.sql;
exit;
