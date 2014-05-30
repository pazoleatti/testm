insert into form_kind (id, name) values (1, 'Первичная');
insert into form_kind (id, name) values (2, 'Консолидированная');
insert into form_kind (id, name) values (3, 'Сводная');
insert into form_kind (id, name) values (4, 'Форма УНП');
insert into form_kind (id, name) values (5, 'Выходная');

insert into declaration_type (id, name, tax_type) values (1, 'Вид декларации (тест)', 'T');

-- TODO: сейчас с источниками бардак формы являются источниками друг друга без учёта вида налога

insert into form_type (id, name, tax_type) values (1, 'FormType - Transport', 'T');
insert into form_type (id, name, tax_type) values (2, 'FormType - Income', 'I');
insert into form_type (id, name, tax_type) values (3, 'FormType - VAT', 'V');
insert into form_type (id, name, tax_type) values (4, 'FormType - Property', 'P');
insert into form_type (id, name, tax_type) values (5, 'FormType - DEAL', 'D');

insert into form_type (id, name, tax_type) values (11, 'FormType - Transport2', 'T');
insert into form_type (id, name, tax_type) values (12, 'FormType - Transport3', 'T');

insert into form_type (id, name, tax_type) values (21, 'FormType - Income2', 'I');

insert into department (id, name, parent_id, type, code) values (1, 'Банк', null, 1, 1);
insert into department (id, name, parent_id, type, code) values (2, 'ТБ1', 1, 2, 2);
insert into department (id, name, parent_id, type, code) values (3, 'ТБ2', 1, 2, 3);
insert into department (id, name, parent_id, type, code) values (4, 'ГОСБ1', 2, 3, 4);
insert into department (id, name, parent_id, type, code) values (5, 'ГОСБ2', 2, 3, 5);
insert into department (id, name, parent_id, type, code) values (6, 'ГОСБ3', 2, 3, 6);
insert into department (id, name, parent_id, type, code) values (7, 'ОСБ1', 4, 4, 7);

-- В подразделении 1 есть все налоговые формы
insert into department_form_type (id, department_id, form_type_id, kind) values (1, 1, 1, 3);
insert into department_form_type (id, department_id, form_type_id, kind) values (2, 1, 2, 3);
insert into department_form_type (id, department_id, form_type_id, kind) values (3, 1, 3, 3);
insert into department_form_type (id, department_id, form_type_id, kind) values (4, 1, 4, 3);
insert into department_form_type (id, department_id, form_type_id, kind) values (5, 1, 11, 3);
insert into department_form_type (id, department_id, form_type_id, kind) values (6, 1, 12, 3);
insert into department_form_type (id, department_id, form_type_id, kind) values (7, 1, 21, 3);

-- В подразделении 2 есть формы 1 и 2
insert into department_form_type (id, department_id, form_type_id, kind) values (11, 2, 1, 3);
insert into department_form_type (id, department_id, form_type_id, kind) values (12, 2, 2, 3);

-- В подразделении 3 есть формы 1 и 2
insert into department_form_type (id, department_id, form_type_id, kind) values (21, 3, 1, 3);
insert into department_form_type (id, department_id, form_type_id, kind) values (22, 3, 2, 3);

-- В подразделении 4 форма 4
insert into department_form_type (id, department_id, form_type_id, kind) values (23, 4, 2, 2);

-- В подразделении 6 форма 2
insert into department_form_type (id, department_id, form_type_id, kind) values (24, 6, 2, 2);

-- Для Формы 1 в подразделении 2 источниками являются формы 1, 2, 3, 4 из подразделения 1  и форма 2 из подразделения 3
insert into form_data_source (department_form_type_id, src_department_form_type_id) values (11, 1);
insert into form_data_source (department_form_type_id, src_department_form_type_id) values (11, 2);
insert into form_data_source (department_form_type_id, src_department_form_type_id) values (11, 3);
insert into form_data_source (department_form_type_id, src_department_form_type_id) values (11, 4);
insert into form_data_source (department_form_type_id, src_department_form_type_id) values (11, 22);

-- Для формы 2 в подразделении 2 источниками являются форма 1 из подразлделения 1 и форма 1 из подразделения 2
insert into form_data_source (department_form_type_id, src_department_form_type_id) values (12, 1);
insert into form_data_source (department_form_type_id, src_department_form_type_id) values (12, 11);

-- Для формы 2 в подразделении 4 источниками являются форма 2 из подразделения 6
insert into form_data_source (department_form_type_id, src_department_form_type_id) values (23, 24);

-- В подразделении 2 есть декларация 1 
insert into department_declaration_type (id, department_id, declaration_type_id) values (1, 2, 1);

-- Для декларации 1 в подразделении 2 источником является формы 1,2 из подразделения 3 и формы 11, 12 из подразделения 1
insert into declaration_source (department_declaration_type_id, src_department_form_type_id) values (1, 21);
insert into declaration_source (department_declaration_type_id, src_department_form_type_id) values (1, 22);
insert into declaration_source (department_declaration_type_id, src_department_form_type_id) values (1, 5);
insert into declaration_source (department_declaration_type_id, src_department_form_type_id) values (1, 6);
