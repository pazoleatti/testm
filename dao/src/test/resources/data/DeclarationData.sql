insert into declaration_template(id, edition, version, is_active, declaration_type_id) values (1, 1, '0.01', 1, 1);
insert into department (id, name, parent_id, type) values (1, 'Department name 1', null, 1);
insert into department (id, name, parent_id, type) values (2, 'Department name 5', 1, 2);
insert into department (id, name, parent_id, type) values (3, 'Department name 2', 1, 2);
insert into department (id, name, parent_id, type) values (4, 'Department name 3', 1, 2);
insert into department (id, name, parent_id, type) values (5, 'Department name 4', 1, 2);
insert into tax_period(id, tax_type, year) values (1, 'T', 2013);

-- для проверки get, getXmlData и hasXmlData
insert into ref_book(id, name) values (8, 'Коды, определяющие налоговый (отчётный) период');
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (21, 1, 8, date '2013-01-01', 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (22, 2, 8, date '2013-01-01', 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (23, 3, 8, date '2013-01-01', 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (24, 4, 8, date '2013-01-01', 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (25, 5, 8, date '2013-01-01', 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (26, 6, 8, date '2013-01-01', 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (27, 7, 8, date '2013-01-01', 0);
insert into report_period (id, name, tax_period_id, ord, dict_tax_period_id, start_date, end_date, calendar_start_date) values (1, 'Transport report period 2', 1, 1, 21, date '2013-01-01', date '2013-03-31', date '2013-01-01');
insert into report_period (id, name, tax_period_id, ord, dict_tax_period_id, start_date, end_date, calendar_start_date) values (2, 'Transport report period 3', 1, 2, 22, date '2013-04-01', date '2013-06-30', date '2013-04-01');
insert into declaration_data(id, declaration_template_id, report_period_id, department_id, data, is_accepted) values (1, 1, 1, 2, null, 1);
insert into declaration_data(id, declaration_template_id, report_period_id, department_id, data, is_accepted) values (2, 1, 2, 4, null, 0);

-- для проверки setAccepted 
insert into report_period (id, name, tax_period_id, ord, dict_tax_period_id, start_date, end_date, calendar_start_date) values (3, 'Transport report period 1', 1, 3, 23, date '2013-07-01', date '2013-09-30', date '2013-07-01');
insert into report_period (id, name, tax_period_id, ord, dict_tax_period_id, start_date, end_date, calendar_start_date) values (4, 'Transport report period 4', 1, 4, 24, date '2013-10-01', date '2013-12-31', date '2013-10-01');


insert into declaration_data(id, declaration_template_id, report_period_id, department_id, data, is_accepted) values (3, 1, 3, 3, null, 1);
insert into declaration_data(id, declaration_template_id, report_period_id, department_id, data, is_accepted) values (4, 1, 4, 1, null, 0);

-- для проверки delete
insert into report_period (id, name, tax_period_id, ord, dict_tax_period_id, start_date, end_date, calendar_start_date) values (5, 'Transport report period 6', 1, 5, 25, date '2013-01-01', date '2013-03-31', date '2013-01-01');
insert into declaration_data(id, declaration_template_id, report_period_id, department_id, data, is_accepted) values (5, 1, 5, 5, null, 1);

-- для проверки saveNew
insert into report_period (id, name, tax_period_id, ord, dict_tax_period_id, start_date, end_date, calendar_start_date) values (6, 'Transport report period 5', 1, 6, 26, date '2013-01-01', date '2013-03-31', date '2013-01-01');

-- для проверки setXmlData
insert into report_period (id, name, tax_period_id, ord, dict_tax_period_id, start_date, end_date, calendar_start_date) values (7, 'Transport report period 7', 1, 7, 27, date '2013-01-01', date '2013-03-31', date '2013-01-01');
insert into declaration_data(id, declaration_template_id, report_period_id, department_id, data, is_accepted) values (7, 1, 7, 5, null, 1);







