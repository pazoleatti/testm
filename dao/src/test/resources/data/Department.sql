INSERT INTO department_type (id, name) VALUES (1, 'Банк');
INSERT INTO department_type (id, name) VALUES (2, 'Территориальный банк');
INSERT INTO department_type (id, name) VALUES (3, 'ЦСКО, ПЦП');
INSERT INTO department_type (id, name) VALUES (4, 'Управление');
INSERT INTO department_type (id, name) VALUES (5, '');

INSERT INTO department (id, name, parent_id, type, shortname, tb_index, sbrf_code, region_id, code)
  VALUES (1, 'Банк', NULL, 1, NULL, NULL, '12', 1, 1);
INSERT INTO department (id, name, parent_id, type, shortname, tb_index, sbrf_code, region_id, code)
  VALUES (2, 'ТБ1', 1, 2, NULL, NULL, '23', 2, 2);
INSERT INTO department (id, name, parent_id, type, shortname, tb_index, sbrf_code, region_id, code)
  VALUES (3, 'ТБ2', 1, 2, NULL, NULL, NULL, 1, 3);
INSERT INTO department (id, name, parent_id, type, shortname, tb_index, sbrf_code, region_id, code)
  VALUES (4, 'ЦСКО 1', 3, 3, NULL, NULL, NULL, 2, 4);
INSERT INTO department (id, name, parent_id, type, shortname, tb_index, sbrf_code, region_id, code)
  VALUES (5, 'ЦСКО 1', 3, 3, NULL, NULL, NULL, 1, 5);
INSERT INTO department (id, name, parent_id, type, shortname, tb_index, sbrf_code, region_id, code)
  VALUES (6, 'ЦСКО 1', 2, 3, NULL, NULL, NULL, 2, 6);
