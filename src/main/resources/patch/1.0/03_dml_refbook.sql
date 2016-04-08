--https://jira.aplana.com/browse/SBRFACCTAX-15172: Код налогового органа (пром.) для МУКС
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (872,37,'Код налогового органа (пром.)','TAX_ORGAN_CODE_PROM',1,21,null,null,1,null,10,0,0,null,null,0,4);
UPDATE ref_book_attribute SET name = 'Код налогового органа (кон.)' WHERE id = 185;	

-----------------------------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-15302: 0.8.4 Классификаторы доходов/расходов: Изменить название поля "Символ ОПУ" на "Символ ОФР"
UPDATE REF_BOOK_ATTRIBUTE SET NAME = 'Символ ОФР' WHERE REF_BOOK_ID IN (27, 28) AND ALIAS = 'OPU';

UPDATE REF_BOOK_ATTRIBUTE SET NAME = 'Символ ОФР' WHERE REF_BOOK_ID = 29  AND ALIAS = 'OPU';

UPDATE REF_BOOK SET NAME = 'Классификатор соответствия кодов операций налоговой формы 724.2.1 по НДС символам ОФР' WHERE ID = 102;
UPDATE REF_BOOK_ATTRIBUTE SET NAME = 'Символ ОФР' WHERE REF_BOOK_ID = 102 AND ALIAS = 'OPU';

UPDATE REF_BOOK_ATTRIBUTE SET NAME = 'Код ОФР' WHERE REF_BOOK_ID = 52  AND ALIAS = 'OPU_CODE';

-----------------------------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-15305: 1.0 БД. Добавить справочник "Коды валют и драгоценных металлов"
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (542,'Коды валют и драгоценных металлов',1,0,1,null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5421,542,'Код','CODE',1,1,null,null,1,null,10,1,1,null,null,0,3);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5422,542,'Наименование','NAME',1,2,null,null,1,null,10,1,0,null,null,0,255);
-----------------------------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-15018: 1.0. ТЦО. Реализовать справочник "Вид корректировки"

INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (540,'Вид корректировки',1,0,0,null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5401, 540, 'Код', 		'CODE', 2, 1, null, null, 1, 0, 	5, 1, 1, 1, 	null, 0, 1);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5402, 540, 'Название корректировки', 	'NAME', 1, 2, null, null, 1, null, 	20, 0, 2, null, null, 0, 255);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 540, to_date('01.01.2015', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5401, 1);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5402, 'Самостоятельная корректировка');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 540, to_date('01.01.2015', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5401, 2);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5402, 'Симметричная корректировка');
	
insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 540, to_date('01.01.2015', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5401, 3);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5402, 'Обратная корректировка');

-----------------------------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-15019: 1.0. ТЦО. Реализовать справочник "Коды основания отнесения сделки к контролируемой"
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (541,'Коды основания отнесения сделки к контролируемой',1,0,0,null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5411, 541, 'Код', 		'CODE', 1, 1, null, null, 1, null, 	5, 1, 1, 1, 	null, 0, 3);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5412, 541, 'Наименование', 	'NAME', 1, 2, null, null, 1, null, 	20, 1, 0, null, null, 0, 1000);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 541, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
 	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5411, '121');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5412, 'Сделка между взаимозависимыми лицами');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 541, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
 	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5411, '122');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5412, 'Сделка в области внешней торговли товарами мировой биржевой торговли');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 541, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
 	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5411, '123');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5412, 'Сделки, одной из сторон которых является лицо, местом регистрации, либо местом жительства, либо местом налогового резидентства которого являются государство или территория, включенные в утверждаемый Министерством финансов Российской Федерации перечень государств и территорий, предоставляющих льготный режим налогообложения и (или) не предусматривающих раскрытия и предоставления информации при проведении финансовых операций');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 4, 541, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
 	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5411, '124');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5412, 'Совокупность сделок по реализации (перепродаже) товаров (выполнению работ, оказанию услуг), совершаемых с участием (при посредничестве) лиц, не являющихся взаимозависимыми (с учетом особенностей, предусмотренных этим подпунктом)');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 5, 541, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
 	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5411, '131');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5412, 'Сумма доходов по сделкам (сумма цен сделок) между взаимозависимыми лицами за соответствующий календарный год превышает 1 млрд. рублей, за 2012 год - 3 млрд. рублей, за 2013 - 2 млрд. рублей');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 6, 541, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
 	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5411, '132');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5412, 'Хотя бы одна из сторон сделки между взаимозависимыми лицами является налогоплательщиком налога на добычу полезных ископаемых, исчисляемого по налоговой ставке, установленной в процентах, и предметом сделки является добытое полезное ископаемое, признаваемое для указанной стороны сделки объектом налогообложения налогом на добычу полезных ископаемых, при добыче которого налогообложение производится по налоговой ставке, установленной в процентах');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 7, 541, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
 	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5411, '133');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5412, 'Хотя бы одна из сторон сделки между взаимозависимыми лицами является налогоплательщиком, применяющим единый сельскохозяйственный налог или систему налогообложения в виде единого налога на вмененный доход для отдельных видов деятельности (если соответствующая сделка заключена в рамках такой деятельности), при этом в числе других лиц, являющихся сторонами указанной сделки, есть лицо, не применяющее указанные специальные налоговые режимы');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 8, 541, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
 	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5411, '134');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5412, 'Хотя бы одна из сторон сделки между взаимозависимыми лицами освобождена от обязанностей налогоплательщика налога на прибыль организаций или применяет к налоговой базе по указанному налогу налоговую ставку 0 процентов в соответствии с пунктов 5.1 статьи 284 Налогового кодекса Российской Федерации, при этом другая сторона (стороны) сделки не освобождена (не освобождены) от этих обязанностей и не применяет (не применяют) налоговую ставку 0 процентов по указанным обстоятельствам');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 9, 541, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
 	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5411, '135');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5412, 'Хотя бы одна из сторон сделки между взаимозависимыми лицами является резидентом особой экономической зоны, налоговый режим в которой предусматривает специальные льготы по налогу на прибыль организаций (по сравнению с общим налоговым режимом в соответствующем субъекте Российской Федерации), при этом другая сторона (стороны) сделки не является (не являются) резидентом такой особой экономической зоны');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 10, 541, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
 	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5411, '136');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5412, 'Сделка удовлетворяет одновременно следующим условиям: одна из сторон сделки является налогоплательщиком, указанным в пункте 1 статьи 275.2 Налогового кодекса Российской Федерации, и учитывает доходы (расходы) по такой сделке при определении налоговой базы по налогу на прибыль организаций в соответствии со статьей 275.2 Налогового кодекса Российской Федерации; любая другая сторона сделки не является налогоплательщиком, указанным в пункте 1 статьи 275.2 Налогового кодекса Российской Федерации, либо является налогоплательщиком, указанным в пункте 1 статьи 275.2 Налогового кодекса Российской Федерации, но не учитывает доходы (расходы) по такой сделке при определении налоговой базы по налогу на прибыль организаций в соответствии со статьей 275.2 Налогового кодекса Российской Федерации');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 11, 541, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
 	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5411, '137');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5412, 'Хотя бы одна из сторон сделки является участником регионального инвестиционного проекта, применяющим налоговую ставку по налогу на прибыль организаций, подлежащему зачислению в федеральный бюджет, в размере 0 процентов и (или) пониженную налоговую ставку по налогу на прибыль организаций, подлежащему зачислению в бюджет субъекта Российской Федерации, в порядке и на условиях, предусмотренных статьей 284.3 Налогового кодекса Российской Федерации');

-----------------------------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-15021: 1.0. ТЦО. Добавить поля InnKio и RSK в справочник "Участники ТЦО"
update ref_book_attribute set is_unique = 0 where ref_book_id = 520 and alias in ('IKKSR', 'IKSR');
insert into ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (5220,520,'InnKio','INNKIO',1,20,null,null,0,null,10,0,0,null,null,0, 10);
insert into ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (5221,520,'RSK','RSK',1,21,null,null,0,null,10,0,0,null,null,0,50);

--https://jira.aplana.com/browse/SBRFACCTAX-15169: 1.0. ТЦО. Добавить поле RS в справочник "Участники ТЦО"
insert into ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (5222,520,'RS','RS',1,22,null,null,0,null,10,0,0,null,null,0,50);

set serveroutput on size 30000;

begin
merge into ref_book_value tgt
using (
	with 
    dt as (
      select rbr.id, cast(org_code.number_value as number(1)) as ORG_CODE, rba.alias, rbv.string_value as val
      from ref_book_record rbr
      join ref_book_value org on org.record_id = rbr.id and org.attribute_id = 5203
      join ref_book_value org_code on org.reference_value = org_code.record_id and org_code.attribute_id = 5131
      join ref_book_attribute rba on rba.ref_book_id = rbr.ref_book_id and rbr.ref_book_id = 520
      join ref_book_value rbv on rbv.record_id = rbr.id and rbv.attribute_id = rba.id 
      where rbr.status <> -1 and rba.alias in ('INN', 'KIO', 'REG_NUM', 'TAX_CODE_INCORPORATION', 'SWIFT')),
    dt_pivot as (
      select id as record_id,
      case when org_code = 1 then INN when org_code = 2 and KIO is not null then KIO else null end INNKIO,
      case when org_code = 1 then null when org_code = 2 then nvl(nvl(REG_NUM, TAX_CODE_INCORPORATION), SWIFT) else null end as RSK,
      case when org_code = 1 then null when org_code = 2 then nvl(REG_NUM, SWIFT) else null end as RS
       from dt
      pivot
      (max(val) for alias in ('INN' INN, 'KIO' KIO, 'REG_NUM' REG_NUM, 'TAX_CODE_INCORPORATION' TAX_CODE_INCORPORATION, 'SWIFT' SWIFT)))
  select * 
  from dt_pivot
  unpivot (string_value for attribute_id in (INNKIO as 5220, RSK as 5221, RS as 5222))) src
on (tgt.attribute_id = src.attribute_id and tgt.record_id = src.record_id)
when matched then
     update set tgt.string_value = src.string_value where tgt.string_value <> src.string_value
when not matched then
     insert (tgt.record_id, tgt.attribute_id, tgt.string_value) values (src.record_id, src.attribute_id, src.string_value);
	 
dbms_output.put_line('REFBOOK (FTP Participants): '||sql%rowcount||' rows merged');
	 
end;	 
/

-----------------------------------------------------------------------------------------------
COMMIT;
EXIT;