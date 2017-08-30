-- alter ref_book_person.old_id and update attributes
prompt alter ref_book_person.old_id and update attributes
@@alter_person_old_id.sql;
commit;

-- alter ref_book_calendar  
prompt alter ref_book_calendar
@@alter_ref_book_calendar.sql;
commit;

prompt recreate indexes on fias_addrobj
@@recreate_fias_idxs.sql;

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
