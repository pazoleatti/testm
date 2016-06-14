--https://jira.aplana.com/browse/SBRFACCTAX-15172: Код налогового органа (пром.) для МУКС
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (872,37,'Код налогового органа (пром.)','TAX_ORGAN_CODE_PROM',1,21,null,null,1,null,10,0,0,null,null,0,4);
UPDATE ref_book_attribute SET name = 'Код налогового органа (кон.)' WHERE id = 185;	

-----------------------------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-15302: 0.8.4 Классификаторы доходов/расходов: Изменить название поля "Символ ОПУ" на "Символ ОФР"
UPDATE REF_BOOK_ATTRIBUTE SET NAME = 'Символ ОФР' WHERE REF_BOOK_ID IN (27, 28) AND ALIAS = 'OPU';

--https://jira.aplana.com/browse/SBRFACCTAX-15805: Переименовать "Код ОПУ" в "Код ОФР" в отображении "Форма 102" БО
UPDATE REF_BOOK_ATTRIBUTE SET NAME = 'Код ОФР' WHERE REF_BOOK_ID = 52  AND ALIAS = 'OPU_CODE';

--https://jira.aplana.com/browse/SBRFACCTAX-15342: 1.0 Сделать неактивными и неотображаемыми справочники с классификаторами
DELETE FROM ref_book_record WHERE ref_book_id IN (29, 102);
DELETE FROM ref_book_attribute WHERE ref_book_id IN (29, 102);
DELETE FROM ref_book WHERE id IN (29, 102);
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
--https://jira.aplana.com/browse/SBRFACCTAX-15587: 1.0. Изменить названия полей в справочнике "Участники ТЦО"
update ref_book_attribute set ord = 100 + ord where ref_book_id = 520 and id not in (5210, 5211);

merge into ref_book_attribute tgt
using (select id, row_number() over (order by ord) ord from Ref_Book_Attribute where ref_book_id = 520) src
on (tgt.id = src.id)
when matched then update set tgt.ord = src.ord;

update ref_book_attribute set name = 'Дата включения в ВЗЛ' where id = 5210;
update ref_book_attribute set name = 'Дата исключения из ВЗЛ' where id = 5211;

--https://jira.aplana.com/browse/SBRFACCTAX-15650: 1.0 Участники ТЦО. Не хватает пробела в названии поля "КИО"
update ref_book_attribute set name = 'КИО (заполняется для нерезидентов)' where id = 5207;
-----------------------------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-15725: Транспорт. Доработать справочник "Средняя стоимость транспортных средств (с 2015)" (БД)
update ref_book set name = 'Средняя стоимость транспортных средств (с 2015)' where id = 218;

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) 
VALUES (2188, 218, 'Количество лет, прошедших с года выпуска', 'YOM_RANGE', 1, 7, null, null, 1, null, 10, 1, 0, null, null, 0, 120);

-- -- https://jira.aplana.com/browse/SBRFACCTAX-15963: 1.0 Справочник "Средняя стоимость транспортных средств (с 2015)". Изменить уникальность полей
UPDATE ref_book_attribute SET is_unique=1 WHERE id = 2188;

begin
	merge into ref_book_value tgt
using (
  with t as (
  select rbr.id as record_id, 2188 as attribute_id, rbv_from.number_value as a, rbv_to.number_value as b
  from ref_book_record rbr
  left join ref_book_value rbv_from on rbv_from.record_id = rbr.id and rbv_from.attribute_id = 2186
  left join ref_book_value rbv_to on rbv_to.record_id = rbr.id and rbv_to.attribute_id = 2187
  where rbr.ref_book_id = 218)
  select record_id, attribute_id,
         case when a=0 and b<>0 then 'не более '||b||' '||'лет'
              when a<>0 and b<>0 then 'от '||a||' до '||b||' лет' end as string_value 
  from t
  where a is not null and b is not null
       ) src
on (tgt.attribute_id = src.attribute_id and tgt.record_id = src.record_id)
when matched then
     update set tgt.string_value = src.string_value where tgt.string_value <> src.string_value
when not matched then
     insert (tgt.record_id, tgt.attribute_id, tgt.string_value) values (src.record_id, src.attribute_id, src.string_value);       
	 
dbms_output.put_line('REFBOOK (Transport YOM Range): '||sql%rowcount||' rows merged');
	 
end;	 
/
delete from ref_book_value where attribute_id in (2186, 2187);	 
delete from ref_book_attribute where id in (2186, 2187);
-----------------------------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-15863: 1.0 ТН. Сделать поле "Код ТС" справочным в справочнике

SET SERVEROUTPUT ON SIZE 100000;
BEGIN
merge into ref_book_value tgt
using ( select rbv.record_id, rbv.attribute_id, translate(rbv.string_value, '?', '0') as new_string_value, rfr.ref_book_record_id
        from ref_book_value rbv
        join ref_book_attribute a on a.id = rbv.attribute_id and a.type = 1
        join ref_book_record rbr on rbr.id = rbv.record_id
        left join (
             select rbv.record_id as ref_book_record_id, rbv.string_value, rbr.version, lead(rbr.version) over (partition by rbr.record_id order by version) - interval '1' day as end_version  
              from ref_book_value rbv
              join ref_book_record rbr on rbv.record_id = rbr.id
              where rbv.attribute_id = 422 and rbr.status <> -1) rfr on trim(rfr.string_value) =  trim(translate(rbv.string_value, '?', '0')) and rbr.version between rfr.version and nvl(rfr.end_version, to_date('31.12.9999', 'DD.MM.YYYY')) 
        where rbv.attribute_id = 411) src
on (src.record_id = tgt.record_id and src.attribute_id = tgt.attribute_id)        
when matched then
     update set tgt.reference_value = src.ref_book_record_id;

dbms_output.put_line('REFBOOK(41): '||sql%rowcount);  

update ref_book_attribute set type=4, reference_id = 42, attribute_id = 422, max_length = null where id = 411;     
update ref_book_value set string_value = null where attribute_id = 411; 
     
END;
/ 

-----------------------------------------------------------------------------------------------
COMMIT;
EXIT;