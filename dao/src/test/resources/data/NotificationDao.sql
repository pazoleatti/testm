insert into log (id, creation_date) values ('uuid_1', sysdate);
insert into blob_data (id, name, data, creation_date) values ('uuid_2', 'name2', 'b2', sysdate);

insert into notification (ID, REPORT_PERIOD_ID, SENDER_DEPARTMENT_ID, RECEIVER_DEPARTMENT_ID, IS_READ, TEXT, CREATE_DATE, DEADLINE, USER_ID, ROLE_ID) values (1, 1, 2, 1, 1, 'asaa', date '2013-12-31', date '2013-12-31', null, null)
insert into notification (ID, REPORT_PERIOD_ID, SENDER_DEPARTMENT_ID, RECEIVER_DEPARTMENT_ID, IS_READ, TEXT, CREATE_DATE, DEADLINE, USER_ID, ROLE_ID) values (2, 1, 3, 1, 1, 'asaa', date '2013-12-31', date '2013-12-31', null, null)
insert into notification (ID, REPORT_PERIOD_ID, SENDER_DEPARTMENT_ID, RECEIVER_DEPARTMENT_ID, IS_READ, TEXT, CREATE_DATE, DEADLINE, USER_ID, ROLE_ID) values (3, 2, 2, 1, 0, 'asaa', date '2013-12-31', date '2013-12-31', null, null)

insert into notification (ID, REPORT_PERIOD_ID, SENDER_DEPARTMENT_ID, RECEIVER_DEPARTMENT_ID, IS_READ, TEXT, CREATE_DATE, DEADLINE, USER_ID, ROLE_ID) values (4, null, null, null, 0, 'asaa', date '2013-12-31', date '2013-12-31', 1, null)
insert into notification (ID, REPORT_PERIOD_ID, SENDER_DEPARTMENT_ID, RECEIVER_DEPARTMENT_ID, IS_READ, TEXT, CREATE_DATE, DEADLINE, USER_ID, ROLE_ID) values (5, null, null, null, 0, 'asaa', date '2013-12-31', date '2013-12-31', 2, null)

insert into notification (ID, REPORT_PERIOD_ID, SENDER_DEPARTMENT_ID, RECEIVER_DEPARTMENT_ID, IS_READ, TEXT, CREATE_DATE, DEADLINE, USER_ID, ROLE_ID) values (6, null, null, null, 0, 'asaa6', date '2013-12-31', date '2013-12-31', null, 1)
insert into notification (ID, REPORT_PERIOD_ID, SENDER_DEPARTMENT_ID, RECEIVER_DEPARTMENT_ID, IS_READ, TEXT, CREATE_DATE, DEADLINE, USER_ID, ROLE_ID) values (7, null, null, null, 0, 'asaa7', date '2013-12-31', date '2013-12-31', null, 2)
insert into notification (ID, REPORT_PERIOD_ID, SENDER_DEPARTMENT_ID, RECEIVER_DEPARTMENT_ID, IS_READ, TEXT, CREATE_DATE, DEADLINE, USER_ID, ROLE_ID) values (8, null, null, null, 0, 'asaa8', date '2013-12-31', date '2013-12-31', null, 2)
insert into notification (ID, REPORT_PERIOD_ID, SENDER_DEPARTMENT_ID, RECEIVER_DEPARTMENT_ID, IS_READ, TEXT, CREATE_DATE, DEADLINE, USER_ID, ROLE_ID) values (9, null, null, null, 0, 'asaa8', date '2013-12-31', date '2013-12-31', null, 3)
insert into notification (ID, REPORT_PERIOD_ID, SENDER_DEPARTMENT_ID, RECEIVER_DEPARTMENT_ID, IS_READ, TEXT, CREATE_DATE, DEADLINE, USER_ID, ROLE_ID) values (10, null, null, null, 0, 'asaa8', date '2013-12-31', date '2013-12-31', 3, 1)