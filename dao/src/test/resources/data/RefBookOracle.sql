--Справочник со списком месяцом для периода. ref_book_record.id = report_period.DICT_TAX_PERIOD_ID. Получается январь, февраль, март.
insert into ref_book(id, name) values (8, 'Коды, определяющие налоговый (отчётный) период');
Insert into ref_book_attribute (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('25','8','Код','CODE','1','0',null,null,'1',null,'10','1','1',null,null,'0','2');
Insert into ref_book_attribute (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('26','8','Наименование','NAME','1','1',null,null,'1',null,'30','1','0',null,null,'0','255');
Insert into ref_book_attribute (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('27','8','Принадлежность к налогу на прибыль','I','2','2',null,null,'0','0','10','0','0',null,'6','0','1');
Insert into ref_book_attribute (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('28','8','Принадлежность к транспортному налогу','T','2','3',null,null,'0','0','10','0','0',null,'6','0','1');
Insert into ref_book_attribute (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('29','8','Принадлежность к налогу на имущество','P','2','4',null,null,'0','0','10','0','0',null,'6','0','1');
Insert into ref_book_attribute (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('30','8','Принадлежность к налогу НДС','V','2','5',null,null,'0','0','10','0','0',null,'6','0','1');
Insert into ref_book_attribute (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('31','8','Принадлежность к ТЦО','D','2','6',null,null,'0','0','10','0','0',null,'6','0','1');
Insert into ref_book_attribute (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('3000','8','Принадлежность к ЭНС','E','2','7',null,null,'0','0','10','0','0',null,'6','0','1');
Insert into ref_book_attribute (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('820','8','Дата начала периода','START_DATE','3','9',null,null,'1',null,'10','1','0',null,'5','0',null);
Insert into ref_book_attribute (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('821','8','Дата окончания периода','END_DATE','3','10',null,null,'1',null,'10','1','0',null,'5','0',null);
Insert into ref_book_attribute (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('844','8','Календарная дата начала периода','CALENDAR_START_DATE','3','11',null,null,'1',null,'10','1','0',null,'5','0',null);

Insert into ref_book_record (ID,RECORD_ID,REF_BOOK_ID,VERSION,STATUS) values ('181220','16','8',to_date('01.01.12','DD.MM.RR'),'0');
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181220','25','21',null,null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181220','26','первый квартал',null,null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181220','27',null,'1',null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181220','28',null,'1',null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181220','29',null,'1',null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181220','30',null,'1',null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181220','31',null,'1',null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181220','820',null,null,to_date('01.01.70','DD.MM.RR'),null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181220','821',null,null,to_date('31.03.70','DD.MM.RR'),null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181220','844',null,null,to_date('01.01.70','DD.MM.RR'),null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181220','3000',null,'1',null,null);

Insert into tax_type (ID,NAME) values ('D','ТЦО');

Insert into form_kind (ID,NAME) values ('1','Первичная');

insert into ref_book (id, name) values (13, 'Виды услуг');
insert into ref_book (id, name) values (1, 'ОКП');
Insert into ref_book (ID,NAME,SCRIPT_ID,VISIBLE,TYPE,READ_ONLY,REGION_ATTRIBUTE_ID,TABLE_NAME,IS_VERSIONED) values ('37','Параметры подразделения по УКС',null,'0','0','0',null,null,'1');
Insert into ref_book (ID,NAME,SCRIPT_ID,VISIBLE,TYPE,READ_ONLY,REGION_ATTRIBUTE_ID,TABLE_NAME,IS_VERSIONED) values ('35','Признак лица, подписавшего документ',null,'1','0','0',null,null,'1');


-- ОКП
Insert into ref_book_attribute (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('1','1','ОКП','NAME','1','1',null,null,'1',null,'50','0','0',null,null,'0','200');

-- Виды услуг
Insert into ref_book_attribute (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('60','13','Код','CODE','2','0',null,null,'1','0','10','1','1',null,null,'0','2');
Insert into ref_book_attribute (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('61','13','Услуга','NAME','1','1',null,null,'1',null,'50','0','0',null,null,'0','200');
Insert into ref_book_attribute (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('62','13','Код ОКП','OKP_CODE','4','2','1','1','1',null,'10','0','0',null,null,'0',null);

--Признак лица, подписавшего документ
Insert into ref_book_attribute (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('212','35','Код лица, подписавшего документ','CODE','2','0',null,null,'1','0','10','1','1',null,null,'0','1');
Insert into ref_book_attribute (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('213','35','Лицо, подписавшее документ','NAME','1','1',null,null,'1',null,'50','1','0',null,null,'0','50');

--Типы подразделений
Insert into department_type (ID,NAME) values ('1','Банк');
Insert into department_type (ID,NAME) values ('2','Территориальный банк');

--Подразделения
Insert into department (ID,NAME,PARENT_ID,TYPE,SHORTNAME,TB_INDEX,SBRF_CODE,REGION_ID,IS_ACTIVE,CODE,GARANT_USE) values ('0','Открытое акционерное общество "Сбербанк России"',null,'1','Банк','5','00__',null,'1','0','1');
Insert into department (ID,NAME,PARENT_ID,TYPE,SHORTNAME,TB_INDEX,SBRF_CODE,REGION_ID,IS_ACTIVE,CODE,GARANT_USE) values ('4','Байкальский банк','0','2','Байкальский банк','18','18_0000_00','181350','1','4','1');
Insert into department (ID,NAME,PARENT_ID,TYPE,SHORTNAME,TB_INDEX,SBRF_CODE,REGION_ID,IS_ACTIVE,CODE,GARANT_USE) values ('8','Волго-Вятский банк','0','2','Волго-Вятский банк','42','42_0000_00',null,'1','8','0');
Insert into department (ID,NAME,PARENT_ID,TYPE,SHORTNAME,TB_INDEX,SBRF_CODE,REGION_ID,IS_ACTIVE,CODE,GARANT_USE) values ('88','Среднерусский банк','0','2','Среднерусский банк','40','40_0000_00',null,'1','88','1');
Insert into ref_book (ID,NAME,SCRIPT_ID,VISIBLE,TYPE,READ_ONLY,REGION_ATTRIBUTE_ID,TABLE_NAME,IS_VERSIONED) values ('30','Подразделения',null,'1','1','0',null,'DEPARTMENT','0');
Insert into ref_book_attribute (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('162','30','Сокращенное наименование подразделения','SHORTNAME','1','2',null,null,'1',null,'50','0','0',null,null,'0','255');

--Настройки для МУКСа
Insert into ref_book_attribute (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('238','37','Признак лица подписавшего документ','SIGNATORY_ID','4','12','35','212','1',null,'10','0','0',null,null,'0',null);
Insert into ref_book_attribute (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('180','37','Идентификатор (первичный ключ)','DEPARTMENT_ID','4','0','30','162','1',null,'10','0','0',null,null,'0',null);


------------------------- getInactiveRecordsInPeriodTest ----------------------
--период  2015-01-01 -
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(18, 11, 13, date '2015-01-01', 0);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('18','60',null,'123',null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('18','61','запись_18',null,null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('18','62',null,null,null,1);

--период  2017-01-01 -
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(19, 12, 13, date '2017-01-01', 0);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('19','60',null,'19',null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('19','61','запись_19',null,null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('19','62',null,null,null,1);

--период  2015-01-01 - 2015-03-30
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(20, 13, 13, date '2015-01-01', 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(21, 13, 13, date '2015-03-30', 2);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('20','60',null,'20',null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('20','61','запись_20',null,null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('20','62',null,null,null,1);

--период  2015-01-01 - 2016-03-30
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(22, 14, 13, date '2015-01-01', 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(23, 14, 13, date '2016-03-30', 2);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('22','60',null,'123',null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('22','61','запись_22',null,null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('22','62',null,null,null,1);

--удалена
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(24, 15, 13, date '2015-01-01', -1);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('24','60',null,'24',null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('24','61','запись_24',null,null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('24','62',null,null,null,1);

--период  2015-01-01 - 2016-03-30 + следующая за ней версия от 01.04.2016
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(25, 16, 13, date '2015-01-01', 0);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('25','60',null,'25',null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('25','61','запись_25',null,null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('25','62',null,null,null,1);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(28, 16, 13, date '2016-01-01', -1);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(26, 16, 13, date '2015-02-28', 2);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(27, 16, 13, date '2016-04-01', 0);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('27','60',null,'25',null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('27','61','запись_27',null,null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('27','62',null,null,null,1);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(31, 16, 13, date '2017-01-01', -1);

--период  2015-01-01 - 2015-12-31 (задана следующей версией)
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(29, 17, 13, date '2015-01-01', 0);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('29','60',null,'29',null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('29','61','запись_29',null,null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('29','62',null,null,null,1);

insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(30, 17, 13, date '2016-01-01', 0);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('30','60',null,'29',null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('30','61','запись_30',null,null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('30','62',null,null,null,1);

-- ОКП
--период  2015-01-01 -
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(1, 1, 1, date '2015-01-01', 0);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('1','1','ОКП_1',null,null,null);

--Признак лица подписавшего документ
--период  2015-01-01 -
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(32, 18, 35, date '2015-01-01', 0);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('32','212',null,'1',null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('32','213','Налогоплательщик (налоговый агент)',null,null,null);

--Настройки для МУКСа
--Байскальский банк период  2015-01-01 -
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(33, 19, 37, date '2015-01-01', 0);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('33','180',null,null,null,'4');
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('33','238',null,null,null,'32');

--Сбербанк период  2015-01-01 - 2015-12-31
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(34, 20, 37, date '2015-01-01', 0);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('34','180',null,null,null,'0');
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('34','238',null,null,null,'32');
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(35, 20, 37, date '2015-12-31', 2);

--Волго-вятский банк период  2015-01-01 - 2015-12-31
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(36, 21, 37, date '2015-01-01', 0);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('36','180',null,null,null,'8');
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('36','238',null,null,null,'32');
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(37, 21, 37, date '2015-12-31', 2);
--Волго-вятский банк период  2016-01-01 -
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(38, 21, 37, date '2016-01-01', 0);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('38','180',null,null,null,'8');
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('38','238',null,null,null,'32');

--Среднерусский банк банк период  2015-01-01 - 2015-12-31
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(39, 22, 37, date '2015-01-01', 0);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('39','180',null,null,null,'88');
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('39','238',null,null,null,'32');
--Среднерусский банк банк период  2016-01-01 -
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(40, 22, 37, date '2016-01-01', 0);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('40','180',null,null,null,'88');
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('40','238',null,null,null,'32');

--Период 1 квартал 2015 для Байкальского банка
Insert into tax_period (ID,TAX_TYPE,YEAR) values ('11740','D','2015');
Insert into report_period (ID,NAME,TAX_PERIOD_ID,DICT_TAX_PERIOD_ID,START_DATE,END_DATE,CALENDAR_START_DATE) values ('2160','первый квартал','11740','181220',to_date('01.01.15','DD.MM.RR'),to_date('31.03.15','DD.MM.RR'),to_date('01.01.15','DD.MM.RR'));
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4','2160','0','0',null,'34182');
--Период 1 квартал 2017 для Байкальского банка
Insert into tax_period (ID,TAX_TYPE,YEAR) values ('11741','D','2017');
Insert into report_period (ID,NAME,TAX_PERIOD_ID,DICT_TAX_PERIOD_ID,START_DATE,END_DATE,CALENDAR_START_DATE) values ('2161','первый квартал','11741','181220',to_date('01.01.17','DD.MM.RR'),to_date('31.03.17','DD.MM.RR'),to_date('01.01.17','DD.MM.RR'));
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4','2161','0','0',null,'34183');

--РНУ-4 с макетами 2012-2016-
Insert into form_type (ID,NAME,TAX_TYPE,STATUS,CODE,IS_IFRS,IFRS_NAME) values ('316','(РНУ-4) Простой регистр налогового учета "доходы"','D','0','852-4','0',null);
Insert into form_template (ID,TYPE_ID,DATA_ROWS,FIXED_ROWS,NAME,FULLNAME,SCRIPT,DATA_HEADERS,VERSION,STATUS,MONTHLY,HEADER,COMPARATIVE,ACCRUING)
values ('316','316','','0','(РНУ-4) Простой регистр налогового учета "доходы"','(РНУ-4) Простой регистр налогового учета "доходы"','','',to_date('01.01.12','DD.MM.RR'),'0','0','852-4','0','0');
Insert into form_template (ID,TYPE_ID,DATA_ROWS,FIXED_ROWS,NAME,FULLNAME,SCRIPT,DATA_HEADERS,VERSION,STATUS,MONTHLY,HEADER,COMPARATIVE,ACCRUING)
values ('1316','316','','0','(РНУ-4) Простой регистр налогового учета "доходы"','(РНУ-4) Простой регистр налогового учета "доходы"','','',to_date('01.01.16','DD.MM.RR'),'0','0','852-4','0','0');

--РНУ-4 для Байкальского банка за 1 квартал 2015 года
Insert into form_data (ID,FORM_TEMPLATE_ID,STATE,KIND,RETURN_SIGN,PERIOD_ORDER,NUMBER_PREVIOUS_ROW,DEPARTMENT_REPORT_PERIOD_ID,MANUAL,SORTED,NUMBER_CURRENT_ROW,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,SORTED_BACKUP,EDITED,NOTE)
values ('1','316','1','1','0',null,'0','34182','0','0',null,null,'0','0','0',null);
Insert into form_data_ref_book (FORM_DATA_ID,REF_BOOK_ID,RECORD_ID) values ('1','35','32');
--РНУ-4 для Байкальского банка за 1 квартал 2017 года
Insert into form_data (ID,FORM_TEMPLATE_ID,STATE,KIND,RETURN_SIGN,PERIOD_ORDER,NUMBER_PREVIOUS_ROW,DEPARTMENT_REPORT_PERIOD_ID,MANUAL,SORTED,NUMBER_CURRENT_ROW,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,SORTED_BACKUP,EDITED,NOTE)
values ('2','1316','1','1','0',null,'0','34183','0','0',null,null,'0','0','0',null);
Insert into form_data_ref_book (FORM_DATA_ID,REF_BOOK_ID,RECORD_ID) values ('2','35','32');