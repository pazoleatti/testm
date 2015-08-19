--http://jira.aplana.com/browse/SBRFACCTAX-11881: Удаление всех текущих блокировок
delete from lock_data;

--http://jira.aplana.com/browse/SBRFACCTAX-12347: Удаление задачи "Загрузка ТФ с локального компьютера" из списка асинхронных задач 
delete from async_task_type where id=12;

--http://jira.aplana.com/browse/SBRFACCTAX-12360: 0.7 Параметры асинхронных заданий. Переименовать "Загрузка ТФ налоговой формы из каталога загрузки" в "Обработка ТФ налоговой формы/справочника из каталога загрузки"
update async_task_type set name = 'Обработка ТФ налоговой формы/справочника из каталога загрузки' where id = 13;

--http://jira.aplana.com/browse/SBRFACCTAX-11881: DECLARATION_TYPE.ID = 7 переименовать с "Декларация по НДС (короткая, раздел 1-7) " на "Декларация по НДС (аудит, раздел 1-7)
UPDATE declaration_type SET name = 'Декларация по НДС (аудит, раздел 1-7)' WHERE id = 7;

--http://jira.aplana.com/browse/SBRFACCTAX-11977: В справочнике "Тип подразделений" значение "Пустой" переименовать в "Прочие"
update department_type set name='Прочие' where id = 5;

--http://jira.aplana.com/browse/SBRFACCTAX-12131: Новый вид налога E
insert into tax_type (id, name) values ('E', 'Эффективная налоговая ставка');

--http://jira.aplana.com/browse/SBRFACCTAX-12177: Добавить в справочник "Коды, определяющие налоговый (отчётный) период"(id=8) новый атрибут для ЭНС
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (3000,8,'Принадлежность к ЭНС','E',2,7,null,null,0,0,10,0,0,null,6,0,1);

merge into ref_book_value tgt
using (
  select rbr.id as record_id, 3000 as attribute_id, case when rbv.string_value in ('21', '22', '23', '24') then 1 else 0 end as number_value 
  from ref_book_record rbr
  join ref_book_value rbv on rbv.record_id = rbr.id and rbv.attribute_id = 25) src
on (tgt.record_id = src.record_id and tgt.attribute_id = src.attribute_id) 
when matched then
     update set tgt.number_value = src.number_value
when not matched then 
     insert (tgt.record_id, tgt.attribute_id, tgt.number_value) values (src.record_id, src.attribute_id, src.number_value); 	 
	 
--http://jira.aplana.com/browse/SBRFACCTAX-12299: Новые атрибуты для формы настроек подразделения НДС и Налог на прибыль 
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (871,98,'Код налогового органа (пром.)','TAX_ORGAN_CODE_PROM',1,24,null,null,1,null,10,0,0,null,null,0,4);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3324,330,'Код налогового органа (пром.)','TAX_ORGAN_CODE_PROM',1,23,null,null,1,null,10,0,0,null,null,0,4);
UPDATE ref_book_attribute SET name = 'Код налогового органа (кон.)' WHERE id in (3304, 853);	 

--http://jira.aplana.com/browse/SBRFACCTAX-12358: 0.7 Справочник "Классификатор расходов", "Классификатор доходов". Изменить названия полей с ОАО на ПАО и отредактировать записи справочника
update ref_book_attribute set name = 'Учётное подразделение Центрального аппарата ПАО «Сбербанк России»' where ref_book_id in (27, 28) and alias = 'UNIT';

set serveroutput on size 30000;
begin
merge into ref_book_value tgt
using (
select record_id, rba.id as attribute_id, string_value from (
with t as (
    select '11360' as CODE, '70601' as BALANCE_ACCOUNT, '16203.26' as OPU, '--------------------' as NORMATIVE_DOCUMENT, 'ПЦП МСЦ' as UNIT, 'Выписка, М/О' as BASIC_DOCUMENT, 'РНУ-4' as FORM, 'Доходы от реализации' as TYPE from dual
    union all
    select '11360' as CODE, '70601' as BALANCE_ACCOUNT, '16203.27' as OPU, '--------------------' as NORMATIVE_DOCUMENT, 'ПЦП МСЦ' as UNIT, 'Выписка, М/О' as BASIC_DOCUMENT, 'РНУ-4' as FORM, 'Доходы от реализации' as TYPE from dual
    union all
    select '11360' as CODE, '70601' as BALANCE_ACCOUNT, '16203.28' as OPU, '--------------------' as NORMATIVE_DOCUMENT, 'ПЦП МСЦ' as UNIT, 'Выписка, М/О' as BASIC_DOCUMENT, 'РНУ-4' as FORM, 'Доходы от реализации' as TYPE from dual
    union all
    select '11370' as CODE, '70601' as BALANCE_ACCOUNT, '16306.05' as OPU, '--------------------' as NORMATIVE_DOCUMENT, 'УУОГРиО ПЦП МСЦ' as UNIT, 'Выписка, М/О' as BASIC_DOCUMENT, 'РНУ-4' as FORM, 'Доходы от реализации' as TYPE from dual
    union all
    select '13925' as CODE, '70613' as BALANCE_ACCOUNT, '16101.50' as OPU, '--------------------' as NORMATIVE_DOCUMENT, 'УУОГРиО ПЦП МСЦ' as UNIT, 'Выписка, Распоряжение по сделке' as BASIC_DOCUMENT, 'РНУ-4' as FORM, 'Внереализационные доходы' as TYPE from dual
	),
rbvdata as (
      select record_id, code, balance_account, opu 
        from (
        select rbv.record_id, rbv.attribute_id, rbv.string_value 
        from ref_book_value rbv
        join ref_book_record rbr on rbr.id = rbv.record_id and rbr.ref_book_id = 28 and rbv.attribute_id in (140, 143, 144)) d
        pivot (max(string_value) for attribute_id in (140 as CODE, 143 as BALANCE_ACCOUNT, 144 as OPU)))
  select d.record_id, md.* 
  from t md
  left join rbvdata d on md.code = d.code and md.balance_account = d.balance_account and md.opu = d.opu 
  )
  UNPIVOT (string_value for attribute_code in (CODE, BALANCE_ACCOUNT, OPU, NORMATIVE_DOCUMENT, UNIT, BASIC_DOCUMENT, FORM, TYPE))
  join ref_book_attribute rba on rba.ref_book_id = 28 and attribute_code = rba.alias 
  where record_id is not null) src
on (tgt.attribute_id = src.attribute_id and tgt.record_id = src.record_id)
when matched then
     update set tgt.string_value = src.string_value;
  
dbms_output.put_line('INCOME: '||sql%rowcount||' rows merged ');


merge into ref_book_value tgt
using (
select record_id, rba.id as attribute_id, string_value from (
with t as (
    select '20490' as CODE, '70606' as BALANCE_ACCOUNT, '26101.07' as OPU, '--------------------' as NORMATIVE_DOCUMENT, 'УУВО ПЦП МСЦ' as UNIT, 'Выписка, М/О, Расчетная ведомость, Табель' as BASIC_DOCUMENT, 'РНУ-5, РНУ-7' as FORM, 'Расходы от реализации' as TYPE from dual
    union all
    select '21400' as CODE, '70606' as BALANCE_ACCOUNT, '26412.38' as OPU, '--------------------' as NORMATIVE_DOCUMENT, 'УУВО ПЦП МСЦ' as UNIT, 'Выписка, Договор, Акт, М/О' as BASIC_DOCUMENT, 'РНУ-5, РНУ-7' as FORM, 'Расходы от реализации' as TYPE from dual
    ),
rbvdata as (
      select record_id, code, balance_account, opu 
        from (
        select rbv.record_id, rbv.attribute_id, rbv.string_value 
        from ref_book_value rbv
        join ref_book_record rbr on rbr.id = rbv.record_id and rbr.ref_book_id = 27 and rbv.attribute_id in (130, 133, 134)) d
        pivot (max(string_value) for attribute_id in (130 as CODE, 133 as BALANCE_ACCOUNT, 134 as OPU)))
  select d.record_id, md.* 
  from t md
  left join rbvdata d on md.code = d.code and md.balance_account = d.balance_account and md.opu = d.opu 
  )
  UNPIVOT (string_value for attribute_code in (CODE, BALANCE_ACCOUNT, OPU, NORMATIVE_DOCUMENT, UNIT, BASIC_DOCUMENT, FORM, TYPE))
  join ref_book_attribute rba on rba.ref_book_id = 27 and attribute_code = rba.alias 
  where record_id is not null) src
on (tgt.attribute_id = src.attribute_id and tgt.record_id = src.record_id)
when matched then
     update set tgt.string_value = src.string_value;
  
dbms_output.put_line('EXPENSES(1): '||sql%rowcount||' rows merged ');

merge into ref_book_value tgt
using (
select record_id, rba.id as attribute_id, string_value from (
with t as (
    select '20490' as CODE, '60348.04' as BALANCE_ACCOUNT,  '--------------------' as NORMATIVE_DOCUMENT, 'УУВО ПЦП МСЦ' as UNIT, 'Выписка, М/О, Расчетная ведомость, Табель' as BASIC_DOCUMENT, 'РНУ-5, РНУ-7' as FORM, 'Расходы от реализации' as TYPE from dual
    ),
rbvdata as (
      select record_id, code, balance_account
        from (
        select rbv.record_id, rbv.attribute_id, rbv.string_value 
        from ref_book_value rbv
        join ref_book_record rbr on rbr.id = rbv.record_id and rbr.ref_book_id = 27 and rbv.attribute_id in (130, 133)) d
        pivot (max(string_value) for attribute_id in (130 as CODE, 133 as BALANCE_ACCOUNT)))
  select d.record_id, md.* 
  from t md
  left join rbvdata d on md.code = d.code and md.balance_account = d.balance_account
  )
  UNPIVOT (string_value for attribute_code in (CODE, BALANCE_ACCOUNT, NORMATIVE_DOCUMENT, UNIT, BASIC_DOCUMENT, FORM, TYPE))
  join ref_book_attribute rba on rba.ref_book_id = 27 and attribute_code = rba.alias 
  where record_id is not null) src
on (tgt.attribute_id = src.attribute_id and tgt.record_id = src.record_id)
when matched then
     update set tgt.string_value = src.string_value;
  
dbms_output.put_line('EXPENSES(2): '||sql%rowcount||' rows merged ');

end;
/

--Удалить двойные пробелы в подразделениях
UPDATE department SET NAME = 'Отдел сопровождения операций с ценными бумагами - корпоративные облигации' WHERE 
  name = 'Отдел сопровождения операций с ценными бумагами -  корпоративные облигации';
UPDATE department SET SHORTNAME = 'Отдел сопровождения операций с ценными бумагами - корпоративные облигации' WHERE 
  shortname = 'Отдел сопровождения операций с ценными бумагами -  корпоративные облигации';
UPDATE department SET NAME = 'Балашихинское отделение №8038 ПАО "Сбербанк России"' WHERE 
  name LIKE 'Балашихинское отделение  №8038 % "Сбербанк России"';
UPDATE department SET SHORTNAME = 'Балашихинское отделение №8038 ПАО "Сбербанк России"' WHERE 
  shortname LIKE 'Балашихинское отделение  №8038 % "Сбербанк России"';
  
UPDATE form_type SET name = 'Расчет налога на прибыль организаций с доходов, удерживаемого налоговым агентом (источником выплаты доходов)' WHERE id = 10070;  

COMMIT;
EXIT;