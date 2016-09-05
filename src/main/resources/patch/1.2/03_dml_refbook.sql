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
	
	dbms_output.put_line(l_task_name||'[INFO]: SUCCESS');	
	
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
	
	dbms_output.put_line(l_task_name||'[INFO]: SUCCESS');	
	
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
COMMIT;
EXIT;