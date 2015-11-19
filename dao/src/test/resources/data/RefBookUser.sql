INSERT INTO ref_book (id, name) VALUES (74, 'Пользователи');

INSERT INTO ref_book (id, name) VALUES (30, 'Подразделения');

INSERT INTO ref_book_attribute (id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, max_length) VALUES
(162, 30, 1, 'Имя', 'name', 1, NULL, NULL, 1, NULL, 10, 100);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, max_length) VALUES
(651, 74, 'Код пользователя', 'ID', 2, 1, NULL, NULL, 1, 0, 9, 0, 19);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required) VALUES
(653, 74, 'Код подразделения', 'DEPARTMENT_ID', 4, 3, 30, 162, 1, NULL, 10, 0);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, max_length) VALUES
(654, 74, 'E-mail', 'EMAIL', 1, 4, NULL, NULL, 1, NULL, 128, 0, 100);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, max_length) VALUES
(652, 74, 'ФИО', 'NAME', 1, 2, NULL, NULL, 1, NULL, 50, 0, 100);

INSERT INTO department_type (id, name) VALUES (1, 'Банк');
INSERT INTO department_type (id, name) VALUES (2, 'Территориальный банк');

INSERT INTO department (id, name, parent_id, type, shortname, tb_index, sbrf_code, code) VALUES
(1, 'Главный Банк', NULL, 1, 'Банк', NULL, '018_075_01', 1);
INSERT INTO department (id, name, parent_id, type, shortname, tb_index, sbrf_code, code) VALUES
(2, 'Территориальный Банк №1', 1, 2, 'ТБ1', NULL, '018_003_01', 2);

INSERT INTO sec_user (id, login, name, department_id, is_active, email) VALUES
(3, 'controlBank', 'Контролёр Банка', 1, 1, NULL);
INSERT INTO sec_user (id, login, name, department_id, is_active, email) VALUES
(1, 'controlTB1', 'Контролёр ТБ1', 2, 1, NULL);
INSERT INTO sec_user (id, login, name, department_id, is_active, email) VALUES
(2, 'controlTB2', 'Контролёр ТБ2', 2, 1, NULL);
INSERT INTO sec_user (id, login, name, department_id, is_active, email) VALUES
(100, 'controlUnp', 'Контролёр УНП', 1, 1, NULL);
