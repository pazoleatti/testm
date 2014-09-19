﻿---------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8357: Завести справочники по налогу на имущество
-- -- http://jira.aplana.com/browse/SBRFACCTAX-8359: Переименовать существующие справочники «Коды налоговых льгот» и «Параметры налоговых льгот»
UPDATE ref_book SET name = 'Коды налоговых льгот транспортного налога' WHERE ID = 6;
UPDATE ref_book SET name = 'Параметры налоговых льгот транспортного налога' WHERE ID = 7;

ALTER TABLE ref_book DISABLE CONSTRAINT ref_book_fk_region;

-- -- http://jira.aplana.com/browse/SBRFACCTAX-8318: Cправочник "Параметры представления деклараций по налогу на имущество"
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (200, 'Параметры представления деклараций по налогу на имущество', 1, 0, 0, 2001);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2001, 200, 'Код субъекта РФ представителя декларации', 'DECLARATION_REGION_ID', 4, 1, 4, 10, 1, null, 10, 1, 0, null, null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2002, 200, 'Код субъекта РФ', 'REGION_ID', 4, 2, 4, 10, 1, null, 10, 1, 0, null, null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2003, 200, 'Код налогового органа', 'TAX_ORGAN_CODE', 1, 3, null, null, 1, null, 10, 0, 0, null, null, 0, 4);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2004, 200, 'КПП', 'KPP', 1, 4, null, null, 1, null, 10, 0, 0, null, null, 0, 9);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2005, 200, 'Код ОКТМО', 'OKTMO', 4, 5, 96, 840, 1, null, 10, 0, 0, null, null, 0, null);

-- -- http://jira.aplana.com/browse/SBRFACCTAX-8358: Справочник "Ставки налога на имущество"
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (201, 'Ставки налога на имущество', 1, 0, 0, 2011);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2011, 201, 'Код субъекта РФ представителя декларации', 'DECLARATION_REGION_ID', 4, 1, 4, 10, 1, null, 10, 1, 0, null, null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2012, 201, 'Код субъекта РФ', 'REGION_ID', 4, 2, 4, 10, 1, null, 10, 1, 0, null, null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2013, 201, 'Ставка (%)', 'RATE', 2, 3, null, null, 1, 2, 10, 0, 0, null, null, 0, 3);

-- -- http://jira.aplana.com/browse/SBRFACCTAX-8360: Cправочник "Коды налоговых льгот налога на имущество"
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (202, 'Коды налоговых льгот налога на имущество', 1, 0, 0, null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2021,202,'Код налоговой льготы','CODE',1,1,null,null,1,null,7,1,1,null,null,0,7);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2022,202,'Наименование льготы','NAME',1,2,null,null,1,null,50,1,0,null,null,0,2000);

INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) VALUES (seq_ref_book_record.NEXTVAL, 1, 202, to_date('01.01.2012', 'DD.MM.YYYY'), 0);
	INSERT INTO ref_book_value (record_id, attribute_id, string_value) VALUES (seq_ref_book_record.CURRVAL, 2021, '2012000');
	INSERT INTO ref_book_value (record_id, attribute_id, string_value) VALUES (seq_ref_book_record.CURRVAL, 2022, 'Дополнительные льготы по налогу на имущество организаций, устанавливаемые законами субъектов РФ, за исключением льгот в виде снижения ставки для отдельной категории налогоплательщиков и в виде уменьшения суммы налога, подлежащей уплате в бюджет');

INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) VALUES (seq_ref_book_record.NEXTVAL, 2, 202, to_date('01.01.2012', 'DD.MM.YYYY'), 0);
	INSERT INTO ref_book_value (record_id, attribute_id, string_value) VALUES (seq_ref_book_record.CURRVAL, 2021, '2012400');
	INSERT INTO ref_book_value (record_id, attribute_id, string_value) VALUES (seq_ref_book_record.CURRVAL, 2022, 'Дополнительные льготы по налогу на имущество организаций, устанавливаемые законами субъектов РФ в виде понижения налоговой ставки для отдельной категории налогоплательщиков');

INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) VALUES (seq_ref_book_record.NEXTVAL, 3, 202, to_date('01.01.2012', 'DD.MM.YYYY'), 0);
	INSERT INTO ref_book_value (record_id, attribute_id, string_value) VALUES (seq_ref_book_record.CURRVAL, 2021, '2012500');
	INSERT INTO ref_book_value (record_id, attribute_id, string_value) VALUES (seq_ref_book_record.CURRVAL, 2022, 'Дополнительные льготы по налогу на имущество организаций, устанавливаемые законами субъектов РФ в виде уменьшения суммы налога, подлежащей уплате в бюджет');	

-- -- http://jira.aplana.com/browse/SBRFACCTAX-8361: Cправочник "Параметры налоговых льгот налога на имущество"	
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (203, 'Параметры налоговых льгот налога на имущество',  1, 0, 0, 2031);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2031, 203, 'Код субъекта РФ представителя декларации', 'DECLARATION_REGION_ID', 4, 1, 4, 10, 1, null, 10, 1, 0, null, null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2032, 203, 'Код субъекта РФ', 'REGION_ID', 4, 2, 4, 10, 1, null, 10, 1, 0, null, null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2030, 203, 'Назначение параметра (0 – по средней, 1 – категория, 2 – по кадастровой)', 'PARAM_DESTINATION', 2, 3, null, null, 1, 0, 10, 1, 0, null, null, 0, 1);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2033, 203, 'Код налоговой льготы', 'TAX_BENEFIT_ID', 4, 4, 202, 2022, 1, null, 20, 1, 0, null, null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2034, 203, 'Категория имущества', 'ASSETS_CATEGORY', 1, 5, null, null, 1, null, 20, 0, 0, null, null, 0, 200); 
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2035, 203, 'Основание - статья','SECTION',1,6,null,null,1,null,10,1,0,null,null,0,4);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2036, 203, 'Основание - пункт','ITEM',1,7,null,null,1,null,10,1,0,null,null,0,4);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2037, 203, 'Основание - подпункт','SUBITEM',1,8,null,null,1,null,10,1,0,null,null,0,4);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2038, 203, 'Льготная ставка, %', 'RATE', 2, 9, null, null, 1, 2, 10, 0, 0, null, null, 0, 3);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2039, 203, 'Уменьшение суммы исчисленного налога, руб.', 'REDUCTION_SUM', 2, 10, null, null, 1, 2, 10, 0, 0, null, null, 0, 17);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2040, 203, 'Уменьшение суммы исчисленного налога, %', 'REDUCTION_PCT', 2, 11, null, null, 1, 2, 10, 0, 0, null, null, 0, 3);

ALTER TABLE ref_book ENABLE CONSTRAINT ref_book_fk_region;

---------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8465: Добавить поле "название таблицы" в таблицу REF_BOOK
UPDATE ref_book SET table_name = 'REF_BOOK_OKTMO' WHERE id = 96;
UPDATE ref_book SET table_name = 'FORM_TYPE' WHERE id = 93;
UPDATE ref_book SET table_name = 'FORM_KIND' WHERE id = 94;
UPDATE ref_book SET table_name = 'SEC_USER' WHERE id = 74;
UPDATE ref_book SET table_name = 'SEC_ROLE' WHERE id = 95;
UPDATE ref_book SET table_name = 'DEPARTMENT' WHERE id = 30;
UPDATE ref_book SET table_name = 'DEPARTMENT_TYPE' WHERE id = 103;
UPDATE ref_book SET table_name = 'INCOME_101' WHERE id = 50;
UPDATE ref_book SET table_name = 'INCOME_102' WHERE id = 52;

---------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8403 - Изменения таблицы DECLARATION_DATA для налога на имущество

INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (204, 'Коды налоговых органов', 0, 0, 1, null);
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (205, 'КПП налоговых органов', 0, 0, 1, null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2041, 204, 'Код налогового органа', 'TAX_ORGAN_CODE', 1, 1, NULL, NULL, 1, NULL, 10, 0, 0, NULL, NULL, 0, 4);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2051, 205, 'КПП', 'KPP', 1, 1, NULL, NULL, 1, NULL, 10, 0, 0, NULL, NULL, 0, 9);

---------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8414 - Внести изменения на форму настроек подразделений

INSERT INTO ref_book_attribute (id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, max_length) VALUES
( 2052, 33, 26, 'Наименование для Приложения № 5', 'ADDITIONAL_NAME', 1, NULL, NULL, 1, NULL, 100, 1000);

DELETE FROM ref_book_value WHERE attribute_id = 178;
DELETE FROM ref_book_attribute WHERE alias = 'APP_VERSION' AND ref_book_id = 31;
DELETE FROM ref_book_value WHERE attribute_id = 201;
DELETE FROM ref_book_attribute WHERE alias = 'APP_VERSION' AND ref_book_id = 33;
DELETE FROM ref_book_value WHERE attribute_id = 245;
DELETE FROM ref_book_attribute WHERE alias = 'APP_VERSION' AND ref_book_id = 37;
DELETE FROM ref_book_value WHERE attribute_id = 863;
DELETE FROM ref_book_attribute WHERE alias = 'APP_VERSION' AND ref_book_id = 98;

UPDATE ref_book_attribute SET name = 'Наименование для титульного листа' WHERE id IN (217, 228, 191, 865);

---------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-8804 - "Код ТН ВЭД" в справочник "Коды драгоценных металлов"
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (1743,17,'Код ТН ВЭД','TN_VED_CODE',4,3,73,648,1,null,10,1,1,null,null,0,null);

MERGE INTO ref_book_value tgt USING (WITH mapping(name, tn_ved_code) AS
                                      (SELECT 'Палладий', 7110210001 FROM dual
                                       UNION ALL SELECT 'Платина', 7110110001 FROM dual
                                       UNION ALL SELECT 'Золото, его сырье и сплавы', 7108120001 FROM dual
                                       UNION ALL SELECT 'Серебро, его сырье, сплавы и соединения', 7106910001 FROM dual)
                                     SELECT m.name,
                                            m.tn_ved_code,
                                            rbv_tgt.string_value,
                                            rbv_tgt.record_id AS src_record_id,
                                            rbr_src.id AS src_reference_id,
                                            1743 AS src_attribute_id
                                     FROM mapping m
                    JOIN ref_book_value rbv_tgt ON rbv_tgt.attribute_id = 42 AND rbv_tgt.string_value = m.name
                    JOIN ref_book_value rbv_src ON rbv_src.attribute_id = 648 AND rbv_src.number_value = m.tn_ved_code
                    JOIN ref_book_record rbr_src ON rbr_src.id = rbv_src.record_id AND rbr_src.status <> -1) src 
ON (tgt.record_id = src.src_record_id AND tgt.attribute_id = src.src_attribute_id) 
WHEN MATCHED THEN UPDATE SET tgt.reference_value = src.src_reference_id 
WHEN NOT MATCHED THEN INSERT (tgt.record_id, tgt.attribute_id, tgt.reference_value) VALUES (src.src_record_id, src.src_attribute_id, src.src_reference_id);

---------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-8783 - Уникальность атрибутов

-- Если в справочнике уже существовало несколько независимых уникальных атрибутов
UPDATE ref_book_attribute SET is_unique=2 WHERE id = 51;
UPDATE ref_book_attribute SET is_unique=3 WHERE id = 52;
UPDATE ref_book_attribute SET is_unique=4 WHERE id = 53;
UPDATE ref_book_attribute SET is_unique=5 WHERE id = 54;
UPDATE ref_book_attribute SET is_unique=2 WHERE id = 69;
UPDATE ref_book_attribute SET is_unique=2 WHERE id = 166;
UPDATE ref_book_attribute SET is_unique=2 WHERE id = 250;
UPDATE ref_book_attribute SET is_unique=2 WHERE id = 630;
UPDATE ref_book_attribute SET is_unique=2 WHERE id = 632;
UPDATE ref_book_attribute SET is_unique=2 WHERE id = 634;
UPDATE ref_book_attribute SET is_unique=2 WHERE id = 636;
UPDATE ref_book_attribute SET is_unique=2 WHERE id = 642;
UPDATE ref_book_attribute SET is_unique=2 WHERE id = 647;
UPDATE ref_book_attribute SET is_unique=2 WHERE id = 834;
UPDATE ref_book_attribute SET is_unique=2 WHERE id = 1743;

-- Уникальность кортежей
-- Классификатор доходов
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 140;
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 143;
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 144;

-- Классификатор расходов
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 130;
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 133;
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 134;

-- Классификатор соответствия кодов операций налоговой формы 724.2.1 по НДС символам ОПУ
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 1000;
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 1001;
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 1002;

-- Классификатор соответствия счетов бухгалтерского учёта кодам налогового учёта
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 150;
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 151;
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 152;
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 153;
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 154;

-- Оборотная ведомость (Форма 0409101-СБ)
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 502;
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 511;

-- Отчет о прибылях и убытках (Форма 0409102-СБ)
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 521;
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 527;

-- Параметры налоговых льгот транспортного налога
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 18;
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 19;

-- Параметры представления деклараций по налогу на имущество
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 2001;
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 2002;
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 2003;
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 2004;
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 2005;

-- Ставки налога на имущество
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 2011;
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 2012;

-- Ставки транспортного налога
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 411;
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 412;
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 413;
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 414;
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 415;
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 418;
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 417;
---------------------------------------------------------------------------------------------------

COMMIT;
EXIT;