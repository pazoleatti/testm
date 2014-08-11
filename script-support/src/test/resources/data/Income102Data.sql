-- tax period
INSERT INTO tax_period (id, tax_type, year) VALUES (1, 'T', 2002);
INSERT INTO ref_book(id, name) VALUES (8, 'Коды, определяющие налоговый (отчётный) период');
INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES (21, 1, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);
INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES (22, 2, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);

-- department
INSERT INTO department_type (id, name) VALUES (1, 'Банк');
INSERT INTO department_type (id, name) VALUES (2, 'Территориальный банк');
INSERT INTO department (id, name, parent_id, type, shortname, tb_index, sbrf_code) VALUES (1, 'банк', NULL, 1, NULL, NULL, NULL);
INSERT INTO department (id, name, parent_id, type, shortname, tb_index, sbrf_code) VALUES (2, 'банк2', 1, 2, NULL, NULL, NULL);

-- report period
INSERT INTO report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) VALUES (1, '2002 - 1 квартал', 1, 21, date '2013-01-01', date '2013-03-31', date '2013-01-01');
INSERT INTO report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) VALUES (2, '2002 - 2 квартал', 1, 22, date '2013-04-01', date '2013-06-30', date '2013-04-01');

-- income102 data
INSERT INTO income_102 (id, report_period_id,  opu_code,  total_sum, department_id) VALUES (1, 1, '2', 666, 1);
INSERT INTO income_102 (id, report_period_id,  opu_code,  total_sum, department_id) VALUES (2, 1, '2.1', 666, 1);
INSERT INTO income_102 (id, report_period_id,  opu_code,  total_sum, department_id) VALUES (3, 2, '2', 555, 1);
INSERT INTO income_102 (id, report_period_id,  opu_code,  total_sum, department_id) VALUES (4, 1, '3', 444, 1);