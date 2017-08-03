-- indexes
prompt alter ref_book_address & update attributes
@@alter_ref_book_address.sql;
commit;


-- changes fias_addrobj
prompt drop unuseable columns from fias_addrobj
@@fias_addrobj_drop_cols.sql

prompt recreate mat views
@@recreate_mat_views.sql;

-- compile source
prompt compile source
@@person_pkg.sql;
@@fias_pkg.sql;

exit;
