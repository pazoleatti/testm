delete from ref_book_attribute where ref_book_id in (select id from ref_book where table_name in ('REF_BOOK_FOND','REF_BOOK_FOND_DETAIL'));
delete from ref_book where table_name in ('REF_BOOK_FOND','REF_BOOK_FOND_DETAIL');

--SBRFNDFL-2142
update ref_book set is_versioned=0 where table_name='REPORT_PERIOD_TYPE';