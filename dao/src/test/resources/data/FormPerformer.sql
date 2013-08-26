insert into department (id, name, parent_id, type) values (1, 'А - департамент', null, 1);
insert into department (id, name, parent_id, type) values (2, 'Б - департамент', 1, 2);

insert into form_type (id, name, tax_type) values (1, 'А - тип', 'T');
insert into form_type (id, name, tax_type) values (2, 'Б - тип', 'I');

insert into form_template (id, type_id, data_rows, version, is_active, edition, numbered_columns, fixed_rows, name, fullname, code) values (1, 1, null, '0.1', 1, 1, 1, 1,'name', 'fullname', 'code');
insert into form_template (id, type_id, data_rows, version, is_active, edition, numbered_columns, fixed_rows, name, fullname, code) values (2, 2, null, '0.1', 1, 1, 1, 1,'name', 'fullname', 'code');

insert into tax_period(id, tax_type, start_date, end_date) values (1, 'T', date '2013-01-01', date '2013-12-31');
insert into ref_book(id, name) values (8, 'Коды, определяющие налоговый (отчётный) период');
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (21, 1, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);

insert into report_period (id, name, months, tax_period_id, ord, dict_tax_period_id) values (1, '1 - период', 3, 1, 1, 21);
insert into report_period (id, name, months, tax_period_id, ord, dict_tax_period_id) values (2, '2 - период', 3, 1, 2, 21);

insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (1, 1, 1, 1, 2, 1, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (2, 2, 2, 2, 3, 2, 0);

insert into form_data_performer (form_data_id, name, phone) values (1, 'name1', 'phone1');