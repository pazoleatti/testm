insert into ref_book (id, name) values (13, 'Виды услуг');

-- Виды услуг
Insert into ref_book_attribute (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('60','13','Код','CODE','2','0',null,null,'1','0','10','1','1',null,null,'0','2');
Insert into ref_book_attribute (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH) values ('61','13','Услуга','NAME','1','1',null,null,'1',null,'50','0','0',null,null,'0','200');

--Виды услуг
--период  2015-01-01 -
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(18, 11, 13, date '2015-01-01', 0);
--период  2017-01-01 -
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(19, 12, 13, date '2017-01-01', 0);
--период  2015-01-01 - 2015-03-30
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(20, 13, 13, date '2015-01-01', 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(21, 13, 13, date '2015-03-30', 2);
--период  2015-01-01 - 2016-03-30
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(22, 14, 13, date '2015-01-01', 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(23, 14, 13, date '2016-03-30', 2);
--удалена
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(24, 15, 13, date '2015-01-01', -1);
--период  2015-01-01 - 2016-03-30 + следующая за ней версия от 01.04.2016
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(25, 16, 13, date '2015-01-01', 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(26, 16, 13, date '2016-03-30', 2);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values
(27, 16, 13, date '2016-04-01', 0);