INSERT INTO ref_book (ID, NAME) VALUES (50, 'Оборотная ведомость (Форма 0409101-СБ)');
INSERT INTO ref_book (ID, NAME) VALUES (52, 'Отчет о прибылях и убытках (Форма 0409102-СБ)');
INSERT INTO ref_book (ID, NAME) VALUES (30, 'Подразделения');

INSERT INTO ref_book_attribute (id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 162, 30, 1, 'Имя', 'name', 1, NULL, NULL, 1, NULL, 10, 100);
INSERT INTO ref_book_attribute (id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 2, 30, 2, 'СБРФ КОД', 'sbrf_code', 1, NULL, NULL, 1, NULL, 10, 100);

INSERT INTO ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 501, 50,'Идентификатор отчетного периода','REPORT_PERIOD_ID', 2, 1, NULL, NULL, 1, 0, 10, 27);
INSERT INTO ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 502, 50,'Номер счета','ACCOUNT', 1, 2, NULL, NULL, 1,NULL, 10, 100);
INSERT INTO ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 510, 50,'Наименование счета','ACCOUNT_NAME', 1, 3, NULL, NULL, 1,NULL, 100, 100);
INSERT INTO ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 503, 50,'Входящие остатки по дебету','INCOME_DEBET_REMAINS', 2, 4, NULL, NULL, 1, 4, 10, 27);
INSERT INTO ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 504, 50,'Входящие остатки по кредиту','INCOME_CREDIT_REMAINS', 2, 5, NULL, NULL, 1, 4, 10, 27);
INSERT INTO ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 505, 50,'Обороты по дебету','DEBET_RATE', 2, 6, NULL, NULL, 1, 4, 10, 27);
INSERT INTO ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 506, 50,'Обороты по кредиту','CREDIT_RATE', 2, 7, NULL, NULL, 1, 4, 10, 27);
INSERT INTO ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 507, 50,'Исходящие остатки по дебету','OUTCOME_DEBET_REMAINS', 2, 8, NULL, NULL, 1, 4, 10, 27);
INSERT INTO ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 508, 50,'Исходящие остатки по кредиту','OUTCOME_CREDIT_REMAINS', 2, 9, NULL, NULL, 1, 4, 10, 27);
INSERT INTO ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width) VALUES
( 509, 50,'Код подразделения','DEPARTMENT_ID', 4, 10, 30, 162, 1, NULL, 10);

INSERT INTO ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 520, 52,'Идентификатор отчетного периода','REPORT_PERIOD_ID', 2, 1, NULL, NULL, 1, 0, 10, 27);
INSERT INTO ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 521, 52,'Код ОПУ','OPU_CODE', 1, 2, NULL, NULL, 1,NULL, 10, 100);
INSERT INTO ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 522, 52,'Сумма','TOTAL_SUM', 2, 3, NULL, NULL, 1, 4, 10, 27);
INSERT INTO ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 524, 52,'Наименование статьи','ITEM_NAME', 1, 5, NULL, NULL, 1,NULL, 100, 100);
INSERT INTO ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width) VALUES
( 525, 52,'Код подразделения','DEPARTMENT_ID', 4, 6, 30, 162,1, NULL, 10);

-- tax period
INSERT INTO tax_period(id, tax_type, year) VALUES (1, 'I', 2013);
INSERT INTO ref_book(id, NAME) VALUES (8, 'Коды, определяющие налоговый (отчётный) период');
INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES (21, 1, 8, to_date('01.01.2013', 'DD.MM.YYYY'), 0);
INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES (22, 2, 8, to_date('01.01.2013', 'DD.MM.YYYY'), 0);

-- department
INSERT INTO department (id, NAME, parent_id, TYPE, shortname, tb_index, sbrf_code, code) VALUES (1, 'банк1', NULL, 1, NULL, NULL, NULL, 1);
INSERT INTO department (id, NAME, parent_id, TYPE, shortname, tb_index, sbrf_code, code) VALUES (2, 'банк2', 1, 2, NULL, NULL, NULL, 2);

-- report period
INSERT INTO report_period (id, NAME, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) VALUES (1, '2002 - 1 квартал', 1, 21, date '2013-01-01', date '2013-03-31', date '2013-01-01');
INSERT INTO report_period (id, NAME, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) VALUES (2, '2002 - 2 квартал', 1, 22, date '2013-01-01', date '2013-06-30', date '2013-04-01');

-- income101 data
INSERT INTO income_101 (id, report_period_id, account, income_debet_remains, income_credit_remains, debet_rate, credit_rate,
                        outcome_debet_remains, outcome_credit_remains, department_id) VALUES (1, 1, '2', 3, 4, 5, 6, 7, 8, 1);
INSERT INTO income_101 (id, report_period_id, account, income_debet_remains, income_credit_remains, debet_rate, credit_rate,
                        outcome_debet_remains, outcome_credit_remains, department_id) VALUES (2, 1, '3', 4, 5, 6, 7, 8, 9, 1);
INSERT INTO income_101 (id, report_period_id, account, income_debet_remains, income_credit_remains, debet_rate, credit_rate,
                        outcome_debet_remains, outcome_credit_remains, department_id) VALUES (3, 2, '3', 5, 6, 7, 8, 9, 0, 1);
INSERT INTO income_101 (id, report_period_id, account, income_debet_remains, income_credit_remains, debet_rate, credit_rate,
                        outcome_debet_remains, outcome_credit_remains, department_id) VALUES (4, 1, '4', 5, 6, 7, 8, 9, 0, 2);

-- income102 data
INSERT INTO income_102 (id, report_period_id,  opu_code,  total_sum, department_id) VALUES (1, 1, '2', 666, 1);
INSERT INTO income_102 (id, report_period_id,  opu_code,  total_sum, department_id) VALUES (2, 1, '3', 666, 1);
INSERT INTO income_102 (id, report_period_id,  opu_code,  total_sum, department_id) VALUES (3, 2, '2', 555, 1);
INSERT INTO income_102 (id, report_period_id,  opu_code,  total_sum, department_id) VALUES (4, 1, '3.1', 444, 1);
INSERT INTO income_102 (id, report_period_id,  opu_code,  total_sum, department_id) VALUES (5, 1, '55', 444, 2);