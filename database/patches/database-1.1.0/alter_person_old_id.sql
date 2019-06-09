alter table ref_book_person modify old_id number(18);
update ref_book_attribute set max_length=18 where id=9063;
commit;
