
GRANT CREATE SESSION TO &1;
GRANT CREATE TABLE TO &1;
GRANT CREATE PROCEDURE TO &1;
GRANT CREATE VIEW TO &1;
GRANT CREATE SEQUENCE TO &1;
GRANT CREATE SYNONYM TO &1;
GRANT CREATE TRIGGER TO &1;
GRANT CREATE MATERIALIZED VIEW TO &1;
GRANT CREATE JOB TO &1;

GRANT select on &3..subsystem_role to &1;
GRANT select on &3..vw_log_table_change to &1;
GRANT select on &3..department to &1;
GRANT references on &3..department to &1;
GRANT select on &3..sec_user to &1;
GRANT references on &3..sec_user to &1;
GRANT select on &3..sec_role to &1;
GRANT references on &3..sec_role to &1;
GRANT select on &3..sec_user_role to &1;
GRANT references on &3..sec_user_role to &1;
GRANT EXECUTE ON &3..ADD_LOG_SYSTEM_NDFL to &1;


GRANT select on &2..subsystem_role to &1;
GRANT select on &2..vw_log_table_change to &1;
GRANT select on &2..department to &1;
GRANT references on &2..department to &1;
GRANT select on &2..sec_user to &1;
GRANT references on &2..sec_user to &1;
GRANT select on &2..sec_role to &1;
GRANT references on &2..sec_role to &1;
GRANT select on &2..sec_user_role to &1;
GRANT references on &2..sec_user_role to &1;
GRANT EXECUTE ON &2..ADD_LOG_SYSTEM_NDFL to &1;

exit;