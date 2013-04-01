insert into sec_user (id, name, login, department_id, is_active, email, uuid) values
  (1, 'Контролёр Банка', 'controlBank', 1, 1, 'controlBank@bank.ru', 'F32C1F04-7860-43CA-884F-39CC1D740064');
insert into sec_user (id, name, login, department_id, is_active, email, uuid) values
  (2, 'Контролёр ТБ1', 'controlTB1', 2, 1, 'controlTB1@bank.ru', '4054E57F-6CE9-4C31-848D-57E4653DD25E');
insert into sec_user (id, name, login, department_id, is_active, email, uuid) values
  (3, 'Контролёр ТБ2', 'controlTB2', 3, 1, 'controlTB2@bank.ru', '3BFDB8D0-C2F8-4EAE-8F72-B992199A0671');

insert into sec_role (id, alias, name) values (1, 'ROLE_CONTROL', 'Контролёр');

insert into sec_user_role (user_id, role_id) values (1, 1);
insert into sec_user_role (user_id, role_id) values (2, 1);
insert into sec_user_role (user_id, role_id) values (3, 1);