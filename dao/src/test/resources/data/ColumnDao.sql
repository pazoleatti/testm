INSERT INTO color (id, name, r, g, b, hex) VALUES (0, 'Черный', 0, 0, 0, '#000000');
INSERT INTO color (id, name, r, g, b, hex) VALUES (4,'Белый', 255, 255, 255, '#FFFFFF');
INSERT INTO color (id, name, r, g, b, hex) VALUES (1,'Светло - желтый', 255, 255, 153, '#FFFF99');
INSERT INTO color (id, name, r, g, b, hex) VALUES (2,'Светло - коричневый', 255, 204, 153, '#FFCC99');
INSERT INTO color (id, name, r, g, b, hex) VALUES (3,'Светло - голубой', 204, 255, 255, '#CCFFFF');
INSERT INTO color (id, name, r, g, b, hex) VALUES (5,'Темно - серый', 149, 149, 149, '#959595');
INSERT INTO color (id, name, r, g, b, hex) VALUES (6,'Серый', 192, 192, 192, '#C0C0C0');
INSERT INTO color (id, name, r, g, b, hex) VALUES (7,'Голубой', 153, 204, 255, '#99CCFF');
INSERT INTO color (id, name, r, g, b, hex) VALUES (8,'Светло - красный', 240, 128, 128, '#F08080');
INSERT INTO color (id, name, r, g, b, hex) VALUES (9,'Светло - оранжевый', 255, 220, 130, '#FFDC82');
INSERT INTO color (id, name, r, g, b, hex) VALUES (10,'Красный', 255, 0, 0, '#FF0000');
INSERT INTO color (id, name, r, g, b, hex) VALUES (11,'Синий', 0, 0, 255, '#0000FF');
INSERT INTO color (id, name, r, g, b, hex) VALUES (12,'Светло - зеленый', 152, 251, 152, '#98FB98');
INSERT INTO color (id, name, r, g, b, hex) VALUES (13,'Темно - зеленый', 0, 108, 0, '#006C00');

INSERT INTO ref_book(id, name) VALUES (1, 'Книга');
INSERT INTO ref_book(id, name) VALUES  (2, 'Человек');
INSERT INTO ref_book(id, name) VALUES (8, 'Коды, определяющие налоговый (отчётный) период');
INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES (21, 1, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);

INSERT INTO ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, is_unique, max_length) VALUES
  (4, 2, 1, 'ФИО', 'name', 1, NULL, NULL, 1, NULL, 10, 0, 40);
INSERT INTO ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, is_unique, max_length) VALUES
  (1, 1, 1, 'Наименование', 'name', 1, NULL, NULL, 1, NULL, 10, 1, 40);
INSERT INTO ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, is_unique, max_length) VALUES
  (2, 1, 2, 'Количество страниц', 'order', 2, NULL, NULL, 1, 0, 10, 2, 5);
INSERT INTO ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, is_unique, max_length) VALUES
  (3, 1, 4, 'Автор', 'author', 4, 2, 4, 1, NULL, 10, 1, NULL);
INSERT INTO ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, is_unique, max_length) VALUES
  (5, 1, 5, 'Вес', 'weight', 2, NULL, NULL, 1, 3, 10, 1, 10);
INSERT INTO ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, is_unique, max_length) VALUES
  (9, 1, 6, 'Ничего', 'NULL', 1, NULL, NULL, 1, NULL, 10, 0, 50);
INSERT INTO ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, is_unique, max_length) VALUES
  (10, 1, 7, 'Уникальный атрибут', 'unique', 1, NULL, NULL, 1, NULL, 10, 1, 50);
INSERT INTO ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, is_unique, max_length) VALUES
  (11, 1, 8, 'Еще один уникальный атрибут', 'unique_2', 1, NULL, NULL, 1, NULL, 10, 1, 50);

INSERT INTO tax_type (id, name) VALUES ('T', 'Транспортный');
INSERT INTO tax_type (id, name) VALUES ('I', 'Прибыль');

INSERT INTO form_type (id, name, tax_type, status, code) VALUES (1, 'FormType - Transport', 'T', 0, 'code_1');
INSERT INTO form_type (id, name, tax_type, status, code) VALUES (2, 'FormType - Income', 'I', 0, 'code_2');

INSERT INTO form_template (id, type_id, data_rows, version, monthly, fixed_rows, name, fullname, header, status)
  VALUES (1, 1, NULL, to_date('01.01.2013 12.01.01', 'DD.MM.YY HH.MI.SS'), 0, 1, 'name_1', 'fullname_1', 'header_1', 0);
INSERT INTO form_template (id, type_id, data_rows, version, monthly, fixed_rows, name, fullname, header, status)
  VALUES (2, 2, NULL, to_date('01.01.2014 12.01.01', 'DD.MM.YY HH.MI.SS'), 1, 0, 'name_2', 'fullname_2', 'header_2', 0);
INSERT INTO form_template (id, type_id, data_rows, version, monthly, fixed_rows, name, fullname, header, status)
  VALUES (3, 2, NULL, date '2015-01-01', 0, 0, 'name_3', 'fullname_3', 'header_2', 1);
INSERT INTO form_template (id, type_id, data_rows, version, monthly, fixed_rows, name, fullname, header, status)
  VALUES (4, 2, NULL, date '2016-01-01', 0, 0, 'name_4', 'fullname_4', 'header_4', 1);

INSERT INTO form_column (id, name, form_template_id, ord, alias, type, width, precision, max_length, checking, data_ord, short_name)
	values (1, 'Строковый столбец', 1, 1, 'stringColumn', 'S', 10, NULL, 500, 1, 0, 'Стр. столбец');
INSERT INTO form_column (id, name, form_template_id, ord, alias, type, width, precision, max_length, checking, data_ord, short_name)
	values (2, 'Числовой столбец', 1, 2, 'numericColumn', 'N', 10, 2, 15, 0, 1, 'Чис. столбец');
INSERT INTO form_column (id, name, form_template_id, ord, alias, type, width, precision, checking, format, data_ord, short_name)
	values (3, 'Дата-столбец', 1, 3, 'dateColumn', 'D', 10, NULL, 0, 1, 2, 'Д-столбец');
INSERT INTO form_column (id, name, form_template_id, ord, alias, type, width, precision, checking, numeration_row, data_ord, short_name)
	values (4, 'Автонумеруемая графа', 1, 4, 'autoNumerationColumn', 'A', 10, NULL, 0, 1, 3, 'Авт. графа');

INSERT INTO form_style (alias, form_template_id, font_color, back_color, italic, bold) VALUES ('alias1', 1, 3, 2, 1, 0);
INSERT INTO form_style (alias, form_template_id, font_color, back_color, italic, bold) VALUES ('alias2', 1, 2, 3, 0, 1);
INSERT INTO form_style (alias, form_template_id, font_color, back_color, italic, bold) VALUES ('alias3', 1, 1, 1, 1, 1);

--INSERT INTO form_script (id, form_template_id, name, ord, body, condition, per_row) VALUES (1, 1, 'scriptName1', 1, '', '', 0);
--INSERT INTO event_script (event_code, script_id, ord) VALUES (1, 1, 0);

-- данные экземпляров НФ
INSERT INTO department_type (id, name) VALUES (1, 'Банк');
INSERT INTO department (id, name, parent_id, type, shortname, tb_index, sbrf_code, region_id, code, garant_use) VALUES (1, 'Банк', NULL, 1, NULL, NULL, '12', 1, 1, 0);
INSERT INTO tax_period(id, tax_type, year) VALUES (1, 'T', 2013);
INSERT INTO report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) VALUES (1, 'первый квартал',  1, 21, date '2013-01-01', date '2013-03-31', date '2013-01-01');
INSERT INTO department_report_period(id, department_id, report_period_id, is_active, is_balance_period) VALUES (101, 1, 1, 1, 0);
INSERT INTO form_kind (id, name) VALUES (3, 'Сводная');
INSERT INTO form_data(id, form_template_id, department_report_period_id, state, kind, return_sign) VALUES (1, 1, 101, 1, 3, 0);
INSERT INTO form_data(id, form_template_id, department_report_period_id, state, kind, return_sign) VALUES (2, 1, 101, 1, 3, 0);

INSERT INTO form_data_row(id, form_data_id, temporary, manual, ord, alias, c0, c1, c2, c3, c4)
  SELECT 1, 1, 0, 0, 1, trim('row_alias №1'), 'string1', '636.2', NULL, NULL, NULL FROM DUAL UNION
  SELECT 2, 1, 0, 0, 2, NULL, 'string2', NULL, NULL, NULL, NULL FROM DUAL UNION
  SELECT 3, 1, 1, 0, 1, NULL, NULL, '666', NULL, NULL, NULL FROM DUAL UNION
  SELECT 4, 1, 1, 0, 2, NULL, 'string3', NULL, NULL, NULL, NULL FROM DUAL UNION
  SELECT 5, 1, 1, 0, 3, trim('total'), trim('sum'), trim('50'), NULL, NULL, trim(':)') FROM DUAL UNION
  SELECT 6, 1, 0, 1, 1, NULL, NULL, trim('1000'), NULL, NULL, NULL FROM DUAL UNION
  SELECT 7, 2, 0, 0, 1, NULL, NULL, trim('1000'), NULL, NULL, trim('182633') FROM DUAL UNION
  SELECT 8, 2, 1, 0, 1, NULL, trim('qwerty'), trim('666'), NULL, NULL, NULL FROM DUAL UNION
  SELECT 9, 2, 1, 0, 2, trim('some alias{wan}'), NULL, NULL, NULL, NULL, NULL FROM DUAL UNION
  SELECT 10, 2, 1, 0, 3, trim('total'), trim('sum'), trim('50'), NULL, NULL, NULL FROM DUAL;