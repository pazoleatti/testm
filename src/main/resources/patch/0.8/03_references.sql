--http://jira.aplana.com/browse/SBRFACCTAX-12832: Отметка о выполнении
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (501,'Отметка о выполнении',1,0,0,null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5011, 501, 'Код', 		'CODE', 2, 1, null, null, 1, 0, 	5, 1, 1, 1, 	null, 0, 1);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5012, 501, 'Отметка', 	'NAME', 1, 2, null, null, 1, null, 	20, 1, 2, null, null, 0, 50);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 501, to_date('01.01.2012', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5011, 1);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5012, 'Выполнено');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 501, to_date('01.01.2012', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5011, 2);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5012, 'Не выполнено');
	
insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 501, to_date('01.01.2012', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5011, 3);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5012, 'На исполнении');
	
insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 4, 501, to_date('01.01.2012', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5011, 4);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5012, 'Утрачена актуальность');

----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-12833: Виды страховых взносов
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (502,'Виды страховых взносов',1,0,0,null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5021, 502, 'Код вида страхового взноса', 		'CODE', 2, 1, null, null, 1, 0, 	5, 1, 1, 1, 	null, 0, 1);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5022, 502, 'Вид страхового взноса', 			'NAME', 1, 2, null, null, 1, null, 	20, 1, 0, null, null, 0, 128);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 502, to_date('01.01.2012', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5021, 1);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5022, 'Предельная величина для взносов, уплачиваемых в ФСС');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 502, to_date('01.01.2012', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5021, 2);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5022, 'Предельная величина для взносов, уплачиваемых в ПФР');
	
insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 502, to_date('01.01.2012', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5021, 3);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5022, 'Тариф отчислений страховых взносов');

----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-1284: Страховые взносы
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (503,'Страховые взносы',1,0,0,null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5031, 503, 'Вид страхового взноса', 'CODE', 	4, 1, 502, 	5022, 1, null, 5, 	1, 1, null, null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5032, 503, 'Значение', 				'VALUE', 	2, 2, null, null, 1, 2,    10, 	1, 0, null, null, 0, 4);

----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-12832: Реестр проблемных зон/ зон потенциального риска
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (504,'Реестр проблемных зон/ зон потенциального риска',1,0,0,null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5041,	504,	'Код субъекта РФ',		'REGION_ID',		4,	1, 4,		10,		1, null,	10, 1, 1, null, null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5042, 	504, 	'Территориальный банк', 'DEPARTMENT_ID', 	4, 	2, 30, 		161, 	1, null, 	10, 1, 1, null, null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5043, 	504, 	'Проблемная зона', 		'NAME', 			1, 	3, null, 	null, 	1, null, 	30, 1, 1, null, null, 0, 255);

UPDATE REF_BOOK SET REGION_ATTRIBUTE_ID = 5041 WHERE ID = 504;
----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-12860: Типы юридических лиц, сделки с которыми признаются контролируемыми

INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (505,'Типы юридических лиц, сделки с которыми признаются контролируемыми',1,0,0,null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5051, 505, 'Код типа', 		'CODE', 1, 1, null, null, 1, null, 	5, 1, 1, 1, 	null, 0, 15);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5052, 505, 'Наименование', 	'NAME', 1, 2, null, null, 1, null, 20, 1, 0, null, 	null, 0, 256);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 505, to_date('01.01.2008', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5051, 'ВЗЛ ОРН');	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5052, 'Взаимозависимые лица, применяющие общий режим налогообложения ');
	
insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 505, to_date('01.01.2008', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5051, 'ВЗЛ СРН');	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5052, 'Взаимозависимые лица, применяющие специальный режим налогообложения');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 505, to_date('01.01.2008', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5051, 'ИВЗЛ');	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5052, 'Иностранные взаимозависимые лица');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 4, 505, to_date('01.01.2008', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5051, 'РОЗ');	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5052, 'Резиденты оффшорных зон');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 5, 505, to_date('01.01.2008', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5051, 'НЛ');	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5052, 'Независимые лица');

----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-12857: Правила назначения категории юридическому лицу



----------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------

COMMIT;
EXIT;