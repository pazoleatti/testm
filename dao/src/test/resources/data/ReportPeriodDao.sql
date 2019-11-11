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

INSERT INTO tax_period(id, tax_type, year) VALUES (1, 'N', 2013);
INSERT INTO tax_period(id, tax_type, year) VALUES (11, 'N', 2012);
INSERT INTO tax_period(id, tax_type, year) VALUES (21, 'N', 2015);
INSERT INTO tax_period(id, tax_type, year) VALUES (31, 'N', 2018);
INSERT INTO tax_period(id, tax_type, year) VALUES (41, 'N', 2019);

INSERT INTO ref_book(id, name) VALUES (8, 'Коды, определяющие налоговый (отчётный) период');
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, max_length) VALUES
  (25, 8, 'Код', 'CODE', 1, 0, NULL, NULL, 1, NULL, 2, 1, 1, 100);
INSERT INTO report_period_type (id,code,name,start_date,end_date,calendar_start_date) VALUES (21, '99', 'первый квартал', DATE '1970-01-01', DATE '1970-03-31', DATE '1970-01-11');
INSERT INTO report_period_type (id,code,name,start_date,end_date,calendar_start_date) VALUES (22, '22', 'второй квартал', DATE '1970-04-01', DATE '1970-06-30', DATE '1970-04-04');

INSERT INTO report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date, form_type_id) VALUES (1, 'Transport report period 1', 1, 21, date '2013-01-01', date '2013-03-31', date '2013-01-01', 5);
INSERT INTO report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date, form_type_id) VALUES (2, 'Transport report period 2', 1, 22, date '2013-04-01', date '2013-06-30', date '2013-04-01', 3);
INSERT INTO report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date, form_type_id) VALUES (3, 'VAT report period 1'      , 21, 21, date '2013-01-01', date '2015-03-31', date '2015-01-01', 5);
INSERT INTO report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date, form_type_id) VALUES (4, 'Common period 1'      , 41, 21, date '2013-01-01', date '2015-03-31', date '2015-01-01', 5);

INSERT INTO department_report_period(id, department_id, report_period_id, is_active) VALUES (1, 1, 1, 1);
INSERT INTO department_report_period(id, department_id, report_period_id, is_active) VALUES (2, 1, 2, 1);
INSERT INTO department_report_period(id, department_id, report_period_id, is_active) VALUES (3, 1, 3, 1);
INSERT INTO department_report_period(id, department_id, report_period_id, is_active) VALUES (5, 2, 1, 1);
INSERT INTO department_report_period(id, department_id, report_period_id, is_active) VALUES (6, 2, 2, 1);
INSERT INTO department_report_period(id, department_id, report_period_id, is_active) VALUES (7, 1, 2, 0);
INSERT INTO department_report_period(id, department_id, report_period_id, is_active) VALUES (8, 1, 2, 0);

INSERT INTO department_report_period(id, department_id, report_period_id, is_active) VALUES (9, 3, 4, 0);
INSERT INTO department_report_period(id, department_id, report_period_id, is_active, correction_date) VALUES (10, 3, 4, 1, to_date('01.04.2019', 'DD.MM.YYYY'));