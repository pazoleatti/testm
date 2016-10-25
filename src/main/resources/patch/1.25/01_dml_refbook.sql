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
COMMIT;
EXIT;