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

insert into form_template (id, type_id, data_rows, version, monthly, fixed_rows, name, fullname, header, status)
  values (1, 1, null, to_date('01.01.2013 12.01.01', 'DD.MM.YY HH.MI.SS'), 0, 1, 'name_1', 'fullname_1', 'header_1', 0);
insert into form_template (id, type_id, data_rows, version, monthly, fixed_rows, name, fullname, header, status)
  values (2, 2, null, to_date('01.01.2014 12.01.01', 'DD.MM.YY HH.MI.SS'), 1, 0, 'name_2', 'fullname_2', 'header_2', 0);
insert into form_template (id, type_id, data_rows, version, monthly, fixed_rows, name, fullname, header, status)
  values (3, 2, null, date '2015-01-01', 0, 0, 'name_3', 'fullname_3', 'header_2', 1);
insert into form_template (id, type_id, data_rows, version, monthly, fixed_rows, name, fullname, header, status)
  values (4, 2, null, date '2016-01-01', 0, 0, 'name_4', 'fullname_4', 'header_4', 1);

insert into form_column (id, name, form_template_id, ord, alias, type, width, precision, max_length, checking, data_ord)
	values (1, 'Строковый столбец', 1, 1, 'stringColumn', 'S', 10, null, 500, 1, 0);
insert into form_column (id, name, form_template_id, ord, alias, type, width, precision, max_length, checking, data_ord)
	values (2, 'Числовой столбец', 1, 2, 'numericColumn', 'N', 10, 2, 15, 0, 1);
insert into form_column (id, name, form_template_id, ord, alias, type, width, precision, checking, format, data_ord)
	values (3, 'Дата-столбец', 1, 3, 'dateColumn', 'D', 10, null, 0, 1, 2);
insert into form_column (id, name, form_template_id, ord, alias, type, width, precision, checking, numeration_row, data_ord)
	values (4, 'Автонумеруемая графа', 1, 4, 'autoNumerationColumn', 'A', 10, null, 0, 1, 3);

insert into form_style (alias, form_template_id, font_color, back_color, italic, bold) values ('alias1', 1, 3, 2, 1, 0);
insert into form_style (alias, form_template_id, font_color, back_color, italic, bold) values ('alias2', 1, 2, 3, 0, 1);
insert into form_style (alias, form_template_id, font_color, back_color, italic, bold) values ('alias3', 1, 1, 1, 1, 1);

--insert into form_script (id, form_template_id, name, ord, body, condition, per_row) values (1, 1, 'scriptName1', 1, '', '', 0);
--insert into event_script (event_code, script_id, ord) values (1, 1, 0);