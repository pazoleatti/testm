DECLARE
	v_id number;
BEGIN
	delete from ref_book_attribute where ref_book_id in (select id from ref_book where table_name in ('REF_BOOK_FOND','REF_BOOK_FOND_DETAIL'));
	delete from ref_book where table_name in ('REF_BOOK_FOND','REF_BOOK_FOND_DETAIL');

	--SBRFNDFL-2142
	update ref_book set is_versioned=0 where table_name='REPORT_PERIOD_TYPE';

	update ref_book_attribute set attribute_id = (select id from ref_book_attribute where ref_book_id = 904 and alias = 'RECORD_ID') where ref_book_id = 905 and alias = 'PERSON_ID';

	select min(record_id) into v_id from ref_book_income_type where code='2791';
	update ref_book_income_type set record_id=v_id where code='2791';
	
	update ref_book_income_type set version=to_date('26.12.2016','DD.MM.YYYY') where status=2 and code in ('1543','2791');
	
	--https://jira.aplana.com/browse/SBRFNDFL-1382 Исправить наполнение справочника REF_BOOK_TARIFF_PAYER. Нужно удалить таблицу
	delete from ref_book_attribute where ref_book_id=938;
	delete from ref_book where table_name='REF_BOOK_TARIFF_PAYER';
	
	merge into ref_book_attribute a using
	(select 9007 as id, 900 as ref_book_id, 'Приоритет АСНУ' as name, 'PRIORITY' as alias, 2 as type, 5 as ord, 1 as visible, 20 as width,
		1 as required, 0 as is_unique, 0 as read_only, 0 as precision, 4 as max_length from dual) b
	on (a.id=b.id)
	when not matched then
		insert (id, ref_book_id, name, alias, type, ord, visible, width, required, is_unique, read_only, precision, max_length)
		values (b.id, b.ref_book_id, b.name, b.alias, b.type, b.ord, b.visible, b.width, b.required, b.is_unique, b.read_only, b.precision, b.max_length);
		
	update ref_book set table_name='SEC_ROLE_NDFL' where id=95;
	
	update ref_book_asnu set priority=case when code='1000' then 900
												   when code='2000' then 800
												   when code='3000' then 600
												   when code='4000' then 500
												   when code='5000' then 700
												   when code='6000' then 400
												   when code='6001' then 400
												   when code='7000' then 300
												   when code='6002' then 400
												   when code='6003' then 400
												   when code='6004' then 400
												   when code='6005' then 400
												   when code='1001' then 900
												   when code='8000' then 200
												   when code='9000' then 100
												   else 999 end;
	
	--https://jira.aplana.com/browse/SBRFNDFL-3524 Обновить данные справочников кодов видов дохода и вычетов по НДФЛ
	update ref_book_income_type set name='Проценты (за исключением процентов по облигациям с ипотечным покрытием, эмитированным до 01.01.2007, доходов в виде процентов, получаемых по вкладам в банках, и доходов, получаемых при погашении векселя), включая дисконт, полученный по долговому обязательству любого вида, за исключением сумм дохода в виде процента (купона), получаемого налогоплательщиком по обращающимся облигациям российских организаций, номинированным в рублях и эмитированным после 1 января 2017 года'
				where code='1011' and status=0;
				
	merge into ref_book_income_type a using
	(select to_date('24.10.2017','DD.MM.YYYY') as version, 0 as status, '2013' as code, 'Сумма компенсации за неиспользованный отпуск' as name from dual
	union all
	select to_date('24.10.2017','DD.MM.YYYY') as version, 0 as status, '2014' as code, 'Сумма выплаты в виде выходного пособия, среднего месячного заработка на период трудоустройства, компенсации руководителю, заместителям руководителя и главному бухгалтеру организации в части, превышающей в целом трехкратный размер среднего месячного заработка или шестикратный размер среднего месячного заработка для работников, уволенных из организаций, расположенных в районах Крайнего Севера и приравненных к ним местностях' as name from dual
	union all
	select to_date('24.10.2017','DD.MM.YYYY') as version, 0 as status, '2301' as code, 'Суммы штрафов и неустойки, выплачиваемые организацией на основании решения суда за несоблюдение в добровольном порядке удовлетворения требований потребителей в соответствии с Законом Российской Федерации от 07.02.1992 N 2300-1 "О защите прав потребителей"' as name from dual
	union all
	select to_date('24.10.2017','DD.MM.YYYY') as version, 0 as status, '2611' as code, 'Сумма списанного в установленном порядке безнадежного долга с баланса организации' as name from dual
	union all
	select to_date('24.10.2017','DD.MM.YYYY') as version, 0 as status, '3023' as code, 'Сумма дохода в виде процента (купона), получаемого налогоплательщиком по обращающимся облигациям российских организаций, номинированным в рублях и эмитированным после 1 января 2017 года' as name from dual) b
	on (a.code=b.code and a.name=b.name and a.status=b.status)
	when not matched then
		insert (id, record_id, version, status, code, name)
		values (seq_ref_book_record.nextval,seq_ref_book_record_row_id.nextval, b.version, b.status, b.code, b.name);
		
	merge into ref_book_deduction_type a using
	(select to_date('24.10.2017','DD.MM.YYYY') as version, 0 as status, '619' as code, 'Вычет в сумме положительного финансового результата, полученного по операциям, учитываемым на индивидуальном инвестиционном счете' as name, (select id from ref_book_deduction_mark where code='3') as deduction_mark from dual) b
	on (a.code=b.code and a.name=b.name and a.status=b.status)
	when not matched then
		insert (id, record_id, version, status, code, name, deduction_mark)
		values (seq_ref_book_record.nextval,seq_ref_book_record_row_id.nextval, b.version, b.status, b.code, b.name, b.deduction_mark);
		
	--https://jira.aplana.com/browse/SBRFNDFL-3524 Обновить данные справочников кодов видов дохода и вычетов по НДФЛ
	merge into ref_book_income_kind a using
	(select (select id from (select id,version from ref_book_income_type where code='1532' and status=0 order by version desc) where rownum=1) as income_type_id, 
	'01' as mark, 'Начисление дохода при выводе денежных средств с брокерского счёта' as name from dual
	union all
	select (select id from (select id,version from ref_book_income_type where code='1532' and status=0 order by version desc) where rownum=1) as income_type_id, 
	'02' as mark, 'Начисление дохода при выводе ценных бумаг с торгового раздела счёта депо' as name from dual
	union all
	select (select id from (select id,version from ref_book_income_type where code='1532' and status=0 order by version desc) where rownum=1) as income_type_id, 
	'03' as mark, 'Начисление дохода при расторжении договора брокерского обслуживания' as name from dual
	union all
	select (select id from (select id,version from ref_book_income_type where code='1532' and status=0 order by version desc) where rownum=1) as income_type_id, 
	'04' as mark, 'Начисление дохода по окончанию налогового периода за который начислен доход' as name from dual
	union all
	select (select id from (select id,version from ref_book_income_type where code='2003' and status=0 order by version desc) where rownum=1) as income_type_id, 
	'05' as mark, 'Процентная надбавка' as name from dual
	union all
	select (select id from (select id,version from ref_book_income_type where code='2003' and status=0 order by version desc) where rownum=1) as income_type_id, 
	'06' as mark, 'Процентная надбавка при увольнении' as name from dual
	union all
	select (select id from (select id,version from ref_book_income_type where code='2003' and status=0 order by version desc) where rownum=1) as income_type_id, 
	'07' as mark, 'Разовая премия' as name from dual
	union all
	select (select id from (select id,version from ref_book_income_type where code='2003' and status=0 order by version desc) where rownum=1) as income_type_id, 
	'08' as mark, 'Ежемесячная премия' as name from dual
	union all
	select (select id from (select id,version from ref_book_income_type where code='2003' and status=0 order by version desc) where rownum=1) as income_type_id, 
	'09' as mark, 'Ежеквартальная премия' as name from dual
	union all
	select (select id from (select id,version from ref_book_income_type where code='2003' and status=0 order by version desc) where rownum=1) as income_type_id, 
	'10' as mark, 'Премия по итогам года' as name from dual
	union all
	select (select id from (select id,version from ref_book_income_type where code='2013' and status=0 order by version desc) where rownum=1) as income_type_id, 
	'13' as mark,'Выплата дохода в денежной форме' as name from dual
	union all
	select (select id from (select id,version from ref_book_income_type where code='2014' and status=0 order by version desc) where rownum=1) as income_type_id, 
	'13' as mark, 'Выплата дохода в денежной форме' as name from dual
	union all
	select (select id from (select id,version from ref_book_income_type where code='2520' and status=0 order by version desc) where rownum=1) as income_type_id, 
	'13' as mark, 'Выплата дохода в денежной форме' as name from dual
	union all
	select (select id from (select id,version from ref_book_income_type where code='1552' and status=0 order by version desc) where rownum=1) as income_type_id, 
	'00' as mark, '-' as name from dual
	union all
	select (select id from (select id,version from ref_book_income_type where code='2301' and status=0 order by version desc) where rownum=1) as income_type_id, 
	'00' as mark, '-' as name from dual
	union all
	select (select id from (select id,version from ref_book_income_type where code='2611' and status=0 order by version desc) where rownum=1) as income_type_id, 
	'00' as mark, '-' as name from dual
	union all
	select (select id from (select id,version from ref_book_income_type where code='3023' and status=0 order by version desc) where rownum=1) as income_type_id, 
	'00' as mark, '-' as name from dual) b
	on (a.income_type_id=b.income_type_id and a.mark=b.mark)
	when not matched then
		insert (id, income_type_id, mark, name, record_id)
		values (seq_ref_book_record.nextval, b.income_type_id, b.mark, b.name, seq_ref_book_record.nextval);
	
	delete from REF_BOOK_INCOME_KIND where
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1544' and status=0 order by version desc) where rownum=1) and mark='01')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1545' and status=0 order by version desc) where rownum=1) and mark='01')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1546' and status=0 order by version desc) where rownum=1) and mark='01')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1547' and status=0 order by version desc) where rownum=1) and mark='01')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1548' and status=0 order by version desc) where rownum=1) and mark='01')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1549' and status=0 order by version desc) where rownum=1) and mark='01')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1552' and status=0 order by version desc) where rownum=1) and mark='01')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1544' and status=0 order by version desc) where rownum=1) and mark='02')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1545' and status=0 order by version desc) where rownum=1) and mark='02')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1546' and status=0 order by version desc) where rownum=1) and mark='02')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1547' and status=0 order by version desc) where rownum=1) and mark='02')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1548' and status=0 order by version desc) where rownum=1) and mark='02')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1549' and status=0 order by version desc) where rownum=1) and mark='02')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1552' and status=0 order by version desc) where rownum=1) and mark='02')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1544' and status=0 order by version desc) where rownum=1) and mark='03')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1545' and status=0 order by version desc) where rownum=1) and mark='03')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1546' and status=0 order by version desc) where rownum=1) and mark='03')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1547' and status=0 order by version desc) where rownum=1) and mark='03')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1548' and status=0 order by version desc) where rownum=1) and mark='03')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1549' and status=0 order by version desc) where rownum=1) and mark='03')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1552' and status=0 order by version desc) where rownum=1) and mark='03')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1544' and status=0 order by version desc) where rownum=1) and mark='04')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1545' and status=0 order by version desc) where rownum=1) and mark='04')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1546' and status=0 order by version desc) where rownum=1) and mark='04')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1547' and status=0 order by version desc) where rownum=1) and mark='04')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1548' and status=0 order by version desc) where rownum=1) and mark='04')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1549' and status=0 order by version desc) where rownum=1) and mark='04')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1552' and status=0 order by version desc) where rownum=1) and mark='04')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='2000' and status=0 order by version desc) where rownum=1) and mark='07')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='2000' and status=0 order by version desc) where rownum=1) and mark='08')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='2000' and status=0 order by version desc) where rownum=1) and mark='09')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='2000' and status=0 order by version desc) where rownum=1) and mark='10')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='2003' and status=0 order by version desc) where rownum=1) and mark='13')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1120' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1200' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1201' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1202' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1203' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1211' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1212' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1213' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1215' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1219' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1220' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1240' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1300' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1301' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1532' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1538' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1540' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1550' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='1553' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='2201' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='2202' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='2203' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='2204' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='2205' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='2206' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='2207' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='2208' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='2209' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='2210' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='2400' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='2510' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='2530' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='2630' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='2730' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='2761' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='3010' and status=0 order by version desc) where rownum=1) and mark='00')
				or
				(income_type_id=(select id from (select id,version from ref_book_income_type where code='3022' and status=0 order by version desc) where rownum=1) and mark='00');
	
	--https://jira.aplana.com/browse/SBRFNDFL-3544
	update ref_book set read_only=0 where id in (8, 922, 933);
	
	--https://jira.aplana.com/browse/SBRFNDFL-3025
	merge into ref_book_doc_type a using
	(select to_date('01.01.2016','DD.MM.YYYY') as version, 0 as status, '22' as code, 'Загранпаспорт гражданина Российской Федерации' as name, '4' as priority from dual
	union all
	select to_date('01.01.2016','DD.MM.YYYY') as version, 0 as status, '09' as code, 'Дипломатический паспорт' as name, '5' as priority from dual
	union all
	select to_date('01.01.2016','DD.MM.YYYY') as version, 0 as status, '28' as code, 'Служебный паспорт гражданина Российской Федерации' as name, '6' as priority from dual
	union all
	select to_date('01.01.2016','DD.MM.YYYY') as version, 0 as status, '26' as code, 'Паспорт моряка' as name, '7' as priority from dual
	union all
	select to_date('01.01.2016','DD.MM.YYYY') as version, 0 as status, '19' as code, 'Свидетельство о предоставлении временного убежища на территории Российской Федерации' as name, '15' as priority from dual
	union all
	select to_date('01.01.2016','DD.MM.YYYY') as version, 0 as status, '27' as code, 'Военный билет офицера запаса' as name, '18' as priority from dual
	union all
	select to_date('01.01.2016','DD.MM.YYYY') as version, 0 as status, '05' as code, 'Справка об освобождении из места лишения свободы' as name, '19' as priority from dual
	union all
	select to_date('01.01.2016','DD.MM.YYYY') as version, 0 as status, '81' as code, 'Свидетельство о смерти' as name, '20' as priority from dual
	union all
	select to_date('01.01.2016','DD.MM.YYYY') as version, 0 as status, '60' as code, 'Документы, подтверждающие факт регистрации по месту жительства (пребывания)' as name, '21' as priority from dual
	union all
	select to_date('01.01.2016','DD.MM.YYYY') as version, 0 as status, '61' as code, 'Свидетельство о регистрации по месту жительства' as name, '22' as priority from dual
	union all
	select to_date('01.01.2016','DD.MM.YYYY') as version, 0 as status, '62' as code, 'Вид на жительство иностранного гражданина' as name, '23' as priority from dual
	union all
	select to_date('01.01.2016','DD.MM.YYYY') as version, 0 as status, '63' as code, 'Свидетельство о регистрации по месту пребывания' as name, '24' as priority from dual
	union all
	select to_date('01.01.2016','DD.MM.YYYY') as version, 0 as status, '01' as code, 'Паспорт гражданина СССР' as name, '25' as priority from dual
	union all
	select to_date('01.01.2016','DD.MM.YYYY') as version, 0 as status, '02' as code, 'Загранпаспорт гражданина СССР' as name, '26' as priority from dual
	union all
	select to_date('01.01.2016','DD.MM.YYYY') as version, 0 as status, '04' as code, 'Удостоверение личности офицера' as name, '28' as priority from dual
	union all
	select to_date('01.01.2016','DD.MM.YYYY') as version, 0 as status, '06' as code, 'Паспорт Минморфлота' as name, '29' as priority from dual) b
	on (a.code=b.code and a.name=b.name and a.status=b.status)
	when not matched then
		insert (id, record_id, version, status, code, name, priority)
		values (seq_ref_book_record.nextval,seq_ref_book_record_row_id.nextval, b.version, b.status, b.code, b.name, b.priority);
		
	update ref_book_doc_type set priority=1
    where code='21';
	
	update ref_book_doc_type set priority=2
    where code='03';

    update ref_book_doc_type set priority=3
    where code='07';

    update ref_book_doc_type set priority=8
    where code='08';

    update ref_book_doc_type set priority=9
    where code='10';

    update ref_book_doc_type set priority=10
    where code='11';

    update ref_book_doc_type set priority=11
    where code='12';

    update ref_book_doc_type set priority=12
    where code='13';

    update ref_book_doc_type set priority=13
    where code='14';

    update ref_book_doc_type set priority=14
    where code='15';

    update ref_book_doc_type set priority=16
    where code='23';

    update ref_book_doc_type set priority=17
    where code='24';

    update ref_book_doc_type set priority=27, name='Свидетельство о предоставлении временного убежища на территории Российской Федерации (до 01.01.2013)'
    where code='18';

    update ref_book_doc_type set priority=30
    where code='91';
	
	--https://jira.aplana.com/browse/SBRFNDFL-3672 Обновить состав справочника "Коды места представления расчета"
	merge into ref_book_present_place a using
	(select 0 as status, to_date('01.01.2018','DD.MM.YYYY') as version, '124' as code, 'По месту жительства члена (главы) крестьянского (фермерского) хозяйства' as name from dual
	union all
	select 0 as status, to_date('01.01.2018','DD.MM.YYYY') as version, '214' as code, 'По месту нахождения российской организации, не являющейся крупнейшим налогоплательщиком ' as name from dual
	union all
	select 0 as status, to_date('01.01.2018','DD.MM.YYYY') as version, '215' as code, 'По месту нахождения правопреемника, не являющегося крупнейшим налогоплательщиком' as name from dual
	union all
	select 0 as status, to_date('01.01.2018','DD.MM.YYYY') as version, '216' as code, 'По месту учета правопреемника, являющегося крупнейшим налогоплательщиком' as name from dual) b
	on (a.status=b.status and a.version=b.version and a.code=b.code and a.name=b.name)
	when not matched then
	insert (id, record_id, status, version, code, name)
	values (seq_ref_book_record.nextval,seq_ref_book_record_row_id.nextval, b.status, b.version, b.code, b.name);
	
	merge into ref_book_present_place a using
	(select (select max(record_id) from ref_book_present_place where code='212' and status=0) as record_id, 2 as status, to_date('01.01.2018','DD.MM.YYYY') as version, '212' as code, 'по месту учета российской организации' as name from dual) b
	on (a.status=b.status and a.version=b.version and a.code=b.code and a.name=b.name)
	when not matched then
	insert (id, record_id, status, version, code, name)
	values (seq_ref_book_record.nextval, b.record_id, b.status, b.version, b.code, b.name);
	
	--https://jira.aplana.com/browse/SBRFNDFL-3756 В справочнике АСНУ в наименовании АС "Back Office" лишний пробел в конце
	update ref_book_asnu set name='АС "Back Office"' where code='8000';
	
	--https://jira.aplana.com/browse/SBRFNDFL-3765 Увеличить размеры полей: "Район", "Город", "Населенный пункт", "Улица" в справочнике ФЛ
	update ref_book_attribute set max_length=100 where ref_book_id=901 and alias in ('DISTRICT','CITY','LOCALITY','STREET');

END;
/