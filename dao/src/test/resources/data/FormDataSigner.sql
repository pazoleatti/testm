insert into department (id, name, parent_id, type) values (1, 'А - департамент', null, 1);
insert into department (id, name, parent_id, type) values (2, 'Б - департамент', 1, 2);

insert into form_type (id, name, tax_type) values (1, 'А - тип', 'T');
insert into form_type (id, name, tax_type) values (2, 'Б - тип', 'I');

insert into form_template (id, type_id, data_rows, version, is_active, edition, numbered_columns, fixed_rows, name, fullname, code) values (1, 1, null, '0.1', 1, 1, 1, 1, 'name', 'fullname', 'code');
insert into form_template (id, type_id, data_rows, version, is_active, edition, numbered_columns, fixed_rows, name, fullname, code) values (2, 2, null, '0.1', 1, 1, 1, 1, 'name', 'fullname', 'code');

insert into tax_period(id, tax_type, start_date, end_date) values (1, 'T', date '2013-01-01', date '2013-12-31');

insert into report_period (id, name, is_active, months, tax_period_id, ord, department_id) values (1, '1 - период', 1, 3, 1, 1, 1)
insert into report_period (id, name, is_active, months, tax_period_id, ord, department_id) values (2, '2 - период', 1, 3, 1, 2, 1)

insert into form_data (id, form_template_id, department_id, state, kind, report_period_id) values (1, 1, 1, 1, 2, 1);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id) values (2, 2, 2, 2, 3, 2);

insert into form_data_signer (id, form_data_id, name, position, ord) values (1, 1, 'name1', 'position1', 1);
insert into form_data_signer (id, form_data_id, name, position, ord) values (2, 1, 'name2', 'position2', 2);
insert into form_data_signer (id, form_data_id, name, position, ord) values (3, 2, 'name3', 'position3', 1);
insert into form_data_signer (id, form_data_id, name, position, ord) values (4, 2, 'name4', 'position4', 2);
