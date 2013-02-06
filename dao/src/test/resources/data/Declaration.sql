insert into declaration_template(id, edition, tax_type, version, is_active) values (1, 1, 'T', '0.01', 1);
insert into department (id, name, parent_id, type) values (1, 'Банк', null, 1);

-- для проверки get и getXmlData
insert into report_period (id, name, tax_type, is_active, months) values (1, 'Transport report period 1', 'T', 1, 3)
insert into report_period (id, name, tax_type, is_active, months) values (2, 'Transport report period 2', 'T', 0, 3)
insert into declaration(id, declaration_template_id, report_period_id, department_id, data, is_accepted) values (1, 1, 1, 1, 'test-data-string-1', 1);
insert into declaration(id, declaration_template_id, report_period_id, department_id, data, is_accepted) values (2, 1, 2, 1, null, 0);

-- для проверки setAccepted 
insert into report_period (id, name, tax_type, is_active, months) values (3, 'Transport report period 3', 'T', 0, 3)
insert into report_period (id, name, tax_type, is_active, months) values (4, 'Transport report period 4', 'T', 0, 3)
insert into declaration(id, declaration_template_id, report_period_id, department_id, data, is_accepted) values (3, 1, 3, 1, null, 1);
insert into declaration(id, declaration_template_id, report_period_id, department_id, data, is_accepted) values (4, 1, 4, 1, null, 0);

-- для проверки delete
insert into report_period (id, name, tax_type, is_active, months) values (5, 'Transport report period 5', 'T', 0, 3)
insert into declaration(id, declaration_template_id, report_period_id, department_id, data, is_accepted) values (5, 1, 5, 1, null, 1);

-- для проверки saveNew
insert into report_period (id, name, tax_type, is_active, months) values (6, 'Transport report period 6', 'T', 0, 3)

-- для проверки setXmlData
insert into report_period (id, name, tax_type, is_active, months) values (7, 'Transport report period 7', 'T', 0, 3)
insert into declaration(id, declaration_template_id, report_period_id, department_id, data, is_accepted) values (7, 1, 7, 1, null, 1);








