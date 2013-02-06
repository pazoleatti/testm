insert into form_data(id, form_id, department_id, state, kind, report_period_id)
	values (1, 1, 1, 1, 3, 1);
	
insert into data_row(id, form_data_id, alias, ord, managed_by_scripts) values (1, 1, 'testAlias', 1, 1);

insert into string_value (row_id, column_id, value) values (1, 1, 'Строка 1');
insert into numeric_value (row_id, column_id, value) values (1, 2, 1.01);
insert into date_value (row_id, column_id, value) values (1, 3, DATE '2012-12-31');

insert into data_row(id, form_data_id, alias, ord, managed_by_scripts) values (2, 1, null, 2, 0);

insert into string_value (row_id, column_id, value) values (2, 1, 'Строка 2');
insert into numeric_value (row_id, column_id, value) values (2, 2, 2.02);
insert into date_value (row_id, column_id, value) values (2, 3, DATE '2013-01-01');


-- Для проверки FormDataDao.find
insert into report_period (id, name, tax_type, is_active, months) values (11, 'Transport report period 11', 'T', 0, 3)
insert into form_data(id, form_id, department_id, state, kind, report_period_id) values (11, 1, 1, 1, 3, 11);

insert into report_period (id, name, tax_type, is_active, months) values (12, 'Transport report period 12', 'T', 0, 3)
insert into form_data(id, form_id, department_id, state, kind, report_period_id) values (12, 1, 1, 1, 3, 12);
insert into form_data(id, form_id, department_id, state, kind, report_period_id) values (13, 1, 1, 1, 3, 12);

insert into report_period (id, name, tax_type, is_active, months) values (13, 'Transport report period 13', 'T', 0, 3)


