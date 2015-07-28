INSERT INTO form_template (id, type_id, data_rows, version, monthly, fixed_rows, name, fullname, header, status)
  VALUES (329, 1, null, to_date('01.01.2013 12.01.01', 'DD.MM.YY HH.MI.SS'), 0, 1, 'name_1', 'fullname_1', 'header_1', 0);

insert INTO form_column (id, name, form_template_id, ord, alias, type, width, precision, max_length, checking)
	VALUES (3291, 'Строковый столбец', 329, 1, 'stringColumn', 'S', 10, null, 500, 1);
INSERT INTO form_column (id, name, form_template_id, ord, alias, type, width, precision, max_length, checking)
	VALUES (3292, 'Числовой столбец', 329, 2, 'numericColumn', 'N', 10, 2, 15, 0);
INSERT INTO form_column (id, name, form_template_id, ord, alias, type, width, precision, checking, format)
	VALUES (3293, 'Дата-столбец', 329, 3, 'dateColumn', 'D', 10, null, 0, 1);
INSERT INTO form_column (id, name, form_template_id, ord, alias, type, width, precision, checking, numeration_row)
	VALUES (3294, 'Автонумеруемая графа', 329, 4, 'autoNumerationColumn', 'A', 10, null, 0, 1);

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
	c3294_rowspan NUMBER(3)
);

ALTER TABLE form_data_329 ADD CONSTRAINT form_data_329_pk PRIMARY KEY (id);
CREATE UNIQUE INDEX i_form_data_329_id ON form_data_329 (form_data_id, temporary, manual, ord);

INSERT INTO form_data(id, form_template_id, department_report_period_id, state, kind, return_sign) VALUES (329, 329, 101, 1, 3, 0);
INSERT INTO form_data(id, form_template_id, department_report_period_id, state, kind, return_sign) VALUES (3291, 329, 101, 1, 3, 0);

INSERT INTO form_data_329(id, form_data_id, temporary, manual, ord, alias, c3291, c3292, c3293, c3294)
  SELECT 1, 329, 0, 0, 1, trim('row_alias №1'), trim('number'), 636, null, null FROM DUAL UNION
  SELECT 2, 329, 0, 0, 2, null, trim('some string'), null, null, null FROM DUAL UNION

  SELECT 3, 329, 1, 0, 1, null, null, 666, null, null FROM DUAL UNION
  SELECT 4, 329, 1, 0, 2, null, trim('qwerty'), null, null, null FROM DUAL UNION
  SELECT 5, 329, 1, 0, 3, trim('total'), trim('sum'), 50, null, null FROM DUAL UNION

  SELECT 6, 329, 0, 1, 1, null, null, 1000, null, null FROM DUAL UNION

  SELECT 7, 3291, 1, 0, 1, null, trim('qwerty'), 666, null, null FROM DUAL UNION
  SELECT 8, 3291, 1, 0, 2, trim('some alias{wan}'), null, null, null, null FROM DUAL UNION
  SELECT 9, 3291, 1, 0, 3, trim('total'), trim('sum'), 50, null, null FROM DUAL;