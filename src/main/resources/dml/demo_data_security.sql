insert into sec_user (id, name, login, department_id) values (3, '�������� �����', 'controlBank', 1);
insert into sec_user (id, name, login, department_id) values (1, '�������� ��1', 'controlTB1', 2);
insert into sec_user (id, name, login, department_id) values (2, '�������� ��2', 'controlTB2', 3);
insert into sec_user (id, name, login, department_id) values (100, '�������� ���', 'controlUnp', 1);
insert into sec_user (id, name, login, department_id) values (101, '�������� ��1', 'operTB1', 2);
insert into sec_user (id, name, login, department_id) values (102, '�������� ��2', 'operTB2', 3);
insert into sec_user (id, name, login, department_id) values (103, '����������', 'conf', 1);
insert into sec_user (id, name, login, department_id) values (104, '�������������', 'admin', 1);

insert into sec_role (id, alias, name) values (1, 'ROLE_CONTROL', '��������');
insert into sec_role (id, alias, name) values (2, 'ROLE_OPER', '��������');
insert into sec_role (id, alias, name) values (3, 'ROLE_CONTROL_UNP', '�������� ���');
insert into sec_role (id, alias, name) values (4, 'ROLE_CONF', '����������');
insert into sec_role (id, alias, name) values (5, 'ROLE_ADMIN', '�������������');

insert into sec_user_role (user_id, role_id) values (1, 1);
insert into sec_user_role (user_id, role_id) values (2, 1);
insert into sec_user_role (user_id, role_id) values (3, 1);
insert into sec_user_role (user_id, role_id) values (100, 3);
insert into sec_user_role (user_id, role_id) values (101, 2);
insert into sec_user_role (user_id, role_id) values (102, 2);
insert into sec_user_role (user_id, role_id) values (103, 4);
insert into sec_user_role (user_id, role_id) values (104, 5);
