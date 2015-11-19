INSERT INTO ref_book (id, name)
  VALUES (30, 'Подразделения');

INSERT INTO ref_book_attribute (id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, is_unique, max_length)
  VALUES (101, 30, 1, 'код подразделения', 'code', 2, null, null, 1, 0, 10, 1, 9);
INSERT INTO ref_book_attribute (id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, is_unique, max_length)
  VALUES (162, 30, 2, 'Имя', 'name', 1, null, null, 1, null, 10, 1, 100);
INSERT INTO ref_book_attribute (id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, max_length)
  VALUES (2, 30, 3, 'СБРФ КОД', 'sbrf_code', 1, null, null, 1, null, 10, 100);
INSERT INTO ref_book_attribute (id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, max_length)
  VALUES (103, 30, 4, 'тип', 'type', 2, null, null, 1, 0, 10, 9);

INSERT INTO department_type (id, name) VALUES (1, 'Банк');
INSERT INTO department_type (id, name) VALUES (2, 'Территориальный банк');
INSERT INTO department_type (id, name) VALUES (3, 'ЦСКО, ПЦП');
INSERT INTO department_type (id, name) values (4, 'Управление');

INSERT INTO department (id, name, parent_id, type, shortname, tb_index, sbrf_code, code)
  VALUES (1, 'Главный Банк', null, 1, 'Банк', null, '018_075_01', 1);
INSERT INTO department (id, name, parent_id, type, shortname, tb_index, sbrf_code, code)
  VALUES (2, 'Территориальный Банк №1', 1, 2, 'ТБ1', NULL, '018_003_01', 2);