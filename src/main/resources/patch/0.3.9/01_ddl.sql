---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-7297: �������� ��������� �����
--alter table form_column add numeration_row number(9);
--alter table form_data add number_previous_row number (9);

alter table form_column drop constraint form_column_chk_type;
alter table form_column drop constraint form_column_chk_max_length; 

alter table form_column drop constraint form_column_chk_numrow; 
alter table form_column add constraint form_column_chk_numrow check (numeration_row in (0, 1) or type <> 'A');
alter table form_column add constraint form_column_chk_type check(type in ('N', 'S', 'D', 'R', 'A'));
alter table form_column add constraint form_column_chk_max_length check ((type = 'S' and max_length is not null and max_length > 0 and max_length <= 2000) or (type = 'N' and max_length is not null and max_length > 0 and max_length <= 27) or ((type ='D' or type ='R' or type='A') and max_length is null));

comment on column form_column.type is '��� ������� (S - ������, N � �����, D � ����, R - ������, A - �������������� �����)';
comment on column form_column.numeration_row is '��� ��������� ����� ��� �������������� �����';
comment on column form_data.number_previous_row is '����� ��������� ������ ���������� ��';

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-7329: ������� ���� "ord" �� ������� "report_period"
alter table report_period drop column ord; 

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-7856: ����������� �� ��������� �������
ALTER TABLE tax_period ADD CONSTRAINT tax_period_uniq_taxtype_year UNIQUE (tax_type, year);

--SBRFACCTAX-7406 - ��������������� ����������-����������
--alter table form_data_source add period_start date;
--alter table form_data_source add period_end date;

--alter table declaration_source add period_start date;
--alter table declaration_source add period_end date;

comment on column form_data_source.period_start is '���� ������ �������� ����������';
comment on column form_data_source.period_end is '���� ��������� �������� ����������';

comment on column declaration_source.period_start is '���� ������ �������� ����������';
comment on column declaration_source.period_end is '���� ��������� �������� ����������';

update form_data_source set period_start = to_date('01.01.2008', 'DD.MM.YYYY'), period_end = null;
update declaration_source set period_start = to_date('01.01.2008', 'DD.MM.YYYY'), period_end = null;

alter table form_data_source modify period_start not null;
alter table declaration_source modify  period_start not null;

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8037 - ������� �� �� ���� FORM_TEMPLATE.EDITION � DECLARATION_TEMPLATE.EDITION
ALTER TABLE form_template DROP COLUMN edition;
ALTER TABLE declaration_template DROP COLUMN edition;

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8021 - ��������� � ������� �� � ���������� � ����� � ������� �� ������� "������"
ALTER TABLE template_changes DROP CONSTRAINT changes_fk_form_template_id;
ALTER TABLE template_changes DROP CONSTRAINT changes_fk_dec_template_id;

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-7999 - �������� ������� "header" � ������� form_template / ������� Code � form_type
ALTER TABLE form_template ADD header VARCHAR2(1000 byte);
COMMENT ON COLUMN form_template.header IS '������� ���������� �������� �����';

ALTER TABLE form_type ADD code VARCHAR2(600 byte);
ALTER TABLE form_type ADD CONSTRAINT form_type_uniq_code UNIQUE(code);
COMMENT ON COLUMN form_type.code IS '����� �����';

UPDATE form_template SET header=trim(code);
ALTER TABLE form_template DROP COLUMN code; 

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-7650 - ������� ��������������� � ��������� �����������
ALTER TABLE ref_book_attribute ADD READ_ONLY NUMBER(1) DEFAULT 0 NOT NULL;
COMMENT ON COLUMN ref_book_attribute.read_only IS '������ ��� ������ (0 - �������������� �������� ������������; 1 - �������������� ���������� ������������)';
ALTER TABLE ref_book_attribute ADD CONSTRAINT ref_book_attr_chk_read_only CHECK (read_only IN (0, 1));

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-7686 - ������������ ����� ������/����� ����� �����
ALTER TABLE ref_book_attribute ADD max_length number(4);
COMMENT ON COLUMN ref_book_attribute.max_length IS '������������ ����� ������/������������ ���������� ���� ��� ����� ����� � ����������� �����������';
ALTER TABLE ref_book_attribute ADD CONSTRAINT ref_book_attr_chk_max_length check ((type=1 and max_length between 1 and 2000) or (type=2 and max_length between 1 and 27) or (type in (3,4) and max_length IS null));

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8120 - ����� �������� ����������
CREATE TABLE lock_data
(
key VARCHAR2(1000) NOT NULL,
user_id NUMBER(9) NOT NULL,
date_before DATE NOT NULL
);

COMMENT ON TABLE lock_data IS '���������� � �����������';
COMMENT ON COLUMN lock_data.key IS '��� ����������';
COMMENT ON COLUMN lock_data.user_id IS '������������� ������������, ������������� ����������';
COMMENT ON COLUMN lock_data.date_before IS '���� ��������� ����������';

ALTER TABLE lock_data ADD CONSTRAINT lock_data_pk primary key (key);
ALTER TABLE lock_data ADD CONSTRAINT lock_data_fk_user_id foreign key (user_id) references sec_user(id) on delete cascade;

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-7870 - ��������� ������� CONFIGURATION
ALTER TABLE configuration ADD department_id number(9) DEFAULT 0 NOT NULL;

ALTER TABLE configuration ADD CONSTRAINT configuration_fk FOREIGN KEY (department_id) REFERENCES department (id) ON DELETE CASCADE;
ALTER TABLE configuration DROP CONSTRAINT configuration_pk;
DROP INDEX configuration_pk;
ALTER TABLE configuration ADD CONSTRAINT configuration_pk PRIMARY KEY (code, department_id);
COMMENT ON COLUMN configuration.department_id IS '��';

-- -- ��������� � http://jira.aplana.com/browse/SBRFACCTAX-7698
ALTER TABLE configuration ADD(value_temp clob);
UPDATE configuration SET value_temp = value;
ALTER TABLE configuration drop column value;
ALTER TABLE configuration rename column value_temp to value; 
COMMENT ON COLUMN configuration.value IS '�������� ���������'; 

INSERT INTO configuration (code) values ('OKATO_UPLOAD_DIRECTORY');
INSERT INTO configuration (code) values ('REGION_UPLOAD_DIRECTORY');
INSERT INTO configuration (code) values ('ACCOUNT_PLAN_UPLOAD_DIRECTORY');
INSERT INTO configuration (code) values ('DIASOFT_UPLOAD_DIRECTORY');

UPDATE configuration 
SET value = (SELECT value FROM configuration WHERE code = 'REF_BOOK_DIRECTORY' and department_id = 0)
WHERE department_id = 0 and code in ('OKATO_UPLOAD_DIRECTORY', 'REGION_UPLOAD_DIRECTORY', 'ACCOUNT_PLAN_UPLOAD_DIRECTORY'); 

UPDATE configuration 
SET VALUE = (SELECT value FROM configuration WHERE code = 'REF_BOOK_DIASOFT_DIRECTORY' and department_id = 0)
WHERE department_id = 0 and code in ('DIASOFT_UPLOAD_DIRECTORY');

DELETE FROM configuration WHERE code in ('REF_BOOK_DIRECTORY', 'REF_BOOK_DIASOFT_DIRECTORY');
ALTER TABLE configuration modify department_id default null;

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8101 - � ������� INCOME_101, INCOME_102 ���������� �������� ���� "��. ������� � ������������� ��"
ALTER TABLE income_101 ADD account_period_id NUMBER(9);
ALTER TABLE income_101 ADD CONSTRAINT income_101_fk_accperiod_id FOREIGN KEY (account_period_id) REFERENCES ref_book_record(id);
COMMENT ON COLUMN income_101.account_period_id IS '������������� ������� � ������������� ��';

ALTER TABLE income_102 ADD account_period_id NUMBER(9);
ALTER TABLE income_102 ADD CONSTRAINT income_102_fk_accperiod_id FOREIGN KEY (account_period_id) REFERENCES ref_book_record(id);
COMMENT ON COLUMN income_102.account_period_id IS '������������� ������� � ������������� ��';

---------------------------------------------------------------------------------------------------------
--������ ����� �� LOG_SYSTEM 

-- http://jira.aplana.com/browse/SBRFACCTAX-8207 - ����� ������������� � ���������� ������������
UPDATE sec_user SET department_id = 0 WHERE id = 0;

-- http://jira.aplana.com/browse/SBRFACCTAX-7975 - ��������� ������ ������� ����������� "�������������" � ������ ������
ALTER TABLE log_system ADD user_login varchar2(255);
UPDATE LOG_SYSTEM ls SET ls.user_id = 0 WHERE ls.user_id IS null;
UPDATE LOG_SYSTEM ls SET ls.user_login = (SELECT login FROM SEC_USER su WHERE ls.USER_ID = su.ID);

-- http://jira.aplana.com/browse/SBRFACCTAX-8207 - ��������� DEPARTMENT_NAME �� SEC_USER.DEPARTMENT
MERGE INTO log_system tgt
USING
  (SELECT ls.id as log_system_id, d.name as hier_department_name
  FROM log_system ls
  JOIN sec_user sc on sc.id = ls.user_id
  JOIN (SELECT id, name FROM department WHERE id = 0
        UNION ALL
        SELECT a.id,
           substr(Sys_connect_by_path(name, '/'), 2) AS fullname
        FROM   department a
        START WITH PARENT_ID = 0
        CONNECT BY parent_id = PRIOR id) d
       ON d.id = sc.department_id
  WHERE ls.department_name IS NULL) src
ON (tgt.id = src.log_system_id)
WHEN MATCHED THEN
     UPDATE SET tgt.department_name = src.hier_department_name; 
ALTER TABLE log_system MODIFY department_name NOT NULL;	 	 

ALTER TABLE LOG_SYSTEM drop column user_id;
ALTER TABLE log_system ADD CONSTRAINT log_system_fk_user_login foreign key (user_login) references sec_user(login);
--ALTER TABLE LOG_SYSTEM drop CONSTRAINT LOG_SYSTEM_FK_USER_ID;

--������� LOG_BUSINESS
ALTER TABLE log_business ADD user_login varchar2(255);
UPDATE log_business lb SET lb.user_login = (SELECT login FROM SEC_USER su WHERE lb.USER_ID = su.ID);
ALTER TABLE log_business drop column user_id;
--ALTER TABLE log_business drop CONSTRAINT LOG_BUSINESS_FK_USER_ID;
ALTER TABLE log_business ADD CONSTRAINT log_business_fk_user_login foreign key (user_login) references sec_user(login);

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-6979 - �������������� ���������� ����� LOG_SYSTEM c 0.3.8
ALTER TABLE log_system modify user_login not null;
ALTER TABLE log_business modify user_login not null;

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-7813 - �������� ��������� ������� IMPORT_TRANSPORT_FILE � UPLOAD_TRANSPORT_FILE � ��
ALTER TABLE log_system drop CONSTRAINT log_system_chk_event_id;
ALTER TABLE log_system drop CONSTRAINT log_system_chk_dcl_form;
ALTER TABLE log_system drop CONSTRAINT log_system_chk_rp;

ALTER TABLE log_system ADD CONSTRAINT log_system_chk_event_id check (event_id in (1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 203, 204, 205, 206, 207, 208, 209, 210, 301, 302, 303, 401, 402, 501, 502, 503, 601, 901, 902, 903));
ALTER TABLE log_system ADD CONSTRAINT log_system_chk_dcl_form check (event_id in (7, 11, 401, 402, 501, 502, 503, 601, 901, 902, 903) or declaration_type_id IS not null or (form_type_id IS not null and form_kind_id IS not null));
ALTER TABLE log_system ADD CONSTRAINT log_system_chk_rp check (event_id in (7, 11, 401, 402, 501, 502, 503, 601, 901, 902, 903) or report_period_name IS not null);

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-7840 - �������� �� ����� ��� �� ��������, ������� ������ ��������� ��� ������ � �������� ��� ������� ������
ALTER TABLE log_system ADD declaration_type_name varchar2(80);
ALTER TABLE log_system ADD form_type_name varchar2(1000);
ALTER TABLE log_system ADD form_department_id number(9);

COMMENT ON COLUMN LOG_SYSTEM.DECLARATION_TYPE_NAME IS '��� ����������';
COMMENT ON COLUMN LOG_SYSTEM.FORM_TYPE_NAME IS '��� ��������� �����';
COMMENT ON COLUMN LOG_SYSTEM.FORM_DEPARTMENT_ID IS '������������� ������������� ��������� �����/����������';

UPDATE log_system ls SET declaration_type_name = (SELECT name FROM declaration_type dt WHERE dt.id = ls.declaration_type_id), form_type_name = (SELECT name FROM form_type ft WHERE ft.id = ls.form_type_id);

ALTER TABLE LOG_SYSTEM drop CONSTRAINT LOG_SYSTEM_CHK_DCL_FORM;
ALTER TABLE LOG_SYSTEM ADD CONSTRAINT LOG_SYSTEM_CHK_DCL_FORM
  check (event_id in (7, 11, 401, 402, 501, 502, 503, 601, 901, 902, 903) or declaration_type_name IS not null or (form_type_name IS not null and form_kind_id IS not null));

ALTER TABLE log_system drop column declaration_type_id; 
ALTER TABLE log_system drop column form_type_id;

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-7906 - �������� ������� EVENT � ��������� �� ��� �� ��
CREATE TABLE event
(
id NUMBER(9) NOT NULL,
name VARCHAR(510) NOT NULL
);

COMMENT ON TABLE event IS '���������� ������� � �������';
COMMENT ON COLUMN event.id IS '������������� �������';
COMMENT ON COLUMN event.name IS '������������ �������';

INSERT ALL
  INTO event VALUES (1, '�������')
  INTO event VALUES (12, '����� ��������')
  INTO event VALUES (2, '�������')
  INTO event VALUES (3, '����������')
  INTO event VALUES (4, '��������')
  INTO event VALUES (5, '���������')
  INTO event VALUES (6, '���������')
  INTO event VALUES (7, '������ ������')
  INTO event VALUES (8, '��������� ������')
  INTO event VALUES (9, '��������� ���������� ������')
  INTO event VALUES (10, '������������ ��������')
  INTO event VALUES (11, '�������� �� �� "������� ���"')
  INTO event VALUES (101, '��������� �� "�������"')
  INTO event VALUES (102, '������� �� "����������" � "�������"')
  INTO event VALUES (103, '������� �� "����������"')
  INTO event VALUES (104, '������� �� "�������" � "����������"')
  INTO event VALUES (105, '������� �� "�������"')
  INTO event VALUES (106, '������� �� "�������" � "�������"')
  INTO event VALUES (107, '����������� �� "�������"')
  INTO event VALUES (108, '������� �� "������������" � "�������"')
  INTO event VALUES (109, '������� �� "������������"')
  INTO event VALUES (110, '������� �� "�������" � "������������"')
  INTO event VALUES (111, '��������� �� "������������"')
  INTO event VALUES (112, '������� �� "����������" � "������������"')
  INTO event VALUES (203, '����� ������� �� "����������"')
  INTO event VALUES (204, '����� ������� �� "�������" � "����������"')
  INTO event VALUES (205, '����� ������� �� "�������"')
  INTO event VALUES (206, '����� ������� �� "�������" � "�������"')
  INTO event VALUES (207, '����� ������� �� "������������"')
  INTO event VALUES (208, '����� ������� �� "�������" � "������������"')
  INTO event VALUES (209, '����� ��������� �� "������������"')
  INTO event VALUES (210, '����� ������� "������������" �� "����������"')
  INTO event VALUES (301, '�������� ������')
  INTO event VALUES (303, '������� ������')
  INTO event VALUES (302, '��������')
  INTO event VALUES (401, '������ �� ������������ ������')
  INTO event VALUES (402, '�������� ������������ ������ � ������� ��������')
  INTO event VALUES (501, '���� ������������ � �������')
  INTO event VALUES (502, '����� ������������ �� �������')
  INTO event VALUES (503, '�������������� � ������� ��')
  INTO event VALUES (601, '������������� ������� �������')
  INTO event VALUES (901, '�������� �������������')
  INTO event VALUES (902, '����������� �������������')
  INTO event VALUES (903, '�������� �������������')
  INTO event VALUES (701, '������ �������')
  INTO event VALUES (702, '������ ��������')
  INTO event VALUES (703, '������ ������� � ��������')
  INTO event VALUES (704, '������ �������� �� ��������')
  INTO event VALUES (705, '������ �������')
SELECT * FROM dual;  

ALTER TABLE event ADD CONSTRAINT event_pk PRIMARY KEY (id);
ALTER TABLE log_system ADD CONSTRAINT log_system_fk_event_id FOREIGN KEY (event_id) REFERENCES event(id);
ALTER TABLE log_system DROP CONSTRAINT log_system_chk_event_id;

-- http://jira.aplana.com/browse/SBRFACCTAX-8319 ��������� ��� template_changes, ���������� � ��������� ������� EVENT
ALTER TABLE template_changes DROP CONSTRAINT changes_check_event;
ALTER TABLE template_changes MODIFY event NUMBER(9);

UPDATE template_changes SET event=700+event WHERE event IN (1,2,3,4,5);

ALTER TABLE template_changes ADD CONSTRAINT template_changes_chk_event CHECK (event in (701, 702, 703, 704, 705));
ALTER TABLE template_changes ADD CONSTRAINT template_changes_fk_event FOREIGN KEY (event) references event(id);
---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8032 - �������� ���� "������������� �� ������������� ��������� ����� / ����������" � ������� LOG_SYSTEM
ALTER TABLE log_system ADD tb_department_id NUMBER(9);
COMMENT ON COLUMN log_system.tb_department_id IS '������������� �� ������������� ��������� �����/����������';

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-7971 - ��������� gui ������������
ALTER TABLE task_context ADD modification_date date not null;
COMMENT ON COLUMN task_context.modification_date IS '���� ���������� �������������� ������';

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8269 - ���. ��������� ������������
ALTER TABLE task_context ADD user_id NUMBER(9) NOT NULL;
ALTER TABLE task_context ADD CONSTRAINT task_context_fk_user_id foreign key (user_id) references sec_user(id);
COMMENT ON COLUMN task_context.user_id IS '������������� ������������';

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-7330 - ������� ���� "data_size" �� ������� "blob_data"
ALTER TABLE blob_data DROP COLUMN data_size;

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-6298 - ����������� ������� ����� � 0.3.5
ALTER TABLE data_row ADD CONSTRAINT data_row_chk_manual CHECK (manual IN (0, 1));

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-7553 - �������� ���������� �������� ��� "��� �������������"
ALTER TABLE department MODIFY CODE DEFAULT NULL;

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8183 - �������������� ����������� � ����������
COMMENT ON COLUMN declaration_data.data IS '������ ���������� � ������� ������������ (XML)';
COMMENT ON COLUMN declaration_data.data_pdf IS '������ ���������� � ������� PDF';
COMMENT ON COLUMN declaration_data.data_xlsx IS '������ ���������� � ������� XLSX';
COMMENT ON COLUMN declaration_template.jrxml IS '����� JasperReports ��� ������������ ��������� ������������� �����';
COMMENT ON COLUMN declaration_template.version IS '������';
COMMENT ON COLUMN form_template.version IS '������ ����� (��������� � ������ ����)';
COMMENT ON COLUMN log_business.user_login IS '����� ������������';
COMMENT ON COLUMN log_system.user_login IS '����� ������������';
COMMENT ON COLUMN notification.id IS '���������� ������������� ����������';
COMMENT ON COLUMN ref_book.region_attribute_id IS '��� ��� ������� ���������� ��������� ������������. ��������� �� �������, �� �������� ������������ �������������� � �������';
COMMENT ON COLUMN task_context.id IS '���������� ������������� ������';
COMMENT ON COLUMN template_changes.id IS '���������� ������������� ������';
COMMENT ON COLUMN data_row.type IS '��� ������ (0 - �������������� ������, 1 - ������ ���������, -1 - ������ �������)';
COMMENT ON COLUMN department.region_id IS '��� �������';
COMMENT ON COLUMN form_column.numeration_row IS '��� ��������� ����� ��� �������������� ����� (0 - ����������������, 1 - ��������)';
COMMENT ON COLUMN log_system.department_name IS '������������ ������������� ��\����������';

---------------------------------------------------------------------------------------------------------
commit;
