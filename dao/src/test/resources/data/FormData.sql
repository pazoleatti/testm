insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (1, 1, 1, 1, 3, 1, 0);
	
-- Для проверки FormDataDao.find
insert into tax_period(id, tax_type, start_date, end_date) values (10, 'T', date '2013-01-01', date '2013-12-31');
insert into report_period (id, name, months, tax_period_id, ord, dict_tax_period_id) values (11, 'Transport report period 11', 3, 10, 6, 21);
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (11, 1, 1, 1, 3, 11, 0);

insert into report_period (id, name, months, tax_period_id, ord, dict_tax_period_id) values (12, 'Transport report period 12', 3, 10, 2, 22);
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (12, 1, 1, 1, 3, 12, 0);
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (13, 1, 1, 1, 3, 12, 0);

insert into report_period (id, name, months, tax_period_id, ord, dict_tax_period_id) values (13, 'Transport report period 13', 3, 10, 3, 23);

-- Ежемесячные формы
insert into tax_period(id, tax_type, start_date, end_date) values (12, 'I', date '2013-01-01', date '2013-12-31');
insert into report_period (id, name, months, tax_period_id, ord, dict_tax_period_id) values (14, 'Deal report period 14', 3, 12, 1, 22);
insert into report_period (id, name, months, tax_period_id, ord, dict_tax_period_id) values (15, 'Deal report period 15', 3, 12, 2, 23);
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign, period_order) values (14, 2, 1, 1, 1, 14, 0, 1);
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign, period_order) values (15, 2, 1, 1, 1, 14, 0, 2);
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign, period_order) values (16, 2, 1, 1, 1, 14, 0, 3);
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign, period_order) values (17, 2, 1, 1, 1, 15, 0, 4);
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign, period_order) values (18, 2, 1, 1, 1, 15, 0, 5);
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign, period_order) values (19, 2, 1, 1, 1, 15, 0, 5); /* Повтор для теста */
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign, period_order) values (20, 2, 1, 1, 1, 15, 0, 6);