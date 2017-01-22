INSERT INTO department_type (id, name) VALUES (1, 'Банк');
INSERT INTO department_type (id, name) VALUES (2, 'Территориальный банк');

insert into department (id, name, parent_id, type, code) values (1, 'А - департамент', null, 1, 1);
insert into department (id, name, parent_id, type, code) values (2, 'Б - департамент', 1, 2, 2);

insert into tax_type (id, name) values ('T', 'Транспортный');
insert into tax_type (id, name) values ('I', 'Прибыль');

insert into declaration_type (id, name, tax_type, status) values (1, 'Вид декларации 1', 'T', 1);
insert into declaration_type (id, name, tax_type) values (2, 'Вид декларации 2', 'I');

insert into declaration_template(id, name, version, jrxml, declaration_type_id, status, form_kind, form_type)
  values (1, 'Декларация 1', date '2013-01-01', null, 1, 0, 3, 1);
insert into declaration_template(id, name, version, jrxml, declaration_type_id, status, form_kind, form_type)
  values (2, 'Декларация 2', date '2013-01-01', null, 2, 0, 3, 1);

insert into tax_period(id, tax_type, year) values (1, 'T', 2013);
insert into ref_book(id, name) values (8, 'Коды, определяющие налоговый (отчётный) период');
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (21, 1, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (22, 2, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);

insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) values (1, '1 - период', 1, 21, date '2013-01-01', date '2013-03-31', date '2013-01-01');
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) values (2, '2 - период', 1, 22, date '2013-04-01', date '2013-06-30', date '2013-04-01');

insert into department_report_period(id, department_id, report_period_id, is_active, is_balance_period) values (1, 1, 1, 1, 0);
insert into department_report_period(id, department_id, report_period_id, is_active, is_balance_period) values (2, 2, 2, 1, 0);

insert into declaration_data(id, declaration_template_id, department_report_period_id, state) values (1, 1, 1, 1);
insert into declaration_data(id, declaration_template_id, department_report_period_id, state) values (2, 1, 2, 1);
insert into declaration_data(id, declaration_template_id, department_report_period_id, state) values (3, 2, 2, 1);

insert into blob_data (id, name, data, creation_date) values ('uuid_1', 'file_1', 'b1', sysdate);
insert into blob_data (id, name, data, creation_date) values ('uuid_2', 'file_2', 'b2', sysdate);
insert into blob_data (id, name, data, creation_date) values ('uuid_3', 'file_3', 'b3', sysdate);
insert into blob_data (id, name, data, creation_date) values ('uuid_4', 'file_4', 'b4', sysdate);
insert into blob_data (id, name, data, creation_date) values ('uuid_5', 'file_5', 'b5', sysdate);
insert into blob_data (id, name, data, creation_date) values ('uuid_6', 'file_6', 'b6', sysdate);

insert into declaration_data_file (blob_data_id, declaration_data_id, user_name, user_department_name, note) values ('uuid_1', 1, 'name1', 'dep1', null);
insert into declaration_data_file (blob_data_id, declaration_data_id, user_name, user_department_name, note) values ('uuid_2', 1, 'name2', 'dep2', 'str');
insert into declaration_data_file (blob_data_id, declaration_data_id, user_name, user_department_name, note) values ('uuid_3', 2, 'name3', 'подр3', null);
insert into declaration_data_file (blob_data_id, declaration_data_id, user_name, user_department_name, note) values ('uuid_4', 2, 'name3', 'подр3', 'test');
