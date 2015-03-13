--http://jira.aplana.com/browse/SBRFACCTAX-10529: Создать справочники "Коды доходов", "Коды документов", "Коды вычетов"
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (350,'Коды вычетов',1,0,0,null);
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (360,'Коды документов',1,0,0,null);
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (370,'Коды доходов',1,0,0,null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3501, 350,'Код','CODE',1,1,null,null,1,null,6,1,1,null,null,0,3);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3502, 350,'Наименование вычета','NAME',1,2,null,null,1,null,50,1,0,null,null,0,2000);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3601, 360,'Код','CODE',1,1,null,null,1,null,6,1,1,null,null,0,2);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3602, 360,'Наименование документа','NAME',1,2,null,null,1,null,50,1,0,null,null,0,2000);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3701, 370,'Код','CODE',1,1,null,null,1,null,6,1,1,null,null,0,4);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3702, 370,'Наименование дохода','NAME',1,2,null,null,1,null,50,1,0,null,null,0,2000);

--http://jira.aplana.com/browse/SBRFACCTAX-10479: Изменение отображаемого атрибута по ссылкам "Код субъекта РФ представителя декларации", "Код субъекта РФ" для справочников ТН
update ref_book_attribute set attribute_id = 9 where ref_book_id in (7, 41, 210) and reference_id = 4;


COMMIT;
EXIT;