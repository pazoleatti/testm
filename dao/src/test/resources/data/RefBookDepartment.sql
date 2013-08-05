INSERT INTO REF_BOOK (ID, NAME)
  VALUES (1, 'Подразделения');

INSERT INTO REF_BOOK_ATTRIBUTE (ID, REF_BOOK_ID, ORD, NAME, ALIAS, TYPE, REFERENCE_ID, ATTRIBUTE_ID, VISIBLE, PRECISION, WIDTH)
  VALUES (1, 1, 1, 'Имя', 'name', 1, null, null, 1, null, 10);
INSERT INTO REF_BOOK_ATTRIBUTE (ID, REF_BOOK_ID, ORD, NAME, ALIAS, TYPE, REFERENCE_ID, ATTRIBUTE_ID, VISIBLE, PRECISION, WIDTH)
  VALUES (2, 1, 2, 'СБРФ КОД', 'sbrf_code', 1, null, null, 1, null, 10);

INSERT INTO DEPARTMENT (id, name, parent_id, type, shortname, dict_region_id, tb_index, sbrf_code)
  VALUES (1, 'Главный Банк', null, 1, 'Банк', null, null, '018_075_01');
INSERT INTO DEPARTMENT (id, name, parent_id, type, shortname, dict_region_id, tb_index, sbrf_code)
  VALUES (2, 'Териториальный Банк №1', 1, 2, 'ТБ1', NULL, NULL, '018_003_01');