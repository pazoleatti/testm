insert into department_form_type (id, department_id, form_type_id, kind) values (1, 1, 1, 3);
insert into department_form_type (id, department_id, form_type_id, kind) values (2, 1, 2, 3);
insert into department_form_type (id, department_id, form_type_id, kind) values (3, 1, 3, 3);
insert into department_form_type (id, department_id, form_type_id, kind) values (4, 1, 4, 3);

insert into department_form_type (id, department_id, form_type_id, kind) values (11, 2, 1, 3);
insert into department_form_type_performer (DEPARTMENT_FORM_TYPE_ID, PERFORMER_DEP_ID) values (11, 1);
insert into department_form_type (id, department_id, form_type_id, kind) values (12, 2, 2, 3);

insert into department_form_type (id, department_id, form_type_id, kind) values (21, 3, 1, 3);
insert into department_form_type_performer (DEPARTMENT_FORM_TYPE_ID, PERFORMER_DEP_ID) values (21, 2);
insert into department_form_type (id, department_id, form_type_id, kind) values (22, 3, 2, 3);
insert into department_form_type_performer (DEPARTMENT_FORM_TYPE_ID, PERFORMER_DEP_ID) values (22, 2);

-- FORM_DATA_SOURCE
insert into form_data_source (department_form_type_id, src_department_form_type_id, period_start, period_end) values (11, 1, date '1900-01-01', null);

insert into log_system(id, log_date, ip, event_id, user_login, roles, department_name, report_period_name,
  declaration_type_name, form_type_name, form_kind_id, note, user_department_name, form_department_id, form_type_id)
  values (1, to_date('01.01.2013 01.00.00', 'DD.MM.YY HH.MI.SS'), '192.168.72.16', 1, 'controlBank', 'operator', 'ТБ1', '2013 первый квартал', null, 'test Transport form_type_name', 1, 'the best note', 'Подразделение', 10, 1);

insert into log_system(id, log_date, ip, event_id, user_login, roles, department_name, report_period_name,
  declaration_type_name, form_type_name, form_kind_id, note, user_department_name, form_department_id, form_type_id)
  values (2, to_date('01.01.2013 02.00.00', 'DD.MM.YY HH.MI.SS'), '192.168.72.16', 1, 'controlBank', 'operator', 'ТБ2', '2014 первый квартал', 'test declaration_type_name', null, null, 'the best note', 'Подразделение', 10, 1);

insert into log_system(id, log_date, ip, event_id, user_login, roles, department_name, report_period_name,
                       declaration_type_name, form_type_name, form_kind_id, note, user_department_name, form_department_id, form_type_id)
  values (3, to_date('01.01.2013 03.00.00', 'DD.MM.YY HH.MI.SS'), '192.168.72.16', 601, 'controlBank', 'operator', 'Банк', '2013 первый квартал', null, 'test form_type_name', 3, 'the best note', 'Подразделение', 10, 1);

insert into log_system(id, log_date, ip, event_id, user_login, roles, department_name, report_period_name,
                       declaration_type_name, form_type_name, form_kind_id, note, user_department_name, form_department_id, form_type_id)
  values (4, to_date('01.01.2013 03.00.00', 'DD.MM.YY HH.MI.SS'), '192.168.72.16', 601, 'controlBank', 'operator', 'Банк', '2013 первый квартал', null, 'test form_type_name', 3, 'the best note', 'Подразделение', 10, 1);








