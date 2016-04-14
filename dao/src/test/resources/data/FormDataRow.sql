--создаем справочник
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES
(38, 'Да/Нет', 1, 0, 1, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES
(249,38,'Код','CODE',2,0,null,null,1,0,2,1,1,null,null,0,1);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES
(250,38,'Значение','VALUE',1,1,null,null,1,null,10,1,2,null,null,0,3);
INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) VALUES (182632, 182632, 38, to_date('01.01.12','DD.MM.RR'),0);
INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) VALUES (182633, 182633, 38, to_date('01.01.12','DD.MM.RR'),0);
INSERT INTO ref_book_value (record_id, attribute_id, string_value, number_value) values (182632, 249, null, 0);
INSERT INTO ref_book_value (record_id, attribute_id, string_value, number_value) values (182632, 250, 'Нет', null);
INSERT INTO ref_book_value (record_id, attribute_id, string_value, number_value) values (182633, 249, null, 1);
INSERT INTO ref_book_value (record_id, attribute_id, string_value, number_value) values (182633, 250, 'Да', null);

INSERT INTO form_template (id, type_id, data_rows, version, monthly, fixed_rows, name, fullname, header, status)
  VALUES (329, 1, null, to_date('01.01.2013 12.01.01', 'DD.MM.YY HH.MI.SS'), 0, 1, 'name_1', 'fullname_1', 'header_1', 0);
INSERT INTO form_template (id, type_id, data_rows, version, monthly, fixed_rows, name, fullname, header, status)
  VALUES (330, 1, null, to_date('01.01.2013 12.01.01', 'DD.MM.YY HH.MI.SS'), 0, 1, 'name_2', 'fullname_2', 'header_2', 0);
INSERT INTO form_template (id, type_id, data_rows, version, monthly, fixed_rows, name, fullname, header, status)
VALUES (331, 1, null, to_date('01.01.2013 12.01.01', 'DD.MM.YY HH.MI.SS'), 0, 1, 'name_3', 'fullname_3', 'header_3', 0);

INSERT INTO form_column (id, name, form_template_id, ord, alias, type, width, precision, max_length, checking, data_ord)
	VALUES (3291, 'Строковый столбец', 329, 1, 'stringColumn', 'S', 10, null, 500, 1, 0);
INSERT INTO form_column (id, name, form_template_id, ord, alias, type, width, precision, max_length, checking, data_ord)
	VALUES (3292, 'Числовой столбец', 329, 2, 'numericColumn', 'N', 10, 2, 15, 0, 1);
INSERT INTO form_column (id, name, form_template_id, ord, alias, type, width, precision, checking, format, data_ord)
	VALUES (3293, 'Дата-столбец', 329, 3, 'dateColumn', 'D', 10, null, 0, 1, 2);
INSERT INTO form_column (id, name, form_template_id, ord, alias, type, width, precision, checking, numeration_row, data_ord)
	VALUES (3294, 'Автонумеруемая графа', 329, 4, 'autoNumerationColumn', 'A', 10, null, 0, 1, 3);
INSERT INTO form_column (id, name, form_template_id, ord, alias, type, width, precision, checking, attribute_id, data_ord)
  VALUES (3295, 'Справочная графа', 329, 5, 'refBookColumn', 'R', 10, null, 0, 250, 4);

INSERT INTO form_column (id, name, form_template_id, ord, alias, type, width, precision, checking, attribute_id, data_ord)
  VALUES (3301, 'Справочная графа1', 330, 1, 'refBookColumn1', 'R', 10, null, 0, 250, 0);
INSERT INTO form_column (id, name, form_template_id, ord, alias, type, width, precision, checking, attribute_id, data_ord)
  VALUES (3302, 'Справочная графа2', 330, 2, 'refBookColumn2', 'R', 10, null, 0, 250, 1);

INSERT INTO form_column (id, name, form_template_id, ord, alias, type, width, precision, max_length, checking, data_ord)
VALUES (3311, 'Строковый столбец', 331, 1, 'stringColumn', 'S', 10, null, 500, 1, 0);
INSERT INTO form_column (id, name, form_template_id, ord, alias, type, width, precision, max_length, checking, data_ord)
VALUES (3312, 'Числовой столбец', 331, 2, 'numericColumn', 'N', 10, 2, 15, 0, 1);

INSERT INTO form_style (alias, form_template_id, font_color, back_color, italic, bold) VALUES ('alias1', 329, 3, 2, 1, 0);
INSERT INTO form_style (alias, form_template_id, font_color, back_color, italic, bold) VALUES ('alias2', 329, 2, 3, 0, 1);
INSERT INTO form_style (alias, form_template_id, font_color, back_color, italic, bold) VALUES ('alias3', 329, 1, 1, 1, 1);

INSERT INTO form_data(id, form_template_id, department_report_period_id, state, kind, return_sign, number_current_row) VALUES (329, 329, 101, 1, 3, 0, 2);
INSERT INTO form_data(id, form_template_id, department_report_period_id, state, kind, return_sign) VALUES (3291, 329, 101, 1, 3, 0);
INSERT INTO form_data(id, form_template_id, department_report_period_id, state, kind, return_sign, number_current_row) VALUES (3292, 329, 101, 1, 3, 0, 1);
INSERT INTO form_data(id, form_template_id, department_report_period_id, state, kind, return_sign, number_current_row) VALUES (330, 330, 101, 1, 3, 0, 2);
INSERT INTO form_data(id, form_template_id, department_report_period_id, state, kind, return_sign, number_current_row) VALUES (331, 331, 101, 1, 3, 0, 2);
INSERT INTO form_data(id, form_template_id, department_report_period_id, state, kind, return_sign) VALUES (3293, 329, 101, 1, 3, 0);

INSERT INTO form_data_row(id, form_data_id, temporary, manual, ord, alias, c0, c1, c2, c3, c4)
  SELECT 1, 329, 0, 0, 1, trim('row_alias №1'), trim('number'), '636', null, null, null FROM DUAL UNION
  SELECT 2, 329, 0, 0, 2, null, trim('some string'), null, null, null, null FROM DUAL UNION

  SELECT 11, 3293, 0, 0, 1, null, trim('s1'), '1', null, null, null FROM DUAL UNION
  SELECT 12, 3293, 0, 0, 2, null, trim('s2'), '2', null, null, null FROM DUAL UNION
  SELECT 13, 3293, 0, 0, 3, null, trim('s3'), '3', null, null, null FROM DUAL UNION
  SELECT 14, 3293, 0, 0, 4, null, trim('s4'), '4', null, null, null FROM DUAL UNION
  SELECT 15, 3293, 0, 0, 5, null, trim('s5'), '5', null, null, null FROM DUAL UNION
  SELECT 16, 3293, 0, 0, 6, null, trim('s6'), '6', null, null, null FROM DUAL UNION
  SELECT 17, 3293, 0, 0, 7, null, trim('s7'), '7', null, null, null FROM DUAL UNION
  SELECT 18, 3293, 0, 0, 8, null, trim('s8'), '8', null, null, null FROM DUAL UNION

  SELECT 3, 329, 1, 0, 1, null, null, '666', null, null, null FROM DUAL UNION
  SELECT 4, 329, 1, 0, 2, null, trim('qwerty'), null, null, null, null FROM DUAL UNION
  SELECT 5, 329, 1, 0, 3, trim('total'), trim('sum'), '50', null, null, null FROM DUAL UNION

  SELECT 6, 329, 0, 1, 1, null, null, '1000', null, null, null FROM DUAL UNION

  SELECT 7, 3292, 0, 0, 1, null, null, '1000', null, null, '182633' FROM DUAL UNION

  SELECT 8, 3291, 1, 0, 1, null, trim('qwerty'), '666', null, null, null FROM DUAL UNION
  SELECT 9, 3291, 1, 0, 2, trim('some alias{wan}'), null, null, null, null, null FROM DUAL UNION
  SELECT 10, 3291, 1, 0, 3, trim('total'), trim('sum'), '50', null, null, null FROM DUAL;

--     0   1   2   3   4
--   ---------------------
-- 1 |   |   |   |   |   |
--   |---|---|---|---|---|
-- 2 |   |   |   |   |   |
--   |---|-------|---|---|
-- 3 |   |       |   |   |
--   |---|       |---|---|
-- 4 |   |       |   |   |
--   |   |       |   |---|
-- 5 | o | o   o | o | o |
--   |---|       |---|---|
-- 6 | o | o   o | o | o |
--   |---|       |---|---|
-- 7 | o | o   o | o | o |
--   |---|-------|   |   |
-- 8 |   |   |   |   |   |
--   ---------------------
INSERT INTO form_data_row_span (id, form_data_id, temporary, manual, data_ord, ord, colspan, rowspan)
  SELECT 14, 3293, 0, 0, 0, 4, null, 2 FROM DUAL UNION
  SELECT 13, 3293, 0, 0, 1, 3, 2, 5 FROM DUAL UNION
  SELECT 14, 3293, 0, 0, 3, 4, null, 2  FROM DUAL UNION
  SELECT 17, 3293, 0, 0, 4, 7, null, 2 FROM DUAL UNION
  SELECT 17, 3293, 0, 0, 3, 7, null, 2 FROM DUAL;