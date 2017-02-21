prompt create tmp tables
@@create_tmp.sql;
prompt fill tmp_departs
@@fill_tmp_departs.sql;
prompt fill tmp_dep_params
@@fill_tmp_dep_params.sql;
prompt fill ref_book_ndfl...
@@fill_ref_dep_params.sql;
--prompt drop tmp tables
--@@drop_tmp.sql;

commit;
exit;
