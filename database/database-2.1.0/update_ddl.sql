-- alter tables
prompt alter table notification.id
alter table notification modify id number(18);

--drop tables
prompt drop tmp tables
@@drop_tables.sql;

-- compile source
prompt compile source
@@person_pkg.sql;

exit;
