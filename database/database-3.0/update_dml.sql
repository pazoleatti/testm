set serveroutput on;

prompt configuration
@@configuration.sql;

prompt ref_book
@@ref_book.sql;

prompt update blob_data
@@blob_data.sql;


commit;
exit;
