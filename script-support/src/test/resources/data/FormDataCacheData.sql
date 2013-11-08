-- Справочники
insert into ref_book(id, name) values
(1, 'Книга');
insert into ref_book(id, name) values
(2, 'Человек');
insert into ref_book(id, name) values
(3, 'Библиотека');

insert into ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width) values
  (4, 2, 1, 'ФИО', 'name', 1, null, null, 1, null, 10);
insert into ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width) values
  (1, 1, 1, 'Наименование', 'name', 1, null, null, 1, null, 10);
insert into ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width) values
  (2, 1, 2, 'Количество страниц', 'order', 2, null, null, 1, 0, 10);
insert into ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width) values
  (3, 1, 4, 'Автор', 'author', 4, 2, 4, 1, null, 10);
insert into ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width) values
  (5, 1, 5, 'Вес', 'weight', 2, null, null, 1, 3, 10);

insert into ref_book_record(id, record_id, ref_book_id, version, status) values
  (1, 1, 1, to_date('01.01.2013', 'DD.MM.YYYY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
  (2, 1, 1, to_date('01.02.2013', 'DD.MM.YYYY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
  (3, 1, 1, to_date('01.03.2013', 'DD.MM.YYYY'), -1);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
  (4, 2, 1, to_date('01.01.2013', 'DD.MM.YYYY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
  (5, 1, 2, to_date('01.01.2013', 'DD.MM.YYYY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
  (6, 2, 2, to_date('01.01.2013', 'DD.MM.YYYY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
  (7, 2, 2, to_date('01.04.2013', 'DD.MM.YYYY'), 0);

insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (1, 1, 'Алиса в стране чудес', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (1, 2, null, 1113, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (1, 3, null, null, null, 5);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (1, 5, null, 0.25, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (2, 1, 'Алиса в стране', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (2, 2, null, 1213, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (2, 3, null, null, null, 7);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (2, 5, null, 0.1, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (4, 1, 'Вий', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (4, 2, null, 425, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (4, 3, null, null, null, 6);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (4, 5, null, 2.399, null, null);

insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (5, 4, 'Иванов И.И.', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (6, 4, 'Петров П.П.', null, null, null);
insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (7, 4, 'Петренко П.П.', null, null, null);

-- Подразделения
insert into DEPARTMENT (id, name, parent_id, type, shortname, tb_index, sbrf_code)
  values (1, 'Банк', null, 1, null, null, '12');
insert into DEPARTMENT (id, name, parent_id, type, shortname, tb_index, sbrf_code)
  values (2, 'ТБ1', 1, 2, null, null, '23');
insert into DEPARTMENT (id, name, parent_id, type, shortname, tb_index, sbrf_code)
  values (3, 'ТБ2', 1, 2, null, null, null);

-- FormType
insert into form_type (id, name, tax_type) values (1, 'FormType - Transport', 'T');

-- TaxPeriod
insert into tax_period(id, tax_type, start_date, end_date) values (1, 'T', date '2013-01-01', date '2013-12-31');

-- dict_tax_period_id
insert into ref_book(id, name) values (8, 'Коды, определяющие налоговый (отчётный) период');
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (21, 1, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);

-- ReportPeriod
insert into report_period (id, name, months, tax_period_id, ord, dict_tax_period_id) values (1, 'Transport report period 1', 3,  1, 1, 21);

-- FormTemplate
insert into form_template (id, type_id, data_rows, version, is_active, edition, numbered_columns, fixed_rows, name, fullname, code) values (1, 1, null, '0.1', 1, 1, 0, 1, 'name_1', 'fullname_1', 'code_1');

insert into form_style (id, alias, form_template_id, font_color, back_color, italic, bold) values (1, 'alias1', 1, 3, 2, 1, 0);

insert into form_style (id, alias, form_template_id, font_color, back_color, italic, bold) values (2, 'alias2', 1, 2, 3, 0, 1);

insert into form_style (id, alias, form_template_id, font_color, back_color, italic, bold) values (3, 'alias3', 1, 1, 1, 1, 1);

-- FormData
insert into form_data(id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (1, 1, 1, 1, 3, 1, 0);

-- FormColumn
insert into form_column (id, name, form_template_id, ord, alias, type, width, precision, group_name, max_length, checking)
	values (1, 'Строковый столбец', 1, 1, 'stringColumn', 'S', 10, null, null, 500, 1);

insert into form_column (id, name, form_template_id, ord, alias, type, width, precision, group_name, max_length, checking)
	values (2, 'Числовой столбец', 1, 2, 'numericColumn', 'N', 10, 2, null, 15, 0);

insert into form_column (id, name, form_template_id, ord, alias, type, width, precision, group_name, max_length, checking, attribute_id)
	values (3, 'Справочный столбец 1', 1, 2, 'referenceColumn1', 'R', 10, null, null, null, 0, 1);

insert into form_column (id, name, form_template_id, ord, alias, type, width, precision, group_name, max_length, checking, attribute_id)
	values (4, 'Справочный столбец 2', 1, 2, 'referenceColumn2', 'R', 10, null, null, null, 0, 2);

-- Строки
insert into data_row(id, form_data_id, alias, ord, type) values (1, 1, 'testAlias', 1, 0);
insert into data_row(id, form_data_id, alias, ord, type) values (2, 1, null, 2, 0);
insert into data_row(id, form_data_id, alias, ord, type) values (3, 1, 'alias 3', 3, 0);
insert into data_row(id, form_data_id, alias, ord, type) values (4, 1, 'alias 4', 4, 0);

-- Значения
insert into string_value (row_id, column_id, value) values (1, 1, 'string cell 1');
insert into numeric_value (row_id, column_id, value) values (1, 2, 111);
insert into numeric_value (row_id, column_id, value) values (1, 3, 1);
insert into numeric_value (row_id, column_id, value) values (1, 4, 2);

insert into string_value (row_id, column_id, value) values (2, 1, 'string cell 2');
insert into numeric_value (row_id, column_id, value) values (2, 2, 222);
insert into numeric_value (row_id, column_id, value) values (2, 3, 3);
insert into numeric_value (row_id, column_id, value) values (2, 4, 4);

insert into string_value (row_id, column_id, value) values (3, 1, 'string cell 3');
insert into numeric_value (row_id, column_id, value) values (3, 2, 333);
insert into numeric_value (row_id, column_id, value) values (3, 3, 5);
insert into numeric_value (row_id, column_id, value) values (3, 4, 6);

insert into string_value (row_id, column_id, value) values (4, 1, 'string cell 4');
insert into numeric_value (row_id, column_id, value) values (4, 2, 444);
insert into numeric_value (row_id, column_id, value) values (4, 3, 7);
insert into numeric_value (row_id, column_id, value) values (4, 4, 7);
