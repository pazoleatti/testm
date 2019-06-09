set serveroutput on;

prompt replace person_pkg
@@scripts/person_pkg7.sql;

prompt replace person_pkg_body
@@scripts/person_pkg_body14.sql;

commit;
exit;
