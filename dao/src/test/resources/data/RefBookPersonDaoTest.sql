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

insert into ref_book_country(id, record_id, status, version, code, code_2, code_3, name, fullname) values (1, 1, 0, date '2016-01-01', '643', 'RU', 'RUS', 'РОССИЯ', 'Российская Федерация');
Insert into REF_BOOK_COUNTRY (ID,RECORD_ID,STATUS,VERSION,CODE,CODE_2,CODE_3,NAME,FULLNAME) values (2,2,'0',to_date('01.01.16','DD.MM.RR'),'678','ST','STP','САН-ТОМЕ И ПРИНСИПИ','Демократическая Республика Сан-Томе и Принсипи');
Insert into REF_BOOK_COUNTRY (ID,RECORD_ID,STATUS,VERSION,CODE,CODE_2,CODE_3,NAME,FULLNAME) values (3,3,'0',to_date('01.01.16','DD.MM.RR'),'630','PR','PRI','ПУЭРТО-РИКО',null);

--------------------------------------------------------
--  REF_BOOK_ASNU
--------------------------------------------------------
insert into ref_book (id,name,visible,type,read_only,region_attribute_id,table_name,is_versioned) values (900,'АСНУ',1,0,1,null,'REF_BOOK_ASNU',0);
insert into ref_book_attribute (id,ref_book_id,name,alias,type,ord,reference_id,attribute_id,visible,precision,width,required,is_unique,sort_order,format,read_only,max_length) values (9002,900,'Код АСНУ','CODE',1,1,null,null,1,null,5,1,0,null,null,0,4);
insert into ref_book_attribute (id,ref_book_id,name,alias,type,ord,reference_id,attribute_id,visible,precision,width,required,is_unique,sort_order,format,read_only,max_length) values (9003,900,'Наименование АСНУ','NAME',1,2,null,null,1,null,25,0,0,null,null,0,100);
insert into ref_book_attribute (id,ref_book_id,name,alias,type,ord,reference_id,attribute_id,visible,precision,width,required,is_unique,sort_order,format,read_only,max_length) values (9004,900,'Тип дохода','TYPE',1,3,null,null,1,null,50,0,0,null,null,0,255);

insert into ref_book_asnu (id,code,name,type, priority) values (1,'1000','АС "SAP"','Оплата труда сотрудников, оплата по договорам ГПХ, материальная выгода по кредитам, выданным сотрудникам', 900);
insert into ref_book_asnu (id,code,name,type, priority) values (2,'2000','АИС "Дивиденд"','Сведения по доходам в виде дивидендов по акциям Сбербанка', 800);
insert into ref_book_asnu (id,code,name,type, priority) values (3,'3000','АС "Diasoft Custody 5NT"','Операции с ценными бумагами, ФИСС по договорам брокерского обслуживания ', 600);
insert into ref_book_asnu (id,code,name,type, priority) values (4,'4000','АС "Инфобанк"','Проценты по векселям, сберегательным сертификатам', 500);
insert into ref_book_asnu (id,code,name,type, priority) values (5,'5000','АИС "Депозитарий"','Доходы по ценным бумагам, учитываемым на счетах депо депозитария, дивиденды по акциям, купоны по облигациям', 700);
insert into ref_book_asnu (id,code,name,type, priority) values (6,'6000','Материальная выгода. Кредиты_АС "ЕКП"','Материальная выгода от экономии за пользование заемными средствами (2610)', 400);
insert into ref_book_asnu (id,code,name,type, priority) values (7,'6001','Экономическая выгода. Кредиты_АС "ЕКП"','Экономическая выгода при списании с баланса ссудной задолженности по кредитным договорам (4800)', 400);
insert into ref_book_asnu (id,code,name,type, priority) values (8,'7000','Экономическая выгода. Карты_ АС "ИПС БК"','Экономическая выгода при списании с баланса ссудной задолженности по кредитным картам (4800)', 300);
insert into ref_book_asnu (id,code,name,type, priority) values (9,'6002','Экономическая выгода. Комиссии_АС "ЕКП"','Экономическая выгода при списании с баланса дебиторской задолженности по комиссионным доходам (4800)', 400);
insert into ref_book_asnu (id,code,name,type, priority) values (10,'6003','Реструктуризация валютных кредитов_АС "ЕКП"','Реструктуризация валютных кредитов на льготных условиях (4800)', 400);
insert into ref_book_asnu (id,code,name,type, priority) values (11,'6004','Прощение долга (амнистия). Кредиты_АС "ЕКП"','Прощение (амнистия) задолженности физических лиц (4800)', 400);
insert into ref_book_asnu (id,code,name,type, priority) values (12,'6005','Выплаты клиентам по решениям суда_АС "ЕКП"','Выплаты клиентам по решениям суда (4800)', 400);
insert into ref_book_asnu (id,code,name,type, priority) values (13,'1001','Призы, подарки клиентам_АС "SAP"','Призы, подарки клиентам (2740)', 900);
insert into ref_book_asnu (id,code,name,type, priority) values (14,'8000','АС "Back Office" ','Проценты по вкладам', 200);


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

insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9031, 903, 'Код', 'CODE', 1, 1, null, null, 1, null, 1, 1, 0, null, null, 0, 1);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9032, 903, 'Наименование', 'NAME', 1, 2, null, null, 1, null, 15, 1, 0, null, null, 0, 1000);

insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9059, 904, 'Идентификатор ФЛ', 'RECORD_ID', 2, 0, null, null, 1, 0, 18, 1, 1, null, null, 1, 18);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9041, 904, 'Фамилия', 'LAST_NAME', 1, 1, null, null, 1, null, 15, 1, 0, null, null, 0, 60);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9042, 904, 'Имя', 'FIRST_NAME', 1, 2, null, null, 1, null, 15, 1, 0, null, null, 0, 60);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9043, 904, 'Отчество', 'MIDDLE_NAME', 1, 3, null, null, 1, null, 15, 0, 0, null, null, 0, 60);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9045, 904, 'ИНН в Российской Федерации', 'INN', 1, 5, null, null, 1, null, 12, 0, 1, null, null, 0, 12);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9046, 904, 'ИНН в стране гражданства', 'INN_FOREIGN', 1, 6, null, null, 1, null, 15, 0, 1, null, null, 0, 50);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9047, 904, 'СНИЛС', 'SNILS', 1, 7, null, null, 1, null, 14, 0, 1, null, null, 0, 14);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9048, 904, 'Статус налогоплательщика ', 'TAXPAYER_STATE', 4, 8, 903, 9031, 0, null, 15, 1, 0, null, null, 0, null);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9050, 904, 'Дата рождения', 'BIRTH_DATE', 3, 10, null, null, 1, null, 15, 1, 0, null, 1, 0, null);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9051, 904, 'Место рождения', 'BIRTH_PLACE', 1, 11, null, null, 1, null, 15, 0, 0, null, null, 0, 255);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9052, 904, 'Гражданство', 'CITIZENSHIP', 4, 12, 10, 50, 1, null, 3, 1, 0, null, null, 0, null);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9054, 904, 'Адрес места жительства', 'ADDRESS', 4, 14, 901, 9114, 1, null, 15, 0, 0, null, null, 0, null);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9058, 904, 'Сотрудник', 'EMPLOYEE', 2, 18, null, null, 1, 0, 15, 1, 0, null, null, 0, 1);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9060, 904, 'Система-источник', 'SOURCE_ID', 4, 19, 900, 9002, 1, null, 15, 0, 0, null, null, 0, null);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9063, 904, 'Дублирует', 'DUBLICATES', 4, 20, 904, 9059, 1, null, 15, 0, 0, null, null, 0, null);

insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9061, 905, 'ИНП', 'INP', 1, 1, null, null, 1, null, 15, 1, 1, null, null, 0, 14);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9062, 905, 'АС НУ', 'AS_NU', 4, 2, 900, 9002, 1, null, 15, 1, 0, null, null, 0, null);

--------------------------------------------------------
--  Статусы налогоплательщика
--------------------------------------------------------
insert into ref_book_taxpayer_state(id,code,name, record_id, version) values(1,'1','Налогоплательщик является налоговым резидентом Российской Федерации', 1, date '2016-01-01');
insert into ref_book_taxpayer_state(id,code,name, record_id, version) values(2,'2','Налогоплательщик не является налоговым резидентом Российской Федерации', 2, date '2016-01-01');
insert into ref_book_taxpayer_state(id,code,name, record_id, version) values(3,'3','Налогоплательщик - высококвалифицированный специалист не является налоговым резидентом Российской Федерации', 3, date '2016-01-01');
insert into ref_book_taxpayer_state(id,code,name, record_id, version) values(4,'4','Налогоплательщик - участник Государственной программы по оказанию содействия добровольному переселению в Российскую Федерацию соотечественников, проживающих за рубежом (член экипажа судна, плавающего под Государственным флагом Российской Федерации), не является налоговым резидентом Российской Федерации', 4, date '2016-01-01');
insert into ref_book_taxpayer_state(id,code,name, record_id, version) values(5,'5','Налогоплательщик - иностранный гражданин (лицо без гражданства) признан беженцем или получивший временное убежище на территории Российской Федерации, не является налоговым резидентом Российской Федерации', 5, date '2016-01-01');
insert into ref_book_taxpayer_state(id,code,name, record_id, version) values(6,'6','Налогоплательщик - иностранный гражданин, осуществляет трудовую деятельность по найму в Российской Федерации на основании патента', 6, date '2016-01-01');


--------------------------------------------------------
--  Документы физ. лиц
--------------------------------------------------------
insert into ref_book_doc_type (id, record_id, status, version, code, name) values (1, 1, 0, date '2018-01-01', '21', 'Паспорт');
insert into ref_book_doc_type (id, record_id, status, version, code, name) values (2, 2, 0, date '2018-01-01', '03', 'Свидетельство о рождении');

insert into ref_book_id_doc (id, record_id, version, status, doc_id, doc_number) values (1, 1, date '2018-01-01', 0, 1, '01 01 123456');
insert into ref_book_id_doc (id, record_id, version, status, doc_id, doc_number) values (2, 2, date '2018-01-01', 0, 2, 'Дц-01 123456');
insert into ref_book_id_doc (id, record_id, version, status, doc_id, doc_number) values (3, 3, date '2018-01-01', 0, 1, '12 34 567890');

--------------------------------------------------------
--  Справочник физ. лиц
--------------------------------------------------------
insert into ref_book_person (id,    last_name,  first_name,   middle_name,  inn,    inn_foreign,  snils,      taxpayer_state,   birth_date,         birth_place,  citizenship,  country_id, region_code, postal_code, district,           city, locality,     street,               house, build, appartment, address_foreign,                                              record_id,  start_date,        end_date,          source_id,  old_id, vip,  report_doc)
values                      (1,     'Иванов',   'Борис',      'Петрович',   '1234', null,         null,       1,                date '1975-04-15',  null,         1,            null,       77,          119234,      null,               null, null,         'ЛЕНИНСКИЕ ГОРЫ УЛ',  '1',   'в',   'общ',      '77,119234,,,,Ленинские Горы ул,1,в,общ',                     1,          date '2016-01-01', date '2016-12-31', 1,          1,      1,    1);
insert into ref_book_person (id,    last_name,  first_name,   middle_name,  inn,    inn_foreign,  snils,      taxpayer_state,   birth_date,         birth_place,  citizenship,  country_id, region_code, postal_code, district,           city, locality,     street,               house, build, appartment, address_foreign,                                              record_id,  start_date,                           source_id,  old_id, vip,  report_doc)
values                      (2,     'Чучкалова','Екатерина',  'Викторовна', '2345', null,         null,       1,                date '1981-10-16',  null,         1,            null,       50,          143080,      'ОДИНЦОВСКИЙ р-н',  null, 'ВНИИССОК п', 'Рябиновая ул',       '1',   null,  'общ',      '50,143080,ОДИНЦОВСКИЙ р-н,,ВНИИССОК п,Рябиновая ул,1,,общ',  2,          date '2016-01-01',                    2,          2,      1,    3);
insert into ref_book_person (id,    last_name,  first_name,   middle_name,  inn,    inn_foreign,  snils,      taxpayer_state,   birth_date,         birth_place,  citizenship,  country_id, region_code, postal_code, district,           city, locality,     street,               house, build, appartment, address_foreign,                                              record_id,  start_date,                           source_id,  old_id, vip,  report_doc)
values                      (3,     'Потапов',  'Сергей',     'СЕРГЕЕВИЧ',  null,   null,         '2_1-Zabc', 1,                date '1992-07-29',  null,         1,            null,       50,          143080,      'ОДИНЦОВСКИЙ р-н',  null, 'ВНИИССОК п', 'Рябиновая ул',       '1',   null,  'общ',      '50,143080,ОДИНЦОВСКИЙ р-н,,ВНИИССОК п,Рябиновая ул,1,,общ',  1,          date '2016-01-01',                    6,          3,      0,    null);
insert into ref_book_person (id,    last_name,  first_name,   middle_name,  inn,    inn_foreign,  snils,      taxpayer_state,   birth_date,         birth_place,  citizenship,  country_id, region_code, postal_code, district,           city, locality,     street,               house, build, appartment, address_foreign,                                              record_id,  start_date,                           source_id,  old_id, vip,  report_doc)
values                      (4,     'Сульжик',  'Владислав',  'СЕРГЕЕВИЧ',  null,   null,         null,       1,                date '1992-09-25',  null,         1,            null,       77,          109431,      null,               null, null,         'Привольная ул',      '57',  '1',   '5',        '77,109431,,,,Привольная ул,57,1,5',                          1,          date '2016-01-01',                    6,          10,     0,    null);
insert into ref_book_person (id,    last_name,  first_name,   middle_name,  inn,    inn_foreign,  snils,      taxpayer_state,   birth_date,         birth_place,  citizenship,  country_id, region_code, postal_code, district,           city, locality,     street,               house, build, appartment, address_foreign,                                              record_id,  start_date,                           source_id,  old_id, vip,  report_doc)
values                      (5,     'Doe',      'John',       null,         null,   'bb4',        null,       2,                date '1992-09-25',  null,         2,            2,          null,        null,        null,               null, null,         null,                 null,  null,  null,       'COL, WASHINGTON DC, KENNEDY STREET, 20',                     5,          date '2016-01-01',                    6,          5,      1,    null);
insert into ref_book_person (id,    last_name,  first_name,   middle_name,  inn,    inn_foreign,  snils,      taxpayer_state,   birth_date,         birth_place,  citizenship,  country_id, region_code, postal_code, district,           city, locality,     street,               house, build, appartment, address_foreign,                                              record_id,  start_date,                           source_id,  old_id, vip,  report_doc)
values                      (6,     'Doe',      'Jane',       null,         null,   '123-D123',   null,       3,                date '1992-09-25',  null,         3,            3,          null,        null,        null,               null, null,         null,                 null,  null,  null,       'London, Cromwell street, 10a',                               6,          date '2016-01-01',                    6,          6,      0,    null);
insert into ref_book_person (id,    last_name,  first_name,   middle_name,  inn,    inn_foreign,  snils,      taxpayer_state,   birth_date,         birth_place,  citizenship,  country_id, region_code, postal_code, district,           city, locality,     street,               house, build, appartment, address_foreign,                                              record_id,  start_date,                           source_id,  old_id, vip,  report_doc)
values                      (12122, 'Чхен Джу',  'Владислав',  'СЕРГЕЕВИЧ', null,   null,         null,       1,                date '1992-09-25',  null,         1,            null,       77,          109431,      null,               null, null,         'Привольная ул',      '57',  '1',   '5',        '77,109431,,,,Привольная ул,57,1,5',                          1,          date '2017-01-01',                    6,          10,     0,    null);
-- Присваивам ранее созданным документам ссылку на ФЛ
update ref_book_id_doc set person_id = 1 where id in (1, 2);
update ref_book_id_doc set person_id = 2 where id = 3;

--------------------------------------------------------
--  Подразделения физ. лиц
--------------------------------------------------------


insert into ref_book_person_tb (id, record_id, version, status, person_id, tb_department_id) values(1, 1, date '2018-01-01', 0, 1, 1);
insert into ref_book_person_tb (id, record_id, version, status, person_id, tb_department_id) values(2, 2, date '2018-01-01', 0, 1, 2);

Insert into REF_BOOK_ID_DOC (ID,RECORD_ID,VERSION,STATUS,PERSON_ID,DOC_ID,DOC_NUMBER,DUPLICATE_RECORD_ID) values (4,4,to_date('01.01.17','DD.MM.RR'),'0',1,null,'96 71 570980',null);