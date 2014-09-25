--справочники
insert into REF_BOOK (ID, NAME) values (4,'Коды субъектов Российской Федерации');
insert into REF_BOOK (ID, NAME) values (200,'Параметры представления деклараций по налогу на имущество');
insert into REF_BOOK (ID, NAME) values (204,'Коды налоговых органов');
insert into REF_BOOK (ID, NAME) values (205,'КПП налоговых органов');

--атрибуты справочников
insert into REF_BOOK_ATTRIBUTE (ID, REF_BOOK_ID, NAME, ALIAS, TYPE, ORD, REFERENCE_ID, ATTRIBUTE_ID, VISIBLE, PRECISION, WIDTH, REQUIRED, IS_UNIQUE, SORT_ORDER, FORMAT, READ_ONLY, MAX_LENGTH)
    values (10, 4, 'Наименование', 'NAME', 1, 1, NULL, NULL, 1, NULL, 50, 1, 0, NULL, NULL, 0, 255);
insert into REF_BOOK_ATTRIBUTE (ID ,REF_BOOK_ID, NAME, ALIAS, TYPE, ORD, REFERENCE_ID, ATTRIBUTE_ID, VISIBLE, PRECISION, WIDTH, REQUIRED, IS_UNIQUE, SORT_ORDER, FORMAT, READ_ONLY, MAX_LENGTH)
    values (2001, 200, 'Код субъекта РФ представителя декларации', 'DECLARATION_REGION_ID', 4, 1, 4, 10, 1, NULL, 10, 1, 0, NULL, NULL, 0, NULL);
insert into REF_BOOK_ATTRIBUTE (ID, REF_BOOK_ID, NAME, ALIAS, TYPE, ORD, REFERENCE_ID, ATTRIBUTE_ID, VISIBLE, PRECISION, WIDTH, REQUIRED, IS_UNIQUE, SORT_ORDER, FORMAT, READ_ONLY, MAX_LENGTH)
    values (2003, 200, 'Код налогового органа', 'TAX_ORGAN_CODE', 1, 3, NULL, NULL, 1, NULL, 10, 0, 0, NULL, NULL, 0, 4);
insert into REF_BOOK_ATTRIBUTE (ID, REF_BOOK_ID, NAME, ALIAS, TYPE, ORD, REFERENCE_ID, ATTRIBUTE_ID, VISIBLE, PRECISION, WIDTH, REQUIRED, IS_UNIQUE, SORT_ORDER, FORMAT, READ_ONLY, MAX_LENGTH)
    values (2004, 200, 'КПП', 'KPP', 1, 4, NULL, NULL, 1, NULL, 10, 0, 0, NULL, NULL, 0, 9);
insert into REF_BOOK_ATTRIBUTE (ID, REF_BOOK_ID, NAME, ALIAS, TYPE, ORD, REFERENCE_ID, ATTRIBUTE_ID, VISIBLE, PRECISION, WIDTH, REQUIRED, IS_UNIQUE, SORT_ORDER, FORMAT, READ_ONLY, MAX_LENGTH)
    values (2041, 204, 'Код налогового органа', 'TAX_ORGAN_CODE', 1, 3, NULL, NULL, 1, NULL, 10, 0, 0, NULL, NULL, 0, 4);
insert into REF_BOOK_ATTRIBUTE (ID, REF_BOOK_ID, NAME, ALIAS, TYPE, ORD, REFERENCE_ID, ATTRIBUTE_ID, VISIBLE, PRECISION, WIDTH, REQUIRED, IS_UNIQUE, SORT_ORDER, FORMAT, READ_ONLY, MAX_LENGTH)
    values (2051, 205, 'КПП', 'KPP', 1, 4, NULL, NULL, 1, NULL, 10, 0, 0, NULL, NULL, 0, 9);


--данные справочника 200
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (1,1,200,to_date('01.01.2012','DD.MM.YYYY'),0);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (2,2,200,to_date('01.01.2012','DD.MM.YYYY'),0);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (3,3,200,to_date('01.01.2012','DD.MM.YYYY'),0);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (4,4,200,to_date('01.01.2012','DD.MM.YYYY'),0);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (5,5,200,to_date('01.01.2012','DD.MM.YYYY'),0);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (6,6,200,to_date('01.01.2012','DD.MM.YYYY'),0);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (7,7,200,to_date('01.01.2012','DD.MM.YYYY'),0);

insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(1,2003,'1',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(1,2004,'11',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(1,2001,null,null,null,1);

insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(2,2003,'1',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(2,2004,'12',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(2,2001,null,null,null,2);

insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(3,2003,'1',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(3,2004,'13',null,null,null);

insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(4,2003,'2',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(4,2004,'11',null,null,null);

insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(5,2003,'2',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(5,2004,'12',null,null,null);

insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(6,2003,'2',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(6,2004,'23',null,null,null);

insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(7,2003,'1',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(7,2004,'11',null,null,null);
