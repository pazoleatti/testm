alter table ref_book_address add (address_full varchar2(255 char));

comment on column ref_book_address.address_full is 'Полный адрес';

insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values( 9122, 901, 'Адрес', 'ADDRESS_FULL', 1, 13, null, null, 0, null, 15, 0, 0, null, null, 1, 255);
update ref_book_attribute set attribute_id = 9122 where id = 9054;
