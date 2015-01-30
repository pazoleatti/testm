--http://jira.aplana.com/browse/SBRFACCTAX-9997: Новые атрибуты в справочник параметров подразделений по НДС
MERGE INTO ref_book_attribute tgt
USING
(
	SELECT 856 ID, 15 ORD FROM DUAL
	UNION ALL
	SELECT 857 ID, 16 ORD FROM DUAL
	UNION ALL
	SELECT 858 ID, 17 ORD FROM DUAL
	UNION ALL
	SELECT 859 ID, 18 ORD FROM DUAL
	UNION ALL
	SELECT 860 ID, 19 ORD FROM DUAL
	UNION ALL
	SELECT 861 ID, 20 ORD FROM DUAL
	UNION ALL
	SELECT 862 ID, 21 ORD FROM DUAL
	UNION ALL
	SELECT 864 ID, 23 ORD FROM DUAL
) src
ON (tgt.id = src.id)
WHEN MATCHED THEN
     UPDATE SET tgt.ord = src.ord;

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (981, 98, 'Код формы реорганизации и ликвидации', 'REORG_FORM_CODE', 4, 12, 5, 13, 1, null, 10, 0, 0, null, null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (982, 98, 'ИНН реорганизованного обособленного подразделения', 'REORG_INN', 1, 13, null, null, 1, null, 10, 0, 0, null, null, 0, 10);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (983, 98, 'КПП реорганизованного обособленного подразделения', 'REORG_KPP', 1, 14, null, null, 1, null, 10, 0, 0, null, null, 0, 9);

--------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-10201: Изменение размерности поля "Группа" в справочнике "Амортизационные группы"
update ref_book_attribute set max_length=2 where id = 643;

--------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-10120: Гонения на букву Ё
update form_type set name = translate(name, 'Ёё', 'Ее') where name <> translate(name, 'Ёё', 'Ее');
update form_template set name = translate(name, 'Ёё', 'Ее') where name <> translate(name, 'Ёё', 'Ее');
update form_template set fullname = translate(fullname, 'Ёё', 'Ее') where fullname <> translate(fullname, 'Ёё', 'Ее');
update declaration_type set name = translate(name, 'Ёё', 'Ее') where name <> translate(name, 'Ёё', 'Ее');
update declaration_template set name = translate(name, 'Ёё', 'Ее') where name <> translate(name, 'Ёё', 'Ее');

update form_template set name = trim(name) where name <> trim(name);
update form_template set fullname = trim(fullname) where fullname <> trim(fullname);
update form_type set name = trim(name) where name <> trim(name);
update declaration_type set name = trim(name) where name <> trim(name);
update declaration_template set name = trim(name) where name <> trim(name);

----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-10249: Сделать справочники "Ценные бумаги" и "Эмитенты" редактируемыми
update ref_book set read_only = 0 where id in (84, 100);
----------------------------------------------------------------------------------------------------------------
-- изменение ширины
UPDATE REF_BOOK_ATTRIBUTE SET IS_UNIQUE = 2 WHERE ID = 2111;
UPDATE REF_BOOK_ATTRIBUTE SET WIDTH = 5 WHERE ID = 3304;
UPDATE REF_BOOK_ATTRIBUTE SET WIDTH = 5 WHERE ID = 3305;
UPDATE REF_BOOK_ATTRIBUTE SET WIDTH = 20 WHERE ID = 3307;
UPDATE REF_BOOK_ATTRIBUTE SET WIDTH = 20 WHERE ID = 3308;
UPDATE REF_BOOK_ATTRIBUTE SET WIDTH = 20 WHERE ID = 3319;
UPDATE REF_BOOK_ATTRIBUTE SET WIDTH = 20 WHERE ID = 3320;
UPDATE REF_BOOK_ATTRIBUTE SET WIDTH = 20 WHERE ID = 3321;
UPDATE REF_BOOK_ATTRIBUTE SET WIDTH = 20 WHERE ID = 3322;
UPDATE REF_BOOK_ATTRIBUTE SET WIDTH = 20 WHERE ID = 3323;

COMMIT;
EXIT;
