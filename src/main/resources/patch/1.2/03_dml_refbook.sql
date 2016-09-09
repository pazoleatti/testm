set serveroutput on size 1000000;
set linesize 128;
---------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-16816: 1.2 ЗемНалог. Изменить справочник "Коды налоговых льгот земельного налога"
declare 
	l_task_name varchar2(128) := 'RefBook Block #1 (SBRFACCTAX-16816 - Tax concession codes(L): hierarchical to lineal)';
	l_rerun_condition decimal(1) := 0;
begin
	select type into l_rerun_condition from ref_book where id=704;
	
	if l_rerun_condition = 1 then --still hierarchical
			delete from ref_book_value where attribute_id = 7044;
			delete from ref_book_attribute where id = 7044;
			update ref_book set type=0 where id = 704;	
	
	else
		dbms_output.put_line(l_task_name||'[INFO]:'||' changes to the ref_book had already been made');
	end if;
	
	dbms_output.put_line(l_task_name||'[INFO]: Success');	
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(l_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;
---------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-16815: 1.2 ЗемНалог. Изменить справочник "Категории земли"
declare 
	l_task_name varchar2(128) := 'RefBook Block #2 (SBRFACCTAX-16815 - Land types(L): hierarchical to lineal)';
	l_rerun_condition decimal(1) := 0;
begin
	select type into l_rerun_condition from ref_book where id=702;
	
	if l_rerun_condition = 1 then --still hierarchical
			delete from ref_book_value where attribute_id = 7023;
			delete from ref_book_attribute where id = 7023;
			update ref_book_attribute set width=50 where id=7022;
			update ref_book set type=0 where id = 702;		
	else
		dbms_output.put_line(l_task_name||'[INFO]:'||' changes to the ref_book had already been made');
	end if;
	
	dbms_output.put_line(l_task_name||'[INFO]: Success');	
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(l_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

---------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-16814: 1.2. ЗемНалог. Изменить перечень периодов в справочнике "Коды, определяющие налоговый (отчетный) период"
declare l_task_name varchar2(128) := 'RefBook Block #3 (SBRFACCTAX-16814 - Report period for ''L'' changes))';
begin
	
	merge into ref_book_value tgt
		using (
		  select rbr.id as record_id, 3002 as attribute_id, case when rbv.string_value in ('21', '22', '23', '34') then 1 else 0 end as number_value 
		  from ref_book_record rbr
		  join ref_book_value rbv on rbv.record_id = rbr.id and rbv.attribute_id = 25) src
		on (tgt.record_id = src.record_id and tgt.attribute_id = src.attribute_id) 
		when matched then
			 update set tgt.number_value = src.number_value where tgt.number_value <> src.number_value; 		
			 
	dbms_output.put_line(l_task_name||'[INFO]: '||sql%rowcount||' row(s) merged into ref_book_value');	
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(l_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
commit;	
---------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-16850: 1.2 ЗемНалог. Реализовать справочник "Параметры налоговых льгот земельного налога"
declare 
	l_task_name varchar2(128) := 'RefBook Block #4 (SBRFACCTAX-16850 - TAX deductions'' params(L))';
	l_rerun_condition decimal(1) := 0;
begin	
	select count(*) into l_rerun_condition from ref_book where id=705;
	
	if l_rerun_condition = 0 then
		INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (705, 'Параметры налоговых льгот земельного налога',  1, 0, 0, null);
		
		INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7051, 705, 'Код субъекта РФ представителя декларации', 'DECLARATION_REGION_ID', 4, 1, 4, 9, 1, null, 10, 1, 1, null, null, 0, null);
		INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7052, 705, 'Код ОКТМО', 'OKTMO',4,2,96,840,1,null,11,1,1,null,null,0,null);
		INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7053, 705, 'Код налоговой льготы', 'TAX_BENEFIT_ID', 4, 3, 704, 7041, 1, null, 20, 1, 1, null, null, 0, null);
		INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7054, 705, 'Параметры - статья','SECTION',1,4,null,null,1,null,10,0,0,null,null,0,4);
		INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7055, 705, 'Параметры - пункт','ITEM',1,5,null,null,1,null,10,0,0,null,null,0,4);
		INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7056, 705, 'Параметры - подпункт','SUBITEM',1,6,null,null,1,null,10,0,0,null,null,0,4);
		INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7057, 705, 'Необлагаемая налогом сумма, руб.', 'REDUCTION_SUM', 2, 7, null, null, 1, 0, 10, 0, 0, null, null, 0, 15);
		INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7058, 705, 'Доля необлагаемой площади', 'REDUCTION_SEGMENT', 1, 8, null, null, 1, null, 10, 0, 0, null, null, 0, 21);
		INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7059, 705, 'Уменьшающий процент, %', 'REDUCTION_PERCENT', 2, 9, null, null, 1, 0, 10, 0, 0, null, null, 0, 3);
		INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7060, 705, 'Пониженная ставка, %', 'REDUCTION_RATE', 2, 10, null, null, 1, 4, 10, 0, 0, null, null, 0, 5);
		INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7061, 705, 'Параметры льготы', 'REDUCTION_PARAMS', 1, 11, null, null, 1, null, 10, 0, 1, null, null, 1, 12);
		
		UPDATE ref_book SET region_attribute_id=7051 WHERE id = 705;
		
		dbms_output.put_line(l_task_name||'[INFO]: Success');		
	else
		dbms_output.put_line(l_task_name||'[INFO]: ref_book or its attributes already exist');
	end if;
		
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
--https://jira.aplana.com/browse/SBRFACCTAX-16895: 1.2 ТН. Справочник "Параметры налоговых льгот транспортного налога" в поле "Код" должен отображаться код льготы
declare 
	l_task_name varchar2(128) := 'RefBook Block #5 (SBRFACCTAX-16895 - TAX deductions'' params(T))';
begin	
	
	update ref_book_attribute set attribute_id = 15 where id = 19;	
	dbms_output.put_line(l_task_name||'[INFO]: Success');		
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(l_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

---------------------------------------------------------------------------
COMMIT;
EXIT;