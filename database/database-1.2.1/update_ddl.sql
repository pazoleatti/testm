-- create procedure RefreshFiasViews
prompt create RefreshFiasViews
@@RefreshFiasViews.sql;

-- create job
prompt create job
@@create_job.sql;

-- compile fias_pkg
prompt compile fias_pkg
@@fias_pkg.sql;

commit;
exit;
