insert into sec_role (id,alias,name,tax_type) values (1, 'ROLE_CONTROL', 'Контролёр','N');
insert into sec_role (id,alias,name,tax_type) values (2, 'ROLE_OPER', 'Оператор','N');
insert into sec_role (id,alias,name,tax_type) values (3, 'ROLE_CONTROL_UNP', 'Контролёр УНП','N');
insert into sec_role (id,alias,name,tax_type) values (4, 'ROLE_CONF', 'Настройщик','N');
insert into sec_role (id,alias,name,tax_type) values (5, 'ROLE_ADMIN', 'Администратор','N');
insert into sec_role (id,alias,name,tax_type) values (6, 'ROLE_CONTROL_NS', 'Контролёр НС','N');

insert into sec_role(id,alias,name,tax_type) values(11,'N_ROLE_OPER','Оператор (НДФЛ)','N');
insert into sec_role(id,alias,name,tax_type) values(12,'N_ROLE_CONTROL_UNP','Контролёр УНП (НДФЛ)','N');
insert into sec_role(id,alias,name,tax_type) values(13,'N_ROLE_CONF','Настройщик (НДФЛ)','N');
insert into sec_role(id,alias,name,tax_type) values(14,'N_ROLE_ADMIN','Администратор (НДФЛ)','N');
insert into sec_role(id,alias,name,tax_type) values(15,'N_ROLE_CONTROL_NS','Контролёр НС (НДФЛ)','N');

insert into sec_role(id,alias,name,tax_type) values(16,'F_ROLE_OPER','Оператор (Сборы)','F');
insert into sec_role(id,alias,name,tax_type) values(17,'F_ROLE_CONTROL_UNP','Контролёр УНП (Сборы)','F');
insert into sec_role(id,alias,name,tax_type) values(18,'F_ROLE_CONTROL_NS','Контролёр НС (Сборы)','F');
insert into sec_role(id,alias,name,tax_type) values(19,'F_ROLE_CONF','Настройщик (Сборы)','F');

insert into sec_user (id, login, name, department_id, is_active, email) values (0, 'InternalSystemUser', 'Система', 0, 1, NULL);

insert into sec_user_role (user_id, role_id) values (0, 3);
insert into sec_user_role (user_id, role_id) values (0, 4);
insert into sec_user_role (user_id, role_id) values (0, 5);