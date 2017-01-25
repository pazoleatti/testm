INSERT INTO department_type (id, name) VALUES (1, 'Банк');
INSERT INTO department_type (id, name) VALUES (2, 'Территориальный банк');
INSERT INTO department_type (id, name) VALUES (3, 'ЦСКО, ПЦП');
INSERT INTO department_type (id, name) VALUES (4, 'Управление');
INSERT INTO department_type (id, name) VALUES (5, '');

INSERT INTO department (id, name, parent_id, type, shortname, tb_index, sbrf_code, code) VALUES (1, 'Банк', NULL, 1, NULL, NULL, '12', 1);
INSERT INTO department (id, name, parent_id, type, shortname, tb_index, sbrf_code, code) VALUES (2, 'ТБ1', 1, 2, NULL, NULL, '23', 2);
INSERT INTO department (id, name, parent_id, type, shortname, tb_index, sbrf_code, code) VALUES (3, 'ТБ2', 1, 2, NULL, NULL, NULL, 3);

--TAX_TYPE
INSERT INTO tax_type (id, name) VALUES ('N', 'НДФЛ');
INSERT INTO tax_type (id, name) VALUES ('F', 'Сборы, взносы');

INSERT INTO tax_period(id, tax_type, year) VALUES (1, 'N', 2013);
INSERT INTO tax_period(id, tax_type, year) VALUES (11, 'N', 2012);
INSERT INTO tax_period(id, tax_type, year) VALUES (21, 'F', 2013);

INSERT INTO ref_book(id, name) VALUES (8, 'Коды, определяющие налоговый (отчётный) период');
INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES (21, 1, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);
INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES (22, 2, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, max_length) VALUES
  (25, 8, 'Код', 'CODE', 1, 0, NULL, NULL, 1, NULL, 2, 1, 1, 100);
INSERT INTO ref_book_value(record_id, attribute_id, string_value) VALUES (21, 25, '99');

INSERT INTO report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) VALUES (1, 'Transport report period 1', 1, 21, date '2013-01-01', date '2013-03-31', date '2013-01-01');
INSERT INTO report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) VALUES (2, 'Transport report period 2', 1, 22, date '2013-04-01', date '2013-06-30', date '2013-04-01');
INSERT INTO report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) VALUES (3, 'VAT report period 1'      , 21, 21, date '2013-01-01', date '2013-03-31', date '2013-01-01');

INSERT INTO department_report_period(id, department_id, report_period_id, is_active, is_balance_period) VALUES (1, 1, 1, 1, 0);
INSERT INTO department_report_period(id, department_id, report_period_id, is_active, is_balance_period) VALUES (2, 1, 2, 1, 0);
INSERT INTO department_report_period(id, department_id, report_period_id, is_active, is_balance_period) VALUES (3, 1, 3, 1, 0);
INSERT INTO department_report_period(id, department_id, report_period_id, is_active, is_balance_period) VALUES (5, 2, 1, 1, 0);
INSERT INTO department_report_period(id, department_id, report_period_id, is_active, is_balance_period) VALUES (6, 2, 2, 1, 0);