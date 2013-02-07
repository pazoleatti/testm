insert into declaration_template(id, edition, version, is_active, declaration_type_id) values (1, 1, '0.01', 1, 1);
insert into department (id, name, parent_id, type) values (1, 'Банк', null, 1);

insert into tax_period(id, tax_type, start_date, end_date) values (1, 'T', date '2013-01-01', date '2013-12-31');

-- для проверки get и getXmlData
insert into report_period (id, name, tax_type, is_active, months, tax_period_id, ord) values (1, 'Transport report period 1', 'T', 1, 3, 1, 1);
insert into report_period (id, name, tax_type, is_active, months, tax_period_id, ord) values (2, 'Transport report period 2', 'T', 0, 3, 1, 2);
insert into declaration(id, declaration_template_id, report_period_id, department_id, data, is_accepted) values (1, 1, 1, 1, 'test-data-string-1', 1);
insert into declaration(id, declaration_template_id, report_period_id, department_id, data, is_accepted) values (2, 1, 2, 1, null, 0);

-- для проверки setAccepted 
insert into report_period (id, name, tax_type, is_active, months, tax_period_id, ord) values (3, 'Transport report period 3', 'T', 0, 3, 1, 3);
insert into report_period (id, name, tax_type, is_active, months, tax_period_id, ord) values (4, 'Transport report period 4', 'T', 0, 3, 1, 4);
insert into declaration(id, declaration_template_id, report_period_id, department_id, data, is_accepted) values (3, 1, 3, 1, null, 1);
insert into declaration(id, declaration_template_id, report_period_id, department_id, data, is_accepted) values (4, 1, 4, 1, null, 0);

-- для проверки delete
insert into report_period (id, name, tax_type, is_active, months, tax_period_id, ord) values (5, 'Transport report period 5', 'T', 0, 3, 1, 5)
insert into declaration(id, declaration_template_id, report_period_id, department_id, data, is_accepted) values (5, 1, 5, 1, null, 1);

-- для проверки saveNew
insert into report_period (id, name, tax_type, is_active, months, tax_period_id, ord) values (6, 'Transport report period 6', 'T', 0, 3, 1, 6)

-- для проверки setXmlData
insert into report_period (id, name, tax_type, is_active, months, tax_period_id, ord) values (7, 'Transport report period 7', 'T', 0, 3, 1, 7)
insert into declaration(id, declaration_template_id, report_period_id, department_id, data, is_accepted) values (7, 1, 7, 1, null, 1);








