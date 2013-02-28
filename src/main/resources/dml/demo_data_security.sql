insert into sec_user (id, name, login, department_id) values (3, 'Контролёр Банка', 'controlBank', 1);
insert into sec_user (id, name, login, department_id) values (1, 'Контролёр ТБ1', 'controlTB1', 2);
insert into sec_user (id, name, login, department_id) values (2, 'Контролёр ТБ2', 'controlTB2', 3);
insert into sec_user (id, name, login, department_id) values (100, 'Контролёр УНП', 'controlUnp', 1);
insert into sec_user (id, name, login, department_id) values (101, 'Оператор ТБ1', 'operTB1', 2);
insert into sec_user (id, name, login, department_id) values (102, 'Оператор ТБ2', 'operTB2', 3);
insert into sec_user (id, name, login, department_id) values (103, 'Настройщик', 'conf', 1);
insert into sec_user (id, name, login, department_id) values (104, 'Администратор', 'admin', 1);

insert into sec_role (id, alias, name) values (1, 'ROLE_CONTROL', 'Контролёр');
insert into sec_role (id, alias, name) values (2, 'ROLE_OPER', 'Оператор');
insert into sec_role (id, alias, name) values (3, 'ROLE_CONTROL_UNP', 'Контролёр УНП');
insert into sec_role (id, alias, name) values (4, 'ROLE_CONF', 'Настройщик');
insert into sec_role (id, alias, name) values (5, 'ROLE_ADMIN', 'Администратор');

insert into sec_user_role (user_id, role_id) values (1, 1);
insert into sec_user_role (user_id, role_id) values (2, 1);
insert into sec_user_role (user_id, role_id) values (3, 1);
insert into sec_user_role (user_id, role_id) values (100, 3);
insert into sec_user_role (user_id, role_id) values (101, 2);
insert into sec_user_role (user_id, role_id) values (102, 2);
insert into sec_user_role (user_id, role_id) values (103, 4);
insert into sec_user_role (user_id, role_id) values (104, 5);
