-- alter ref_book_address and update attributes
prompt alter ref_book_address and update attributes
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
@@report_pkg.sql;
@@vw_decl_kpp_oktmo_form7.sql;

-- comments on views
prompt comments on views
@@comments_on_views.sql;

prompt comments on mat. views
@@comments_on_mat_views.sql;

exit;
