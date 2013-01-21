insert into department (id, name, parent_id, type) values (1, 'А - департамент', null, 1);
insert into form_type (id, name, tax_type, fixed_rows) values (1, 'А - тип', 'T', 1);
insert into form (id, type_id, data_rows, version, is_active, edition, numbered_columns) values (1, 1, null, '0.1', 1, 1, 1);
insert into report_period (id, name, tax_type, is_active) values (1, '1 - период', 'T', 1)

insert into sec_user (id, name, login, department_id) values (1, 'user1', 'user1', 1);
insert into sec_user (id, name, login, department_id) values (2, 'user2', 'user2', 1);

-- Данные для проверки блокировки
insert into form_data (id, form_id, department_id, state, kind, report_period_id) values (1, 1, 1, 1, 1, 1);
insert into form_data (id, form_id, department_id, state, kind, report_period_id) values (2, 1, 1, 1, 1, 1);
insert into form_data (id, form_id, department_id, state, kind, report_period_id) values (3, 1, 1, 1, 1, 1);
insert into form_data (id, form_id, department_id, state, kind, report_period_id) values (4, 1, 1, 1, 1, 1);
insert into form_data (id, form_id, department_id, state, kind, report_period_id) values (5, 1, 1, 1, 1, 1);

insert into object_lock(object_id, class, lock_time, user_id) values (2, 'com.aplana.sbrf.taxaccounting.model.FormData', sysdate, 1);
insert into object_lock(object_id, class, lock_time, user_id) values (3, 'com.aplana.sbrf.taxaccounting.model.FormData', timestamp '2000-02-03 12:20:34.000', 1);
insert into object_lock(object_id, class, lock_time, user_id) values (4, 'com.aplana.sbrf.taxaccounting.model.FormData', sysdate, 2);
insert into object_lock(object_id, class, lock_time, user_id) values (5, 'com.aplana.sbrf.taxaccounting.model.FormData', timestamp '2000-02-03 12:20:34.000', 2);

-- Данные для проверки разблокировки
insert into form_data (id, form_id, department_id, state, kind, report_period_id) values (11, 1, 1, 1, 1, 1);
insert into form_data (id, form_id, department_id, state, kind, report_period_id) values (12, 1, 1, 1, 1, 1);
insert into form_data (id, form_id, department_id, state, kind, report_period_id) values (13, 1, 1, 1, 1, 1);
insert into form_data (id, form_id, department_id, state, kind, report_period_id) values (14, 1, 1, 1, 1, 1);
insert into form_data (id, form_id, department_id, state, kind, report_period_id) values (15, 1, 1, 1, 1, 1);

insert into object_lock(object_id, class, lock_time, user_id) values (12, 'com.aplana.sbrf.taxaccounting.model.FormData', sysdate, 1);
insert into object_lock(object_id, class, lock_time, user_id) values (13, 'com.aplana.sbrf.taxaccounting.model.FormData', timestamp '2000-02-03 12:20:34.000', 1);
insert into object_lock(object_id, class, lock_time, user_id) values (14, 'com.aplana.sbrf.taxaccounting.model.FormData', sysdate, 2);
insert into object_lock(object_id, class, lock_time, user_id) values (15, 'com.aplana.sbrf.taxaccounting.model.FormData', timestamp '2000-02-03 12:20:34.000', 2);