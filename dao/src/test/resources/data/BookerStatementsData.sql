INSERT INTO ref_book (ID, NAME) VALUES (50, 'Оборотная ведомость (Форма 0409101-СБ)');
INSERT INTO ref_book (ID, NAME) VALUES (52, 'Отчет о прибылях и убытках (Форма 0409102-СБ)');
INSERT INTO ref_book (ID, NAME) VALUES (30, 'Подразделения');
INSERT INTO ref_book (ID, NAME) VALUES (107, 'Периоды и подразделения БО');
INSERT INTO ref_book (ID, NAME) VALUES (106, 'Коды, определяющие период бухгалтерской отчетности');

INSERT INTO ref_book_attribute (id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 161, 30, 1, 'Имя', 'name', 1, NULL, NULL, 1, NULL, 10, 100);
INSERT INTO ref_book_attribute (id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 2, 30, 2, 'СБРФ КОД', 'sbrf_code', 1, NULL, NULL, 1, NULL, 10, 100);

INSERT INTO ref_book_attribute (id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 1061, 106, 1, 'Код', 'code', 1, NULL, NULL, 1, NULL, 10, 100);
INSERT INTO ref_book_attribute (id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 1062, 106, 2, 'Наименование', 'name', 1, NULL, NULL, 1, NULL, 10, 100);

INSERT INTO ref_book_attribute (id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 1073, 107, 1, 'Код подразделения', 'DEPARTMENT_ID', 4, 30, 161, 1, NULL, 10, NULL);
INSERT INTO ref_book_attribute (id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 1072, 107, 2, 'Код периода бухгалтерской отчетности', 'ACCOUNT_PERIOD_ID', 4, 106, 1062, 1, NULL, 10, NULL);
INSERT INTO ref_book_attribute (id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 1071, 107, 3, 'Год', 'YEAR', 1, NULL, NULL, 1, NULL, 10, 100);

INSERT INTO ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 502, 50,'Номер счета','ACCOUNT', 1, 1, NULL, NULL, 1,NULL, 10, 100);
INSERT INTO ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 510, 50,'Наименование счета','ACCOUNT_NAME', 1, 2, NULL, NULL, 1,NULL, 100, 100);
INSERT INTO ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 503, 50,'Входящие остатки по дебету','INCOME_DEBET_REMAINS', 2, 3, NULL, NULL, 1, 4, 10, 23);
INSERT INTO ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 504, 50,'Входящие остатки по кредиту','INCOME_CREDIT_REMAINS', 2, 4, NULL, NULL, 1, 4, 10, 23);
INSERT INTO ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 505, 50,'Обороты по дебету','DEBET_RATE', 2, 5, NULL, NULL, 1, 4, 10, 23);
INSERT INTO ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 506, 50,'Обороты по кредиту','CREDIT_RATE', 2, 6, NULL, NULL, 1, 4, 10, 23);
INSERT INTO ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 507, 50,'Исходящие остатки по дебету','OUTCOME_DEBET_REMAINS', 2, 7, NULL, NULL, 1, 4, 10, 23);
INSERT INTO ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 508, 50,'Исходящие остатки по кредиту','OUTCOME_CREDIT_REMAINS', 2, 8, NULL, NULL, 1, 4, 10, 23);
INSERT INTO ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 511, 50,'Идентификатор периода и подразделения БО','ACCOUNT_PERIOD_ID', 4, 9, 107, 1071, 1, NULL, 10, NULL);

INSERT INTO ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 521, 52,'Код ОПУ','OPU_CODE', 1, 1, NULL, NULL, 1,NULL, 10, 100);
INSERT INTO ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 522, 52,'Сумма','TOTAL_SUM', 2, 2, NULL, NULL, 1, 4, 10, 23);
INSERT INTO ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 524, 52,'Наименование статьи','ITEM_NAME', 1, 3, NULL, NULL, 1,NULL, 100, 100);
INSERT INTO ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 527, 52,'Идентификатор периода и подразделения БО','ACCOUNT_PERIOD_ID', 4, 4, 107, 1071, 1, NULL, 10, NULL);

INSERT INTO department_type (id, name) VALUES (1, 'Банк');
INSERT INTO department_type (id, name) VALUES (2, 'Территориальный банк');

-- department
INSERT INTO department (id, NAME, parent_id, TYPE, shortname, tb_index, sbrf_code, code) VALUES (1, 'банк1', NULL, 1, NULL, NULL, NULL, 1);
INSERT INTO department (id, NAME, parent_id, TYPE, shortname, tb_index, sbrf_code, code) VALUES (2, 'банк2', 1, 2, NULL, NULL, NULL, 2);

insert into ref_book_record(id, record_id, ref_book_id, version, status) values (11, 11, 106, to_date('01.01.2013', 'DD.MM.YY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (12, 12, 106, to_date('01.01.2013', 'DD.MM.YY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (13, 13, 106, to_date('01.01.2013', 'DD.MM.YY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (14, 14, 106, to_date('01.01.2013', 'DD.MM.YY'), 0);

insert into ref_book_record(id, record_id, ref_book_id, version, status) values (1, 1, 107, to_date('01.01.2013', 'DD.MM.YY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (2, 2, 107, to_date('01.01.2013', 'DD.MM.YY'), 0);

insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (1, 1073, null, null, null, 1);

-- income101 data
INSERT INTO income_101 (id, account, income_debet_remains, income_credit_remains, debet_rate, credit_rate,
                        outcome_debet_remains, outcome_credit_remains, account_period_id) VALUES (1, '2', 3, 4, 5, 6, 7, 8, 1);
INSERT INTO income_101 (id, account, income_debet_remains, income_credit_remains, debet_rate, credit_rate,
                        outcome_debet_remains, outcome_credit_remains, account_period_id) VALUES (2, '3', 4, 5, 6, 7, 8, 9, 1);
INSERT INTO income_101 (id, account, income_debet_remains, income_credit_remains, debet_rate, credit_rate,
                        outcome_debet_remains, outcome_credit_remains, account_period_id) VALUES (3, '3', 5, 6, 7, 8, 9, 0, 1);
INSERT INTO income_101 (id, account, income_debet_remains, income_credit_remains, debet_rate, credit_rate,
                        outcome_debet_remains, outcome_credit_remains, account_period_id) VALUES (4, '4', 5, 6, 7, 8, 9, 0, 2);

-- income102 data
INSERT INTO income_102 (id, opu_code, total_sum, account_period_id) VALUES (1, '2', 666, 1);
INSERT INTO income_102 (id, opu_code, total_sum, account_period_id) VALUES (2, '3', 666, 1);
INSERT INTO income_102 (id, opu_code, total_sum, account_period_id) VALUES (3, '2', 555, 1);
INSERT INTO income_102 (id, opu_code, total_sum, account_period_id) VALUES (4, '3.1', 444, 1);
INSERT INTO income_102 (id, opu_code, total_sum, account_period_id) VALUES (5, '55', 444, 2);