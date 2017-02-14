INSERT INTO form_kind (id, name) VALUES (1, 'Первичная');
INSERT INTO form_kind (id, name) VALUES (2, 'Консолидированная');
INSERT INTO form_kind (id, name) VALUES (3, 'Сводная');
INSERT INTO form_kind (id, name) VALUES (4, 'Форма УНП');
INSERT INTO form_kind (id, name) VALUES (5, 'Выходная');

--TAX_TYPE
INSERT INTO tax_type (id, name) VALUES ('T', 'Транспортный');
INSERT INTO tax_type (id, name) VALUES ('I', 'Прибыль');
INSERT INTO tax_type (id, name) VALUES ('P', 'Имущество');
INSERT INTO tax_type (id, name) VALUES ('V', 'НДС');
INSERT INTO tax_type (id, name) VALUES ('D', 'ТЦО');

INSERT INTO declaration_type (id, name, tax_type) VALUES (1, 'Вид налоговой формы (тест)', 'T');

INSERT INTO form_type (id, name, tax_type) VALUES (1, 'FormType - Transport', 'T');
INSERT INTO form_type (id, name, tax_type) VALUES (2, 'FormType - Income', 'I');
INSERT INTO form_type (id, name, tax_type) VALUES (3, 'FormType - VAT', 'V');
INSERT INTO form_type (id, name, tax_type) VALUES (4, 'FormType - Property', 'P');
INSERT INTO form_type (id, name, tax_type) VALUES (5, 'FormType - DEAL', 'D');

INSERT INTO form_type (id, name, tax_type) VALUES (11, 'FormType - Transport2', 'T');
INSERT INTO form_type (id, name, tax_type) VALUES (12, 'FormType - Transport3', 'T');

INSERT INTO form_type (id, name, tax_type) VALUES (21, 'FormType - Income2', 'I');

INSERT INTO department_type (id, name) VALUES (1, 'Банк');
INSERT INTO department_type (id, name) VALUES (2, 'Территориальный банк');
INSERT INTO department_type (id, name) VALUES (3, 'ЦСКО, ПЦП');
INSERT INTO department_type (id, name) VALUES (4, 'Управление');

INSERT INTO department (id, name, parent_id, type, code) VALUES (1, 'Банк', NULL, 1, 1);
INSERT INTO department (id, name, parent_id, type, code) VALUES (2, 'ТБ1', 1, 2, 2);
INSERT INTO department (id, name, parent_id, type, code) VALUES (3, 'ТБ2', 1, 2, 3);
INSERT INTO department (id, name, parent_id, type, code) VALUES (4, 'ГОСБ1', 2, 3, 4);
INSERT INTO department (id, name, parent_id, type, code) VALUES (5, 'ГОСБ2', 2, 3, 5);
INSERT INTO department (id, name, parent_id, type, code) VALUES (6, 'ГОСБ3', 2, 3, 6);
INSERT INTO department (id, name, parent_id, type, code) VALUES (7, 'ОСБ1', 4, 4, 7);

-- В подразделении 1 есть все налоговые формы
INSERT INTO department_form_type (id, department_id, form_type_id, kind) VALUES (1, 1, 1, 3);
INSERT INTO department_form_type (id, department_id, form_type_id, kind) VALUES (2, 1, 2, 3);
INSERT INTO department_form_type (id, department_id, form_type_id, kind) VALUES (3, 1, 3, 3);
INSERT INTO department_form_type (id, department_id, form_type_id, kind) VALUES (4, 1, 4, 3);
INSERT INTO department_form_type (id, department_id, form_type_id, kind) VALUES (5, 1, 11, 3);
INSERT INTO department_form_type (id, department_id, form_type_id, kind) VALUES (6, 1, 12, 3);
INSERT INTO department_form_type (id, department_id, form_type_id, kind) VALUES (7, 1, 21, 3);

-- В подразделении 2 есть формы 1 и 2
INSERT INTO department_form_type (id, department_id, form_type_id, kind) VALUES (11, 2, 1, 3);
INSERT INTO department_form_type (id, department_id, form_type_id, kind) VALUES (12, 2, 2, 3);

-- В подразделении 3 есть формы 1 и 2
INSERT INTO department_form_type (id, department_id, form_type_id, kind) VALUES (21, 3, 1, 3);
INSERT INTO department_form_type (id, department_id, form_type_id, kind) VALUES (22, 3, 2, 3);

-- В подразделении 4 форма 4
INSERT INTO department_form_type (id, department_id, form_type_id, kind) VALUES (23, 4, 2, 2);

-- В подразделении 6 форма 2
INSERT INTO department_form_type (id, department_id, form_type_id, kind) VALUES (24, 6, 2, 2);

insert into department_form_type_performer (DEPARTMENT_FORM_TYPE_ID, PERFORMER_DEP_ID) values (7, 1);

insert into department_form_type_performer (DEPARTMENT_FORM_TYPE_ID, PERFORMER_DEP_ID) values (1, 2);
insert into department_form_type_performer (DEPARTMENT_FORM_TYPE_ID, PERFORMER_DEP_ID) values (2, 2);
insert into department_form_type_performer (DEPARTMENT_FORM_TYPE_ID, PERFORMER_DEP_ID) values (5, 2);
insert into department_form_type_performer (DEPARTMENT_FORM_TYPE_ID, PERFORMER_DEP_ID) values (21, 2);
insert into department_form_type_performer (DEPARTMENT_FORM_TYPE_ID, PERFORMER_DEP_ID) values (22, 2);

-- Для Формы 1 в подразделении 2 источниками являются формы 1, 2, 3, 4 из подразделения 1  и форма 2 из подразделения 3
INSERT INTO form_data_source (department_form_type_id, src_department_form_type_id, period_start, period_end) VALUES (11, 1, date '2013-01-01', date '2014-01-01');
INSERT INTO form_data_source (department_form_type_id, src_department_form_type_id, period_start, period_end) VALUES (11, 2, date '1900-01-01', NULL);
INSERT INTO form_data_source (department_form_type_id, src_department_form_type_id, period_start, period_end) VALUES (11, 3, date '1900-01-01', NULL);
INSERT INTO form_data_source (department_form_type_id, src_department_form_type_id, period_start, period_end) VALUES (11, 4, date '1900-01-01', NULL);
INSERT INTO form_data_source (department_form_type_id, src_department_form_type_id, period_start, period_end) VALUES (11, 22, date '1900-01-01', NULL);

-- Для формы 2 в подразделении 2 источниками являются форма 1 из подразлделения 1 и форма 1 из подразделения 2
INSERT INTO form_data_source (department_form_type_id, src_department_form_type_id, period_start, period_end) VALUES (12, 1, date '2013-01-01', date '2014-01-01');
INSERT INTO form_data_source (department_form_type_id, src_department_form_type_id, period_start, period_end) VALUES (12, 11, date '1900-01-01', NULL);

-- Для формы 2 в подразделении 4 источниками являются форма 2 из подразделения 6
INSERT INTO form_data_source (department_form_type_id, src_department_form_type_id, period_start, period_end) VALUES (23, 24, date '1900-01-01', NULL);

-- В подразделении 2 есть декларация 1 
INSERT INTO department_declaration_type (id, department_id, declaration_type_id) VALUES (1, 2, 1);

-- Для декларации 1 в подразделении 2 источником является формы 1,2 из подразделения 3 и формы 11, 12 из подразделения 1
INSERT INTO declaration_source (department_declaration_type_id, src_department_form_type_id, period_start, period_end) VALUES (1, 21, date '1900-01-01', NULL);
INSERT INTO declaration_source (department_declaration_type_id, src_department_form_type_id, period_start, period_end) VALUES (1, 22, date '1900-01-01', NULL);
INSERT INTO declaration_source (department_declaration_type_id, src_department_form_type_id, period_start, period_end) VALUES (1, 5, date '1900-01-01', NULL);
INSERT INTO declaration_source (department_declaration_type_id, src_department_form_type_id, period_start, period_end) VALUES (1, 6, date '1900-01-01', NULL);
