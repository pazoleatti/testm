insert into department_type (id, name) values (1, 'Банк');
insert into department_type (id, name) values (2, 'Территориальный банк');

insert into department (id, name, parent_id, type, code) values (1, 'Банк', null, 1, 1);
insert into department (id, name, parent_id, type, code) values (2, 'ТБ 1', 1, 2, 2);
insert into department (id, name, parent_id, type, code) values (3, 'ТБ 2', 1, 2, 3);
insert into department (id, name, parent_id, type, code) values (4, 'ТБ 3', 1, 2, 4);

insert into department_declaration_type (id, department_id, declaration_type_id) values (1, 1, 1);
insert into department_declaration_type (id, department_id, declaration_type_id) values (2, 1, 2);
insert into department_declaration_type (id, department_id, declaration_type_id) values (3, 1, 3);
insert into department_declaration_type (id, department_id, declaration_type_id) values (4, 2, 2);
insert into department_declaration_type (id, department_id, declaration_type_id) values (5, 3, 1);
insert into department_declaration_type (id, department_id, declaration_type_id) values (6, 3, 3);
insert into department_declaration_type (id, department_id, declaration_type_id) values (7, 4, 1);