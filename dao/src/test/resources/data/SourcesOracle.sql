--Налог
INSERT INTO tax_type (id, name) VALUES ('I', 'Прибыль');
INSERT INTO tax_type (id, name) VALUES ('V', 'НДС');
-- Тип подразделения
Insert into department_type (ID,NAME) values ('1','Банк');
Insert into department_type (ID,NAME) values ('2','Территориальный банк');
--Виды нф
Insert into FORM_KIND (ID,NAME) values ('1','Первичная');
Insert into FORM_KIND (ID,NAME) values ('2','Консолидированная');
Insert into FORM_KIND (ID,NAME) values ('3','Сводная');

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

insert into ref_book_record(id, record_id, ref_book_id, version, status) values (1, 1, 8, to_date('01.01.2000', 'DD.MM.YY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (181228, 181228, 8, to_date('01.01.2000', 'DD.MM.YY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (181222, 181222, 8, to_date('01.01.2000', 'DD.MM.YY'), 0);

--Первый квартал для прибыли и НДС
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values (1,'25','21',null,null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values (1,'26','первый квартал',null,null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values (1,'27',null,'1',null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values (1,'28',null,'1',null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values (1,'29',null,'1',null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values (1,'30',null,'1',null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values (1,'31',null,'0',null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values (1,'820',null,null,to_date('01.01.70','DD.MM.RR'),null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values (1,'821',null,null,to_date('31.03.70','DD.MM.RR'),null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values (1,'844',null,null,to_date('01.01.70','DD.MM.RR'),null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values (1,'3000',null,'1',null,null);

--Полугодие для прибыли
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181228','25','31',null,null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181228','26','полугодие',null,null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181228','27',null,'1',null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181228','28',null,'0',null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181228','29',null,'1',null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181228','30',null,'0',null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181228','31',null,'1',null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181228','820',null,null,to_date('01.01.70','DD.MM.RR'),null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181228','821',null,null,to_date('30.06.70','DD.MM.RR'),null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181228','844',null,null,to_date('01.04.70','DD.MM.RR'),null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181228','3000',null,'0',null,null);

--третий квартал для НДС
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181222','25','23',null,null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181222','26','третий квартал',null,null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181222','27',null,'0',null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181222','28',null,'1',null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181222','29',null,'0',null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181222','30',null,'1',null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181222','31',null,'0',null,null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181222','820',null,null,to_date('01.07.70','DD.MM.RR'),null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181222','821',null,null,to_date('30.09.70','DD.MM.RR'),null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181222','844',null,null,to_date('01.07.70','DD.MM.RR'),null);
Insert into ref_book_value (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('181222','3000',null,'1',null,null);

--CREATE TABLE form_data_1 (
--  id NUMBER(18) NOT NULL,
--	form_data_id NUMBER(18) NOT NULL
--);

--СБ
Insert into department (ID,NAME,PARENT_ID,TYPE,SHORTNAME,TB_INDEX,SBRF_CODE,REGION_ID,IS_ACTIVE,CODE,GARANT_USE) values ('0','Открытое акционерное общество "Сбербанк России"',null,'1','Банк','5','00__',null,'1','0','1');
--Подразделение Байкальский банк
Insert into department (ID,NAME,PARENT_ID,TYPE,SHORTNAME,TB_INDEX,SBRF_CODE,REGION_ID,IS_ACTIVE,CODE,GARANT_USE)
values ('4','Байкальский банк','0','2','Байкальский банк','18','18_0000_00','181350','1','4','1');
--Подразделение Волго-вятский банк
Insert into department (ID,NAME,PARENT_ID,TYPE,SHORTNAME,TB_INDEX,SBRF_CODE,REGION_ID,IS_ACTIVE,CODE,GARANT_USE)
values ('8','Волго-Вятский банк','0','2','Волго-Вятский банк','42','42_0000_00','181325','1','8','0');

--Тип нф = рну-1
insert into form_type (id, name, tax_type, status, code) values (1, 'РНУ-1', 'I', 0, 'code_1');
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (1, 1, to_date('01.01.2000', 'DD.MM.YY'), 0, 0, 'РНУ-1', 'РНУ-1', 0, 0, 0);
--Тип нф = рну-2
insert into form_type (id, name, tax_type, status, code) values (2, 'РНУ-2', 'I', 0, 'code_2');
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (2, 2, to_date('01.01.1990', 'DD.MM.YY'), 0, 0, 'РНУ-2', 'РНУ-2', 0, 0, 0);
-- Удаленный РНУ-2, проверка http://jira.aplana.com/browse/SBRFACCTAX-13952
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (1122, 2, to_date('01.01.1995', 'DD.MM.YY'), 0, 0, 'РНУ-2-2', 'РНУ-2', 2, 0, 0);
--Тип нф = рну-3
insert into form_type (id, name, tax_type, status, code) values (3, 'РНУ-3', 'I', 0, 'code_3');
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (3, 3, to_date('01.01.2000', 'DD.MM.YY'), 0, 0, 'РНУ-3', 'РНУ-3', 0, 0, 0);
--Тип нф = рну-4 (ежемесячная)
insert into form_type (id, name, tax_type, status, code) values (4, 'РНУ-4', 'I', 0, 'code_4');
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (4, 4, to_date('01.01.2000', 'DD.MM.YY'), 1, 0, 'РНУ-4', 'РНУ-4', 0, 0, 0);
--Тип нф = рну-5
insert into form_type (id, name, tax_type, status, code) values (5, 'РНУ-5', 'I', 0, 'code_5');
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (5, 5, to_date('01.01.2000', 'DD.MM.YY'), 0, 0, 'РНУ-5', 'РНУ-5', 0, 0, 0);
--Тип нф = рну-6
insert into form_type (id, name, tax_type, status, code) values (6, 'РНУ-6', 'I', 0, 'code_6');
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (6, 6, to_date('01.01.2000', 'DD.MM.YY'), 0, 0, 'РНУ-6', 'РНУ-6', 0, 0, 0);
--Тип нф = рну-7 (с периодом сравнения и признаком нарастающего итога)
insert into form_type (id, name, tax_type, status, code) values (7, 'РНУ-7', 'I', 0, 'code_7');
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (7, 7, to_date('01.01.2000', 'DD.MM.YY'), 0, 0, 'РНУ-7', 'РНУ-7', 0, 1, 1);
--Тип нф = рну-8 (с периодом сравнения и признаком нарастающего итога)
insert into form_type (id, name, tax_type, status, code) values (8, 'РНУ-8', 'I', 0, 'code_8');
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (8, 8, to_date('01.01.2000', 'DD.MM.YY'), 0, 0, 'РНУ-8', 'РНУ-8', 0, 1, 1);
--Тип нф = рну-9
insert into form_type (id, name, tax_type, status, code) values (9, 'РНУ-9', 'I', 0, 'code_9');
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (9, 9, to_date('01.01.2000', 'DD.MM.YY'), 0, 0, 'РНУ-9', 'РНУ-9', 0, 0, 0);
--Тип нф = рну-10
insert into form_type (id, name, tax_type, status, code) values (10, 'РНУ-10', 'I', 0, 'code_10');
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (10, 10, to_date('01.01.2000', 'DD.MM.YY'), 0, 0, 'РНУ-10', 'РНУ-10', 0, 0, 0);
--Тип нф = рну-11
insert into form_type (id, name, tax_type, status, code) values (11, 'РНУ-11', 'I', 0, 'code_11');
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (11, 11, to_date('01.01.2000', 'DD.MM.YY'), 0, 0, 'РНУ-11', 'РНУ-11', 0, 0, 0);
--Тип нф = рну-88
insert into form_type (id, name, tax_type, status, code) values (88, 'РНУ-88', 'I', 0, 'code_88');
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (88, 88, to_date('01.01.2000', 'DD.MM.YY'), 0, 0, 'РНУ-88', 'РНУ-88', 0, 1, 1);
--Тип нф = рну-89
insert into form_type (id, name, tax_type, status, code) values (89, 'РНУ-89', 'I', 0, 'code_89');
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (89, 89, to_date('01.01.2000', 'DD.MM.YY'), 0, 0, 'РНУ-89', 'РНУ-89', 0, 0, 0);
--Тип нф = рну-12
insert into form_type (id, name, tax_type, status, code) values (12, 'РНУ-12', 'I', 0, 'code-12');
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (12, 12, to_date('01.01.2000', 'DD.MM.YY'), 0, 0, 'РНУ-12', 'РНУ-12', 0, 0, 0);
--Тип нф = рну-13
insert into form_type (id, name, tax_type, status, code) values (13, 'РНУ-13', 'I', 0, 'code-13');
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (13, 13, to_date('01.01.2000', 'DD.MM.YY'), 0, 0, 'РНУ-13', 'РНУ-13', 0, 0, 0);
--Тип нф = рну-14
insert into form_type (id, name, tax_type, status, code) values (14, 'РНУ-14', 'I', 0, 'code-14');
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (14, 14, to_date('01.01.2000', 'DD.MM.YY'), 0, 0, 'РНУ-14', 'РНУ-14', 0, 0, 0);
--Тип нф = рну-15
insert into form_type (id, name, tax_type, status, code) values (15, 'РНУ-15', 'I', 0, 'code-15');
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (15, 15, to_date('01.01.2000', 'DD.MM.YY'), 0, 0, 'РНУ-15', 'РНУ-15', 0, 1, 1);
--Тип нф = рну-16
insert into form_type (id, name, tax_type, status, code) values (16, 'РНУ-16', 'I', 0, 'code-16');
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (16, 16, to_date('01.01.2000', 'DD.MM.YY'), 0, 0, 'РНУ-16', 'РНУ-16', 0, 1, 1);
--Тип нф = рну-17
insert into form_type (id, name, tax_type, status, code) values (17, 'РНУ-17', 'I', 0, 'code-17');
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (17, 17, to_date('01.01.2000', 'DD.MM.YY'), 0, 0, 'РНУ-17', 'РНУ-17', 0, 1, 1);
--Тип нф = рну-33
insert into form_type (id, name, tax_type, status, code) values (33, 'РНУ-33', 'I', 0, 'code-33');
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (33, 33, to_date('01.01.2000', 'DD.MM.YY'), 0, 0, 'РНУ-33', 'РНУ-33', 0, 0, 0);
--Тип нф = рну-18
insert into form_type (id, name, tax_type, status, code) values (18, 'РНУ-18', 'I', 0, 'code-18');
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (18, 18, to_date('01.01.2000', 'DD.MM.YY'), 0, 0, 'РНУ-18', 'РНУ-18', 0, 0, 0);
--Тип нф = рну-19
insert into form_type (id, name, tax_type, status, code) values (19, 'РНУ-19', 'I', 0, 'code-19');
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (19, 19, to_date('01.01.2000', 'DD.MM.YY'), 0, 0, 'РНУ-19', 'РНУ-19', 0, 0, 0);
--Тип нф = рну-20
insert into form_type (id, name, tax_type, status, code) values (20, 'РНУ-20', 'I', 0, 'code-20');
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (20, 20, to_date('01.01.2000', 'DD.MM.YY'), 0, 0, 'РНУ-20', 'РНУ-20', 0, 0, 0);
--Тип нф = рну-21
insert into form_type (id, name, tax_type, status, code) values (21, 'РНУ-21', 'I', 0, 'code-21');
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (21, 21, to_date('01.01.2000', 'DD.MM.YY'), 0, 0, 'РНУ-21', 'РНУ-21', 0, 0, 0);
--Тип нф = рну-100
insert into form_type (id, name, tax_type, status, code) values (100, 'РНУ-100', 'I', 0, 'code-100');
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (100, 100, to_date('01.01.2000', 'DD.MM.YY'), 0, 0, 'РНУ-100', 'РНУ-100', 0, 1, 0);
--Тип нф = рну-77 (ежемесячная)
insert into form_type (id, name, tax_type, status, code) values (77, 'РНУ-77', 'I', 0, 'code-77');
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (77, 77, to_date('01.01.2000', 'DD.MM.YY'), 1, 0, 'РНУ-77', 'РНУ-77', 0, 0, 0);

--------------НДС--------
--Тип нф = ндс-200
insert into form_type (id, name, tax_type, status, code) values (200, 'НДС-200', 'V', 0, 'code-200');
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (200, 200, to_date('01.01.2000', 'DD.MM.YY'), 0, 0, 'НДС-200', 'НДС-200', 0, 0, 0);
--Тип нф = ндс-201 (ежемесячная)
insert into form_type (id, name, tax_type, status, code) values (201, 'НДС-201', 'V', 0, 'code-201');
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (201, 201, to_date('01.01.2000', 'DD.MM.YY'), 1, 0, 'НДС-201', 'НДС-201', 0, 0, 0);
--Тип нф = ндс-202
insert into form_type (id, name, tax_type, status, code) values (202, 'НДС-202', 'V', 0, 'code-202');
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (202, 202, to_date('01.01.2000', 'DD.MM.YY'), 0, 0, 'НДС-202', 'НДС-202', 0, 0, 0);
--Тип нф = ндс-203 (ежемесячная)
insert into form_type (id, name, tax_type, status, code) values (203, 'НДС-203', 'V', 0, 'code-203');
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (203, 203, to_date('01.01.2000', 'DD.MM.YY'), 1, 0, 'НДС-203', 'НДС-203', 0, 0, 0);
--Тип нф = ндс-204 (с периодом сравнения)
insert into form_type (id, name, tax_type, status, code) values (204, 'НДС-204', 'V', 0, 'code-204');
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (204, 204, to_date('01.01.2000', 'DD.MM.YY'), 0, 0, 'НДС-204', 'НДС-204', 0, 1, 0);
--Тип нф = ндс-205 (с периодом сравнения и признаком нарастающего итога)
insert into form_type (id, name, tax_type, status, code) values (205, 'НДС-205', 'V', 0, 'code-205');
insert into form_template (id, type_id, version, monthly, fixed_rows, name, fullname, status, comparative, accruing)
  values (205, 205, to_date('01.01.2000', 'DD.MM.YY'), 0, 0, 'НДС-205', 'НДС-205', 0, 1, 1);


--Тип декларации = Д-1
Insert into declaration_type (ID,TAX_TYPE,NAME,STATUS,IS_IFRS,IFRS_NAME) values (1,'I','Д-1','0','0',null);
Insert into declaration_template (ID,CREATE_SCRIPT,DECLARATION_TYPE_ID,XSD,VERSION,NAME,STATUS,JRXML)
values (1,'',1,null,to_date('01.01.2000', 'DD.MM.YY'),'Д-1','0',null);
--Тип декларации = Д-2
Insert into declaration_type (ID,TAX_TYPE,NAME,STATUS,IS_IFRS,IFRS_NAME) values (2,'I','Д-2','0','0',null);
Insert into declaration_template (ID,CREATE_SCRIPT,DECLARATION_TYPE_ID,XSD,VERSION,NAME,STATUS,JRXML)
values (2,'',2,null,to_date('01.01.2000', 'DD.MM.YY'),'Д-2','0',null);
--Тип декларации = Д-3
Insert into declaration_type (ID,TAX_TYPE,NAME,STATUS,IS_IFRS,IFRS_NAME) values (3,'I','Д-3','0','0',null);
Insert into declaration_template (ID,CREATE_SCRIPT,DECLARATION_TYPE_ID,XSD,VERSION,NAME,STATUS,JRXML)
values (3,'',3,null,to_date('01.01.2000', 'DD.MM.YY'),'Д-3','0',null);
--Тип декларации = Д-4
Insert into declaration_type (ID,TAX_TYPE,NAME,STATUS,IS_IFRS,IFRS_NAME) values (4,'I','Д-4','0','0',null);
Insert into declaration_template (ID,CREATE_SCRIPT,DECLARATION_TYPE_ID,XSD,VERSION,NAME,STATUS,JRXML)
values (4,'',4,null,to_date('01.01.2000', 'DD.MM.YY'),'Д-4','0',null);


--Назначение первичная РНУ-1 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (1,'4',1,'1');
--Назначение консолидированная РНУ-1 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (2,'4',1,'2');
--Назначение первичная РНУ-2 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (3,'4',2,'1');
--Назначение первичная РНУ-1 для Волго-Вятского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (4,'8',1,'1');
--Назначение первичная РНУ-3 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (5,'4',3,'1');
--Назначение первичная РНУ-4 (ежемесячная) для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (6,'4',4,'1');
--Назначение консолидированная РНУ-5 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (7,'4',5,'2');
--Назначение консолидированная РНУ-4 (ежемесячная) для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (8,'4',4,'2');
--Назначение консолидированная РНУ-6 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (9,'4',6,'2');
--Назначение консолидированная РНУ-8 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (10,'4',8,'2');
--Назначение первичная РНУ-7 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (11,'4',7,'1');
--Назначение консолидированная РНУ-1 для Волго-Вятского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (12,'8',1,'2');
--Назначение консолидированная РНУ-3 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (13,'4',3,'2');
--Назначение консолидированная РНУ-7 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (14,'4',7,'2');
--Назначение консолидированная РНУ-9 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (15,'4',9,'2');
--Назначение первичная РНУ-10 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (16,'4',10,'1');
--Назначение консолидированная РНУ-9 для Волго-Вятского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (17,'8',9,'2');
--Назначение первичная РНУ-11 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (18,'4',11,'1');
--Назначение консолидированная РНУ-2 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (19,'4',2,'2');
--Назначение консолидированная РНУ-88 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (20,'4',88,'2');
--Назначение первичная РНУ-89 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (21,'4',89,'1');
--Назначение первичная РНУ-12 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (22,'4',12,'1');
--Назначение первичная РНУ-14 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (23,'4',14,'1');
--Назначение первичная РНУ-16 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (24,'4',16,'1');
--Назначение первичная РНУ-17 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (25,'4',17,'1');
--Назначение консолидированная РНУ-13 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (26,'4',13,'2');
--Назначение консолидированная РНУ-33 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (27,'4',33,'2');
--Назначение консолидированная РНУ-15 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (28,'4',15,'2');
--Назначение консолидированная РНУ-13 для Волго-вятского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (29,'8',13,'2');
--Назначение консолидированная РНУ-18 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (30,'4',18,'2');
--Назначение первичная РНУ-19 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (31,'4',19,'1');
--Назначение консолидированная РНУ-20 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (32,'4',20,'2');
--Назначение консолидированная РНУ-21 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (33,'4',21,'2');
--Назначение консолидированная РНУ-100 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (34,'4',100,'2');
--Назначение первичная РНУ-100 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (35,'4',100,'1');
--Назначение первичная РНУ-100 для Волго-Вятского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (36,'8',100,'1');
--Назначение первичная РНУ-77 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (37,'4',77,'1');

----------НДС-----------
--Назначение первичная НДС-200 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (200,'4',200,'1');
--Назначение первичная НДС-201 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (201,'4',201,'1');
--Назначение первичная НДС-202 для Волго-вятского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (202,'8',202,'1');
--Назначение первичная НДС-203 для Байкальского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (203,'4',203,'1');
--Назначение первичная НДС-203 для Волго-вятского банка
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (204,'8',203,'1');
--Назначение первичная НДС-204 для Волго-вятского банка (с периодом сравнения)
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (205,'8',204,'1');
--Назначение первичная НДС-205 для Волго-вятского банка (с периодом сравнения и признаком нарастающего итога)
Insert into department_form_type (ID,DEPARTMENT_ID,FORM_TYPE_ID,KIND) values (206,'8',205,'1');

--Назначение декларация Д-1 для Байкальского банка
Insert into department_declaration_type (ID,DEPARTMENT_ID,DECLARATION_TYPE_ID) values (1,'4',1);
--Назначение декларация Д-2 для Байкальского банка
Insert into department_declaration_type (ID,DEPARTMENT_ID,DECLARATION_TYPE_ID) values (2,'4',2);
--Назначение декларация Д-3 для Байкальского банка
Insert into department_declaration_type (ID,DEPARTMENT_ID,DECLARATION_TYPE_ID) values (3,'4',3);
--Назначение декларация Д-4 для Байкальского банка
Insert into department_declaration_type (ID,DEPARTMENT_ID,DECLARATION_TYPE_ID) values (4,'4',4);
--Назначение декларация Д-1 для Волго-Вятского банка
Insert into department_declaration_type (ID,DEPARTMENT_ID,DECLARATION_TYPE_ID) values (5,'8',1);

--Связка источник->приемник "первичная РНУ-1 Байкальского банка -> консолидированная РНУ-1 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (2,1,date '2000-01-01',null);
--Связка источник->приемник "первичная РНУ-2 Байкальского банка -> консолидированная РНУ-1 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (2,3,date '2000-01-01',null);
--Связка источник->приемник "первичная РНУ-1 Волго-Вятского банка -> консолидированная РНУ-1 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (2,4,date '2000-01-01',null);
--Связка источник->приемник "первичная РНУ-3 Байкальского банка -> консолидированная РНУ-1 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (2,5,date '2100-01-01',null);
--Связка источник->приемник "первичная РНУ-4 (ежемесячная) Байкальского банка -> консолидированная РНУ-5 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (7,6,date '2000-01-01',null);
--Связка источник->приемник "первичная РНУ-2 Байкальского банка -> консолидированная РНУ-5 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (7,3,date '2000-01-01',null);
--Связка источник->приемник "первичная РНУ-1 Байкальского банка -> консолидированная РНУ-4 (ежемесячная) Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (8,1,date '2000-01-01',null);
--Связка источник->приемник "первичная РНУ-4 (ежемесячная) Байкальского банка -> консолидированная РНУ-4 (ежемесячная) Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (8,6,date '2000-01-01',null);
--Связка источник->приемник "первичная РНУ-7 (с периодом сравнения и признаком нарастающего итога) Байкальского банка -> консолидированная РНУ-6 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (9,11,date '2000-01-01',null);
--Связка источник->приемник "первичная РНУ-7 (с периодом сравнения и признаком нарастающего итога) Байкальского банка -> консолидированная РНУ-8 (с периодом сравнения и признаком нарастающего итога) Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (10,11,date '2000-01-01',null);
--Связка источник->приемник "первичная РНУ-1 Байкальского банка -> консолидированная РНУ-9 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (15,1,date '2000-01-01',null);
--Связка источник->приемник "первичная РНУ-10 Байкальского банка -> консолидированная РНУ-2 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (19,16,date '2000-01-01',null);
--Связка источник->приемник "первичная РНУ-10 Байкальского банка -> консолидированная РНУ-9 Волго-вятского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (17,16,date '2000-01-01',null);
--Связка источник->приемник "первичная РНУ-11 Байкальского банка -> консолидированная РНУ-4 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (8,18,date '2000-01-01',null);
--Связка источник->приемник "первичная РНУ-11 Байкальского банка -> консолидированная РНУ-2 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (19,18,date '2000-01-01',null);
--Связка источник->приемник "первичная РНУ-89 Байкальского банка -> консолидированная РНУ-88 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (20,21,date '2000-01-01',null);
--Связка источник->приемник "первичная РНУ-7 (с периодом сравнения и признаком нарастающего итога) Байкальского банка -> консолидированная РНУ-88 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (20,11,date '2000-01-01',null);
--Связка источник->приемник "первичная РНУ-12 Байкальского банка -> консолидированная РНУ-13 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (26,22,date '2000-01-01',null);
--Связка источник->приемник "первичная РНУ-12 Байкальского банка -> консолидированная РНУ-33 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (27,22,date '2000-01-01',null);
--Связка источник->приемник "первичная РНУ-14 Байкальского банка -> консолидированная РНУ-15 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (28,23,date '2000-01-01',null);
--Связка источник->приемник "первичная РНУ-16 Байкальского банка -> консолидированная РНУ-15 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (28,24,date '2000-01-01',null);
--Связка источник->приемник "первичная РНУ-12 Байкальского банка -> консолидированная РНУ-13 Волго-вятского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (29,22,date '2000-01-01',null);
--Связка источник->приемник "первичная РНУ-17 Байкальского банка -> консолидированная РНУ-13 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (26,25,date '2000-01-01',null);
--Связка источник->приемник "первичная РНУ-17 Байкальского банка -> консолидированная РНУ-33 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (27,25,date '2000-01-01',null);
--Связка источник->приемник "первичная РНУ-19 Байкальского банка -> консолидированная РНУ-20 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (32,31,date '2000-01-01',null);
--Связка источник->приемник "первичная РНУ-19 Байкальского банка -> консолидированная РНУ-21 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (33,31,date '2000-01-01',null);
--Связка источник->приемник "первичная РНУ-100 Байкальского банка -> консолидированная РНУ-100 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (34,35,date '2000-01-01',null);
--Связка источник->приемник "первичная РНУ-100 Волго-вятского банка -> консолидированная РНУ-100 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (34,36,date '2000-01-01',null);
--Связка источник->приемник "первичная РНУ-77 Байкальского банка -> консолидированная РНУ-1 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (2,37,date '2000-01-01',null);

---------------НДС------------
--Связка источник->приемник "первичная НДС-200 Байкальского банка -> консолидированная РНУ-1 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (2,200,date '2000-01-01',null);
--Связка источник->приемник "первичная НДС-200 Байкальского банка -> консолидированная РНУ-5 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (7,200,date '2000-01-01',null);
--Связка источник->приемник "первичная НДС-201 Байкальского банка -> консолидированная РНУ-5 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (7,201,date '2000-01-01',null);
--Связка источник->приемник "первичная НДС-202 Волго-вятского банка -> консолидированная РНУ-5 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (7,202,date '2000-01-01',null);
--Связка источник->приемник "первичная НДС-203 Байкальского банка -> консолидированная РНУ-5 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (7,203,date '2000-01-01',null);
--Связка источник->приемник "первичная НДС-203 Волго-вятского банка -> консолидированная РНУ-5 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (7,204,date '2000-01-01',null);
--Связка источник->приемник "первичная НДС-204 Волго-вятского банка (с периодом сравнения) -> консолидированная РНУ-8 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (10,205,date '2000-01-01',null);
--Связка источник->приемник "первичная НДС-205 Волго-вятского банка (с периодом сравнения и признаком нарастающего итога) -> консолидированная РНУ-8 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (10,206,date '2000-01-01',null);
--Связка источник->приемник "первичная РНУ-4 Байкальского банка -> первичная НДС-200 Байкальского банка"
Insert into form_data_source (DEPARTMENT_FORM_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (200,6,date '2000-01-01',null);

--Связка источник->приемник "консолидированная РНУ-1 Байкальского банка -> Декларация Д-1 Байкальского банка"
Insert into declaration_source (DEPARTMENT_DECLARATION_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (1,2,date '2000-01-01',null);
--Связка источник->приемник "консолидированная РНУ-5 Байкальского банка -> Декларация Д-1 Байкальского банка"
Insert into declaration_source (DEPARTMENT_DECLARATION_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (1,7,date '2000-01-01',null);
--Связка источник->приемник "консолидированная РНУ-1 Байкальского банка -> Декларация Д-2 Байкальского банка"
Insert into declaration_source (DEPARTMENT_DECLARATION_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (2,2,date '2000-01-01',null);
--Связка источник->приемник "консолидированная РНУ-1 Волго-вятского банка -> Декларация Д-2 Байкальского банка"
Insert into declaration_source (DEPARTMENT_DECLARATION_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (2,12,date '2000-01-01',null);
--Связка источник->приемник "консолидированная РНУ-3 Байкальского банка -> Декларация Д-1 Байкальского банка"
Insert into declaration_source (DEPARTMENT_DECLARATION_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (1,13,date '2100-01-01',null);
--Связка источник->приемник "первичная РНУ-4 (ежемесячная) Байкальского банка -> Декларация Д-3 Байкальского банка"
Insert into declaration_source (DEPARTMENT_DECLARATION_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (3,6,date '2000-01-01',null);
--Связка источник->приемник "консолидированная РНУ-5 Байкальского банка -> Декларация Д-3 Байкальского банка"
Insert into declaration_source (DEPARTMENT_DECLARATION_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (3,7,date '2000-01-01',null);
--Связка источник->приемник "консолидированная РНУ-7 Байкальского банка -> Декларация Д-4 Байкальского банка"
Insert into declaration_source (DEPARTMENT_DECLARATION_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (4,14,date '2000-01-01',null);
--Связка источник->приемник "консолидированная РНУ-18 Байкальского банка -> Декларация Д-1 Байкальского банка"
Insert into declaration_source (DEPARTMENT_DECLARATION_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (1,30,date '2000-01-01',null);
--Связка источник->приемник "консолидированная РНУ-18 Байкальского банка -> Декларация Д-1 Волго-Вятского банка"
Insert into declaration_source (DEPARTMENT_DECLARATION_TYPE_ID,SRC_DEPARTMENT_FORM_TYPE_ID,PERIOD_START,PERIOD_END) values (5,30,date '2000-01-01',null);


--------------------------- НФ-источники НФ ----------------------------

----------------test1, test2-------------

--Отчетный период 1 квартал 2000 - прибыль
insert into tax_period(id, tax_type, year) values (1, 'I', 2000);
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (1, 'первый квартал',  1, 1, date '2000-01-01', date '2000-03-31', date '2000-01-01');

--Отчетный период 1 квартал 2000 - ндс
insert into tax_period(id, tax_type, year) values (1001, 'V', 2000);
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (1001, 'первый квартал',  1001, 1, date '2000-01-01', date '2000-03-31', date '2000-01-01');

--Связка подразделение Байкальский банк - период 1 квартал 2000
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',1,'1','0',null,1);
--Связка подразделение Байкальский банк - период 1 квартал 2000
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',1001,'1','0',null,1001);

-- НФ консолидированная РНУ-1 в периоде 1 квартал 2000 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (1,1,3,2,null,1,null,0,0,    0,0,0,0);
-- НФ первичная РНУ-1 в периоде 1 квартал 2000 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (2,1,3,1,null,1,null,0,0,    0,0,0,0);

----------------test55-------------

--Отчетный период полугодие 2000 для прибыли
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (200, 'полугодие',  1, 181228, date '2000-01-01', date '2000-03-31', date '2000-01-01');

--Отчетный период полугодие 2000 для ндс
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (1200, 'полугодие',  1001, 181228, date '2000-01-01', date '2000-03-31', date '2000-01-01');

--Связка подразделение Байкальский банк - период полугодие 2000
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',200,'1','0',null,200);

-- НФ консолидированная РНУ-1 в периоде полугодие 2000 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (200,1,3,2,null,200,null,0,0,    0,0,0,0);
-- НФ первичная РНУ-1 в периоде полугодие 2000 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (201,1,3,1,null,200,null,0,0,    0,0,0,0);

----------------test56-------------

--Отчетный период 1 квартал 2050 для прибыли
insert into tax_period(id, tax_type, year) values (200, 'I', 2050);
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (201, '1 квартал',  200, 1, date '2050-01-01', date '2050-03-31', date '2050-01-01');
--Отчетный период 1 квартал 2050 для НДС
insert into tax_period(id, tax_type, year) values (201, 'V', 2050);
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (202, '1 квартал',  201, 1, date '2050-01-01', date '2050-03-31', date '2050-01-01');

--Связка подразделение Байкальский банк - период 1 квартал 2050 - прибыль
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',201,'1','0',null,201);
--Связка подразделение Байкальский банк - период 1 квартал 2050 - ндс
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',202,'1','0',null,202);

-- НФ консолидированная РНУ-1 в периоде 1 квартал 2050 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (202,1,3,2,null,201,null,0,0,    0,0,0,0);
-- НФ первичная РНУ-1 в периоде 1 квартал 2050 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (203,1,3,1,null,201,null,0,0,    0,0,0,0);
-- НФ первичная НДС-200 в периоде 1 квартал 2050 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (204,200,1,1,null,202,null,0,0,    0,0,0,0);

----------------test57-------------

--Отчетный период полугодие 2050 для прибыли
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (203, 'полугодие',  200, 181228, date '2050-01-01', date '2050-03-31', date '2050-06-30');
--Отчетный период 3 квартал 2050 для НДС
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (204, '3 квартал',  201, 181222, date '2050-01-01', date '2050-09-30', date '2050-07-01');

--Связка подразделение Байкальский банк - период полугодие 2050 - прибыль
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',203,'1','0',null,203);
--Связка подразделение Байкальский банк - период 3 квартал 2050 - ндс
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',204,'1','0',null,204);

-- НФ консолидированная РНУ-1 в периоде полугодие 2050 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (205,1,3,2,null,203,null,0,0,    0,0,0,0);
-- НФ первичная РНУ-1 в периоде полугодие 2050 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (206,1,3,1,null,203,null,0,0,    0,0,0,0);
-- НФ первичная НДС-200 в периоде 3 квартал 2050 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (207,200,1,1,null,204,null,0,0,    0,0,0,0);

----------------test3-------------

--Отчетный период 1 квартал 2002
insert into tax_period(id, tax_type, year) values (2, 'I', 2002);
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (2, 'первый квартал',  2, 1, date '2002-01-01', date '2002-03-31', date '2002-01-01');

--Связка подразделение Волго-Вятский банк - период 1 квартал 2002
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('8',2,'1','0',null,2);
--Связка подразделение Байкальский банк - период 1 квартал 2002
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',2,'1','0',null,3);

-- НФ консолидированная РНУ-1 в периоде 1 квартал 2002 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (3,1,1,2,null,3,null,0,0,    0,0,0,0);
-- НФ первичная РНУ-1 в периоде 1 квартал 2002 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (4,1,1,1,null,3,null,0,0,    0,0,0,0);
-- НФ первичная РНУ-1 в периоде 1 квартал 2002 для Волго-Вятского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (5,1,1,1,null,2,null,0,0,    0,0,0,0);

----------------test4-------------

--Отчетный период 1 квартал 2003
insert into tax_period(id, tax_type, year) values (3, 'I', 2003);
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (3, 'первый квартал',  3, 1, date '2003-01-01', date '2003-03-31', date '2003-01-01');

--Связка подразделение Байкальский банк - период 1 квартал 2003
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',3,'1','0',null,4);

-- НФ консолидированная РНУ-1 в периоде 1 квартал 2003 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (6,1,1,2,null,4,null,0,0,    0,0,0,0);
-- НФ первичная РНУ-3 в периоде 1 квартал 2003 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (7,3,1,1,null,4,null,0,0,    0,0,0,0);

----------------test5-------------

--Отчетный период 1 квартал 2004 прибыль
insert into tax_period(id, tax_type, year) values (4, 'I', 2004);
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (4, 'первый квартал',  4, 1, date '2004-01-01', date '2004-03-31', date '2004-01-01');

--Отчетный период 1 квартал 2004 НДС
insert into tax_period(id, tax_type, year) values (205, 'V', 2004);
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (205, 'первый квартал',  205, 1, date '2004-01-01', date '2004-03-31', date '2004-01-01');

--Связка подразделение Байкальский банк - период 1 квартал 2004 прибыль
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',4,'1','0',null,5);
--Связка подразделение Байкальский банк - период 1 квартал 2004 НДС
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',205,'1','0',null,105);
--Связка подразделение Волго-вятский банк - период 1 квартал 2004 НДС
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('8',205,'1','0',null,205);

-- НФ консолидированная РНУ-5 в периоде 1 квартал 2004 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (8,5,1,2,null,5,null,0,0,    0,0,0,0);
-- НФ первичная РНУ-4 (январь) в периоде 1 квартал 2004 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (9,4,1,1,1,5,null,0,0,    0,0,0,0);
-- НФ первичная РНУ-4 (февраль) в периоде 1 квартал 2004 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (10,4,1,1,2,5,null,0,0,    0,0,0,0);
-- НФ первичная НДС-202 в периоде 1 квартал 2004 для Волго-вятского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (208,202,1,1,null,205,null,0,0,    0,0,0,0);
-- НФ первичная НДС-203 в периоде 1 квартал 2004 для Волго-вятского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (209,203,1,1,1,105,null,0,0,    0,0,0,0);
-- НФ первичная НДС-203 в периоде 1 квартал 2004 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (210,203,1,1,1,205,null,0,0,    0,0,0,0);

----------------test6-------------

--Отчетный период 1 квартал 2024
insert into tax_period(id, tax_type, year) values (5, 'I', 2024);
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (5, 'первый квартал',  5, 1, date '2024-01-01', date '2024-03-31', date '2024-01-01');

--Связка подразделение Байкальский банк - период 1 квартал 2024
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',5,'1','0',null,6);

-- НФ консолидированная РНУ-4 (январь) в периоде 1 квартал 2024 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (11,4,1,2,1,6,null,0,0,    0,0,0,0);
-- НФ первичная РНУ-1 в периоде 1 квартал 2024 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (12,1,1,1,null,6,null,0,0,    0,0,0,0);

----------------test7-------------

--Отчетный период 1 квартал 2025
insert into tax_period(id, tax_type, year) values (6, 'I', 2025);
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (6, 'первый квартал',  6, 1, date '2025-01-01', date '2025-03-31', date '2025-01-01');
--Отчетный период 1 квартал 2025 НДС
insert into tax_period(id, tax_type, year) values (1005, 'V', 2025);
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (1005, 'первый квартал',  1005, 1, date '2025-01-01', date '2025-03-31', date '2025-01-01');

--Связка подразделение Байкальский банк - период 1 квартал 2025
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',6,'1','0',null,7);
--Связка подразделение Байкальский банк - период 1 квартал 2024 НДС
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',1005,'1','0',null,1006);

-- НФ консолидированная РНУ-4 (январь) в периоде 1 квартал 2025 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (13,4,1,2,1,7,null,0,0,    0,0,0,0);
-- НФ первичная РНУ-1 в периоде 1 квартал 2025 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (14,1,1,1,null,7,null,0,0,    0,0,0,0);
-- НФ первичная РНУ-4 (январь) в периоде 1 квартал 2025 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (15,4,1,1,1,7,null,0,0,    0,0,0,0);
-- НФ первичная НДС-200 в периоде 1 квартал 2024 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (1012,200,1,1,null,1006,null,0,0,    0,0,0,0);

----------------test8------------

--Отчетный период 1 квартал 2005
insert into tax_period(id, tax_type, year) values (7, 'I', 2005);
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (7, 'первый квартал',  7, 1, date '2005-01-01', date '2005-03-31', date '2005-01-01');

--Связка подразделение Байкальский банк - период 1 квартал 2005
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',7,'0','0',null,8);
--Связка подразделение Байкальский банк - период 1 квартал 2005 (дата корректировки 08.01.2005)
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',7,'0','0',date '2005-01-08',9);
--Связка подразделение Байкальский банк - период 1 квартал 2005 (дата корректировки 10.01.2005)
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',7,'1','0',date '2005-01-10',10);

-- НФ консолидированная РНУ-1 в периоде 1 квартал 2005 (дата корректировки 10.01.2005) для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (16,1,1,2,null,10,null,0,0,    0,0,0,0);
-- НФ первичная РНУ-1 в периоде 1 квартал 2005 для Байкальского банка (дата корректировки 08.01.2005)
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (17,1,1,1,null,9,null,0,0,    0,0,0,0);
-- НФ первичная РНУ-77 в периоде 1 квартал 2005 для Байкальского банка (дата корректировки 08.01.2005) за январь
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (117,77,1,1,1,9,null,0,0,    0,0,0,0);

----------------test8_2------------

--Отчетный период 1 квартал 2005 НДС
insert into tax_period(id, tax_type, year) values (1007, 'V', 2005);
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (1007, 'первый квартал',  1007, 1, date '2005-01-01', date '2005-03-31', date '2005-01-01');

--Связка подразделение Байкальский банк - период 1 квартал 2005
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',1007,'0','0',null,1008);
--Связка подразделение Байкальский банк - период 1 квартал 2005 (дата корректировки 08.01.2005)
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',1007,'0','0',date '2005-01-08',1009);
--Связка подразделение Байкальский банк - период 1 квартал 2005 (дата корректировки 10.01.2005)
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',1007,'1','0',date '2005-01-10',1010);

-- НФ консолидированная РНУ-5 в периоде 1 квартал 2005 (дата корректировки 10.01.2005) для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (1016,5,1,2,null,10,null,0,0,    0,0,0,0);
-- НФ первичная НДС-200 в периоде 1 квартал 2005 для Байкальского банка (дата корректировки 08.01.2005)
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (1017,200,1,1,null,1009,null,0,0,    0,0,0,0);
-- НФ первичная НДС-201 в периоде 1 квартал 2005 для Байкальского банка (дата корректировки 08.01.2005) за январь
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (1018,201,1,1,1,1009,null,0,0,    0,0,0,0);

----------------test9------------

--Отчетный период 1 квартал 2006
insert into tax_period(id, tax_type, year) values (8, 'I', 2006);
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (8, 'первый квартал',  8, 1, date '2006-01-01', date '2006-03-31', date '2006-01-01');

--Связка подразделение Байкальский банк - период 1 квартал 2006
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',8,'0','0',null,11);
--Связка подразделение Байкальский банк - период 1 квартал 2006 (дата корректировки 10.01.2006)
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',8,'0','0',date '2006-01-10',12);
--Связка подразделение Байкальский банк - период 1 квартал 2006 (дата корректировки 15.01.2006)
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',8,'1','0',date '2006-01-15',13);

-- НФ консолидированная РНУ-1 в периоде 1 квартал 2006 (дата корректировки 10.01.2006) для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (18,1,1,2,null,12,null,0,0,    0,0,0,0);
-- НФ первичная РНУ-1 в периоде 1 квартал 2006 (дата корректировки 15.01.2006) для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (19,1,1,1,null,13,null,0,0,    0,0,0,0);

----------------test9_2------------

--Отчетный период 1 квартал 2006 НДС
insert into tax_period(id, tax_type, year) values (1008, 'V', 2006);
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (1008, 'первый квартал',  1008, 1, date '2006-01-01', date '2006-03-31', date '2006-01-01');

--Связка подразделение Байкальский банк - период 1 квартал 2006
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',1008,'0','0',null,1011);
--Связка подразделение Байкальский банк - период 1 квартал 2006 (дата корректировки 10.01.2006)
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',1008,'0','0',date '2006-01-10',1012);
--Связка подразделение Байкальский банк - период 1 квартал 2006 (дата корректировки 15.01.2006)
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',1008,'1','0',date '2006-01-15',1013);
--Связка подразделение Байкальский банк - период 1 квартал 2006 (дата корректировки 05.01.2006)
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',1008,'0','0',date '2006-01-05',1014);

-- НФ консолидированная РНУ-5 в периоде 1 квартал 2006 (дата корректировки 10.01.2005) для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (1019,5,1,2,null,12,null,0,0,    0,0,0,0);
-- НФ первичная НДС-200 в периоде 1 квартал 2006 (дата корректировки 15.01.2006) для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (1020,200,1,1,null,1013,null,0,0,    0,0,0,0);
-- НФ первичная НДС-201 в периоде 1 квартал 2006 (дата корректировки 05.01.2006) для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (1021,201,1,1,1,1014,null,0,0,    0,0,0,0);
-- НФ первичная НДС-201 в периоде 1 квартал 2006 (дата корректировки 05.01.2006) для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (1022,201,1,1,1,1011,null,0,0,    0,0,0,0);

----------------test10------------

--Отчетный период 1 квартал 2007 - прибыль
insert into tax_period(id, tax_type, year) values (9, 'I', 2007);
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (9, 'первый квартал',  9, 1, date '2007-01-01', date '2007-03-31', date '2007-01-01');

--Отчетный период 1 квартал 2007 - ндс
insert into tax_period(id, tax_type, year) values (109, 'V', 2007);
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (109, 'первый квартал',  109, 1, date '2007-01-01', date '2007-03-31', date '2007-01-01');

--Связка подразделение Байкальский банк - период 1 квартал 2007
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',9,'0','0',null,14);
--Связка подразделение Байкальский банк - период 1 квартал 2007 (дата корректировки 10.01.2007)
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',9,'0','0',date '2007-01-10',15);
--Связка подразделение Байкальский банк - период 1 квартал 2007 (дата корректировки 09.01.2007)
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',9,'1','0',date '2007-01-09',16);

-- НФ консолидированная РНУ-1 в периоде 1 квартал 2007 (дата корректировки 10.01.2007) для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (20,1,1,2,null,15,null,0,0,    0,0,0,0);
-- НФ первичная РНУ-1 в периоде 1 квартал 2007 (дата корректировки 10.01.2007) для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (21,1,1,1,null,15,null,0,0,    0,0,0,0);
-- НФ первичная РНУ-1 в периоде 1 квартал 2007 (дата корректировки 09.01.2007) для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (22,1,1,1,null,16,null,0,0,    0,0,0,0);

----------------test11------------

--Отчетный период 1 квартал 2008
insert into tax_period(id, tax_type, year) values (10, 'I', 2008);
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (10, 'первый квартал',  10, 1, date '2008-01-01', date '2008-03-31', date '2008-01-01');

--Связка подразделение Байкальский банк - период 1 квартал 2008
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',10,'0','0',null,17);
--Связка подразделение Байкальский банк - период 1 квартал 2008 (дата корректировки 09.01.2008)
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',10,'1','0',date '2008-01-09',18);

-- НФ консолидированная РНУ-1 в периоде 1 квартал 2008 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (23,1,1,2,null,17,null,0,0,    0,0,0,0);
-- НФ первичная РНУ-1 в периоде 1 квартал 2008 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (24,1,1,1,null,17,null,0,0,    0,0,0,0);
-- НФ первичная РНУ-1 в периоде 1 квартал 2008 (дата корректировки 09.01.2008) для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (25,1,1,1,null,18,null,0,0,    0,0,0,0);
-- НФ первичная РНУ-2 в периоде 1 квартал 2008 (дата корректировки 09.01.2008) для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (225,2,1,1,null,18,null,0,0,    0,0,0,0);

----------------test45------------

--Отчетный период 1 квартал 2088
insert into tax_period(id, tax_type, year) values (100, 'I', 2088);
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (100, 'первый квартал',  100, 1, date '2088-01-01', date '2088-03-31', date '2088-01-01');

--Связка подразделение Байкальский банк - период 1 квартал 2088
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',100,'0','0',null,117);
--Связка подразделение Байкальский банк - период 1 квартал 2088 (дата корректировки 09.01.2088)
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',100,'1','0',date '2088-01-09',118);

-- НФ консолидированная РНУ-1 в периоде 1 квартал 2088 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (223,1,1,2,null,117,null,0,0,    0,0,0,0);
-- НФ первичная РНУ-1 в периоде 1 квартал 2088 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (224,1,1,1,null,118,null,0,0,    0,0,0,0);

----------------test12------------

--Отчетный период 1 квартал 2009
insert into tax_period(id, tax_type, year) values (11, 'I', 2009);
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (11, 'первый квартал',  11, 1, date '2009-01-01', date '2009-03-31', date '2009-01-01');

--Связка подразделение Байкальский банк - период 1 квартал 2009
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',11,'0','0',null,19);

-- НФ консолидированная РНУ-6 в периоде 1 квартал 2009 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (26,6,1,2,null,19,null,0,0,    0,0,0,0);
-- НФ первичная РНУ-7 в периоде 1 квартал 2009 для Байкальского банка и периодом сравнения 1 квартал 2009
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (27,7,1,1,null,19,19,0,0,    0,0,0,0);

----------------test13------------

--Отчетный период 3 квартал 2010
insert into tax_period(id, tax_type, year) values (12, 'I', 2010);
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (12, 'третий квартал',  12, 1, date '2010-07-01', date '2010-09-30', date '2010-07-01');

--Связка подразделение Байкальский банк - период 3 квартал 2010
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',12,'0','0',null,20);

-- НФ консолидированная РНУ-6 в периоде 3 квартал 2010 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (28,6,1,2,null,20,null,0,0,    0,0,0,0);
-- НФ первичная РНУ-7 в периоде 3 квартал 2010 для Байкальского банка и периодом сравнения 3 квартал 2010  + признак нарастающео итога
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (29,7,1,1,null,20,20,1,0,    0,0,0,0);

----------------test14------------

--Отчетный период 3 квартал 2011
insert into tax_period(id, tax_type, year) values (13, 'I', 2011);
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (13, 'третий квартал',  13, 1, date '2011-07-01', date '2011-09-30', date '2011-07-01');

--Отчетный период 3 квартал 2011 НДС
insert into tax_period(id, tax_type, year) values (1013, 'V', 2011);
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (1013, 'третий квартал',  1013, 1, date '2011-07-01', date '2011-09-30', date '2011-07-01');
--Отчетный период 1 квартал 2008 НДС
insert into tax_period(id, tax_type, year) values (1014, 'V', 2008);
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (1014, 'первый квартал',  1014, 1, date '2008-01-01', date '2008-03-31', date '2008-01-01');
--Отчетный период 1 квартал 2009 НДС
insert into tax_period(id, tax_type, year) values (1015, 'V', 2009);
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (1015, 'первый квартал',  1015, 1, date '2009-01-01', date '2009-03-31', date '2009-01-01');

--Связка подразделение Байкальский банк - период 3 квартал 2011
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',13,'0','0',null,21);
--Связка подразделение Волго-вятский банк - период 3 квартал 2011
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('8',1013,'0','0',null,1021);
--Связка подразделение Волго-вятский банк - период 1 квартал 2008 НДС
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('8',1014,'0','0',null,1022);
--Связка подразделение Волго-вятский банк - период 1 квартал 2009 НДС
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('8',1015,'0','0',null,1023);

-- НФ консолидированная РНУ-8 в периоде 1 квартал 2011 для Байкальского банка  и периодом сравнения 3 квартал 2010
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (30,8,1,2,null,21,20,0,0,    0,0,0,0);
-- НФ первичная РНУ-7 в периоде 3 квартал 2011 для Байкальского банка и периодом сравнения 1 квартал 2009
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (31,7,1,1,null,21,19,0,0,    0,0,0,0);
-- НФ первичная РНУ-7 в периоде 3 квартал 2011 для Байкальского банка и периодом сравнения 1 квартал 2008
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (32,7,1,1,null,21,17,0,0,    0,0,0,0);
-- НФ первичная НДС-204 в периоде 3 квартал 2011 для Волго-вятского банка и периодом сравнения 1 квартал 2009
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (1031,204,1,1,null,1021,1023,0,0,    0,0,0,0);
-- НФ первичная НДС-204 в периоде 3 квартал 2011 для Волго-вятского банка и периодом сравнения 1 квартал 2008
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (1032,204,1,1,null,1021,1022,0,0,    0,0,0,0);

----------------test15------------

--Отчетный период 3 квартал 2012
insert into tax_period(id, tax_type, year) values (14, 'I', 2012);
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (14, 'третий квартал',  14, 1, date '2012-07-01', date '2012-09-30', date '2012-07-01');
--Отчетный период 3 квартал 2012 НДС
insert into tax_period(id, tax_type, year) values (1016, 'V', 2012);
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (1016, 'третий квартал',  1016, 1, date '2012-07-01', date '2012-09-30', date '2012-07-01');
--Отчетный период 1 квартал 2010 НДС
insert into tax_period(id, tax_type, year) values (1017, 'V', 2010);
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (1017, 'третий квартал',  1017, 1, date '2010-01-01', date '2010-03-31', date '2010-01-01');

--Связка подразделение Байкальский банк - период 3 квартал 2012
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',14,'0','0',null,22);
--Связка подразделение Волго-вятский банк - период 3 квартал 2012
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('8',1016,'0','0',null,1024);
--Связка подразделение Волго-вятский банк - период 3 квартал 2010 НДС
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('8',1017,'0','0',null,1025);

-- НФ консолидированная РНУ-8 в периоде 3 квартал 2012 для Байкальского банка  и периодом сравнения 3 квартал 2012 и признаком нарастающего итога
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (33,8,1,2,null,22,22,1,0,    0,0,0,0);
-- НФ первичная РНУ-7 в периоде 3 квартал 2012 для Байкальского банка и периодом сравнения 3 квартал 2012 и признаком нарастающего итога
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (34,7,1,1,null,22,22,1,0,    0,0,0,0);
-- НФ первичная РНУ-7 в периоде 3 квартал 2012 для Байкальского банка и периодом сравнения 3 квартал 2010 и признаком нарастающего итога
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (35,7,1,1,null,22,20,1,0,    0,0,0,0);

-- НФ первичная НДС-204 в периоде 3 квартал 2012 для Волго-вятского банка и периодом сравнения 3 квартал 2012
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (1034,204,1,1,null,1024,1024,0,0,    0,0,0,0);
-- НФ первичная НДС-205 в периоде 3 квартал 2012 для Байкальского банка и периодом сравнения 3 квартал 2012 и признаком нарастающего итога
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (1035,205,1,1,null,1024,1024,1,0,    0,0,0,0);
-- НФ первичная НДС-205 в периоде 3 квартал 2012 для Байкальского банка и периодом сравнения 3 квартал 2010 и признаком нарастающего итога
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (1036,205,1,1,null,1024,1025,1,0,    0,0,0,0);

----------------test30------------

--Отчетный период 3 квартал 2026
insert into tax_period(id, tax_type, year) values (114, 'I', 2026);
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
values (114, 'третий квартал',  114, 1, date '2026-07-01', date '2026-09-30', date '2026-07-01');

--Связка подразделение Байкальский банк - период 3 квартал 2026
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',114,'0','0',null,122);
--Связка подразделение Байкальский банк - период 3 квартал 2026 и датой корректировки (10.01.2026)
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',114,'0','0',date '2026-01-10',123);
--Связка подразделение Байкальский банк - период 3 квартал 2026 и датой корректировки (15.01.2026)
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',114,'0','0',date '2026-01-15',124);

-- НФ консолидированная РНУ-88 в периоде 3 квартал 2026 для Байкальского банка  и периодом сравнения 3 квартал 2026 и признаком нарастающего итога
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (133,88,1,2,null,122,122,1,0,    0,0,0,0);
-- НФ первичная РНУ-89 в периоде 3 квартал 2026 для Байкальского банка и датой корректировки (10.01.2026)
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (134,89,1,1,null,123,null,0,0,    0,0,0,0);
-- НФ первичная РНУ-7 в периоде 3 квартал 2026 для Байкальского банка и датой корректировки (15.01.2026)
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (135,89,1,1,null,124,null,0,0,    0,0,0,0);


----------------test52------------

--Связка подразделение Волго-вятский банк - период 3 квартал 2026
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('8',114,'0','0',null,33);

-- НФ консолидированная РНУ-100 в периоде 3 квартал 2026 для Байкальского банка и периодом сравнения 3 квартал 2026
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (78,100,1,2,null,122,122,0,0,    0,0,0,0);
-- НФ первичная РНУ-100 в периоде 3 квартал 2026 для Байкальского банка и периодом сравнения 3 квартал 2026
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (79,100,1,1,null,122,122,0,0,    0,0,0,0);
-- НФ первичная РНУ-100 в периоде 3 квартал 2026 для Волго-вятского банка и периодом сравнения 3 квартал 2026
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (80,100,1,1,null,33,33,0,0,    0,0,0,0);


--------------------------- НФ-источники декларация ----------------------------

----------------test16-test17------------

-- Декларация Д-1 в периоде 1 квартал 2000 для Байкальского банка
Insert into declaration_data (ID,DECLARATION_TEMPLATE_ID,IS_ACCEPTED,TAX_ORGAN_CODE,KPP,DEPARTMENT_REPORT_PERIOD_ID) values (1,1,'1','111','222',1);
-- НФ консолидированная РНУ-1 уже создана в test1

----------------test18------------

-- Декларация Д-2 в периоде 1 квартал 2002 для Байкальского банка
Insert into declaration_data (ID,DECLARATION_TEMPLATE_ID,IS_ACCEPTED,TAX_ORGAN_CODE,KPP,DEPARTMENT_REPORT_PERIOD_ID) values (2,2,'1',null,null,3);
-- НФ консолидированная РНУ-1 уже создана в test3
-- НФ консолидированная РНУ-1 в периоде 1 квартал 2002 для Волго-Вятского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (37,1,1,2,null,2,null,0,0,    0,0,0,0);

----------------test19------------

-- Декларация Д-1 в периоде 1 квартал 2003 для Байкальского банка
Insert into declaration_data (ID,DECLARATION_TEMPLATE_ID,IS_ACCEPTED,TAX_ORGAN_CODE,KPP,DEPARTMENT_REPORT_PERIOD_ID) values (3,1,'1',null,null,4);
-- НФ консолидированная РНУ-1 уже создана в test4
-- НФ консолидированная РНУ-3 в периоде 1 квартал 2003 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (38,3,1,2,null,4,null,0,0,    0,0,0,0);

----------------test20------------

-- Декларация Д-3 в периоде 1 квартал 2004 для Байкальского банка
Insert into declaration_data (ID,DECLARATION_TEMPLATE_ID,IS_ACCEPTED,TAX_ORGAN_CODE,KPP,DEPARTMENT_REPORT_PERIOD_ID) values (4,3,'1',null,null,5);
-- НФ первичная РНУ-4 (январь) уже создана в test5
-- НФ первичная РНУ-4 (февраль) уже создана в test5
-- НФ консолидированная РНУ-5 уже создана в test5

----------------test21------------
--Связка подразделение Байкальский банк - период 1 квартал 2005 (дата корректировки 15.01.2005)
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',7,'1','0',date '2005-01-15',23);

-- Декларация Д-1 в периоде 1 квартал 2005 (с датой корректировки 15.01.2005) для Байкальского банка
Insert into declaration_data (ID,DECLARATION_TEMPLATE_ID,IS_ACCEPTED,TAX_ORGAN_CODE,KPP,DEPARTMENT_REPORT_PERIOD_ID) values (5,1,'1',null,null,23);
-- НФ консолидированная РНУ-1 за 1 квартал 2005 (дата корректировки 10.01.2005) уже создана в test8

----------------test22------------
--Связка подразделение Байкальский банк - период 1 квартал 2006 (дата корректировки 05.01.2006)
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',8,'1','0',date '2006-01-05',24);

-- Декларация Д-1 в периоде 1 квартал 2006 (с датой корректировки 05.01.2006) для Байкальского банка
Insert into declaration_data (ID,DECLARATION_TEMPLATE_ID,IS_ACCEPTED,TAX_ORGAN_CODE,KPP,DEPARTMENT_REPORT_PERIOD_ID) values (6,1,'1',null,null,24);
-- НФ консолидированная РНУ-1 за 1 квартал 2005 (дата корректировки 10.01.2006) уже создана в test9

----------------test23------------
-- Декларация Д-1 в периоде 1 квартал 2007 (с датой корректировки 10.01.2007) для Байкальского банка
Insert into declaration_data (ID,DECLARATION_TEMPLATE_ID,IS_ACCEPTED,TAX_ORGAN_CODE,KPP,DEPARTMENT_REPORT_PERIOD_ID) values (7,1,'1',null,null,15);
-- НФ консолидированная РНУ-1 за 1 квартал 2007 (дата корректировки 10.01.2007) уже создана в test10
-- НФ консолидированная РНУ-1 в периоде 1 квартал 2007 (дата корректировки 09.01.2007) для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (39,1,1,2,null,16,null,0,0,    0,0,0,0);

----------------test24------------
-- Декларация Д-1 в периоде 1 квартал 2008 для Байкальского банка
Insert into declaration_data (ID,DECLARATION_TEMPLATE_ID,IS_ACCEPTED,TAX_ORGAN_CODE,KPP,DEPARTMENT_REPORT_PERIOD_ID) values (8,1,'1',null,null,17);
-- НФ консолидированная РНУ-1 в периоде 1 квартал 2008 для Байкальского банка в test11
-- НФ консолидированная РНУ-1 в периоде 1 квартал 2008 (дата корректировки 09.01.2008) для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (40,1,1,2,null,18,null,0,0,    0,0,0,0);

----------------test44------------
--Связка подразделение Байкальский банк - период 1 квартал 2009 (дата корректировки 09.01.2009)
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',11,'0','0',date '2009-01-09',30);

-- Декларация Д-1 в периоде 1 квартал 2009 для Байкальского банка
Insert into declaration_data (ID,DECLARATION_TEMPLATE_ID,IS_ACCEPTED,TAX_ORGAN_CODE,KPP,DEPARTMENT_REPORT_PERIOD_ID) values (9,1,'1',null,null,19);
-- НФ консолидированная РНУ-1 в периоде 1 квартал 2009 (дата корректировки 09.01.2009) для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (75,1,1,2,null,30,null,0,0,    0,0,0,0);


--------------------------- НФ-приемники НФ ----------------------------

----------------test27------------
-- НФ первичная РНУ-10 в периоде 1 квартал 2002 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (41,10,1,1,null,3,null,0,0,    0,0,0,0);
-- НФ консолидированная РНУ-2 в периоде 1 квартал 2002 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (42,2,1,2,null,3,null,0,0,    0,0,0,0);
-- НФ консолидированная РНУ-9 в периоде 1 квартал 2002 для Волго-Вятского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (43,9,1,2,null,2,null,0,0,    0,0,0,0);

----------------test29------------
-- НФ первичная РНУ-11 в периоде 1 квартал 2004 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (44,11,1,1,null,5,null,0,0,    0,0,0,0);
-- НФ консолидированная РНУ-2 в периоде 1 квартал 2004 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (45,2,1,2,null,5,null,0,0,    0,0,0,0);
-- НФ консолидированная РНУ-4 (январь) в периоде 1 квартал 2004 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (46,4,1,2,1,5,null,0,0,    0,0,0,0);
-- НФ консолидированная РНУ-4 (февраль) в периоде 1 квартал 2004 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (47,4,1,2,2,5,null,0,0,    0,0,0,0);

----------------test31------------
-- НФ первичная РНУ-4 (январь) в периоде 1 квартал 2025 для Байкальского банка уже создана в test7 (id = 15)
-- НФ консолидированная РНУ-4 (январь) в периоде 1 квартал 2025 для Байкальского банка  уже создана в test7 (id = 13)
-- НФ консолидированная РНУ-5 в периоде 1 квартал 2025 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (48,5,1,2,null,7,null,0,0,    0,0,0,0);

----------------test32------------

-- НФ первичная РНУ-12 в периоде 1 квартал 2005 (дата корректировки 10.01.2005) для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (49,12,1,1,null,10,null,0,0,    0,0,0,0);
-- НФ консолидированная РНУ-13 в периоде 1 квартал 2005 (дата корректировки 10.01.2005) для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (50,13,1,2,null,10,null,0,0,    0,0,0,0);

----------------test33------------

-- НФ первичная РНУ-12 в периоде 1 квартал 2006 (дата корректировки 10.01.2006) для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (51,12,1,1,null,12,null,0,0,    0,0,0,0);
-- НФ консолидированная РНУ-13 в периоде 1 квартал 2006 (дата корректировки 15.01.2006) для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (52,13,1,2,null,13,null,0,0,    0,0,0,0);

----------------test34------------

-- НФ первичная РНУ-12 в периоде 1 квартал 2007 (дата корректировки 10.01.2007) для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (53,12,1,1,null,15,null,0,0,    0,0,0,0);
-- НФ консолидированная РНУ-13 в периоде 1 квартал 2007 (дата корректировки 09.01.2007) для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (54,13,1,2,null,16,null,0,0,    0,0,0,0);

----------------test35------------

--Связка подразделение Байкальский банк - период 1 квартал 2008 (дата корректировки 05.01.2008)
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',10,'1','0',date '2008-01-05',25);
--Связка подразделение Байкальский банк - период 1 квартал 2008 (дата корректировки 10.01.2008)
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',10,'1','0',date '2008-01-10',26);
--Связка подразделение Байкальский банк - период 1 квартал 2008 (дата корректировки 15.01.2008)
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',10,'1','0',date '2008-01-15',27);
--Связка подразделение Волго-вятски банк - период 1 квартал 2008
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('8',10,'1','0',null,28);
--Связка подразделение Волго-вятский банк - период 1 квартал 2008 (дата корректировки 15.01.2008)
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('8',10,'1','0',date '2008-01-15',29);


-- НФ первичная РНУ-12 в периоде 1 квартал 2008 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (55,12,1,1,null,17,null,0,0,    0,0,0,0);
-- НФ первичная РНУ-12 в периоде 1 квартал 2008 (дата корректировки 15.01.2008) для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (56,12,1,1,null,27,null,0,0,    0,0,0,0);
-- НФ консолидированная РНУ-13 в периоде 1 квартал 2008 (дата корректировки 05.01.2008) для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (57,13,1,2,null,25,null,0,0,    0,0,0,0);
-- НФ консолидированная РНУ-13 в периоде 1 квартал 2008 (дата корректировки 10.01.2008) для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (58,13,1,2,null,26,null,0,0,    0,0,0,0);
-- НФ консолидированная РНУ-13 в периоде 1 квартал 2008 (дата корректировки 15.01.2008) для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (59,13,1,2,null,27,null,0,0,    0,0,0,0);
-- НФ консолидированная РНУ-13 в периоде 1 квартал 2008 (дата корректировки 15.01.2008) для Волго-вятского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (60,13,1,2,null,29,null,0,0,    0,0,0,0);
-- НФ первичная РНУ-33 в периоде 1 квартал 2008 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (61,33,1,2,null,17,null,0,0,    0,0,0,0);

----------------test36------------

-- НФ первичная РНУ-14 в периоде 1 квартал 2009 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (62,14,1,1,null,19,null,0,0,    0,0,0,0);
-- НФ консолидированная РНУ-15 в периоде 1 квартал 2009 для Байкальского банка и периодом сравнения 1 квартал 2009
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (63,15,1,2,null,19,19,0,0,    0,0,0,0);

----------------test37------------

-- НФ первичная РНУ-14 в периоде 3 квартал 2010 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (64,14,1,1,null,20,null,0,0,    0,0,0,0);
-- НФ консолидированная РНУ-15 в периоде 3 квартал 2010 для Байкальского банка и периодом сравнения 3 квартал 2010  + признак нарастающео итога
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (65,15,1,2,null,20,20,1,0,    0,0,0,0);

----------------test38------------

-- НФ первичная РНУ-16 в периоде 3 квартал 2011 для Байкальского банка и периодом сравнения 3 квартал 2010
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (66,16,1,1,null,21,20,0,0,    0,0,0,0);
-- НФ консолидированная РНУ-15 в периоде 1 квартал 2011 для Байкальского банка  и периодом сравнения 1 квартал 2009
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (67,15,1,2,null,21,19,0,0,    0,0,0,0);
-- НФ консолидированная РНУ-15 в периоде 1 квартал 2011 для Байкальского банка  и периодом сравнения 1 квартал 2008
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (68,15,1,2,null,21,17,0,0,    0,0,0,0);

----------------test39------------

-- НФ первичная РНУ-16 в периоде 3 квартал 2012 для Байкальского банка и периодом сравнения 3 квартал 2012 и признаком нарастающего итога
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (69,16,1,1,null,22,22,1,0,    0,0,0,0);
-- НФ консолидированная РНУ-15 в периоде 3 квартал 2012 для Байкальского банка  и периодом сравнения 3 квартал 2012 и признаком нарастающего итога
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (70,15,1,2,null,22,22,1,0,    0,0,0,0);
-- НФ консолидированная РНУ-15 в периоде 3 квартал 2012 для Байкальского банка и периодом сравнения 3 квартал 2010 и признаком нарастающего итога
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (71,15,1,2,null,22,20,1,0,    0,0,0,0);

----------------test40------------

-- НФ первичная РНУ-17 в периоде 3 квартал 2026 для Байкальского банка  и периодом сравнения 3 квартал 2026 и признаком нарастающего итога
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (72,17,1,1,null,122,122,1,0,    0,0,0,0);
-- НФ первичная РНУ-13 в периоде 3 квартал 2026 для Байкальского банка и датой корректировки (10.01.2026)
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (73,13,1,2,null,123,null,0,0,    0,0,0,0);
-- НФ первичная РНУ-13 в периоде 3 квартал 2026 для Байкальского банка и датой корректировки (15.01.2026)
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (74,13,1,2,null,124,null,0,0,    0,0,0,0);

----------------test53------------

-- НФ первичная РНУ-19 в периоде 1 квартал 2008 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (124,19,1,1,null,17,null,0,0,    0,0,0,0);
-- НФ первичная РНУ-19 в периоде 1 квартал 2008 (дата корректировки 09.01.2008) для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (125,19,1,1,null,18,null,0,0,    0,0,0,0);
-- НФ консолидированная РНУ-20 в периоде 1 квартал 2008 (дата корректировки 05.01.2008) для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (126,20,1,2,null,25,null,0,0,    0,0,0,0);
-- НФ консолидированная РНУ-20 в периоде 1 квартал 2008 для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (127,20,1,2,null,17,null,0,0,    0,0,0,0);


--------------------------- Декларации-приемники НФ ----------------------------


----------------test46------------

--Связка подразделение Волго-вятский банк - период 1 квартал 2009
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('8',11,'0','0',null,31);

-- НФ консолидированная РНУ-18 в периоде 1 квартал 2009
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (76,18,1,2,null,19,null,0,0,    0,0,0,0);
-- Декларация Д-1 в периоде 1 квартал 2009 для Байкальского банка уже создана в test44
-- Декларация Д-1 в периоде 1 квартал 2009 для Волго-вятского банка
Insert into declaration_data (ID,DECLARATION_TEMPLATE_ID,IS_ACCEPTED,TAX_ORGAN_CODE,KPP,DEPARTMENT_REPORT_PERIOD_ID) values (10,1,'1',null,null,31);

----------------test49------------
--Связка подразделение Байкальский банк - период 1 квартал 2005 (дата корректировки 05.01.2005)
Insert into department_report_period (DEPARTMENT_ID,REPORT_PERIOD_ID,IS_ACTIVE,IS_BALANCE_PERIOD,CORRECTION_DATE,ID) values ('4',7,'0','0',date '2005-01-05',32);

-- НФ консолидированная РНУ-1 в периоде 1 квартал 2005 (с датой корректировки 10.01.2005) для Байкальского банка
Insert into FORM_DATA (ID,FORM_TEMPLATE_ID,STATE,KIND,PERIOD_ORDER,DEPARTMENT_REPORT_PERIOD_ID,COMPARATIVE_DEP_REP_PER_ID,ACCRUING,MANUAL,  RETURN_SIGN,SORTED,EDITED,SORTED_BACKUP)
values (77,1,1,2,null,10,null,0,0,    0,0,0,0);
-- Декларация Д-1 в периоде 1 квартал 2005 (с датой корректировки 15.01.2005) для Байкальского банка уже создана в test21
-- Декларация Д-1 в периоде 1 квартал 2005 (с датой корректировки 05.01.2005) для Байкальского банка
Insert into declaration_data (ID,DECLARATION_TEMPLATE_ID,IS_ACCEPTED,TAX_ORGAN_CODE,KPP,DEPARTMENT_REPORT_PERIOD_ID) values (11,1,'1',null,null,32);
-- Декларация Д-2 в периоде 1 квартал 2005 для Байкальского банка
Insert into declaration_data (ID,DECLARATION_TEMPLATE_ID,IS_ACCEPTED,TAX_ORGAN_CODE,KPP,DEPARTMENT_REPORT_PERIOD_ID) values (12,1,'1',null,null,8);
-- Декларация Д-2 в периоде 1 квартал 2005 (с датой корректировки 15.01.2005) для Байкальского банка
Insert into declaration_data (ID,DECLARATION_TEMPLATE_ID,IS_ACCEPTED,TAX_ORGAN_CODE,KPP,DEPARTMENT_REPORT_PERIOD_ID) values (13,2,'1',null,null,23);

----------------test52------------
-- НФ консолидированная РНУ-1 в периоде 1 квартал 2008 для Байкальского банка в test11 (id=23)
-- НФ консолидированная РНУ-1 в периоде 1 квартал 2008 (дата корректировки 09.01.2008) для Байкальского банка  уже создана в test24 (id=40)
-- Декларация Д-1 в периоде 1 квартал 2008 (дата корректировки 05.01.2008) для Байкальского банка
Insert into declaration_data (ID,DECLARATION_TEMPLATE_ID,IS_ACCEPTED,TAX_ORGAN_CODE,KPP,DEPARTMENT_REPORT_PERIOD_ID) values (14,1,'1',null,null,25);
-- Декларация Д-1 в периоде 1 квартал 2008 для Байкальского банка уже создана в test24