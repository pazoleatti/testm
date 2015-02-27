--http://jira.aplana.com/browse/SBRFACCTAX-10529: Создать справочники "Коды доходов", "Коды документов", "Коды вычетов" (Наполнение)

--Коды вычетов
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,1,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'104');	
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'500 рублей на налогоплательщика, относящегося к категориям, перечисленным в подпункте 2 пункта 1 статьи 218 Налогового кодекса Российской Федерации');
	
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,2,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'105');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'3000 рублей на налогоплательщика, относящегося к категориям, перечисленным в подпункте 1 пункта 1 статьи 218 Налогового кодекса Российской Федерации');
	
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,3,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'114');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'На первого ребенка в возрасте до 18 лет, а также на каждого учащегося очной формы обучения, аспиранта, ординатора, интерна, студента, курсанта в возрасте до 24 лет родителю, супруге (супругу) родителя, усыновителю, опекуну, попечителю, приемному родителю, супруге (супругу) приемного родителя, на обеспечении которых находится ребенок');
	
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,4,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'115');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'На второго ребенка в возрасте до 18 лет, а также на каждого учащегося очной формы обучения, аспиранта, ординатора, интерна, студента, курсанта в возрасте до 24 лет родителю, супруге (супругу) родителя, усыновителю, опекуну, попечителю, приемному родителю, супруге (супругу) приемного родителя, на обеспечении которых находится ребенок');
	
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,5,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'116');	
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'На третьего и каждого последующего ребенка в возрасте до 18 лет, а также на каждого учащегося очной формы обучения, аспиранта, ординатора, интерна, студента, курсанта в возрасте до 24 лет родителю, супруге (супругу) родителя, усыновителю, опекуну, попечителю, приемному родителю, супруге (супругу) приемного родителя, на обеспечении которых находится ребенок');
	
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,6,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'117');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'На ребенка-инвалида в возрасте до 18 лет или учащегося очной формы обучения, аспиранта, ординатора, интерна, студента в возрасте до 24 лет, являющегося инвалидом I или II группы родителю, супруге (супругу) родителя, усыновителю, опекуну, попечителю, приемному родителю, супруге (супругу) приемного родителя, на обеспечении которых находится ребенок');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,7,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'118');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'В двойном размере на первого ребенка в возрасте до 18 лет, а также на каждого учащегося очной формы обучения, аспиранта, ординатора, интерна, студента, курсанта в возрасте до 24 лет единственному родителю (приемному родителю), усыновителю, опекуну, попечителю');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,8,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'119');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'В двойном размере на второго ребенка в возрасте до 18 лет, а также на каждого учащегося очной формы обучения, аспиранта, ординатора, интерна, студента, курсанта в возрасте до 24 лет единственному родителю (приемному родителю), опекуну, попечителю');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,9,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'120');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'В двойном размере на третьего и каждого последующего ребенка в возрасте до 18 лет, а также на каждого учащегося очной формы обучения, аспиранта, ординатора, интерна, студента, курсанта в возрасте до 24 лет единственному родителю (приемному родителю), усыновителю, опекуну, попечителю');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,10,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'121');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'В двойном размере на ребенка-инвалида в возрасте до 18 лет или учащегося очной формы обучения, аспиранта, ординатора, интерна, студента в возрасте до 24 лет, являющегося инвалидом I или II группы единственному родителю (приемному родителю), усыновителю, опекуну, попечителю');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,11,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'122');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'В двойном размере на первого ребенка в возрасте до 18 лет, а также на каждого учащегося очной формы обучения, аспиранта, ординатора, интерна, студента, курсанта в возрасте до 24 лет одному из родителей (приемных родителей) по их выбору на основании заявления об отказе одного из родителей (приемных родителей) от получения налогового вычета');
	
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,12,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'123');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'В двойном размере на второго ребенка в возрасте до 18 лет, а также на каждого учащегося очной формы обучения, аспиранта, ординатора, интерна, студента, курсанта в возрасте до 24 лет одному из родителей (приемных родителей) по их выбору на основании заявления об отказе одного из родителей (приемных родителей) от получения налогового вычета');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,13,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'124');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'В двойном размере на третьего и каждого последующего ребенка в возрасте до 18 лет, а также на каждого учащегося очной формы обучения, аспиранта, ординатора, интерна, студента, курсанта в возрасте до 24 лет одному из родителей (приемных родителей) по их выбору на основании заявления об отказе одного из родителей (приемных родителей) от получения налогового вычета');
	
insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,14,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'125');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'В двойном размере на ребенка-инвалида в возрасте до 18 лет или учащегося очной формы обучения, аспиранта, ординатора, интерна, студента в возрасте до 24 лет, являющегося инвалидом I или II группы, одному из родителей (приемных родителей) по их выбору на основании заявления об отказе одного из родителей (приемных родителей) от получения налогового вычета');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,15,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'201');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'Расходы по операциям с ценными бумагами, обращающимися на организованном рынке ценных бумаг');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,16,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'202');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'Расходы по операциям с ценными бумагами, не обращающимися на организованном рынке ценных бумаг');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,17,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'203');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'Расходы по операциям с ценными бумагами, не обращающимися на организованном рынке ценных бумаг, которые на момент их приобретения относились к ценным бумагам, обращающимся на организованном рынке ценных бумаг');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,18,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'205');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'Сумма убытка по операциям с ценными бумагами, обращающимися на организованном рынке ценных бумаг, уменьшающая налоговую базу по операциям с финансовыми инструментами срочных сделок которые обращаются на организованном рынке ценных бумаг и базисным активом которых являются ценные бумаги, фондовые индексы или иные финансовые инструменты срочных сделок, базисным активом которых являются ценные бумаги или фондовые индексы');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,19,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'206');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'Расходы по операциям с финансовыми инструментами срочных сделок, которые обращаются на организованном рынке ценных бумаг и базисным активом которых являются ценные бумаги, фондовые индексы или иные финансовые инструменты срочных сделок, базисным активом которых являются ценные бумаги или фондовые индексы');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,20,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'207');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'Расходы по операциям с финансовыми инструментами срочных сделок, которые обращаются на организованном рынке ценных бумаг и базисным активом которых не являются ценные бумаги, фондовые индексы или иные финансовые инструменты срочных сделок, базисным активом которых являются ценные бумаги или фондовые индексы');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,21,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'208');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'Сумма убытка по операциям с финансовыми инструментами срочных сделок, которые обращаются на организованном рынке и базисным активом которых являются ценные бумаги, фондовые индексы или иные финансовые инструменты срочных сделок, базисным активом которых являются ценные бумаги или фондовые индексы, уменьшающая налоговую базу по операциям с ценными бумагами, обращающимися на организованном рынке ценных бумаг');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,22,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'209');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'Сумма убытка по операциям с финансовыми инструментами срочных сделок? которые обращаются на организованном рынке и базисным активом которых не являются ценные бумаги, фондовые индексы или иные финансовые инструменты срочных сделок, базисным активом которых являются ценные бумаги или фондовые индексы, уменьшающая налоговую базу по операциям с финансовыми инструментами срочных сделок? которые обращаются на организованном рынке ценных бумаг');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,23,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'210');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'Сумма убытка по операциям с финансовыми инструментами срочных сделок, обращающимися на организованном рынке ценных бумаг и базисным активом которых являются ценные бумаги, фондовые индексы или иные финансовые инструменты срочных сделок, базисным активом которых являются ценные бумаги или фондовые индексы, уменьшающая налоговую базу по операциям с финансовыми инструментами срочных сделок, которые обращаются на организованном рынке ценных бумаг');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,24,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'211');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'Расходы, в виде процентов по займу, произведенные по совокупности операций РЕПО');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,25,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'213');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'Расходы по операциям, связанным с закрытием короткой позиции, и затраты, связанные с приобретением и реализацией ценных бумаг, являющимся объектом операций РЕПО');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,26,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'215');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'Расходы в виде процентов, уплаченных в налоговом периоде по совокупности договоров займа');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,27,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'216');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'Сумма превышения расходов в виде процентов, уплаченных по совокупности договоров займа над доходами, полученными по совокупности договоров займа, уменьшающая налоговую базу по операциям с ценными бумагами, обращающимися на организованном рынке ценных бумаг, рассчитанная в соответствии с пропорцией, с учетом положений абзаца шестого пункта 5 статьи 214.4 Налогового кодекса Российской Федерации');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,28,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'217');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'Сумма превышения расходов в виде процентов, уплаченных по совокупности договоров займа над доходами, полученными по совокупности договоров займа, уменьшающая налоговую базу по операциям с ценными бумагами, не обращающимися на организованном рынке ценных бумаг, рассчитанная в соответствии с пропорцией, с учетом положений абзаца шестого пункта 5 статьи 214.4 Налогового кодекса Российской Федерации');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,29,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'218');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'Процентный (купонный) расход, признаваемый налогоплательщиком в случае открытия короткой позиции по ценным бумагам, обращающимся на организованном рынке ценных бумаг, по которым предусмотрено начисление процентного (купонного) дохода');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,30,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'219');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'Процентный (купонный) расход, признаваемый налогоплательщиком в случае открытия короткой позиции по ценным бумагам, не обращающимся на организованном рынке ценных бумаг, по которым предусмотрено начисление процентного (купонного) дохода');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,31,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'220');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'Суммы расходов по операциям с финансовыми инструментами срочных сделок, не обращающимися на организованном рынке ценных бумаг');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,32,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'221');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'Суммы расходов по операциям с ценными бумагами, учитываем на индивидуальном инвестиционном счете');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,33,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'222');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'Сумма убытка по операциям РЕПО, принимаемого в уменьшение доходов по операциям с ценными бумагами, обращающимися на организованном рынке ценных бумаг, в пропорции, рассчитанной как отношение стоимости ценных бумаг, являющихся объектом операций РЕПО, обращающихся на организованном рынке ценных бумаг, к общей стоимости ценных бумаг, являющихся объектом операций РЕПО');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,34,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'223');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'Сумма убытка по операциям РЕПО, принимаемого в уменьшение доходов по операциям с ценными бумагами, не обращающимися на организованном рынке ценных бумаг, в пропорции, рассчитанной как отношение стоимости ценных бумаг, являющихся объектом операций РЕПО, не обращающихся на организованном рынке ценных бумаг, к общей стоимости ценных бумаг, являющихся объектом операций РЕПО');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,35,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'224');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'Сумма отрицательного финансового результата, полученного в налоговом периоде по операциям с ценными бумагами, обращающимися на организованном рынке ценных бумаг, уменьшающего финансовый результат, полученный в налоговом периоде по отдельным операциям с ценными бумагами, не обращающимися на организованном рынке ценных бумаг, которые на момент их приобретения относились к ценным бумагам, обращающимся на организованном рынке ценных бумаг');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,36,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'601');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'Сумма, уменьшающая налоговую базу по доходам в виде дивидендов');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,37,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'617');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'Вычет в сумме доходов, полученных по операциям, учитываемым на индивидуальном инвестиционном счете');

insert into REF_BOOK_RECORD(ID, RECORD_ID, REF_BOOK_ID, VERSION, STATUS) values (seq_ref_book_record.nextval,38,350,to_date('01.01.2012','dd.mm.yyyy'),0);
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3501,'618');
	insert into REF_BOOK_VALUE(RECORD_ID, ATTRIBUTE_ID, STRING_VALUE) values(seq_ref_book_record.currval,3502,'Вычет в сумме положительного финансового результата, полученного налогоплательщиком в налоговом периоде от реализации (погашения) ценных бумаг, обращающихся на организованном рынке ценных бумаг по подпункту 1 пункта 1 статьи 219.1 Налогового кодекса Российской Федерации');

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
	
COMMIT;
EXIT;	
