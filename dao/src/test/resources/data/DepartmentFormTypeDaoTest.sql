insert into declaration_type (id, name, tax_type) values (1, 'Вид декларации (тест)', 'T');

insert into form_type (id, name, tax_type, fixed_rows) values (1, 'FormType - Transport', 'T', 1);
insert into form_type (id, name, tax_type, fixed_rows) values (2, 'FormType - Income', 'I', 0);
insert into form_type (id, name, tax_type, fixed_rows) values (3, 'FormType - VAT', 'V', 1);
insert into form_type (id, name, tax_type, fixed_rows) values (4, 'FormType - Property', 'P', 0);

insert into department (id, name, parent_id, type) values (1, 'Банк', null, 1);
insert into department (id, name, parent_id, type) values (2, 'ТБ1', 1, 2);
insert into department (id, name, parent_id, type) values (3, 'ТБ2', 1, 2);

insert into department_form_type (id, department_id, form_type_id, kind) values (1, 1, 1, 3);
insert into department_form_type (id, department_id, form_type_id, kind) values (2, 1, 2, 3);
insert into department_form_type (id, department_id, form_type_id, kind) values (3, 1, 3, 3);
insert into department_form_type (id, department_id, form_type_id, kind) values (4, 1, 4, 3);

insert into department_form_type (id, department_id, form_type_id, kind) values (11, 2, 1, 3);
insert into department_form_type (id, department_id, form_type_id, kind) values (12, 2, 2, 3);

insert into department_form_type (id, department_id, form_type_id, kind) values (21, 3, 1, 3);
insert into department_form_type (id, department_id, form_type_id, kind) values (22, 3, 2, 3);

insert into form_data_source (department_form_type_id, src_department_form_type_id) values (11, 1);
insert into form_data_source (department_form_type_id, src_department_form_type_id) values (11, 2);
insert into form_data_source (department_form_type_id, src_department_form_type_id) values (11, 3);
insert into form_data_source (department_form_type_id, src_department_form_type_id) values (11, 4);
insert into form_data_source (department_form_type_id, src_department_form_type_id) values (12, 1);
insert into form_data_source (department_form_type_id, src_department_form_type_id) values (12, 11);
insert into form_data_source (department_form_type_id, src_department_form_type_id) values (11, 22);

insert into department_declaration_type (id, department_id, declaration_type_id) values (1, 2, 1);
insert into declaration_source (department_declaration_type_id, src_department_form_type_id) values (1, 21);
insert into declaration_source (department_declaration_type_id, src_department_form_type_id) values (1, 22);



