insert into form_type (id, name, tax_type) values (1, 'FormType - Transport', 'T');
insert into department (id, name, parent_id, type) values (1, 'Банк', null, 1);
insert into form_template (id, type_id, data_rows, version, is_active, edition, numbered_columns, fixed_rows, name, fullname, code) values (1, 1, null, '0.1', 1, 1, 0, 1, 'name', 'fullname', 'code');
insert into tax_period(id, tax_type, start_date, end_date) values (1, 'T', date '2013-01-01', date '2013-12-31');
insert into ref_book(id, name) values (8, 'Коды, определяющие налоговый (отчётный) период');
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (21, 1, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);
insert into report_period (id, name, is_active, months, tax_period_id, ord, department_id, dict_tax_period_id) values (1, 'Transport report period 1', 1, 3, 1, 1, 1, 21);
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (1, 1, 1, 1, 3, 1, 0);

insert into data_row(id, form_data_id, alias, ord, type) values (1, 1, 'testAlias', 1, 0);
insert into data_row(id, form_data_id, alias, ord, type) values (2, 1, null, 2, 0);
insert into data_row(id, form_data_id, alias, ord, type) values (3, 1, 'alias 3', 3, 0);
insert into data_row(id, form_data_id, alias, ord, type) values (4, 1, 'alias 4', 4, 0);

insert into form_column (id, name, form_template_id, ord, alias, type, width, precision, dictionary_code, group_name, max_length, checking)
	values (1, 'Строковый столбец', 1, 1, 'alias 1', 'S', 10, null, null, null, 500, 1);
insert into form_column (id, name, form_template_id, ord, alias, type, width, precision, dictionary_code, group_name, max_length, checking)
	values (2, 'Числовой столбец', 1, 1, 'alias 2', 'N', 10, 2, null, null, 15, 0);
insert into form_column (id, name, form_template_id, ord, alias, type, width, precision, dictionary_code, group_name, checking)
	values (3, 'Дата-столбец', 1, 1, 'alias 3' , 'D', 10, null, null, null, 0);
insert into form_column (id, name, form_template_id, ord, alias, type, width, precision, dictionary_code, group_name, max_length, checking)
	values (4, 'Строковый столбец', 1, 1, 'alias 4', 'S', 10, null, null, null, 500, 1);

insert into form_style (id, alias, form_template_id, font_color, back_color, italic, bold) values (1, 'alias 1', 1, 3, 2, 1, 0);
insert into form_style (id, alias, form_template_id, font_color, back_color, italic, bold) values (2, 'alias 2', 1, 2, 3, 0, 1);
insert into form_style (id, alias, form_template_id, font_color, back_color, italic, bold) values (3, 'alias 3', 1, 1, 1, 1, 1);

insert into cell_style (row_id, column_id, style_id) values (1, 1, 1);
insert into cell_style (row_id, column_id, style_id) values (2, 1, 2);