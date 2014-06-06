insert into ref_book(id, name) values (8, 'Коды, определяющие налоговый (отчётный) период');
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (21, 1, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (22, 2, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (23, 3, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);

insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) values (1, 'первый квартал',  1, 21, date '2013-01-01', date '2013-03-31', date '2013-01-01');
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) values (2, 'полугодие',  1, 22, date '2013-04-01', date '2013-06-30', date '2013-04-01');
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) values (3, 'год'      , 21, 21, date '2013-01-01', date '2013-03-31', date '2013-01-01');
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) values (4, 'Income report period 1'   , 31, 21, date '2014-01-01', date '2014-03-31', date '2014-01-01');