insert into form_kind (id, name) values (2, 'Консолидированная');
insert into form_kind (id, name) values (3, 'Сводная');

INSERT INTO department_type (id, name) VALUES (1, 'Банк');
INSERT INTO department_type (id, name) VALUES (2, 'Территориальный банк');

insert into department (id, name, parent_id, type, code) values (1, 'А - департамент', null, 1, 1);
insert into department (id, name, parent_id, type, code) values (2, 'Б - департамент', 1, 2, 2);

insert into tax_type (id, name) values ('T', 'Транспортный');
insert into tax_type (id, name) values ('I', 'Прибыль');

insert into form_type (id, name, tax_type) values (1, 'А - тип', 'T');
insert into form_type (id, name, tax_type) values (2, 'Б - тип', 'I');

insert into form_template (id, type_id, data_rows, version, fixed_rows, name, fullname, header)
  values (1, 1, null, date '2013-01-01', 1, 'name', 'fullname', 'header');
insert into form_template (id, type_id, data_rows, version, fixed_rows, name, fullname, header)
  values (2, 2, null, date '2013-01-01', 1, 'name', 'fullname', 'header');

insert into tax_period(id, tax_type, year) values (1, 'T', 2013);
INSERT INTO report_period_type (id,code,name,start_date,end_date,calendar_start_date) VALUES (21, '21', 'первый квартал', DATE '1970-01-01', DATE '1970-03-31', DATE '1970-01-11');
INSERT INTO report_period_type (id,code,name,start_date,end_date,calendar_start_date) VALUES (22, '22', 'второй квартал', DATE '1970-04-01', DATE '1970-06-30', DATE '1970-04-04');

insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) values (1, '1 - период', 1, 21, date '2013-01-01', date '2013-03-31', date '2013-01-01');
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) values (2, '2 - период', 1, 22, date '2013-04-01', date '2013-06-30', date '2013-04-01');

insert into department_report_period(id, department_id, report_period_id, is_active, is_balance_period) values (1, 1, 1, 1, 0);
insert into department_report_period(id, department_id, report_period_id, is_active, is_balance_period) values (2, 2, 2, 1, 0);

insert into form_data (id, form_template_id, department_report_period_id, state, kind, return_sign) values (1, 1, 1, 1, 2, 0);
insert into form_data (id, form_template_id, department_report_period_id, state, kind, return_sign) values (2, 1, 2, 2, 3, 0);
insert into form_data (id, form_template_id, department_report_period_id, state, kind, return_sign) values (3, 2, 2, 2, 3, 0);

insert into blob_data (id, name, data, creation_date) values ('uuid_1', 'file_1', 'b1', sysdate);
insert into blob_data (id, name, data, creation_date) values ('uuid_2', 'file_2', 'b2', sysdate);
insert into blob_data (id, name, data, creation_date) values ('uuid_3', 'file_3', 'b3', sysdate);
insert into blob_data (id, name, data, creation_date) values ('uuid_4', 'file_4', 'b4', sysdate);
insert into blob_data (id, name, data, creation_date) values ('uuid_5', 'file_5', 'b5', sysdate);
insert into blob_data (id, name, data, creation_date) values ('uuid_6', 'file_6', 'b6', sysdate);

insert into form_data_file (blob_data_id, form_data_id, user_name, user_department_name, note) values ('uuid_1', 1, 'name1', 'dep1', null);
insert into form_data_file (blob_data_id, form_data_id, user_name, user_department_name, note) values ('uuid_2', 1, 'name2', 'dep2', 'str');
insert into form_data_file (blob_data_id, form_data_id, user_name, user_department_name, note) values ('uuid_3', 2, 'name3', 'подр3', null);
insert into form_data_file (blob_data_id, form_data_id, user_name, user_department_name, note) values ('uuid_4', 2, 'name3', 'подр3', 'test');
