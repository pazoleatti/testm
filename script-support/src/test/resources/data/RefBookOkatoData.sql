INSERT INTO ref_book (id, name) VALUES (3, 'Коды ОКАТО');

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord,reference_id, attribute_id, visible, precision, width, max_length) VALUES
(7, 3, 'Код ОКАТО', 'OKATO', 1, 2, NULL, NULL, 1, NULL, 11, 100);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, max_length) VALUES
(8, 3, 'Наименование муниципального образования', 'NAME', 1, 3, NULL, NULL, 1, NULL, 510, 100);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width) VALUES
(6, 3, 'Идентификатор родительской записи', 'PARENT_ID', 4, 1, 3, 8, 1, NULL, 510);

INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES 
(1, 1, 3, to_date('01.01.2013', 'DD.MM.YYYY'), 0);
INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES 
(2, 2, 3, to_date('01.01.2013', 'DD.MM.YYYY'), 0);
INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES 
(3, 3, 3, to_date('01.01.2013', 'DD.MM.YYYY'), 0);

INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES 
(1, 7, '57401365000', NULL, NULL, NULL);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES 
(1, 8, 'Дзержинский', NULL, NULL, NULL);

INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES 
(2, 7, '57401000000', NULL, NULL, NULL);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES 
(2, 8, 'Пермь', NULL, NULL, NULL);

INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES 
(3, 7, '57000000000', NULL, NULL, NULL);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES 
(3, 8, 'Пермский край', NULL, NULL, NULL);