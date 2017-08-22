-- alter tables
prompt alter table notification.id
alter table notification modify id number(18);


-- indexes
prompt add indexes
@@indexes.sql;

-- compile source
prompt compile source
@@person_pkg.sql;
@@fias_pkg.sql;

exit;
