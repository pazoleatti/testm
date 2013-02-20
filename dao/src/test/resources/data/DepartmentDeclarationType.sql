-- DEPARTMENT
insert into department (id, name, parent_id, type) values (1, 'testName1', null, 1);
insert into department (id, name, parent_id, type) values (2, 'testName2', 1, 2);
insert into department (id, name, parent_id, type) values (3, 'testName3', 1, 2);
insert into department (id, name, parent_id, type) values (4, 'testName4', 1, 2);
insert into department (id, name, parent_id, type) values (5, 'testName5', 1, 2);

-- DECLARATION_TYPE
insert into declaration_type (id, tax_type, name) values (1, 'T', 'testName1');
insert into declaration_type (id, tax_type, name) values (2, 'I', 'testName2');
insert into declaration_type (id, tax_type, name) values (3, 'P', 'testName3');
insert into declaration_type (id, tax_type, name) values (4, 'V', 'testName4');

-- DEPARTMENT_DECLARATION_TYPE
insert into department_declaration_type (id, department_id, declaration_type_id) values (1, 1, 1);
insert into department_declaration_type (id, department_id, declaration_type_id) values (2, 1, 2);
insert into department_declaration_type (id, department_id, declaration_type_id) values (3, 2, 1);
insert into department_declaration_type (id, department_id, declaration_type_id) values (4, 2, 1);

-- FORM_TYPE
insert into form_type (id, name, tax_type, fixed_rows) values (1, 'FormType - Transport', 'T', 1);
insert into form_type (id, name, tax_type, fixed_rows) values (2, 'FormType - Income', 'I', 0);
insert into form_type (id, name, tax_type, fixed_rows) values (3, 'FormType - VAT', 'V', 1);
insert into form_type (id, name, tax_type, fixed_rows) values (4, 'FormType - Property', 'P', 0);

-- DEPARTMENT_FORM_TYPE
insert into department_form_type (id, department_id, form_type_id, kind) values (1, 1, 1, 1);
insert into department_form_type (id, department_id, form_type_id, kind) values (2, 2, 1, 1);
insert into department_form_type (id, department_id, form_type_id, kind) values (3, 3, 1, 1);
insert into department_form_type (id, department_id, form_type_id, kind) values (4, 1, 2, 2);

-- DECLARATION_SOURCE
insert into declaration_source (department_declaration_type_id, src_department_form_type_id) values (1, 1);
insert into declaration_source (department_declaration_type_id, src_department_form_type_id) values (2, 2);
insert into declaration_source (department_declaration_type_id, src_department_form_type_id) values (3, 1);
insert into declaration_source (department_declaration_type_id, src_department_form_type_id) values (1, 4);
insert into declaration_source (department_declaration_type_id, src_department_form_type_id) values (2, 4);
insert into declaration_source (department_declaration_type_id, src_department_form_type_id) values (4, 4);