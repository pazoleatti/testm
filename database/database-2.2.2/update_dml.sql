set serveroutput on;

prompt fill ref_book
@@ref_book.sql;

prompt fill async_task_type
@@async_task_type.sql;

prompt fill event
@@event.sql;

commit;
exit;
