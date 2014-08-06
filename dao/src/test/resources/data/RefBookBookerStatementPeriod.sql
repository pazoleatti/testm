--справочники
INSERT INTO ref_book (id, name) VALUES (106,'Коды, определяющие период бухгалтерской отчетности');
INSERT INTO ref_book (id, name) VALUES (107,'Периоды и подразделения БО');
INSERT INTO ref_book (id, name) VALUES (108,'Периоды БО');

--атрибуты справочника 106
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length)
                        VALUES (1061,106,'Код','CODE',1,1,null,null,1,null,10,1,1,null,null,0,2);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length)
                        VALUES (1062,106,'Наименование','NAME',1,2,null,null,1,null,50,1,0,null,null,0,255);

--атрибуты справочника 107
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length)
                        VALUES (1071,107,'Год','YEAR',2,1,null,null,1,0,10,1,0,-1,null,0,4);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length)
                        VALUES (1072,107,'Код периода бухгалтерской отчетности','ACCOUNT_PERIOD_ID',4,2,106,1062,1,null,20,1,0,null,null,0,null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length)
                        VALUES (1073,107,'Код подразделения','DEPARTMENT_ID',2,3,null,null,1,0,20,1,0,null,null,0,null);

--атрибуты справочника 108
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length)
                        VALUES (1081,108,'Год','YEAR',2,1,null,null,1,0,10,1,0,-1,null,1,4);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length)
                        VALUES (1082,108,'Период','ACCOUNT_PERIOD_ID',4,2,106,1062,1,null,20,1,0,null,null,1,null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length)
                        VALUES (1083,108,'Название периода бухгалтерской отчетности','PERIOD_NAME',1,3,null,null,0,null,20,1,0,null,null,1,null);

--данные справочника 106
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (1,1,106,to_date('01.01.2012','DD.MM.YYYY'),0);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (2,2,106,to_date('01.01.2012','DD.MM.YYYY'),0);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (3,3,106,to_date('01.01.2012','DD.MM.YYYY'),0);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (4,4,106,to_date('01.01.2012','DD.MM.YYYY'),0);

insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(1,1061,'1',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(1,1062,'первый квартал',null,null,null);

insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(2,1061,'2',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(2,1062,'полугодие',null,null,null);

--данные справочника 107
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (11,1,107,to_date('01.01.2012','DD.MM.YYYY'),0);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (12,2,107,to_date('01.01.2012','DD.MM.YYYY'),0);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (13,3,107,to_date('01.01.2012','DD.MM.YYYY'),0);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (14,4,107,to_date('01.01.2012','DD.MM.YYYY'),0);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (15,5,107,to_date('01.01.2012','DD.MM.YYYY'),0);

insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(11,1071,null,2014,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(11,1072,null,null,null,1);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(11,1073,null,null,null,null);

insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(12,1071,null,2015,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(12,1072,null,null,null,1);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(12,1073,null,null,null,null);

insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(13,1071,null,2015,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(13,1072,null,null,null,1);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(13,1073,null,null,null,null);

insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(14,1071,null,2015,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(14,1072,null,null,null,2);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(14,1073,null,null,null,null);

insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(15,1071,null,2016,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(15,1072,null,null,null,2);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(15,1073,null,null,null,null);