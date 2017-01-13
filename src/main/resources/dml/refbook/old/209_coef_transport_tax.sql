--Справочник "Повышающие коэффициенты транспортного налога"
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,1,209,to_date('01.01.2012','dd.mm.yyyy'),0);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE)
(select
  seq_ref_book_record.currval, 2090, null,null,null, r.id as id
from
  ref_book_record r join (select max(version) version, record_id from ref_book_record
                           where ref_book_id = 211 and version <= to_date('10.01.2013', 'dd.mm.yy') group by record_id) t 
                    on (r.version = t.version and r.record_id = t.record_id)
                    left join ref_book_value a1 on a1.record_id = r.id and a1.attribute_id = 2110 
where
  r.ref_book_id = 211 and
  r.status <> -1
  and a1.string_value='0');
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,2091,null,2,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,2092,null,3,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,2093,null,1.1,null,null);

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,2,209,to_date('01.01.2012','dd.mm.yyyy'),0);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE)
(select
  seq_ref_book_record.currval, 2090, null,null,null, r.id as id
from
  ref_book_record r join (select max(version) version, record_id from ref_book_record
                           where ref_book_id = 211 and version <= to_date('10.01.2013', 'dd.mm.yy') group by record_id) t 
                    on (r.version = t.version and r.record_id = t.record_id)
                    left join ref_book_value a1 on a1.record_id = r.id and a1.attribute_id = 2110 
where
  r.ref_book_id = 211 and
  r.status <> -1
  and a1.string_value='0');
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,2091,null,1,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,2092,null,2,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,2093,null,1.3,null,null);

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,3,209,to_date('01.01.2012','dd.mm.yyyy'),0);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE)
(select
  seq_ref_book_record.currval, 2090, null,null,null, r.id as id
from
  ref_book_record r join (select max(version) version, record_id from ref_book_record
                           where ref_book_id = 211 and version <= to_date('10.01.2013', 'dd.mm.yy') group by record_id) t 
                    on (r.version = t.version and r.record_id = t.record_id)
                    left join ref_book_value a1 on a1.record_id = r.id and a1.attribute_id = 2110 
where
  r.ref_book_id = 211 and
  r.status <> -1
  and a1.string_value='0');
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,2091,null,0,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,2092,null,1,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,2093,null,1.5,null,null);

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,4,209,to_date('01.01.2012','dd.mm.yyyy'),0);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE)
(select
  seq_ref_book_record.currval, 2090, null,null,null, r.id as id
from
  ref_book_record r join (select max(version) version, record_id from ref_book_record
                           where ref_book_id = 211 and version <= to_date('10.01.2013', 'dd.mm.yy') group by record_id) t 
                    on (r.version = t.version and r.record_id = t.record_id)
                    left join ref_book_value a1 on a1.record_id = r.id and a1.attribute_id = 2110 
where
  r.ref_book_id = 211 and
  r.status <> -1
  and a1.string_value='1');
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,2091,null,0,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,2092,null,5,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,2093,null,2,null,null);

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,5,209,to_date('01.01.2012','dd.mm.yyyy'),0);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE)
(select
  seq_ref_book_record.currval, 2090, null,null,null, r.id as id
from
  ref_book_record r join (select max(version) version, record_id from ref_book_record
                           where ref_book_id = 211 and version <= to_date('10.01.2013', 'dd.mm.yy') group by record_id) t 
                    on (r.version = t.version and r.record_id = t.record_id)
                    left join ref_book_value a1 on a1.record_id = r.id and a1.attribute_id = 2110 
where
  r.ref_book_id = 211 and
  r.status <> -1
  and a1.string_value='2');
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,2091,null,0,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,2092,null,10,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,2093,null,3,null,null);

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,6,209,to_date('01.01.2012','dd.mm.yyyy'),0);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE)
(select
  seq_ref_book_record.currval, 2090, null,null,null, r.id as id
from
  ref_book_record r join (select max(version) version, record_id from ref_book_record
                           where ref_book_id = 211 and version <= to_date('10.01.2013', 'dd.mm.yy') group by record_id) t 
                    on (r.version = t.version and r.record_id = t.record_id)
                    left join ref_book_value a1 on a1.record_id = r.id and a1.attribute_id = 2110 
where
  r.ref_book_id = 211 and
  r.status <> -1
  and a1.string_value='3');
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,2091,null,0,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,2092,null,20,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,2093,null,3,null,null);

commit;