/*DF FX  Поставочный форвард на валюту
NDF FX  Беспоставочный (расчетный) форвард на валюту
FRA  Беспоставочный (расчетный) форвард на процентные ставки
DF PM  Поставочный форвард на драгоценные металлы
NDF PM  Беспоставочный (расчетный) форвард на драгоценные металлы
CCIRS  Валютно-процентный своп
IRS  Процентный своп
CCS  Валютный своп*/
-- Виды сделок
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,1,91,to_date('01.01.2012','dd.mm.yyyy'),0);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,831,'DF FX',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,832,'Поставочный форвард на валюту',null,null,null);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,2,91,to_date('01.01.2012','dd.mm.yyyy'),0);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,831,'NDF FX',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,832,'Беспоставочный (расчетный) форвард на валюту',null,null,null);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,3,91,to_date('01.01.2012','dd.mm.yyyy'),0);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,831,'FRA',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,832,'Беспоставочный (расчетный) форвард на процентные ставки',null,null,null);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,4,91,to_date('01.01.2012','dd.mm.yyyy'),0);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,831,'DF PM',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,832,'Поставочный форвард на драгоценные металлы',null,null,null);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,5,91,to_date('01.01.2012','dd.mm.yyyy'),0);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,831,'NDF PM',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,832,'Беспоставочный (расчетный) форвард на драгоценные металлы',null,null,null);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,6,91,to_date('01.01.2012','dd.mm.yyyy'),0);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,831,'CCIRS',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,832,'Валютно-процентный своп',null,null,null);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,7,91,to_date('01.01.2012','dd.mm.yyyy'),0);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,831,'IRS',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,832,'Процентный своп',null,null,null);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,8,91,to_date('01.01.2012','dd.mm.yyyy'),0);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,831,'CCS',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,832,'Валютный своп',null,null,null);
commit;
--запрос
/*with t as (select
  max(version) version, record_id
from
  ref_book_record
where
  ref_book_id = 91 and version <= to_date('10.01.2013', 'dd.mm.yy')
group by
  record_id)
   
select
  r.id as id,
  a1.string_value as code,
  a2.string_value as code_2
  
from
  ref_book_record r join t on (r.version = t.version and r.record_id = t.record_id)
  left join ref_book_value a1 on a1.record_id = r.id and a1.attribute_id = 831
  left join ref_book_value a2 on a2.record_id = r.id and a2.attribute_id = 832
  
where
  r.ref_book_id = 91 and
  r.status <> -1*/
