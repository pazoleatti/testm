INSERT INTO department_type (id, name) VALUES (1, 'Банк');
INSERT INTO ref_book_region (id, record_id, version, code, name) VALUES (1, 1, date '2016-01-01', '01', 'Адыгея');
INSERT INTO department (id, name, parent_id, type, shortname, tb_index, sbrf_code, region_id, code)
  VALUES (1, 'Банк', null, 1, null, null, '12', 1, 1);

INSERT INTO sec_user (id, name, login, department_id, is_active, email) VALUES
(0, 'Вася', 'controlBank', 1, 1, 'controlBank@bank.ru');
INSERT INTO sec_user (id, name, login, department_id, is_active, email) VALUES
(1, 'Петя', 'controlTB1', 1, 1, 'controlTB1@bank.ru');
INSERT INTO sec_user (id, name, login, department_id, is_active, email) VALUES
(2, 'Петя2', 'controlTB2', 1, 1, 'controlTB2@bank.ru');

INSERT INTO sec_role (id, alias, name) VALUES (1, 'ROLE_CONTROL', 'Контролёр');
INSERT INTO sec_role (id, alias, name) VALUES (2, 'ROLE_OPER', 'Оператор');
INSERT INTO sec_role (id, alias, name) VALUES (3, 'ROLE_CONTROL_UNP', 'Контролёр УНП');

INSERT INTO lock_data (key, user_id, date_lock) VALUES ('a', 0, TIMESTAMP '2013-01-01 00:05:00.000000');
INSERT INTO lock_data (key, user_id) VALUES ('b', 1);
INSERT INTO lock_data (key, user_id) VALUES ('FORM_DATA_1', 2);
INSERT INTO lock_data (key, user_id) VALUES ('aaa', 0);
INSERT INTO lock_data (key, user_id) VALUES ('q', 0);

INSERT INTO lock_data_subscribers (lock_key, user_id) VALUES ('a', 1);
INSERT INTO lock_data_subscribers (lock_key, user_id) VALUES ('a', 2);