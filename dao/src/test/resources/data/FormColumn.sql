insert into form_type (id, name, tax_type, fixed_rows) values (1, 'FormType - Transport', 'T', 1);
insert into form (id, type_id, data_rows, version, is_active, edition, numbered_columns) values (1, 1, null, '0.1', 1, 1, 0);

insert into form_column (id, name, form_id, ord, alias, type, editable, width, precision, dictionary_code, group_name, max_length, checking)
	values (1, 'Строковый столбец', 1, 1, 'stringColumn', 'S', 1, 10, null, null, null, 500, 1);

insert into form_column (id, name, form_id, ord, alias, type, editable, width, precision, dictionary_code, group_name, checking)
	values (2, 'Числовой столбец', 1, 2, 'numericColumn', 'N', 1, 10, 2, null, null, 0);
	
insert into form_column (id, name, form_id, ord, alias, type, editable, width, precision, dictionary_code, group_name, checking)
	values (3, 'Дата-столбец', 1, 3, 'dateColumn', 'D', 1, 10, null, null, null, 0);
