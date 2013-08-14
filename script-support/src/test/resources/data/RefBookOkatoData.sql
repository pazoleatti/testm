INSERT INTO REF_BOOK (ID, NAME) VALUES (3, 'Коды ОКАТО');

INSERT INTO REF_BOOK_ATTRIBUTE (ID, REF_BOOK_ID, NAME, ALIAS, TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION, WIDTH) VALUES (5, 3, 'Идентификатор записи', 'ID', 2, 0, null, null, 1, 0, 9);
INSERT INTO REF_BOOK_ATTRIBUTE (ID, REF_BOOK_ID, NAME, ALIAS, TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION, WIDTH) VALUES (7, 3, 'Код ОКАТО', 'OKATO', 1, 2, null, null, 1, null, 11);
INSERT INTO REF_BOOK_ATTRIBUTE (ID, REF_BOOK_ID, NAME, ALIAS, TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION, WIDTH) VALUES (8, 3, 'Наименование муниципального образования', 'NAME', 1, 3, null, null, 1, null, 510);
INSERT INTO REF_BOOK_ATTRIBUTE (ID, REF_BOOK_ID, NAME, ALIAS, TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION, WIDTH) VALUES (6, 3, 'Идентификатор родительской записи', 'PARENT_ID', 4, 1, 3, 8, 1, null, 510);

insert into ref_book_record(id, record_id, ref_book_id, version, status) values (1, 1, 3, to_date('01.01.2013', 'DD.MM.YYYY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (2, 2, 3, to_date('01.01.2013', 'DD.MM.YYYY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (3, 3, 3, to_date('01.01.2013', 'DD.MM.YYYY'), 0);

insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values (1, 5, null, 1, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values (1, 7, '57401365000', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values (1, 8, 'Дзержинский', null, null, null);

insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values (2, 5, null, 2, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values (2, 7, '57401000000', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values (2, 8, 'Пермь', null, null, null);

insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values (3, 5, null, 3, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values (3, 7, '57000000000', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values (3, 8, 'Пермский край', null, null, null);