INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id,is_versioned) VALUES (204, 'Коды налоговых органов', 0, 0, 1, null,0);
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id,is_versioned) VALUES (205, 'КПП налоговых органов', 0, 0, 1, null,0);
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id, table_name) VALUES (1020,'ФИАС Типы адресных объектов',0,0,1,null, 'fias_socrbase');

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2041, 204, 'Код налогового органа', 'TAX_ORGAN_CODE', 1, 1, NULL, NULL, 1, NULL, 10, 0, 0, NULL, NULL, 0, 4);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2051, 205, 'КПП', 'KPP', 1, 1, NULL, NULL, 1, NULL, 10, 0, 0, NULL, NULL, 0, 9);

update ref_book_attribute set max_length=25 where id=9523;

commit;
exit;