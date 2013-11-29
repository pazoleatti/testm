insert into form_template (id, type_id, data_rows, version, is_active, edition, numbered_columns, fixed_rows, name, fullname, code) values (1, 1, null, '0.1', 1, 1, 0, 1, 'name_1', 'fullname_1', 'code_1');
insert into form_template (id, type_id, data_rows, version, is_active, edition, numbered_columns, fixed_rows, name, fullname, code) values (2, 2, null, '0.1', 0, 1, 1, 0, 'name_2', 'fullname_2', 'code_2');

insert into form_column (id, name, form_template_id, ord, alias, type, width, precision, max_length, checking)
	values (1, 'Строковый столбец', 1, 1, 'stringColumn', 'S', 10, null, 500, 1);

insert into form_column (id, name, form_template_id, ord, alias, type, width, precision, max_length, checking)
	values (2, 'Числовой столбец', 1, 2, 'numericColumn', 'N', 10, 2, 15, 0);

insert into form_column (id, name, form_template_id, ord, alias, type, width, precision, checking)
	values (3, 'Дата-столбец', 1, 3, 'dateColumn', 'D', 10, null, 0);

insert into form_style (id, alias, form_template_id, font_color, back_color, italic, bold) values (1, 'alias1', 1, 3, 2, 1, 0);

insert into form_style (id, alias, form_template_id, font_color, back_color, italic, bold) values (2, 'alias2', 1, 2, 3, 0, 1);

insert into form_style (id, alias, form_template_id, font_color, back_color, italic, bold) values (3, 'alias3', 1, 1, 1, 1, 1);

--insert into form_script (id, form_template_id, name, ord, body, condition, per_row) values (1, 1, 'scriptName1', 1, '', '', 0);

--insert into event_script (event_code, script_id, ord) values (1, 1, 0);