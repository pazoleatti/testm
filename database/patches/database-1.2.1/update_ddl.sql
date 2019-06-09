-- create procedure DropAndCreateFiasViews
prompt create DropAndCreateFiasViews
@@DropAndCreateFiasViews.sql;

-- drop index
prompt drop index
@@drop_index.sql;

-- drop constraints
prompt drop constraints
@@drop_constraints.sql;

-- create job
prompt create job
@@create_job.sql;

-- compile fias_pkg
prompt compile fias_pkg
@@fias_pkg.sql;

commit;
exit;
