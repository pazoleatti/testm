insert into department (id, name, parent_id, type) values (1, 'А - департамент', null, 1);
insert into department (id, name, parent_id, type) values (2, 'Б - департамент', 1, 2);
insert into department (id, name, parent_id, type) values (3, 'В - департамент', 1, 2);

insert into form_type (id, name, tax_type) values (1, 'А - тип', 'T');
insert into form_type (id, name, tax_type) values (2, 'Б - тип', 'I');
insert into form_type (id, name, tax_type) values (3, 'В - тип', 'T');
insert into form_type (id, name, tax_type) values (4, 'Г - тип', 'T');

insert into form_template (id, type_id, data_rows, version, is_active, edition, numbered_columns, fixed_rows, name, fullname, code) values (1, 1, null, '0.1', 1, 1, 1, 1,'name', 'fullname', 'code');
insert into form_template (id, type_id, data_rows, version, is_active, edition, numbered_columns, fixed_rows, name, fullname, code) values (2, 2, null, '0.1', 1, 1, 1, 1,'name', 'fullname', 'code');
insert into form_template (id, type_id, data_rows, version, is_active, edition, numbered_columns, fixed_rows, name, fullname, code) values (3, 3, null, '0.1', 1, 1, 1, 1,'name', 'fullname', 'code');
insert into form_template (id, type_id, data_rows, version, is_active, edition, numbered_columns, fixed_rows, name, fullname, code) values (4, 4, null, '0.1', 1, 1, 1, 1,'name', 'fullname', 'code');

insert into tax_period(id, tax_type, start_date, end_date) values (1, 'T', date '2013-01-01', date '2013-12-31');
insert into dict_tax_period (code, name, I, T, P, V, D) values ('21', 'первый квартал', 1, 1, 1, 1, 0);

insert into report_period (id, name, is_active, months, tax_period_id, ord, department_id, dict_tax_period_id) values (1, '1 - период', 1, 3, 1, 1, 1, 21);
insert into report_period (id, name, is_active, months, tax_period_id, ord, department_id, dict_tax_period_id) values (2, '2 - период', 1, 3, 1, 2, 1, 21);
insert into report_period (id, name, is_active, months, tax_period_id, ord, department_id, dict_tax_period_id) values (3, '3 - период', 1, 3, 1, 3, 1, 21);

insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (1,  4, 1, 1, 2, 1, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (2,  3, 2, 2, 3, 2, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (3,  2, 3, 3, 2, 3, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (4,  1, 1, 4, 3, 1, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (5,  4, 2, 1, 2, 2, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (6,  3, 3, 2, 3, 3, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (7,  2, 1, 3, 2, 1, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (8,  1, 2, 4, 3, 2, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (9,  4, 3, 1, 2, 3, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (10, 3, 1, 2, 3, 1, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (11, 2, 2, 3, 2, 2, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (12, 1, 3, 4, 3, 3, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (13, 4, 1, 1, 2, 1, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (14, 3, 2, 2, 3, 2, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (15, 2, 3, 3, 2, 3, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (16, 1, 1, 4, 3, 1, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (17, 4, 2, 1, 2, 2, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (18, 3, 3, 2, 3, 3, 0);

-- Каждая из четырёх форм может быть в каждом из трёх департаментов с Kind = 2 и 3
insert into department_form_type(id, department_id, form_type_id, kind) values (11, 1, 1, 2);
insert into department_form_type(id, department_id, form_type_id, kind) values (12, 1, 1, 3);
insert into department_form_type(id, department_id, form_type_id, kind) values (13, 1, 2, 2);
insert into department_form_type(id, department_id, form_type_id, kind) values (14, 1, 2, 3);
insert into department_form_type(id, department_id, form_type_id, kind) values (15, 1, 3, 2);
insert into department_form_type(id, department_id, form_type_id, kind) values (16, 1, 3, 3);
insert into department_form_type(id, department_id, form_type_id, kind) values (17, 1, 4, 2);
insert into department_form_type(id, department_id, form_type_id, kind) values (18, 1, 4, 3);

insert into department_form_type(id, department_id, form_type_id, kind) values (21, 2, 1, 2);
insert into department_form_type(id, department_id, form_type_id, kind) values (22, 2, 1, 3);
insert into department_form_type(id, department_id, form_type_id, kind) values (23, 2, 2, 2);
insert into department_form_type(id, department_id, form_type_id, kind) values (24, 2, 2, 3);
insert into department_form_type(id, department_id, form_type_id, kind) values (25, 2, 3, 2);
insert into department_form_type(id, department_id, form_type_id, kind) values (26, 2, 3, 3);
insert into department_form_type(id, department_id, form_type_id, kind) values (27, 2, 4, 2);
insert into department_form_type(id, department_id, form_type_id, kind) values (28, 2, 4, 3);

insert into department_form_type(id, department_id, form_type_id, kind) values (31, 3, 1, 2);
insert into department_form_type(id, department_id, form_type_id, kind) values (32, 3, 1, 3);
insert into department_form_type(id, department_id, form_type_id, kind) values (33, 3, 2, 2);
insert into department_form_type(id, department_id, form_type_id, kind) values (34, 3, 2, 3);
insert into department_form_type(id, department_id, form_type_id, kind) values (35, 3, 3, 2);
insert into department_form_type(id, department_id, form_type_id, kind) values (36, 3, 3, 3);
insert into department_form_type(id, department_id, form_type_id, kind) values (37, 3, 4, 2);
insert into department_form_type(id, department_id, form_type_id, kind) values (38, 3, 4, 3);

-- Для форм подразделения 1 источниками будут являться форма 3 из подразделения 2 и форма 4 из подразделения 3
insert into form_data_source(department_form_type_id, src_department_form_type_id) values (11, 25);
insert into form_data_source(department_form_type_id, src_department_form_type_id) values (11, 26);
insert into form_data_source(department_form_type_id, src_department_form_type_id) values (14, 37);
insert into form_data_source(department_form_type_id, src_department_form_type_id) values (14, 38);

-- Для деклараций подразделения 1  источником будет являться форма 1 из подразделений 2 и 3
insert into declaration_type(id, name, tax_type) values (1, 'Декларация', 'T');
insert into department_declaration_type(id, department_id, declaration_type_id) values (1, 1, 1);
insert into declaration_source (department_declaration_type_id, src_department_form_type_id) values (1, 21);
insert into declaration_source (department_declaration_type_id, src_department_form_type_id) values (1, 22);
insert into declaration_source (department_declaration_type_id, src_department_form_type_id) values (1, 31);
insert into declaration_source (department_declaration_type_id, src_department_form_type_id) values (1, 32);