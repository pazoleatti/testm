--Справочник "Коды единиц измерения на основании ОКЕИ"
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,1,12,to_date('01.01.2012','dd.mm.yyyy'),0);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,57,'055',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,58,'Квадратный метр',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,59,'М2',null,null,null);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,2,12,to_date('01.01.2012','dd.mm.yyyy'),0);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,57,'181',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,58,'Брутто-регистровая тонна (2,8316 м3)',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,59,'БРТ',null,null,null);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,3,12,to_date('01.01.2012','dd.mm.yyyy'),0);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,57,'251',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,58,'Лошадиная сила',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,59,'л.с.',null,null,null);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,4,12,to_date('01.01.2012','dd.mm.yyyy'),0);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,57,'744',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,58,'Проценты',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,59,'ПРОЦ',null,null,null);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,5,12,to_date('01.01.2012','dd.mm.yyyy'),0);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,57,'796',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,58,'Штука',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,59,'Шт',null,null,null);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,6,12,to_date('01.01.2012','dd.mm.yyyy'),0);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,57,'999',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,58,'Килограмм силы',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,59,'Кгс',null,null,null);
commit;