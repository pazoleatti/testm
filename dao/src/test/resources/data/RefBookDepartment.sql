INSERT INTO REF_BOOK (ID, NAME)
  VALUES (30, 'Подразделения');

INSERT INTO REF_BOOK_ATTRIBUTE (ID, REF_BOOK_ID, ORD, NAME, ALIAS, TYPE, REFERENCE_ID, ATTRIBUTE_ID, VISIBLE, PRECISION, WIDTH)
  VALUES (162, 30, 1, 'Имя', 'name', 1, null, null, 1, null, 10);
INSERT INTO REF_BOOK_ATTRIBUTE (ID, REF_BOOK_ID, ORD, NAME, ALIAS, TYPE, REFERENCE_ID, ATTRIBUTE_ID, VISIBLE, PRECISION, WIDTH)
  VALUES (2, 30, 2, 'СБРФ КОД', 'sbrf_code', 1, null, null, 1, null, 10);

INSERT INTO DEPARTMENT (id, name, parent_id, type, shortname, tb_index, sbrf_code)
  VALUES (1, 'Главный Банк', null, 1, 'Банк', null, '018_075_01');
INSERT INTO DEPARTMENT (id, name, parent_id, type, shortname, tb_index, sbrf_code)
  VALUES (2, 'Териториальный Банк №1', 1, 2, 'ТБ1', NULL, '018_003_01');