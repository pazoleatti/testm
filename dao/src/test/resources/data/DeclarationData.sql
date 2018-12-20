insert into declaration_data(id, declaration_template_id, department_report_period_id, state, tax_organ_code, kpp, oktmo, note, file_name) values (1, 1, 102, 3, 'CD12', '123456789', 'oktmo', 'Первичка по', 'fileName');
insert into declaration_data(id, declaration_template_id, department_report_period_id, state) values (2, 1, 204, 1);
insert into declaration_data(id, declaration_template_id, department_report_period_id, state) values (3, 1, 303, 3);
insert into declaration_data(id, declaration_template_id, department_report_period_id, state) values (4, 1, 401, 1);
insert into declaration_data(id, declaration_template_id, department_report_period_id, state) values (5, 1, 501, 3);
insert into declaration_data(id, declaration_template_id, department_report_period_id, state, tax_organ_code, kpp, oktmo, note, file_name) values (123, 1, 605, 3, 'CD12', '123456789', 'oktmo', 'Первичка по', 'fileName');

insert into log_business(id, log_date, event_id, user_login, roles, declaration_data_id, user_department_name, note) values (1, date '2013-01-01', 1, 'controlBank', 'operator', 1, 'А - департамент', null);
insert into log_business(id, log_date, event_id, user_login, roles, declaration_data_id, user_department_name, note) values (2, date '2013-01-01', 1, 'controlBank', 'operator', 2, 'А - департамент', null);
insert into log_business(id, log_date, event_id, user_login, roles, declaration_data_id, user_department_name, note) values (3, date '2013-01-01', 1, 'controlBank', 'operator', 3, 'А - департамент', null);
insert into log_business(id, log_date, event_id, user_login, roles, declaration_data_id, user_department_name, note) values (4, date '2013-01-01', 1, 'controlBank', 'operator', 4, 'А - департамент', null);
insert into log_business(id, log_date, event_id, user_login, roles, declaration_data_id, user_department_name, note) values (5, date '2013-01-01', 1, 'controlBank', 'operator', 5, 'А - департамент', null);
insert into log_business(id, log_date, event_id, user_login, roles, declaration_data_id, user_department_name, note) values (123, date '2013-01-01', 1, 'controlBank', 'operator', 123, 'А - департамент', null);







