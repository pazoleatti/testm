-- http://jira.aplana.com/browse/SBRFACCTAX-9759: справочники для формы настроек подразделений прибыли
-- http://jira.aplana.com/browse/SBRFACCTAX-10158: скрипт для копирования данных формы настройки подразделений по прибыли из ГОСБов ТБ в УНП ТБ
--------------------------------------------------------------------------------------------------------------------------
-- Структура
create table ref_book_record_backup as (select * from ref_book_record where ref_book_id = 33 and not exists(select 1 from ref_book where id = 330)); 
create table ref_book_value_backup as (select * from ref_book_value where attribute_id in (select id from ref_book_attribute where ref_book_id = 33) and not exists(select 1 from ref_book where id = 330));
create table record_mapping (old_id number(18), new_id number(18));

delete from ref_book_record where ref_book_id = 33 and not exists(select 1 from ref_book where id = 330);

INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (330,'Параметры подразделения по налогу на прибыль (таблица)',0,0,0,null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3301,330, 'Ссылка на родительскую запись', 'LINK', 4, 0, 33, 192, 0, null, 10, 1, 0, null, null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3302,330, 'Порядок следования', 'ROW_ORD', 2, 1, null, null, 0, 0, 10, 1, 0, null, null, 0, 4);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3303,330,'Код обособленного подразделения','DEPARTMENT_ID',4,2,30,161,1,null,10,0,0,null,null,0,null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3304,330,'Код налогового органа','TAX_ORGAN_CODE',1,3,null,null,1,null,10,0,0,null,null,0,4);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3305,330,'КПП','KPP',1,4,null,null,1,null,10,0,0,null,null,0,9);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3306,330,'Код места, по которому представляется документ','TAX_PLACE_TYPE_CODE',4,5,2,3,1,null,10,0,0,null,null,0,null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3307,330,'Наименование для титульного листа','NAME',1,6,null,null,1,null,100,0,0,null,null,0,1000);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3308,330,'Наименование для Приложения № 5','ADDITIONAL_NAME',1,7,null,null,1,null,100,0,0,null,null,0,1000);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3309,330,'Код вида экономической деятельности и по классификатору ОКВЭД','OKVED_CODE',4,8,34,210,1,null,10,0,0,null,null,0,null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3310,330,'Субъект Российской Федерации (код)','DICT_REGION_ID',4,9,4,9,1,null,10,0,0,null,null,0,null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3311,330,'ОКТМО','OKTMO',4,10,96,840,1,null,11,0,0,null,null,0,null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3312,330,'Номер контактного телефона','PHONE',1,11,null,null,1,null,20,0,0,null,null,0,20);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3313,330,'Обязанность по уплате налога','OBLIGATION',4,12,25,110,1,null,10,0,0,null,null,0,null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3314,330,'Признак расчёта','TYPE',4,13,26,120,1,null,10,0,0,null,null,0,null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3315,330,'Код формы реорганизации и ликвидации','REORG_FORM_CODE',4,14,5,13,1,null,10,0,0,null,null,0,null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3316,330,'ИНН реорганизованного обособленного подразделения','REORG_INN',1,15,null,null,1,null,10,0,0,null,null,0,10);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3317,330,'КПП реорганизованного обособленного подразделения','REORG_KPP',1,16,null,null,1,null,10,0,0,null,null,0,9);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3318,330,'Признак лица подписавшего документ','SIGNATORY_ID',4,17,35,212,1,null,10,0,0,null,null,0,null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3319,330,'Фамилия подписанта','SIGNATORY_SURNAME',1,18,null,null,1,null,100,0,0,null,null,0,60);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3320,330,'Имя подписанта','SIGNATORY_FIRSTNAME',1,19,null,null,1,null,100,0,0,null,null,0,60);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3321,330,'Отчество подписанта','SIGNATORY_LASTNAME',1,20,null,null,1,null,100,0,0,null,null,0,60);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3322,330,'Наименование документа, подтверждающего полномочия представителя','APPROVE_DOC_NAME',1,21,null,null,1,null,100,0,0,null,null,0,120);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3323,330,'Наименование организации-представителя налогоплательщика','APPROVE_ORG_NAME',1,22,null,null,1,null,100,0,0,null,null,0,1000);

--------------------------------------------------------------------------------------------------------------------------
-- Перенос данных

-- Удалить данные выше ТБ и ниже ГОСБов, которые не попадают под перенос
delete 
from ref_book_record_backup rbr
where exists (select * 
            from ref_book_value_backup rbv 
            where rbv.record_id = rbr.id  and 
                  rbv.attribute_id = 192 and
                  rbv.reference_value not in 
                      (select id
                      from department 
                      where level <= 2
                      start with type = 2
                      connect by prior id = parent_id));

-- 1. ТБ
insert into record_mapping
select rbr.id, seq_ref_book_record.nextval
from department d
join ref_book_value_backup rbv on rbv.attribute_id = 192 and d.type = 2 and rbv.reference_value = d.id
join ref_book_record_backup rbr on rbr.id = rbv.record_id
where (select count(*) from ref_book_attribute where ref_book_id = 33)>6;

-- -- Нетабличная часть
insert into ref_book_record
select rbr.* 
from ref_book_record_backup rbr
join record_mapping rm on rm.old_id = rbr.id;

insert into ref_book_value
select rbv.* 
from ref_book_value_backup rbv
join record_mapping rm on rm.old_id = rbv.record_id;

-- -- Табличная часть
insert into ref_book_record (id, record_id, ref_book_id, version, status)
select tb.new_id, rbr.record_id, 330 as ref_book_id, rbr.version, rbr.status
from department d
join ref_book_value_backup rbv on rbv.attribute_id = 192 and d.type = 2 and rbv.reference_value = d.id
join ref_book_record_backup rbr on rbr.id = rbv.record_id
join record_mapping tb on tb.old_id = rbr.id;

insert into ref_book_value (record_id, attribute_id, string_value, number_value, date_value, reference_value)
select tb.new_id, rba330.id, rbv33.string_value, rbv33.number_value, rbv33.date_value, rbv33.reference_value 
from ref_book_record_backup rbr33
join record_mapping tb on tb.old_id = rbr33.id
join ref_book_value_backup rbv33 on rbv33.record_id = rbr33.id
join ref_book_attribute rba33 on rba33.id = rbv33.attribute_id
join ref_book_attribute rba330 on rba330.ref_book_id = 330 and rba33.alias = rba330.alias;

--LINK
insert into ref_book_value (record_id, attribute_id, reference_value)
select new_id, 3301 as attribute_id, old_id 
from record_mapping;

--ROW_ORD
insert into ref_book_value (record_id, attribute_id, number_value)
select new_id, 3302, 0 
from record_mapping;    

delete from ref_book_record_backup where id in (select old_id from record_mapping);
delete from record_mapping;   

--------------------------------------------------------------------------------------------------------------------------
-- 2. УНП
insert into record_mapping
select rbr.id, seq_ref_book_record.nextval
from department d
join department pd on pd.id = d.parent_id
join ref_book_value_backup rbv on rbv.attribute_id = 192 and pd.type = 2 and (lower(d.name) like '%управление налогового планирования%' or lower(d.name) like '%отдел налогового планирования%') and rbv.reference_value = d.id
join ref_book_record_backup rbr on rbr.id = rbv.record_id
where (select count(*) from ref_book_attribute where ref_book_id = 33)>6;

-- -- Нетабличная часть УНП
insert into ref_book_record
select rbr.* 
from ref_book_record_backup rbr
join record_mapping rm on rm.old_id = rbr.id;

insert into ref_book_value
select rbv.* 
from ref_book_value_backup rbv
join record_mapping rm on rm.old_id = rbv.record_id;

-- -- Табличная часть УНП
insert into ref_book_record (id, record_id, ref_book_id, version, status)
select tb.new_id, rbr.record_id, 330 as ref_book_id, rbr.version, rbr.status
from department d
join department pd on pd.id = d.parent_id
join ref_book_value_backup rbv on rbv.attribute_id = 192 and pd.type = 2 and (lower(d.name) like '%управление налогового планирования%' or lower(d.name) like '%отдел налогового планирования%') and rbv.reference_value = d.id
join ref_book_record_backup rbr on rbr.id = rbv.record_id
join record_mapping tb on tb.old_id = rbr.id;

insert into ref_book_value (record_id, attribute_id, string_value, number_value, date_value, reference_value)
select tb.new_id, rba330.id, rbv33.string_value, rbv33.number_value, rbv33.date_value, rbv33.reference_value 
from ref_book_record_backup rbr33
join record_mapping tb on tb.old_id = rbr33.id
join ref_book_value_backup rbv33 on rbv33.record_id = rbr33.id
join ref_book_attribute rba33 on rba33.id = rbv33.attribute_id
join ref_book_attribute rba330 on rba330.ref_book_id = 330 and rba33.alias = rba330.alias;

--LINK
insert into ref_book_value (record_id, attribute_id, reference_value)
select new_id, 3301 as attribute_id, old_id 
from record_mapping;

--ROW_ORD
insert into ref_book_value (record_id, attribute_id, number_value)
select new_id, 3302, 0 
from record_mapping;

--------------------------------------------------------------------------------------------------------------------------

-- 3. Фиктивные УНП (чтобы не потерялись данные по подчиненным ТБ)
--Вставить данные по фиктивным УНП (т.е. данные по ГОСБ-у на период есть, а на уровне УНП нет)
insert into ref_book_record (id, record_id, ref_book_id, version, status)
select seq_ref_book_record.nextval, -id, 33 as ref_book_id, version, 0 from 
(
select distinct unp.id, rbr_gosb.version from department tb 
join department unp on tb.id = unp.parent_id and (lower(unp.name) like '%управление налогового планирования%' or lower(unp.name) like '%отдел налогового планирования%')
join department gosb on tb.id = gosb.parent_id and not (lower(gosb.name) like '%управление налогового планирования%' or lower(gosb.name) like '%отдел налогового планирования%')
join ref_book_value_backup rbv_gosb on rbv_gosb.attribute_id = 192 and rbv_gosb.reference_value = gosb.id
join ref_book_record_backup rbr_gosb on rbr_gosb.ref_book_id = 33 and rbv_gosb.record_id = rbr_gosb.id 
where tb.type=2 and 
      not exists (select 1 
                 from ref_book_value_backup rbv_unp, 
                      Ref_Book_Record_backup rbr_unp 
                 where rbv_unp.record_id = rbr_unp.id 
                       and rbr_unp.version = rbr_gosb.version 
                       and rbv_unp.attribute_id=192 
                       and rbv_unp.reference_value = unp.id)
order by 2, 1
)
where (select count(*) from ref_book_attribute where ref_book_id = 33)>6;

insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value)
select rbr.id, rba.id, null, null, null, case when rba.id = 192 then -rbr.record_id else null end 
from ref_book_record rbr
join ref_book_attribute rba on rbr.record_id<0 and rbr.ref_book_id=33 and rba.ref_book_id = 33
where (select count(*) from ref_book_attribute where ref_book_id = 33)>6;

--------------------------------------------------------------------------------------------------------------------------
-- ГОСбы
delete from record_mapping;

insert into record_mapping
select rbr.id, seq_ref_book_record.nextval
from department d
join department pd on pd.id = d.parent_id
join ref_book_value_backup rbv on rbv.attribute_id = 192 and pd.type = 2 and not(lower(d.name) like '%управление налогового планирования%' or lower(d.name) like '%отдел налогового планирования%') and rbv.reference_value = d.id
join ref_book_record_backup rbr on rbr.id = rbv.record_id
where (select count(*) from ref_book_attribute where ref_book_id = 33)>6;

insert into ref_book_record (id, record_id, ref_book_id, version, status)
select tb.new_id, rbr.record_id, 330 as ref_book_id, rbr.version, rbr.status
from department d
join department pd on pd.id = d.parent_id
join ref_book_value_backup rbv on rbv.attribute_id = 192 and pd.type = 2 and not(lower(d.name) like '%управление налогового планирования%' or lower(d.name) like '%отдел налогового планирования%') and rbv.reference_value = d.id
join ref_book_record_backup rbr on rbr.id = rbv.record_id
join record_mapping tb on tb.old_id = rbr.id;

insert into ref_book_value (record_id, attribute_id, string_value, number_value, date_value, reference_value)
select tb.new_id, rba330.id, rbv33.string_value, rbv33.number_value, rbv33.date_value,  
case when rba33.id = 192 then unp.id else rbv33.reference_value end reference_value --department_id
from ref_book_record_backup rbr33
join record_mapping tb on tb.old_id = rbr33.id
join ref_book_value_backup rbv33 on rbv33.record_id = rbr33.id
join ref_book_attribute rba33 on rba33.id = rbv33.attribute_id
join ref_book_attribute rba330 on rba330.ref_book_id = 330 and rba33.alias = rba330.alias
left join department gosb on gosb.id = rbv33.reference_value and rba33.id = 192
left join department unp on unp.parent_id = gosb.parent_id and (lower(unp.name) like '%управление налогового планирования%' or lower(unp.name) like '%отдел налогового планирования%');

--row_ord
insert into ref_book_value (record_id, attribute_id, number_value)
select m.new_id, 3302 as attribute_id, row_number() over (partition by rbr.version, rbv.reference_value order by m.new_id) 
from record_mapping m
join ref_book_value rbv on rbv.attribute_id = 3303 and rbv.record_id = m.new_id
join ref_book_record rbr on rbr.ref_book_id = 330 and rbv.record_id = rbr.id;

--link
insert into ref_book_value (record_id, attribute_id, reference_value)
select m.new_id, 3301 as attribute_id, t_rbr.id as reference_value
from record_mapping m
join ref_book_value rbv on rbv.attribute_id = 3303 and rbv.record_id = m.new_id
join ref_book_record rbr on rbr.ref_book_id = 330 and rbv.record_id = rbr.id
join ref_book_value t_rbv on t_rbv.attribute_id = 192 and t_rbv.reference_value = rbv.reference_value 
join ref_book_record t_rbr on t_rbr.id = t_rbv.record_id and t_rbr.ref_book_id = 33 and t_rbr.version = rbr.version;

---------------------------------------------------------------------------------------------------
update ref_book_record set record_id = -record_id where record_id < 0;
delete from ref_book_value where attribute_id in (select id from ref_book_attribute a where ref_book_id = 33 and alias in (select alias from ref_book_attribute b where b.ref_book_id = 330 and alias not in ('LINK', 'ORD', 'DEPARTMENT_ID')));
delete from ref_book_attribute a where ref_book_id = 33 and alias in (select alias from ref_book_attribute b where b.ref_book_id = 330 and alias not in ('LINK', 'ORD', 'DEPARTMENT_ID'));
drop table ref_book_record_backup;
drop table ref_book_value_backup;
drop table record_mapping;


----------------------------------------------------------------------------------------------------
COMMIT;
EXIT;