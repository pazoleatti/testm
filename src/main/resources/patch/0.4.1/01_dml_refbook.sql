-- http://jira.aplana.com/browse/SBRFACCTAX-9316: Доработки справочников в связи с изменениями в транспортном налоге

--Параметры налоговых льгот транспортного налога
update ref_book_attribute set name='Код субъекта РФ' where id=18;
update ref_book_attribute set is_unique=0 where id in (18,19);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) 
VALUES (701,7,'Код субъекта РФ представителя декларации','DECLARATION_REGION_ID',4,0,4,10,1,null,20,1,0,null,null,0,null);
update ref_book set region_attribute_id=701 where id=7;
commit;

insert into ref_book_value (record_id, attribute_id, string_value, number_value, date_value, reference_value)
select record_id, 701,null,null,null,reference_value from ref_book_value where record_id in (select id from ref_book_record where ref_book_id=7) and attribute_id=18;
commit;

--Категории средней стоимости транспортных средств
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (211,'Категории средней стоимости транспортных средств',1,0,0,null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2110, 211, 'Код', 'CODE', 1, 1, null, null, 1, null, 1, 1, 1, null, null, 0, 1);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2111, 211, 'Наименование', 'NAME', 1, 2, null, null, 1, null, 50, 1, 1, null, null, 0, 50);
commit;

--Средняя стоимость транспортных средств
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (208,'Средняя стоимость транспортных средств',1,0,0,null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2080, 208, 'Средняя стоимость', 'AVG_COST', 4, 0, 211, 2110, 1, null, 1, 1, 1, null, null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2081, 208, 'Марка', 'BREND', 1, 1, null, null, 1, null, 120, 1, 0, null, null, 0, 120);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2082, 208, 'Модель (Версия)', 'MODEL', 1, 2, null, null, 1, null, 120, 1, 0, null, null, 0, 120);
commit;

--Повышающие коэффициенты транспортного налога
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (209,'Повышающие коэффициенты транспортного налога',1,0,0,null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2090, 209, 'Средняя стоимость', 'AVG_COST', 4, 0, 211, 2110, 1, null, 1, 1, 0, null, null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2091, 209, 'Количество лет, прошедших с года выпуска ТС (от)', 'YEAR_FROM', 2, 1, null, null, 1, 0, 10, 1, 0, null, null, 0, 3);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2092, 209, 'Количество лет, прошедших с года выпуска ТС (до)', 'YEAR_TO', 2, 2, null, null, 1, 0, 10, 1, 0, null, null, 0, 3);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2093, 209, 'Повышающий коэффициент', 'COEF', 2, 3, null, null, 1, 1, 10, 0, 0, null, null, 0, 3);
commit;

--Параметры представления деклараций по транспортному налогу
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (210,'Параметры представления деклараций по транспортному налогу',1,0,0,null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2100, 210, 'Код субъекта РФ представителя декларации', 'DECLARATION_REGION_ID', 4, 1, 4, 10, 1, null, 10, 1, 1, null, null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2101, 210, 'Код субъекта РФ', 'REGION_ID', 4, 2, 4, 10, 1, null, 10, 1, 1, null, null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2102, 210, 'Код налогового органа', 'TAX_ORGAN_CODE', 1, 3, null, null, 1, null, 10, 1, 1, null, null, 0, 4);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2103, 210, 'КПП', 'KPP', 1, 4, null, null, 1, null, 10, 1, 1, null, null, 0, 9);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2104, 210, 'Код ОКТМО', 'OKTMO', 4, 5, 96, 840, 1, null, 10, 1, 1, null, null, 0, null);
update ref_book set region_attribute_id=2100 where id=210;
commit;

--Ставки транспортного налога
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (410,41,'Код субъекта РФ','DECLARATION_REGION_ID',4,0,4,10,1,null,10,1,1,null,null,0,null);
update ref_book set region_attribute_id=410 where id=41;

insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value)
select record_id, 410, null, null, null, reference_value  from ref_book_value where record_id in (select id from ref_book_record where ref_book_id=41 and status=0) and attribute_id=417 and reference_value is not null;
commit;

update ref_book_attribute set is_unique=1 where id=416;
update ref_book_attribute set max_length=8 where id=416;
update ref_book_attribute set ord=1 where id=417;
update ref_book_attribute set ord=8 where id=416;
commit;

--Коды единиц измерения на основании ОКЕИ
update ref_book_attribute set max_length=3 where id=57;
commit;
----------------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-9584 - 0.4.1 Требования к уникальности атрибутов справочников "Повышающие коэффициенты транспортного налога" и "Параметры налоговых льгот транспортного налога"
update ref_book_attribute set is_unique = 1 where id in (701, 18, 19, 20, 21, 22);
update ref_book_attribute set is_unique = 1 where id in (2090, 2091, 2092);

----------------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-9464 - Ширина колонок в 208-справочнике
update ref_book_attribute set width = 10 where id = 2080;
update ref_book_attribute set width = 30 where id = 2081;
update ref_book_attribute set width = 30 where id = 2082;
----------------------------------------------------------------------------------------------------------------

commit;
exit;