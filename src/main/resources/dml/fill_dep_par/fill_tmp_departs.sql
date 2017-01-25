-- Промежуточная таблица подразделений для импорта настроек
-- скрипт выполняется первым
create table tmp_depart
(
  code varchar2(4 char),
  name varchar2(100 char),
  dep_id number
);

--truncate table TMP_DEPART;
insert into TMP_DEPART (code, name) values ('ББ', 'Байкальский банк');
insert into TMP_DEPART (code, name) values ('ВВБ', 'Волго-Вятский банк');
insert into TMP_DEPART (code, name) values ('ДВБ', 'Дальневосточный банк');
insert into TMP_DEPART (code, name) values ('ЗСБ', 'Западно-Сибирский банк');
insert into TMP_DEPART (code, name) values ('ЗУБ', 'Западно-Уральский банк');
insert into TMP_DEPART (code, name) values ('МБ', 'Московский банк');
insert into TMP_DEPART (code, name) values ('ПБ', 'Поволжский банк');
insert into TMP_DEPART (code, name) values ('СЕВ', 'Северный банк');
insert into TMP_DEPART (code, name) values ('СЗБ', 'Северо-Западный банк');
insert into TMP_DEPART (code, name) values ('СИБ', 'Сибирский банк');
insert into TMP_DEPART (code, name) values ('СРБ', 'Среднерусский банк');
insert into TMP_DEPART (code, name) values ('УБ', 'Уральский банк');
insert into TMP_DEPART (code, name) values ('ЦА', 'Центральный аппарат ПАО Сбербанк');
insert into TMP_DEPART (code, name) values ('ЦЧБ', 'Центрально-Черноземный банк');
insert into TMP_DEPART (code, name) values ('ЮЗБ', 'Юго-Западный банк');
commit;

-- определяем id подразделений
merge into tmp_depart tab
using (select t.code,t.name,d.id dep_id
         from tmp_depart t left join department d on (replace(d.name,'ё','е')=trim(t.name))) val
  on (tab.code=val.code)
when matched then update set tab.dep_id=val.dep_id;