insert into log_system(id, log_date, ip, event_id, user_login, roles, department_name, report_period_name,
  declaration_type_name, form_type_name, form_kind_id, note, user_department_name)
  values (1, to_date('01.01.2013 01.00.00', 'DD.MM.YY HH.MI.SS'), '192.168.72.16', 1, 'controlBank', 'operator', 'ТБ1', '2013 первый квартал', null, 'test Transport form_type_name', 1, 'the best note', 'Подразделение');

insert into log_system(id, log_date, ip, event_id, user_login, roles, department_name, report_period_name,
  declaration_type_name, form_type_name, form_kind_id, note, user_department_name)
  values (2, to_date('01.01.2013 02.00.00', 'DD.MM.YY HH.MI.SS'), '192.168.72.16', 1, 'controlBank', 'operator', 'ТБ2', '2014 первый квартал', 'test declaration_type_name', null, null, 'the best note', 'Подразделение');

insert into log_system(id, log_date, ip, event_id, user_login, roles, department_name, report_period_name,
                       declaration_type_name, form_type_name, form_kind_id, note, user_department_name)
  values (3, to_date('01.01.2013 03.00.00', 'DD.MM.YY HH.MI.SS'), '192.168.72.16', 601, 'controlBank', 'operator', 'Банк', '2013 первый квартал', null, 'test form_type_name', 3, 'the best note', 'Подразделение');








