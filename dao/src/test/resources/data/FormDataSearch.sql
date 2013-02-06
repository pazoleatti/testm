insert into department (id, name, parent_id, type) values (1, 'А - департамент', null, 1);
insert into department (id, name, parent_id, type) values (2, 'Б - департамент', 1, 2);
insert into department (id, name, parent_id, type) values (3, 'В - департамент', 1, 2);

insert into form_type (id, name, tax_type, fixed_rows) values (1, 'А - тип', 'T', 1);
insert into form_type (id, name, tax_type, fixed_rows) values (2, 'Б - тип', 'I', 1);
insert into form_type (id, name, tax_type, fixed_rows) values (3, 'В - тип', 'T', 1);
insert into form_type (id, name, tax_type, fixed_rows) values (4, 'Г - тип', 'T', 1);

insert into form (id, type_id, data_rows, version, is_active, edition, numbered_columns) values (1, 1, null, '0.1', 1, 1, 1);
insert into form (id, type_id, data_rows, version, is_active, edition, numbered_columns) values (2, 2, null, '0.1', 1, 1, 1);
insert into form (id, type_id, data_rows, version, is_active, edition, numbered_columns) values (3, 3, null, '0.1', 1, 1, 1);
insert into form (id, type_id, data_rows, version, is_active, edition, numbered_columns) values (4, 4, null, '0.1', 1, 1, 1);

insert into report_period (id, name, tax_type, is_active, months) values (1, '1 - период', 'T', 1, 3)
insert into report_period (id, name, tax_type, is_active, months) values (2, '2 - период', 'T', 1, 3)
insert into report_period (id, name, tax_type, is_active, months) values (3, '3 - период', 'T', 1, 3)

insert into form_data (id, form_id, department_id, state, kind, report_period_id) values (1,  4, 1, 1, 2, 1);
insert into form_data (id, form_id, department_id, state, kind, report_period_id) values (2,  3, 2, 2, 3, 2);
insert into form_data (id, form_id, department_id, state, kind, report_period_id) values (3,  2, 3, 3, 2, 3);
insert into form_data (id, form_id, department_id, state, kind, report_period_id) values (4,  1, 1, 4, 3, 1);
insert into form_data (id, form_id, department_id, state, kind, report_period_id) values (5,  4, 2, 1, 2, 2);
insert into form_data (id, form_id, department_id, state, kind, report_period_id) values (6,  3, 3, 2, 3, 3);
insert into form_data (id, form_id, department_id, state, kind, report_period_id) values (7,  2, 1, 3, 2, 1);
insert into form_data (id, form_id, department_id, state, kind, report_period_id) values (8,  1, 2, 4, 3, 2);
insert into form_data (id, form_id, department_id, state, kind, report_period_id) values (9,  4, 3, 1, 2, 3);
insert into form_data (id, form_id, department_id, state, kind, report_period_id) values (10, 3, 1, 2, 3, 1);
insert into form_data (id, form_id, department_id, state, kind, report_period_id) values (11, 2, 2, 3, 2, 2);
insert into form_data (id, form_id, department_id, state, kind, report_period_id) values (12, 1, 3, 4, 3, 3);
insert into form_data (id, form_id, department_id, state, kind, report_period_id) values (13, 4, 1, 1, 2, 1);
insert into form_data (id, form_id, department_id, state, kind, report_period_id) values (14, 3, 2, 2, 3, 2);
insert into form_data (id, form_id, department_id, state, kind, report_period_id) values (15, 2, 3, 3, 2, 3);
insert into form_data (id, form_id, department_id, state, kind, report_period_id) values (16, 1, 1, 4, 3, 1);
insert into form_data (id, form_id, department_id, state, kind, report_period_id) values (17, 4, 2, 1, 2, 2);
insert into form_data (id, form_id, department_id, state, kind, report_period_id) values (18, 3, 3, 2, 3, 3);