---------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8333: Уничтожить ограничение ref_book_attr_chk_is_unique
ALTER TABLE ref_book_attribute DROP CONSTRAINT ref_book_attr_chk_is_unique;

---------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8465: Добавить поле "название таблицы" в таблицу REF_BOOK
ALTER TABLE ref_book ADD table_name VARCHAR2(100);
COMMENT ON COLUMN ref_book.table_name IS 'Название таблицы БД, в которой хранятся данные';

---------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8376: Перенести справочник RefBookDepartmentType в базу данных
CREATE TABLE department_type
(
id NUMBER(9) NOT NULL,
name VARCHAR2(50 BYTE)
);

COMMENT ON TABLE department_type IS 'Типы подразделений банка';
COMMENT ON COLUMN department_type.id IS 'Идентификатор типа';
COMMENT ON COLUMN department_type.name IS 'Наименование типа';

ALTER TABLE department_type ADD CONSTRAINT department_type_pk PRIMARY KEY (id);

INSERT ALL
	INTO department_type (id, name) VALUES (1, 'Банк')
	INTO department_type (id, name) VALUES (2, 'Территориальный банк')
	INTO department_type (id, name) VALUES (3, 'ЦСКО, ПЦП')
	INTO department_type (id, name) VALUES (4, 'Управление')
	INTO department_type (id, name) VALUES (5, NULL)
SELECT * FROM dual;	

ALTER TABLE department DROP CONSTRAINT dept_chk_type;
ALTER TABLE department ADD CONSTRAINT department_fk_type FOREIGN KEY(type) REFERENCES department_type(id);

-- http://jira.aplana.com/browse/SBRFACCTAX-8678 - 0.4 Доработка проверки корректности подразделения
ALTER TABLE department DROP CONSTRAINT department_uniq_code;
DROP INDEX department_uniq_code;

---------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-7074 - Реализовать хранение в ЖА ссылок на LogEntry-сущности в BLOB_DATA с возможностью просмотра
ALTER TABLE log_system ADD blob_data_id VARCHAR2(36);
ALTER TABLE log_system ADD CONSTRAINT log_system_fk_blob_data FOREIGN KEY (blob_data_id) REFERENCES blob_data(id) ON DELETE SET NULL;
COMMENT ON COLUMN log_system.blob_data_id IS 'Ссылка на логи';

---------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8880 - Актуализация реализации журнала аудита
ALTER TABLE log_system ADD form_type_id NUMBER(9);
COMMENT ON COLUMN log_system.form_type_id is 'Идентификатор вида НФ';
ALTER TABLE log_system DROP COLUMN tb_department_id;

---------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8471 - Добавить в LOG_SYSTEM поле IS_ERROR
ALTER TABLE log_system ADD is_error number(1) DEFAULT 0 NOT NULL;
COMMENT ON COLUMN log_system.is_error IS 'Признак ошибки';
ALTER TABLE log_system ADD CONSTRAINT log_system_chk_is_error CHECK (is_error IN (0, 1));

---------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8403 - Изменения таблицы DECLARATION_DATA для налога на имущество
ALTER TABLE declaration_data ADD tax_organ_code VARCHAR2(4);
ALTER TABLE declaration_data ADD kpp VARCHAR2(9);

COMMENT ON COLUMN declaration_data.tax_organ_code IS 'Налоговый орган';
COMMENT ON COLUMN declaration_data.kpp IS 'КПП';

---------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8738 - Удаление поля BLOB_DATA.TYPE
ALTER TABLE blob_data DROP CONSTRAINT blob_data_chk_type;
ALTER TABLE blob_data DROP COLUMN type;

---------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8759 - Таблица для асинхронных задач
CREATE TABLE async_task_type
(
id NUMBER(18) NOT NULL,
name VARCHAR2(100) NOT NULL,
handler_jndi VARCHAR2(500) NOT NULL
);

ALTER TABLE async_task_type ADD CONSTRAINT async_task_type_pk primary key (id);

COMMENT ON TABLE async_task_type IS 'Типы асинхронных задач';
COMMENT ON COLUMN async_task_type.id IS 'Идентификатор строки';
COMMENT ON COLUMN async_task_type.name IS 'Название типа задачи';
COMMENT ON COLUMN async_task_type.handler_jndi IS 'JNDI имя класса-обработчика';

---------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8815 - Новые типы события для ЖА
-- С изменениями от http://jira.aplana.com/browse/SBRFACCTAX-9230 и http://jira.aplana.com/browse/SBRFACCTAX-9232
INSERT ALL 
INTO event (id, name) VALUES(810,'Гарантии: Загрузка данных о договорах обеспечения') 
INTO event (id, name) VALUES(811,'Гарантии: Загрузка данных о клиентах') 
INTO event (id, name) VALUES(812,'Гарантии: Загрузка данных о платежах') 
INTO event (id, name) VALUES(813,'Гарантии: Загрузка справочника') 
INTO event (id, name) VALUES(820,'Гарантии: Создание анкеты клиента') 
INTO event (id, name) VALUES(821,'Гарантии: Редактирование анкеты клиента') 
INTO event (id, name) VALUES(830,'Гарантии: Создание договора гарантии') 
INTO event (id, name) VALUES(831,'Гарантии: Редактирование договора гарантии') 
INTO event (id, name) VALUES(832,'Гарантии: Закрытие договора гарантии') 
INTO event (id, name) VALUES(840,'Гарантии: Создание договора обеспечения') 
INTO event (id, name) VALUES(841,'Гарантии: Редактирование договора обеспечения') 
INTO event (id, name) VALUES(842,'Гарантии: Закрытие договора обеспечения') 
INTO event (id, name) VALUES(850,'Гарантии: Создание задачи формирования РНУ-23') 
INTO event (id, name) VALUES(860,'Гарантии: Создание задачи формирования отчета')
INTO event (id, name) VALUES(650,'Отправка email')
SELECT * FROM dual;

ALTER TABLE log_system DROP CONSTRAINT log_system_chk_dcl_form;
ALTER TABLE log_system ADD CONSTRAINT log_system_chk_dcl_form CHECK (event_id IN (7, 11, 401, 402, 501, 502, 503, 601, 650, 901, 902, 903, 810, 811, 812, 813, 820, 821, 830, 831, 832, 840, 841, 842, 850, 860) OR declaration_type_name IS NOT NULL OR (form_type_name IS NOT NULL AND form_kind_id IS NOT NULL));

ALTER TABLE log_system DROP CONSTRAINT log_system_chk_rp;
ALTER TABLE log_system ADD CONSTRAINT log_system_chk_rp CHECK (event_id IN (7, 11, 401, 402, 501, 502, 503, 601, 650, 901, 902, 903, 810, 811, 812, 813, 820, 821, 830, 831, 832, 840, 841, 842, 850, 860) OR report_period_name IS NOT NULL);

---------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-9170 - Добавить роль "Оператор гарантий"
INSERT INTO sec_role (id, ALIAS, name) VALUES (7, 'ROLE_GARANT', 'Оператор Гарантий');

---------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-9017 - Связь таблиц EVENT и SEC_ROLE
CREATE TABLE role_event (event_id number(9) NOT NULL, role_id number(9) NOT NULL);

COMMENT ON TABLE role_event IS 'Настройка прав доступа к событиям журнала аудита по ролям';
COMMENT ON COLUMN role_event.event_id IS 'Идентификатор события';
COMMENT ON COLUMN role_event.role_id IS 'Идентификатор роли';

ALTER TABLE role_event ADD CONSTRAINT role_event_pk PRIMARY KEY (event_id, role_id);
ALTER TABLE role_event ADD CONSTRAINT role_event_fk_event_id FOREIGN KEY (event_id) REFERENCES event(id);
ALTER TABLE role_event ADD CONSTRAINT role_event_fk_role_id FOREIGN KEY (role_id) REFERENCES sec_role(id);

INSERT INTO role_event(event_id, role_id) (SELECT id, 3 FROM event WHERE id NOT IN (501, 502, 601, 701));
INSERT INTO role_event(event_id, role_id) (SELECT id, 5 FROM event);

INSERT INTO role_event(event_id, role_id)
 (SELECT id, 2 FROM event
  WHERE id IN (1, 2, 3, 6, 7)
   OR to_char(id) LIKE '10_'
   OR to_char(id) LIKE '40_'
   OR to_char(id) LIKE '90_');

INSERT INTO role_event(event_id, role_id)
 (SELECT id, 1 FROM event
  WHERE id IN (1, 2, 3, 6, 7)
   OR to_char(id) LIKE '10_'
   OR to_char(id) LIKE '40_'
   OR to_char(id) LIKE '90_');

INSERT INTO role_event(event_id, role_id)
 (SELECT id, 6 FROM event
  WHERE id IN (1, 2, 3, 6, 7)
   OR to_char(id) LIKE '10_'
   OR to_char(id) LIKE '40_'
   OR to_char(id) LIKE '90_');
   
INSERT INTO role_event SELECT id AS event_id, 7 AS role_id FROM event WHERE to_char(id) LIKE '8__';   

ALTER TABLE log_business ADD CONSTRAINT log_business_fk_event_id FOREIGN KEY (event_id) REFERENCES event(id);

---------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8512 - Переход на новый механизм блокировок
DROP TABLE object_lock;

---------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8861 - Связь НФ/деклараций с таблицей BLOB_DATA
CREATE TABLE form_data_report (form_data_id number(18) NOT NULL, blob_data_id varchar2(36) NOT NULL, type number(1) NOT NULL, checking number(1) NOT NULL, manual number(1) NOT NULL, absolute number(1) NOT NULL);

COMMENT ON TABLE form_data_report IS 'Отчет';
COMMENT ON COLUMN form_data_report.form_data_id IS 'Идентификатор налоговой формы';
COMMENT ON COLUMN form_data_report.blob_data_id IS 'Идентификатор отчета';
COMMENT ON COLUMN form_data_report.TYPE IS 'Тип отчета (0 - Excel, 1 - CSV, 2 - PDF, 3 - Jasper)';
COMMENT ON COLUMN form_data_report.manual IS 'Режим ввода данных (0 - обычная версия; 1 - версия ручного ввода)';
COMMENT ON COLUMN form_data_report.checking IS 'Типы столбцов (0 - только обычные, 1 - вместе с контрольными)';
COMMENT ON COLUMN form_data_report.ABSOLUTE IS 'Режим вывода данных (0 - только дельты, 1 - абсолютные значения)';

alter table form_data_report add constraint form_data_rep_pk primary key (form_data_id,type, manual,checking,absolute);
alter table form_data_report add constraint form_data_rep_fk_form_data_id foreign key (form_data_id) references form_data(id) on delete cascade;
alter table form_data_report add constraint form_data_rep_fk_blob_data_id foreign key (blob_data_id) references blob_data(id);
alter table form_data_report add constraint form_data_rep_chk_type check (type in (0,1,2,3));
alter table form_data_report add constraint form_data_rep_chk_manual check (manual in (0,1));
alter table form_data_report add constraint form_data_rep_chk_checking check (checking in (0,1));
alter table form_data_report add constraint form_data_rep_chk_absolute check (absolute in (0,1));

------------------------------------------------------------------------------------------------------------------
create table declaration_report
(
declaration_data_id number(18) not null,
blob_data_id varchar2(36),
type number(1) not null
);

comment on table declaration_report is 'Отчеты по декларациям';
comment on column declaration_report.declaration_data_id is 'Идентификатор декларации';
comment on column declaration_report.blob_data_id is 'Идентификатор отчета';
comment on column declaration_report.type is 'Тип отчета (0 - Excel, 1 - XML, 2 - PDF, 3 - Jasper)';

alter table declaration_report add constraint decl_report_pk primary key (declaration_data_id, type);
alter table declaration_report add constraint decl_report_fk_decl_data foreign key(declaration_data_id) references declaration_data(id) on delete cascade;
alter table declaration_report add constraint decl_report_fk_blob_data foreign key(blob_data_id) references blob_data(id);
alter table declaration_report add constraint decl_report_chk_type check (type in (0, 1, 2, 3));

-- Перенос данных
insert into declaration_report (declaration_data_id, blob_data_id, type)
select id as declaration_data_id, data_xlsx as blob_data_id, 0 as type from declaration_data where data_xlsx is not null
union all
select id as declaration_data_id, data, 1 from declaration_data where data is not null
union all
select id as declaration_data_id, data_pdf, 2 from declaration_data where data_pdf is not null
union all
select id as declaration_data_id, jasper_print, 3 from declaration_data  where jasper_print is not null;

--Отложенные изменения
alter table declaration_data drop column data;
alter table declaration_data drop column data_xlsx;
alter table declaration_data drop column data_pdf;
alter table declaration_data drop column jasper_print;

---------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8879 - Функционал оповещения
ALTER TABLE notification MODIFY report_period_id NULL;
ALTER TABLE notification MODIFY sender_department_id NULL;
ALTER TABLE notification MODIFY deadline NULL;
ALTER TABLE notification ADD user_id number(9);
COMMENT ON COLUMN notification.user_id IS 'Идентификатор пользователя, который получит оповещение';
ALTER TABLE notification ADD CONSTRAINT notification_fk_notify_user FOREIGN KEY (user_id) REFERENCES sec_user(id);
ALTER TABLE notification ADD role_id number(9);
COMMENT ON COLUMN notification.role_id IS 'Идентификатор роли пользователя, который получит оповещение';
ALTER TABLE notification ADD CONSTRAINT notification_fk_notify_role FOREIGN KEY (role_id) REFERENCES sec_role(id);

ALTER TABLE notification DROP COLUMN first_reader_id;
ALTER TABLE notification ADD is_read number(1) default 0 not null;
COMMENT ON COLUMN notification.is_read IS 'Признак прочтения';
ALTER TABLE notification ADD CONSTRAINT notification_chk_isread CHECK (is_read in (0, 1));

CREATE TABLE lock_data_subscribers (lock_key varchar2(1000 byte) NOT NULL, user_id number(9) NOT NULL);
COMMENT ON TABLE lock_data_subscribers IS 'Cписок пользователей, ожидающих выполнения операций над объектом блокировки';
COMMENT ON COLUMN lock_data_subscribers.lock_key IS 'Ключ блокировки объекта, после завершения операции над которым, будет выполнено оповещение';
COMMENT ON COLUMN lock_data_subscribers.user_id IS 'Идентификатор пользователя, который получит оповещение';
ALTER TABLE lock_data_subscribers ADD CONSTRAINT lock_data_subscr_fk_lock_data FOREIGN KEY (lock_key) REFERENCES lock_data(KEY) ON DELETE CASCADE;
ALTER TABLE lock_data_subscribers ADD CONSTRAINT lock_data_subscr_fk_sec_user FOREIGN KEY (user_id) REFERENCES sec_user(id) ON DELETE CASCADE;

---------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8809: Новые связи для form_data и declaration_data с department_report_period

ALTER TABLE form_data ADD department_report_period_id number(18);
ALTER TABLE declaration_data ADD department_report_period_id number(18);

COMMENT ON COLUMN form_data.department_report_period_id IS 'Идентификатор отчетного периода подразделения';
COMMENT ON COLUMN declaration_data.department_report_period_id IS 'Идентификатор отчетного периода подразделения';

MERGE INTO form_data tgt USING
  (SELECT fd.id,
          drp.id AS department_period_id
   FROM form_data fd
   LEFT JOIN department_report_period drp ON drp.department_id = fd.department_id
   AND fd.report_period_id = drp.report_period_id
   AND drp.correction_date IS NULL) src ON (tgt.id = src.id) WHEN matched THEN
UPDATE
SET tgt.department_report_period_id = src.department_period_id;

MERGE INTO declaration_data tgt USING
  (SELECT fd.id,
          drp.id AS department_period_id
   FROM declaration_data fd
   LEFT JOIN department_report_period drp ON drp.department_id = fd.department_id
   AND fd.report_period_id = drp.report_period_id
   AND drp.correction_date IS NULL) src ON (tgt.id = src.id) WHEN matched THEN
UPDATE
SET tgt.department_report_period_id = src.department_period_id;

ALTER TABLE form_data MODIFY department_report_period_id NOT NULL;
ALTER TABLE declaration_data MODIFY department_report_period_id NOT NULL;

ALTER TABLE form_data ADD CONSTRAINT FORM_DATA_FK_DEP_REP_PER_ID FOREIGN KEY (department_report_period_id) REFERENCES department_report_period(id);
ALTER TABLE declaration_data ADD CONSTRAINT DECL_DATA_FK_DEP_REP_PER_ID FOREIGN KEY (department_report_period_id) REFERENCES department_report_period(id);

CREATE INDEX i_form_data_dep_rep_per_id ON form_data (department_report_period_id);
CREATE INDEX i_decl_data_dep_rep_per_id ON declaration_data (department_report_period_id);

ALTER TABLE declaration_data DROP CONSTRAINT declaration_data_uniq_template;
ALTER TABLE form_data DROP COLUMN department_id;
ALTER TABLE form_data DROP COLUMN report_period_id;
ALTER TABLE declaration_data DROP COLUMN department_id;
ALTER TABLE declaration_data DROP COLUMN report_period_id;
ALTER TABLE declaration_data ADD CONSTRAINT declaration_data_uniq_template UNIQUE (department_report_period_id, declaration_template_id, tax_organ_code, kpp);

---------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-9184: Обновление таблицы NOTIFICATION для хранения ссылок на логи

ALTER TABLE notification ADD blob_data_id varchar2(36);
ALTER TABLE notification ADD CONSTRAINT notification_fk_blob_data_id FOREIGN KEY (blob_data_id) REFERENCES blob_data(id);
COMMENT ON COLUMN notification.blob_data_id IS 'Ссылка на логи';
---------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-9217: Изменения по таблице LOG_BUSINESS

alter table log_business drop constraint log_business_fk_user_login;
alter table log_business drop constraint log_business_fk_usr_departm_id;

alter table log_business add user_department_name varchar2(4000);
comment on column log_business.user_department_name is 'Подразделение пользователя';

merge into log_business lg
using
(
select id, substr(sys_connect_by_path(name, '/'), 2) as fullname
from department
start with parent_id = 0
connect by parent_id = prior id
union all
select id, name from department where id = 0
) d
on
(lg.user_department_id = d.id)
when matched then
     update set lg.user_department_name = d.fullname;


alter table log_business modify user_department_name not null;
alter table log_business drop column user_department_id;

---------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-9218: Таблица для МСФО
create table ifrs_data 
(
report_period_id number(9) not null,
blob_data_id varchar2(36)
);

comment on table ifrs_data is 'Отчетность для МСФО';
comment on column ifrs_data.report_period_id is 'Отчетный период';
comment on column ifrs_data.blob_data_id is 'Файл архива с отчетностью для МСФО';

alter table ifrs_data add constraint ifrs_data_pk primary key (report_period_id);
alter table ifrs_data add constraint ifrs_data_fk_report_period foreign key (report_period_id) references report_period(id);
alter table ifrs_data add constraint ifrs_data_fk_blob_data foreign key (blob_data_id) references blob_data(id);

---------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-9254: Обновление таблиц макетов НФ/деклараций

ALTER TABLE form_type ADD is_ifrs NUMBER(1) default 0 not null;
ALTER TABLE declaration_type ADD is_ifrs NUMBER(1) default 0 not null;

ALTER TABLE form_type add constraint form_type_chk_is_ifrs check ((is_ifrs in (0,1) and tax_type='I') or (is_ifrs = 0 and tax_type<>'I'));
ALTER TABLE declaration_type add constraint declaration_type_chk_is_ifrs check ((is_ifrs in (0,1) and tax_type='I') or (is_ifrs = 0 and tax_type<>'I'));

COMMENT ON COLUMN form_type.is_ifrs is 'Отчетность для МСФО" (0 - не отчетность МСФО, 1 - отчетность МСФО)';
COMMENT ON COLUMN declaration_type.is_ifrs is 'Отчетность для МСФО" (0 - не отчетность МСФО, 1 - отчетность МСФО)';

ALTER TABLE form_type ADD ifrs_name VARCHAR2(200);
ALTER TABLE declaration_type ADD ifrs_name VARCHAR2(200);

COMMENT ON COLUMN form_type.ifrs_name IS 'Наименование формы для файла данного макета, включаемого в архив с отчетностью для МСФО'; 
COMMENT ON COLUMN declaration_type.ifrs_name IS 'Наименование формы для файла данного макета, включаемого в архив с отчетностью для МСФО'; 

---------------------------------------------------------------------------------------------------
-- Оптимизация удаления из BLOB_DATA устаревших данных

create index i_ifrs_data_blob_data_id       on ifrs_data(blob_data_id);
create index i_form_data_rep_blob_data_id     on form_data_report(blob_data_id);
create index i_decl_report_blob_data_id      on declaration_report(blob_data_id);
create index i_log_system_blob_data_id       on log_system(blob_data_id);
create index i_ref_book_script_id         on ref_book(script_id);
create index i_declaration_template_xsd     on declaration_template(xsd);
create index i_declaration_template_jrxml    on declaration_template(jrxml);
create index i_notification_blob_data_id       on notification(blob_data_id);
---------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-9694: Справочник "Настройки почты"
create table configuration_email
(
id number(9) not null,
name varchar2(200) not null,
value varchar2(200),
description varchar2(1000) 
);

comment on table configuration_email is 'Настройки почты';
comment on column configuration_email.id is 'Идентификатор записи';
comment on column configuration_email.name is 'Код параметра';
comment on column configuration_email.value is 'Значение параметра';
comment on column configuration_email.description is 'Описание параметра';

alter table configuration_email add constraint configuration_email_pk primary key (id);
alter table configuration_email add constraint configuration_email_unqname unique (name);

INSERT INTO configuration_email (id, name, value, description) values (1, 'mail.smtp.user', '', 'Default user name for SMTP.');
INSERT INTO configuration_email (id, name, value, description) values (2, 'mail.smtp.host', '', 'The SMTP server to connect to.');
INSERT INTO configuration_email (id, name, value, description) values (3, 'mail.smtp.port', '', 'The SMTP server port to connect to, if the connect() method doesn''t explicitly specify one. Defaults to 25.');
INSERT INTO configuration_email (id, name, value, description) values (4, 'mail.smtp.connectiontimeout', '', 'Socket connection timeout value in milliseconds. This timeout is implemented by java.net.Socket. Default is infinite timeout.');
INSERT INTO configuration_email (id, name, value, description) values (5, 'mail.smtp.timeout', '', 'Socket read timeout value in milliseconds. This timeout is implemented by java.net.Socket. Default is infinite timeout.');
INSERT INTO configuration_email (id, name, value, description) values (6, 'mail.smtp.writetimeout', '', 'Socket write timeout value in milliseconds. This timeout is implemented by using a java.util.concurrent.ScheduledExecutorService per connection that schedules a thread to close the socket if the timeout expires. Thus, the overhead of using this timeout is one thread per connection. Default is infinite timeout.');
INSERT INTO configuration_email (id, name, value, description) values (7, 'mail.smtp.from', '', 'Email address to use for SMTP MAIL command. This sets the envelope return address. Defaults to msg.getFrom() or InternetAddress.getLocalAddress(). NOTE: mail.smtp.user was previously used for this.');
INSERT INTO configuration_email (id, name, value, description) values (8, 'mail.smtp.localhost', '', 'Local host name used in the SMTP HELO or EHLO command. Defaults to InetAddress.getLocalHost().getHostName(). Should not normally need to be set if your JDK and your name service are configured properly.');
INSERT INTO configuration_email (id, name, value, description) values (9, 'mail.smtp.localaddress', '', 'Local address (host name) to bind to when creating the SMTP socket. Defaults to the address picked by the Socket class. Should not normally need to be set, but useful with multi-homed hosts where it''s important to pick a particular local address to bind to.');
INSERT INTO configuration_email (id, name, value, description) values (10, 'mail.smtp.localport', '', 'Local port number to bind to when creating the SMTP socket. Defaults to the port number picked by the Socket class.');
INSERT INTO configuration_email (id, name, value, description) values (11, 'mail.smtp.ehlo', '', 'If false, do not attempt to sign on with the EHLO command. Defaults to true. Normally failure of the EHLO command will fallback to the HELO command; this property exists only for servers that don''t fail EHLO properly or don''t implement EHLO properly.');
INSERT INTO configuration_email (id, name, value, description) values (12, 'mail.smtp.auth', '', 'If true, attempt to authenticate the user using the AUTH command. Defaults to false.');
INSERT INTO configuration_email (id, name, value, description) values (13, 'mail.smtp.auth.mechanisms', '', 'If set, lists the authentication mechanisms to consider, and the order in which to consider them. Only mechanisms supported by the server and supported by the current implementation will be used. The default is "LOGIN PLAIN DIGEST-MD5 NTLM", which includes all the authentication mechanisms supported by the current implementation.');
INSERT INTO configuration_email (id, name, value, description) values (14, 'mail.smtp.auth.login.disable', '', 'If true, prevents use of the AUTH LOGIN command. Default is false.');
INSERT INTO configuration_email (id, name, value, description) values (15, 'mail.smtp.auth.plain.disable', '', 'If true, prevents use of the AUTH PLAIN command. Default is false.');
INSERT INTO configuration_email (id, name, value, description) values (16, 'mail.smtp.auth.digest-md5.disable', '', 'If true, prevents use of the AUTH DIGEST-MD5 command. Default is false.');
INSERT INTO configuration_email (id, name, value, description) values (17, 'mail.smtp.auth.ntlm.disable', '', 'If true, prevents use of the AUTH NTLM command. Default is false.');
INSERT INTO configuration_email (id, name, value, description) values (18, 'mail.smtp.auth.ntlm.domain', '', 'The NTLM authentication domain.');
INSERT INTO configuration_email (id, name, value, description) values (19, 'mail.smtp.auth.ntlm.flags', '', 'NTLM protocol-specific flags. See http://curl.haxx.se/rfc/ntlm.html#theNtlmFlags for details.');
INSERT INTO configuration_email (id, name, value, description) values (20, 'mail.smtp.submitter', '', 'The submitter to use in the AUTH tag in the MAIL FROM command. Typically used by a mail relay to pass along information about the original submitter of the message. See also the setSubmitter method of SMTPMessage. Mail clients typically do not use this.');
INSERT INTO configuration_email (id, name, value, description) values (21, 'mail.smtp.dsn.notify', '', 'The NOTIFY option to the RCPT command. Either NEVER, or some combination of SUCCESS, FAILURE, and DELAY (separated by commas).');
INSERT INTO configuration_email (id, name, value, description) values (22, 'mail.smtp.dsn.ret', '', 'The RET option to the MAIL command. Either FULL or HDRS.');
INSERT INTO configuration_email (id, name, value, description) values (23, 'mail.smtp.allow8bitmime', '', 'If set to true, and the server supports the 8BITMIME extension, text parts of messages that use the "quoted-printable" or "base64" encodings are converted to use "8bit" encoding if they follow the RFC2045 rules for 8bit text.');
INSERT INTO configuration_email (id, name, value, description) values (24, 'mail.smtp.sendpartial', '', 'If set to true, and a message has some valid and some invalid addresses, send the message anyway, reporting the partial failure with a SendFailedException. If set to false (the default), the message is not sent to any of the recipients if there is an invalid recipient address.');
INSERT INTO configuration_email (id, name, value, description) values (25, 'mail.smtp.sasl.enable', '', 'If set to true, attempt to use the javax.security.sasl package to choose an authentication mechanism for login. Defaults to false.');
INSERT INTO configuration_email (id, name, value, description) values (26, 'mail.smtp.sasl.mechanisms', '', 'A space or comma separated list of SASL mechanism names to try to use.');
INSERT INTO configuration_email (id, name, value, description) values (27, 'mail.smtp.sasl.authorizationid', '', 'The authorization ID to use in the SASL authentication. If not set, the authentication ID (user name) is used.');
INSERT INTO configuration_email (id, name, value, description) values (28, 'mail.smtp.sasl.realm', '', 'The realm to use with DIGEST-MD5 authentication.');
INSERT INTO configuration_email (id, name, value, description) values (29, 'mail.smtp.sasl.usecanonicalhostname', '', 'If set to true, the canonical host name returned by InetAddress.getCanonicalHostName is passed to the SASL mechanism, instead of the host name used to connect. Defaults to false.');
INSERT INTO configuration_email (id, name, value, description) values (30, 'mail.smtp.quitwait', '', 'If set to false, the QUIT command is sent and the connection is immediately closed. If set to true (the default), causes the transport to wait for the response to the QUIT command.');
INSERT INTO configuration_email (id, name, value, description) values (31, 'mail.smtp.reportsuccess', '', 'If set to true, causes the transport to include an SMTPAddressSucceededException for each address that is successful. Note also that this will cause a SendFailedException to be thrown from the sendMessage method of SMTPTransport even if all addresses were correct and the message was sent successfully.');
INSERT INTO configuration_email (id, name, value, description) values (32, 'mail.smtp.socketFactory', '', 'If set to a class that implements the javax.net.SocketFactory interface, this class will be used to create SMTP sockets. Note that this is an instance of a class, not a name, and must be set using the put method, not the setProperty method.');
INSERT INTO configuration_email (id, name, value, description) values (33, 'mail.smtp.socketFactory.class', '', 'If set, specifies the name of a class that implements the javax.net.SocketFactory interface. This class will be used to create SMTP sockets.');
INSERT INTO configuration_email (id, name, value, description) values (34, 'mail.smtp.socketFactory.fallback', '', 'If set to true, failure to create a socket using the specified socket factory class will cause the socket to be created using the java.net.Socket class. Defaults to true.');
INSERT INTO configuration_email (id, name, value, description) values (35, 'mail.smtp.socketFactory.port', '', 'Specifies the port to connect to when using the specified socket factory. If not set, the default port will be used.');
INSERT INTO configuration_email (id, name, value, description) values (36, 'mail.smtp.ssl.enable', '', 'If set to true, use SSL to connect and use the SSL port by default. Defaults to false for the "smtp" protocol and true for the "smtps" protocol.');
INSERT INTO configuration_email (id, name, value, description) values (37, 'mail.smtp.ssl.checkserveridentity', '', 'If set to true, check the server identity as specified by RFC 2595. These additional checks based on the content of the server''s certificate are intended to prevent man-in-the-middle attacks. Defaults to false.');
INSERT INTO configuration_email (id, name, value, description) values (38, 'mail.smtp.ssl.trust', '', 'If set, and a socket factory hasn''t been specified, enables use of a MailSSLSocketFactory. If set to "*", all hosts are trusted. If set to a whitespace separated list of hosts, those hosts are trusted. Otherwise, trust depends on the certificate the server presents.');
INSERT INTO configuration_email (id, name, value, description) values (39, 'mail.smtp.ssl.socketFactory', '', 'If set to a class that extends the javax.net.ssl.SSLSocketFactory class, this class will be used to create SMTP SSL sockets. Note that this is an instance of a class, not a name, and must be set using the put method, not the setProperty method.');
INSERT INTO configuration_email (id, name, value, description) values (40, 'mail.smtp.ssl.socketFactory.class', '', 'If set, specifies the name of a class that extends the javax.net.ssl.SSLSocketFactory class. This class will be used to create SMTP SSL sockets.');
INSERT INTO configuration_email (id, name, value, description) values (41, 'mail.smtp.ssl.socketFactory.port', '', 'Specifies the port to connect to when using the specified socket factory. If not set, the default port will be used.');
INSERT INTO configuration_email (id, name, value, description) values (42, 'mail.smtp.ssl.protocols', '', 'Specifies the SSL protocols that will be enabled for SSL connections. The property value is a whitespace separated list of tokens acceptable to the javax.net.ssl.SSLSocket.setEnabledProtocols method.');
INSERT INTO configuration_email (id, name, value, description) values (43, 'mail.smtp.ssl.ciphersuites', '', 'Specifies the SSL cipher suites that will be enabled for SSL connections. The property value is a whitespace separated list of tokens acceptable to the javax.net.ssl.SSLSocket.setEnabledCipherSuites method.');
INSERT INTO configuration_email (id, name, value, description) values (44, 'mail.smtp.starttls.enable', '', 'If true, enables the use of the STARTTLS command (if supported by the server) to switch the connection to a TLS-protected connection before issuing any login commands. Note that an appropriate trust store must configured so that the client will trust the server''s certificate. Defaults to false.');
INSERT INTO configuration_email (id, name, value, description) values (45, 'mail.smtp.starttls.required', '', 'If true, requires the use of the STARTTLS command. If the server doesn''t support the STARTTLS command, or the command fails, the connect method will fail. Defaults to false.');
INSERT INTO configuration_email (id, name, value, description) values (46, 'mail.smtp.socks.host', '', 'Specifies the host name of a SOCKS5 proxy server that will be used for connections to the mail server. (Note that this only works on JDK 1.5 or newer.)');
INSERT INTO configuration_email (id, name, value, description) values (47, 'mail.smtp.socks.port', '', 'Specifies the port number for the SOCKS5 proxy server. This should only need to be used if the proxy server is not using the standard port number of 1080.');
INSERT INTO configuration_email (id, name, value, description) values (48, 'mail.smtp.mailextension', '', 'Extension string to append to the MAIL command. The extension string can be used to specify standard SMTP service extensions as well as vendor-specific extensions. Typically the application should use the SMTPTransport method supportsExtension to verify that the server supports the desired service extension. See RFC 1869 and other RFCs that define specific extensions.');
INSERT INTO configuration_email (id, name, value, description) values (49, 'mail.smtp.userset', '', 'If set to true, use the RSET command instead of the NOOP command in the isConnected method. In some cases sendmail will respond slowly after many NOOP commands; use of RSET avoids this sendmail issue. Defaults to false.');
INSERT INTO configuration_email (id, name, value, description) values (50, 'mail.smtp.noop.strict', '', 'If set to true (the default), insist on a 250 response code from the NOOP command to indicate success. The NOOP command is used by the isConnected method to determine if the connection is still alive. Some older servers return the wrong response code on success, some servers don''t implement the NOOP command at all and so always return a failure code. Set this property to false to handle servers that are broken in this way. Normally, when a server times out a connection, it will send a 421 response code, which the client will see as the response to the next command it issues. Some servers send the wrong failure response code when timing out a connection. Do not set this property to false when dealing with servers that are broken in this way.');

---------------------------------------------------------------------------------------------------
COMMIT;
EXIT;