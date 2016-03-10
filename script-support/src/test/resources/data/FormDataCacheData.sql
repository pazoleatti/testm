-- Типы налоговых форм
INSERT INTO form_kind (id, name) VALUES (1, 'Первичная');
INSERT INTO form_kind (id, name) VALUES (2, 'Консолидированная');
INSERT INTO form_kind (id, name) VALUES (3, 'Сводная');
INSERT INTO form_kind (id, name) VALUES (4, 'Форма УНП');
INSERT INTO form_kind (id, name) VALUES (5, 'Выходная');

-- Справочники
INSERT INTO ref_book(id, name) VALUES (1, 'Книга');
INSERT INTO ref_book(id, name) VALUES (2, 'Человек');
INSERT INTO ref_book(id, name) VALUES (3, 'Библиотека');

INSERT INTO ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, max_length) VALUES
  (4, 2, 1, 'ФИО', 'name', 1, NULL, NULL, 1, NULL, 10, 500);
INSERT INTO ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, max_length) VALUES
  (1, 1, 1, 'Наименование', 'name', 1, NULL, NULL, 1, NULL, 10, 500);
INSERT INTO ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, max_length) VALUES
  (2, 1, 2, 'Количество страниц', 'order', 2, NULL, NULL, 1, 0, 10, 5);
INSERT INTO ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width) VALUES
  (3, 1, 4, 'Автор', 'author', 4, 2, 4, 1, NULL, 10);
INSERT INTO ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, max_length) VALUES
  (5, 1, 5, 'Вес', 'weight', 2, NULL, NULL, 1, 3, 10, 10);

INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES
  (1, 1, 1, to_date('01.01.2013', 'DD.MM.YYYY'), 0);
INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES
  (2, 1, 1, to_date('01.02.2013', 'DD.MM.YYYY'), 0);
INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES
  (3, 1, 1, to_date('01.03.2013', 'DD.MM.YYYY'), -1);
INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES
  (4, 2, 1, to_date('01.01.2013', 'DD.MM.YYYY'), 0);
INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES
  (5, 1, 2, to_date('01.01.2013', 'DD.MM.YYYY'), 0);
INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES
  (6, 2, 2, to_date('01.01.2013', 'DD.MM.YYYY'), 0);
INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES
  (7, 2, 2, to_date('01.04.2013', 'DD.MM.YYYY'), 0);

INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
  (1, 1, 'Алиса в стране чудес', NULL, NULL, NULL);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
  (1, 2, NULL, 1113, NULL, NULL);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
  (1, 3, NULL, NULL, NULL, 5);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
  (1, 5, NULL, 0.25, NULL, NULL);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
  (2, 1, 'Алиса в стране', NULL, NULL, NULL);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
  (2, 2, NULL, 1213, NULL, NULL);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
  (2, 3, NULL, NULL, NULL, 7);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
  (2, 5, NULL, 0.1, NULL, NULL);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
  (4, 1, 'Вий', NULL, NULL, NULL);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
  (4, 2, NULL, 425, NULL, NULL);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
  (4, 3, NULL, NULL, NULL, 6);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
  (4, 5, NULL, 2.399, NULL, NULL);

INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
  (5, 4, 'Иванов И.И.', NULL, NULL, NULL);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
  (6, 4, 'Петров П.П.', NULL, NULL, NULL);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
  (7, 4, 'Петренко П.П.', NULL, NULL, NULL);


INSERT INTO department_type (id, name) VALUES (1, 'Банк');
INSERT INTO department_type (id, name) VALUES (2, 'Территориальный банк');

-- Подразделения
INSERT INTO department (id, name, parent_id, type, shortname, tb_index, sbrf_code, code)
  VALUES (1, 'Банк', NULL, 1, NULL, NULL, '12', 1);
INSERT INTO department (id, name, parent_id, type, shortname, tb_index, sbrf_code, code)
  VALUES (2, 'ТБ1', 1, 2, NULL, NULL, '23', 2);
INSERT INTO department (id, name, parent_id, type, shortname, tb_index, sbrf_code, code)
  VALUES (3, 'ТБ2', 1, 2, NULL, NULL, NULL, 3);

--TaxType
INSERT INTO tax_type (id, name) VALUES ('T', 'Транспортный');

-- FormType
INSERT INTO form_type (id, name, tax_type) VALUES (1, 'FormType - Transport', 'T');

-- TaxPeriod
INSERT INTO tax_period(id, tax_type, year) VALUES (1, 'T', 2013);

-- dict_tax_period_id
INSERT INTO ref_book(id, name) VALUES (8, 'Коды, определяющие налоговый (отчётный) период');
INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES (21, 1, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);

-- ReportPeriod
INSERT INTO report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) VALUES (1, 'Transport report period 1', 1, 21, date '2013-01-01', date '2013-03-31', date '2013-01-01');

-- FormTemplate
insert into form_template (id, type_id, data_rows, version, fixed_rows, name, fullname, header)
  values (1, 1, null, date '2013-01-01', 1, 'name_1', 'fullname_1', 'header_1');

INSERT INTO color (id, name, r, g, b, hex) VALUES (1,'Светло - желтый', 255, 255, 153, '#FFFF99');
INSERT INTO color (id, name, r, g, b, hex) VALUES (2,'Светло - коричневый', 255, 204, 153, '#FFCC99');
INSERT INTO color (id, name, r, g, b, hex) VALUES (3,'Светло - голубой', 204, 255, 255, '#CCFFFF');

INSERT INTO form_style (id, alias, form_template_id, font_color, back_color, italic, bold) VALUES (1, 'alias1', 1, 3, 2, 1, 0);
INSERT INTO form_style (id, alias, form_template_id, font_color, back_color, italic, bold) VALUES (2, 'alias2', 1, 2, 3, 0, 1);
INSERT INTO form_style (id, alias, form_template_id, font_color, back_color, italic, bold) VALUES (3, 'alias3', 1, 1, 1, 1, 1);

INSERT INTO department_report_period(id, department_id, report_period_id, is_active, is_balance_period) VALUES (1, 1, 1, 1, 0);

-- FormColumn
INSERT INTO form_column (id, name, form_template_id, ord, alias, type, width, precision, max_length, checking, data_ord)
	values (1, 'Строковый столбец', 1, 1, 'stringColumn', 'S', 10, NULL, 500, 1, 0);
INSERT INTO form_column (id, name, form_template_id, ord, alias, type, width, precision, max_length, checking, data_ord)
	values (2, 'Числовой столбец', 1, 2, 'numericColumn', 'N', 10, 2, 15, 0, 1);
INSERT INTO form_column (id, name, form_template_id, ord, alias, type, width, precision, max_length, checking, attribute_id, data_ord)
	values (3, 'Справочный столбец 1', 1, 2, 'referenceColumn1', 'R', 10, NULL, NULL, 0, 1, 2);
INSERT INTO form_column (id, name, form_template_id, ord, alias, type, width, precision, max_length, checking, attribute_id, data_ord)
	values (4, 'Справочный столбец 2', 1, 2, 'referenceColumn2', 'R', 10, NULL, NULL, 0, 2, 3);

-- FormData
INSERT INTO form_data(id, form_template_id, department_report_period_id, state, kind, return_sign) VALUES (1, 1, 1, 1, 3, 0);

INSERT INTO form_data_row(id, form_data_id, temporary, manual, ord, alias, c0, c1, c2, c3)
  SELECT 1, 1, 0, 0, 1, trim('testAlias'), trim('string cell 1'), 111, 1, 2 FROM DUAL UNION
  SELECT 2, 1, 0, 0, 2, null,              trim('string cell 2'), 222, 3, 4 FROM DUAL UNION
  SELECT 3, 1, 0, 0, 3, trim('alias 3'),   trim('string cell 3'), 333, 5, 6 FROM DUAL UNION
  SELECT 4, 1, 0, 0, 4, trim('alias 4'),   trim('string cell 4'), 444, 7, 8 FROM DUAL;