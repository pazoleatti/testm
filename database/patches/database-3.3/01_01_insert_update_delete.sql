-- https://jira.aplana.com/browse/SBRFNDFL-5625 Реализовать изменения в массовой выгрузке файлов в статусах, отличных от Принята
declare 
  v_task_name varchar2(128):='insert_update_delete block #1 - merge into async_task_type';  
begin
	merge into async_task_type a using
	(select 41 as id, 'Выгрузка отчетности' as name, 'ExportReportsAsyncTask' as handler_bean from dual
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

-- https://jira.aplana.com/browse/SBRFNDFL-5901 Написать скрипт по обновлению даты изменения строк всех форм
declare 
  v_task_name varchar2(250):='insert_update_delete block #2 - update modified_date, merge into ndfl_person where declaration_data_id = 100 (Section 1, Primary Form).';  
begin
	merge into ndfl_person np
	using 
	(
	  select np2.id as np_id, lb.log_date
	  from ndfl_person np2
	  join declaration_data dd on dd.id = np2.declaration_data_id
	  join log_business lb on lb.declaration_data_id = dd.id
	  where 
	  np2.modified_date is null
	  and dd.declaration_template_id = 100
	  and lb.event_id = 1
	) upd_rows
	on (np.id = upd_rows.np_id)
	when matched then update set np.modified_date = upd_rows.log_date;
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success. Updated '||to_char(SQL%ROWCOUNT)||' rows');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

-- https://jira.aplana.com/browse/SBRFNDFL-5901 Написать скрипт по обновлению даты изменения строк всех форм
declare 
  v_task_name varchar2(250):='insert_update_delete block #3 - update modified_date, merge into ndfl_person where declaration_data_id = 101 (Section 1, Consolidated Form).';  
begin
	merge into ndfl_person np
	using 
	(
	  select np2.id as np_id, max(lb.log_date) log_date
	  from ndfl_person np2
	  join declaration_data dd on dd.id = np2.declaration_data_id
	  join log_business lb on lb.declaration_data_id = dd.id
	  where 
	  np2.modified_date is null
	  and dd.declaration_template_id = 101
	  and lb.event_id = 6
	  group by np2.id
	) upd_rows
	on (np.id = upd_rows.np_id)
	when matched then update set np.modified_date = upd_rows.log_date;
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success. Updated '||to_char(SQL%ROWCOUNT)||' rows');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

-- https://jira.aplana.com/browse/SBRFNDFL-5901 Написать скрипт по обновлению даты изменения строк всех форм
declare 
  v_task_name varchar2(250):='insert_update_delete block #4 - update modified_date, merge into ndfl_person_income where declaration_data_id = 100 (Section 2, Primary Form).';  
begin
	merge into ndfl_person_income npi
	using 
	(
	  select lb.log_date, npi2.id as npi_id
	  from ndfl_person_income npi2 
	  join ndfl_person np on np.id = npi2.ndfl_person_id
	  join declaration_data dd on dd.id = np.declaration_data_id
	  join log_business lb on lb.declaration_data_id = dd.id
	  where 
	  npi2.modified_date is null 
	  and dd.declaration_template_id = 100 
	  and lb.event_id = 1 
	) upd_rows
	on (npi.id = upd_rows.npi_id)
	when matched then update set npi.modified_date = upd_rows.log_date;
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success. Updated '||to_char(SQL%ROWCOUNT)||' rows');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

-- https://jira.aplana.com/browse/SBRFNDFL-5901 Написать скрипт по обновлению даты изменения строк всех форм
declare 
  v_task_name varchar2(250):='insert_update_delete block #5 - update modified_date, merge into ndfl_person_income where declaration_data_id = 101 (Section 2, Consolidated Form).';  
begin
	merge into ndfl_person_income npi -- Сведения о доходах физического лица
	using 
	(
	  select npi2.id as npi_id, npi3.modified_date
	  from ndfl_person_income npi2
	  join ndfl_person_income npi3 on npi3.id = npi2.source_id
	  where
	  npi2.modified_date is null
	  and npi3.modified_date is not null
	) upd_rows
	on (npi.id = upd_rows.npi_id)
	when matched then update set npi.modified_date = upd_rows.modified_date;
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success. Updated '||to_char(SQL%ROWCOUNT)||' rows');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

-- https://jira.aplana.com/browse/SBRFNDFL-5901 Написать скрипт по обновлению даты изменения строк всех форм
declare 
  v_task_name varchar2(250):='insert_update_delete block #6 - update modified_date, merge into ndfl_person_deduction where declaration_data_id = 100 (Section 3, Primary Form).';  
begin
	merge into ndfl_person_deduction npd 
	using 
	(
	  select lb.log_date, npd2.id as npd_id 
	  from
	  ndfl_person_deduction npd2
	  join ndfl_person np on np.id = npd2.ndfl_person_id
	  join declaration_data dd on dd.id = np.declaration_data_id
	  join log_business lb on lb.declaration_data_id = dd.id
	  where dd.declaration_template_id = 100 
	  and lb.event_id = 1 
	  and npd2.modified_date is null
	) upd_rows
	on (npd.id = upd_rows.npd_id)
	when matched then update set npd.modified_date = upd_rows.log_date;
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success. Updated '||to_char(SQL%ROWCOUNT)||' rows');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

-- https://jira.aplana.com/browse/SBRFNDFL-5901 Написать скрипт по обновлению даты изменения строк всех форм
declare 
  v_task_name varchar2(250):='insert_update_delete block #7 - update modified_date, merge into ndfl_person_deduction where declaration_data_id = 101 (Section 3, Consolidated Form).';  
begin
	merge into ndfl_person_deduction npd 
	using 
	(
	  select npd2.id as npd_id, npd3.modified_date
	  from ndfl_person_deduction npd2
	  join ndfl_person_deduction npd3 on npd3.id = npd2.source_id
	  where
	  npd2.modified_date is null
	  and npd3.modified_date is not null
	) upd_rows
	on (npd.id = upd_rows.npd_id)
	when matched then update set npd.modified_date = upd_rows.modified_date;
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success. Updated '||to_char(SQL%ROWCOUNT)||' rows');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

-- https://jira.aplana.com/browse/SBRFNDFL-5901 Написать скрипт по обновлению даты изменения строк всех форм
declare 
  v_task_name varchar2(250):='insert_update_delete block #8 - update modified_date, merge into ndfl_person_prepayment where declaration_data_id = 100 (Section 4, Primary Form).';  
begin
	merge into ndfl_person_prepayment npp 
	using 
	(
	  select lb.log_date, npp2.id as npp_id 
	  from
	  ndfl_person_prepayment npp2
	  join ndfl_person np on np.id = npp2.ndfl_person_id
	  join declaration_data dd on dd.id = np.declaration_data_id
	  join log_business lb on lb.declaration_data_id = dd.id
	  where dd.declaration_template_id = 100 
	  and lb.event_id = 1 
	  and npp2.modified_date is null
	) upd_rows
	on (npp.id = upd_rows.npp_id)
	when matched then update set npp.modified_date = upd_rows.log_date;
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success. Updated '||to_char(SQL%ROWCOUNT)||' rows');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

-- https://jira.aplana.com/browse/SBRFNDFL-5901 Написать скрипт по обновлению даты изменения строк всех форм
declare 
  v_task_name varchar2(250):='insert_update_delete block #9 - update modified_date, merge into ndfl_person_prepayment where declaration_data_id = 101 (Section 4, Consolidated Form).';  
begin
	merge into ndfl_person_prepayment npp 
	using 
	(
	  select npp2.id as npp_id, npp3.modified_date
	  from ndfl_person_prepayment npp2
	  join ndfl_person_prepayment npp3 on npp3.id = npp2.source_id
	  where
	  npp2.modified_date is null
	  and npp3.modified_date is not null
	) upd_rows
	on (npp.id = upd_rows.npp_id)
	when matched then update set npp.modified_date = upd_rows.modified_date;
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success. Updated '||to_char(SQL%ROWCOUNT)||' rows');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

-- https://jira.aplana.com/browse/SBRFNDFL-5461 новый спецотчет
declare 
  v_task_name varchar2(128):='insert_update_delete block #10 - merge into declaration_subreport';  
begin
	merge into declaration_subreport a using
	(select 100 as declaration_template_id, 'Отчет в разрезе ставок' as name, 4 as ord, 'rnu_rate_report' as alias from dual
	) b
	on (a.declaration_template_id=b.declaration_template_id and a.alias=b.alias)
	when not matched then
		insert (id, declaration_template_id, name, ord, alias)
		values (SEQ_DECLARATION_SUBREPORT.nextval, b.declaration_template_id, b.name, b.ord, b.alias);
	
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

-- https://jira.aplana.com/browse/SBRFNDFL-5461 новый спецотчет
declare 
  v_task_name varchar2(128):='insert_update_delete block #11 - merge into declaration_subreport';  
begin
	merge into declaration_subreport a using
	(select 101 as declaration_template_id, 'Отчет в разрезе ставок' as name, 4 as ord, 'rnu_rate_report' as alias from dual
	) b
	on (a.declaration_template_id=b.declaration_template_id and a.alias=b.alias)
	when not matched then
		insert (id, declaration_template_id, name, ord, alias)
		values (SEQ_DECLARATION_SUBREPORT.nextval, b.declaration_template_id, b.name, b.ord, b.alias);
	
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

-- https://jira.aplana.com/browse/SBRFNDFL-5461 новый спецотчет
declare 
  v_task_name varchar2(128):='insert_update_delete block #12 - merge into declaration_subreport';  
begin
	merge into declaration_subreport a using
	(select 100 as declaration_template_id, 'Отчет в разрезе платёжных поручений' as name, 4 as ord, 'rnu_payment_report' as alias from dual
	) b
	on (a.declaration_template_id=b.declaration_template_id and a.alias=b.alias)
	when not matched then
		insert (id, declaration_template_id, name, ord, alias)
		values (SEQ_DECLARATION_SUBREPORT.nextval, b.declaration_template_id, b.name, b.ord, b.alias);
	
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

-- https://jira.aplana.com/browse/SBRFNDFL-5461 новый спецотчет
declare 
  v_task_name varchar2(128):='insert_update_delete block #13 - merge into declaration_subreport';  
begin
	merge into declaration_subreport a using
	(select 101 as declaration_template_id, 'Отчет в разрезе платёжных поручений' as name, 4 as ord, 'rnu_payment_report' as alias from dual
	) b
	on (a.declaration_template_id=b.declaration_template_id and a.alias=b.alias)
	when not matched then
		insert (id, declaration_template_id, name, ord, alias)
		values (SEQ_DECLARATION_SUBREPORT.nextval, b.declaration_template_id, b.name, b.ord, b.alias);
	
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

-- https://jira.aplana.com/browse/SBRFNDFL-6092 новые спецотчеты
declare 
  v_task_name varchar2(128):='insert_update_delete block #14 - merge into declaration_subreport';  
begin
	merge into declaration_subreport a using
	(select 100 as declaration_template_id, 'отчет "Данные для включения в разделы 2-НДФЛ и 6-НДФЛ"' as name, 6 as ord, 'rnu_ndfl_2_6_data_xlsx_report' as alias from dual
	) b
	on (a.declaration_template_id=b.declaration_template_id and a.alias=b.alias)
	when not matched then
		insert (id, declaration_template_id, name, ord, alias)
		values (SEQ_DECLARATION_SUBREPORT.nextval, b.declaration_template_id, b.name, b.ord, b.alias);
	
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

-- https://jira.aplana.com/browse/SBRFNDFL-6092 новые спецотчеты
declare 
  v_task_name varchar2(128):='insert_update_delete block #15 - merge into declaration_subreport';  
begin
	merge into declaration_subreport a using
	(select 101 as declaration_template_id, 'отчет "Данные для включения в разделы 2-НДФЛ и 6-НДФЛ"' as name, 6 as ord, 'rnu_ndfl_2_6_data_xlsx_report' as alias from dual
	) b
	on (a.declaration_template_id=b.declaration_template_id and a.alias=b.alias)
	when not matched then
		insert (id, declaration_template_id, name, ord, alias)
		values (SEQ_DECLARATION_SUBREPORT.nextval, b.declaration_template_id, b.name, b.ord, b.alias);
	
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

-- https://jira.aplana.com/browse/SBRFNDFL-6092 новые спецотчеты
declare 
  v_task_name varchar2(128):='insert_update_delete block #16 - merge into declaration_subreport';  
begin
	merge into declaration_subreport a using
	(select 100 as declaration_template_id, 'файл выгрузки "Данные для включения в разделы 2-НДФЛ и 6-НДФЛ"' as name, 7 as ord, 'rnu_ndfl_2_6_data_txt_report' as alias from dual
	) b
	on (a.declaration_template_id=b.declaration_template_id and a.alias=b.alias)
	when not matched then
		insert (id, declaration_template_id, name, ord, alias)
		values (SEQ_DECLARATION_SUBREPORT.nextval, b.declaration_template_id, b.name, b.ord, b.alias);
	
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

-- https://jira.aplana.com/browse/SBRFNDFL-6092 новые спецотчеты
declare 
  v_task_name varchar2(128):='insert_update_delete block #17 - merge into declaration_subreport';  
begin
	merge into declaration_subreport a using
	(select 101 as declaration_template_id, 'файл выгрузки "Данные для включения в разделы 2-НДФЛ и 6-НДФЛ"' as name, 7 as ord, 'rnu_ndfl_2_6_data_txt_report' as alias from dual
	) b
	on (a.declaration_template_id=b.declaration_template_id and a.alias=b.alias)
	when not matched then
		insert (id, declaration_template_id, name, ord, alias)
		values (SEQ_DECLARATION_SUBREPORT.nextval, b.declaration_template_id, b.name, b.ord, b.alias);
	
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

-- https://jira.aplana.com/browse/SBRFNDFL-5459 новые спецотчеты
declare 
  v_task_name varchar2(128):='insert_update_delete block #18 - merge into declaration_subreport';  
begin
	merge into declaration_subreport a using
	(select 100 as declaration_template_id, 'Детализация – доходы, вычеты, налоги' as name, 6 as ord, 'rnu_ndfl_detail_report' as alias from dual
	) b
	on (a.declaration_template_id=b.declaration_template_id and a.alias=b.alias)
	when not matched then
		insert (id, declaration_template_id, name, ord, alias)
		values (SEQ_DECLARATION_SUBREPORT.nextval, b.declaration_template_id, b.name, b.ord, b.alias);
	
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

-- https://jira.aplana.com/browse/SBRFNDFL-5459 новые спецотчеты
declare 
  v_task_name varchar2(128):='insert_update_delete block #19 - merge into declaration_subreport';  
begin
	merge into declaration_subreport a using
	(select 101 as declaration_template_id, 'Детализация – доходы, вычеты, налоги' as name, 6 as ord, 'rnu_ndfl_detail_report' as alias from dual
	) b
	on (a.declaration_template_id=b.declaration_template_id and a.alias=b.alias)
	when not matched then
		insert (id, declaration_template_id, name, ord, alias)
		values (SEQ_DECLARATION_SUBREPORT.nextval, b.declaration_template_id, b.name, b.ord, b.alias);
	
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
	v_task_name varchar2(128):='insert_update_delete block #20 - update DECL_TEMPLATE_CHECKS';  
begin	
	
    update DECL_TEMPLATE_CHECKS set
			CHECK_TYPE = 'Строка вычета не соответствует строке начисления',
			DESCRIPTION = 'Соответствие строки вычета строке начисления'
			where CHECK_CODE = '003-0001-00002';

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
