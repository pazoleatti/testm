--SET DATABASE REFERENTIAL INTEGRITY FALSE;

--------------------------------------------------------
--  Типы документов
--------------------------------------------------------
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (360,'Коды документов',1,0,0,null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3601, 360,'Код','CODE',1,1,null,null,1,null,6,1,1,null,null,0,2);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3602, 360,'Наименование документа','NAME',1,2,null,null,1,null,50,1,0,null,null,0,2000);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3603, 360,'Приоритет','PRIORITY',2,3,null,null,1,0,6,1,0,null,null,0,2);
--------------------------------------------------------
--  Страны мира
--------------------------------------------------------
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (10,'ОК 025-2001 (Общероссийский классификатор стран мира)',1,0,0,null);
-- attr
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (50,10,'Код','CODE',1,0,null,null,1,null,3,1,1,null,null,0,3);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (51,10,'Код (2-х букв.)','CODE_2',1,1,null,null,1,null,10,1,2,null,null,0,2);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (52,10,'Код (3-х букв.)','CODE_3',1,2,null,null,1,null,10,1,3,null,null,0,3);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (53,10,'Краткое наименование','NAME',1,3,null,null,1,null,30,1,4,null,null,0,500);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (54,10,'Полное наименование','FULLNAME',1,4,null,null,1,null,50,1,5,null,null,0,500);
-- values
Insert into REF_BOOK_RECORD (ID,RECORD_ID,REF_BOOK_ID,VERSION,STATUS) values ('262254399','284','10',to_date('01.01.12','DD.MM.YY'),'0');
Insert into REF_BOOK_RECORD (ID,RECORD_ID,REF_BOOK_ID,VERSION,STATUS) values ('262259899','339','10',to_date('01.01.12','DD.MM.YY'),'0');
Insert into REF_BOOK_RECORD (ID,RECORD_ID,REF_BOOK_ID,VERSION,STATUS) values ('262244299','183','10',to_date('01.01.12','DD.MM.YY'),'0');
Insert into REF_BOOK_VALUE (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('262244299','50','276',null,null,null);
Insert into REF_BOOK_VALUE (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('262244299','51','DE',null,null,null);
Insert into REF_BOOK_VALUE (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('262244299','52','DEU',null,null,null);
Insert into REF_BOOK_VALUE (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('262244299','53','ГЕРМАНИЯ',null,null,null);
Insert into REF_BOOK_VALUE (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('262244299','54','Федеративная Республика Германия',null,null,null);
Insert into REF_BOOK_VALUE (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('262254399','50','643',null,null,null);
Insert into REF_BOOK_VALUE (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('262254399','51','RU',null,null,null);
Insert into REF_BOOK_VALUE (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('262254399','52','RUS',null,null,null);
Insert into REF_BOOK_VALUE (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('262254399','53','РОССИЯ',null,null,null);
Insert into REF_BOOK_VALUE (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('262254399','54','Российская Федерация',null,null,null);
Insert into REF_BOOK_VALUE (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('262259899','50','840',null,null,null);
Insert into REF_BOOK_VALUE (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('262259899','51','US',null,null,null);
Insert into REF_BOOK_VALUE (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('262259899','52','USA',null,null,null);
Insert into REF_BOOK_VALUE (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('262259899','53','СОЕДИНЕННЫЕ ШТАТЫ',null,null,null);
Insert into REF_BOOK_VALUE (RECORD_ID,ATTRIBUTE_ID,STRING_VALUE,NUMBER_VALUE,DATE_VALUE,REFERENCE_VALUE) values ('262259899','54','Соединенные Штаты Америки',null,null,null);

--------------------------------------------------------
--  REF_BOOK_ASNU
--------------------------------------------------------
insert into ref_book (id,name,visible,type,read_only,region_attribute_id,table_name,is_versioned) values (900,'АСНУ',1,0,1,null,'REF_BOOK_ASNU',0);
insert into ref_book_attribute (id,ref_book_id,name,alias,type,ord,reference_id,attribute_id,visible,precision,width,required,is_unique,sort_order,format,read_only,max_length) values (9002,900,'Код АСНУ','CODE',1,1,null,null,1,null,5,1,0,null,null,0,4);
insert into ref_book_attribute (id,ref_book_id,name,alias,type,ord,reference_id,attribute_id,visible,precision,width,required,is_unique,sort_order,format,read_only,max_length) values (9003,900,'Наименование АСНУ','NAME',1,2,null,null,1,null,25,0,0,null,null,0,100);
insert into ref_book_attribute (id,ref_book_id,name,alias,type,ord,reference_id,attribute_id,visible,precision,width,required,is_unique,sort_order,format,read_only,max_length) values (9004,900,'Тип дохода','TYPE',1,3,null,null,1,null,50,0,0,null,null,0,255);

Insert into REF_BOOK_ASNU (ID,CODE,NAME,TYPE) values ('1','1000','АС "SAP"','Оплата труда сотрудников, оплата по договорам ГПХ, материальная выгода по кредитам, выданным сотрудникам');
Insert into REF_BOOK_ASNU (ID,CODE,NAME,TYPE) values ('2','2000','АИС "Дивиденд"','Сведения по доходам в виде дивидендов по акциям Сбербанка');
Insert into REF_BOOK_ASNU (ID,CODE,NAME,TYPE) values ('3','3000','АС "Diasoft Custody 5NT"','Операции с ценными бумагами, ФИСС по договорам брокерского обслуживания ');
Insert into REF_BOOK_ASNU (ID,CODE,NAME,TYPE) values ('4','4000','АС "Инфобанк"','Проценты по векселям, сберегательным сертификатам');
Insert into REF_BOOK_ASNU (ID,CODE,NAME,TYPE) values ('5','5000','АИС "Депозитарий"','Доходы по ценным бумагам, учитываемым на счетах депо депозитария, дивиденды по акциям, купоны по облигациям');
Insert into REF_BOOK_ASNU (ID,CODE,NAME,TYPE) values ('6','6000','Материальная выгода. Кредиты_АС "ЕКП"','Материальная выгода от экономии за пользование заемными средствами (2610)');
Insert into REF_BOOK_ASNU (ID,CODE,NAME,TYPE) values ('7','6001','Экономическая выгода. Кредиты_АС "ЕКП"','Экономическая выгода при списании с баланса ссудной задолженности по кредитным договорам (4800)');
Insert into REF_BOOK_ASNU (ID,CODE,NAME,TYPE) values ('8','7000','Экономическая выгода. Карты_ АС "ИПС БК"','Экономическая выгода при списании с баланса ссудной задолженности по кредитным картам (4800)');
Insert into REF_BOOK_ASNU (ID,CODE,NAME,TYPE) values ('9','6002','Экономическая выгода. Комиссии_АС "ЕКП"','Экономическая выгода при списании с баланса дебиторской задолженности по комиссионным доходам (4800)');
Insert into REF_BOOK_ASNU (ID,CODE,NAME,TYPE) values ('10','6003','Реструктуризация валютных кредитов_АС "ЕКП"','Реструктуризация валютных кредитов на льготных условиях (4800)');
Insert into REF_BOOK_ASNU (ID,CODE,NAME,TYPE) values ('11','6004','Прощение долга (амнистия). Кредиты_АС "ЕКП"','Прощение (амнистия) задолженности физических лиц (4800)');
Insert into REF_BOOK_ASNU (ID,CODE,NAME,TYPE) values ('12','6005','Выплаты клиентам по решениям суда_АС "ЕКП"','Выплаты клиентам по решениям суда (4800)');
Insert into REF_BOOK_ASNU (ID,CODE,NAME,TYPE) values ('13','1001','Призы, подарки клиентам_АС "SAP"','Призы, подарки клиентам (2740)');
Insert into REF_BOOK_ASNU (ID,CODE,NAME,TYPE) values ('14','8000','АС "Back Office" ','Проценты по вкладам');

--------------------------------------------------------
--  Физические лица и статусы налогоплательщиков
--------------------------------------------------------
insert into ref_book (id, name, visible, type, read_only, table_name, is_versioned) values (901, 'Адреса физических лиц', 0, 0, 0, 'REF_BOOK_ADDRESS', 0);
insert into ref_book (id, name, visible, type, read_only, table_name, is_versioned) values (902, 'Документы, удостоверяющие личность', 0, 0, 0, 'REF_BOOK_ID_DOC', 0);
insert into ref_book (id, name, visible, type, read_only, table_name, is_versioned) values (903, 'Статусы налогоплательщика', 1, 0, 0, 'REF_BOOK_TAXPAYER_STATE', 0);
insert into ref_book (id, name, visible, type, read_only, table_name, is_versioned) values (904, 'Физические лица', 1, 0, 0, 'REF_BOOK_PERSON', 1);
insert into ref_book (id, name, visible, type, read_only, table_name, is_versioned) values (905, 'Идентификаторы налогоплательщика', 0, 0, 0, 'REF_BOOK_ID_TAX_PAYER', 0);

insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9120, 901, 'Тип адреса', 'ADDRESS_TYPE', 2, 1, null, null, 1, 0, 15, 1, 0, null, null, 0, 1);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9111, 901, 'Код региона', 'REGION_CODE', 1, 3, null, null, 1, null, 15, 0, 0, null, null, 0, 2);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9112, 901, 'Индекс', 'POSTAL_CODE', 1, 4, null, null, 1, null, 15, 0, 0, null, null, 0, 6);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9113, 901, 'Район', 'DISTRICT', 1, 5, null, null, 1, null, 15, 0, 0, null, null, 0, 50);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9114, 901, 'Город', 'CITY', 1, 6, null, null, 1, null, 15, 0, 0, null, null, 0, 50);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9115, 901, 'Населенный пункт (село, поселок)', 'LOCALITY', 1, 7, null, null, 1, null, 15, 0, 0, null, null, 0, 50);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9116, 901, 'Улица (проспект, переулок)', 'STREET', 1, 8, null, null, 1, null, 15, 0, 0, null, null, 0, 50);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9117, 901, 'Номер дома (владения)', 'HOUSE', 1, 9, null, null, 1, null, 15, 0, 0, null, null, 0, 20);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9118, 901, 'Номер корпуса (строения)', 'BUILD', 1, 10, null, null, 1, null, 15, 0, 0, null, null, 0, 20);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9119, 901, 'Номер квартиры', 'APPARTMENT', 1, 11, null, null, 1, null, 15, 0, 0, null, null, 0, 20);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9110, 901, 'Страна', 'COUNTRY_ID', 4, 2, 10, 50, 1, null, 15, 0, 0, null, null, 0, null);

insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9021, 902, 'Код ДУЛ', 'DOC_ID', 4, 1, 360, 3601, 1, null, 15, 1, 0, null, null, 0, null);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9022, 902, 'Серия и номер ДУЛ', 'DOC_NUMBER', 1, 2, null, null, 1, null, 15, 1, 0, null, null, 0, 25);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9023, 902, 'Кем выдан  ДУЛ', 'ISSUED_BY', 1, 3, null, null, 1, null, 15, 0, 0, null, null, 0, 255);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9024, 902, 'Дата выдачи документа ДУЛ', 'ISSUED_DATE', 3, 4, null, null, 1, null, 15, 0, 0, null, 1, 0, null);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9025, 902, 'Включается в отчетность', 'INC_REP', 2, 5, null, null, 1, 0, 6, 0, 0, null, null, 0, 1);

insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9031, 903, 'Код', 'CODE', 1, 1, null, null, 1, null, 1, 1, 0, null, null, 0, 1);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9032, 903, 'Наименование', 'NAME', 1, 2, null, null, 1, null, 15, 1, 0, null, null, 0, 1000);

insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9059, 904, 'Идентификатор ФЛ', 'RECORD_ID', 2, 0, null, null, 1, 0, 18, 1, 1, null, null, 1, 18);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9041, 904, 'Фамилия', 'LAST_NAME', 1, 1, null, null, 1, null, 15, 1, 0, null, null, 0, 60);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9042, 904, 'Имя', 'FIRST_NAME', 1, 2, null, null, 1, null, 15, 1, 0, null, null, 0, 60);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9043, 904, 'Отчество', 'MIDDLE_NAME', 1, 3, null, null, 1, null, 15, 0, 0, null, null, 0, 60);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9044, 904, 'Пол', 'SEX', 2, 4, null, null, 1, 0, 15, 0, 0, null, null, 0, 1);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9045, 904, 'ИНН в Российской Федерации', 'INN', 1, 5, null, null, 1, null, 12, 0, 1, null, null, 0, 12);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9046, 904, 'ИНН в стране гражданства', 'INN_FOREIGN', 1, 6, null, null, 1, null, 15, 0, 1, null, null, 0, 50);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9047, 904, 'СНИЛС', 'SNILS', 1, 7, null, null, 1, null, 14, 0, 1, null, null, 0, 14);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9048, 904, 'Статус налогоплательщика ', 'TAXPAYER_STATE', 4, 8, 903, 9031, 0, null, 15, 1, 0, null, null, 0, null);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9050, 904, 'Дата рождения', 'BIRTH_DATE', 3, 10, null, null, 1, null, 15, 1, 0, null, 1, 0, null);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9051, 904, 'Место рождения', 'BIRTH_PLACE', 1, 11, null, null, 1, null, 15, 0, 0, null, null, 0, 255);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9052, 904, 'Гражданство', 'CITIZENSHIP', 4, 12, 10, 50, 1, null, 3, 1, 0, null, null, 0, null);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9054, 904, 'Адрес места жительства', 'ADDRESS', 4, 14, 901, 9114, 1, null, 15, 0, 0, null, null, 0, null);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9055, 904, 'Признак застрахованного лица в системе обязательного пенсионного страхования', 'PENSION', 2, 15, null, null, 1, 0, 15, 1, 0, null, null, 0, 1);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9056, 904, 'Признак застрахованного лица в системе обязательного медицинского страхования', 'MEDICAL', 2, 16, null, null, 1, 0, 15, 1, 0, null, null, 0, 1);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9057, 904, 'Признак застрахованного лица в системе обязательного социального страхования', 'SOCIAL', 2, 17, null, null, 1, 0, 15, 1, 0, null, null, 0, 1);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9058, 904, 'Сотрудник', 'EMPLOYEE', 2, 18, null, null, 1, 0, 15, 1, 0, null, null, 0, 1);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9060, 904, 'Система-источник', 'SOURCE_ID', 4, 19, 900, 9002, 1, null, 15, 0, 0, null, null, 0, null);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9063, 904, 'Дублирует', 'DUBLICATES', 4, 20, 904, 9059, 1, null, 15, 0, 0, null, null, 0, null);

insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9061, 905, 'ИНП', 'INP', 1, 1, null, null, 1, null, 15, 1, 1, null, null, 0, 14);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9062, 905, 'АС НУ', 'AS_NU', 4, 2, 900, 9002, 1, null, 15, 1, 0, null, null, 0, null);


insert into ref_book_taxpayer_state(id,code,name, record_id, version) values(seq_ref_book_record.nextval,'1','Налогоплательщик является налоговым резидентом Российской Федерации', 1, date '2016-01-01');
insert into ref_book_taxpayer_state(id,code,name, record_id, version) values(seq_ref_book_record.nextval,'2','Налогоплательщик не является налоговым резидентом Российской Федерации', 2, date '2016-01-01');
insert into ref_book_taxpayer_state(id,code,name, record_id, version) values(seq_ref_book_record.nextval,'3','Налогоплательщик - высококвалифицированный специалист не является налоговым резидентом Российской Федерации', 3, date '2016-01-01');
insert into ref_book_taxpayer_state(id,code,name, record_id, version) values(seq_ref_book_record.nextval,'4','Налогоплательщик - участник Государственной программы по оказанию содействия добровольному переселению в Российскую Федерацию соотечественников, проживающих за рубежом (член экипажа судна, плавающего под Государственным флагом Российской Федерации), не является налоговым резидентом Российской Федерации', 4, date '2016-01-01');
insert into ref_book_taxpayer_state(id,code,name, record_id, version) values(seq_ref_book_record.nextval,'5','Налогоплательщик - иностранный гражданин (лицо без гражданства) признан беженцем или получивший временное убежище на территории Российской Федерации, не является налоговым резидентом Российской Федерации', 5, date '2016-01-01');
insert into ref_book_taxpayer_state(id,code,name, record_id, version) values(seq_ref_book_record.nextval,'6','Налогоплательщик - иностранный гражданин, осуществляет трудовую деятельность по найму в Российской Федерации на основании патента', 6, date '2016-01-01');




