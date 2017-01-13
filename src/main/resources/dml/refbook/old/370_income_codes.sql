--Коды доходов
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,1,370,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3701,'1010');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3702,'Дивиденды');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,2,370,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3701,'1011');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3702,'Проценты (за исключением процентов по облигациям с ипотечным покрытием, эмитированным до 01.01.2007, доходов в виде процентов, получаемых по вкладам в банках, и доходов, получаемых при погашении векселя), включая дисконт, полученный по долговому обязательству любого вида');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,3,370,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3701,'1110');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3702,'Проценты по облигациям с ипотечным покрытием, эмитированным до 01.01.2007');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,4,370,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3701,'1120');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3702,'Доходы учредителей доверительного управления ипотечным покрытием, полученные на основании приобретения ипотечных сертификатов участия, выданных управляющим ипотечным покрытием до 01.01.2007');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,5,370,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3701,'1530');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3702,'Доходы, полученные по операциям с ценными бумагами, обращающимися на организованном рынке ценных бумаг');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,6,370,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3701,'1531');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3702,'Доходы по операциям с ценными бумагами, не обращающимися на организованном рынке ценных бумаг');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,7,370,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3701,'1532');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3702,'Доходы по операциям с финансовыми инструментами срочных сделок, которые обращаются на организованном рынке и базисным активом которых являются ценные бумаги, фондовые индексы или иные финансовые инструменты срочных сделок, базисным активом которых являются ценные бумаги или фондовые индексы');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,8,370,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3701,'1533');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3702,'Доходы по операциям с финансовыми инструментами срочных сделок, не обращающимися на организованном рынке ценных бумаг');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,9,370,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3701,'1535');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3702,'Доходы по операциям с финансовыми инструментами срочных сделок, которые обращаются на организованном рынке и базисным активом которых не являются ценные бумаги, фондовые индексы или иные финансовые инструменты срочных сделок, базисным активом которых являются ценные бумаги или фондовые индексы');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,10,370,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3701,'1536');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3702,'Доходы, полученные по операциям с ценными бумагами, не обращающимися на организованном рынке ценных бумаг, которые на момент их приобретения отвечали требованиям, предъявляемым к обращающимся ценным бумагам');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,11,370,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3701,'1537');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3702,'Доходы в виде процентов по займу, полученные по совокупности операций РЕПО');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,12,370,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3701,'1538');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3702,'Доходы в виде процентов, полученные в налоговом периоде по совокупности договоров займа');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,13,370,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3701,'1539');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3702,'Доходы по операциям, связанным с открытием короткой позиции, являющимся объектом операций РЕПО');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,14,370,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3701,'1541');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3702,'Доходы, получаемые в результате обмена ценных бумаг, переданных по первой части РЕПО');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,15,370,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3701,'1540');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3702,'Доходы, полученные от реализации долей участия в уставном капитале организаций');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,16,370,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3701,'1543');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3702,'Доходы по операциям, учитываемым на индивидуальном инвестиционном счете');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,17,370,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3701,'2640');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3702,'Материальная выгода, полученная от приобретения ценных бумаг');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,18,370,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3701,'2641');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3702,'Материальная выгода, полученная от приобретения финансовых инструментов срочных сделок');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,19,370,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3701,'2800');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3702,'Проценты (дисконт), полученные при оплате предъявленного к платежу векселя');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,20,370,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3701,'4800');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3702,'Иные доходы');