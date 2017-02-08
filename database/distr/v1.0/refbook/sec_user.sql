insert into sec_role (id, alias, name) values (1, 'ROLE_CONTROL', 'Контролёр');
insert into sec_role (id, alias, name) values (2, 'ROLE_OPER', 'Оператор');
insert into sec_role (id, alias, name) values (3, 'ROLE_CONTROL_UNP', 'Контролёр УНП');
insert into sec_role (id, alias, name) values (4, 'ROLE_CONF', 'Настройщик');
insert into sec_role (id, alias, name) values (5, 'ROLE_ADMIN', 'Администратор');
insert into sec_role (id, alias, name) values (6, 'ROLE_CONTROL_NS', 'Контролёр НС');


insert into sec_user (id, login, name, department_id, is_active, email) values (0, 'InternalSystemUser', 'Система', 0, 1, NULL);

insert into sec_user_role (user_id, role_id) values (0, 3);
insert into sec_user_role (user_id, role_id) values (0, 4);
insert into sec_user_role (user_id, role_id) values (0, 5);