update ref_book_income_type set name='Проценты (за исключением процентов по облигациям с ипотечным покрытием, эмитированным до 01.01.2007, доходов в виде процентов, получаемых по вкладам в банках, и доходов, получаемых при погашении векселя), включая дисконт, полученный по долговому обязательству любого вида'
where code='1011' and version=to_date('01.01.2016','DD.MM.YYYY') and status=0;
			
merge into ref_book_income_type a using
(select to_date('01.01.2017','DD.MM.YYYY') as version, 0 as status, '1011' as code, 'Проценты (за исключением процентов по облигациям с ипотечным покрытием, эмитированным до 01.01.2007, доходов в виде процентов, получаемых по вкладам в банках, и доходов, получаемых при погашении векселя), включая дисконт, полученный по долговому обязательству любого вида, за исключением сумм дохода в виде процента (купона), получаемого налогоплательщиком по обращающимся облигациям российских организаций, номинированным в рублях и эмитированным после 1 января 2017 года' as name from dual
) b
on (a.code=b.code and a.name=b.name and a.status=b.status and a.version=b.version)
when not matched then
	insert (id, record_id, version, status, code, name)
	values (seq_ref_book_record.nextval,(select max(record_id) from ref_book_income_type where code='1011'), b.version, b.status, b.code, b.name);
			
update ref_book_income_type set version=to_date('01.01.2016','DD.MM.YYYY') where code in ('2002','2003');
			
update ref_book_income_type set version=to_date('01.01.2017','DD.MM.YYYY') where code in ('2013','2014','2301','2611','3023');
			
merge into ref_book_income_kind a using
(select (select id from (select id,version from ref_book_income_type where code='1544' and status=0 order by version desc) where rownum=1) as income_type_id, 
'01' as mark, 'Начисление дохода при выводе денежных средств с брокерского счёта' as name, 
to_date('26.12.2016','DD.MM.YYYY') as version from dual
union all
select (select id from (select id,version from ref_book_income_type where code='1544' and status=0 order by version desc) where rownum=1) as income_type_id, 
'02' as mark, 'Начисление дохода при выводе ценных бумаг с торгового раздела счёта депо' as name, 
to_date('26.12.2016','DD.MM.YYYY') as version from dual
union all
select (select id from (select id,version from ref_book_income_type where code='1544' and status=0 order by version desc) where rownum=1) as income_type_id, 
'03' as mark, 'Начисление дохода при расторжении договора брокерского обслуживания' as name, 
to_date('26.12.2016','DD.MM.YYYY') as version from dual
union all
select (select id from (select id,version from ref_book_income_type where code='1544' and status=0 order by version desc) where rownum=1) as income_type_id, 
'04' as mark, 'Начисление дохода по окончанию налогового периода за который начислен доход' as name, 
to_date('26.12.2016','DD.MM.YYYY') as version from dual
union all
select (select id from (select id,version from ref_book_income_type where code='1546' and status=0 order by version desc) where rownum=1) as income_type_id, 
'01' as mark, 'Начисление дохода при выводе денежных средств с брокерского счёта' as name, 
to_date('26.12.2016','DD.MM.YYYY') as version from dual
union all
select (select id from (select id,version from ref_book_income_type where code='1546' and status=0 order by version desc) where rownum=1) as income_type_id, 
'02' as mark, 'Начисление дохода при выводе ценных бумаг с торгового раздела счёта депо' as name, 
to_date('26.12.2016','DD.MM.YYYY') as version from dual
union all
select (select id from (select id,version from ref_book_income_type where code='1546' and status=0 order by version desc) where rownum=1) as income_type_id, 
'03' as mark, 'Начисление дохода при расторжении договора брокерского обслуживания' as name, 
to_date('26.12.2016','DD.MM.YYYY') as version from dual
union all
select (select id from (select id,version from ref_book_income_type where code='1546' and status=0 order by version desc) where rownum=1) as income_type_id, 
'04' as mark, 'Начисление дохода по окончанию налогового периода за который начислен доход' as name, 
to_date('26.12.2016','DD.MM.YYYY') as version from dual
union all
select (select id from (select id,version from ref_book_income_type where code='1548' and status=0 order by version desc) where rownum=1) as income_type_id, 
'01' as mark, 'Начисление дохода при выводе денежных средств с брокерского счёта' as name, 
to_date('26.12.2016','DD.MM.YYYY') as version from dual
union all
select (select id from (select id,version from ref_book_income_type where code='1548' and status=0 order by version desc) where rownum=1) as income_type_id, 
'02' as mark, 'Начисление дохода при выводе ценных бумаг с торгового раздела счёта депо' as name, 
to_date('26.12.2016','DD.MM.YYYY') as version from dual
union all
select (select id from (select id,version from ref_book_income_type where code='1548' and status=0 order by version desc) where rownum=1) as income_type_id, 
'03' as mark, 'Начисление дохода при расторжении договора брокерского обслуживания' as name, 
to_date('26.12.2016','DD.MM.YYYY') as version from dual
union all
select (select id from (select id,version from ref_book_income_type where code='1548' and status=0 order by version desc) where rownum=1) as income_type_id, 
'04' as mark, 'Начисление дохода по окончанию налогового периода за который начислен доход' as name, 
to_date('26.12.2016','DD.MM.YYYY') as version from dual
union all
select (select id from (select id,version from ref_book_income_type where code='4800' and status=0 order by version desc) where rownum=1) as income_type_id, 
'00' as mark, '-' as name, 
to_date('01.01.2016','DD.MM.YYYY') as version from dual
) b
on (a.income_type_id=b.income_type_id and a.mark=b.mark)
when not matched then
insert (id, income_type_id, mark, name, version, record_id)
values (seq_ref_book_record.nextval, b.income_type_id, b.mark, b.name, b.version, seq_ref_book_record.nextval);
	
update ref_book_deduction_type set version=to_date('01.01.2017','DD.MM.YYYY') where code='619';

update ref_book_attribute set required=1 where ref_book_id = 904 and alias = 'EMPLOYEE';

update ref_book_attribute set width = 3 where ref_book_id = 901 and alias = 'ADDRESS_TYPE';
update ref_book_attribute set width = 5 where ref_book_id = 901 and alias = 'COUNTRY_ID';
update ref_book_attribute set width = 4 where ref_book_id = 901 and alias = 'REGION_CODE';
update ref_book_attribute set width = 3 where ref_book_id = 901 and alias = 'POSTAL_CODE';
update ref_book_attribute set width = 10 where ref_book_id = 901 and alias = 'DISTRICT';
update ref_book_attribute set width = 7 where ref_book_id = 901 and alias = 'CITY';
update ref_book_attribute set width = 10 where ref_book_id = 901 and alias = 'LOCALITY';
update ref_book_attribute set width = 10 where ref_book_id = 901 and alias = 'STREET';
update ref_book_attribute set width = 6 where ref_book_id = 901 and alias = 'HOUSE';
update ref_book_attribute set width = 4 where ref_book_id = 901 and alias = 'BUILD';
update ref_book_attribute set width = 4 where ref_book_id = 901 and alias = 'APPARTMENT';

update ref_book_attribute set width = 4 where ref_book_id = 900 and alias = 'CODE';
update ref_book_attribute set width = 15 where ref_book_id = 900 and alias = 'NAME';
update ref_book_attribute set width = 4 where ref_book_id = 900 and alias = 'PRIORITY';
update ref_book_attribute set width = 7 where ref_book_id = 900 and alias = 'ROLE_ALIAS';
update ref_book_attribute set width = 20 where ref_book_id = 900 and alias = 'ROLE_NAME';
update ref_book_attribute set width = 40 where ref_book_id = 900 and alias = 'TYPE';

update ref_book_attribute set ord = 4 where ref_book_id = 921 and alias = 'DEDUCTION_MARK';
update ref_book_attribute set ord = 3 where ref_book_id = 921 and alias = 'NAME';
update ref_book_attribute set ord = 2 where ref_book_id = 921 and alias = 'DEDUCTION_MARK';
update ref_book_attribute set width = 4 where ref_book_id = 921 and alias = 'CODE';
update ref_book_attribute set width = 4 where ref_book_id = 921 and alias = 'DEDUCTION_MARK';

update ref_book_attribute set width = 4 where ref_book_id = 922 and alias = 'CODE';

update ref_book_attribute set ord = 4 where ref_book_id = 360 and alias = 'NAME';
update ref_book_attribute set ord = 2 where ref_book_id = 360 and alias = 'PRIORITY';
update ref_book_attribute set ord = 3 where ref_book_id = 360 and alias = 'NAME';
update ref_book_attribute set width = 3 where ref_book_id = 360 and alias = 'CODE';
update ref_book_attribute set width = 4 where ref_book_id = 360 and alias = 'PRIORITY';

update ref_book_attribute set ord = 7 where ref_book_id = 8 and alias = 'NAME';
update ref_book_attribute set ord = 1 where ref_book_id = 8 and alias = 'START_DATE';
update ref_book_attribute set ord = 2 where ref_book_id = 8 and alias = 'END_DATE';
update ref_book_attribute set ord = 3 where ref_book_id = 8 and alias = 'CALENDAR_START_DATE';
update ref_book_attribute set ord = 4 where ref_book_id = 8 and alias = 'NAME';
update ref_book_attribute set width = 3 where ref_book_id = 8 and alias = 'CODE';
update ref_book_attribute set width = 4 where ref_book_id = 8 and alias = 'START_DATE';
update ref_book_attribute set width = 5 where ref_book_id = 8 and alias = 'END_DATE';
update ref_book_attribute set width = 5 where ref_book_id = 8 and alias = 'CALENDAR_START_DATE';

update ref_book_attribute set ord = 7 where ref_book_id = 923 and alias = 'NAME';
update ref_book_attribute set ord = 1 where ref_book_id = 923 and alias = 'OKATO_DEFINITION';
update ref_book_attribute set ord = 2 where ref_book_id = 923 and alias = 'OKTMO';
update ref_book_attribute set ord = 3 where ref_book_id = 923 and alias = 'OKTMO_DEFINITION';
update ref_book_attribute set ord = 4 where ref_book_id = 923 and alias = 'NAME';
update ref_book_attribute set width = 3 where ref_book_id = 923 and alias = 'CODE';
update ref_book_attribute set width = 6 where ref_book_id = 923 and alias = 'OKATO_DEFINITION';
update ref_book_attribute set width = 5 where ref_book_id = 923 and alias = 'OKTMO';
update ref_book_attribute set width = 6 where ref_book_id = 923 and alias = 'OKTMO_DEFINITION';

update ref_book_attribute set width = 3 where ref_book_id = 928 and alias = 'CODE';

update ref_book_attribute set width = 6 where ref_book_id = 35 and alias = 'CODE';
            
update ref_book_attribute set width = 3 where ref_book_id = 903 and alias = 'CODE';

update ref_book_income_kind set ref_book_income_kind.VERSION=(select ref_book_income_type.version from ref_book_income_type where ref_book_income_type.id = ref_book_income_kind.income_type_id)
where exists(select 1 from ref_book_income_type where ref_book_income_type.id = ref_book_income_kind.income_type_id);

delete from ref_book_income_type where code='3010' and name='Выигрыши, выплачиваемые организаторами лотерей, тотализаторов и других основанных на риске игр (в том числе с использованием игровых автоматов)';
		
update ref_book_income_type set version=to_date('01.01.2016','DD.MM.YYYY') where code='3010';