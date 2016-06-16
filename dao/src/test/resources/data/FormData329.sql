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

insert INTO form_column (id, name, form_template_id, ord, alias, type, width, precision, max_length, checking)
	VALUES (3291, 'Строковый столбец', 329, 1, 'stringColumn', 'S', 10, null, 500, 1);
INSERT INTO form_column (id, name, form_template_id, ord, alias, type, width, precision, max_length, checking)
	VALUES (3292, 'Числовой столбец', 329, 2, 'numericColumn', 'N', 10, 2, 15, 0);
INSERT INTO form_column (id, name, form_template_id, ord, alias, type, width, precision, checking, format)
	VALUES (3293, 'Дата-столбец', 329, 3, 'dateColumn', 'D', 10, null, 0, 1);
INSERT INTO form_column (id, name, form_template_id, ord, alias, type, width, precision, checking, numeration_row)
	VALUES (3294, 'Автонумеруемая графа', 329, 4, 'autoNumerationColumn', 'A', 10, null, 0, 1);
INSERT INTO form_column (id, name, form_template_id, ord, alias, type, width, precision, checking, attribute_id)
  VALUES (3295, 'Справочная графа', 329, 5, 'refBookColumn', 'R', 10, null, 0, 250);

INSERT INTO form_column (id, name, form_template_id, ord, alias, type, width, precision, checking, attribute_id)
  VALUES (3301, 'Справочная графа1', 330, 1, 'refBookColumn1', 'R', 10, null, 0, 250);
INSERT INTO form_column (id, name, form_template_id, ord, alias, type, width, precision, checking, attribute_id)
  VALUES (3302, 'Справочная графа2', 330, 2, 'refBookColumn2', 'R', 10, null, 0, 250);

INSERT INTO form_style (id, alias, form_template_id, font_color, back_color, italic, bold) VALUES (3291, 'alias1', 329, 3, 2, 1, 0);
INSERT INTO form_style (id, alias, form_template_id, font_color, back_color, italic, bold) VALUES (3292, 'alias2', 329, 2, 3, 0, 1);
INSERT INTO form_style (id, alias, form_template_id, font_color, back_color, italic, bold) VALUES (3293, 'alias3', 329, 1, 1, 1, 1);

--создать таблицу
CREATE TABLE form_data_329 (
  id NUMBER(18) NOT NULL,
	form_data_id NUMBER(18) NOT NULL,
	temporary NUMBER(1) NOT NULL,
	manual NUMBER(1) NOT NULL,
	ord NUMBER(14) NOT NULL,
	alias VARCHAR2(20),

  c3291 VARCHAR2(2000),
	c3291_style_id NUMBER(9),
	c3291_editable NUMBER(1) DEFAULT 0,
	c3291_colspan NUMBER(3),
	c3291_rowspan NUMBER(3),

	c3292 DECIMAL(27, 10),
	c3292_style_id NUMBER(9),
	c3292_editable NUMBER(1) DEFAULT 0,
	c3292_colspan NUMBER(3),
	c3292_rowspan NUMBER(3),

	c3293 DATE,
	c3293_style_id NUMBER(9),
	c3293_editable NUMBER(1) DEFAULT 0,
	c3293_colspan NUMBER(3),
	c3293_rowspan NUMBER(3),

	c3294 DECIMAL(18),
	c3294_style_id NUMBER(9),
	c3294_editable NUMBER(1) DEFAULT 0,
	c3294_colspan NUMBER(3),
	c3294_rowspan NUMBER(3),

  c3295 DECIMAL(17),
  c3295_style_id NUMBER(9),
  c3295_editable NUMBER(1) DEFAULT 0,
  c3295_colspan NUMBER(3),
  c3295_rowspan NUMBER(3)
);
ALTER TABLE form_data_329 ADD CONSTRAINT form_data_329_pk PRIMARY KEY (id);
CREATE UNIQUE INDEX i_form_data_329_id ON form_data_329 (form_data_id, temporary, manual, ord);

CREATE TABLE form_data_330 (
  id NUMBER(18) NOT NULL,
  form_data_id NUMBER(18) NOT NULL,
  temporary NUMBER(1) NOT NULL,
  manual NUMBER(1) NOT NULL,
  ord NUMBER(14) NOT NULL,
  alias VARCHAR2(20),

  c3301 DECIMAL(17),
  c3301_style_id NUMBER(9),
  c3301_editable NUMBER(1) DEFAULT 0,
  c3301_colspan NUMBER(3),
  c3301_rowspan NUMBER(3),

  c3302 DECIMAL(17),
  c3302_style_id NUMBER(9),
  c3302_editable NUMBER(1) DEFAULT 0,
  c3302_colspan NUMBER(3),
  c3302_rowspan NUMBER(3)
);
ALTER TABLE form_data_330 ADD CONSTRAINT form_data_330_pk PRIMARY KEY (id);
CREATE UNIQUE INDEX i_form_data_330_id ON form_data_330 (form_data_id, temporary, manual, ord);

INSERT INTO form_data(id, form_template_id, department_report_period_id, state, kind, return_sign, number_current_row) VALUES (329, 329, 101, 1, 3, 0, 2);
INSERT INTO form_data(id, form_template_id, department_report_period_id, state, kind, return_sign) VALUES (3291, 329, 101, 1, 3, 0);
INSERT INTO form_data(id, form_template_id, department_report_period_id, state, kind, return_sign, number_current_row) VALUES (3292, 329, 101, 1, 3, 0, 1);
INSERT INTO form_data(id, form_template_id, department_report_period_id, state, kind, return_sign, number_current_row) VALUES (330, 330, 101, 1, 3, 0, 2);

INSERT INTO form_data_329(id, form_data_id, temporary, manual, ord, alias, c3291, c3292, c3293, c3294, c3295)
  SELECT 1, 329, 0, 0, 1, trim('row_alias №1'), trim('number'), 636, null, null, null FROM DUAL UNION
  SELECT 2, 329, 0, 0, 2, null, trim('some string'), null, null, null, null FROM DUAL UNION

  SELECT 3, 329, 1, 0, 1, null, null, 666, null, null, null FROM DUAL UNION
  SELECT 4, 329, 1, 0, 2, null, trim('qwerty'), null, null, null, null FROM DUAL UNION
  SELECT 5, 329, 1, 0, 3, trim('total'), trim('sum'), 50, null, null, null FROM DUAL UNION

  SELECT 6, 329, 0, 1, 1, null, null, 1000, null, null, null FROM DUAL UNION

  SELECT 7, 3292, 0, 0, 1, null, null, 1000, null, null, 182633 FROM DUAL UNION

  SELECT 8, 3291, 1, 0, 1, null, trim('qwerty'), 666, null, null, null FROM DUAL UNION
  SELECT 9, 3291, 1, 0, 2, trim('some alias{wan}'), null, null, null, null, null FROM DUAL UNION
  SELECT 10, 3291, 1, 0, 3, trim('total'), trim('sum'), 50, null, null, null FROM DUAL;


