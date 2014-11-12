--Справочник "Категории средней стоимости транспортных средств"
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,1,207,to_date('01.01.2012','dd.mm.yyyy'),0);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,2070,'0',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,2071,'От 3 до 5 млн. руб.',null,null,null);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,2,207,to_date('01.01.2012','dd.mm.yyyy'),0);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,2070,'1',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,2071,'От 5 до 10 млн. руб.',null,null,null);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,3,207,to_date('01.01.2012','dd.mm.yyyy'),0);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,2070,'2',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,2071,'От 10 до 15 млн. руб.',null,null,null);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,4,207,to_date('01.01.2012','dd.mm.yyyy'),0);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,2070,'3',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,2071,'Свыше 15 млн. руб.',null,null,null);
commit;