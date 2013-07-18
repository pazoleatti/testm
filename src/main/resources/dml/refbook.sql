INSERT INTO REF_BOOK (ID, NAME) VALUES (1, 'Коды, определяющие способ представления налоговой декларации в налоговый орган');
INSERT INTO REF_BOOK (ID, NAME) VALUES (2, 'Коды представления налоговой по месту нахождения (учёта)');
INSERT INTO REF_BOOK (ID, NAME) VALUES (3, 'Коды ОКАТО и Муниципальных образований');
INSERT INTO REF_BOOK (ID, NAME) VALUES (4, 'Коды субъектов Российской Федерации');
INSERT INTO REF_BOOK (ID, NAME) VALUES (5, 'Коды форм реорганизации и ликвидации организации');
INSERT INTO REF_BOOK (ID, NAME) VALUES (6, 'Коды налоговых льгот');
INSERT INTO REF_BOOK (ID, NAME) VALUES (7, 'Параметры налоговых льгот');
INSERT INTO REF_BOOK (ID, NAME) VALUES (8, 'Коды, определяющие налоговый (отчётный) период');

INSERT INTO REF_BOOK_ATTRIBUTE VALUES (1,	1,	'код',										'CODE',				1,		0,		null,	null,	1,	null,	2);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (2,	1,	'наименование',								'NAME',				1,		1,		null,	null,	1,	null,	510);

INSERT INTO REF_BOOK_ATTRIBUTE VALUES (3,	2,	'код',										'CODE',				1,		0,		null,	null,	1,	null,	3);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (4,	2,	'наименование',								'NAME',				1,		1,		null,	null,	1,	null,	510);

INSERT INTO REF_BOOK_ATTRIBUTE VALUES (5,	3,	'Идентификатор записи',						'ID',				2,		0,		null,	null,	1,	0,		9);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (6,	3,	'Идентификатор родительской записи',		'PARENT_ID',		4,		1,		3,		8,		1,	null,	510);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (7,	3,	'Код ОКАТО',								'OKATO',			1,		2,		null,	null,	1,	null,	11);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (8,	3,	'Наименование муниципального образования',	'NAME',				1,		3,		null,	null,	1,	null,	510);

INSERT INTO REF_BOOK_ATTRIBUTE VALUES (9,	4,	'код',										'CODE',				1,		0,		null,	null,	1,	null,	2);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (10,	4,	'наименование',								'NAME',				1,		1,		null,	null,	1,	null,	510);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (11,	4,	'код ОКАТО',								'OKATO',			1,		2,		null,	null,	1,	null,	11);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (12,	4,	'определяющая часть кода ОКАТО',			'OKATO_DEFINITION',	1,		3,		null,	null,	1,	null,	11);

INSERT INTO REF_BOOK_ATTRIBUTE VALUES (13,	5,	'код',										'CODE',				1,		0,		null,	null,	1,	null,	1);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (14,	5,	'наименование',								'NAME',				1,		1,		null,	null,	1,	null,	510);

INSERT INTO REF_BOOK_ATTRIBUTE VALUES (15,	6,	'код',										'CODE',				1,		0,		null,	null,	1,	null,	10);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (16,	6,	'Наименование льготы',						'NAME',				1,		1,		null,	null,	1,	null,	4000);

INSERT INTO REF_BOOK_ATTRIBUTE VALUES (17,	7,	'код',										'CODE',				2,		0,		null,	null,	1,	0,		9);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (18,	7,	'Код региона',								'DICT_REGION_ID',	4,		1,		5,		14,		1,	null,	510);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (19,	7,	'Код налоговой  льготы',					'TAX_BENEFIT_ID',	4,		2,		6,		16,		1,	null,	4000);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (20,	7,	'Основание - статья',						'SECTION',			1,		3,		null,	null,	1,	null,	4);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (21,	7,	'Основание - пункт',						'ITEM',				1,		4,		null,	null,	1,	null,	4);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (22,	7,	'Основание - подпункт',						'SUBITEM',			1,		5,		null,	null,	1,	null,	4);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (23,	7,	'Уменьшающий процент, %',					'PERCENT',			2,		6,		null,	null,	1,	2,		6);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (24,	7,	'Пониженная ставка',						'RATE',				2,		7,		null,	null,	1,	2,		20);

INSERT INTO REF_BOOK_ATTRIBUTE VALUES (25,	8,	'код',										'CODE',				1,		0,		null,	null,	1,	null,	2);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (26,	8,	'наименование',								'NAME',				1,		1,		null,	null,	1,	null,	510);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (27,	8,	'Принадлежность к налогу на прибыль',		'I',				2,		2,		null,	null,	1,	0,		10);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (28,	8,	'Принадлежность к налогу на транспорт',		'T',				2,		3,		null,	null,	1,	0,		10);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (29,	8,	'Принадлежность к налогу на имущество',		'P',				2,		4,		null,	null,	1,	0,		10);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (30,	8,	'Принадлежность к налогу НДС',				'V',				2,		5,		null,	null,	1,	0,		10);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (31,	8,	'Принадлежность к ТЦО',						'D',				2,		6,		null,	null,	1,	0,		10);
commit;
