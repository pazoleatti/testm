-- tax period
insert into tax_period (id, tax_type, start_date, end_date) values (1, 'T', to_date('01.01.02', 'DD.MM.RR'), to_date('31.12.02', 'DD.MM.RR'));
insert into ref_book(id, name) values (8, 'Коды, определяющие налоговый (отчётный) период');
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (21, 1, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (22, 2, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);

-- department
insert into department (id, name, parent_id, type, shortname, dict_region_id, tb_index, sbrf_code) values (1, 'банк1', null, 1, null, null, null, null);
insert into department (id, name, parent_id, type, shortname, dict_region_id, tb_index, sbrf_code) values (2, 'банк2', 1, 2, null, null, null, null);

-- report period
insert into report_period (id, name, months, tax_period_id, ord, dict_tax_period_id) values (1, '2002 - 1 квартал', 3, 1, 1, 21);
insert into report_period (id, name, months, tax_period_id, ord, dict_tax_period_id) values (2, '2002 - 2 квартал', 3, 1, 2, 22);

-- income101 data
insert into income_101 (id, report_period_id, account, income_debet_remains, income_credit_remains, debet_rate, credit_rate, outcome_debet_remains, outcome_credit_remains) values (1, 1, '2', 3, 4, 5, 6, 7, 8);
insert into income_101 (id, report_period_id, account, income_debet_remains, income_credit_remains, debet_rate, credit_rate, outcome_debet_remains, outcome_credit_remains) values (2, 1, '2.1', 4, 5, 6, 7, 8, 9);
insert into income_101 (id, report_period_id, account, income_debet_remains, income_credit_remains, debet_rate, credit_rate, outcome_debet_remains, outcome_credit_remains) values (3, 2, '3', 5, 6, 7, 8, 9, 0);