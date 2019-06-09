set serveroutput on;

prompt fill ref_book
@@ref_book.sql;

prompt update declaration_type
@@declaration_type.sql;

prompt update report_period_type
@@report_period_type.sql;

prompt update declaration_template
@@declaration_template.sql;

prompt fill async_task_type
@@async_task_type.sql;

prompt fill configuration
@@configuration.sql;

prompt fill event
@@event.sql;

commit;
exit;
