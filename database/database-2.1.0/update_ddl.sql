-- alter tables
prompt alter table notification.id
alter table notification modify id number(18);

--drop tables
prompt drop tmp tables
@@drop_tables.sql;

-- compile source
prompt compile DEP_REP_PER_BEFORE_DELETE
@@DEP_REP_PER_BEFORE_DELETE.sql;

-- compile source
prompt compile person_pkg
@@person_pkg.sql;

exit;
