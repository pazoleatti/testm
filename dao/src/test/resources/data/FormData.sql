-- Типы налоговых форм
insert into form_kind (id, name) values (1, 'Первичная');
insert into form_kind (id, name) values (2, 'Консолидированная');
insert into form_kind (id, name) values (3, 'Сводная');
insert into form_kind (id, name) values (4, 'Форма УНП');
insert into form_kind (id, name) values (5, 'Выходная');

insert into form_data(id, form_template_id, department_report_period_id, state, kind, return_sign) values (1, 1, 101, 1, 3, 0);
insert into form_data(id, form_template_id, department_report_period_id, state, kind, return_sign) values (11, 1, 111, 1, 3, 0);
insert into form_data(id, form_template_id, department_report_period_id, state, kind, return_sign) values (12, 1, 112, 1, 3, 0);
insert into form_data(id, form_template_id, department_report_period_id, state, kind, return_sign) values (13, 1, 112, 1, 3, 0);

insert into form_data(id, form_template_id, department_report_period_id, state, kind, return_sign, period_order) values (14, 2, 114, 1, 1, 0, 1);
insert into form_data(id, form_template_id, department_report_period_id, state, kind, return_sign, period_order) values (15, 2, 114, 1, 1, 0, 2);
insert into form_data(id, form_template_id, department_report_period_id, state, kind, return_sign, period_order) values (16, 2, 114, 1, 1, 0, 3);
insert into form_data(id, form_template_id, department_report_period_id, state, kind, return_sign, period_order) values (17, 2, 115, 1, 1, 0, 4);
insert into form_data(id, form_template_id, department_report_period_id, state, kind, return_sign, period_order) values (18, 2, 115, 1, 1, 0, 5);
insert into form_data(id, form_template_id, department_report_period_id, state, kind, return_sign, period_order) values (19, 2, 115, 1, 1, 0, 5); /* Повтор для теста */
insert into form_data(id, form_template_id, department_report_period_id, state, kind, return_sign, period_order) values (20, 2, 115, 1, 1, 0, 6);

insert into form_data_performer(form_data_id, name, phone, PRINT_DEPARTMENT_ID, REPORT_DEPARTMENT_NAME) values (11, 'Bank', '', 1, 'Bank/Uralsib');
insert into form_data_performer(form_data_id, name, phone, PRINT_DEPARTMENT_ID, REPORT_DEPARTMENT_NAME) values (12, 'Bank', '', 2, 'BankTB1');
insert into form_data_performer(form_data_id, name, phone, PRINT_DEPARTMENT_ID, REPORT_DEPARTMENT_NAME) values (13, 'Bank', '', 3, 'BankTB1k/Uralsib');

insert into form_data(id, form_template_id, department_report_period_id, state, kind, return_sign, period_order) values (301, 1, 301, 1, 1, 0, 1);
insert into form_data(id, form_template_id, department_report_period_id, state, kind, return_sign, period_order) values (302, 1, 302, 1, 1, 0, 2);
insert into form_data(id, form_template_id, department_report_period_id, state, kind, return_sign, period_order) values (303, 1, 303, 1, 1, 0, 3);
insert into form_data(id, form_template_id, department_report_period_id, state, kind, return_sign, period_order) values (304, 1, 304, 1, 1, 1, 4);
insert into form_data(id, form_template_id, department_report_period_id, state, kind, return_sign, period_order) values (305, 1, 305, 1, 1, 0, 5);
insert into form_data(id, form_template_id, department_report_period_id, state, kind, return_sign, period_order) values (306, 1, 306, 1, 1, 0, 6);
insert into form_data(id, form_template_id, department_report_period_id, state, kind, return_sign, period_order) values (307, 1, 307, 1, 1, 0, 7);
insert into form_data(id, form_template_id, department_report_period_id, state, kind, return_sign, period_order) values (308, 1, 307, 1, 2, 0, 7);

-- Для проверки изменения количества строк в табличной части НФ
insert into form_data(id, form_template_id, department_report_period_id, state, kind, return_sign) values (1000, 1, 101, 1, 3, 0);

-- Для проверки метода getFormDataListByTemplateId
insert into form_data(id, form_template_id, department_report_period_id, state, kind, return_sign, period_order) values (400, 4, 120, 2, 1, 0, 1);
insert into form_data(id, form_template_id, department_report_period_id, state, kind, return_sign, period_order) values (401, 4, 222, 1, 1, 0, 3);
insert into form_data(id, form_template_id, department_report_period_id, state, kind, return_sign, period_order) values (402, 4, 303, 1, 1, 0, 2);
insert into form_data(id, form_template_id, department_report_period_id, state, kind, return_sign, period_order) values (403, 4, 420, 1, 1, 0, 1);