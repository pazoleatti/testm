set serveroutput on size 1000000;

---------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-15922: 1.1 РнРнтб. Реализовать справочник "Классы кредитоспособности"
declare l_task_name varchar2(128) := 'DML Block #1 (SBRFACCTAX-15922 - Credit quality rates))';
begin
	
	INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (601,'Классы кредитоспособности',1,0,0,null);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (6011, 601, 'Класс кредитоспособности', 'NAME',1,1,null,null,1,null,10,1,1,null,null,0,20);
	
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 601, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6011, '1 КЛАСС');		

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 601, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6011, '2 КЛАСС');	

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 601, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6011, '3 КЛАСС');			
	
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

---------------------------------------------------------------------------
COMMIT;
EXIT;