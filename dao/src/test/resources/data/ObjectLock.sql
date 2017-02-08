INSERT INTO tax_type (id, name) VALUES ('T', 'Транспортный');
insert into form_kind (id, name) values (1, 'Первичная');
insert into tax_period(id, tax_type, year) values (1, 'T', 2013);
INSERT INTO report_period_type (id,code,name,start_date,end_date,calendar_start_date) VALUES (21, '21', 'первый квартал', DATE '1970-01-01', DATE '1970-03-31', DATE '1970-01-11');

INSERT INTO department_type (id, name) VALUES (1, 'Банк');
insert into department (id, name, parent_id, type, code) values (1, 'А - департамент', null, 1, 1);
INSERT INTO tax_type (id, name) VALUES ('T', 'Транспортный');
insert into form_type (id, name, tax_type) values (1, 'А - тип', 'T');
insert into form_template (id, type_id, data_rows, version, fixed_rows, name, fullname, header)
  values (1, 1, null, date '2013-01-01', 1, 'name', 'fullname', 'header');
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) values (1, 'Transport report period 1', 1, 21, date '2013-01-01', date '2013-03-31', date '2013-01-01');

insert into sec_user (id, name, login, department_id, is_active, email) values (1, 'user1', 'user1', 1, 1, 'user1@bank.ru');
insert into sec_user (id, name, login, department_id, is_active, email) values (2, 'user2', 'user2', 1, 1, 'user2@bank.ru');

-- Данные для проверки блокировки
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (1, 1, 1, 1, 1, 1, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (2, 1, 1, 1, 1, 1, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (3, 1, 1, 1, 1, 1, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (4, 1, 1, 1, 1, 1, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (5, 1, 1, 1, 1, 1, 0);

insert into object_lock(object_id, class, lock_time, user_id) values (2, 'com.aplana.sbrf.taxaccounting.model.FormData', sysdate, 1);
insert into object_lock(object_id, class, lock_time, user_id) values (3, 'com.aplana.sbrf.taxaccounting.model.FormData', timestamp '2000-02-03 12:20:34.000', 1);
insert into object_lock(object_id, class, lock_time, user_id) values (4, 'com.aplana.sbrf.taxaccounting.model.FormData', sysdate, 2);
insert into object_lock(object_id, class, lock_time, user_id) values (5, 'com.aplana.sbrf.taxaccounting.model.FormData', timestamp '2000-02-03 12:20:34.000', 2);

-- Данные для проверки разблокировки
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (11, 1, 1, 1, 1, 1, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (12, 1, 1, 1, 1, 1, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (13, 1, 1, 1, 1, 1, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (14, 1, 1, 1, 1, 1, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (15, 1, 1, 1, 1, 1, 0);

insert into object_lock(object_id, class, lock_time, user_id) values (12, 'com.aplana.sbrf.taxaccounting.model.FormData', sysdate, 1);
insert into object_lock(object_id, class, lock_time, user_id) values (13, 'com.aplana.sbrf.taxaccounting.model.FormData', timestamp '2000-02-03 12:20:34.000', 1);
insert into object_lock(object_id, class, lock_time, user_id) values (14, 'com.aplana.sbrf.taxaccounting.model.FormData', sysdate, 2);
insert into object_lock(object_id, class, lock_time, user_id) values (15, 'com.aplana.sbrf.taxaccounting.model.FormData', timestamp '2000-02-03 12:20:34.000', 2);

-- Данные для проверки isLocked
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (21, 1, 1, 1, 1, 1, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (22, 1, 1, 1, 1, 1, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (23, 1, 1, 1, 1, 1, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (24, 1, 1, 1, 1, 1, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (25, 1, 1, 1, 1, 1, 0);

insert into object_lock(object_id, class, lock_time, user_id) values (22, 'com.aplana.sbrf.taxaccounting.model.FormData', sysdate, 1);
insert into object_lock(object_id, class, lock_time, user_id) values (23, 'com.aplana.sbrf.taxaccounting.model.FormData', timestamp '2000-02-03 12:20:34.000', 1);
insert into object_lock(object_id, class, lock_time, user_id) values (24, 'com.aplana.sbrf.taxaccounting.model.FormData', sysdate, 2);
insert into object_lock(object_id, class, lock_time, user_id) values (25, 'com.aplana.sbrf.taxaccounting.model.FormData', timestamp '2000-02-03 12:20:34.000', 2);

-- Данные для проверки refreshLock
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (31, 1, 1, 1, 1, 1, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (32, 1, 1, 1, 1, 1, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (33, 1, 1, 1, 1, 1, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (34, 1, 1, 1, 1, 1, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (35, 1, 1, 1, 1, 1, 0);

insert into object_lock(object_id, class, lock_time, user_id) values (32, 'com.aplana.sbrf.taxaccounting.model.FormData', sysdate, 1);
insert into object_lock(object_id, class, lock_time, user_id) values (33, 'com.aplana.sbrf.taxaccounting.model.FormData', timestamp '2000-02-03 12:20:34.000', 1);
insert into object_lock(object_id, class, lock_time, user_id) values (34, 'com.aplana.sbrf.taxaccounting.model.FormData', sysdate, 2);
insert into object_lock(object_id, class, lock_time, user_id) values (35, 'com.aplana.sbrf.taxaccounting.model.FormData', timestamp '2000-02-03 12:20:34.000', 2);

-- Данные для проверки getLock
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (41, 1, 1, 1, 1, 1, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (42, 1, 1, 1, 1, 1, 0);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id, return_sign) values (43, 1, 1, 1, 1, 1, 0);

insert into object_lock(object_id, class, lock_time, user_id) values (42, 'com.aplana.sbrf.taxaccounting.model.FormData', sysdate, 1);
insert into object_lock(object_id, class, lock_time, user_id) values (43, 'com.aplana.sbrf.taxaccounting.model.FormData', timestamp '2000-02-03 12:20:34.000', 1);
