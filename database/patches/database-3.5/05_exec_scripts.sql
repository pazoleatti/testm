set serveroutput on;
spool &1
prompt update-ndfl_person_income
@@scripts/update-ndfl_person_income.sql;
spool &2
prompt merge_ref_book_oktmo
@@scripts/merge_ref_book_oktmo.sql;

exit;
