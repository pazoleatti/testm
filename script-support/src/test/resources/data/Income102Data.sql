-- tax period
insert into tax_period (id, tax_type, start_date, end_date) values (1, 'T', to_date('01.01.02', 'DD.MM.RR'), to_date('31.12.02', 'DD.MM.RR'));
insert into ref_book(id, name) values (8, 'Коды, определяющие налоговый (отчётный) период');
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (21, 1, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);

-- department
insert into department (id, name, parent_id, type, shortname, dict_region_id, tb_index, sbrf_code) values (1, 'банк', null, 1, null, null, null, null);
insert into department (id, name, parent_id, type, shortname, dict_region_id, tb_index, sbrf_code) values (2, 'банк2', 1, 2, null, null, null, null);

-- report period
insert into report_period (id, name, months, tax_period_id, ord,  dict_tax_period_id) values (1, '2002 - 1 квартал', 3, 1, 1, 21);
insert into report_period (id, name, months, tax_period_id, ord,  dict_tax_period_id) values (2, '2002 - 2 квартал', 3, 1, 2, 21)

-- income102 data
insert into income_102 (id, report_period_id,  opu_code,  total_sum) values (1, 1, '2', 666);
insert into income_102 (id, report_period_id,  opu_code,  total_sum) values (2, 1, '2.1', 666);
insert into income_102 (id, report_period_id,  opu_code,  total_sum) values (3, 2, '2', 555);
insert into income_102 (id, report_period_id,  opu_code,  total_sum) values (4, 1, '3', 444);