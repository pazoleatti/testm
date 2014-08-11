INSERT INTO form_kind (id, name) VALUES (2, 'Консолидированная');
INSERT INTO form_kind (id, name) VALUES (3, 'Сводная');

INSERT INTO department_type (id, name) VALUES (1, 'Банк');
INSERT INTO department_type (id, name) VALUES (2, 'Территориальный банк');

INSERT INTO department (id, name, parent_id, type, code) VALUES (1, 'А - департамент', NULL, 1, 1);
INSERT INTO department (id, name, parent_id, type, code) VALUES (2, 'Б - департамент', 1, 2, 2);

INSERT INTO form_type (id, name, tax_type) VALUES (1, 'А - тип', 'T');
INSERT INTO form_type (id, name, tax_type) VALUES (2, 'Б - тип', 'I');

INSERT INTO form_template (id, type_id, data_rows, version, fixed_rows, name, fullname, header)
  VALUES (1, 1, NULL, date '2013-01-01', 1,'name', 'fullname', 'header');
INSERT INTO form_template (id, type_id, data_rows, version, fixed_rows, name, fullname, header)
  VALUES (2, 2, NULL, date '2013-01-01', 1,'name', 'fullname', 'header');

INSERT INTO tax_period(id, tax_type, year) VALUES (1, 'T', 2013);
INSERT INTO ref_book(id, name) VALUES (8, 'Коды, определяющие налоговый (отчётный) период');
INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES (21, 1, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);
INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES (22, 2, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);

INSERT INTO report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) VALUES (1, '1 - период', 1, 21, date '2013-01-01', date '2013-03-31', date '2013-01-01');
INSERT INTO report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) VALUES (2, '2 - период', 1, 22, date '2013-04-01', date '2013-06-30', date '2013-04-01');

INSERT INTO form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) VALUES (1, 1, 1, 1, 2, 1, 0);
INSERT INTO form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) VALUES (2, 2, 2, 2, 3, 2, 0);

INSERT INTO form_data_performer (form_data_id, name, phone, print_department_id) VALUES (1, 'name1', 'phone1', 2);
