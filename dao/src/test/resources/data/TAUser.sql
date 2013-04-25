insert into sec_user (id, name, login, department_id, is_active, email) values
  (1, 'Контролёр Банка', 'controlBank', 1, 1, 'controlBank@bank.ru');
insert into sec_user (id, name, login, department_id, is_active, email) values
  (2, 'Контролёр ТБ1', 'controlTB1', 2, 1, 'controlTB1@bank.ru');
insert into sec_user (id, name, login, department_id, is_active, email) values
  (3, 'Контролёр ТБ2', 'controlTB2', 3, 1, 'controlTB2@bank.ru');

insert into sec_role (id, alias, name) values (1, 'ROLE_CONTROL', 'Контролёр');
insert into sec_role (id, alias, name) values (2, 'ROLE_OPER', 'Оператор');
insert into sec_role (id, alias, name) values (3, 'ROLE_CONTROL_UNP', 'Контролёр УНП');

insert into sec_user_role (user_id, role_id) values (1, 1);
insert into sec_user_role (user_id, role_id) values (2, 1);
insert into sec_user_role (user_id, role_id) values (3, 1);