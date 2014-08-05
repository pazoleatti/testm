---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-7297: Сквозная нумерация строк
--alter table form_column add numeration_row number(9);
--alter table form_data add number_previous_row number (9);

alter table form_column drop constraint form_column_chk_type;
alter table form_column drop constraint form_column_chk_max_length; 

alter table form_column drop constraint form_column_chk_numrow; 
alter table form_column add constraint form_column_chk_numrow check (numeration_row in (0, 1) or type <> 'A');
alter table form_column add constraint form_column_chk_type check(type in ('N', 'S', 'D', 'R', 'A'));
alter table form_column add constraint form_column_chk_max_length check ((type = 'S' and max_length is not null and max_length > 0 and max_length <= 2000) or (type = 'N' and max_length is not null and max_length > 0 and max_length <= 27) or ((type ='D' or type ='R' or type='A') and max_length is null));

comment on column form_column.type is 'Тип столбца (S - строка, N – число, D – дата, R - ссылка, A - автонумеруемая графа)';
comment on column form_column.numeration_row is 'Тип нумерации строк для автонумеруемой графы';
comment on column form_data.number_previous_row is 'Номер последней строки предыдущей НФ';

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-7329: Удалить поле "ord" из таблицы "report_period"
alter table report_period drop column ord; 

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-7856: Ограничение на налоговые периоды
ALTER TABLE tax_period ADD CONSTRAINT tax_period_uniq_taxtype_year UNIQUE (tax_type, year);

--SBRFACCTAX-7406 - Версионирование источников-приемников
--alter table form_data_source add period_start date;
--alter table form_data_source add period_end date;

--alter table declaration_source add period_start date;
--alter table declaration_source add period_end date;

comment on column form_data_source.period_start is 'Дата начала действия назначения';
comment on column form_data_source.period_end is 'Дата окончания действия назначения';

comment on column declaration_source.period_start is 'Дата начала действия назначения';
comment on column declaration_source.period_end is 'Дата окончания действия назначения';

update form_data_source set period_start = to_date('01.01.2008', 'DD.MM.YYYY'), period_end = null;
update declaration_source set period_start = to_date('01.01.2008', 'DD.MM.YYYY'), period_end = null;

alter table form_data_source modify period_start not null;
alter table declaration_source modify  period_start not null;

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8037 - Удалить из БД поля FORM_TEMPLATE.EDITION и DECLARATION_TEMPLATE.EDITION
ALTER TABLE form_template DROP COLUMN edition;
ALTER TABLE declaration_template DROP COLUMN edition;

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8021 - Изменения в макетах НФ и деклараций в связи с отказом от статуса "Удален"
ALTER TABLE template_changes DROP CONSTRAINT changes_fk_form_template_id;
ALTER TABLE template_changes DROP CONSTRAINT changes_fk_dec_template_id;

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-7999 - Добавить столбец "header" в таблицу form_template / столбец Code в form_type
ALTER TABLE form_template ADD header VARCHAR2(1000 byte);
COMMENT ON COLUMN form_template.header IS 'Верхний колонтитул печатной формы';

ALTER TABLE form_type ADD code VARCHAR2(600 byte);
ALTER TABLE form_type ADD CONSTRAINT form_type_uniq_code UNIQUE(code);
COMMENT ON COLUMN form_type.code IS 'Номер формы';

UPDATE form_template SET header=trim(code);
ALTER TABLE form_template DROP COLUMN code; 

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-7650 - признак редактируемости в атрибутах справочника
ALTER TABLE ref_book_attribute ADD READ_ONLY NUMBER(1) DEFAULT 0 NOT NULL;
COMMENT ON COLUMN ref_book_attribute.read_only IS 'Только для чтения (0 - редактирование доступно пользователю; 1 - редактирование недоступно пользователю)';
ALTER TABLE ref_book_attribute ADD CONSTRAINT ref_book_attr_chk_read_only CHECK (read_only IN (0, 1));

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-7686 - максимальная длина строки/целой части числа
ALTER TABLE ref_book_attribute ADD max_length number(4);
COMMENT ON COLUMN ref_book_attribute.max_length IS 'Максимальная длина строки/Максимальное количество цифр без учета знака и десятичного разделителя';
ALTER TABLE ref_book_attribute ADD CONSTRAINT ref_book_attr_chk_max_length check ((type=1 and max_length between 1 and 2000) or (type=2 and max_length between 1 and 27) or (type in (3,4) and max_length IS null));

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8120 - новый механизм блокировок
CREATE TABLE lock_data
(
key VARCHAR2(1000) NOT NULL,
user_id NUMBER(9) NOT NULL,
date_before DATE NOT NULL
);

COMMENT ON TABLE lock_data IS 'Информация о блокировках';
COMMENT ON COLUMN lock_data.key IS 'Код блокировки';
COMMENT ON COLUMN lock_data.user_id IS 'Идентификатор пользователя, установившего блокировку';
COMMENT ON COLUMN lock_data.date_before IS 'Срок истечения блокировки';

ALTER TABLE lock_data ADD CONSTRAINT lock_data_pk primary key (key);
ALTER TABLE lock_data ADD CONSTRAINT lock_data_fk_user_id foreign key (user_id) references sec_user(id) on delete cascade;

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-7870 - изменения таблицы CONFIGURATION
ALTER TABLE configuration ADD department_id number(9) DEFAULT 0 NOT NULL;

ALTER TABLE configuration ADD CONSTRAINT configuration_fk FOREIGN KEY (department_id) REFERENCES department (id) ON DELETE CASCADE;
ALTER TABLE configuration DROP CONSTRAINT configuration_pk;
DROP INDEX configuration_pk;
ALTER TABLE configuration ADD CONSTRAINT configuration_pk PRIMARY KEY (code, department_id);
COMMENT ON COLUMN configuration.department_id IS 'ТБ';

-- -- изменения с http://jira.aplana.com/browse/SBRFACCTAX-7698
ALTER TABLE configuration ADD(value_temp clob);
UPDATE configuration SET value_temp = value;
ALTER TABLE configuration drop column value;
ALTER TABLE configuration rename column value_temp to value; 
COMMENT ON COLUMN configuration.value IS 'Значение параметра'; 

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
-- http://jira.aplana.com/browse/SBRFACCTAX-8101 - В таблицы INCOME_101, INCOME_102 необходимо добавить поле "Ид. периода и подразделения БО"
ALTER TABLE income_101 ADD account_period_id NUMBER(9);
ALTER TABLE income_101 ADD CONSTRAINT income_101_fk_accperiod_id FOREIGN KEY (account_period_id) REFERENCES ref_book_record(id);
COMMENT ON COLUMN income_101.account_period_id IS 'Идентификатор периода и подразделения БО';

ALTER TABLE income_102 ADD account_period_id NUMBER(9);
ALTER TABLE income_102 ADD CONSTRAINT income_102_fk_accperiod_id FOREIGN KEY (account_period_id) REFERENCES ref_book_record(id);
COMMENT ON COLUMN income_102.account_period_id IS 'Идентификатор периода и подразделения БО';

---------------------------------------------------------------------------------------------------------
--Группа задач на LOG_SYSTEM 

-- http://jira.aplana.com/browse/SBRFACCTAX-8207 - Смена подразделения у системного пользователя
UPDATE sec_user SET department_id = 0 WHERE id = 0;

-- http://jira.aplana.com/browse/SBRFACCTAX-7975 - Доработка записи событий справочника "Подразделения" в журнал аудита
ALTER TABLE log_system ADD user_login varchar2(255);
UPDATE LOG_SYSTEM ls SET ls.user_id = 0 WHERE ls.user_id IS null;
UPDATE LOG_SYSTEM ls SET ls.user_login = (SELECT login FROM SEC_USER su WHERE ls.USER_ID = su.ID);

-- http://jira.aplana.com/browse/SBRFACCTAX-8207 - Заполнить DEPARTMENT_NAME по SEC_USER.DEPARTMENT
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

--таблица LOG_BUSINESS
ALTER TABLE log_business ADD user_login varchar2(255);
UPDATE log_business lb SET lb.user_login = (SELECT login FROM SEC_USER su WHERE lb.USER_ID = su.ID);
ALTER TABLE log_business drop column user_id;
--ALTER TABLE log_business drop CONSTRAINT LOG_BUSINESS_FK_USER_ID;
ALTER TABLE log_business ADD CONSTRAINT log_business_fk_user_login foreign key (user_login) references sec_user(login);

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-6979 - Обязательность заполнения полей LOG_SYSTEM c 0.3.8
ALTER TABLE log_system modify user_login not null;
ALTER TABLE log_business modify user_login not null;

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-7813 - Добавить поддержку событий IMPORT_TRANSPORT_FILE и UPLOAD_TRANSPORT_FILE в ЖА
ALTER TABLE log_system drop CONSTRAINT log_system_chk_event_id;
ALTER TABLE log_system drop CONSTRAINT log_system_chk_dcl_form;
ALTER TABLE log_system drop CONSTRAINT log_system_chk_rp;

ALTER TABLE log_system ADD CONSTRAINT log_system_chk_event_id check (event_id in (1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 203, 204, 205, 206, 207, 208, 209, 210, 301, 302, 303, 401, 402, 501, 502, 503, 601, 901, 902, 903));
ALTER TABLE log_system ADD CONSTRAINT log_system_chk_dcl_form check (event_id in (7, 11, 401, 402, 501, 502, 503, 601, 901, 902, 903) or declaration_type_id IS not null or (form_type_id IS not null and form_kind_id IS not null));
ALTER TABLE log_system ADD CONSTRAINT log_system_chk_rp check (event_id in (7, 11, 401, 402, 501, 502, 503, 601, 901, 902, 903) or report_period_name IS not null);

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-7840 - заменить на текст все те атрибуты, которые раньше хранились как ссылки в таблицах для Журнала аудита
ALTER TABLE log_system ADD declaration_type_name varchar2(80);
ALTER TABLE log_system ADD form_type_name varchar2(1000);
ALTER TABLE log_system ADD form_department_id number(9);

COMMENT ON COLUMN LOG_SYSTEM.DECLARATION_TYPE_NAME IS 'Вид декларации';
COMMENT ON COLUMN LOG_SYSTEM.FORM_TYPE_NAME IS 'Вид налоговой формы';
COMMENT ON COLUMN LOG_SYSTEM.FORM_DEPARTMENT_ID IS 'Идентификатор подразделения налоговой формы/декларации';

UPDATE log_system ls SET declaration_type_name = (SELECT name FROM declaration_type dt WHERE dt.id = ls.declaration_type_id), form_type_name = (SELECT name FROM form_type ft WHERE ft.id = ls.form_type_id);

ALTER TABLE LOG_SYSTEM drop CONSTRAINT LOG_SYSTEM_CHK_DCL_FORM;
ALTER TABLE LOG_SYSTEM ADD CONSTRAINT LOG_SYSTEM_CHK_DCL_FORM
  check (event_id in (7, 11, 401, 402, 501, 502, 503, 601, 901, 902, 903) or declaration_type_name IS not null or (form_type_name IS not null and form_kind_id IS not null));

ALTER TABLE log_system drop column declaration_type_id; 
ALTER TABLE log_system drop column form_type_id;

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-7906 - добавить таблицу EVENT и ссылаться на нее из ЖА
CREATE TABLE event
(
id NUMBER(9) NOT NULL,
name VARCHAR(510) NOT NULL
);

COMMENT ON TABLE event IS 'Справочник событий в системе';
COMMENT ON COLUMN event.id IS 'Идентификатор события';
COMMENT ON COLUMN event.name IS 'Наименование события';

INSERT ALL
  INTO event VALUES (1, 'Создать')
  INTO event VALUES (12, 'После создания')
  INTO event VALUES (2, 'Удалить')
  INTO event VALUES (3, 'Рассчитать')
  INTO event VALUES (4, 'Обобщить')
  INTO event VALUES (5, 'Проверить')
  INTO event VALUES (6, 'Сохранить')
  INTO event VALUES (7, 'Импорт данных')
  INTO event VALUES (8, 'Получение данных')
  INTO event VALUES (9, 'Получение защищенных данных')
  INTO event VALUES (10, 'Тестирование скриптов')
  INTO event VALUES (11, 'Миграция из АС "Ведение РНУ"')
  INTO event VALUES (101, 'Утвердить из "Создана"')
  INTO event VALUES (102, 'Вернуть из "Утверждена" в "Создана"')
  INTO event VALUES (103, 'Принять из "Утверждена"')
  INTO event VALUES (104, 'Вернуть из "Принята" в "Утверждена"')
  INTO event VALUES (105, 'Принять из "Создана"')
  INTO event VALUES (106, 'Вернуть из "Принята" в "Создана"')
  INTO event VALUES (107, 'Подготовить из "Создана"')
  INTO event VALUES (108, 'Вернуть из "Подготовлена" в "Создана"')
  INTO event VALUES (109, 'Принять из "Подготовлена"')
  INTO event VALUES (110, 'Вернуть из "Принята" в "Подготовлена"')
  INTO event VALUES (111, 'Утвердить из "Подготовлена"')
  INTO event VALUES (112, 'Вернуть из "Утверждена" в "Подготовлена"')
  INTO event VALUES (203, 'После принять из "Утверждена"')
  INTO event VALUES (204, 'После вернуть из "Принята" в "Утверждена"')
  INTO event VALUES (205, 'После принять из "Создана"')
  INTO event VALUES (206, 'После вернуть из "Принята" в "Создана"')
  INTO event VALUES (207, 'После принять из "Подготовлена"')
  INTO event VALUES (208, 'После вернуть из "Принята" в "Подготовлена"')
  INTO event VALUES (209, 'После утвердить из "Подготовлена"')
  INTO event VALUES (210, 'После вернуть "Подготовлена" из "Утверждена"')
  INTO event VALUES (301, 'Добавить строку')
  INTO event VALUES (303, 'Удалить строку')
  INTO event VALUES (302, 'Загрузка')
  INTO event VALUES (401, 'Импорт из транспортных файлов')
  INTO event VALUES (402, 'Загрузка транспортных файлов в каталог загрузки')
  INTO event VALUES (501, 'Вход пользователя в Систему')
  INTO event VALUES (502, 'Выход пользователя из Системы')
  INTO event VALUES (503, 'Взаимодействие с внешней АС')
  INTO event VALUES (601, 'Архивирование журнала событий')
  INTO event VALUES (901, 'Создание подразделения')
  INTO event VALUES (902, 'Модификация подразделения')
  INTO event VALUES (903, 'Удаление подразделения')
  INTO event VALUES (701, 'Версия создана')
  INTO event VALUES (702, 'Версия изменена')
  INTO event VALUES (703, 'Версия введена в действие')
  INTO event VALUES (704, 'Версия выведена из действия')
  INTO event VALUES (705, 'Версия удалена')
SELECT * FROM dual;  

ALTER TABLE event ADD CONSTRAINT event_pk PRIMARY KEY (id);
ALTER TABLE log_system ADD CONSTRAINT log_system_fk_event_id FOREIGN KEY (event_id) REFERENCES event(id);
ALTER TABLE log_system DROP CONSTRAINT log_system_chk_event_id;

-- http://jira.aplana.com/browse/SBRFACCTAX-8319 Изменения для template_changes, связаннные с введением таблицы EVENT
ALTER TABLE template_changes DROP CONSTRAINT changes_check_event;
ALTER TABLE template_changes MODIFY event NUMBER(9);

UPDATE template_changes SET event=700+event WHERE event IN (1,2,3,4,5);

ALTER TABLE template_changes ADD CONSTRAINT template_changes_chk_event CHECK (event in (701, 702, 703, 704, 705));
ALTER TABLE template_changes ADD CONSTRAINT template_changes_fk_event FOREIGN KEY (event) references event(id);
---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8032 - Добавить поле "Идентификатор ТБ подразделения налоговой формы / декларации" в таблицу LOG_SYSTEM
ALTER TABLE log_system ADD tb_department_id NUMBER(9);
COMMENT ON COLUMN log_system.tb_department_id IS 'Идентификатор ТБ подразделения налоговой формы/декларации';

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-7971 - Доработка gui планировщика
ALTER TABLE task_context ADD modification_date date not null;
COMMENT ON COLUMN task_context.modification_date IS 'Дата последнего редактирования задачи';

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8269 - Доп. доработка планировщика
ALTER TABLE task_context ADD user_id NUMBER(9) NOT NULL;
ALTER TABLE task_context ADD CONSTRAINT task_context_fk_user_id foreign key (user_id) references sec_user(id);
COMMENT ON COLUMN task_context.user_id IS 'Идентификатор пользователя';

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-7330 - Удалить поле "data_size" из таблицы "blob_data"
ALTER TABLE blob_data DROP COLUMN data_size;

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-6298 - Ограничения ручного ввода с 0.3.5
ALTER TABLE data_row ADD CONSTRAINT data_row_chk_manual CHECK (manual IN (0, 1));

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-7553 - Удаление дефолтного значения для "Код подразделения"
ALTER TABLE department MODIFY CODE DEFAULT NULL;

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8183 - Дополнительные комментарии к метаданным
COMMENT ON COLUMN declaration_data.data IS 'Данные декларации в формате законодателя (XML)';
COMMENT ON COLUMN declaration_data.data_pdf IS 'Данные декларации в формате PDF';
COMMENT ON COLUMN declaration_data.data_xlsx IS 'Данные декларации в формате XLSX';
COMMENT ON COLUMN declaration_template.jrxml IS 'Макет JasperReports для формирования печатного представления формы';
COMMENT ON COLUMN declaration_template.version IS 'Версия';
COMMENT ON COLUMN form_template.version IS 'Версия формы (уникально в рамках типа)';
COMMENT ON COLUMN log_business.user_login IS 'Логин пользователя';
COMMENT ON COLUMN log_system.user_login IS 'Логин пользователя';
COMMENT ON COLUMN notification.id IS 'Уникальный идентификатор оповещения';
COMMENT ON COLUMN ref_book.region_attribute_id IS 'При его наличии справочник считается региональным. Указывает на атрибут, по которому определяется принадлежность к региону';
COMMENT ON COLUMN task_context.id IS 'Уникальный идентификатор записи';
COMMENT ON COLUMN template_changes.id IS 'Уникальный идентификатор записи';
COMMENT ON COLUMN data_row.type IS 'Тип строки (0 - подтвержденные данные, 1 - строка добавлена, -1 - строка удалена)';
COMMENT ON COLUMN department.region_id IS 'Код региона';
COMMENT ON COLUMN form_column.numeration_row IS 'Тип нумерации строк для автонумеруемой графы (0 - последовательная, 1 - сквозная)';
COMMENT ON COLUMN log_system.department_name IS 'Наименование подразделения НФ\декларации';

---------------------------------------------------------------------------------------------------------
commit;
