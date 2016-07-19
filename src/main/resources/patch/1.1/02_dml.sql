set serveroutput on size 1000000;

--https://jira.aplana.com/browse/SBRFACCTAX-15978: 1.1 Добавить налог "Рыночные интервалы ТЦО"
--https://jira.aplana.com/browse/SBRFACCTAX-15917: 1.1 РнРнтб. Добавить отчетные периоды
declare l_task_name varchar2(128) := 'DML Block #1 (SBRFACCTAX-15978 / SBRFACCTAX-15917 - M))';
begin
	
	insert into tax_type values ('M', 'Рыночные интервалы ТЦО');
	dbms_output.put_line(l_task_name||'[INFO]: '||sql%rowcount||' row(s) added in tax_type');	

	insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (3001,8,'Принадлежность к рыночным интервалам ТЦО','M',2,8,null,null,0,0,10,0,0,null,6,0,1);
	insert into ref_book_value (record_id, attribute_id, number_value) select id as record_id, 3001 as attribute_id, 0 as number_value from ref_book_record where ref_book_id = 8; 
	
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 70, 8, to_date('01.01.2012', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 25, '83');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 26, 'первое полугодие');
		insert into ref_book_value (record_id, attribute_id, number_value) select seq_ref_book_record.currval, id, 0 from ref_book_attribute where ref_book_id = 8 and length(alias)=1;
		insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 820, to_date('01.01.1970', 'DD.MM.YYYY'));
		insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 821, to_date('30.06.1970', 'DD.MM.YYYY'));
		insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 844, to_date('01.01.1970', 'DD.MM.YYYY'));
	
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 71, 8, to_date('01.01.2012', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 25, '84');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 26, 'второе полугодие');
		insert into ref_book_value (record_id, attribute_id, number_value) select seq_ref_book_record.currval, id, 0 from ref_book_attribute where ref_book_id = 8 and length(alias)=1;
		insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 820, to_date('01.07.1970', 'DD.MM.YYYY'));
		insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 821, to_date('31.12.1970', 'DD.MM.YYYY'));
		insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 844, to_date('01.07.1970', 'DD.MM.YYYY'));
		
	merge into ref_book_value tgt
		using (
		  select rbr.id as record_id, 3001 as attribute_id, case when rbv.string_value in ('83', '84', '34') then 1 else 0 end as number_value 
		  from ref_book_record rbr
		  join ref_book_value rbv on rbv.record_id = rbr.id and rbv.attribute_id = 25) src
		on (tgt.record_id = src.record_id and tgt.attribute_id = src.attribute_id) 
		when matched then
			 update set tgt.number_value = src.number_value where tgt.number_value <> src.number_value; 		
			 
	dbms_output.put_line(l_task_name||'[INFO]: '||sql%rowcount||' row(s) merged into ref_book_value');	
	
EXCEPTION
	when DUP_VAL_ON_INDEX then
		dbms_output.put_line(l_task_name||'[ERROR]: tax_type ''M'' already exists ('||sqlerrm||')');
		ROLLBACK;
	when OTHERS then
		dbms_output.put_line(l_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
commit;	
-----------------------------------------------------------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-16250: 1.1 Добавить налог "Земельный налог" в бд
--https://jira.aplana.com/browse/SBRFACCTAX-16251: 1.1 ЗемНалог. Добавить отчетные периоды
declare l_task_name varchar2(128) := 'DML Block #2 (SBRFACCTAX-16250 / SBRFACCTAX-16251 - L))';
begin
	
	insert into tax_type values ('L', 'Земельный налог');
	dbms_output.put_line(l_task_name||'[INFO]: '||sql%rowcount||' row(s) added in tax_type');	

	update ref_book_attribute set ord = 12 where id = 844;
	update ref_book_attribute set ord = 11 where id = 821;
	update ref_book_attribute set ord = 10 where id = 820;
	update ref_book_attribute set ord = 9 where id = 3001;
	update ref_book_attribute set ord = 8 where id = 3000;
	update ref_book_attribute set ord = 7 where id = 31;
	insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (3002,8,'Признак принадлежности к земельному налогу','L',2,7,null,null,0,0,10,0,0,null,6,0,1);
	insert into ref_book_value (record_id, attribute_id, number_value) select id as record_id, 3002 as attribute_id, 0 as number_value from ref_book_record where ref_book_id = 8; 
		
	merge into ref_book_value tgt
		using (
		  select rbr.id as record_id, 3002 as attribute_id, case when rbv.string_value in ('21', '31', '33', '34') then 1 else 0 end as number_value 
		  from ref_book_record rbr
		  join ref_book_value rbv on rbv.record_id = rbr.id and rbv.attribute_id = 25) src
		on (tgt.record_id = src.record_id and tgt.attribute_id = src.attribute_id) 
		when matched then
			 update set tgt.number_value = src.number_value where tgt.number_value <> src.number_value; 		
			 
	dbms_output.put_line(l_task_name||'[INFO]: '||sql%rowcount||' row(s) merged into ref_book_value');	
	
EXCEPTION
	when DUP_VAL_ON_INDEX then
		dbms_output.put_line(l_task_name||'[ERROR]: tax_type ''L'' already exists ('||sqlerrm||')');
		ROLLBACK;
	when OTHERS then
		dbms_output.put_line(l_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
commit;	
-----------------------------------------------------------------------------------------------------------------------------


COMMIT;
EXIT;