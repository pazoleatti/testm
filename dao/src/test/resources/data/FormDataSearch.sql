insert into form_kind (id, name) values (2, 'Консолидированная');
insert into form_kind (id, name) values (3, 'Сводная');

INSERT INTO department_type (id, name) VALUES (1, 'Банк');
INSERT INTO department_type (id, name) VALUES (2, 'Территориальный банк');

insert into department (id, name, parent_id, type, code) values (1, 'А - департамент', null, 1, 1);
insert into department (id, name, parent_id, type, code) values (2, 'Б - департамент', 1, 2, 2);
insert into department (id, name, parent_id, type, code) values (3, 'В - департамент', 1, 2, 3);

insert into tax_type (id, name) values ('T', 'Транспортный');
insert into tax_type (id, name) values ('I', 'Прибыль');

insert into form_type (id, name, tax_type) values (1, 'А - тип', 'T');
insert into form_type (id, name, tax_type) values (2, 'Б - тип', 'I');
insert into form_type (id, name, tax_type) values (3, 'В - тип', 'T');
insert into form_type (id, name, tax_type) values (4, 'Г - тип', 'T');

insert into form_template (id, type_id, data_rows, version, fixed_rows, name, fullname, header)
  values (1, 1, null, date '2013-01-01', 1,'name', 'fullname', 'header');
insert into form_template (id, type_id, data_rows, version, fixed_rows, name, fullname, header)
  values (2, 2, null, date '2013-01-01', 1,'name', 'fullname', 'header');
insert into form_template (id, type_id, data_rows, version, fixed_rows, name, fullname, header)
  values (3, 3, null, date '2013-01-01', 1,'name', 'fullname', 'header');
insert into form_template (id, type_id, data_rows, version, fixed_rows, name, fullname, header)
  values (4, 4, null, date '2013-01-01', 1,'name', 'fullname', 'header');

insert into tax_period(id, tax_type, year) values (1, 'T', 2013);
INSERT INTO report_period_type (id,code,name,start_date,end_date,calendar_start_date) VALUES (21, '21', 'первый квартал', DATE '1970-01-01', DATE '1970-03-31', DATE '1970-01-11');
INSERT INTO report_period_type (id,code,name,start_date,end_date,calendar_start_date) VALUES (22, '22', 'второй квартал', DATE '1970-04-01', DATE '1970-06-30', DATE '1970-04-04');
INSERT INTO report_period_type (id,code,name,start_date,end_date,calendar_start_date) VALUES (23, '23', 'третий квартал', DATE '1970-07-01', DATE '1970-09-30', DATE '1970-07-07');

insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) values (1, '1 - период', 1, 21, date '2013-01-01', date '2013-03-31', date '2013-01-01');
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) values (2, '2 - период', 1, 22, date '2013-04-01', date '2013-06-30', date '2013-04-01');
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) values (3, '3 - период', 1, 23, date '2013-07-01', date '2013-09-30', date '2013-07-01');

insert into department_report_period(id, department_id, report_period_id, is_active, is_balance_period) values (1, 1, 1, 1, 0);
insert into department_report_period(id, department_id, report_period_id, is_active, is_balance_period) values (5, 2, 2, 1, 0);
insert into department_report_period(id, department_id, report_period_id, is_active, is_balance_period) values (9, 3, 3, 1, 0);

insert into form_data (id, form_template_id, department_report_period_id, state, kind, return_sign) values (1,  4, 1, 1, 2, 0);
insert into form_data (id, form_template_id, department_report_period_id, state, kind, return_sign) values (2,  3, 5, 2, 3, 1);
insert into form_data (id, form_template_id, department_report_period_id, state, kind, return_sign) values (3,  2, 9, 3, 2, 0);
insert into form_data (id, form_template_id, department_report_period_id, state, kind, return_sign) values (4,  1, 1, 4, 3, 0);
insert into form_data (id, form_template_id, department_report_period_id, state, kind, return_sign) values (5,  4, 5, 1, 2, 1);
insert into form_data (id, form_template_id, department_report_period_id, state, kind, return_sign) values (6,  3, 9, 2, 3, 0);
insert into form_data (id, form_template_id, department_report_period_id, state, kind, return_sign) values (7,  2, 1, 3, 2, 0);
insert into form_data (id, form_template_id, department_report_period_id, state, kind, return_sign) values (8,  1, 5, 4, 3, 1);
insert into form_data (id, form_template_id, department_report_period_id, state, kind, return_sign) values (9,  4, 9, 1, 2, 0);
insert into form_data (id, form_template_id, department_report_period_id, state, kind, return_sign) values (10, 3, 1, 2, 3, 0);
insert into form_data (id, form_template_id, department_report_period_id, state, kind, return_sign) values (11, 2, 5, 3, 2, 0);
insert into form_data (id, form_template_id, department_report_period_id, state, kind, return_sign) values (12, 1, 9, 4, 3, 1);
insert into form_data (id, form_template_id, department_report_period_id, state, kind, return_sign) values (13, 4, 1, 1, 2, 0);
insert into form_data (id, form_template_id, department_report_period_id, state, kind, return_sign) values (14, 3, 5, 2, 3, 0);
insert into form_data (id, form_template_id, department_report_period_id, state, kind, return_sign) values (15, 2, 9, 3, 2, 0);
insert into form_data (id, form_template_id, department_report_period_id, state, kind, return_sign) values (16, 1, 1, 4, 3, 0);
insert into form_data (id, form_template_id, department_report_period_id, state, kind, return_sign) values (17, 4, 5, 1, 2, 0);
insert into form_data (id, form_template_id, department_report_period_id, state, kind, return_sign) values (18, 3, 9, 2, 3, 0);

-- Каждая из четырёх форм может быть в каждом из трёх департаментов с Kind = 2 и 3
insert into department_form_type(id, department_id, form_type_id, kind) values (11, 1, 1, 2);
insert into department_form_type(id, department_id, form_type_id, kind) values (12, 1, 1, 3);
insert into department_form_type(id, department_id, form_type_id, kind) values (13, 1, 2, 2);
insert into department_form_type(id, department_id, form_type_id, kind) values (14, 1, 2, 3);
insert into department_form_type(id, department_id, form_type_id, kind) values (15, 1, 3, 2);
insert into department_form_type(id, department_id, form_type_id, kind) values (16, 1, 3, 3);
insert into department_form_type(id, department_id, form_type_id, kind) values (17, 1, 4, 2);
insert into department_form_type(id, department_id, form_type_id, kind) values (18, 1, 4, 3);

insert into department_form_type(id, department_id, form_type_id, kind) values (21, 2, 1, 2);
insert into department_form_type(id, department_id, form_type_id, kind) values (22, 2, 1, 3);
insert into department_form_type(id, department_id, form_type_id, kind) values (23, 2, 2, 2);
insert into department_form_type(id, department_id, form_type_id, kind) values (24, 2, 2, 3);
insert into department_form_type(id, department_id, form_type_id, kind) values (25, 2, 3, 2);
insert into department_form_type(id, department_id, form_type_id, kind) values (26, 2, 3, 3);
insert into department_form_type(id, department_id, form_type_id, kind) values (27, 2, 4, 2);
insert into department_form_type(id, department_id, form_type_id, kind) values (28, 2, 4, 3);

insert into department_form_type(id, department_id, form_type_id, kind) values (31, 3, 1, 2);
insert into department_form_type(id, department_id, form_type_id, kind) values (32, 3, 1, 3);
insert into department_form_type(id, department_id, form_type_id, kind) values (33, 3, 2, 2);
insert into department_form_type(id, department_id, form_type_id, kind) values (34, 3, 2, 3);
insert into department_form_type(id, department_id, form_type_id, kind) values (35, 3, 3, 2);
insert into department_form_type(id, department_id, form_type_id, kind) values (36, 3, 3, 3);
insert into department_form_type(id, department_id, form_type_id, kind) values (37, 3, 4, 2);
insert into department_form_type(id, department_id, form_type_id, kind) values (38, 3, 4, 3);

-- Для форм подразделения 1 источниками будут являться форма 3 из подразделения 2 и форма 4 из подразделения 3
insert into form_data_source(department_form_type_id, src_department_form_type_id, period_start, period_end) values (11, 25, date '1900-01-01', null);
insert into form_data_source(department_form_type_id, src_department_form_type_id, period_start, period_end) values (11, 26, date '1900-01-01', null);
insert into form_data_source(department_form_type_id, src_department_form_type_id, period_start, period_end) values (14, 37, date '1900-01-01', null);
insert into form_data_source(department_form_type_id, src_department_form_type_id, period_start, period_end) values (14, 38, date '1900-01-01', null);

-- Для деклараций подразделения 1  источником будет являться форма 1 из подразделений 2 и 3
insert into declaration_type(id, name, tax_type) values (1, 'Декларация', 'T');
insert into department_declaration_type(id, department_id, declaration_type_id) values (1, 1, 1);
insert into declaration_source (department_declaration_type_id, src_department_form_type_id, period_start, period_end) values (1, 21, date '1900-01-01', null);
insert into declaration_source (department_declaration_type_id, src_department_form_type_id, period_start, period_end) values (1, 22, date '1900-01-01', null);
insert into declaration_source (department_declaration_type_id, src_department_form_type_id, period_start, period_end) values (1, 31, date '1900-01-01', null);
insert into declaration_source (department_declaration_type_id, src_department_form_type_id, period_start, period_end) values (1, 32, date '1900-01-01', null);

-- Для проверки сортировки по дате создания экземпляра НФ
insert into sec_user (id, login, name, department_id, is_active) values (1, 'controlUnp', 'Контролёр УНП', 1, 1);
insert into sec_user (id, login, name, department_id, is_active) values (2, 'god', 'Настройщик', 2, 1);

insert into log_business (id, log_date, event_id, roles, form_data_id, user_department_name, user_login) values (1, date '2014-01-01', 1, 'Контролёр УНП', 18, 'А - департамент', 'controlUnp');
insert into log_business (id, log_date, event_id, roles, form_data_id, user_department_name, user_login) values (2, date '2014-01-05', 6, 'Настройщик', 18, 'Б - департамент', 'god');
insert into log_business (id, log_date, event_id, roles, form_data_id, user_department_name, user_login) values (3, date '2014-01-02', 1, 'Контролёр УНП', 17, 'А - департамент', 'controlUnp');
insert into log_business (id, log_date, event_id, roles, form_data_id, user_department_name, user_login) values (4, date '2014-01-03', 1, 'Настройщик', 16, 'Б - департамент', 'god');
insert into log_business (id, log_date, event_id, roles, form_data_id, user_department_name, user_login) values (5, date '2014-01-04', 1, 'Контролёр УНП', 15, 'А - департамент', 'controlUnp');
insert into log_business (id, log_date, event_id, roles, form_data_id, user_department_name, user_login) values (6, date '2014-01-05', 1, 'Настройщик', 14, 'Б - департамент', 'god');
insert into log_business (id, log_date, event_id, roles, form_data_id, user_department_name, user_login) values (7, date '2014-01-06', 1, 'Настройщик', 13, 'Б - департамент', 'god');
insert into log_business (id, log_date, event_id, roles, form_data_id, user_department_name, user_login) values (8, date '2014-01-07', 1, 'Настройщик', 12, 'Б - департамент', 'god');
insert into log_business (id, log_date, event_id, roles, form_data_id, user_department_name, user_login) values (9, date '2014-01-08', 1, 'Настройщик', 11, 'Б - департамент', 'god');
insert into log_business (id, log_date, event_id, roles, form_data_id, user_department_name, user_login) values (10, date '2014-01-09', 1, 'Настройщик', 10, 'Б - департамент', 'god');
insert into log_business (id, log_date, event_id, roles, form_data_id, user_department_name, user_login) values (11, date '2014-01-10', 1, 'Настройщик', 9, 'Б - департамент', 'god');
insert into log_business (id, log_date, event_id, roles, form_data_id, user_department_name, user_login) values (12, date '2014-01-11', 1, 'Настройщик', 8, 'Б - департамент', 'god');
insert into log_business (id, log_date, event_id, roles, form_data_id, user_department_name, user_login) values (13, date '2014-01-12', 1, 'Настройщик', 7, 'Б - департамент', 'god');
insert into log_business (id, log_date, event_id, roles, form_data_id, user_department_name, user_login) values (14, date '2014-01-13', 1, 'Настройщик', 1, 'Б - департамент', 'god');
insert into log_business (id, log_date, event_id, roles, form_data_id, user_department_name, user_login) values (15, date '2014-01-14', 1, 'Настройщик', 5, 'Б - департамент', 'god');
insert into log_business (id, log_date, event_id, roles, form_data_id, user_department_name, user_login) values (16, date '2014-01-15', 1, 'Настройщик', 2, 'Б - департамент', 'god');
insert into log_business (id, log_date, event_id, roles, form_data_id, user_department_name, user_login) values (17, date '2014-01-16', 1, 'Настройщик', 3, 'Б - департамент', 'god');
insert into log_business (id, log_date, event_id, roles, form_data_id, user_department_name, user_login) values (18, date '2014-01-17', 1, 'Настройщик', 4, 'Б - департамент', 'god');
insert into log_business (id, log_date, event_id, roles, form_data_id, user_department_name, user_login) values (19, date '2014-01-17', 1, 'Настройщик', 6, 'Б - департамент', 'god');
