-- tax period
insert into tax_period (id, tax_type, year) values (1, 'T', 2002);
insert into ref_book(id, name) values (8, 'Коды, определяющие налоговый (отчётный) период');
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (21, 1, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (22, 2, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);

-- department
insert into department (id, name, parent_id, type, shortname, tb_index, sbrf_code) values (1, 'банк', null, 1, null, null, null);
insert into department (id, name, parent_id, type, shortname, tb_index, sbrf_code) values (2, 'банк2', 1, 2, null, null, null);

-- report period
insert into report_period (id, name, tax_period_id, ord,  dict_tax_period_id, start_date, end_date, calendar_start_date) values (1, '2002 - 1 квартал', 1, 1, 21, date '2013-01-01', date '2013-03-31', date '2013-01-01');
insert into report_period (id, name, tax_period_id, ord,  dict_tax_period_id, start_date, end_date, calendar_start_date) values (2, '2002 - 2 квартал', 1, 2, 22, date '2013-04-01', date '2013-06-30', date '2013-04-01');

-- income102 data
insert into income_102 (id, report_period_id,  opu_code,  total_sum, department_id) values (1, 1, '2', 666, 1);
insert into income_102 (id, report_period_id,  opu_code,  total_sum, department_id) values (2, 1, '2.1', 666, 1);
insert into income_102 (id, report_period_id,  opu_code,  total_sum, department_id) values (3, 2, '2', 555, 1);
insert into income_102 (id, report_period_id,  opu_code,  total_sum, department_id) values (4, 1, '3', 444, 1);