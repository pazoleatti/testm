-- Справочник "Коды драгоценных металлов"
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,1,90,to_date('01.01.2012','dd.mm.yyyy'),0);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE)
(select
  seq_ref_book_record.currval, 829, null,null,null, r.id as id
from
  ref_book_record r join (select max(version) version, record_id from ref_book_record
                           where ref_book_id = 17 and version <= to_date('10.01.2013', 'dd.mm.yy') group by record_id) t 
                    on (r.version = t.version and r.record_id = t.record_id)
                    left join ref_book_value a1 on a1.record_id = r.id and a1.attribute_id = 40 
where
  r.ref_book_id = 17 and
  r.status <> -1
  and a1.string_value='A98');
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,830,null,1352.00,null,null);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,2,90,to_date('01.01.2012','dd.mm.yyyy'),0);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE)
(select
  seq_ref_book_record.currval, 829, null,null,null, r.id as id
from
  ref_book_record r join (select max(version) version, record_id from ref_book_record
                           where ref_book_id = 17 and version <= to_date('10.01.2013', 'dd.mm.yy') group by record_id) t 
                    on (r.version = t.version and r.record_id = t.record_id)
                    left join ref_book_value a1 on a1.record_id = r.id and a1.attribute_id = 40 
where
  r.ref_book_id = 17 and
  r.status <> -1
  and a1.string_value='A33');
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,830,null,754.00,null,null);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,3,90,to_date('01.01.2012','dd.mm.yyyy'),0);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE)
(select
  seq_ref_book_record.currval, 829, null,null,null, r.id as id
from
  ref_book_record r join (select max(version) version, record_id from ref_book_record
                           where ref_book_id = 17 and version <= to_date('10.01.2013', 'dd.mm.yy') group by record_id) t 
                    on (r.version = t.version and r.record_id = t.record_id)
                    left join ref_book_value a1 on a1.record_id = r.id and a1.attribute_id = 40 
where
  r.ref_book_id = 17 and
  r.status <> -1
  and a1.string_value='A76');
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,830,null,1481.00,null,null);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,4,90,to_date('01.01.2012','dd.mm.yyyy'),0);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE)
(select
  seq_ref_book_record.currval, 829, null,null,null, r.id as id
from
  ref_book_record r join (select max(version) version, record_id from ref_book_record
                           where ref_book_id = 17 and version <= to_date('10.01.2013', 'dd.mm.yy') group by record_id) t 
                    on (r.version = t.version and r.record_id = t.record_id)
                    left join ref_book_value a1 on a1.record_id = r.id and a1.attribute_id = 40 
where
  r.ref_book_id = 17 and
  r.status <> -1
  and a1.string_value='A99');
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,830,null,22.06,null,null);
commit;
--запрос
/*with t as (select
  max(version) version, record_id
from
  ref_book_record
where
  ref_book_id = 90 and version <= to_date('10.01.2013', 'dd.mm.yy')
group by
  record_id)
   
select
  r.id as id,
  a1.reference_value as code,
  a2.number_value as code_2
  
from
  ref_book_record r join t on (r.version = t.version and r.record_id = t.record_id)
  left join ref_book_value a1 on a1.record_id = r.id and a1.attribute_id = 829
  left join ref_book_value a2 on a2.record_id = r.id and a2.attribute_id = 830
  
where
  r.ref_book_id = 90 and
  r.status <> -1*/
