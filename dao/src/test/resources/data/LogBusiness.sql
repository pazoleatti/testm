insert into form_template (id, type_id, data_rows, version, fixed_rows, name, fullname, header, status)
  values (1, 1, null, to_date('01.01.2013 12.01.01', 'DD.MM.YY HH.MI.SS'), 1, 'name_1', 'fullname_1', 'header_1', 0);
insert into form_template (id, type_id, data_rows, version, fixed_rows, name, fullname, header, status)
  values (2, 2, null, date '2013-01-01', 0, 'name_2', 'fullname_2', 'header_2', 0);
insert into form_template (id, type_id, data_rows, version, fixed_rows, name, fullname, header, status)
  values (3, 2, null, date '2013-02-01', 0, 'name_3', 'fullname_3', 'header_2', 1);

insert into form_kind (id, name) values (3, 'Сводная');
insert into tax_period(id, tax_type, year) values (1, 'T', 2013);
insert into tax_period(id, tax_type, year) values (11, 'T', 2012);
insert into ref_book(id, name) values (8, 'Коды, определяющие налоговый (отчётный) период');
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (21, 1, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (22, 2, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (23, 3, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) values (1, 'Transport report period 1',  1, 21, date '2013-01-01', date '2013-03-31', date '2013-01-01');
insert into department_report_period(id, department_id, report_period_id, is_active, is_balance_period) values (1, 1, 1, 1, 0);

insert into declaration_template(id, name, version, declaration_type_id) values (1, 'Декларация 1', date '2014-01-01', 1);
insert into form_data(id, form_template_id, department_report_period_id, state, kind,  return_sign) values (1, 1, 1, 1, 3, 0);
insert into declaration_data(id, declaration_template_id, department_report_period_id, state) values (1, 1, 1, 3);
insert into log_business(id, log_date, event_id, user_login, roles, declaration_data_id, form_data_id, user_department_name, note) values (1, date '2013-01-01', 1, 'controlBank', 'operator', 1, null, 'А - департамент', 'the best note');
insert into log_business(id, log_date, event_id, user_login, roles, declaration_data_id, form_data_id, user_department_name, note) values (2, date '2013-01-01', 2, 'controlBank', 'operator', null, 1, 'Б - департамент', 'the best note');
