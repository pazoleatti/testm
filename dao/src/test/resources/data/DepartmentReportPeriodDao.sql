insert into DEPARTMENT (id, name, parent_id, type, shortname, tb_index, sbrf_code, code) values (1, 'Банк', null, 1, null, null, '12', 1);
insert into DEPARTMENT (id, name, parent_id, type, shortname, tb_index, sbrf_code, code) values (2, 'ТБ1', 1, 2, null, null, '23', 2);
insert into DEPARTMENT (id, name, parent_id, type, shortname, tb_index, sbrf_code, code) values (3, 'ТБ2', 1, 2, null, null, null, 3);

insert into ref_book(id, name) values (8, 'Коды, определяющие налоговый (отчётный) период');
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (21, 1, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (22, 2, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);

insert into tax_period(id, tax_type, year) values (1, 'T', 2013);

insert into report_period (id, name, tax_period_id, ord, dict_tax_period_id, start_date, end_date, calendar_start_date) values (1, 'Transport report period 1',  1, 1, 21, date '2013-01-01', date '2013-03-31', date '2013-01-01');
insert into report_period (id, name, tax_period_id, ord, dict_tax_period_id, start_date, end_date, calendar_start_date) values (2, 'Transport report period 2',  1, 2, 22, date '2013-04-01', date '2013-06-30', date '2013-04-01');

insert into department_report_period(id, department_id, report_period_id, is_active, is_balance_period, correction_date) values (1, 2, 1, 1, 0, to_date('01.01.2013', 'DD.MM.YY'));
insert into department_report_period(id, department_id, report_period_id, is_active, is_balance_period, correction_date) values (2, 2, 2, 0, 0, to_date('01.01.2013', 'DD.MM.YY'));