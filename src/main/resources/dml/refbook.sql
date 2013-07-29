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
INSERT INTO REF_BOOK (ID, NAME) VALUES (31, 'Параметры подразделения по транспортному налогу');
INSERT INTO REF_BOOK (ID, NAME) VALUES (32, 'Общие сведения');
INSERT INTO REF_BOOK (ID, NAME) VALUES (34, 'Общероссийский классификатор видов экономической деятельности');
INSERT INTO REF_BOOK (ID, NAME) VALUES (35, 'Признак лица, подписавшего документ');


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

INSERT INTO REF_BOOK_ATTRIBUTE VALUES (210,	34,	'Код ОКВЭД',						'CODE',			2,		0,		null,	null,	1,	0,			6);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (211,	34,	'Наименование',						'NAME',			1,		1,		null,	null,	1,	null,		250);

INSERT INTO REF_BOOK_ATTRIBUTE VALUES (212,	35,	'Код лица, подписавшего документ',	'CODE',			2,		0,		null,	null,	1,	0,			1);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (213,	35,	'Лицо, подписавшее документ',		'NAME',			1,		1,		null,	null,	1,	null,		50);

INSERT INTO REF_BOOK_ATTRIBUTE VALUES (170,	31,	'идентификатор (первичный ключ)',									'DEPARTMENT_ID',		4,		0,		null,	null,	1,	null,	9);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (171,	31,	'Признак лица подписавшего документ',								'SIGNATORY_ID',			4,		1,		35,		212,	1,	0,		1);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (172,	31,	'Фамилия подписанта',												'SIGNATORY_SURNAME',	1,		2,		null,	null,	1,	null,	120);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (173,	31,	'Имя подписанта',													'SIGNATORY_FIRSTNAME',	1,		3,		null,	null,	1,	null,	120);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (174,	31,	'Отчество подписанта',												'SIGNATORY_LASTNAME',	1,		4,		null,	null,	1,	null,	120);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (175,	31,	'Наименование документа, подтверждающего полномочия представителя',	'APPROVE_DOC_NAME',		1,		5,		null,	null,	1,	null,	240);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (176,	31,	'Наименование организации-представителя налогоплательщика',			'APPROVE_ORG_NAME',		1,		6,		null,	null,	1,	null,	2000);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (177,	31,	'Код места, по которому представляется документ',					'TAX_PLACE_TYPE_CODE',	4,		7,		2,		3,		1,	null,	3);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (178,	31,	'Версия программы, с помощью которой сформирован файл',				'APP_VERSION',			1,		8,		null,	null,	1,	null,	40);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (179,	31,	'Версия формата',													'FORMAT_VERSION',		1,		9,		null,	null,	1,	null,	5);

INSERT INTO REF_BOOK_ATTRIBUTE VALUES (180,	32,	'идентификатор (первичный ключ)',									'DEPARTMENT_ID',	4,		0,		null,	null,	1,	null,		9);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (181,	32,	'Субъект Российской Федерации (код)',								'DICT_REGION_ID',	4,		1,		4,		9,		1,	null,		2);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (182,	32,	'Код по ОКАТО',														'OKATO',			4,		2,		3,		7,		1,	null,		11);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (183,	32,	'ИНН',																'INN',				1,		3,		null,	null,	1,	null,		10);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (184,	32,	'КПП',																'KPP',				1,		4,		null,	null,	1,	null,		9);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (185,	32,	'Код налогового органа',											'TAX_ORGAN_CODE',	1,		5,		null,	null,	1,	null,		4);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (186,	32,	'Код вида экономической деятельности и по классификатору ОКВЭД',	'OKVED_CODE',		4,		6,		34,		210,	1,	null,		8);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (187,	32,	'Номер контактного телефона',										'PHONE',			1,		7,		null,	null,	1,	null,		20);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (188,	32,	'Код формы реорганизации и ликвидации',								'REORG_FORM_CODE',	4,		8,		5,		13,		1,	null,		1);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (189,	32,	'ИНН реорганизованного обособленного подразделения',				'REORG_INN',		1,		9,		null,	null,	1,	null,		10);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (190,	32,	'КПП реорганизованного обособленного подразделения',				'REORG_KPP',		1,		10,		null,	null,	1,	null,		9);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (191,	32,	'Наименование подразделения',										'NAME',				1,		11,		null,	null,	1,	null,		2000);

INSERT INTO REF_BOOK_ATTRIBUTE VALUES (192,	33,	'идентификатор (первичный ключ)',																					'DEPARTMENT_ID',		4,		0,		null,	null,	1,	null,			9);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (193,	33,	'Признак лица подписавшего документ',																				'SIGNATORY_ID',			4,		1,		35,		212,	1,	null,			1);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (194,	33,	'Фамилия подписанта',																								'SIGNATORY_SURNAME',	1,		2,		null,	null,	1,	null,			120);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (195,	33,	'Имя подписанта ',																									'SIGNATORY_FIRSTNAME',	1,		3,		null,	null,	1,	null,			120);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (196,	33,	'Отчество подписанта',																								'SIGNATORY_LASTNAME',	1,		4,		null,	null,	1,	null,			120);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (197,	33,	'Наименование документа, подтверждающего полномочия представителя',													'APPROVE_DOC_NAME',		1,		5,		null,	null,	1,	null,			240);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (198,	33,	'Наименование организации-представителя налогоплательщика',															'APPROVE_ORG_NAME',		1,		6,		null,	null,	1,	null,			2000);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (199,	33,	'Код места, по которому представляется документ',																	'TAX_PLACE_TYPE_CODE',	4,		7,		2,		3,		1,	null,			3);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (200,	33,	'Ставка налога',																									'TAX_RATE',				2,		8,		null,	null,	1,	2,				7);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (201,	33,	'Версия программы, с помощью которой сформирован файл',																'APP_VERSION',			1,		9,		null,	null,	1,	null,			40);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (202,	33,	'Версия формата',																									'FORMAT_VERSION',		1,		10,		null,	null,	1,	null,			5);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (203,	33,	'Обязанность по уплате налога',																						'OBLIGATION',			4,		11,		25,		110,	1,	0,				1);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (204,	33,	'Признак расчёта',																									'TYPE',					4,		12,		26,		120,	1,	0,				1);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (205,	33,	'Сумма налога на прибыль, выплаченная за пределами Российской Федерации в отчётном периоде',						'SUM_TAX',				2,		13,		null,	null,	1,	0,				15);
INSERT INTO REF_BOOK_ATTRIBUTE VALUES (206,	33,	'Сумма налога с выплаченных дивидендов за пределами Российской Федерации в последнем квартале отчётного периода',	'SUM_DIVIDENDS',		2,		14,		null,	null,	1,	0,				15);

update ref_book_attribute
set width=10
where width<10 and (ref_book_id>=22 OR ref_book_id<=9);

update ref_book_attribute
set width=100
where width>100 and (ref_book_id>=22 OR ref_book_id<=9);

commit;