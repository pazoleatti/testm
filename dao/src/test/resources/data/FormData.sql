insert into form_data(id, form_template_id, department_id, state, kind, report_period_id)
	values (1, 1, 1, 1, 3, 1);
	
insert into data_row(id, form_data_id, alias, ord) values (1, 1, 'testAlias', 1);

insert into string_value (row_id, column_id, value) values (1, 1, 'Строка 1');
insert into numeric_value (row_id, column_id, value) values (1, 2, 1.01);
insert into date_value (row_id, column_id, value) values (1, 3, DATE '2012-12-31');

insert into data_row(id, form_data_id, alias, ord) values (2, 1, null, 2);

insert into string_value (row_id, column_id, value) values (2, 1, 'Строка 2');
insert into numeric_value (row_id, column_id, value) values (2, 2, 2.02);
insert into date_value (row_id, column_id, value) values (2, 3, DATE '2013-01-01');


-- Для проверки FormDataDao.find
insert into tax_period(id, tax_type, start_date, end_date) values (10, 'T', date '2013-01-01', date '2013-12-31');
insert into report_period (id, name, is_active, months, tax_period_id, ord, department_id) values (11, 'Transport report period 11', 0, 3, 10, 1, 1);
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id) values (11, 1, 1, 1, 3, 11);

insert into report_period (id, name, is_active, months, tax_period_id, ord, department_id) values (12, 'Transport report period 12', 0, 3, 10, 2, 1);
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id) values (12, 1, 1, 1, 3, 12);
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id) values (13, 1, 1, 1, 3, 12);

insert into report_period (id, name, is_active, months, tax_period_id, ord, department_id) values (13, 'Transport report period 13', 0, 3, 10, 3, 1);