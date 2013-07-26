INSERT INTO REF_BOOK (ID, NAME) VALUES (1, 'Коды, определяющие способ представления налоговой декларации в налоговый орган');
INSERT INTO REF_BOOK (ID, NAME) VALUES (2, 'Коды представления налоговой по месту нахождения (учёта)');
INSERT INTO REF_BOOK (ID, NAME) VALUES (3, 'Коды ОКАТО и Муниципальных образований');
INSERT INTO REF_BOOK (ID, NAME) VALUES (4, 'Коды субъектов Российской Федерации');
INSERT INTO REF_BOOK (ID, NAME) VALUES (5, 'Коды форм реорганизации и ликвидации организации');
INSERT INTO REF_BOOK (ID, NAME) VALUES (6, 'Коды налоговых льгот');
INSERT INTO REF_BOOK (ID, NAME) VALUES (7, 'Параметры налоговых льгот');
INSERT INTO REF_BOOK (ID, NAME) VALUES (8, 'Коды, определяющие налоговый (отчётный) период');

INSERT INTO REF_BOOK (ID, NAME) VALUES (22, 'Курсы Валют');
INSERT INTO REF_BOOK (ID, NAME) VALUES (23, 'Ставки рефинансирования ЦБ РФ');
INSERT INTO REF_BOOK (ID, NAME) VALUES (24, 'Коды видов платежей');
INSERT INTO REF_BOOK (ID, NAME) VALUES (25, 'Признак возложения обязанности по уплате налога на обособленное подразделение');
INSERT INTO REF_BOOK (ID, NAME) VALUES (26, 'Признак составления расчёта');
INSERT INTO REF_BOOK (ID, NAME) VALUES (27, 'Классификатор расходов Сбербанка России для целей налогового учёта');
INSERT INTO REF_BOOK (ID, NAME) VALUES (28, 'Классификатор доходов Сбербанка России для целей налогового учёта');
INSERT INTO REF_BOOK (ID, NAME) VALUES (29, 'Классификатор соответствия счетов бухгалтерского учёта кодам налогового учёта');

INSERT INTO REF_BOOK (ID, NAME) VALUES (30, 'Подразделения');


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



INSERT INTO REF_BOOK_ATTRIBUTE VALUES (80,	22,	'Цифровой код валюты',						'CODE_NUMBER',		4,		0,		3,		40,		1,	null,	3); 
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (81,	22,	'Курс валюты',								'RATE',				2,		1,		null,	null,	1,	4,		24);

INSERT INTO REF_BOOK_ATTRIBUTE VALUES (90,	23,	'Ставка рефинансирования,  %',				'RATE',				2,		0,		null,	null,	1,	2,		19); 
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (91,	23,	'Нормативный документ',						'DOCUMENT',			1,		1,		null,	null,	1,	null,	255);

INSERT INTO REF_BOOK_ATTRIBUTE VALUES (100,	24,	'Код вида платежа',							'CODE',				1,		0,		null,	null,	1,	null,	1); 
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (101,	24,	'Наименование кода вида платежа',			'NAME',				1,		1,		null,	null,	1,	null,	4000);

INSERT INTO REF_BOOK_ATTRIBUTE VALUES (110,	25,	'Код',										'CODE',				2,		0,		null,	null,	1,	0,		1); 
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (111,	25,	'Значение',									'NAME',				1,		1,		null,	null,	1,	null,	50);

INSERT INTO REF_BOOK_ATTRIBUTE VALUES (120,	26,	'Код',										'CODE',				2,		0,		null,	null,	1,	0,		1); 
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (121,	26,	'Значение',									'NAME',				1,		1,		null,	null,	1,	null,	255);

INSERT INTO REF_BOOK_ATTRIBUTE VALUES (130,	27,	'Код налогового учёта ',					'CODE',				1,		0,		null,	null,	1,	null,	5); 
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (131,	27,	'Группа расхода',							'GROUP_EXP',		1,		1,		null,	null,	1,	null,	255);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (132,	27,	'Вид расхода по операциям',					'TYPE_EXP',			1,		2,		null,	null,	1,	null,	255);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (133,	27,	'Балансовый счёт по учёту расхода',			'BALANCE_ACCOUNT',	1,		3,		null,	null,	1,	null,	13);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (134,	27,	'Символ ОПУ',								'OPU',				1,		4,		null,	null,	1,	null,	13);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (135,	27,	'Нормативный документ(в актуальной редакции)', 
																							'NORMATIVE_DOCUMENT',1,		5,		null,	null,	1,	null,	255);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (136,	27,	'Учётное подразделение Центрального аппарата ОАО «Сбербанк России»', 
																							'UNIT',				1,		6,		null,	null,	1,	null,	255);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (137,	27,	'Первичный документ для налогового учёта', 	'BASIC_DOCUMENT',	1,		7,		null,	null,	1,	null,	255);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (138,	27,	'Форма регистра налогового учёта', 			'FORM',				1,		8,		null,	null,	1,	null,	255);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (139,	27,	'Признак расхода', 							'TYPE',				1,		9,		null,	null,	1,	null,	255);

INSERT INTO REF_BOOK_ATTRIBUTE VALUES (140,	28,	'Код налогового учёта ',					'CODE',				1,		0,		null,	null,	1,	null,	5); 
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (141,	28,	'Группа дохода',							'GROUP_INCOME',		1,		1,		null,	null,	1,	null,	255);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (142,	28,	'Вид доходов по операциям',					'TYPE_INCOME',		1,		2,		null,	null,	1,	null,	255);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (143,	28,	'Балансовый счёт по учёту расхода',			'BALANCE_ACCOUNT',	1,		3,		null,	null,	1,	null,	13);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (144,	28,	'Символ ОПУ',								'OPU',				1,		4,		null,	null,	1,	null,	13);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (145,	28,	'Нормативный документ(в актуальной редакции)', 
																							'NORMATIVE_DOCUMENT',1,		5,		null,	null,	1,	null,	255);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (146,	28,	'Учётное подразделение Центрального аппарата ОАО «Сбербанк России»', 
																							'UNIT',				1,		6,		null,	null,	1,	null,	255);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (147,	28,	'Первичный документ для налогового учёта', 	'BASIC_DOCUMENT',	1,		7,		null,	null,	1,	null,	255);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (148,	28,	'Форма регистра налогового учёта', 			'FORM',				1,		8,		null,	null,	1,	null,	255);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (149,	28,	'Признак дохода', 							'TYPE',				1,		9,		null,	null,	1,	null,	255);

INSERT INTO REF_BOOK_ATTRIBUTE VALUES (150,	29,	'КНУ',										'CODE',				1,		0,		null,	null,	1,	null,	5); 
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (151,	29,	'Наименование операции',					'NAME',				1,		1,		null,	null,	1,	null,	255);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (152,	29,	'Балансовый счёт',							'BALANCE_ACCOUNT',	1,		2,		null,	null,	1,	null,	255);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (153,	29,	'Символ ОПУ',								'OPU',				1,		3,		null,	null,	1,	null,	13);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (154,	29,	'РНУ',										'RNU',				1,		4,		null,	null,	1,	null,	13);

INSERT INTO REF_BOOK_ATTRIBUTE VALUES (160,	30,	'Код подразделения',							'CODE',				2,		0,		null,	null,	1,	0,		15);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (161,	30,	'Наименование подразделения',					'NAME',				1,		1,		null,	null,	1,	null,	255);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (162,	30,	'Сокращенное наименование подразделения',		'SHORTNAME',		1,		2,		null,	null,	1,	null,	255);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (163,	30,	'Код родительского подразделения',				'DICT_REGION_ID',	4,		3,		4,		9,		1,	null,	13);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (164,	30,	'Тип подразделения',							'TYPE',				2,		4,		null,	null,	1,	0,		15);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (165,	30,	'Индекс территориального банка',				'TB_INDEX',			1,		5,		null,	null,	1,	null,	2);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (166,	30,	'Код подразделения в нотации Сбербанка',		'SBRF_CODE',		1,		6,		null,	null,	1,	null,	255);

update ref_book_attribute
set width=10
where width<10 and (ref_book_id>=22 OR ref_book_id<=9);

update ref_book_attribute
set width=100
where width>100 and (ref_book_id>=22 OR ref_book_id<=9);

commit;