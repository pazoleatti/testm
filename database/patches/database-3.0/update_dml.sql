set serveroutput on;

prompt configuration
@@configuration.sql;

prompt ref_book
@@ref_book.sql;

prompt update blob_data
@@blob_data.sql;

prompt fill async_task_type
@@async_task_type.sql;

prompt fill event
@@event.sql;


commit;
exit;
