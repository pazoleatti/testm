declare
	v_run_condition number(1);
begin	
--truncate table TMP_DEPART;
insert into TMP_DEPART (name) values ( 'Байкальский банк');
insert into TMP_DEPART (name) values ( 'Волго-Вятский банк');
insert into TMP_DEPART (name) values ( 'Дальневосточный банк');
insert into TMP_DEPART (name) values ('Западно-Сибирский банк');
insert into TMP_DEPART (name) values ( 'Западно-Уральский банк');
insert into TMP_DEPART (name) values ( 'Московский банк');
insert into TMP_DEPART (name) values ('Поволжский банк');
insert into TMP_DEPART (name) values ( 'Северный банк');
insert into TMP_DEPART (name) values ( 'Северо-Западный банк');
insert into TMP_DEPART (name) values ( 'Сибирский банк');
insert into TMP_DEPART (name) values ( 'Среднерусский банк');
insert into TMP_DEPART (name) values ( 'Уральский банк');
insert into TMP_DEPART (name) values ('Центральный аппарат');
insert into TMP_DEPART (name) values ('Центрально-Черноземный банк');
insert into TMP_DEPART (name) values ('Юго-Западный банк');

-- определяем id подразделений
merge into tmp_depart t
using (
select t.name,d.id dep_id
  from tmp_depart t left join department d on upper(replace(d.name,'ё','е'))=upper(t.name)
      ) v
on (t.name=v.name)
when matched then update set t.dep_id=v.dep_id;

-- чтобы  убедиться, что все подразделения нашлись
EXECUTE IMMEDIATE 'alter table tmp_depart modify dep_id not null';

-- для корректного повторного запуска
select decode(count(*),0,1,0) into v_run_condition from user_tables where lower(table_name)='ref_book_ndfl';
IF v_run_condition=1 THEN
	EXECUTE IMMEDIATE 'alter table ref_book_ndfl_old rename to ref_book_ndfl';
END IF;

-- для корректного повторного запуска
select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_ndfl_detail' and lower(column_name)='row_ord';
IF v_run_condition=1 THEN
	EXECUTE IMMEDIATE 'alter table ref_book_ndfl_detail add row_ord number(9)';
END IF;  

-- для корректного повторного запуска
select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_ndfl_detail' and lower(column_name)='ref_book_ndfl_id';
IF v_run_condition=1 THEN
	EXECUTE IMMEDIATE 'alter table ref_book_ndfl_detail add ref_book_ndfl_id number(18)';
END IF;  

end;
/	
commit;