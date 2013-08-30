insert into DEPARTMENT (id, name, parent_id, type, shortname, tb_index, sbrf_code) values (1, 'Банк', null, 1, null, null, '12');
insert into DEPARTMENT (id, name, parent_id, type, shortname, tb_index, sbrf_code) values (2, 'ТБ1', 1, 2, null, null, '23');
insert into DEPARTMENT (id, name, parent_id, type, shortname, tb_index, sbrf_code) values (3, 'ТБ2', 1, 2, null, null, null);

insert into tax_period(id, tax_type, start_date, end_date) values (1, 'T', date '2013-01-01', date '2013-12-31');
insert into tax_period(id, tax_type, start_date, end_date) values (11, 'T', date '2012-01-01', date '2012-12-31');
insert into tax_period(id, tax_type, start_date, end_date) values (21, 'V', date '2013-01-01', date '2013-12-31');
insert into tax_period(id, tax_type, start_date, end_date) values (31, 'P', date '2013-01-01', date '2013-12-31');

insert into ref_book(id, name) values (8, 'Коды, определяющие налоговый (отчётный) период');
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (21, 1, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (22, 2, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);

insert into report_period (id, name, months, tax_period_id, ord, dict_tax_period_id) values (1, 'Transport report period 1', 3,  1, 1, 21);
insert into report_period (id, name, months, tax_period_id, ord, dict_tax_period_id) values (2, 'Transport report period 2', 3,  1, 2, 22);
insert into report_period (id, name, months, tax_period_id, ord, dict_tax_period_id) values (3, 'VAT report period 1'      , 3, 21, 1, 21);
insert into report_period (id, name, months, tax_period_id, ord, dict_tax_period_id) values (4, 'Income report period 1'   , 3, 31, 1, 21);