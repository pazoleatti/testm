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
COMMIT;
EXIT;