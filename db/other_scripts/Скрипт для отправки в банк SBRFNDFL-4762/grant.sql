GRANT select on subsystem_role to &1;
GRANT select on vw_log_table_change to &1;
GRANT select on department to &1;
GRANT references on department to &1;
GRANT select on sec_user to &1;
GRANT references on sec_user to &1;
GRANT select on sec_role to &1;
GRANT references on sec_role to &1;
GRANT select on sec_user_role to &1;
GRANT references on sec_user_role to &1;
GRANT EXECUTE ON ADD_LOG_SYSTEM_NDFL to &1;

alter view vw_log_table_change compile;

exit;
