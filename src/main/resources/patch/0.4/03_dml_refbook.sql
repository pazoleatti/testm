---------------------------------------------------------------------------------------------------

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
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2033, 203, 'Код налоговой льготы', 'TAX_BENEFIT_ID', 4, 3, 202, 2022, 1, null, 20, 1, 0, null, null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2034, 203, 'Категория имущества', 'ASSETS_CATEGORY', 1, 4, null, null, 1, null, 20, 0, 0, null, null, 0, 200); 
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2035, 203, 'Основание - статья','SECTION',1,5,null,null,1,null,10,1,0,null,null,0,4);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2036, 203, 'Основание - пункт','ITEM',1,6,null,null,1,null,10,1,0,null,null,0,4);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2037, 203, 'Основание - подпункт','SUBITEM',1,7,null,null,1,null,10,1,0,null,null,0,4);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2038, 203, 'Льготная ставка, %', 'RATE', 2, 8, null, null, 1, 2, 10, 0, 0, null, null, 0, 3);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2039, 203, 'Сумма уменьшения платежа, руб.', 'REDUCTION_SUM', 2, 9, null, null, 1, 2, 10, 0, 0, null, null, 0, 17);

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


