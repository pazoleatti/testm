set serveroutput on;

prompt insert into ref_book
@@03_01_insert_ref_book.sql;

prompt insert into ref_book_attribute
@@03_02_insert_ref_book_attribute.sql;

commit;
exit;
