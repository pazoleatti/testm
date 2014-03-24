/*
Справочник:
	Виды сделок
Список изменений:
	Первоначальное заполнение данными
Данные:
	DF FX;Поставочный форвард на валюту
	NDF FX;Беспоставочный (расчетный) форвард на валюту
	FRA;Беспоставочный (расчетный) форвард на процентные ставки
	DF PM;Поставочный форвард на драгоценные металлы
	NDF PM;Беспоставочный (расчетный) форвард на драгоценные металлы
	CCIRS;Валютно-процентный своп
	IRS;Процентный своп
	CCS;Валютный своп
	DOP FX;оставочный опцион на валюту
	NDOP FX;Беспоставочный (расчетный) опцион на валюту
	DOP PM;Поставочный опцион на драгоценные металлы
	NDOP PM;Беспоставочный (расчетный) опцион на драгоценные металлы
*/
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
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,9,91,to_date('01.01.2012','dd.mm.yyyy'),0);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,831,'DOP FX',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,832,'Поставочный опцион на валюту',null,null,null);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,10,91,to_date('01.01.2012','dd.mm.yyyy'),0);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,831,'NDOP FX',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,832,'Беспоставочный (расчетный) опцион на валюту',null,null,null);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,11,91,to_date('01.01.2012','dd.mm.yyyy'),0);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,831,'DOP PM',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,832,'Поставочный опцион на драгоценные металлы',null,null,null);
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,12,91,to_date('01.01.2012','dd.mm.yyyy'),0);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,831,'NDOP PM',null,null,null);
insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE, NUMBER_VALUE, DATE_VALUE, REFERENCE_VALUE) values(seq_ref_book_record.currval,832,'Беспоставочный (расчетный) опцион на драгоценные металлы',null,null,null);

commit;
exit;