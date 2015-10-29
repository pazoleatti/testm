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

INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (505,'Типы участников ТЦО (расширенный)',1,0,0,null);

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
--http://jira.aplana.com/browse/SBRFACCTAX-12856: Пороговые значения

INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (514, 'Пороговые значения',1,0,0,null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5141, 514, 'Тип участника ТЦО',       'CODE',   4, 1, 505, 5051, 1, null,   30, 1, 1, null,   null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5142, 514, 'Пороговое значение (руб.)',     'VALUE',   2, 2, null, null, 1, 0, 20, 1, 0, null, 	null, 0, 12);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 514, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5141, record_id from ref_book_value where attribute_id = 5051 and string_value = 'ВЗЛ ОРН';
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5142, 1000000000);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 514, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5141, record_id from ref_book_value where attribute_id = 5051 and string_value = 'ВЗЛ СРН';
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5142, 60000000);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 514, to_date('02.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5141, record_id from ref_book_value where attribute_id = 5051 and string_value = 'ИВЗЛ';
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5142, 0);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 4, 514, to_date('03.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5141, record_id from ref_book_value where attribute_id = 5051 and string_value = 'РОЗ';
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5142, 60000000);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 5, 514, to_date('04.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5141, record_id from ref_book_value where attribute_id = 5051 and string_value = 'НЛ';
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5142, 60000000);

----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-12857: Правила назначения категории юридическому лицу

INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (515, 'Правила назначения категории юридическому лицу',1,0,0,null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5151, 515, 'Тип юридического лица',       'CODE',   4, 1, 505, 5051, 1, null,   15, 1, 1, null,   null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5152, 515, 'Категория',       'CATEGORY',   4, 2, 506, 5061, 1, null,   15, 1, 0, 1,   null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5153, 515, 'Минимальный объем доходов и расходов (руб.)',  'MIN_VALUE',   2, 3, null, null, 1, 0, 10, 1, 1, null,   null, 0, 12);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5154, 515, 'Максимальный объем доходов и расходов (руб.)', 'MAX_VALUE',   2, 4, null, null, 1, 0, 10, 0, 0, null, 	null, 0, 12);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 515, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
  insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5151, record_id from ref_book_value where attribute_id = 5051 and string_value = 'ВЗЛ СРН';
  insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5152, record_id from ref_book_value where attribute_id = 5061 and string_value = 'Категория 1';
  insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5153, 0);
  
insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 515, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
  insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5151, record_id from ref_book_value where attribute_id = 5051 and string_value = 'ИВЗЛ';
  insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5152, record_id from ref_book_value where attribute_id = 5061 and string_value = 'Категория 1';
  insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5153, 0);
  
insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 515, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
  insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5151, record_id from ref_book_value where attribute_id = 5051 and string_value = 'РОЗ';
  insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5152, record_id from ref_book_value where attribute_id = 5061 and string_value = 'Категория 1';
  insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5153, 0);
  
insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 4, 515, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
  insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5151, record_id from ref_book_value where attribute_id = 5051 and string_value = 'НЛ';
  insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5152, record_id from ref_book_value where attribute_id = 5061 and string_value = 'Категория 1';
  insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5153, 0);
  
insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 5, 515, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
  insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5151, record_id from ref_book_value where attribute_id = 5051 and string_value = 'ВЗЛ ОРН';
  insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5152, record_id from ref_book_value where attribute_id = 5061 and string_value = 'Категория 2';
  insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5153, 700000000);
  
insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 6, 515, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
  insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5151, record_id from ref_book_value where attribute_id = 5051 and string_value = 'ВЗЛ ОРН';
  insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5152, record_id from ref_book_value where attribute_id = 5061 and string_value = 'Категория 3';
  insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5153, 500000000);
  insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5154, 699999999);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 7, 515, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
  insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5151, record_id from ref_book_value where attribute_id = 5051 and string_value = 'ВЗЛ ОРН';
  insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5152, record_id from ref_book_value where attribute_id = 5061 and string_value = 'Категория 4';
  insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5153, 0);
  insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5154, 499999999);

----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-12882: Группировка доходов и расходов, не учитываемых при расчете налога на прибыль
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (516, 'Группировка доходов и расходов, не учитываемых при расчете налога на прибыль',1,1,0,null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5161, 516, '№ п/п', 'NUMBER', 1, 1, null, null, 1, null, 6, 1, 1, null, null, 0, 5);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5162, 516, 'Наименование показателей', 'NAME', 1, 2, null, null, 1, null, 30, 1, 1, null, null, 0, 300);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5163, 516, 'Символ формы 102', 'SYMBOL_102', 1, 3, null, null, 1, null, 30, 1, 0, null, null, 0, 350);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5164, 516, 'Родительский показатель', 'PARENT_ID',   4, 4, 516, 5162, 1, null,   15, 0, 0, null,   null, 0, null);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, 'I');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, 'Доходы не учитываемые всего');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '(16305.02+17201.97+17202.07+17202.08 +17202.09+17202.97+17202.99+17203.02 +17203.03+17203.06+17203.09+17203.11 +17203.13+17203.14+17203.97+17306.19 +17306.20+17306.99+17307)');
	
insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, 'II');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, 'Расходы не учитываемые всего');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, 'сумма строк по полю № п/п (1+2+3+4+5+6+7+8+9+10+11+12+ 13+14+15+16+17+18+19+20)');
	
insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '1');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, 'Оплата труда, премии ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '(26101.04+26101.12+26101.13+26101.14 +26101.99+26401.04+27203.08)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = 'Расходы не учитываемые всего';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 4, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '2');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, 'Социальные выплаты (материальная помощь) ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '(26104.01+26104.02+26104.03+26104.04 +26104.05+26104.06+26104.99+27308.15 +27308.16+27308.17+27308.18)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = 'Расходы не учитываемые всего';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 5, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '3');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, 'Платежи по договорам добровольного личного страхования (негосударственного пенсионного обеспечения) работников ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '26410.04');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = 'Расходы не учитываемые всего';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 6, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '4');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, 'Пенсионные взносы в рамках КПП');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '(27308.20+27308.21)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = 'Расходы не учитываемые всего';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 7, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '5');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, 'Техническое обслуживание и ремонт, стоимость инвентаря и принадлежностей, переданных в эксплуатацию');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '(26301.02+26301.06+26301.08+26301.10 +26301.12+26301.14+26301.16+26302.02 +26302.04+26302.06+26302.08+26302.10 +26302.12+26302.14+26302.16+26302.18 +26302.20+26305.02+26305.05+26305.07 +26305.09+26305.11+26305.13+27203.20 +27203.22+27203.24+27203.26+27203.28 +27203.30+27203.32+27203.34+27203.46)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = 'Расходы не учитываемые всего';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 8, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '6');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, 'Арендная плата');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '(26303.02+27203.36)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = 'Расходы не учитываемые всего';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 9, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '7');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, 'Расходы на служебные командировки');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '26402.02');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = 'Расходы не учитываемые всего';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 10, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '8');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, 'Расходы по договорам ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '(26403.04+26406.06+26406.07+26406.08 +26406.09+26406.10+26406.11+26406.13)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = 'Расходы не учитываемые всего';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 11, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '9');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, 'Представительские расходы');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '26405.02');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = 'Расходы не учитываемые всего';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 12, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '10');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, 'Налоги, всего');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '(26411.02+26411.05+26411.06+26411.09 +26411.10+26411.11+27203.02+27203.05)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = 'Расходы не учитываемые всего';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 13, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '10.1');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, 'НДС');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '(26411.02+26411.11+27203.02)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = 'Налоги, всего';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 14, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '10.2');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, 'Другие налоги и сборы, не учитываемые для целей налогообложения');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '(26411.05+26411.10+27203.05)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = 'Налоги, всего';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 15, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '10.3');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, 'Налог на прибыль (доход), удержанный за рубежом, не учитываемый для целей налогообложения');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '26411.06');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = 'Налоги, всего';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 16, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '10.4');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, 'Плата за негативное воздействие на окружающую среду, сверх установленных норм');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '26411.09');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = 'Налоги, всего';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 17, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '11');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, 'Транспортные расходы');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '26412.03');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = 'Расходы не учитываемые всего';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 18, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '12');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, 'Типографские и канцелярские расходы, не учитываемые для целей налогообложения');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '(26412.09+26412.22+26412.24)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = 'Расходы не учитываемые всего';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 19, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '13');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, 'Расходы на форменную и специальную одежду');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '26412.12');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = 'Расходы не учитываемые всего';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 20, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '14');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, 'Капитальные затраты в арендованные банком объекты основных средств, не возмещаемые арендодателями');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '26412.14');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = 'Расходы не учитываемые всего';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 21, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '15');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, 'Прочие расходы, относящиеся к организационным и управленческим расходам, не учитываемые для целей налогообложения');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '(26412.99+27203.13+27203.48+26412.33 +26412.34+26412.35+26412.36+26412.37)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = 'Расходы не учитываемые всего';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 22, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '16');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, 'Платежи в возмещение причиненных убытков');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '27301');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = 'Расходы не учитываемые всего';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 23, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '17');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, 'Переоценка и перевод имущества, временно не использованное; начисление амортизации');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '(27203.09+27203.11+27308.22+27308.23)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = 'Расходы не учитываемые всего';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 24, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '18');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, ' Уплата штрафов, пеней, неустоек, не учитываемые для целей налогообложения');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '(27103+27203.15)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = 'Расходы не учитываемые всего';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 25, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '19');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, 'Прочие расходы');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '(25203.03+25302.02+26401.02+26403.02 +26410.06+26410.12+26410.98+27201.97 +27202.99+27203.38+27203.40+27203.42 +27203.44+27203.97+27203.99+27302 +27303+27304+27307+27308.97+27308.98)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = 'Расходы не учитываемые всего';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 26, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '20');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, 'Иные расходы');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, 'сумма строк по полю № п/п (20.1+20.2+20.3)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = 'Расходы не учитываемые всего';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 27, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '20.1');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, 'Расходы на благотворительность и другие аналогичные цели');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '27305');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = 'Иные расходы';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 28, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '20.2');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, 'Расходы на осуществление спортивных мероприятий, отдыха, мероприятий культурно-просветительского характера и иных аналогичных мероприятий');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '27306');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = 'Иные расходы';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 29, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '20.3');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, 'Подарки и денежные поощрения клиентов, партнеров Банка, прочих физических и юридических лиц');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '27308.25');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = 'Иные расходы';

----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-12861: Юридические лица
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (520, 'Юридические лица',1,0,0,null);

insert into ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (5201,520,'Полное наименование юридического лица с указанием ОПФ','NAME',1,1,null,null,1,null,30,1,1,null,null,0,256);
insert into ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (5202,520,'Место нахождения (юридический адрес) юридического лица (из устава)','ADDRESS',1,2,null,null,1,null,30,1,0,null,null,0,256);
insert into ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (5203,520,'Код организации ','ORG_CODE',4,3,513,5131,1,null,10,1,0,null,null,0,null);
insert into ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (5204,520,'Код страны по ОКСМ','COUNTRY_CODE',4,4,10,50,1,null,10,1,0,null,null,0,null);
insert into ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (5205,520,'ИНН (заполняется для резидентов, некредитных организаций)','INN',1,5,null,null,1,null,10,0,0,null,null,0,10);
insert into ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (5206,520,'КПП (заполняется для резидентов, некредитных организаций)','KPP',1,6,null,null,1,null,10,0,0,null,null,0,9);
insert into ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (5207,520,'КИО(заполняется для нерезидентов)','KIO',1,7,null,null,1,null,10,0,0,null,null,0,10);
insert into ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (5208,520,'Код SWIFT (заполняется для кредитных организаций, резидентов и нерезидентов)','SWIFT',1,8,null,null,1,null,10,0,0,null,null,0,11);
insert into ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (5209,520,'Регистрационный номер в стране инкорпорации (заполняется для нерезидентов)','REG_NUM',1,9,null,null,1,null,10,0,0,null,null,0,60);
insert into ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (5210,520,'Дата наступления основания для включения в список','START_DATE',3,10,null,null,1,null,10,1,0,null,1,0,null);
insert into ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (5211,520,'Дата наступления основания для исключения из списка','END_DATE',3,11,null,null,1,null,10,0,0,null,1,0,null);
insert into ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (5212,520,'Статус по НДС','VAT_STATUS',4,12,510,5101,1,null,10,1,0,null,null,0,null);
insert into ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (5213,520,'Специальный налоговый статус','TAX_STATUS',4,13,511,5111,1,null,10,0,0,null,null,0,null);
insert into ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (5214,520,'Критерий взаимозависимости','DEP_CRITERION',4,14,512,5121,1,null,10,1,0,null,null,0,null);

----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-13088
--http://jira.aplana.com/browse/SBRFACCTAX-13084
update ref_book set is_versioned = 0 where id in (510, 511);

COMMIT;
EXIT;