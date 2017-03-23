-- REF_BOOK_PERSON

INSERT INTO ref_book (id,name,script_id,visible,type,read_only,region_attribute_id,table_name,is_versioned) VALUES ('904','Физические лица',NULL,'1','0','0',NULL,'REF_BOOK_PERSON','1');
INSERT INTO ref_book (id,name,script_id,visible,type,read_only,region_attribute_id,table_name,is_versioned) VALUES ('903','Статусы налогоплательщика',NULL,'1','0','0',NULL,'REF_BOOK_TAXPAYER_STATE','0');
INSERT INTO ref_book (id,name,script_id,visible,type,read_only,region_attribute_id,table_name,is_versioned) VALUES ('10','ОК 025-2001 (Общероссийский классификатор стран мира)',NULL,'1','0','0',NULL,'REF_BOOK_COUNTRY','1');
INSERT INTO ref_book (id,name,script_id,visible,type,read_only,region_attribute_id,table_name,is_versioned) VALUES ('901','Адреса физических лиц',NULL,'0','0','0',NULL,'REF_BOOK_ADDRESS','0');

INSERT INTO ref_book_attribute (id,ref_book_id,name,alias,type,ord,reference_id,attribute_id,visible,precision,width,required,is_unique,sort_order,format,read_only,max_length) VALUES ('9031','903','Код','CODE','1','1',NULL,NULL,'1',NULL,'15','1','0',NULL,NULL,'0','1');
INSERT INTO ref_book_attribute (id,ref_book_id,name,alias,type,ord,reference_id,attribute_id,visible,precision,width,required,is_unique,sort_order,format,read_only,max_length) VALUES ('50','10','Код','CODE','1','0',NULL,NULL,'1',NULL,'3','1','1',NULL,NULL,'0','3');
INSERT INTO ref_book_attribute (id,ref_book_id,name,alias,type,ord,reference_id,attribute_id,visible,precision,width,required,is_unique,sort_order,format,read_only,max_length) VALUES ('9114','901','Город','CITY','1','6',NULL,NULL,'1',NULL,'15','0','0',NULL,NULL,'0','50');

INSERT INTO ref_book_country (id,record_id,status,version,code,code_2,code_3,name,fullname) VALUES (-1,282,0, date '2012-01-01','638','RE','REU','РЕЮНЬОН',NULL);
INSERT INTO ref_book_country (id,record_id,status,version,code,code_2,code_3,name,fullname) VALUES (266174099,283,0, date '2012-01-01','642','RO','ROU','РУМЫНИЯ',NULL);

INSERT INTO ref_book_attribute (id,ref_book_id,name,alias,type,ord,reference_id,attribute_id,visible,precision,width,required,is_unique,sort_order,format,read_only,max_length) VALUES ('9041','904','Фамилия','LAST_NAME','1','1',NULL,NULL,'1',NULL,'15','1','0',NULL,NULL,'0','60');
INSERT INTO ref_book_attribute (id,ref_book_id,name,alias,type,ord,reference_id,attribute_id,visible,precision,width,required,is_unique,sort_order,format,read_only,max_length) VALUES ('9042','904','Имя','FIRST_NAME','1','2',NULL,NULL,'1',NULL,'15','1','0',NULL,NULL,'0','60');
INSERT INTO ref_book_attribute (id,ref_book_id,name,alias,type,ord,reference_id,attribute_id,visible,precision,width,required,is_unique,sort_order,format,read_only,max_length) VALUES ('9043','904','Отчество','MIDDLE_NAME','1','3',NULL,NULL,'1',NULL,'15','0','0',NULL,NULL,'0','60');
INSERT INTO ref_book_attribute (id,ref_book_id,name,alias,type,ord,reference_id,attribute_id,visible,precision,width,required,is_unique,sort_order,format,read_only,max_length) VALUES ('9044','904','Пол','SEX','2','4',NULL,NULL,'1','0','15','0','0',NULL,NULL,'0','1');
INSERT INTO ref_book_attribute (id,ref_book_id,name,alias,type,ord,reference_id,attribute_id,visible,precision,width,required,is_unique,sort_order,format,read_only,max_length) VALUES ('9045','904','ИНН в Российской Федерации','INN','1','5',NULL,NULL,'1',NULL,'12','0','1',NULL,NULL,'0','12');
INSERT INTO ref_book_attribute (id,ref_book_id,name,alias,type,ord,reference_id,attribute_id,visible,precision,width,required,is_unique,sort_order,format,read_only,max_length) VALUES ('9046','904','ИНН в стране гражданства','INN_FOREIGN','1','6',NULL,NULL,'1',NULL,'15','0','1',NULL,NULL,'0','50');
INSERT INTO ref_book_attribute (id,ref_book_id,name,alias,type,ord,reference_id,attribute_id,visible,precision,width,required,is_unique,sort_order,format,read_only,max_length) VALUES ('9047','904','СНИЛС','SNILS','1','7',NULL,NULL,'1',NULL,'14','0','1',NULL,NULL,'0','14');
INSERT INTO ref_book_attribute (id,ref_book_id,name,alias,type,ord,reference_id,attribute_id,visible,precision,width,required,is_unique,sort_order,format,read_only,max_length) VALUES ('9048','904','Статус налогоплательщика ','TAXPAYER_STATE','4','8','903','9031','1',NULL,'15','0','0',NULL,NULL,'0',NULL);
INSERT INTO ref_book_attribute (id,ref_book_id,name,alias,type,ord,reference_id,attribute_id,visible,precision,width,required,is_unique,sort_order,format,read_only,max_length) VALUES ('9050','904','Дата рождения','BIRTH_DATE','3','10',NULL,NULL,'1',NULL,'15','1','0',NULL,'1','0',NULL);
INSERT INTO ref_book_attribute (id,ref_book_id,name,alias,type,ord,reference_id,attribute_id,visible,precision,width,required,is_unique,sort_order,format,read_only,max_length) VALUES ('9051','904','Место рождения','BIRTH_PLACE','1','11',NULL,NULL,'1',NULL,'15','0','0',NULL,NULL,'0','255');
INSERT INTO ref_book_attribute (id,ref_book_id,name,alias,type,ord,reference_id,attribute_id,visible,precision,width,required,is_unique,sort_order,format,read_only,max_length) VALUES ('9052','904','Гражданство','CITIZENSHIP','4','12','10','50','1',NULL,'3','1','0',NULL,NULL,'0',NULL);
INSERT INTO ref_book_attribute (id,ref_book_id,name,alias,type,ord,reference_id,attribute_id,visible,precision,width,required,is_unique,sort_order,format,read_only,max_length) VALUES ('9054','904','Адрес места жительства','ADDRESS','4','14','901','9114','1',NULL,'15','0','0',NULL,NULL,'0',NULL);
INSERT INTO ref_book_attribute (id,ref_book_id,name,alias,type,ord,reference_id,attribute_id,visible,precision,width,required,is_unique,sort_order,format,read_only,max_length) VALUES ('9055','904','Признак застрахованного лица в системе обязательного пенсионного страхования','PENSION','2','15',NULL,NULL,'1','0','15','1','0',NULL,NULL,'0','1');
INSERT INTO ref_book_attribute (id,ref_book_id,name,alias,type,ord,reference_id,attribute_id,visible,precision,width,required,is_unique,sort_order,format,read_only,max_length) VALUES ('9056','904','Признак застрахованного лица в системе обязательного медицинского страхования','MEDICAL','2','16',NULL,NULL,'1','0','15','1','0',NULL,NULL,'0','1');
INSERT INTO ref_book_attribute (id,ref_book_id,name,alias,type,ord,reference_id,attribute_id,visible,precision,width,required,is_unique,sort_order,format,read_only,max_length) VALUES ('9057','904','Признак застрахованного лица в системе обязательного социального страхования','SOCIAL','2','17',NULL,NULL,'1','0','15','1','0',NULL,NULL,'0','1');
INSERT INTO ref_book_attribute (id,ref_book_id,name,alias,type,ord,reference_id,attribute_id,visible,precision,width,required,is_unique,sort_order,format,read_only,max_length) VALUES ('9059','904','Идентификатор ФЛ','RECORD_ID','2','0',NULL,NULL,'1','0','18','1','1',NULL,NULL,'1','18');

INSERT INTO REF_BOOK_PERSON (ID,LAST_NAME,FIRST_NAME,MIDDLE_NAME,SEX,INN,INN_FOREIGN,SNILS,TAXPAYER_STATE,BIRTH_DATE,BIRTH_PLACE,CITIZENSHIP,ADDRESS,PENSION,MEDICAL,SOCIAL,EMPLOYEE,RECORD_ID,VERSION,STATUS) VALUES ('-1','-','-','-','1',NULL,NULL,NULL,NULL,to_date('01.01.1300','DD.MM.YYYY'),'Москва','-1',NULL,'2','2','2','2','1',to_date('09.01.1970','DD.MM.YYYY'),'2');
INSERT INTO REF_BOOK_PERSON (ID,LAST_NAME,FIRST_NAME,MIDDLE_NAME,SEX,INN,INN_FOREIGN,SNILS,TAXPAYER_STATE,BIRTH_DATE,BIRTH_PLACE,CITIZENSHIP,ADDRESS,PENSION,MEDICAL,SOCIAL,EMPLOYEE,RECORD_ID,VERSION,STATUS) VALUES ('1','Иванов','Федор','Семенович','1',NULL,NULL,NULL,NULL,to_date('01.01.1972','DD.MM.YYYY'),'Москва','266174099',NULL,'2','2','2','2','1',to_date('09.01.2017','DD.MM.YYYY'),'0');
INSERT INTO REF_BOOK_PERSON (ID,LAST_NAME,FIRST_NAME,MIDDLE_NAME,SEX,INN,INN_FOREIGN,SNILS,TAXPAYER_STATE,BIRTH_DATE,BIRTH_PLACE,CITIZENSHIP,ADDRESS,PENSION,MEDICAL,SOCIAL,EMPLOYEE,RECORD_ID,VERSION,STATUS) VALUES ('2','Иванов','Петр','Семенович','1',NULL,NULL,NULL,NULL,to_date('01.01.1970','DD.MM.YYYY'),'Москва','266174099',NULL,'2','2','2','2','2',to_date('09.01.2017','DD.MM.YYYY'),'0');
INSERT INTO REF_BOOK_PERSON (ID,LAST_NAME,FIRST_NAME,MIDDLE_NAME,SEX,INN,INN_FOREIGN,SNILS,TAXPAYER_STATE,BIRTH_DATE,BIRTH_PLACE,CITIZENSHIP,ADDRESS,PENSION,MEDICAL,SOCIAL,EMPLOYEE,RECORD_ID,VERSION,STATUS) VALUES ('3','Иванов','Феофан','Семенович','1',NULL,NULL,NULL,NULL,to_date('08.02.1980','DD.MM.YYYY'),'Москва','266174099',NULL,'2','2','2','2','3',to_date('01.01.2012','DD.MM.YYYY'),'0');
INSERT INTO REF_BOOK_PERSON (ID,LAST_NAME,FIRST_NAME,MIDDLE_NAME,SEX,INN,INN_FOREIGN,SNILS,TAXPAYER_STATE,BIRTH_DATE,BIRTH_PLACE,CITIZENSHIP,ADDRESS,PENSION,MEDICAL,SOCIAL,EMPLOYEE,RECORD_ID,VERSION,STATUS) VALUES ('4','Иванов','Семен','Васильевич','1',NULL,NULL,NULL,NULL,to_date('08.02.1946','DD.MM.YYYY'),'Москва','266174099',NULL,'2','2','2','2','4',to_date('01.01.2010','DD.MM.YYYY'),'0');
INSERT INTO REF_BOOK_PERSON (ID,LAST_NAME,FIRST_NAME,MIDDLE_NAME,SEX,INN,INN_FOREIGN,SNILS,TAXPAYER_STATE,BIRTH_DATE,BIRTH_PLACE,CITIZENSHIP,ADDRESS,PENSION,MEDICAL,SOCIAL,EMPLOYEE,RECORD_ID,VERSION,STATUS) VALUES ('5','Иванов','Семен','Васильевич','1',NULL,NULL,NULL,NULL,to_date('08.02.1948','DD.MM.YYYY'),'Калуга','266174099',NULL,'2','2','2','2','4',to_date('01.01.2011','DD.MM.YYYY'),'0');

-- Вымышленная REF_BOOK_FAMILY

CREATE TABLE ref_book_family
(
  id NUMBER(18) NOT NULL,
  parent_id NUMBER(18),
  last_name VARCHAR2(60 CHAR) NOT NULL,
  first_name VARCHAR2(60 CHAR) NOT NULL,
  middle_name VARCHAR2(60 CHAR),
  record_id NUMBER(18) NOT NULL,
  version DATE NOT NULL,
  status NUMBER(1) DEFAULT 0 NOT NULL
);

INSERT INTO ref_book (id,name,script_id,visible,type,read_only,region_attribute_id,table_name,is_versioned) VALUES ('1983','Отцы и дети',NULL,'1','1','0',NULL,'REF_BOOK_FAMILY','1');
INSERT INTO ref_book_attribute (id,ref_book_id,name,alias,type,ord,reference_id,attribute_id,visible,precision,width,required,is_unique,sort_order,format,read_only,max_length) VALUES
  ('999991','1983','Фамилия','LAST_NAME','1','1',NULL,NULL,'1',NULL,'25','1','0',NULL,NULL,'0','25');
INSERT INTO ref_book_attribute (id,ref_book_id,name,alias,type,ord,reference_id,attribute_id,visible,precision,width,required,is_unique,sort_order,format,read_only,max_length) VALUES
  ('999992','1983','Имя','FIRST_NAME','1','2',NULL,NULL,'1',NULL,'25','1','0',NULL,NULL,'0','25');
INSERT INTO ref_book_attribute (id,ref_book_id,name,alias,type,ord,reference_id,attribute_id,visible,precision,width,required,is_unique,sort_order,format,read_only,max_length) VALUES
  ('999993','1983','Отчество','MIDDLE_NAME','1','3',NULL,NULL,'1',NULL,'25','1','0',NULL,NULL,'0','25');
INSERT INTO ref_book_attribute (id,ref_book_id,name,alias,type,ord,reference_id,attribute_id,visible,precision,width,required,is_unique,sort_order,format,read_only,max_length) VALUES
  ('999994','1983','Имя родителя','PARENT_ID','4','4','1983','999992','1',NULL,'25','0','0',NULL,NULL,'0',NULL);

INSERT INTO ref_book_family (id, parent_id, last_name, first_name, middle_name, record_id, version, status) VALUES
  ('-1', NULL, '-', '-', '-', '1', to_date('01.01.1970','DD.MM.YYYY'), '2');
INSERT INTO ref_book_family (id, parent_id, last_name, first_name, middle_name, record_id, version, status) VALUES
  ('1', NULL, 'Иванов', 'Иван', 'Аристархович', '1', to_date('01.01.2016','DD.MM.YYYY'), '0');
INSERT INTO ref_book_family (id, parent_id, last_name, first_name, middle_name, record_id, version, status) VALUES
  ('2', '1', 'Иванов', 'Василий', 'Иванович', '2', to_date('01.01.2016','DD.MM.YYYY'), '0');
INSERT INTO ref_book_family (id, parent_id, last_name, first_name, middle_name, record_id, version, status) VALUES
  ('3', '2', 'Иванов', 'Сергей', 'Иванович', '3', to_date('01.01.2016','DD.MM.YYYY'), '0');