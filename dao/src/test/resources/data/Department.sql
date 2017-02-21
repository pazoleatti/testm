INSERT INTO department_type (id, name) VALUES (1, 'Банк');
INSERT INTO department_type (id, name) VALUES (2, 'Территориальный банк');
INSERT INTO department_type (id, name) VALUES (3, 'ЦСКО, ПЦП');
INSERT INTO department_type (id, name) VALUES (4, 'Управление');
INSERT INTO department_type (id, name) VALUES (5, '');

INSERT INTO ref_book_region (id, record_id, version, code, name) VALUES (1, 1, date '2016-01-01', '01', 'Адыгея');
INSERT INTO ref_book_region (id, record_id, version, code, name) VALUES (2, 2, date '2016-01-01', '02', 'Башкортостан');

INSERT INTO department (id, name, parent_id, type, shortname, tb_index, sbrf_code, region_id, code, garant_use)
  VALUES (1, 'Банк', NULL, 1, NULL, NULL, '12', 1, 1, 0);
INSERT INTO department (id, name, parent_id, type, shortname, tb_index, sbrf_code, region_id, code, garant_use)
  VALUES (2, 'ТБ1', 1, 2, NULL, NULL, '23', 2, 2, 0);
INSERT INTO department (id, name, parent_id, type, shortname, tb_index, sbrf_code, region_id, code, garant_use)
  VALUES (3, 'ТБ2', 1, 2, NULL, NULL, NULL, 1, 3, 0);
INSERT INTO department (id, name, parent_id, type, shortname, tb_index, sbrf_code, region_id, code, garant_use)
  VALUES (4, 'ЦСКО 1', 3, 3, NULL, NULL, NULL, 2, 4, 0);
INSERT INTO department (id, name, parent_id, type, shortname, tb_index, sbrf_code, region_id, code, garant_use)
  VALUES (5, 'ЦСКО 1', 3, 3, NULL, NULL, NULL, 1, 5, 0);
INSERT INTO department (id, name, parent_id, type, shortname, tb_index, sbrf_code, region_id, code, garant_use)
  VALUES (6, 'ЦСКО 1', 2, 3, NULL, NULL, NULL, 2, 6, 0);
INSERT INTO department (id, name, parent_id, type, shortname, tb_index, sbrf_code, region_id, code, garant_use)
  VALUES (7, 'ТБ3', 1, 2, NULL, NULL, 'tb3', 2, 3, 0);
