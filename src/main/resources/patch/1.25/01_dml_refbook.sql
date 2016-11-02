set serveroutput on size 1000000;
set linesize 128;
---------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-17342: 1.25 Прибыль. Реализовать справочник "Признаки признания сделки контролируемой в целях налогообложения"
declare l_task_name varchar2(128) := 'RefBook Block #1 (SBRFACCTAX-17342) - new ref_book(I))';
begin
	
	INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (800,'Признаки признания сделки контролируемой в целях налогообложения',1,0,0,null);
		INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (8001, 800, 'Признак',  'CODE',1,1,null,null,1,null,5,1,1,null,null,0,1);
		INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (8002, 800, 'Описание', 'NAME',1,2,null,null,1,null,30,1,0,null,null,0,256);
	
	INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 800, to_date('01.01.2017', 'DD.MM.YYYY'), 0);
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 8001, '+');		
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 8002, 'Сделка признана контролируемой в целях налогообложения');		

	INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 800, to_date('01.01.2017', 'DD.MM.YYYY'), 0);
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 8001, '-');	
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 8002, 'Сделка признана неконтролируемой в целях налогообложения');	
	
	dbms_output.put_line(l_task_name||'[INFO]: Success');	
	
EXCEPTION
	when DUP_VAL_ON_INDEX then
		dbms_output.put_line(l_task_name||'[ERROR]: ref_book or its attributes already exist ('||sqlerrm||')');
		ROLLBACK;
	when OTHERS then
		dbms_output.put_line(l_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/

COMMIT;
---------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-17343: 1.25 Прибыль. Реализовать справочник "Типы сделок по приобретению ценных бумаг"
declare l_task_name varchar2(128) := 'RefBook Block #2 (SBRFACCTAX-17343) - new ref_book(I))';
begin
	
	INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (801,'Типы сделок по приобретению ценных бумаг',1,0,0,null);
		INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (8011, 801, 'Код',  'CODE',1,1,null,null,1,null,5,1,1,null,null,0,1);
		INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (8012, 801, 'Тип сделки', 'TYPE',1,2,null,null,1,null,30,1,0,null,null,0,256);
	
	INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 801, to_date('01.01.2017', 'DD.MM.YYYY'), 0);
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 8011, '1');		
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 8012, 'Ценные бумаги приобретены в рамках сделки ПФИ');		

	INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 801, to_date('01.01.2017', 'DD.MM.YYYY'), 0);
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 8011, '2');	
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 8012, 'Ценные бумаги приобретены в рамках любых сделок, кроме ПФИ');	
	
	dbms_output.put_line(l_task_name||'[INFO]: Success');	
	
EXCEPTION
	when DUP_VAL_ON_INDEX then
		dbms_output.put_line(l_task_name||'[ERROR]: ref_book or its attributes already exist ('||sqlerrm||')');
		ROLLBACK;
	when OTHERS then
		dbms_output.put_line(l_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/

COMMIT;
---------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-17344: 1.25 Прибыль. Реализовать справочник "Типы операций по сделкам"
declare l_task_name varchar2(128) := 'RefBook Block #3 (SBRFACCTAX-17344) - new ref_book(I))';
begin
	
	INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (802,'Типы операций по сделкам',1,0,0,null);
		INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (8021, 802, 'Тип операции', 'TYPE',1,1,null,null,1,null,30,1,1,null,null,0,256);
	
	INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 802, to_date('01.01.2017', 'DD.MM.YYYY'), 0);
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 8021, 'Уступка (новация)');		

	INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 802, to_date('01.01.2017', 'DD.MM.YYYY'), 0);
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 8021, 'Досрочное прекращение');	
	
	dbms_output.put_line(l_task_name||'[INFO]: Success');	
	
EXCEPTION
	when DUP_VAL_ON_INDEX then
		dbms_output.put_line(l_task_name||'[ERROR]: ref_book or its attributes already exist ('||sqlerrm||')');
		ROLLBACK;
	when OTHERS then
		dbms_output.put_line(l_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/

COMMIT;
---------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-17302: 1.25 ТН. Доработать справочник "Коды налоговых льгот транспортного налога"
declare 
	l_task_name varchar2(128) := 'RefBook Block #4 (SBRFACCTAX-17302) - tax codes(T))';
	l_rerun_condition number(1) :=0;
begin
	
	select case when count(*) = 2 then 1 else 0 end into l_rerun_condition from ref_book_attribute where ref_book_id = 6;
	
	if l_rerun_condition = 0 then
				
		update ref_book set name = 'Коды налоговых льгот и вычетов транспортного налога', type=0 where id = 6;
		
		update ref_book_attribute set name = 'Код' where id = 15;
		update ref_book_attribute set name = 'Наименование льготы/вычета', width = 40 where id = 16;
		
		delete from ref_book_value where attribute_id = 300;
		delete from ref_book_attribute where id = 300;
		
		INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, seq_ref_book_record_row_id.nextval, 6, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
			INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 15, '40200');	
			INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 16, 'Налоговый вычет в отношении каждого транспортного средства, имеющего разрешенную максимальную массу свыше 12 тонн, зарегистрированного в реестре транспортных средств системы взимания платы');	
	
		
		dbms_output.put_line(l_task_name||'[INFO]: Success');	
	
	else
		dbms_output.put_line(l_task_name||'[ERROR]: ref_book had already been modified');
	
	end if;
	
EXCEPTION
	when DUP_VAL_ON_INDEX then
		dbms_output.put_line(l_task_name||'[ERROR]: ref_book_value had already been added('||sqlerrm||')');
		ROLLBACK;
	when OTHERS then
		dbms_output.put_line(l_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/

COMMIT;
---------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-17303: 1.25 ТН. Доработать справочник "Параметры налоговых льгот транспортного налога"
declare 
	l_task_name varchar2(128) := 'RefBook Block #5 (SBRFACCTAX-17303) - tax params(T))';
	l_rerun_condition number(1) := 0;
begin
	
	select case when count(*) > 8 then 1 else 0 end into l_rerun_condition from ref_book_attribute where ref_book_id = 7;
	
	if l_rerun_condition = 0 then
		
		INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (702,7,'Основание','BASE',1,8,null,null,1,null,10,0,1,null,null,1,12);
		
		INSERT INTO ref_book_value (record_id, attribute_id, string_value)
		SELECT DISTINCT record_id,
						702 AS attribute_id,
						listagg(lpad(string_value, 4, '0'), '') within GROUP (ORDER  BY attribute_id) over(PARTITION BY record_id)
		FROM   ref_book_value
		WHERE  attribute_id IN (20, 21, 22) AND string_value IS NOT NULL;
		
		UPDATE ref_book_attribute SET precision = 0 WHERE id = 23;
		UPDATE ref_book_attribute SET max_length = 8 WHERE id = 24;
		
	dbms_output.put_line(l_task_name||'[INFO]: Success');

	else
		dbms_output.put_line(l_task_name||'[ERROR]: ref_book had already been modified');
	end if;	
	
EXCEPTION
	when DUP_VAL_ON_INDEX then
		dbms_output.put_line(l_task_name||'[ERROR]: ('||sqlerrm||')');
		ROLLBACK;
	when OTHERS then
		dbms_output.put_line(l_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/

COMMIT;	
---------------------------------------------------------------------------
COMMIT;
EXIT;