insert into declaration_template(id, edition, version, is_active, declaration_type_id) values (1, 1, '0.01', 1, 1);
insert into department (id, name, parent_id, type) values (1, 'Department name 1', null, 1);
insert into department (id, name, parent_id, type) values (2, 'Department name 5', 1, 2);
insert into department (id, name, parent_id, type) values (3, 'Department name 2', 1, 2);
insert into department (id, name, parent_id, type) values (4, 'Department name 3', 1, 2);
insert into department (id, name, parent_id, type) values (5, 'Department name 4', 1, 2);
insert into tax_period(id, tax_type, start_date, end_date) values (1, 'T', date '2013-01-01', date '2013-12-31');

-- для проверки get, getXmlData и hasXmlData
insert into ref_book(id, name) values (8, 'Коды, определяющие налоговый (отчётный) период');
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (21, 1, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);
insert into report_period (id, name, is_active, months, tax_period_id, ord, department_id, dict_tax_period_id) values (1, 'Transport report period 2', 1, 3, 1, 1, 2, 21);
insert into report_period (id, name, is_active, months, tax_period_id, ord, department_id, dict_tax_period_id) values (2, 'Transport report period 3', 0, 3, 1, 2, 2, 21);
insert into declaration_data(id, declaration_template_id, report_period_id, department_id, data, is_accepted) values (1, 1, 1, 2, 'test-data-string-1', 1);
insert into declaration_data(id, declaration_template_id, report_period_id, department_id, data, is_accepted) values (2, 1, 2, 4, null, 0);

-- для проверки setAccepted 
insert into report_period (id, name, is_active, months, tax_period_id, ord, department_id, dict_tax_period_id) values (3, 'Transport report period 1', 0, 3, 1, 3, 1, 21);
insert into report_period (id, name, is_active, months, tax_period_id, ord, department_id, dict_tax_period_id) values (4, 'Transport report period 4', 0, 3, 1, 4, 1, 21);
insert into declaration_data(id, declaration_template_id, report_period_id, department_id, data, is_accepted) values (3, 1, 3, 3, null, 1);
insert into declaration_data(id, declaration_template_id, report_period_id, department_id, data, is_accepted) values (4, 1, 4, 1, null, 0);

-- для проверки delete
insert into report_period (id, name, is_active, months, tax_period_id, ord, department_id, dict_tax_period_id) values (5, 'Transport report period 6', 0, 3, 1, 5, 5, 21);
insert into declaration_data(id, declaration_template_id, report_period_id, department_id, data, is_accepted) values (5, 1, 5, 5, null, 1);

-- для проверки saveNew
insert into report_period (id, name, is_active, months, tax_period_id, ord, department_id, dict_tax_period_id) values (6, 'Transport report period 5', 0, 3, 1, 6, 5, 21);

-- для проверки setXmlData
insert into report_period (id, name, is_active, months, tax_period_id, ord, department_id, dict_tax_period_id) values (7, 'Transport report period 7', 0, 3, 1, 7, 5, 21);
insert into declaration_data(id, declaration_template_id, report_period_id, department_id, data, is_accepted) values (7, 1, 7, 5, null, 1);







