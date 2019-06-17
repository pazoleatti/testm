-- 3.6-ytrofimov-3 https://jira.aplana.com/browse/SBRFNDFL-7175 Реализовать запись в историю изменений ОНФ информацию о замене "состояние ЭД" и прикладывании файла
-- 3.7-dnovikov-21 https://jira.aplana.com/browse/SBRFNDFL-7092 - Изменения событий

declare 
  v_task_name varchar2(128):='insert_update_delete block #1 - merge into event';  
begin
	merge into event dst using
	(select 10002 as id, 'Загрузка файла ответа ФНС' as name from dual union
	 select 1, 'Создание' from dual union
 	 select 2, 'Удаление' from dual union
	 select 5, 'Проверка' from dual union
	 select 11, 'Обновление данных ФЛ' from dual union
	 select 26, 'Изменение состояния ЭД' from dual union
	 select 106, 'Возврат в Создана' from dual union
	 select 107, 'Проверка' from dual union
	 select 109, 'Принятие' from dual union
	 select 401, 'Импорт из файла Excel' from dual union
	 select 10003, 'Идентификация ФЛ' from dual union
	 select 10004, 'Консолидация' from dual union
	 select 10005, 'Создание из ТФ xml' from dual union
	 select 10006, 'Редактирование строки РНУ НДФЛ' from dual union
	 select 10007, 'Массовое редактирование дат' from dual union
	 select 10008, 'Отправка в ЭДО' from dual union
	 select 10009, 'Выгрузка для отправки в ФНС' from dual 
	) src
	on (src.id=dst.id)
	when not matched then
		insert (id, name)
		values (src.id, src.name)
	when matched then
		update set dst.name=src.name;
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 

EXCEPTION
  when OTHERS then 
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

-- 3.6-snazin-1, 3.6-snazin-2 https://jira.aplana.com/browse/SBRFNDFL-7093 Реализовать добавление новых конфигурационных параметров для взаимодействия с ЭДО
-- 3.7-ytrofimov-9 https://jira.aplana.com/browse/SBRFNDFL-7689 Реализовать настройку для проверки корректности исчисленного налога в скрипте КНФ
-- 3.7-dnovikov-24 https://jira.aplana.com/browse/SBRFNDFL-6697 Добавление нового параметра в общих параметрах  "Замена Даты удержания налога в 6-НДФЛ" 
declare 
  v_task_name varchar2(128):='insert_update_delete block #2 - merge into configuration';  
begin
	merge into configuration dst using
	(select 'TAX_MESSAGE_RECEIPT_WAITING_TIME' as code, 0 as department_id, 30 as value from dual union
	 select 'TAX_MESSAGE_RETRY_COUNT', 0, 20 from dual union
	 select 'DOCUMENTS_SENDING_ENABLED', 0, 0 from dual union
	 select 'DOCUMENTS_RECEPTION_ENABLED', 0, 0 from dual union
	 select 'NDFL_SUBSYSTEM_ID', 0, 11 from dual union
	 select 'CALCULATED_TAX_DIFF', 0, 1 from dual union
	 select 'NDFL6_TAX_DATE_REPLACEMENT', 0, 1 from dual
	) src
	on (src.code=dst.code)
	when not matched then
		insert (code, department_id, value)
		values (src.code, src.department_id, src.value);
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

-- 3.7-dnovikov-11, 3.7-dnovikov-12 https://jira.aplana.com/browse/SBRFNDFL-6590 - удалил старый отчет
declare 
  v_task_name varchar2(128):='insert_update_delete block #3 - delete old metadata';  
begin
	delete from async_task_type where id = 35;
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||'Part1: No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||'Part1: Success');
	END CASE; 

	delete from DECL_TEMPLATE_EVENT_SCRIPT where id = 18;
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||'Part2: No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||'Part2: Success');
	END CASE; 


EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

-- 3.7-dnovikov-13 https://jira.aplana.com/browse/SBRFNDFL-7164 - Новая ассинхронная задача отправить ЭД в ЭДО
declare 
  v_task_name varchar2(128):='insert_update_delete block #4 - add new task';  
begin
	merge into async_task_type dst using
	(select 10 as id, 'Отправка ЭД в ЭДО' as name, 'SendEdoAsyncTask' as handler_bean from dual
	) src
	on (src.id=dst.id)
	when not matched then
		insert (id, name, handler_bean)
		values (src.id, src.name, src.handler_bean);

	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 

EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;


-- 3.7-snazin-1, 3.7-snazin-2, 3.7-snazin-3 https://jira.aplana.com/browse/SBRFNDFL-7494 Справочник Подсистемы АС УН отсутствует в списке справочников
-- 3.7-dnovikov-27 https://jira.aplana.com/browse/SBRFNDFL-7756 Добавить в систему "Справочник Расширяющие интервалы для загрузки данных"

declare 
  v_task_name varchar2(128):='insert_update_delete block #5 - add refs into ref_book';  
begin
	merge into ref_book dst using
	(select 910 as id, 'Подсистемы АС УН' as name, 1 as visible, 0 as type, 1 as read_only, 'VW_SUBSYSTEM_SYN' as table_name, 0 as is_versioned  from dual union
	 select 1040, 'Дополнительные интервалы для загрузки данных', 1, 0, 0,  'REPORT_PERIOD_IMPORT', 1 from dual
	) src
	on (src.id=dst.id)
	when not matched then
		insert (id, name, visible, type, read_only, table_name, is_versioned)
		values (src.id, src.name, src.visible, src.type, src.read_only, src.table_name, src.is_versioned);

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||'Part1: No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||'Part1: Success');
	END CASE; 


	merge into ref_book_attribute dst using 
	(select 9101 as id, 910 as ref_book_id, 'ID' as name, 'id' as alias, 2 as type, 1 as ord, 1 as visible, 0 as precision, 3 as width, 1 as required,
	       1 as is_unique, 1 as read_only, 19 as max_length from dual union
	select 9102, 910, 'Код', 'code', 1, 2, 1, null, 10, 1, 1, 1, 30 from dual union
	select 9103, 910, 'Наименование', 'name', 1, 3, 1, null, 50, 1, 0, 1, 100 from dual union
	select 9104, 910, 'Краткое наименование', 'short_name', 1, 4, 1, null, 30, 1, 0, 1, 30 from dual  
	) src
	on (src.id=dst.id)
 	when not matched then
		insert (id, ref_book_id, name, alias, type, ord, visible, precision, width, required, is_unique, read_only, max_length) values 
		(src.id, src.ref_book_id, src.name, src.alias, src.type, src.ord, src.visible, src.precision, src.width, src.required, src.is_unique, src.read_only, src.max_length);

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||'Part2: No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||'Part2: Success');
	END CASE; 

	merge into ref_book_attribute dst using
	(select 10401 as id, 1040 as ref_book_id, 'Код периода' as name, 'REPORT_PERIOD_TYPE_ID' as alias, 4 as type, 1 as ord, 8 as reference_id, 26 as attribute_id, null as precision, 15 as width, 1 as required, null as max_length, null as format from dual union
	select 10402, 1040, 'Дата начала интервала', 'PERIOD_START_DATE', 3, 2, null, null, null, 15, 1, null, 5 from dual union
	select 10403, 1040, 'Дата окончания интервала', 'PERIOD_END_DATE', 3, 3, null, null, null, 15, 1, null, 5 from dual union
	select 10404, 1040, 'АСНУ', 'ASNU_ID', 4, 4, 900, 9003, null, 15, 1, null, null from dual
	) src 
	on (src.id=dst.id)
 	when not matched then
		insert (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, precision, width, required, max_length, format) values 
		(src.id, src.ref_book_id, src.name, src.alias, src.type, src.ord, src.reference_id, src.attribute_id, src.precision, src.width, src.required, src.max_length, src.format);

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||'Part3: No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||'Part3: Success');
	END CASE; 


EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

--3.7-dnovikov-8 https://jira.aplana.com/browse/SBRFNDFL-5679 - рефакторинг настроек подразделений, перекидываем данные в новую таблицу
declare 
  v_task_name varchar2(128):='insert_update_delete block #5 - fill department_config';  
begin
	merge into department_config dst using
	(select id, kpp, oktmo oktmo_id, version start_date, end_date, department_id, TAX_ORGAN_CODE, TAX_ORGAN_CODE_MID, present_place present_place_id, NAME, PHONE,
		REORG_FORM_CODE reorganization_id, REORG_INN, REORG_KPP, SIGNATORY_ID, SIGNATORY_SURNAME, SIGNATORY_FIRSTNAME, SIGNATORY_LASTNAME, APPROVE_DOC_NAME, APPROVE_ORG_NAME
			from (
				select * from (
					select rbnd.*, lead(version) over(partition by rbnd.record_id order by version) - interval '1' DAY end_date
					from ref_book_ndfl_detail_old rbnd
					where status != -1
				) where status = 0
			)) src
	on (src.id=dst.id)
	when not matched then
	insert (id, kpp, oktmo_id, start_date, end_date, department_id, tax_organ_code, 
		tax_organ_code_mid, present_place_id, name, phone, reorganization_id, reorg_inn, reorg_kpp, signatory_id, signatory_surname, signatory_firstname,
		 SIGNATORY_LASTNAME, APPROVE_DOC_NAME, APPROVE_ORG_NAME  )
	values (src.id, src.kpp, src.oktmo_id, src.start_date, src.end_date, src.department_id, src.tax_organ_code, 
		src.tax_organ_code_mid, src.present_place_id, src.name, src.phone, src.reorganization_id, 
		src.reorg_inn, src.reorg_kpp, src.signatory_id, src.signatory_surname, src.signatory_firstname,
		 src.SIGNATORY_LASTNAME, src.APPROVE_DOC_NAME, src.APPROVE_ORG_NAME);


	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||'Part1: No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||'Part1: Success');
	END CASE; 

EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

-- 3.7-dnovikov-27 https://jira.aplana.com/browse/SBRFNDFL-7756 Добавить в систему "Справочник Расширяющие интервалы для загрузки данных"

declare 
  v_task_name varchar2(128):='insert_update_delete block #6 - fill report_period_type';  
begin
        merge into report_period_type dst using
	( select -1 as id, -1 as record_id, ' ' as code, ' ' as name, 2  as status from dual
	) src
	on (src.code=dst.code)
	when not matched then
	insert  (id, record_id, code, name, status) values (src.id, src.record_id, src.code, src.name, src.status);	

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 

EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

-- 3.7-ytrofimov-10

declare 
  v_task_name varchar2(128):='insert_update_delete block #7 - add task';  
begin
        merge into CONFIGURATION_SCHEDULER dst using
	( select 6 as id, 'Периодическое сжатие таблиц' as task_name, '0 0 22 ? * SAT' as schedule, 1 as active  from dual
	) src
	on (src.id=dst.id)
	when not matched then
	insert  (id, task_name,schedule, active,modification_date, last_fire_date) values 
	(src.id, src.task_name, src.schedule, src.active, sysdate, sysdate)
	when matched then
	update set dst.task_name=src.task_name, dst.schedule = src.schedule, dst.active = src.active,
		dst.modification_date=sysdate;	

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 

EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

-- 3.7-skononova-6 https://jira.aplana.com/browse/SBRFNDFL-7804 Добавить в справочник "Виды дохода" новые записи
-- 3.7-skononova-8 https://jira.aplana.com/browse/SBRFNDFL-7842 Значение в столбце "Наименование" для "2510 - 13" справочника "Виды дохода" отличается от постановки
declare 
  v_task_name varchar2(128):='insert_update_delete block #8 - merge into ref_book_income_kind';  
begin
	merge into ref_book_income_kind a using
	 (select (select id from (select id,version from ref_book_income_type where code='2520' and status=0 order by version desc) where rownum=1) as income_type_id, 
	        '00' as mark, '' as name, to_date('01.01.2016','DD.MM.YYYY') as version from dual
	 union
	 select (select id from (select id,version from ref_book_income_type where code='2720' and status=0 order by version desc) where rownum=1) as income_type_id, 
	        '00' as mark, '' as name, to_date('01.01.2016','DD.MM.YYYY') as version from dual
	 union
	 select (select id from (select id,version from ref_book_income_type where code='2720' and status=0 order by version desc) where rownum=1) as income_type_id, 
	        '13' as mark, 'Выплата дохода в денежной форме' as name, to_date('01.01.2016','DD.MM.YYYY') as version from dual
	 union
	 select (select id from (select id,version from ref_book_income_type where code='2740' and status=0 order by version desc) where rownum=1) as income_type_id, 
	        '00' as mark, '' as name, to_date('01.01.2016','DD.MM.YYYY') as version from dual
	 union
	 select (select id from (select id,version from ref_book_income_type where code='2750' and status=0 order by version desc) where rownum=1) as income_type_id, 
	        '00' as mark, '' as name, to_date('01.01.2016','DD.MM.YYYY') as version from dual
	 union
	 select (select id from (select id,version from ref_book_income_type where code='2790' and status=0 order by version desc) where rownum=1) as income_type_id, 
	        '00' as mark, '' as name, to_date('01.01.2016','DD.MM.YYYY') as version from dual
	) b
	on (a.income_type_id=b.income_type_id and a.mark=b.mark and a.version=b.version)
	when not matched then
		insert (id,record_id,income_type_id,mark,name,version)
		values (seq_ref_book_record.nextval,seq_ref_book_record_row_id.nextval,b.income_type_id,b.mark,b.name,b.version);
		
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;


