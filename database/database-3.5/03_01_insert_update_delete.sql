-- 3.5-snazin-2 https://jira.aplana.com/browse/SBRFNDFL-6483 Реализовать возможность массового редактирования данных ПНФ (для коррекции проблем SAP)
declare 
  v_task_name varchar2(128):='insert_update_delete block #1 - merge into configuration';  
begin
	merge into configuration a using
	(select 'DECLARATION_ROWS_BULK_EDIT_MAX_COUNT' as code, 0 as department_id, 200 as value from dual
	) b
	on (a.code=b.code)
	when not matched then
		insert (code, department_id, value)
		values (b.code, b.department_id, b.value);
	
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

-- 3.5-ishevchuk-1 https://jira.aplana.com/browse/SBRFNDFL-6376 Два одинаковых типа задания "Выгрузка отчетности" во вкладке "Параметры асинхронных заданий"
declare
	v_task_name varchar2(128):='insert_update_delete block #2 - delete from async_task_type';  
begin		
    delete from async_task_type where id = 29;

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

-- 3.5-dnovikov-9 https://jira.aplana.com/browse/SBRFNDFL-6513 6-НДФЛ, новые атрибуты, значения по-умолчанию
declare
	v_task_name varchar2(128):='insert_update_delete block #3 - update declaration_data';  
begin	
	
    update declaration_data dd set negative_income = 0, negative_tax = 0, negative_sums_sign = 0
			where 103 = (select declaration_type_id from declaration_template where id = dd.declaration_template_id);

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

-- 3.5-ytrofimov-6 https://jira.aplana.com/browse/SBRFNDFL-6610 Реализовать "дизайн" печатного представления 2-НДФЛ
declare 
  v_task_name varchar2(128):='insert_update_delete block #4 - merge into declaration_template_file';  
begin
	merge into declaration_template_file a using
	(select 102 as declaration_template_id, '79c363f8-52f7-4fa0-94bd-27c693f5cfd9' as blob_data_id from dual
	 union
	 select 104 as declaration_template_id, '79c363f8-52f7-4fa0-94bd-27c693f5cfd9' as blob_data_id from dual
	 union
	 select 102 as declaration_template_id, '3a8afe75-a882-4ecb-9bd2-46920d12803d' as blob_data_id from dual
	 union
	 select 104 as declaration_template_id, '3a8afe75-a882-4ecb-9bd2-46920d12803d' as blob_data_id from dual
	) b
	on (a.declaration_template_id=b.declaration_template_id and a.blob_data_id=b.blob_data_id)
	when not matched then
		insert (declaration_template_id, blob_data_id)
		values (b.declaration_template_id, b.blob_data_id);
	
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

-- 3.5-dnovikov-12 https://jira.aplana.com/browse/SBRFNDFL-6408 Корректировка описания лица, подписавшего документ
declare
	v_task_name varchar2(128):='insert_update_delete block #5 - update ref_book_attribute';  
begin	
	
    update ref_book_attribute set max_length = 100 where id = 213;

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

-- 3.5-dnovikov-12 https://jira.aplana.com/browse/SBRFNDFL-6408 Корректировка описания лица, подписавшего документ
declare
	v_task_name varchar2(128):='insert_update_delete block #6 - update 1 ref_book_signatory_mark';  
begin	
	
    update ref_book_signatory_mark set name = 'Налоговый агент, правопреемник налогового агента' where code = 1;

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

-- 3.5-dnovikov-12 https://jira.aplana.com/browse/SBRFNDFL-6408 Корректировка описания лица, подписавшего документ
declare
	v_task_name varchar2(128):='insert_update_delete block #7 - update 2 ref_book_signatory_mark';  
begin	
	
    update ref_book_signatory_mark set name = 'Представитель налогового агента, представитель правопреемника налогового агента' where code = 2;

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

-- 3.5-ishevchuk-6 https://jira.aplana.com/browse/SBRFNDFL-6633 Реализовать использование кода дохода 1552 с признаками 01,02,03,04
declare 
  v_task_name varchar2(128):='insert_update_delete block #8 - merge into ref_book_income_kind';  
begin
	merge into ref_book_income_kind a using
	(
	 select (select id from (select id,version from ref_book_income_type where code='1552' and status=0 order by version desc) where rownum=1) as income_type_id, 
	        '01' as mark, 'Начисление дохода при выводе денежных средств с брокерского счёта' as name, to_date('26.12.2016','DD.MM.YYYY') as version from dual
	 union
	 select (select id from (select id,version from ref_book_income_type where code='1552' and status=0 order by version desc) where rownum=1) as income_type_id, 
	        '02' as mark, 'Начисление дохода при выводе ценных бумаг с торгового раздела счёта депо' as name, to_date('26.12.2016','DD.MM.YYYY') as version from dual
	 union
	 select (select id from (select id,version from ref_book_income_type where code='1552' and status=0 order by version desc) where rownum=1) as income_type_id, 
	        '03' as mark, 'Начисление дохода при расторжении договора брокерского обслуживания' as name, to_date('26.12.2016','DD.MM.YYYY') as version from dual
	 union
	 select (select id from (select id,version from ref_book_income_type where code='1552' and status=0 order by version desc) where rownum=1) as income_type_id, 
	        '04' as mark, 'Начисление дохода по окончанию налогового периода за который начислен доход' as name, to_date('26.12.2016','DD.MM.YYYY') as version from dual
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

-- 3.5-ishevchuk-9
declare 
  v_task_name varchar2(128):='insert_update_delete block #9 - merge into ref_book_doc_type';  
begin
	merge into ref_book_doc_type a using
	(
	 select 0 as status, to_date('01.01.2016','DD.MM.YYYY') as version, '22' as code, 'Загранпаспорт гражданина Российской Федерации' as name, '3' as priority from dual
	 union
	 select 0 as status, to_date('01.01.2016','DD.MM.YYYY') as version, '27' as code, 'Военный билет офицера запаса' as name, '6' as priority from dual
	) b
	on (a.status=b.status and a.version=b.version and a.code=b.code and a.priority=b.priority)
	when not matched then
		insert (id, record_id, status, version, code, name, priority)
		values (seq_ref_book_record.nextval,seq_ref_book_record_row_id.nextval,b.status, b.version, b.code, b.name, b.priority);
		
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

-- 3.5-ishevchuk-9
declare
	v_task_name varchar2(128):='insert_update_delete block #10 - update 1 ref_book_doc_type';  
begin	
	
	update ref_book_doc_type set priority=2
	where code='14';

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

-- 3.5-ishevchuk-9
declare
	v_task_name varchar2(128):='insert_update_delete block #11 - update 2 ref_book_doc_type';  
begin	
	
	update ref_book_doc_type set priority=4
	where code='03';

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

-- 3.5-ishevchuk-9
declare
	v_task_name varchar2(128):='insert_update_delete block #12 - update 3 ref_book_doc_type';  
begin	
	
	update ref_book_doc_type set priority=5
	where code='07';

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
