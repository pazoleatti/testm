insert into blob_data(id, name, data, creation_date, type, data_size) values ('24af57ef-ec1c-455f-a4fa-f0fb29483066', 'Скрипт', '', sysdate, 1, 0)

insert into ref_book(id, name) values
(1, 'Книга');
insert into ref_book(id, name) values
(2, 'Человек');
insert into ref_book(id, name, script_id, visible) values
(3, 'Библиотека', '24af57ef-ec1c-455f-a4fa-f0fb29483066', 0);
insert into ref_book (id, name, type, visible, read_only) values
(4, 'Коды ОКАТО', 1, 1, 0);

insert into ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, is_unique) values
  (4, 2, 1, 'ФИО', 'name', 1, null, null, 1, null, 10, 0);
insert into ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, is_unique) values
  (1, 1, 1, 'Наименование', 'name', 1, null, null, 1, null, 10, 1);
insert into ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, is_unique) values
  (2, 1, 2, 'Количество страниц', 'order', 2, null, null, 1, 0, 10, 0);
insert into ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, is_unique) values
  (3, 1, 4, 'Автор', 'author', 4, 2, 4, 1, null, 10, 0);
insert into ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, is_unique) values
  (5, 1, 5, 'Вес', 'weight', 2, null, null, 1, 3, 10, 0);
insert into ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, is_unique) values
  (9, 1, 6, 'Ничего', 'null', 1, null, null, 1, null, 10, 0);
--ОКАТО
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique) values
  (8, 4, 'Наименование муниципального образования', 'NAME', 1, 3, null, null, 1, null, 510, 1, 0);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique) values
  (6, 4, 'Идентификатор родительской записи', 'PARENT_ID', 4, 1, 4, 8, 1, null, 510, 0, 0);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique) values
  (7, 4, 'Код ОКАТО', 'OKATO', 1, 2, null, null, 1, null, 11, 1, 1);

insert into ref_book_record(id, record_id, ref_book_id, version, status) values
  (1, 1, 1, date '2013-01-01', 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
  (2, 1, 1, date '2013-02-01', 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
  (3, 1, 1, date '2013-03-01', 2);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
  (4, 2, 1, date '2013-01-01', 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
  (5, 1, 2, date '2013-01-01', 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
  (6, 2, 2, date '2013-01-01', 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
  (7, 2, 2, date '2013-04-01', 0);
--ОКАТО (для проверки иерархии)
--иерархия 8(9,10[11]),12(13,14,15),16,17
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(8, 1, 4, date '2013-01-01', 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(9, 2, 4, date '2013-01-01', 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(10, 3, 4, date '2013-01-01', 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(11, 4, 4, date '2013-01-01', 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(12, 5, 4, date '2013-01-01', 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(13, 6, 4, date '2012-01-01', 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(14, 7, 4, date '2012-01-01', 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(15, 8, 4, date '2012-01-01', 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(16, 9, 4, date '2013-01-01', 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(17, 10, 4, date '2013-01-01', 0);

insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (1, 1, 'Алиса в стране чудес', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (1, 2, null, 1113, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (1, 3, null, null, null, 5);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (1, 5, null, 0.25, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (2, 1, 'Алиса в стране', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (2, 2, null, 1213, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (2, 3, null, null, null, 7);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (2, 5, null, 0.1, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (4, 1, 'Вий', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (4, 2, null, 425, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (4, 3, null, null, null, 6);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (4, 5, null, 2.399, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (6, 1, 'Алиса в стране чудес', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (6, 2, null, 1113, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (6, 3, null, null, null, 5);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (6, 5, null, 0.25, null, null);

insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (5, 4, 'Иванов И.И.', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (6, 4, 'Петров П.П.', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (7, 4, 'Петренко П.П.', null, null, null);
-- ОКАТО
--иерархия 8(9,10[11]),12(13,14,15),16,17
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (8, 8, 'Уфа', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (8, 7, '02010000', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (9, 8, 'Нефтекамск', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (9, 7, '02010100', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (9, 6, null, null, null, 8);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (10, 8, 'Стерлитамак', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (10, 7, '02010200', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (10, 6, null, null, null, 8);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (11, 8, 'Салават', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (11, 7, '02010201', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (11, 6, null, null, null, 10);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (12, 8, 'Казань', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (12, 7, '03010000', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (13, 8, 'Набережные Челны', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (13, 7, '03010100', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (13, 6, null, null, null, 12);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (14, 8, 'Нижнекамск', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (14, 7, '03010200', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (14, 6, null, null, null, 12);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (15, 8, 'Актаныш', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (15, 7, '03010300', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (15, 6, null, null, null, 12);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (16, 8, 'Челябинск', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (16, 7, '04000000', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (17, 8, 'Самара', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (17, 7, '05000000', null, null, null);