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
--http://jira.aplana.com/browse/SBRFACCTAX-12997: Цвета
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id, table_name, is_versioned) VALUES (1,'Цвета',1,0,1,null, 'COLOR', 0);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (95, 1, 'Наименование цвета', 	'NAME', 1, 1, null, null, 1, null, 20, 1, 1, null, 	null, 0, 50);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (96, 1, 'R', 'R', 	2, 2, null, null, 1, 0, 	5, 1, 2, null, 	null, 0, 3);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (97, 1, 'G', 'G', 	2, 3, null, null, 1, 0, 	5, 1, 2, null, 	null, 0, 3);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (98, 1, 'B', 'B', 	2, 4, null, null, 1, 0, 	5, 1, 2, null, 	null, 0, 3);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (99, 1, 'HEX', 'HEX', 1, 5, null, null, 1, null, 	7, 1, 3, null, 	null, 0, 7);

----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-12852: Категории юридического лица по системе «светофор»
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (506, 'Категории юридического лица по системе «светофор»',1,0,0,null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5061, 506, 'Код категории', 'CODE',   1, 1, null, null, 1, null,   5, 1, 1, 1,   null, 0, 30);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5062, 506, 'Цвет',       'COLOR',   4, 2, 1, 95, 1, null,   5, 1, 0, null,   null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5063, 506, 'Описание',     'NAME',   1, 3, null, null, 1, null, 20, 1, 0, null, 	null, 0, 256);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 506, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5061, 'Категория 1');
	insert into ref_book_value (record_id, attribute_id, reference_value) values (seq_ref_book_record.currval, 5062, 8);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5063, 'Пороговые значения с большей долей вероятности за Налоговый период будут превышен. ');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 506, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5061, 'Категория 2');
	insert into ref_book_value (record_id, attribute_id, reference_value) values (seq_ref_book_record.currval, 5062, 8);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5063, 'Пороговые значения с большей долей вероятности за Налоговый период будут превышен. Назначается только ВЗЛ с общим режимом налогообложения');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 506, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5061, 'Категория 3');
	insert into ref_book_value (record_id, attribute_id, reference_value) values (seq_ref_book_record.currval, 5062, 1);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5063, 'Существует неопределенность в отношении того, будут ли превышены пороговые значения');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 4, 506, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5061, 'Категория 4');
	insert into ref_book_value (record_id, attribute_id, reference_value) values (seq_ref_book_record.currval, 5062, 12);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5063, 'Пороговые значения, с высокой долей вероятности, не будут превышены');


----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-12857: Правила назначения категории юридическому лицу



----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-12859: Статус по НДС
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (510,'Статус по НДС',1,0,0,null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5101, 510, 'Код статуса по НДС', 		'CODE', 2, 1, null, null, 1, 0, 	5, 1, 1, 1, 	null, 0, 1);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5102, 510, 'Описание', 					'NAME', 1, 2, null, null, 1, null, 	20, 1, 0, null, null, 0, 256);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 510, to_date('01.01.2008', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5101, 1);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5102, 'Организация, не признаваемая не признаваемая налогоплательщиком по НДС, или организация, освобожденная от обязанностей налогоплательщика');
	
insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 510, to_date('01.01.2008', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5101, 2);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5102, 'Прочие организации');


----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-12858: Специальный налоговый статус

INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (511,'Специальный налоговый статус',1,0,0,null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5111, 511, 'Код', 		'CODE', 2, 1, null, null, 1, 0, 	5, 1, 1, 1, 	null, 0, 1);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5112, 511, 'Описание',  'NAME', 1, 2, null, null, 1, null, 	20, 1, 0, null, null, 0, 256);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 511, to_date('01.01.2008', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5111, 1);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5112, 'Назначается только для российских юр. лиц, относящихся к одной из групп организаций, имеющих льготные условия уплаты налогов');
	
insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 511, to_date('01.01.2008', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5111, 2);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5112, 'Прочие российские юридические лица');

----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-12854: Критерии взаимозависимости
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (512, 'Критерии взаимозависимости',1,0,0,null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5121, 512, 'Код', 			'CODE', 	1, 1, null, null, 1, null, 	5, 1, 1, 1, 	null, 0, 15);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5122, 512, 'Описание', 		'NAME', 	1, 2, null, null, 1, null, 20, 1, 0, null, 	null, 0, 256);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 512, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5121, '1а');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5122, 'Организация и Банк в случае, если данная организация прямо и/ или косвенно участвует в капитале Банка, и доля такого участия составляет более 25%');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 512, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5121, '1б');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5122, 'Банк и организация в случае, если Банк прямо и/ или косвенно участвует в капитале данной организации, и доля такого участия составляет более 25%');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 512, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5121, '2');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5122, 'Физическое лицо и Банк в случае, если такое физическое лицо прямо и/ или косвенно участвует в капитале Банка, и доля такого участия составляет более 25%');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 4, 512, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5121, '3');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5122, 'Организация и Банк в случае, если одно и то же лицо прямо и/ или косвенно участвует в капитале этой организации и Банка, и доля такого участия в этой организации и Банке составляет более 25%');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 5, 512, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5121, '4а');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5122, 'Банк и лицо, имеющее полномочия по назначению единоличного исполнительного органа Банка или по назначению не менее 50% состава коллегиального исполнительного органа или совета директоров Банка');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 6, 512, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5121, '4б');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5122, 'Организация, имеющая полномочия по назначению единоличного исполнительного органа этой организации или по назначению не менее 50% состава коллегиального исполнительного органа или совета директоров этой организации');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 7, 512, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5121, '5');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5122, 'Организация, единоличные исполнительные органы которой либо не менее 50% состава коллегиального исполнительного органа или совета директоров которых назначены или избраны по решению одного и того же лица');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 8, 512, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5121, '6');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5122, 'Организация, в которых более 50% состава коллегиального исполнительного органа или совета директоров составляют одни и те же физические лица совместно с ВЗЛ, перечисленными для целей Критерия 11');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 9, 512, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5121, '7а');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5122, 'Банк и лицо, осуществляющее полномочия его единоличного исполнительного органа');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 10, 512, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5121, '7б');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5122, 'Организация и Банк, осуществляющий полномочия его единоличного исполнительного органа');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 11, 512, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5121, '8');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5122, 'Организация и Банк, в которых полномочия единоличного исполнительного органа осуществляет одно и то же лицо');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 12, 512, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5121, '9');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5122, 'Организации и/или физические лица в случае, если доля прямого участия каждого предыдущего лица в каждой последующей организации составляет более 50%');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 13, 512, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5121, '10');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5122, 'Физические лица в случае, если одно физическое лицо подчиняется другому физическому лицу по должностному положению');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 14, 512, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5121, '11');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5122, 'Физическое лицо, его супруг (супруга), родители (в том числе усыновители), дети (в том числе усыновленные), полнородные и неполнородные братья и сестры, опекун (попечитель) и подопечный');


----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-12853: Код организации
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (513,'Код организации',1,0,0,null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5131, 513, 'Код', 		'CODE', 2, 1, null, null, 1, 0, 	5, 1, 1, 1, 	null, 0, 1);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5132, 513, 'Описание',  'NAME', 1, 2, null, null, 1, null, 	20, 1, 0, null, null, 0, 256);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 513, to_date('01.01.2008', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5131, 1);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5132, 'Юридическое лицо является российской организацией');
	
insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 513, to_date('01.01.2008', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5131, 2);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5132, 'Юридическое лицо является иностранной организацией');

----------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------

COMMIT;
EXIT;