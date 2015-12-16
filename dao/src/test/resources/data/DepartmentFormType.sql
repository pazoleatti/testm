insert into form_kind (id, name) values (1, 'Первичная');
insert into form_kind (id, name) values (2, 'Консолидированная');
insert into form_kind (id, name) values (3, 'Сводная');

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

