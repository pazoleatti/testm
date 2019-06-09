set serveroutput on;

prompt replace person_pkg
@@scripts/person_pkg6.sql;

prompt replace person_pkg_body
@@scripts/person_pkg_body8.sql;

commit;
exit;
