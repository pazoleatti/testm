INSERT INTO DEPARTMENT (id, name, parent_id, type, shortname, tb_index, sbrf_code, region_id, code)
  VALUES (1, 'Банк', null, 1, null, null, '12', 1, 1);

INSERT INTO sec_user (id, name, login, department_id, is_active, email) VALUES
(0, 'Вася', 'controlBank', 1, 1, 'controlBank@bank.ru');
INSERT INTO sec_user (id, name, login, department_id, is_active, email) VALUES
(1, 'Петя', 'controlTB1', 1, 1, 'controlTB1@bank.ru');

INSERT INTO sec_role (id, alias, name) VALUES (1, 'ROLE_CONTROL', 'Контролёр');
INSERT INTO sec_role (id, alias, name) VALUES (2, 'ROLE_OPER', 'Оператор');
INSERT INTO sec_role (id, alias, name) VALUES (3, 'ROLE_CONTROL_UNP', 'Контролёр УНП');

INSERT INTO lock_data (key, user_id, date_before) VALUES ('a', 0, TIMESTAMP '2013-01-01 00:05:00.000000');
INSERT INTO lock_data (key, user_id, date_before) VALUES ('b', 1, TIMESTAMP '2013-01-01 00:00:00.000000');
