insert into form_kind (id, name) values (1, 'Первичная');
insert into form_kind (id, name) values (2, 'Консолидированная');

INSERT INTO department_type (id, name) VALUES (1, 'Банк');
INSERT INTO department_type (id, name) VALUES (2, 'Территориальный банк');
INSERT INTO department_type (id, name) VALUES (3, 'ЦСКО, ПЦП');
INSERT INTO department_type (id, name) VALUES (4, 'Управление');

-- DEPARTMENT
insert into department (id, name, parent_id, type, code) values (1, 'testName1', null, 1, 1);
insert into department (id, name, parent_id, type, code) values (2, 'testName2', 1, 2, 2);
insert into department (id, name, parent_id, type, code) values (3, 'testName3', 1, 2, 3);
insert into department (id, name, parent_id, type, code) values (4, 'testName4', 1, 2, 4);
insert into department (id, name, parent_id, type, code) values (5, 'testName5', 1, 2, 5);
insert into department (id, name, parent_id, type, code) values (6, 'testName6', 2, 3, 6);
insert into department (id, name, parent_id, type, code) values (7, 'testName7', 6, 4, 7);
insert into department (id, name, parent_id, type, code) values (8, 'testName8', 2, 3, 8);
insert into department (id, name, parent_id, type, code) values (9, 'testName9', 6, 4, 9);
insert into department (id, name, parent_id, type, code) values (10, 'testName10', 2, 3, 10);
insert into department (id, name, parent_id, type, code) values (11, 'testName11', 1, 2, 11);
insert into department (id, name, parent_id, type, code) values (12, 'testName12', 1, 2, 12);
insert into department (id, name, parent_id, type, code) values (13, 'testName13', 1, 2, 13);

-- DECLARATION_TYPE
insert into declaration_type (id, tax_type, name) values (1, 'T', 'testName1');
insert into declaration_type (id, tax_type, name) values (2, 'I', 'testName2');
insert into declaration_type (id, tax_type, name) values (3, 'P', 'testName3');
insert into declaration_type (id, tax_type, name) values (4, 'V', 'testName4');

-- DEPARTMENT_DECLARATION_TYPE
insert into department_declaration_type (id, department_id, declaration_type_id) values (1, 1, 1);
insert into department_declaration_type (id, department_id, declaration_type_id) values (2, 1, 2);
insert into department_declaration_type (id, department_id, declaration_type_id) values (3, 2, 1);
insert into department_declaration_type (id, department_id, declaration_type_id) values (4, 7, 2);
insert into department_declaration_type (id, department_id, declaration_type_id) values (5, 9, 2);

-- FORM_TYPE
insert into form_type (id, name, tax_type) values (1, 'FormType - Transport', 'T');
insert into form_type (id, name, tax_type) values (2, 'FormType - Income', 'I');
insert into form_type (id, name, tax_type) values (3, 'FormType - VAT', 'V');
insert into form_type (id, name, tax_type) values (4, 'FormType - Property', 'P');

-- DEPARTMENT_FORM_TYPE
insert into department_form_type (id, department_id, form_type_id, kind) values (1, 1, 1, 1);
insert into department_form_type (id, department_id, form_type_id, kind) values (2, 2, 1, 1);
insert into department_form_type (id, department_id, form_type_id, kind) values (3, 3, 1, 1);
insert into department_form_type (id, department_id, form_type_id, kind) values (4, 1, 2, 2);
insert into department_form_type (id, department_id, form_type_id, kind) values (5, 9, 2, 1);
insert into department_form_type (id, department_id, form_type_id, kind) values (6, 10, 2, 1);
insert into department_form_type (id, department_id, form_type_id, kind) values (7, 11, 2, 2);
insert into department_form_type (id, department_id, form_type_id, kind) values (8, 12, 2, 2);
insert into department_form_type (id, department_id, form_type_id, kind) values (9, 13, 2, 2);

-- DECLARATION_SOURCE
insert into declaration_source (department_declaration_type_id, src_department_form_type_id, period_start, period_end) values (1, 1, date '2013-01-01', date '2014-01-01');
insert into declaration_source (department_declaration_type_id, src_department_form_type_id, period_start, period_end) values (2, 2, date '2013-01-01', date '2014-01-01');
insert into declaration_source (department_declaration_type_id, src_department_form_type_id, period_start, period_end) values (3, 1, date '1900-01-01', null);
insert into declaration_source (department_declaration_type_id, src_department_form_type_id, period_start, period_end) values (1, 4, date '1900-01-01', null);
insert into declaration_source (department_declaration_type_id, src_department_form_type_id, period_start, period_end) values (2, 4, date '1900-01-01', null);
insert into declaration_source (department_declaration_type_id, src_department_form_type_id, period_start, period_end) values (4, 5, date '1900-01-01', null);
insert into declaration_source (department_declaration_type_id, src_department_form_type_id, period_start, period_end) values (4, 6, date '1900-01-01', null);
insert into declaration_source (department_declaration_type_id, src_department_form_type_id, period_start, period_end) values (5, 7, date '1900-01-01', null);

-- FORM_DATA_SOURCE
insert into form_data_source (department_form_type_id, src_department_form_type_id, period_start, period_end) values (6, 7, date '1900-01-01', null);
insert into form_data_source (department_form_type_id, src_department_form_type_id, period_start, period_end) values (6, 8, date '1900-01-01', null);
insert into form_data_source (department_form_type_id, src_department_form_type_id, period_start, period_end) values (8, 9, date '1900-01-01', null);