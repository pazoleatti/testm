--Удаление всех текущих блокировок
delete from lock_data;

--http://jira.aplana.com/browse/SBRFACCTAX-14449: Дублирование текста в значении справочника "Статус по НДС"
update ref_book_value
set string_value = 'Организация, не признаваемая налогоплательщиком по НДС, или организация, освобожденная от обязанностей налогоплательщика'
where attribute_id = 5102 and record_id in (
	select id from ref_book_record rbr
	join ref_book_value rbv on rbr.id = rbv.record_id and rbv.attribute_id = 5101 and rbv.number_value = 1
	where rbr.ref_book_id = 510);		
	
--https://jira.aplana.com/browse/SBRFACCTAX-14735: Формирование специфического отчета декларации
INSERT INTO async_task_type (id, name, handler_jndi, limit_kind, dev_mode) values (26, 'Формирование специфического отчета декларации', 'ejb/taxaccounting/async-task.jar/SpecificReportDeclarationDataAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote', 'Сумма по всем формам-источникам. Ячейки = строки * графы', 0);

--https://jira.aplana.com/browse/SBRFACCTAX-15840: 1.0 Справочники. Реализовать возможность загрузки данных из файла в справочник
INSERT INTO async_task_type (id, name, handler_jndi, limit_kind, dev_mode) values (27, 'Загрузка данных из файла в справочник', 'ejb/taxaccounting/async-task.jar/UploadRefBookAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote', 'Размер файла (Кбайт)', 0);

--https://jira.aplana.com/browse/SBRFACCTAX-15059: Обновить справочник цветов в БД в соответствии с ENUM
merge into color color_tbl
using (
  with color_enum(val) as
  (
  select 'BLACK(0, 0, 0, 0, "#000000 Чёрный")' from dual union all
  select 'WHITE(4, 255, 255, 255, "#FFFFFF Белый")' from dual union all
  select 'LIGHT_YELLOW(1, 255, 255, 153, "#FFFF99 Светло-желтый")' from dual union all
  select 'LIGHT_BROWN(2, 255, 204, 153, "#FFCC99 Светло-коричневый")' from dual union all
  select 'LIGHT_BLUE(3, 204, 255, 255, "#CCFFFF Светло-голубой")' from dual union all
  select 'DARK_GREY(5, 192, 192, 192, "#C0C0C0 Темно-серый")' from dual union all
  select 'GREY(6, 217, 217, 217, "#D9D9D9 Серый")' from dual union all
  select 'BLUE(7, 197, 225, 253, "#C5E1FD Голубой")' from dual union all
  select 'LIGHT_CORAL(8, 246, 180, 180, "#F6B4B4 Светло-красный")' from dual union all
  select 'LIGHT_ORANGE(9, 249, 199, 73, "#F9C749 Светло-оранжевый")' from dual union all
  select 'RED(10, 250, 88, 88, "#FA5858 Красный")' from dual union all
  select 'DARK_BLUE(11, 137, 198, 233, "#89C6E9 Синий")' from dual union all
  select 'PALE_GREEN(12, 143, 199, 13, "#8FC70D Светло-зеленый")' from dual union all
  select 'DARK_GREEN(13, 113, 157, 11, "#719D0B Темно-зеленый")' from dual
  ) 
  select val, 
  regexp_substr(val, '\(([0-9]+),\s([0-9]+),\s([0-9]+),\s([0-9]+),\s\"#(.{6}).*', 1,1,NULL,1) as id,  
  regexp_substr(val, '\(([0-9]+),\s([0-9]+),\s([0-9]+),\s([0-9]+),\s\"#(.{6}).*', 1,1,NULL,2) as r, 
  regexp_substr(val, '\(([0-9]+),\s([0-9]+),\s([0-9]+),\s([0-9]+),\s\"#(.{6}).*', 1,1,NULL,3) as g, 
  regexp_substr(val, '\(([0-9]+),\s([0-9]+),\s([0-9]+),\s([0-9]+),\s\"#(.{6}).*', 1,1,NULL,4) as b, 
  '#'||regexp_substr(val, '\(([0-9]+),\s([0-9]+),\s([0-9]+),\s([0-9]+),\s\"#(.{6}).*', 1,1,NULL,5) as hex,
  regexp_substr(val, '\(([0-9]+),\s([0-9]+),\s([0-9]+),\s([0-9]+),\s\"#(.{6})\s(.*)\"\)', 1,1,NULL,6) as name  
  from color_enum ) color_enum
on (color_tbl.id = color_enum.id)
when matched then
     update set color_tbl.name = color_enum.name, 
                color_tbl.r = color_enum.r, 
                color_tbl.g = color_enum.g, 
                color_tbl.b = color_enum.b, 
                color_tbl.hex = color_enum.hex;
commit;				
---------------------------------------------------------------------------------------------------------				
--https://jira.aplana.com/browse/SBRFACCTAX-15546: Замена кавычек и переносов строк

set serveroutput on size 1000000;

--Переносы строк
begin
dbms_output.put_line('SBRFACCTAX-15546: Line breaks removal');
-- -- Средняя стоимость транспортных средств (2015) / Модель (Версия)
	UPDATE ref_book_value SET    string_value = regexp_replace(string_value, chr(10), '') WHERE  attribute_id = 2183 AND regexp_replace(string_value, chr(10), '') <> string_value;
	dbms_output.put_line('ref_book_id = 218 (avg transport costs): '||sql%rowcount||' row(s) updated');

-- -- План счетов бухгалтерского учета / Наименование счета
	UPDATE ref_book_value SET string_value = trim(regexp_replace(string_value, chr(10), '')) WHERE  attribute_id = 901 AND regexp_replace(string_value, chr(10), '') <> string_value;
	dbms_output.put_line('ref_book_id = 101 (accounting plan - char 10): '||sql%rowcount||' row(s) updated');
	UPDATE ref_book_value SET string_value = trim(regexp_replace(string_value, chr(13), '')) WHERE attribute_id = 901 AND regexp_replace(string_value, chr(13), '') <> string_value;
	dbms_output.put_line('ref_book_id = 101 (accounting plan - char 13): '||sql%rowcount||' row(s) updated');

-- -- Параметры подразделения по УКС / Наименование для тит.листа + Наименование организации представителя
	UPDATE ref_book_value SET string_value = trim(regexp_replace(string_value, chr(10), '')) WHERE  attribute_id = 243 AND regexp_replace(string_value, chr(10), '') <> string_value;
	dbms_output.put_line('ref_book_id = 37 (Deals.orgname): '||sql%rowcount||' row(s) updated');
	UPDATE ref_book_value SET string_value = trim(regexp_replace(string_value, chr(10), '')) WHERE  attribute_id = 191 AND regexp_replace(string_value, chr(10), '') <> string_value;
	dbms_output.put_line('ref_book_id = 37 (Deals.titlename): '||sql%rowcount||' row(s) updated');

-- -- Параметры подразделения по налогу на прибыль (таблица) / Наименование для титульного листа
	UPDATE ref_book_value SET string_value = trim(regexp_replace(string_value, chr(10), '')) WHERE  attribute_id = 3307 AND regexp_replace(string_value, chr(10), '') <> string_value;
	dbms_output.put_line('ref_book_id = 330 (Income_params): '||sql%rowcount||' row(s) updated');
end;
/

--Подразделения (кавычки и двойные пробелы)
BEGIN
	dbms_output.put_line('Spaces');
	
	UPDATE department SET name = regexp_replace(name, '\s{2,}', ' ') WHERE  regexp_like(name, '\s{2,}');
	dbms_output.put_line('department (name): '||sql%rowcount||' row(s) updated');	
	UPDATE department SET shortname = regexp_replace(shortname, '\s{2,}', ' ') WHERE  regexp_like(shortname, '\s{2,}');
	dbms_output.put_line('department (shortname): '||sql%rowcount||' row(s) updated');
	
	dbms_output.put_line('Quotes');
	
	UPDATE department SET name = translate(name, '«»', '""') where name <> translate(name, '«»', '""');
	dbms_output.put_line('department (name): '||sql%rowcount||' row(s) updated');	
	UPDATE department SET shortname = translate(shortname, '«»', '""') where name <> translate(shortname, '«»', '""');
	dbms_output.put_line('department (shortname): '||sql%rowcount||' row(s) updated');
END;
/

--ОКТМО:
--Подразделения (кавычки и двойные пробелы)
BEGIN
	UPDATE ref_book_oktmo SET name = regexp_replace(name, '\s{2,}', ' ') WHERE regexp_like(name, '\s{2,}');
	dbms_output.put_line('oktmo (spaces): '||sql%rowcount||' row(s) updated');	

	UPDATE ref_book_oktmo SET name = translate(name, '«»“”', '""""') where name <> translate(name, '«»“”', '""""');
	dbms_output.put_line('oktmo (quotes): '||sql%rowcount||' row(s) updated');	
END;
/

BEGIN
	dbms_output.put_line('---------------------------------------------------------------------------------');
	for x in (
	  with t as (
		select attribute_id, count(*) as cnt
		from ref_book_value
		where string_value like '% %' and string_value <> translate(string_value, ' ', ' ')
		group by attribute_id)
	  select r.name||'.'||a.name||': '||t.cnt as str from t
	  join ref_book_attribute a on a.id = t.attribute_id
	  join ref_book r on r.id = a.ref_book_id
	  where r.id <> 3) loop
	  
	dbms_output.put_line(x.str);  
	end loop;  	
	dbms_output.put_line('---------------------------------------------------------------------------------');
	
	update ref_book_value set string_value = trim(translate(string_value, ' ', ' ')) where string_value like '% %' and string_value <> translate(string_value, ' ', ' ');
	dbms_output.put_line('Universal structure (nbsp): '||sql%rowcount||' row(s) updated');	
	
	dbms_output.put_line('---------------------------------------------------------------------------------');
	for x in (
	  with t as (
		select attribute_id, count(*) as cnt
		from ref_book_value
		where regexp_like(string_value, '\s{2,}')
		group by attribute_id)
	  select r.name||'.'||a.name||': '||t.cnt as str from t
	  join ref_book_attribute a on a.id = t.attribute_id
	  join ref_book r on r.id = a.ref_book_id
	  where r.id <> 3) loop
	  
	dbms_output.put_line(x.str);  
	end loop;  	
	dbms_output.put_line('---------------------------------------------------------------------------------');
	
	UPDATE ref_book_value SET string_value = trim(regexp_replace(string_value, '\s{2,}', ' ')) WHERE regexp_like(string_value, '\s{2,}') and attribute_id not in (select id from ref_book_attribute where ref_book_id = 3);
	dbms_output.put_line('Universal structure (double+ spaces): '||sql%rowcount||' row(s) updated');
	
	dbms_output.put_line('---------------------------------------------------------------------------------');
	for x in (
	  with t as (
		select attribute_id, count(*) as cnt
		from ref_book_value
		where string_value <> translate(string_value, '«»“”', '""""')
		group by attribute_id)
	  select r.name||'.'||a.name||': '||t.cnt as str from t
	  join ref_book_attribute a on a.id = t.attribute_id
	  join ref_book r on r.id = a.ref_book_id
	  where r.id <> 3) loop
	  
	dbms_output.put_line(x.str);  
	end loop;  	
	dbms_output.put_line('---------------------------------------------------------------------------------');
	
	UPDATE ref_book_value SET string_value = trim(translate(string_value, '«»“”', '""""')) WHERE string_value <> translate(string_value, '«»“”', '""""') and attribute_id not in (select id from ref_book_attribute where ref_book_id = 3);
	dbms_output.put_line('Universal structure (quotes): '||sql%rowcount||' row(s) updated');

END;
/
				
---------------------------------------------------------------------------------------------------------					
commit;
exit;