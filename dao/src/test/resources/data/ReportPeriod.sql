insert into ref_book(id, name) values (8, 'Коды, определяющие налоговый (отчётный) период');
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (21, 1, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);

insert into report_period (id, name, is_active, months, tax_period_id, ord, department_id, dict_tax_period_id) values (1, 'Transport report period 1', 1, 3, 1, 1, 1, 21);
insert into report_period (id, name, is_active, months, tax_period_id, ord, department_id, dict_tax_period_id) values (2, 'Transport report period 2', 0, 3, 1, 2, 1, 21);
insert into report_period (id, name, is_active, months, tax_period_id, ord, department_id, dict_tax_period_id) values (3, 'VAT report period 1', 0, 3, 21, 1, 1, 21);
insert into report_period (id, name, is_active, months, tax_period_id, ord, department_id, dict_tax_period_id) values (4, 'Income report period 1', 0, 3, 31, 1, 1, 21);