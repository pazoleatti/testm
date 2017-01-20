--Коды документов
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,1,360,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3601,'21');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3602,'Паспорт гражданина Российской Федерации');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,2,360,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3601,'03');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3602,'Свидетельство о рождении');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,3,360,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3601,'07');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3602,'Военный билет');
	
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,4,360,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3601,'08');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3602,'Временное удостоверение, выданное взамен военного билета');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,5,360,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3601,'10');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3602,'Паспорт иностранного гражданина');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,6,360,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3601,'11');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3602,'Свидетельство о рассмотрении ходатайства о признании лица беженцем на территории Российской Федерации по существу');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,7,360,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3601,'12');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3602,'Вид на жительство в Российской Федерации');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,8,360,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3601,'13');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3602,'Удостоверение беженца');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,9,360,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3601,'14');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3602,'Временное удостоверение личности гражданина Российской Федерации');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,10,360,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3601,'15');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3602,'Решение на временное проживание в Российской Федерации');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,11,360,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3601,'18');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3602,'Свидетельство о предоставлении временного убежища на территории Российской Федерации');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,12,360,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3601,'23');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3602,'Свидетельство о рождении, выданное уполномоченным органом иностранного государства');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,13,360,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3601,'24');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3602,'Удостоверение личности военнослужащего Российской Федерации');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,14,360,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3601,'91');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3602,'Иные документы');

-- Еремеева М.: заполняем приоритеты документов
insert into ref_book_value(record_id,attribute_id,number_value)
select v.record_id,3603,rownum
  from ref_book_record r join ref_book_value v on (v.record_id=r.id)
 where r.ref_book_id=360
   and r.version=(select max(m.version) from ref_book_record m where m.ref_book_id=360)
   and v.attribute_id=3601
 order by 1;