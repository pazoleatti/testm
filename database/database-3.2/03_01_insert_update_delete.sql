declare
	v_task_name varchar2(128):='insert_update_delete block #1 - delete from ref_book_attribute';  
begin		
	-- https://jira.aplana.com/browse/SBRFNDFL-5492 удалил справочник "Настройки асинхронных задач"
    delete from ref_book_attribute where ref_book_id=401;

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #2 - delete from ref_book';  
begin		
	-- https://jira.aplana.com/browse/SBRFNDFL-5492 удалил справочник "Настройки асинхронных задач"
    delete from ref_book where id=401;

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #3 - delete from ref_book_attribute';  
begin		
	-- https://jira.aplana.com/browse/SBRFNDFL-5492 удалил справочник "Настройки почты"
    delete from ref_book_attribute where ref_book_id=400;

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #4 - delete from ref_book';  
begin		
	-- https://jira.aplana.com/browse/SBRFNDFL-5492 удалил справочник "Настройки почты"
    delete from ref_book where id=400;

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #5 - delete from ref_book_attribute';  
begin		
	-- https://jira.aplana.com/browse/SBRFNDFL-5717 удалил справочник "Конфигурационные параметры"
    delete from ref_book_attribute where ref_book_id=105;

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #6 - delete from ref_book';  
begin		
	-- https://jira.aplana.com/browse/SBRFNDFL-5717 удалил справочник "Конфигурационные параметры"
    delete from ref_book where id=105;

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #7 - update ref_book';  
begin	
	-- https://jira.aplana.com/browse/SBRFNDFL-5494 удалил скрипт у справочника подразделений
    update ref_book set script_id = null where id = 30;

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

-- https://jira.aplana.com/browse/SBRFNDFL-5080 новые виды КНФ
declare 
  v_task_name varchar2(128):='insert_update_delete block #8 - merge into ref_book ';  
begin
	merge into ref_book a using
	(select 909 as id, 'Типы КНФ' as name,'REF_BOOK_KNF_TYPE' as table_name, 1 as read_only, 0 as is_versioned from dual
	) b
	on (a.id=b.id)
	when not matched then
		insert (id, name, table_name, read_only, IS_VERSIONED)
		values (b.id, b.name, b.table_name, b.read_only, b.IS_VERSIONED);
	
	CASE SQL%ROWCOUNT 
	WHEN 1 THEN dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

-- https://jira.aplana.com/browse/SBRFNDFL-5080 новые виды КНФ
declare 
  v_task_name varchar2(128):='insert_update_delete block #9 - merge into ref_book_attribute ';  
begin
	merge into ref_book_attribute a using
	(select 9091 as id, 909 as ref_book_id, 'Код' as name, 'ID' as alias, 2 as type, 1 as ord, 0 as precision, 9 as width, 1 as required, 1 as is_unique, 9 as max_length from dual
	 union all
	 select 9092 as id, 909 as ref_book_id, 'Наименование' as name, 'NAME' as alias, 1 as type, 2 as ord, null as precision, 9 as width, 1 as required, 0 as is_unique, 2000 as max_length from dual
	) b
	on (a.id=b.id)
	when not matched then
		insert (id, ref_book_id, name, alias, type, ord, precision, width, required, is_unique, max_length)
		values (b.id, b.ref_book_id, b.name, b.alias, b.type, b.ord, b.precision, b.width, b.required, b.is_unique, b.max_length);

	CASE SQL%ROWCOUNT 
	WHEN 2 THEN dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	ELSE dbms_output.put_line(v_task_name||'[ERROR]:'||' changes had already been partly implemented');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;
-- https://jira.aplana.com/browse/SBRFNDFL-5080 новые виды КНФ
declare 
  v_task_name varchar2(128):='insert_update_delete block #10 - merge into ref_book_attribute ';  
begin
	merge into ref_book_attribute a using
	(select 9223 as id, 922 as ref_book_id, 'Включается в Приложение 2' as name, 'APP2_INCLUDE' as alias, 2 as type, 3 as ord, 0 as precision, 9 as width, 1 as required, 0 as is_unique, 1 as max_length from dual
	) b
	on (a.id=b.id)
	when not matched then
		insert (id, ref_book_id, name, alias, type, ord, precision, width, required, is_unique, max_length)
		values (b.id, b.ref_book_id, b.name, b.alias, b.type, b.ord, b.precision, b.width, b.required, b.is_unique, b.max_length);
	
	CASE SQL%ROWCOUNT 
	WHEN 1 THEN dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #11 - update ref_book_income_type';  
begin	
	-- https://jira.aplana.com/browse/SBRFNDFL-5080 новые виды КНФ
    update ref_book_income_type set app2_include = case when
                code in ('1010','1011','1110','1120','1530','1531','1532','1533','1535','1536','1537','1538','1539','1540','1541','1542','1544','1545','1546','1547','1548','1549','1550','1551','1552','1553','1554','2640','2641','3023')
                then 1
                else 0 end;

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #12 - delete from ref_book_attribute';  
begin		
	-- https://jira.aplana.com/browse/SBRFNDFL-5692
    delete from ref_book_attribute where id=9054;

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #13 - delete from ref_book_attribute';  
begin		
	-- https://jira.aplana.com/browse/SBRFNDFL-5692
    delete from ref_book_attribute where ref_book_id in (901, 902, 905, 908);

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #14 - delete from ref_book';  
begin		
	-- https://jira.aplana.com/browse/SBRFNDFL-5692
    delete from ref_book where id in (901, 902, 905, 908);

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #15 - update ref_book_income_type';  
begin	
	-- https://jira.aplana.com/browse/SBRFNDFL-5929 Данные справочника "Коды видов доходов" разнятся с постановкой
    update ref_book_income_type
			set name='Доходы по операциям с производными финансовыми инструментами, которые обращаются на организованном рынке и базисным активом которых являются ценные бумаги, фондовые индексы или иные производные финансовые инструменты, базисным активом которых являются ценные бумаги или фондовые индексы'
			where
			code='1532';

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #16 - update ref_book_income_type';  
begin	
	-- https://jira.aplana.com/browse/SBRFNDFL-5929 Данные справочника "Коды видов доходов" разнятся с постановкой
    update ref_book_income_type
			set name='Доходы по операциям с производными финансовыми инструментами, не обращающимися на организованном рынке'
			where
			code='1533';

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #17 - update ref_book_income_type';  
begin	
	-- https://jira.aplana.com/browse/SBRFNDFL-5929 Данные справочника "Коды видов доходов" разнятся с постановкой
    update ref_book_income_type
			set name='Доходы по операциям с производными финансовыми инструментами, которые обращаются на организованном рынке и базисным активом которых не являются ценные бумаги, фондовые индексы или иные производные финансовые инструменты, базисным активом которых являются ценные бумаги или фондовые индексы'
			where
			code='1535';

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #18 - update ref_book_income_type';  
begin	
	-- https://jira.aplana.com/browse/SBRFNDFL-5929 Данные справочника "Коды видов доходов" разнятся с постановкой
    update ref_book_income_type
			set name='Вознаграждение, получаемое налогоплательщиком за выполнение трудовых или иных обязанностей; денежное содержание, денежное довольствие, не подпадающее под действие пункта 29 статьи 217 Налогового кодекса Российской Федерации и иные налогооблагаемые выплаты военнослужащим и приравненным к ним категориям физических лиц (кроме выплат по договорам гражданско-правового характера)'
			where
			code='2000';

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #19 - update ref_book_income_type';  
begin	
	-- https://jira.aplana.com/browse/SBRFNDFL-5929 Данные справочника "Коды видов доходов" разнятся с постановкой
    update ref_book_income_type
			set name='Материальная выгода, полученная от приобретения производных финансовых инструментов'
			where
			code='2641';

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

-- https://jira.aplana.com/browse/SBRFNDFL-5080 новые виды КНФ
declare 
  v_task_name varchar2(128):='insert_update_delete block #20 - merge into async_task_type ';  
begin
	merge into async_task_type a using
	(select 40 as id, 'Формирование Уведомлений о неудержанном налоге' as name,'CreateTaxNotificationAsyncTask' as handler_bean from dual
	) b
	on (a.id=b.id)
	when not matched then
		insert (id, name, handler_bean)
		values (b.id, b.name, b.handler_bean);
	
	CASE SQL%ROWCOUNT 
	WHEN 1 THEN dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #21 - update report_period_type';  
begin	
	-- https://jira.aplana.com/browse/SBRFNDFL-5104 Внести изменения в календарную дату начала периода 90 в справочнике кодов периодов
    update report_period_type set calendar_start_date=to_date('01.10.1970','DD.MM.YYYY') where code='90';

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #22 - update ref_book_attribute';  
begin	
	-- 
    update ref_book_attribute set visible = 0 where id in (167, 168);

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

-- https://jira.aplana.com/browse/SBRFNDFL-5966 КНФ "Файлы и комментарии". Не добавляется файл "Уведомление о неудержанном налоге"
declare 
  v_task_name varchar2(128):='insert_update_delete block #23 - merge into ref_book_attach_file_type ';  
begin
	merge into ref_book_attach_file_type a using
	(select 21657800 as id, 7 as code, 'Уведомление о неудержанном налоге' as name from dual
	) b
	on (a.id=b.id)
	when not matched then
		insert (id, code, name)
		values (b.id, b.code, b.name);
	
	CASE SQL%ROWCOUNT 
	WHEN 1 THEN dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #24 - update REF_BOOK_ASNU';  
begin	
	-- https://jira.aplana.com/browse/SBRFNDFL-5969 В справочнике АСНУ изменить значение в поле "Тип дохода"
    update REF_BOOK_ASNU set type='Возврат платы за подключение к программе страхования' where code='9000';

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #25 - update REF_BOOK_ASNU';  
begin	
	-- https://jira.aplana.com/browse/SBRFNDFL-5969 В справочнике АСНУ изменить значение в поле "Тип дохода"
    update REF_BOOK_ASNU set type='Прощение задолженности по договорам отступного' where code='6002';

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #26 - update REF_BOOK_ASNU';  
begin	
	-- https://jira.aplana.com/browse/SBRFNDFL-5969 В справочнике АСНУ изменить значение в поле "Тип дохода"
    update REF_BOOK_ASNU set type='Доходы вследствие возврата/прощения долга после наступления страхового случая' where code='6003';

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

