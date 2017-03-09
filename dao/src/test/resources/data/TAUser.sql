insert into sec_user (id, name, login, department_id, is_active, email) values
  (1, 'Контролёр Банка', 'controlBank', 1, 1, 'controlBank@bank.ru');
insert into sec_user (id, name, login, department_id, is_active, email) values
  (2, 'Контролёр ТБ1', 'controlTB1', 2, 1, 'controlTB1@bank.ru');
insert into sec_user (id, name, login, department_id, is_active, email) values
  (3, 'Контролёр ТБ2', 'controlTB2', 3, 0, 'controlTB2@bank.ru');

INSERT INTO sec_role (id, alias, name, tax_type) VALUES (1, 'N_ROLE_CONTROL_NS', 'Контролёр', 'N');
INSERT INTO sec_role (id, alias, name, tax_type) VALUES (2, 'N_ROLE_OPER', 'Оператор', 'N');
INSERT INTO sec_role (id, alias, name, tax_type) VALUES (3, 'N_ROLE_CONTROL_UNP', 'Контролёр УНП', 'N');

insert into sec_user_role (user_id, role_id) values (1, 1);
insert into sec_user_role (user_id, role_id) values (2, 1);
insert into sec_user_role (user_id, role_id) values (3, 1);