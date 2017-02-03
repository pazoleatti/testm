-- REF_BOOK_PERSON

Insert into REF_BOOK (ID,NAME,SCRIPT_ID,VISIBLE,TYPE,READ_ONLY,REGION_ATTRIBUTE_ID,TABLE_NAME,IS_VERSIONED) values ('904','Физические лица',null,'1','0','0',null,'REF_BOOK_PERSON','1');
Insert into REF_BOOK (ID,NAME,SCRIPT_ID,VISIBLE,TYPE,READ_ONLY,REGION_ATTRIBUTE_ID,TABLE_NAME,IS_VERSIONED) values ('903','Статусы налогоплательщика',null,'1','0','0',null,'REF_BOOK_TAXPAYER_STATE','0');
Insert into REF_BOOK (ID,NAME,SCRIPT_ID,VISIBLE,TYPE,READ_ONLY,REGION_ATTRIBUTE_ID,TABLE_NAME,IS_VERSIONED) values ('10','ОК 025-2001 (Общероссийский классификатор стран мира)',null,'1','0','0',null,null,'1');
Insert into REF_BOOK (ID,NAME,SCRIPT_ID,VISIBLE,TYPE,READ_ONLY,REGION_ATTRIBUTE_ID,TABLE_NAME,IS_VERSIONED) values ('901','Адреса физических лиц',null,'0','0','0',null,'REF_BOOK_ADDRESS','0');
Insert into REF_BOOK (ID,NAME,SCRIPT_ID,VISIBLE,TYPE,READ_ONLY,REGION_ATTRIBUTE_ID,TABLE_NAME,IS_VERSIONED) values ('504','Реестр проблемных зон/ зон потенциального риска',null,'1','0','0',null,null,'1');

Insert into REF_BOOK_ATTRIBUTE (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('9031','903','Код','CODE','1','1',null,null,'1',null,'15','1','0',null,null,'0','1');
Insert into REF_BOOK_ATTRIBUTE (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('50','10','Код','CODE','1','0',null,null,'1',null,'3','1','1',null,null,'0','3');
Insert into REF_BOOK_ATTRIBUTE (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('9114','901','Город','CITY','1','6',null,null,'1',null,'15','0','0',null,null,'0','50');

Insert into REF_BOOK_RECORD (ID,RECORD_ID,REF_BOOK_ID,VERSION,STATUS) values ('266174099','243278461','504',to_date('29.09.14','DD.MM.RR'),'0');
Insert into REF_BOOK_RECORD (ID,RECORD_ID,REF_BOOK_ID,VERSION,STATUS) values ('-1','-1','10',to_date('01.01.1970','DD.MM.YYYY'),'0');

Insert into REF_BOOK_ATTRIBUTE (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('9041','904','Фамилия','LAST_NAME','1','1',null,null,'1',null,'15','1','0',null,null,'0','60');
Insert into REF_BOOK_ATTRIBUTE (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('9042','904','Имя','FIRST_NAME','1','2',null,null,'1',null,'15','1','0',null,null,'0','60');
Insert into REF_BOOK_ATTRIBUTE (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('9043','904','Отчество','MIDDLE_NAME','1','3',null,null,'1',null,'15','0','0',null,null,'0','60');
Insert into REF_BOOK_ATTRIBUTE (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('9044','904','Пол','SEX','2','4',null,null,'1','0','15','0','0',null,null,'0','1');
Insert into REF_BOOK_ATTRIBUTE (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('9045','904','ИНН в Российской Федерации','INN','1','5',null,null,'1',null,'12','0','1',null,null,'0','12');
Insert into REF_BOOK_ATTRIBUTE (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('9046','904','ИНН в стране гражданства','INN_FOREIGN','1','6',null,null,'1',null,'15','0','1',null,null,'0','50');
Insert into REF_BOOK_ATTRIBUTE (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('9047','904','СНИЛС','SNILS','1','7',null,null,'1',null,'14','0','1',null,null,'0','14');
Insert into REF_BOOK_ATTRIBUTE (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('9048','904','Статус налогоплательщика ','TAXPAYER_STATE','4','8','903','9031','1',null,'15','0','0',null,null,'0',null);
Insert into REF_BOOK_ATTRIBUTE (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('9050','904','Дата рождения','BIRTH_DATE','3','10',null,null,'1',null,'15','1','0',null,'1','0',null);
Insert into REF_BOOK_ATTRIBUTE (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('9051','904','Место рождения','BIRTH_PLACE','1','11',null,null,'1',null,'15','0','0',null,null,'0','255');
Insert into REF_BOOK_ATTRIBUTE (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('9052','904','Гражданство','CITIZENSHIP','4','12','10','50','1',null,'3','1','0',null,null,'0',null);
Insert into REF_BOOK_ATTRIBUTE (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('9054','904','Адрес места жительства','ADDRESS','4','14','901','9114','1',null,'15','0','0',null,null,'0',null);
Insert into REF_BOOK_ATTRIBUTE (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('9055','904','Признак застрахованного лица в системе обязательного пенсионного страхования','PENSION','2','15',null,null,'1','0','15','1','0',null,null,'0','1');
Insert into REF_BOOK_ATTRIBUTE (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('9056','904','Признак застрахованного лица в системе обязательного медицинского страхования','MEDICAL','2','16',null,null,'1','0','15','1','0',null,null,'0','1');
Insert into REF_BOOK_ATTRIBUTE (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('9057','904','Признак застрахованного лица в системе обязательного социального страхования','SOCIAL','2','17',null,null,'1','0','15','1','0',null,null,'0','1');
Insert into REF_BOOK_ATTRIBUTE (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('9059','904','Идентификатор ФЛ','RECORD_ID','2','0',null,null,'1','0','18','1','1',null,null,'1','18');

Insert into REF_BOOK_PERSON (ID,LAST_NAME,FIRST_NAME,MIDDLE_NAME,SEX,INN,INN_FOREIGN,SNILS,TAXPAYER_STATE,BIRTH_DATE,BIRTH_PLACE,CITIZENSHIP,ADDRESS,PENSION,MEDICAL,SOCIAL,EMPLOYEE,RECORD_ID,VERSION,STATUS) values ('-1','-','-','-','1',null,null,null,null,to_date('01.01.1300','DD.MM.YYYY'),'Москва','-1',null,'2','2','2','2','1',to_date('09.01.1970','DD.MM.YYYY'),'2');
Insert into REF_BOOK_PERSON (ID,LAST_NAME,FIRST_NAME,MIDDLE_NAME,SEX,INN,INN_FOREIGN,SNILS,TAXPAYER_STATE,BIRTH_DATE,BIRTH_PLACE,CITIZENSHIP,ADDRESS,PENSION,MEDICAL,SOCIAL,EMPLOYEE,RECORD_ID,VERSION,STATUS) values ('1','Иванов','Федор','Семенович','1',null,null,null,null,to_date('01.01.1972','DD.MM.YYYY'),'Москва','266174099',null,'2','2','2','2','1',to_date('09.01.2017','DD.MM.YYYY'),'0');
Insert into REF_BOOK_PERSON (ID,LAST_NAME,FIRST_NAME,MIDDLE_NAME,SEX,INN,INN_FOREIGN,SNILS,TAXPAYER_STATE,BIRTH_DATE,BIRTH_PLACE,CITIZENSHIP,ADDRESS,PENSION,MEDICAL,SOCIAL,EMPLOYEE,RECORD_ID,VERSION,STATUS) values ('2','Иванов','Петр','Семенович','1',null,null,null,null,to_date('01.01.1970','DD.MM.YYYY'),'Москва','266174099',null,'2','2','2','2','2',to_date('09.01.2017','DD.MM.YYYY'),'0');
Insert into REF_BOOK_PERSON (ID,LAST_NAME,FIRST_NAME,MIDDLE_NAME,SEX,INN,INN_FOREIGN,SNILS,TAXPAYER_STATE,BIRTH_DATE,BIRTH_PLACE,CITIZENSHIP,ADDRESS,PENSION,MEDICAL,SOCIAL,EMPLOYEE,RECORD_ID,VERSION,STATUS) values ('3','Иванов','Феофан','Семенович','1',null,null,null,null,to_date('08.02.1980','DD.MM.YYYY'),'Москва','266174099',null,'2','2','2','2','3',to_date('01.01.2012','DD.MM.YYYY'),'0');
Insert into REF_BOOK_PERSON (ID,LAST_NAME,FIRST_NAME,MIDDLE_NAME,SEX,INN,INN_FOREIGN,SNILS,TAXPAYER_STATE,BIRTH_DATE,BIRTH_PLACE,CITIZENSHIP,ADDRESS,PENSION,MEDICAL,SOCIAL,EMPLOYEE,RECORD_ID,VERSION,STATUS) values ('4','Иванов','Семен','Васильевич','1',null,null,null,null,to_date('08.02.1946','DD.MM.YYYY'),'Москва','266174099',null,'2','2','2','2','4',to_date('01.01.2010','DD.MM.YYYY'),'0');
Insert into REF_BOOK_PERSON (ID,LAST_NAME,FIRST_NAME,MIDDLE_NAME,SEX,INN,INN_FOREIGN,SNILS,TAXPAYER_STATE,BIRTH_DATE,BIRTH_PLACE,CITIZENSHIP,ADDRESS,PENSION,MEDICAL,SOCIAL,EMPLOYEE,RECORD_ID,VERSION,STATUS) values ('5','Иванов','Семен','Васильевич','1',null,null,null,null,to_date('08.02.1948','DD.MM.YYYY'),'Калуга','266174099',null,'2','2','2','2','4',to_date('01.01.2011','DD.MM.YYYY'),'0');

-- Вымышленная REF_BOOK_FAMILY

create table ref_book_family
(
  id number(18) not null,
  parent_id number(18),
  last_name varchar2(60 char) not null,
  first_name varchar2(60 char) not null,
  middle_name varchar2(60 char),
  record_id number(18) not null,
  version date not null,
  status number(1) default 0 not null
);

Insert into REF_BOOK (ID,NAME,SCRIPT_ID,VISIBLE,TYPE,READ_ONLY,REGION_ATTRIBUTE_ID,TABLE_NAME,IS_VERSIONED) values ('1983','Отцы и дети',null,'1','1','0',null,'REF_BOOK_FAMILY','1');
Insert into REF_BOOK_ATTRIBUTE (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values
  ('999991','1983','Фамилия','LAST_NAME','1','1',null,null,'1',null,'25','1','0',null,null,'0','25');
Insert into REF_BOOK_ATTRIBUTE (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values
  ('999992','1983','Имя','FIRST_NAME','1','2',null,null,'1',null,'25','1','0',null,null,'0','25');
Insert into REF_BOOK_ATTRIBUTE (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values
  ('999993','1983','Отчество','MIDDLE_NAME','1','3',null,null,'1',null,'25','1','0',null,null,'0','25');
Insert into REF_BOOK_ATTRIBUTE (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values
  ('999994','1983','Имя родителя','PARENT_ID','4','4','1983','999992','1',null,'25','0','0',null,null,'0',null);

Insert into ref_book_family (id, parent_id, last_name, first_name, middle_name, record_id, version, status) VALUES
  ('-1', null, '-', '-', '-', '1', to_date('01.01.1970','DD.MM.YYYY'), '2');
Insert into ref_book_family (id, parent_id, last_name, first_name, middle_name, record_id, version, status) VALUES
  ('1', null, 'Иванов', 'Иван', 'Аристархович', '1', to_date('01.01.2016','DD.MM.YYYY'), '0');
Insert into ref_book_family (id, parent_id, last_name, first_name, middle_name, record_id, version, status) VALUES
  ('2', '1', 'Иванов', 'Василий', 'Иванович', '2', to_date('01.01.2016','DD.MM.YYYY'), '0');
Insert into ref_book_family (id, parent_id, last_name, first_name, middle_name, record_id, version, status) VALUES
  ('3', '2', 'Иванов', 'Сергей', 'Иванович', '3', to_date('01.01.2016','DD.MM.YYYY'), '0');