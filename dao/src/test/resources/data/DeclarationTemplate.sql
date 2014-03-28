insert into declaration_type (id, name, tax_type, status) values (1, 'Вид декларации 1', 'T', 1);
insert into declaration_type (id, name, tax_type) values (2, 'Вид декларации 2', 'T');
insert into declaration_type (id, name, tax_type) values (3, 'Вид декларации 3', 'V');

insert into tax_period(id, tax_type, year) values (1, 'T', 2013);
insert into tax_period(id, tax_type, year) values (11, 'T', 2012);
insert into tax_period(id, tax_type, year) values (21, 'V', 2013);

insert into ref_book(id, name) values (8, 'Коды, определяющие налоговый (отчётный) период');
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (21, 1, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (22, 2, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (23, 3, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);

insert into report_period (id, name, tax_period_id, ord, dict_tax_period_id, start_date, end_date, calendar_start_date) values (1, 'Transport report period 1',  1, 1, 21, date '2013-01-01', date '2013-03-31', date '2013-01-01');
insert into report_period (id, name, tax_period_id, ord, dict_tax_period_id, start_date, end_date, calendar_start_date) values (2, 'Transport report period 2',  1, 2, 22, date '2013-04-01', date '2013-06-30', date '2013-04-01');
insert into report_period (id, name, tax_period_id, ord, dict_tax_period_id, start_date, end_date, calendar_start_date) values (3, 'VAT report period 1'      , 21, 1, 21, date '2013-01-01', date '2013-03-31', date '2013-01-01');

insert into declaration_template(id, edition, name, version, jrxml, declaration_type_id, status)
  values (1, 1, 'Декларация 1', date '2013-01-01', null, 1, 0);
insert into declaration_template(id, edition, name, version, jrxml, declaration_type_id, status)
  values (2, 2, 'Декларация 2', date '2014-01-01', null, 1, 1);
insert into declaration_template(id, edition, name, version, jrxml, declaration_type_id, status)
  values (3, 3, 'Декларация 3', date '2013-01-01', null, 2, 1);
insert into declaration_template(id, edition, name, version, jrxml, declaration_type_id)
  values (4, 1, 'Декларация 4', date '2013-01-01', null, 2);
insert into declaration_template(id, edition, name, version, jrxml, declaration_type_id)
  values (5, 1, 'Декларация 5', date '2014-01-01', null, 3);
insert into declaration_template(id, edition, name, version, jrxml, declaration_type_id)
  values (6, 1, 'Декларация 6', date '2014-12-31', null, 3);
