declare 
	v_task_name varchar2(128):='insert_update_delete block #1 - update async_task_type';  
begin
	update async_task_type set name='Формирование уведомления о задолженности' where id = 35;
	
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
	v_task_name varchar2(128):='insert_update_delete block #2 - update DECLARATION_TEMPLATE (SBRFNDFL-5088)';  
begin
	--https://jira.aplana.com/browse/SBRFNDFL-5088 Реализовать изменения в форматах чисел в шаблонах файлов Excel-->
	update DECLARATION_TEMPLATE set JRXML='' where declaration_type_id = 100;

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[ERROR]:'||' No changes was done');
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
	v_task_name varchar2(128):='insert_update_delete block #3 - update DECLARATION_TEMPLATE (SBRFNDFL-5088)';  
begin
	--https://jira.aplana.com/browse/SBRFNDFL-5088 Реализовать изменения в форматах чисел в шаблонах файлов Excel-->
	update DECLARATION_TEMPLATE set JRXML='' where declaration_type_id = 101;

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[ERROR]:'||' No changes was done');
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
	v_task_name varchar2(128):='insert_update_delete block #4 - update blob_data';  
begin
	-- У файлов скриптов были ошибочные названия -->
    update blob_data set name='id_doc.groovy' where id = '375ba56a-2509-11e7-93ae-92361f002671';

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[ERROR]:'||' No changes was done');
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
	v_task_name varchar2(128):='insert_update_delete block #5 - update blob_data';  
begin
	-- У файлов скриптов были ошибочные названия -->
    update blob_data set name='person.groovy' where id = '884b9f2e-1678-4d69-9652-b036bba2f728';
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[ERROR]:'||' No changes was done');
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
	v_task_name varchar2(128):='insert_update_delete block #6 - update ref_book_attribute (SBRFNDFL-5245)';  
begin
	--https://jira.aplana.com/browse/SBRFNDFL-5245 Изменить возможные значения поля format из справочника ref_book_attribute -->
	update ref_book_attribute set format=7 where id=9083;
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[ERROR]:'||' No changes was done');
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
	v_task_name varchar2(128):='insert_update_delete block #7 - update ref_book_attribute';  
begin
	update ref_book_attribute set visible = 1 where ref_book_id = 902 and lower(alias) = 'duplicate_record_id';

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[ERROR]:'||' No changes was done');
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
	v_task_name varchar2(128):='insert_update_delete block #8 - update ref_book_ndfl_detail (SBRFNDFL-4982)';  
begin		
	--https://jira.aplana.com/browse/SBRFNDFL-4982 Уведомление об отсутствии пары КПП/ОКТМО в справочнике "Настройки подразделений" -->
	update ref_book_ndfl_detail a set oktmo=(select max(id) from ref_book_oktmo where code=(select code from ref_book_oktmo where id=a.oktmo) and status=0)
	where oktmo in (select id from ref_book_oktmo where status=2);
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare 
	v_task_name varchar2(128):='insert_update_delete block #9 - update ref_book_ndfl_detail (SBRFNDFL-4982)';  
begin				
	-- https://jira.aplana.com/browse/SBRFNDFL-4947 "Дата окончания актуальности" для "Кодов видов вычета" = 221 не соответствует постановке -->
	update ref_book_deduction_type set version=to_date('26.12.16','DD.MM.YY')
	where code in ('221') and version=to_date('25.12.16','DD.MM.YY');
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare 
	v_task_name varchar2(128):='insert_update_delete block #10 - merge into ref_book_income_kind (SBRFNDFL-4984)';  
begin				
	-- https://jira.aplana.com/browse/SBRFNDFL-4984 Уведомление об отсутствии пары "Код дохода"/"Признак дохода" в справочнике "Виды дохода" -->
	merge into ref_book_income_kind a using
	(select (select id from (select id,version from ref_book_income_type where code='1011' and status=0 order by version desc) where rownum=1) as income_type_id,
	'00' as mark, '-' as name,
	to_date('01.01.2017','DD.MM.YYYY') as version from dual
	) b
	on (a.income_type_id=b.income_type_id and a.mark=b.mark)
	when not matched then
	insert (id, income_type_id, mark, name, version, record_id)
	values (seq_ref_book_record.nextval, b.income_type_id, b.mark, b.name, b.version, seq_ref_book_record.nextval);
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare 
	v_task_name varchar2(128):='insert_update_delete block #11 - update ref_book_income_kind (SBRFNDFL-4984)';  
begin						
	-- https://jira.aplana.com/browse/SBRFNDFL-4984 Уведомление об отсутствии пары "Код дохода"/"Признак дохода" в справочнике "Виды дохода" -->
	update ref_book_income_kind b
		set record_id=(select max(record_id) from ref_book_income_kind a
			where exists(select 1 from ref_book_income_type where id=a.income_type_id and code='1011')
			and a.version=to_date('01.01.2016','DD.MM.YYYY')
			and a.mark='00' and a.status=0)
	where exists(select 1 from ref_book_income_type where id=b.income_type_id and code='1011') and b.version=to_date('01.01.2017','DD.MM.YYYY')
	and b.mark='00' and b.status=0;
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_run_condition number(1);
	v_task_name varchar2(128):='insert_update_delete block #12 - insert into CONFIGURATION';  
begin								
	select decode(count(*),0,1,0) into v_run_condition from configuration where code='REPORT_PERIOD_YEAR_MIN' AND department_id=0;
	IF v_run_condition=1 THEN
		insert into CONFIGURATION(code,department_id,value) values('REPORT_PERIOD_YEAR_MIN',0,2003);
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;
	
declare
	v_run_condition number(1);
	v_task_name varchar2(128):='insert_update_delete block #13 - insert into CONFIGURATION';  
begin								
	select decode(count(*),0,1,0) into v_run_condition from configuration where code='REPORT_PERIOD_YEAR_MAX' AND department_id=0;
	IF v_run_condition=1 THEN
		insert into CONFIGURATION(code,department_id,value) values('REPORT_PERIOD_YEAR_MAX',0,2100);
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_run_condition number(1);
	v_task_name varchar2(128):='insert_update_delete block #14 - insert into CONFIGURATION (SBRFNDFL-5299)';  
begin								
	--https://jira.aplana.com/browse/SBRFNDFL-5299 Реализовать добавление в РНУ колонки с АСНУ и фильтра по этой колонке
	select decode(count(*),0,1,0) into v_run_condition from configuration where code='WEIGHT_LAST_NAME' AND department_id=0;
	IF v_run_condition=1 THEN
		insert into CONFIGURATION(code,department_id,value) values('WEIGHT_LAST_NAME',0,5);
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_run_condition number(1);
	v_task_name varchar2(128):='insert_update_delete block #15 - insert into CONFIGURATION (SBRFNDFL-5299)';  
begin								
	--https://jira.aplana.com/browse/SBRFNDFL-5299 Реализовать добавление в РНУ колонки с АСНУ и фильтра по этой колонке
	select decode(count(*),0,1,0) into v_run_condition from configuration where code='WEIGHT_FIRST_NAME' AND department_id=0;
	IF v_run_condition=1 THEN
		insert into CONFIGURATION(code,department_id,value) values('WEIGHT_FIRST_NAME',0,10);
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_run_condition number(1);
	v_task_name varchar2(128):='insert_update_delete block #16 - insert into CONFIGURATION (SBRFNDFL-5299)';  
begin								
	--https://jira.aplana.com/browse/SBRFNDFL-5299 Реализовать добавление в РНУ колонки с АСНУ и фильтра по этой колонке
	select decode(count(*),0,1,0) into v_run_condition from configuration where code='WEIGHT_MIDDLE_NAME' AND department_id=0;
	IF v_run_condition=1 THEN
		insert into CONFIGURATION(code,department_id,value) values('WEIGHT_MIDDLE_NAME',0,5);
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_run_condition number(1);
	v_task_name varchar2(128):='insert_update_delete block #17 - insert into CONFIGURATION (SBRFNDFL-5299)';  
begin								
	--https://jira.aplana.com/browse/SBRFNDFL-5299 Реализовать добавление в РНУ колонки с АСНУ и фильтра по этой колонке
	select decode(count(*),0,1,0) into v_run_condition from configuration where code='WEIGHT_BIRTHDAY' AND department_id=0;
	IF v_run_condition=1 THEN
		insert into CONFIGURATION(code,department_id,value) values('WEIGHT_BIRTHDAY',0,10);
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_run_condition number(1);
	v_task_name varchar2(128):='insert_update_delete block #18 - insert into CONFIGURATION (SBRFNDFL-5299)';  
begin								
	--https://jira.aplana.com/browse/SBRFNDFL-5299 Реализовать добавление в РНУ колонки с АСНУ и фильтра по этой колонке
	select decode(count(*),0,1,0) into v_run_condition from configuration where code='WEIGHT_CITIZENSHIP' AND department_id=0;
	IF v_run_condition=1 THEN
		insert into CONFIGURATION(code,department_id,value) values('WEIGHT_CITIZENSHIP',0,1);
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_run_condition number(1);
	v_task_name varchar2(128):='insert_update_delete block #19 - insert into CONFIGURATION (SBRFNDFL-5299)';  
begin								
	--https://jira.aplana.com/browse/SBRFNDFL-5299 Реализовать добавление в РНУ колонки с АСНУ и фильтра по этой колонке
	select decode(count(*),0,1,0) into v_run_condition from configuration where code='WEIGHT_INP' AND department_id=0;
	IF v_run_condition=1 THEN
		insert into CONFIGURATION(code,department_id,value) values('WEIGHT_INP',0,15);
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_run_condition number(1);
	v_task_name varchar2(128):='insert_update_delete block #20 - insert into CONFIGURATION (SBRFNDFL-5299)';  
begin								
	--https://jira.aplana.com/browse/SBRFNDFL-5299 Реализовать добавление в РНУ колонки с АСНУ и фильтра по этой колонке
	select decode(count(*),0,1,0) into v_run_condition from configuration where code='WEIGHT_INN' AND department_id=0;
	IF v_run_condition=1 THEN
		insert into CONFIGURATION(code,department_id,value) values('WEIGHT_INN',0,10);
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_run_condition number(1);
	v_task_name varchar2(128):='insert_update_delete block #21 - insert into CONFIGURATION (SBRFNDFL-5299)';  
begin								
	--https://jira.aplana.com/browse/SBRFNDFL-5299 Реализовать добавление в РНУ колонки с АСНУ и фильтра по этой колонке
	select decode(count(*),0,1,0) into v_run_condition from configuration where code='WEIGHT_INN_FOREIGN' AND department_id=0;
	IF v_run_condition=1 THEN
		insert into CONFIGURATION(code,department_id,value) values('WEIGHT_INN_FOREIGN',0,10);
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_run_condition number(1);
	v_task_name varchar2(128):='insert_update_delete block #22 - insert into CONFIGURATION (SBRFNDFL-5299)';  
begin								
	--https://jira.aplana.com/browse/SBRFNDFL-5299 Реализовать добавление в РНУ колонки с АСНУ и фильтра по этой колонке
	select decode(count(*),0,1,0) into v_run_condition from configuration where code='WEIGHT_SNILS' AND department_id=0;
	IF v_run_condition=1 THEN
		insert into CONFIGURATION(code,department_id,value) values('WEIGHT_SNILS',0,15);
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_run_condition number(1);
	v_task_name varchar2(128):='insert_update_delete block #23 - insert into CONFIGURATION (SBRFNDFL-5299)';  
begin								
	--https://jira.aplana.com/browse/SBRFNDFL-5299 Реализовать добавление в РНУ колонки с АСНУ и фильтра по этой колонке
	select decode(count(*),0,1,0) into v_run_condition from configuration where code='WEIGHT_TAX_PAYER_STATUS' AND department_id=0;
	IF v_run_condition=1 THEN
		insert into CONFIGURATION(code,department_id,value) values('WEIGHT_TAX_PAYER_STATUS',0,1);
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_run_condition number(1);
	v_task_name varchar2(128):='insert_update_delete block #24 - insert into CONFIGURATION (SBRFNDFL-5299)';  
begin								
	--https://jira.aplana.com/browse/SBRFNDFL-5299 Реализовать добавление в РНУ колонки с АСНУ и фильтра по этой колонке
	select decode(count(*),0,1,0) into v_run_condition from configuration where code='WEIGHT_DUL' AND department_id=0;
	IF v_run_condition=1 THEN
		insert into CONFIGURATION(code,department_id,value) values('WEIGHT_DUL',0,10);
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_run_condition number(1);
	v_task_name varchar2(128):='insert_update_delete block #25 - insert into CONFIGURATION (SBRFNDFL-5299)';  
begin								
	--https://jira.aplana.com/browse/SBRFNDFL-5299 Реализовать добавление в РНУ колонки с АСНУ и фильтра по этой колонке
	select decode(count(*),0,1,0) into v_run_condition from configuration where code='WEIGHT_ADDRESS' AND department_id=0;
	IF v_run_condition=1 THEN
		insert into CONFIGURATION(code,department_id,value) values('WEIGHT_ADDRESS',0,1);
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_run_condition number(1);
	v_task_name varchar2(128):='insert_update_delete block #26 - insert into CONFIGURATION (SBRFNDFL-5299)';  
begin								
	--https://jira.aplana.com/browse/SBRFNDFL-5299 Реализовать добавление в РНУ колонки с АСНУ и фильтра по этой колонке
	select decode(count(*),0,1,0) into v_run_condition from configuration where code='WEIGHT_ADDRESS_INO' AND department_id=0;
	IF v_run_condition=1 THEN
		insert into CONFIGURATION(code,department_id,value) values('WEIGHT_ADDRESS_INO',0,1);
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #27 - insert into department_report_period (SBRFNDFL-5334)';  
begin								
	--https://jira.aplana.com/browse/SBRFNDFL-5334 Создание недостающих связей между новыми подразделениями и периодами
	insert into department_report_period(id,department_id,report_period_id,is_active,correction_date)
	select seq_department_report_period.nextval,id,report_period_id,is_active,correction_date from (
	select d_child.id,drp_parent.report_period_id,drp_parent.is_active,drp_parent.correction_date from department_child_view dcv
	join department d_parent on dcv.parent_id=d_parent.id and d_parent.type=2
	join department_report_period drp_parent on drp_parent.department_id=d_parent.id
	join department d_child on dcv.id=d_child.id
	where not exists (select * from department_report_period drp_child
			where drp_child.department_id=d_child.id and drp_child.report_period_id=drp_parent.report_period_id and
				(drp_child.correction_date=drp_parent.correction_date or (drp_child.correction_date is null and drp_parent.correction_date is null)))
	order by d_child.id,drp_parent.report_period_id,drp_parent.correction_date nulls first) a;
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #29 - update NDFL_PERSON';  
begin								
	update (select np.ID as np_id, np.DECLARATION_DATA_ID as np_dd_id, np.ASNU_ID as np_asnu, dd.ID as dd_id, dd.ASNU_ID as dd_asnu
			from NDFL_PERSON np
			left join DECLARATION_DATA dd on np.DECLARATION_DATA_ID = dd.ID
			where dd.DECLARATION_TEMPLATE_ID = (select dt.ID from DECLARATION_TEMPLATE dt where dt.DECLARATION_TYPE_ID = 100)) sub
	set sub.np_asnu = sub.dd_asnu
	where sub.dd_id = sub.np_dd_id;
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #30 - update NDFL_PERSON_DEDUCTION';  
begin									
	update (select npd.ASNU_ID as npd_asnu, dd.ASNU_ID as dd_asnu, dd.ID as dd_id, np.DECLARATION_DATA_ID as np_dd_id, npd.NDFL_PERSON_ID as npd_np_id, np.ID as np_id
			from NDFL_PERSON_DEDUCTION npd
			left join NDFL_PERSON np on npd.NDFL_PERSON_ID = np.ID
			left join DECLARATION_DATA dd on np.DECLARATION_DATA_ID = dd.ID
			where dd.DECLARATION_TEMPLATE_ID = (select dt.ID from DECLARATION_TEMPLATE dt where dt.DECLARATION_TYPE_ID = 100)) sub
	set sub.npd_asnu = sub.dd_asnu
	where sub.dd_id = sub.np_dd_id and sub.npd_np_id = sub.np_id;
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #31 - update NDFL_PERSON_INCOME';  
begin									
	update (select npi.ASNU_ID as npi_asnu, dd.ASNU_ID as dd_asnu, dd.ID as dd_id, np.DECLARATION_DATA_ID as np_dd_id, npi.NDFL_PERSON_ID as npi_np_id, np.ID as np_id
			from NDFL_PERSON_INCOME npi
			left join NDFL_PERSON np on npi.NDFL_PERSON_ID = np.ID
			left join DECLARATION_DATA dd on np.DECLARATION_DATA_ID = dd.ID
			where dd.DECLARATION_TEMPLATE_ID = (select dt.ID from DECLARATION_TEMPLATE dt where dt.DECLARATION_TYPE_ID = 100)) sub
	set sub.npi_asnu = sub.dd_asnu
	where sub.dd_id = sub.np_dd_id and sub.npi_np_id = sub.np_id;
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #32 - update NDFL_PERSON_PREPAYMENT';  
begin								
	update (select npp.ASNU_ID as npp_asnu, dd.ASNU_ID as dd_asnu, dd.ID as dd_id, np.DECLARATION_DATA_ID as np_dd_id, npp.NDFL_PERSON_ID as npp_np_id, np.ID as np_id
			from NDFL_PERSON_PREPAYMENT npp
			left join NDFL_PERSON np on npp.NDFL_PERSON_ID = np.ID
			left join DECLARATION_DATA dd on np.DECLARATION_DATA_ID = dd.ID
			where dd.DECLARATION_TEMPLATE_ID = (select dt.ID from DECLARATION_TEMPLATE dt where dt.DECLARATION_TYPE_ID = 100)) sub
	set sub.npp_asnu = sub.dd_asnu
	where sub.dd_id = sub.np_dd_id and sub.npp_np_id = sub.np_id;
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #33 - update NDFL_PERSON_DEDUCTION';  
begin									
	update (
		select npd.ASNU_ID as npd_asnu, npd.SOURCE_ID as npd_source, npd.ID as npd_id
		from NDFL_PERSON_DEDUCTION npd
		where npd.SOURCE_ID is not null) sub
	set sub.npd_asnu = (select outer_npd.ASNU_ID from NDFL_PERSON_DEDUCTION outer_npd where outer_npd.id = sub.npd_source);
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #34 - update NDFL_PERSON_INCOME';  
begin									
	update (
		select npi.ASNU_ID as npi_asnu, npi.SOURCE_ID as npi_source, npi.ID as npi_id
		from NDFL_PERSON_INCOME npi
		where npi.SOURCE_ID is not null ) sub
	set sub.npi_asnu = (select outer_npi.ASNU_ID from NDFL_PERSON_INCOME outer_npi where outer_npi.id = sub.npi_source);
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #35 - update NDFL_PERSON_PREPAYMENT';  
begin										
	update (
		select npp.ASNU_ID as npp_asnu, npp.SOURCE_ID as npp_source, npp.ID as npp_id
		from NDFL_PERSON_PREPAYMENT npp
		where npp.SOURCE_ID is not null) sub
	set sub.npp_asnu = (select outer_npp.ASNU_ID from NDFL_PERSON_PREPAYMENT outer_npp where outer_npp.id = sub.npp_source);
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_run_condition number(1);
	v_task_name varchar2(128):='insert_update_delete block #36 - update ref_book_person';  
begin
    -- https://conf.aplana.com/pages/viewpage.action?pageId=40019337 "На что обратить внимание", пункт 2.а
	select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='old_status';
	IF v_run_condition=1 THEN
		execute immediate 'update ref_book_person
		set status = old_status
		where (old_id is not null) and (old_status is not null) and (status = -1)';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');

		v_task_name:='insert_update_delete block #37 - alter table ref_book_person drop column old_status (SBRFNDFL-5415)';  		
		EXECUTE IMMEDIATE 'alter table ref_book_person drop column old_status';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #38 - update ref_book_person';  
begin	
	update ref_book_person
    set old_id = record_id
    where old_id is null;
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #39 - update ref_book_id_doc';  
begin	
    -- https://conf.aplana.com/pages/viewpage.action?pageId=40019337 "На что обратить внимание", пункт 2.b
	-- Выставлено inc_rep = 0, если в нём значение, отличное от (0, 1). Добавлено ограничение на это поле.
	update ref_book_id_doc
    set inc_rep = 0
    where (inc_rep < 0) or (inc_rep > 1);
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #40 - update ref_book_id_doc';  
begin	
    -- https://conf.aplana.com/pages/viewpage.action?pageId=40019337 "На что обратить внимание", пункт 2.b
	-- Если для одного ФЛ было несколько ДУЛ с inc_rep = 1, оставляем только на одном последнем
    update ref_book_id_doc
    set inc_rep = 0
    where id in (
    select id
    from (
    select
    r.id,
    r.inc_rep,
    row_number() over (partition by person_id order by inc_rep desc, version desc, id desc) as rn,
    sum(inc_rep) over (partition by person_id) as inc_rep_sm
    from ref_book_id_doc r
    )
    where inc_rep_sm > 1 and rn > 1 and inc_rep > 0);
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #41 - update ref_book_person';  
begin		
    -- https://conf.aplana.com/pages/viewpage.action?pageId=40019337 "На что обратить внимание", пункт 2.b
	-- Заполнено поле report_doc в таблице ref_book_person данными из ref_book_id_doc
	update ref_book_person person
    set person.report_doc = (
    select doc.id
    from ref_book_id_doc doc
    where (doc.person_id = person.id) and (doc.inc_rep = 1)
    )
    where exists (
    select 1
    from ref_book_id_doc doc
    where (doc.person_id = person.id) and (doc.inc_rep = 1));
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #42 - delete from ref_book_attribute';  
begin		
	-- https://jira.aplana.com/browse/SBRFNDFL-3466
    delete from ref_book_attribute where alias='EMPLOYEE' and ref_book_id=904;

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
	v_task_name varchar2(128):='insert_update_delete block #43 - update ref_book';  
begin	
	-- https://jira.aplana.com/browse/SBRFNDFL-3466
    update ref_book set visible = 0 where id in (901, 902, 904, 905);

	CASE SQL%ROWCOUNT 
	WHEN 4 THEN dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE dbms_output.put_line(v_task_name||'[ERROR]:'||' No rights changes was done');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #44 - delete from ref_book_attribute';  
begin		
	-- https://jira.aplana.com/browse/SBRFNDFL-2158 Реализовать работу с обновленными настройками подразделений
    delete from ref_book_attribute where id in (9524, 9520, 9521, 9525, 9511, 9512);

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
	v_task_name varchar2(128):='insert_update_delete block #45 - update ref_book';  
begin	
	-- https://jira.aplana.com/browse/SBRFNDFL-2158 Реализовать работу с обновленными настройками подразделений
    update ref_book set is_versioned = 1 where id = 951;
	
	CASE SQL%ROWCOUNT 
	WHEN 1 THEN dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE dbms_output.put_line(v_task_name||'[ERROR]:'||' No rights changes was done');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #46 - delete from ref_book_ndfl_detail';  
begin	
	--https://jira.aplana.com/browse/SBRFNDFL-5449 Учесть в скриптах, что для настройки с КПП 164645006, ОКТМО 92626101 дата окончания актуальности - пуста
    delete from ref_book_ndfl_detail where id in (
	select id from ref_book_ndfl_detail where oktmo=(select distinct last_value(id) over(partition by code order by version rows between unbounded preceding and unbounded following) as mx_id
	from ref_book_oktmo where status=0 and code='92626101') and kpp='164645006' and status=2);
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_run_condition number(1);
	v_task_name varchar2(128):='insert_update_delete block #47 - insert into async_task_type';  
begin	
	select decode(count(*),0,1,0) into v_run_condition from async_task_type where id=37;
	IF v_run_condition=1 THEN
		--https://jira.aplana.com/browse/SBRFNDFL-5374 Реализовать выгрузку ФЛ в эксель данных реестра ФЛ-->
		insert into async_task_type(id,name,handler_bean,short_queue_limit,task_limit,limit_kind) 
		values(37,'Выгрузка файла данных Реестра физических лиц в XLSX-формате','ExcelReportPersonsAsyncTask',10000,200000,'количество отобранных для выгрузки в файл записей');
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;