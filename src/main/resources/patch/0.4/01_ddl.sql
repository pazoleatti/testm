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
INSERT ALL 
INTO event (id, name) VALUES(801,'Вход пользователя в модуль') 
INTO event (id, name) VALUES(802,'Выход пользователя из модуля') 
INTO event (id, name) VALUES(810,'Загрузка данных о договорах обеспечения') 
INTO event (id, name) VALUES(811,'Загрузка данных о клиентах') 
INTO event (id, name) VALUES(812,'Загрузка данных о платежах') 
INTO event (id, name) VALUES(813,'Загрузка справочника') 
INTO event (id, name) VALUES(820,'Создание анкеты клиента') 
INTO event (id, name) VALUES(821,'Редактирование анкеты клиента') 
INTO event (id, name) VALUES(830,'Создание договора гарантии') 
INTO event (id, name) VALUES(831,'Редактирование договора гарантии') 
INTO event (id, name) VALUES(832,'Закрытие договора гарантии') 
INTO event (id, name) VALUES(840,'Создание договора обеспечения') 
INTO event (id, name) VALUES(841,'Редактирование договора обеспечения') 
INTO event (id, name) VALUES(842,'Закрытие договора обеспечения') 
INTO event (id, name) VALUES(850,'Создание задачи формирования РНУ-23') 
INTO event (id, name) VALUES(860,'Создание задачи формирования отчета')
INTO event (id, name) VALUES(650,'Отправка email')
SELECT * FROM dual;

ALTER TABLE log_system DROP CONSTRAINT log_system_chk_dcl_form;
ALTER TABLE log_system ADD CONSTRAINT log_system_chk_dcl_form CHECK (event_id IN (7, 11, 401, 402, 501, 502, 503, 601, 650, 901, 902, 903, 801, 802, 810, 811, 812, 813, 820, 821, 830, 831, 832, 840, 841, 842, 850, 860) OR declaration_type_name IS NOT NULL OR (form_type_name IS NOT NULL AND form_kind_id IS NOT NULL));

ALTER TABLE log_system DROP CONSTRAINT log_system_chk_rp;
ALTER TABLE log_system ADD CONSTRAINT log_system_chk_rp CHECK (event_id IN (7, 11, 401, 402, 501, 502, 503, 601, 650, 901, 902, 903, 801, 802, 810, 811, 812, 813, 820, 821, 830, 831, 832, 840, 841, 842, 850, 860) OR report_period_name IS NOT NULL);

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
-- http://jira.aplana.com/browse/SBRFACCTAX-8895 - Изменения в структуре REF_BOOK_ATTRIBUTE/REF_BOOK_VALUE
ALTER TABLE ref_book_attribute ADD is_table NUMBER(1) DEFAULT 0 NOT NULL;
ALTER TABLE ref_book_attribute ADD CONSTRAINT ref_book_attr_chk_istable CHECK (is_table IN (0, 1));
COMMENT ON COLUMN ref_book_attribute.is_table IS 'Признак табличного атрибута';

ALTER TABLE ref_book_value ADD row_num NUMBER(9) DEFAULT 0 NOT NULL;
ALTER TABLE ref_book_value DROP CONSTRAINT REF_BOOK_VALUE_PK;
DROP INDEX REF_BOOK_VALUE_PK;
ALTER TABLE ref_book_value ADD CONSTRAINT REF_BOOK_VALUE_PK primary key (record_id, attribute_id, row_num);
COMMENT ON COLUMN ref_book_value.row_num IS 'Номер строки в табличной части справочника';

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
ALTER TABLE declaration_data ADD CONSTRAINT declaration_data_uniq_template UNIQUE(department_report_period_id, declaration_template_id);

---------------------------------------------------------------------------------------------------
COMMIT;
EXIT;