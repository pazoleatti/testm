insert into declaration_template(id, edition, version, is_active, declaration_type_id) values (1, 1, '0.01', 1, 1);
insert into declaration_data(id, declaration_template_id, report_period_id, department_id, data, is_accepted) values (1, 1, 1, 2, null, 1);
insert into log_business(id, log_date, event_id, user_id, roles, declaration_data_id, form_data_id, user_department_id, note) values (1, date '2013-01-01', 1, 1, 'operator', 1, null, 1, 'the best note');
insert into log_business(id, log_date, event_id, user_id, roles, declaration_data_id, form_data_id, user_department_id, note) values (2, date '2013-01-01', 2, 1, 'operator', null, 1, 2, 'the best note');










