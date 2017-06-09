-- indexes
prompt alter ref_book_address & update attributes
@@alter_ref_book_address.sql;
commit;

-- compile source
prompt compile source
@@person_pkg.sql;
@@fias_pkg.sql;

exit;
