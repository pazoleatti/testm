insert into blob_data(id, name, data, creation_date, type, data_size) values ('24af57ef-ec1c-455f-a4fa-f0fb29483066', 'Скрипт', '', sysdate, 1, 0)

insert into ref_book(id, name) values
(1, 'Книга');
insert into ref_book(id, name) values
(2, 'Человек');
insert into ref_book(id, name, script_id, visible) values
(3, 'Библиотека', '24af57ef-ec1c-455f-a4fa-f0fb29483066', 0);

insert into ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width) values
  (4, 2, 1, 'ФИО', 'name', 1, null, null, 1, null, 10);
insert into ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width) values
  (1, 1, 1, 'Наименование', 'name', 1, null, null, 1, null, 10);
insert into ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width) values
  (2, 1, 2, 'Количество страниц', 'order', 2, null, null, 1, 0, 10);
insert into ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width) values
  (3, 1, 4, 'Автор', 'author', 4, 2, 4, 1, null, 10);
insert into ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width) values
  (5, 1, 5, 'Вес', 'weight', 2, null, null, 1, 3, 10);

insert into ref_book_record(id, record_id, ref_book_id, version, status) values
  (1, 1, 1, to_date('01.01.2013', 'DD.MM.YY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
  (2, 1, 1, to_date('01.02.2013', 'DD.MM.YY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
  (3, 1, 1, to_date('01.03.2013', 'DD.MM.YY'), -1);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
  (4, 2, 1, to_date('01.01.2013', 'DD.MM.YY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
  (5, 1, 2, to_date('01.01.2013', 'DD.MM.YY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
  (6, 2, 2, to_date('01.01.2013', 'DD.MM.YY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
  (7, 2, 2, to_date('01.04.2013', 'DD.MM.YY'), 0);

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
  (5, 4, 'Иванов И.И.', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (6, 4, 'Петров П.П.', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (7, 4, 'Петренко П.П.', null, null, null);