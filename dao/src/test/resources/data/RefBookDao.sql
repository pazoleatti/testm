insert into blob_data(id, name, data, creation_date) values ('24af57ef-ec1c-455f-a4fa-f0fb29483066', 'Скрипт', '', sysdate);

insert into ref_book(id, name) values
(1, 'Книга');
insert into ref_book(id, name) values
(2, 'Человек');
insert into ref_book(id, name, script_id, visible) values
(3, 'Библиотека', '24af57ef-ec1c-455f-a4fa-f0fb29483066', 0);
insert into ref_book (id, name, type, visible, read_only) values
(4, 'Коды ОКАТО', 1, 1, 0);
insert into ref_book (id, name) values (5, 'Участники ТЦО');
insert into ref_book (id, name) values (52, 'Бухотчетность');

insert into ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, is_unique, max_length) values
  (4, 2, 1, 'ФИО', 'name', 1, null, null, 1, null, 10, 0, 40);
insert into ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, is_unique, max_length) values
  (1, 1, 1, 'Наименование', 'name', 1, null, null, 1, null, 10, 1, 40);
insert into ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, is_unique, max_length) values
  (2, 1, 2, 'Количество страниц', 'order', 2, null, null, 1, 0, 10, 2, 5);
insert into ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, is_unique, max_length) values
  (3, 1, 4, 'Автор', 'author', 4, 2, 4, 1, null, 10, 1, null);
insert into ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, is_unique, max_length) values
  (5, 1, 5, 'Вес', 'weight', 2, null, null, 1, 3, 10, 1, 10);
insert into ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, is_unique, max_length) values
  (9, 1, 6, 'Ничего', 'null', 1, null, null, 1, null, 10, 0, 50);
insert into ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, is_unique, max_length) values
  (10, 1, 7, 'Уникальный атрибут', 'unique', 1, null, null, 1, null, 10, 1, 50);
insert into ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, is_unique, max_length) values
  (11, 1, 8, 'Еще один уникальный атрибут', 'unique_2', 1, null, null, 1, null, 10, 1, 50);
--ОКАТО
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, max_length) values
  (8, 4, 'Наименование муниципального образования', 'NAME', 1, 3, null, null, 1, null, 510, 1, 0, 50);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, max_length) values
  (6, 4, 'Идентификатор родительской записи', 'PARENT_ID', 4, 1, 4, 8, 1, null, 510, 0, 0, null);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, max_length) values
  (7, 4, 'Код ОКАТО', 'OKATO', 1, 2, null, null, 1, null, 11, 1, 1, 8);
-- Бухотчетность
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, max_length) values
  (12, 52, 'Код ОПУ', 'OPU_CODE', 1, 1, null, null, 1, null, 11, 1, 1, 8);
insert into ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, is_unique, max_length) values
  (13, 52, 'Сумма', 'TOTAL_SUM', 2, 2, null, null, 1, 4, 10, 1, 5);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, max_length) values
  (14, 52, 'Идентификатор', 'ITEM_NAME', 1, 3, null, null, 1, null, 11, 0, 2, 8);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, max_length) values
  (15, 52, 'Идентификатор 2', 'ACCOUNT_PERIOD_ID', 4, 4, 4, 8, 1, null, 11, 0, 0, null);
-- Участники ТЦО
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, max_length) values
  (16, 5, 'Дата начала', 'DATETIME', 3, 1, NULL, NULL, 1, NULL, 11, 0, 0, null);

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
-- Участники ТЦО
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(18, 1, 5, date '2013-01-01', 0);

insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (1, 1, 'Алиса в стране чудес', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (1, 2, null, 1113, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (1, 3, null, null, null, 5);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (1, 5, null, 0.25, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (1, 10, 'Уникальный атрибут', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (1, 11, 'Еще один уникальный атрибут', null , null, null);
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
--insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
--  (3, 1, 'Алиса в стране чудес', null, null, null);
--insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
--  (3, 2, null, 1113, null, null);
--insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
--  (3, 3, null, null, null, 5);
--insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
--  (3, 5, null, 0.25, null, null);

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
-- Участники ТЦО
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (18, 16, null, null, '2013-01-01 13:12:12', null);
-- Справочник ОКТМО
INSERT INTO ref_book_oktmo(ID, CODE, NAME, PARENT_ID, VERSION, STATUS, RECORD_ID) VALUES (1,'1','Почтовское',null,to_date('01.09.2014', 'DD.MM.YYYY'),1,1);
INSERT INTO ref_book_oktmo(ID, CODE, NAME, PARENT_ID, VERSION, STATUS, RECORD_ID) VALUES (2,'2','Ароматненское',null,to_date('01.09.2014', 'DD.MM.YYYY'),1,2);
INSERT INTO ref_book_oktmo(ID, CODE, NAME, PARENT_ID, VERSION, STATUS, RECORD_ID) VALUES (3,'3','Верхореченское',null,to_date('01.09.2014', 'DD.MM.YYYY'),1,3);
INSERT INTO ref_book_oktmo(ID, CODE, NAME, PARENT_ID, VERSION, STATUS, RECORD_ID) VALUES (4,'4','Вилинское',null,to_date('01.09.2014', 'DD.MM.YYYY'),1,4);
INSERT INTO ref_book_oktmo(ID, CODE, NAME, PARENT_ID, VERSION, STATUS, RECORD_ID) VALUES (5,'5','Голубинское',null,to_date('01.09.2014', 'DD.MM.YYYY'),1,5);
INSERT INTO ref_book_oktmo(ID, CODE, NAME, PARENT_ID, VERSION, STATUS, RECORD_ID) VALUES (6,'6','Долинненское',null,to_date('01.09.2014', 'DD.MM.YYYY'),1,6);
INSERT INTO ref_book_oktmo(ID, CODE, NAME, PARENT_ID, VERSION, STATUS, RECORD_ID) VALUES (7,'7','Тест',null,to_date('01.09.2014', 'DD.MM.YYYY'),1,7);
