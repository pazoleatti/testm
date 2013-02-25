insert into tax_period(id, tax_type, start_date, end_date) values (1, 'T', date '2013-01-01', date '2013-12-31');

insert into department (id, name, parent_id, type) values (1, 'А - департамент', null, 1);
insert into form_type (id, name, tax_type, fixed_rows) values (1, 'А - тип', 'T', 1);
insert into form_template (id, type_id, data_rows, version, is_active, edition, numbered_columns) values (1, 1, null, '0.1', 1, 1, 1);
insert into report_period (id, name, is_active, months, tax_period_id, ord) values (1, '1 - период', 1, 3, 1, 1)

insert into sec_user (id, name, login, department_id) values (1, 'user1', 'user1', 1);
insert into sec_user (id, name, login, department_id) values (2, 'user2', 'user2', 1);

-- Данные для проверки блокировки
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id) values (1, 1, 1, 1, 1, 1);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id) values (2, 1, 1, 1, 1, 1);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id) values (3, 1, 1, 1, 1, 1);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id) values (4, 1, 1, 1, 1, 1);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id) values (5, 1, 1, 1, 1, 1);

insert into object_lock(object_id, class, lock_time, user_id) values (2, 'com.aplana.sbrf.taxaccounting.model.FormData', sysdate, 1);
insert into object_lock(object_id, class, lock_time, user_id) values (3, 'com.aplana.sbrf.taxaccounting.model.FormData', timestamp '2000-02-03 12:20:34.000', 1);
insert into object_lock(object_id, class, lock_time, user_id) values (4, 'com.aplana.sbrf.taxaccounting.model.FormData', sysdate, 2);
insert into object_lock(object_id, class, lock_time, user_id) values (5, 'com.aplana.sbrf.taxaccounting.model.FormData', timestamp '2000-02-03 12:20:34.000', 2);

-- Данные для проверки разблокировки
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id) values (11, 1, 1, 1, 1, 1);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id) values (12, 1, 1, 1, 1, 1);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id) values (13, 1, 1, 1, 1, 1);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id) values (14, 1, 1, 1, 1, 1);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id) values (15, 1, 1, 1, 1, 1);

insert into object_lock(object_id, class, lock_time, user_id) values (12, 'com.aplana.sbrf.taxaccounting.model.FormData', sysdate, 1);
insert into object_lock(object_id, class, lock_time, user_id) values (13, 'com.aplana.sbrf.taxaccounting.model.FormData', timestamp '2000-02-03 12:20:34.000', 1);
insert into object_lock(object_id, class, lock_time, user_id) values (14, 'com.aplana.sbrf.taxaccounting.model.FormData', sysdate, 2);
insert into object_lock(object_id, class, lock_time, user_id) values (15, 'com.aplana.sbrf.taxaccounting.model.FormData', timestamp '2000-02-03 12:20:34.000', 2);

-- Данные для проверки isLocked
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id) values (21, 1, 1, 1, 1, 1);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id) values (22, 1, 1, 1, 1, 1);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id) values (23, 1, 1, 1, 1, 1);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id) values (24, 1, 1, 1, 1, 1);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id) values (25, 1, 1, 1, 1, 1);

insert into object_lock(object_id, class, lock_time, user_id) values (22, 'com.aplana.sbrf.taxaccounting.model.FormData', sysdate, 1);
insert into object_lock(object_id, class, lock_time, user_id) values (23, 'com.aplana.sbrf.taxaccounting.model.FormData', timestamp '2000-02-03 12:20:34.000', 1);
insert into object_lock(object_id, class, lock_time, user_id) values (24, 'com.aplana.sbrf.taxaccounting.model.FormData', sysdate, 2);
insert into object_lock(object_id, class, lock_time, user_id) values (25, 'com.aplana.sbrf.taxaccounting.model.FormData', timestamp '2000-02-03 12:20:34.000', 2);

-- Данные для проверки refreshLock
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id) values (31, 1, 1, 1, 1, 1);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id) values (32, 1, 1, 1, 1, 1);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id) values (33, 1, 1, 1, 1, 1);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id) values (34, 1, 1, 1, 1, 1);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id) values (35, 1, 1, 1, 1, 1);

insert into object_lock(object_id, class, lock_time, user_id) values (32, 'com.aplana.sbrf.taxaccounting.model.FormData', sysdate, 1);
insert into object_lock(object_id, class, lock_time, user_id) values (33, 'com.aplana.sbrf.taxaccounting.model.FormData', timestamp '2000-02-03 12:20:34.000', 1);
insert into object_lock(object_id, class, lock_time, user_id) values (34, 'com.aplana.sbrf.taxaccounting.model.FormData', sysdate, 2);
insert into object_lock(object_id, class, lock_time, user_id) values (35, 'com.aplana.sbrf.taxaccounting.model.FormData', timestamp '2000-02-03 12:20:34.000', 2);

-- Данные для проверки getLock
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id) values (41, 1, 1, 1, 1, 1);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id) values (42, 1, 1, 1, 1, 1);
insert into form_data (id, form_template_id, department_id, state, kind, report_period_id) values (43, 1, 1, 1, 1, 1);

insert into object_lock(object_id, class, lock_time, user_id) values (42, 'com.aplana.sbrf.taxaccounting.model.FormData', sysdate, 1);
insert into object_lock(object_id, class, lock_time, user_id) values (43, 'com.aplana.sbrf.taxaccounting.model.FormData', timestamp '2000-02-03 12:20:34.000', 1);
