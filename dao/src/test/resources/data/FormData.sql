-- Типы налоговых форм
insert into form_kind (id, name) values (1, 'Первичная');
insert into form_kind (id, name) values (2, 'Консолидированная');
insert into form_kind (id, name) values (3, 'Сводная');
insert into form_kind (id, name) values (4, 'Форма УНП');
insert into form_kind (id, name) values (5, 'Выходная');

insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (1, 1, 1, 1, 3, 1, 0);
	
-- Для проверки FormDataDao.find
insert into tax_period(id, tax_type, year) values (10, 'T', 2014);
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) values (11, 'первый квартал', 10, 21, date '2013-01-01', date '2013-03-31', date '2013-01-01');
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (11, 1, 1, 1, 3, 11, 0);

insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) values (12, 'первый квартал', 10, 22, date '2013-04-01', date '2013-06-30', date '2013-04-01');
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (12, 1, 1, 1, 3, 12, 0);
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (13, 1, 1, 1, 3, 12, 0);

insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) values (13, 'Transport report period 13', 10, 23, date '2013-07-01', date '2013-09-30', date '2013-07-01');

-- Ежемесячные формы
insert into tax_period(id, tax_type, year) values (12, 'I', 2013);
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) values (14, 'Deal report period 14', 12, 22, date '2013-01-01', date '2013-03-31', date '2013-01-01');
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) values (15, 'Deal report period 15', 12, 23, date '2013-04-01', date '2013-06-30', date '2013-04-01');
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign, period_order) values (14, 2, 1, 1, 1, 14, 0, 1);
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign, period_order) values (15, 2, 1, 1, 1, 14, 0, 2);
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign, period_order) values (16, 2, 1, 1, 1, 14, 0, 3);
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign, period_order) values (17, 2, 1, 1, 1, 15, 0, 4);
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign, period_order) values (18, 2, 1, 1, 1, 15, 0, 5);
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign, period_order) values (19, 2, 1, 1, 1, 15, 0, 5); /* Повтор для теста */
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign, period_order) values (20, 2, 1, 1, 1, 15, 0, 6);

insert into form_data_performer(form_data_id, name, phone, PRINT_DEPARTMENT_ID, REPORT_DEPARTMENT_NAME) values (11, 'Bank', '', 1, 'Bank/Uralsib');
insert into form_data_performer(form_data_id, name, phone, PRINT_DEPARTMENT_ID, REPORT_DEPARTMENT_NAME) values (12, 'Bank', '', 2, 'BankTB1');
insert into form_data_performer(form_data_id, name, phone, PRINT_DEPARTMENT_ID, REPORT_DEPARTMENT_NAME) values (13, 'Bank', '', 3, 'BankTB1k/Uralsib');

-- Для автонумеруемой графы
insert into tax_period(id, tax_type, year) values (100, 'I', 2014);

insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) values (200, 'первый квартал', 100, 21, date '2014-01-01', date '2014-03-31', date '2014-01-01');
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) values (201, 'полугодие', 100, 22, date '2014-04-01', date '2014-06-30', date '2014-04-01');
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) values (202, '9 месяцев', 100, 23, date '2014-07-01', date '2014-09-30', date '2014-07-01');

insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign, period_order) values (300, 1, 3, 2, 1, 200, 0, 1);
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign, period_order) values (301, 1, 3, 1, 1, 202, 0, 3);
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign, period_order) values (302, 1, 3, 3, 1, 201, 0, 2);
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign, period_order) values (303, 1, 3, 4, 2, 200, 0, 1);

insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign, period_order) values (304, 1, 2, 1, 1, 200, 0, 1);
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign, period_order) values (305, 2, 2, 1, 1, 200, 0, 1);

-- Для проверки изменения количества строк в табличной части НФ
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (1000, 1, 1, 1, 3, 1, 0);
insert into data_row(id, form_data_id, alias, ord, type) values (1, 1000, null, 1, 0);
insert into data_row(id, form_data_id, alias, ord, type) values (2, 1000, 'total1', 2, 0);
insert into data_row(id, form_data_id, alias, ord, type) values (3, 1000, null, 3, 0);
insert into data_row(id, form_data_id, alias, ord, type) values (4, 1000, 'total2', 4, 0);

-- Для проверки метода getFormDataListByTemplateId
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign, period_order) values (400, 4, 1, 2, 1, 200, 0, 1);
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign, period_order) values (401, 4, 2, 1, 1, 202, 0, 3);
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign, period_order) values (402, 4, 3, 1, 1, 201, 0, 2);
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign, period_order) values (403, 4, 4, 1, 1, 200, 0, 1);