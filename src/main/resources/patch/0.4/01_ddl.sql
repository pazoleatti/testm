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
SELECT * FROM dual;

ALTER TABLE log_system DROP CONSTRAINT log_system_chk_dcl_form;
ALTER TABLE log_system ADD CONSTRAINT log_system_chk_dcl_form CHECK (event_id IN (7, 11, 401, 402, 501, 502, 503, 601, 901, 902, 903, 801, 802, 810, 811, 812, 813, 820, 821, 830, 831, 832, 840, 841, 842, 850, 860) OR declaration_type_name IS NOT NULL OR (form_type_name IS NOT NULL AND form_kind_id IS NOT NULL));

ALTER TABLE log_system DROP CONSTRAINT log_system_chk_rp;
ALTER TABLE log_system ADD CONSTRAINT log_system_chk_rp CHECK (event_id IN (7, 11, 401, 402, 501, 502, 503, 601, 901, 902, 903, 801, 802, 810, 811, 812, 813, 820, 821, 830, 831, 832, 840, 841, 842, 850, 860) OR report_period_name IS NOT NULL);

---------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8512 - Переход на новый механизм блокировок
DROP TABLE object_lock;

---------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8861 - Связь НФ с таблицей BLOB_DATA
CREATE TABLE report (form_data_id number(18) NOT NULL, blob_data_id varchar2(36) NOT NULL, type number(1) NOT NULL, checking number(1) NOT NULL, manual number(1) NOT NULL, absolute number(1) NOT NULL);

COMMENT ON TABLE report IS 'Отчет';
COMMENT ON COLUMN report.form_data_id IS 'Идентификатор налоговой формы';
COMMENT ON COLUMN report.blob_data_id IS 'Идентификатор отчета';
COMMENT ON COLUMN report.TYPE IS 'Тип отчета (0 - Excel, 1 - CSV, 2 - PDF, 3 - Jasper)';
COMMENT ON COLUMN report.manual IS 'Режим ввода данных (0 - обычная версия; 1 - версия ручного ввода)';
COMMENT ON COLUMN report.checking IS 'Типы столбцов (0 - только обычные, 1 - вместе с контрольными)';
COMMENT ON COLUMN report.ABSOLUTE IS 'Режим вывода данных (0 - только дельты, 1 - абсолютные значения)';

ALTER TABLE report ADD CONSTRAINT report_pk PRIMARY KEY (form_data_id,type, manual,checking,absolute);

ALTER TABLE report ADD CONSTRAINT report_fk_form_data_id FOREIGN KEY (form_data_id) REFERENCES form_data(id);
ALTER TABLE report ADD CONSTRAINT report_fk_blob_data_id FOREIGN KEY (blob_data_id) REFERENCES blob_data(id);
ALTER TABLE report ADD CONSTRAINT report_chk_type CHECK (type IN (0,1,2,3));
ALTER TABLE report ADD CONSTRAINT report_chk_manual CHECK (manual IN (0,1));
ALTER TABLE report ADD CONSTRAINT report_chk_checking CHECK (checking IN (0,1));
ALTER TABLE report ADD CONSTRAINT report_chk_absolute CHECK (absolute IN (0,1));

---------------------------------------------------------------------------------------------------
COMMIT;
EXIT;