insert into form (id, type_id, data_rows, version, is_active, edition) values (1, 1, null, '0.1', 1, 1);

insert into form_column (id, name, form_id, ord, alias, type, editable, mandatory, width, precision, dictionary_code, group_name) 
	values (1, 'Строковый столбец', 1, 1, 'stringColumn', 'S', 1, 0, 10, null, null, null);

insert into form_column (id, name, form_id, ord, alias, type, editable, mandatory, width, precision, dictionary_code, group_name)
	values (2, 'Числовой столбец', 1, 2, 'numericColumn', 'N', 1, 0, 10, 2, null, null);
	
insert into form_column (id, name, form_id, ord, alias, type, editable, mandatory, width, precision, dictionary_code, group_name)
	values (3, 'Дата-столбец', 1, 3, 'dateColumn', 'D', 1, 0, 10, null, null, null);
	