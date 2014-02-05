insert into department (id, name, parent_id, type) values (1, 'А - департамент', null, 1);
insert into department (id, name, parent_id, type) values (2, 'Б - департамент', 1, 2);

insert into form_type (id, name, tax_type) values (1, 'А - тип', 'T');
insert into form_type (id, name, tax_type) values (2, 'Б - тип', 'I');

insert into form_template (id, type_id, data_rows, version, is_active, edition, numbered_columns, fixed_rows, name, fullname, code)
  values (1, 1, null, date '2013-01-01', 1, 1, 1, 1, 'name', 'fullname', 'code');
insert into form_template (id, type_id, data_rows, version, is_active, edition, numbered_columns, fixed_rows, name, fullname, code)
  values (2, 2, null, date '2013-01-01', 1, 1, 1, 1, 'name', 'fullname', 'code');

insert into tax_period(id, tax_type, year) values (1, 'T', 2013);
insert into ref_book(id, name) values (8, 'Коды, определяющие налоговый (отчётный) период');
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (21, 1, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (22, 2, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);

insert into report_period (id, name, tax_period_id, ord, dict_tax_period_id, start_date, end_date, calendar_start_date) values (1, '1 - период', 1, 1, 21, date '2013-01-01', date '2013-03-31', date '2013-01-01');
insert into report_period (id, name, tax_period_id, ord, dict_tax_period_id, start_date, end_date, calendar_start_date) values (2, '2 - период', 1, 2, 22, date '2013-04-01', date '2013-06-30', date '2013-04-01');

insert into form_data (id, form_template_id, department_id, print_department_id, state, kind, report_period_id, return_sign) values (1, 1, 1, 1, 1, 2, 1, 0);
insert into form_data (id, form_template_id, department_id, print_department_id, state, kind, report_period_id, return_sign) values (2, 2, 2, 2, 2, 3, 2, 0);

insert into form_data_signer (id, form_data_id, name, position, ord) values (1, 1, 'name1', 'position1', 1);
insert into form_data_signer (id, form_data_id, name, position, ord) values (2, 1, 'name2', 'position2', 2);
insert into form_data_signer (id, form_data_id, name, position, ord) values (3, 2, 'name3', 'position3', 1);
insert into form_data_signer (id, form_data_id, name, position, ord) values (4, 2, 'name4', 'position4', 2);
