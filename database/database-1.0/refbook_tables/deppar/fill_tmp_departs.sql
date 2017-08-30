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
merge into tmp_depart t
using (
select t.code,t.name,d.id dep_id
  from tmp_depart t left join department d on (upper(replace(d.name,'ё','е'))=upper(t.name) or 
                                               upper(d.shortname)=upper(t.code))
      ) v
on (t.code=v.code)
when matched then update set t.dep_id=v.dep_id;