insert into ref_book(id, name) values (8, 'Коды, определяющие налоговый (отчётный) период');
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (21, 1, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (22, 2, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (23, 3, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);

insert into report_period (id, name, months, tax_period_id, ord, dict_tax_period_id, start_date, end_date) values (1, 'Transport report period 1', 3,  1, 1, 21, date '2013-01-01', date '2013-03-31');
insert into report_period (id, name, months, tax_period_id, ord, dict_tax_period_id, start_date, end_date) values (2, 'Transport report period 2', 3,  1, 2, 22, date '2013-04-01', date '2013-06-30');
insert into report_period (id, name, months, tax_period_id, ord, dict_tax_period_id, start_date, end_date) values (3, 'VAT report period 1'      , 3, 21, 1, 21, date '2013-01-01', date '2013-03-31');
insert into report_period (id, name, months, tax_period_id, ord, dict_tax_period_id, start_date, end_date) values (4, 'Income report period 1'   , 3, 31, 1, 21, date '2013-01-01', date '2013-03-31');