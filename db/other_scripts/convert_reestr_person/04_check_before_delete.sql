set serveroutput on;

prompt check_before_delete_from ref_book_person
@@04_01_check_delete_rb_person.sql;

prompt check_before_delete_from ref_book_id_doc
@@04_02_check_delete_rb_id_doc.sql;

commit;
exit;
