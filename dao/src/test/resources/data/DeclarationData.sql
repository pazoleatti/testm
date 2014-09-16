insert into declaration_template(id, name, version, declaration_type_id) values (1,'Декларация 1', date '2014-01-01', 1);
insert into declaration_data(id, declaration_template_id, department_report_period_id, data, is_accepted, tax_organ_code, kpp) values (1, 1, 102, null, 1, 'CD12', '123456789');
insert into declaration_data(id, declaration_template_id, department_report_period_id, data, is_accepted) values (2, 1, 204, null, 0);
insert into declaration_data(id, declaration_template_id, department_report_period_id, data, is_accepted) values (3, 1, 303, null, 1);
insert into declaration_data(id, declaration_template_id, department_report_period_id, data, is_accepted) values (4, 1, 401, null, 0);
insert into declaration_data(id, declaration_template_id, department_report_period_id, data, is_accepted) values (5, 1, 501, null, 1);
insert into declaration_data(id, declaration_template_id, department_report_period_id, data, is_accepted) values (7, 1, 605, null, 1);







