set serveroutput on size 1000000;
set linesize 128;
---------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-15922: 1.1 РнРнтб. Реализовать справочник "Классы кредитоспособности"
declare l_task_name varchar2(128) := 'RefBook Block #1 (SBRFACCTAX-15922 - Credit quality rates))';
begin
	
	INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (601,'Классы кредитоспособности',1,0,0,null);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (6011, 601, 'Класс кредитоспособности', 'CREDIT_QUALITY_CLASS',1,1,null,null,1,null,10,1,1,null,null,0,20);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (6012, 601, 'Краткое наименование', 'SHORT_NAME',1,2,null,null,1,null,10,1,2,null,null,0,10);
	
	INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 601, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6011, '1 КЛАСС');		
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6012, '1');		

	INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 601, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6011, '2 КЛАСС');	
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6012, '2');	

	INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 601, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6011, '3 КЛАСС');			
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6012, '3');			
	
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
--https://jira.aplana.com/browse/SBRFACCTAX-15924: 1.1 РнРнтб. Реализовать справочник "Международные кредитные рейтинги по шкале S&P"
declare l_task_name varchar2(128) := 'RefBook Block #2 (SBRFACCTAX-15924/SBRFACCTAX-16417 - S'||chr(38)||'P credit ratings))';
begin
	
	INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (602,'Международные кредитные рейтинги по шкале S'||chr(38)||'P',1,0,0,null);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (6021, 602, 'Международный кредитный рейтинг', 'INTERNATIONAL_CREDIT_RATING',1,1,null,null,1,null,10,1,1,null,null,0,20);
	
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'AAA');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'AA');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'AA+');
		
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 4, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'AA-');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 5, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'A');	
		
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 6, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'A-');	

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 7, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'A+');			

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 8, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'BBB+');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 9, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'BBB');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 10, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'BBB-');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 11, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'BB+');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 12, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'BB');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 13, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'BB-');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 14, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'B+');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 15, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'B');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 16, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'B-');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 17, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'CCC+');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 18, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'CCC');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 19, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'CCC-');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 20, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'CC');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 21, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'C');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 22, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'D');		
	
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
--https://jira.aplana.com/browse/SBRFACCTAX-15921: 1.1 РнРнтб. Реализовать справочник "Кредитные рейтинги"
--https://jira.aplana.com/browse/SBRFACCTAX-16279: 1.1 РнРнтб. Добавить краткое наименование в спр. "Кредитные рейтинги"
declare l_task_name varchar2(128) := 'RefBook Block #3 (SBRFACCTAX-15921/SBRFACCTAX-16279/SBRFACCTAX-16417 - Credit ratings))';
begin
	
	INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (603,'Кредитные рейтинги',1,0,0,null);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (6031, 603, 'Кредитный рейтинг', 'CREDIT_RATING',1,1,null,null,1,null,10,1,1,null,null,0,20);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (6034, 603, 'Краткое наименование', 'SHORT_NAME',1,2,null,null,1,null,10,1,2,null,null,0,10);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (6032, 603, 'Класс кредитоспособности', 'CREDIT_QUALITY_CLASS',4,3,601,6011,1,null,10,1,0,null,null,0,null);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (6033, 603, 'Международный кредитный рейтинг по шкале S'||chr(38)||'P', 'INTERNATIONAL_CREDIT_RATING',4,4,602,6021,1,null,10,0,0,null,null,0,null);
	
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 1');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6034, '1');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '1 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'AAA' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 2');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6034, '2');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '1 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'A' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 3');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6034, '3');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '1 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'A-' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 4, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 4');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6034, '4');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '1 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'A-' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 5, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 5');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6034, '5');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '1 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'BBB+' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 6, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 6');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6034, '6');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '1 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'BBB+' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 7, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 7');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6034, '7');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '1 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'BBB' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 8, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 8');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6034, '8');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '1 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'BBB-' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 9, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 9');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6034, '9');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '1 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'BBB-' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 10, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 10');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6034, '10');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '1 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'BB+' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 11, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 11');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6034, '11');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '1 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'BB+' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 12, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 12');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6034, '12');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '2 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'BB' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 13, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 13');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6034, '13');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '2 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'BB-' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 14, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 14');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6034, '14');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '2 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'BB-' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 15, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 15');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6034, '15');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '2 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'B+' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 16, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 16');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6034, '16');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '2 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'B' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 17, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 17');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6034, '17');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '2 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'B' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 18, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 18');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6034, '18');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '2 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'B-' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 19, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 19');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6034, '19');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '2 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'CCC+' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 20, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 20');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6034, '20');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '2 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'CCC+' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 21, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 21');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6034, '21');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '2 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'CCC' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 22, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 22');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6034, '22');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '2 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'CCC-' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 23, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 23');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6034, '23');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '3 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'CC' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 24, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 24');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6034, '24');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '3 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'C' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 25, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 25');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6034, '25');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '3 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'C' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 26, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 26');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6034, '26');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '3 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'D' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 27, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'RTD');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6034, 'RTD');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '3 КЛАСС' ;
		
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 28, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'D');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6034, 'D');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '3 КЛАСС' ;
		
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 29, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'RD');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6034, 'RD');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '3 КЛАСС' ;
		
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 30, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'НУ');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6034, 'НУ');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '3 КЛАСС' ;
		
		
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
--https://jira.aplana.com/browse/SBRFACCTAX-15923: 1.1 РнРнтб. Реализовать справочник "Кредитные рейтинги и классы кредитоспособности"
declare l_task_name varchar2(128) := 'RefBook Block #4 (SBRFACCTAX-15923 - Credit ratings and credit quality classes))';
begin
	
	INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (604,'Кредитные рейтинги и классы кредитоспособности',1,0,1,null);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (6041, 604, 'Наименование', 'NAME',1,1,null,null,1,null,10,1,0,null,null,0,20);
	
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
--https://jira.aplana.com/browse/SBRFACCTAX-15925: 1.1 РнРнтб. Реализовать справочник "Организационно-правовые формы"
declare l_task_name varchar2(128) := 'RefBook Block #5 (SBRFACCTAX-15925 - Organizational legal forms))';
begin
	
	INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (605,'Организационно-правовые формы',1,0,0,null);
	
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (6051, 605, 'Код ОПФ', 'CODE',2,1,null,null,1,0,5,1,1,null,null,0,3);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (6052, 605, 'Наименование', 'NAME',1,2,null,null,1,null,20,1,0,null,null,0,128);
	
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 39);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'ЮРИДИЧЕСКИЕ ЛИЦА, ЯВЛЯЮЩИЕСЯ КОММЕРЧЕСКИМИ ОРГАНИЗАЦИЯМИ');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 40);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Унитарные предприятия');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 41);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Унитарные предприятия, основанные на праве оперативного управления');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 4, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 42);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Унитарные предприятия, основанные на праве хозяйственного ведения');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 5, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 47);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Открытые акционерные общества');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 6, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 48);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Хозяйственные товарищества и общества');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 7, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 51);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Полные товарищества');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 8, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 52);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Производственные кооперативы');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 9, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 53);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Крестьянские (фермерские) хозяйства (сохраняют статус юридического лица на период до 1 января 2013 года)');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 10, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 60);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Акционерные общества');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 11, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 61);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Хозяйственные партнерства');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 12, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 64);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Товарищества на вере');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 13, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 65);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Общества с ограниченной ответственностью');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 14, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 66);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Общества с дополнительной ответственностью');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 15, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 67);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Закрытые акционерные общества');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 16, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 70);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'ЮРИДИЧЕСКИЕ ЛИЦА, ЯВЛЯЮЩИЕСЯ НЕКОММЕРЧЕСКИМИ ОРГАНИЗАЦИЯМИ');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 17, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 71);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Частные учреждения');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 18, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 72);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Бюджетные учреждения');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 19, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 73);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Автономные учреждения');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 20, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 74);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Казенные учреждения');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 21, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 76);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Садоводческие, огороднические или дачные некоммерческие товарищества');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 22, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 77);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Объединение крестьянских (фермерских) хозяйств');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 23, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 78);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Органы общественной самодеятельности');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 24, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 80);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Территориальные общественные самоуправления');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 25, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 81);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Учреждения');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 26, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 82);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Государственные корпорации');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 27, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 83);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Общественные и религиозные организации (объединения)');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 28, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 84);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Общественные движения');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 29, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 85);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Потребительские кооперативы');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 30, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 86);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Государственные компании');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 31, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 87);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Простые товарищества');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 32, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 88);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Фонды');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 33, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 89);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Прочие некоммерческие организации');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 34, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 90);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Представительства и филиалы');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 35, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 91);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Индивидуальные предприниматели');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 36, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 92);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Паевые инвестиционные фонды');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 37, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 93);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Объединения юридических лиц (ассоциации и союзы)');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 38, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 94);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Товарищества собственников жилья');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 39, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 95);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Крестьянские (фермерские) хозяйства');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 40, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 96);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Некоммерческие партнерства');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 41, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 97);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Автономные некоммерческие организации');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 42, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 98);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'Иные неюридические лица');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 43, 605, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6051, 99);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6052, 'ОРГАНИЗАЦИИ БЕЗ ПРАВ ЮРИДИЧЕСКОГО ЛИЦА');

	
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
--https://jira.aplana.com/browse/SBRFACCTAX-16012: 1.1 РнРнтб. Реализовать справочник "Категории обеспечения"
declare l_task_name varchar2(128) := 'RefBook Block #6 (SBRFACCTAX-16012 - Collateral types))';
begin	
	INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (606,'Категории обеспечения',1,0,0,null);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (6061, 606, 'Наименование', 'NAME',1,1,null,null,1,null,10,1,1,null,null,0,50);
	
	INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 606, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6061, 'Необеспеченный');		

	INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 606, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6061, 'Частично обеспеченный');	

	INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 606, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6061, 'Полностью обеспеченный');			
	
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
--https://jira.aplana.com/browse/SBRFACCTAX-16013: 1.1 РнРнтб. Реализовать справочник "Исключаемые типы кредитов"
declare l_task_name varchar2(128) := 'RefBook Block #7 (SBRFACCTAX-16013 - Excludable credit types))';
begin	
	INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (607,'Исключаемые типы кредитов',1,0,0,null);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (6071, 607, 'Код','CODE',1,1,null,null,1,null,5,1,1,null,null,0,10);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (6072, 607, 'Наименование','NAME',1,2,null,null,1,null,25,1,2,null,null,0,256);
	
	INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 607, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6071, 'РП');		
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6072, 'Сделки обратного РЕПО');	

	INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 607, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6071, 'ВК');	
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6072, 'Кредиты, предоставленные с применением векселей');	

	INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 607, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6071, 'УВ');
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6072, 'Учтенные векселя');	
	
	INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 4, 607, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6071, 'СГ');
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6072, 'Суммы, не взысканные по своим гарантиям');	
	
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
--https://jira.aplana.com/browse/SBRFACCTAX-16273: 1.1 ЗемНалог. Реализовать справочник "Периоды строительства"
declare l_task_name varchar2(128) := 'RefBook Block #8 (SBRFACCTAX-16273 - Construction phases))';
begin	
	INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (701,'Периоды строительства',1,0,0,null);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7011, 701, 'Код','CODE',2,1,null,null,1,0,5,1,1,null,null,0,2);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7012, 701, 'Наименование','NAME',1,2,null,null,1,null,25,1,0,null,null,0,255);
	
	INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 701, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		INSERT INTO ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 7011, 1);		
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7012, '3 года');	

	INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 701, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		INSERT INTO ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 7011, 2);	
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7012, 'Свыше 3 лет');	
	
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
--https://jira.aplana.com/browse/SBRFACCTAX-16291: 1.1 ЗемНалог. Добавить элементы в справочник "Коды представления налоговой декларации по месту нахождения (учёта)"
declare l_task_name varchar2(128) := 'RefBook Block #9 (SBRFACCTAX-16291 - Tax declaration''s codes - new records))';
begin	
	
	for x in (WITH t as (SELECT '250' as code from dual UNION ALL SELECT '251' as code from dual)
				SELECT t.code, d.id
				FROM   t
				LEFT   JOIN (SELECT r.id, v.string_value AS code
							 FROM   ref_book_record r
							 JOIN   ref_book_value v ON r.id = v.record_id AND
														v.attribute_id = 3
							 WHERE  r.ref_book_id = 2 AND v.string_value IN ('250', '251') AND
									r.version = to_date('01.01.2012', 'DD.MM.YYYY') AND
									r.status <> -1) d ON t.code = d.code) loop
		if (x.code = '250' and x.id is null) then
			INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, seq_ref_book_record_row_id.nextval, 2, to_date('01.01.2012', 'DD.MM.YYYY'), 0);
				INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 3, '250');		
				INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 4, 'По месту нахождения участка недр, предоставленного на условиях СРП');	
				
			dbms_output.put_line(l_task_name||'[INFO]: Code ''250'' added');		
		elsif (x.code = '250' and x.id is not null) then	
			dbms_output.put_line(l_task_name||'[WARN]: Code ''250'' already exists');		
		end if;
		
		if (x.code = '251' and x.id is null) then
			INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, seq_ref_book_record_row_id.nextval, 2, to_date('01.01.2012', 'DD.MM.YYYY'), 0);
				INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 3, '251');		
				INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 4, 'По месту нахождения организации – инвестора СРП, если участок недр расположен на континентальном шельфе Российской Федерации и (или) в пределах исключительной экономической зоны Российской Федерации');	
				
			dbms_output.put_line(l_task_name||'[INFO]: Code ''251'' added');		
		elsif (x.code = '251' and x.id is not null) then	
			dbms_output.put_line(l_task_name||'[WARN]: Code ''251'' already exists');		
		end if;			
	end loop;			
	
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
--https://jira.aplana.com/browse/SBRFACCTAX-16309: 1.1 НДС. Реализовать и наполнить справочник "Коды видов операций НДС"
declare l_task_name varchar2(128) := 'RefBook Block #10 (SBRFACCTAX-16309 - Vat operation codes))';
begin	
	INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (650,'Коды видов операций НДС',1,0,0,null);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (6501, 650, 'Код вида операции','CODE',1,1,null,null,1,null,5,1,1,null,null,0,2);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (6502, 650, 'Наименование вида операции','NAME',1,2,null,null,1,null,25,1,0,null,null,0,1000);
	
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 650, to_date('01.07.2016', 'DD.MM.YYYY'), 0);	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6501, '01');	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6502, 'Отгрузка (передача) или приобретение товаров (работ, услуг), имущественных прав, включая операции, перечисленные в подпунктах 2 и 3 пункта 1 статьи 146, 162, в пунктах 3, 4, 5.1 статьи 154, в подпункте 1 пункта 3 статьи 170 Налогового кодекса Российской Федерации (Собрание законодательства Российской Федерации, 2000, N 32, ст. 3340; 2016, N 14, ст. 1902), операции, облагаемые по налоговой ставке 0 процентов, операции, осуществляемые на основе договоров комиссии, агентских договоров, предусматривающих реализацию и (или) приобретение товаров (работ, услуг), имущественных прав от имени комиссионера (агента) или на основе договоров транспортной экспедиции, операции по возврату налогоплательщиком-покупателем товаров продавцу или получение продавцом от указанного лица товаров, за исключением операций, перечисленных по кодам 06; 10; 13; 14; 15; 16; 27; составление или получение единого корректировочного счета-фактуры');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 650, to_date('01.07.2016', 'DD.MM.YYYY'), 0);	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6501, '02');	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6502, 'Оплата, частичная оплата (полученная или переданная) в счет предстоящих поставок товаров (работ, услуг), имущественных прав, включая операции, осуществляемые на основе договоров комиссии, агентских договоров, предусматривающих реализацию и (или) приобретение товаров (работ, услуг), имущественных прав от имени комиссионера (агента) или на основе договоров транспортной экспедиции, за исключением операций, перечисленных по кодам 06; 28');
		
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 4, 650, to_date('01.07.2016', 'DD.MM.YYYY'), 0);	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6501, '06');	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6502, 'Операции, совершаемые налоговыми агентами, перечисленными в статье 161 Налогового кодекса Российской Федерации (Собрание законодательства Российской Федерации, 2000, N 32, ст. 3340; 2016, N 14, ст. 1902), в том числе операции по приобретению товаров (работ, услуг), имущественных прав на основе договоров поручения, комиссии, агентских договоров, заключенных налоговыми агентами с налогоплательщиком, за исключением операций, указанных в пунктах 4 и 5 данной статьи Налогового кодекса Российской Федерации');
		
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 5, 650, to_date('01.07.2016', 'DD.MM.YYYY'), 0);	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6501, '10');	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6502, 'Отгрузка (передача) товаров (выполнение работ, оказание услуг), имущественных прав на безвозмездной основе');
		
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 6, 650, to_date('01.07.2016', 'DD.MM.YYYY'), 0);	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6501, '13');	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6502, 'Выполнение подрядными организациями (застройщиками, заказчиками, выполняющими функции застройщика, техническими заказчиками) работ при осуществлении капитального строительства, модернизации (реконструкции) объектов недвижимости или приобретение этих работ налогоплательщиками-инвесторами; передача указанными лицами (приобретение) объектов завершенного (незавершенного) капитального строительства, оборудования, материалов в рамках исполнения договоров по капитальному строительству (модернизации, реконструкции)');
	
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 7, 650, to_date('01.07.2016', 'DD.MM.YYYY'), 0);	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6501, '14');	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6502, 'Передача имущественных прав, перечисленных в пунктах 1 - 4 статьи 155 Налогового кодекса Российской Федерации');
		
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 8, 650, to_date('01.07.2016', 'DD.MM.YYYY'), 0);	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6501, '15');	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6502, 'Составление (получение) счета-фактуры комиссионером (агентом) при реализации (получении) товаров (работ, услуг), имущественных прав от своего имени, в котором отражены данные в отношении собственных товаров (работ, услуг), имущественных прав, и данные в отношении товаров (работ, услуг), имущественных прав, реализуемых (приобретаемых) по договору комиссии (агентскому договору)');
		
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 9, 650, to_date('01.07.2016', 'DD.MM.YYYY'), 0);	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6501, '16');	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6502, 'Получение продавцом товаров, возвращенных покупателями, не являющимися налогоплательщиками налога на добавленную стоимость, и налогоплательщиками, освобожденным от исполнения обязанностей налогоплательщика, связанных с исчислением и уплатой налога, включая случаи частичного возврата товаров указанными лицами, а также отказ от товаров (работ, услуг) в случае, предусмотренном в абзаце втором пункта 5 статьи 171 Налогового кодекса Российской Федерации, за исключением операций, перечисленных по коду 17');
		
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 10, 650, to_date('01.07.2016', 'DD.MM.YYYY'), 0);	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6501, '17');	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6502, 'Получение продавцом товаров, возвращенных физическими лицами, а также отказ от товаров (работ, услуг) в случае, указанном в абзаце втором пункта 5 статьи 171 Налогового кодекса Российской Федерации');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 11, 650, to_date('01.07.2016', 'DD.MM.YYYY'), 0);	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6501, '18');	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6502, 'Составление или получение корректировочного счета-фактуры в связи с изменением стоимости отгруженных товаров (работ, услуг), переданных имущественных прав в сторону уменьшения, в том числе в случае уменьшения цен (тарифов) и (или) уменьшения количества (объема) отгруженных товаров (работ, услуг), переданных имущественных прав');
		
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 12, 650, to_date('01.07.2016', 'DD.MM.YYYY'), 0);	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6501, '19');	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6502, 'Ввоз товаров на территорию Российской Федерации и иные территории, находящиеся под ее юрисдикцией, с территории государств Евразийского экономического союза');
			
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 13, 650, to_date('01.07.2016', 'DD.MM.YYYY'), 0);	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6501, '20');	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6502, 'Ввоз товаров на территорию Российской Федерации и иные территории, находящиеся под ее юрисдикцией, в таможенных процедурах выпуска для внутреннего потребления, переработки для внутреннего потребления, временного ввоза и переработки вне таможенной территории');
	
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 14, 650, to_date('01.07.2016', 'DD.MM.YYYY'), 0);	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6501, '21');	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6502, 'Операции по восстановлению сумм налога, указанные в пункте 8 статьи 145, пункте 3 статьи 170 (за исключением подпунктов 1 и 4 пункта 3 статьи 170), статье 171.1 Налогового кодекса Российской Федерации, а также при совершении операций, облагаемых по налоговой ставке 0 процентов по налогу на добавленную стоимость');
	
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 15, 650, to_date('01.07.2016', 'DD.MM.YYYY'), 0);	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6501, '22');	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6502, 'Операции по возврату авансовых платежей в случаях, перечисленных в абзаце втором пункта 5 статьи 171, а также операции, перечисленные в пункте 6 статьи 172 Налогового кодекса Российской Федерации');
	
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 16, 650, to_date('01.07.2016', 'DD.MM.YYYY'), 0);	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6501, '23');	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6502, 'Приобретение услуг, оформленных бланками строгой отчетности, в случаях, предусмотренных пунктом 7 статьи 171 Налогового кодекса Российской Федерации');
	
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 17, 650, to_date('01.07.2016', 'DD.MM.YYYY'), 0);	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6501, '24');	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6502, 'Регистрация счетов-фактур в книге покупок в случаях, предусмотренных абзацем вторым пункта 9 статьи 165 и пунктом 10 статьи 171 Налогового кодекса Российской Федерации');
	
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 18, 650, to_date('01.07.2016', 'DD.MM.YYYY'), 0);	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6501, '25');	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6502, 'Регистрация счетов-фактур в книге покупок в отношении сумм налога на добавленную стоимость, ранее восстановленных при совершении операций, облагаемых по налоговой ставке 0 процентов, а также в случаях, предусмотренных пунктом 7 статьи 172 Налогового кодекса Российской Федерации');
	
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 19, 650, to_date('01.07.2016', 'DD.MM.YYYY'), 0);	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6501, '26');	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6502, 'Составление продавцом счетов-фактур, первичных учетных документов, иных документов, содержащих суммарные (сводные) данные по операциям, совершенным в течение календарного месяца (квартала) при реализации товаров (работ, услуг), имущественных прав (в том числе в случае изменения стоимости отгруженных товаров (работ, услуг, имущественных прав)) лицам, не являющимся налогоплательщиками налога на добавленную стоимость, и налогоплательщикам, освобожденным от исполнения обязанностей налогоплательщика налога на добавленную стоимость, связанных с исчислением и уплатой налога, а также при получении от указанных лиц оплаты (частичной оплаты) в счет предстоящих поставок товаров (работ, услуг), имущественных прав; регистрация указанных документов в книге покупок в случаях, предусмотренных пунктами 6 и 10 статьи 172 Налогового кодекса Российской Федерации');
	
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 20, 650, to_date('01.07.2016', 'DD.MM.YYYY'), 0);	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6501, '27');	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6502, 'Составление счета-фактуры на основании двух и более счетов-фактур при реализации и (или) приобретении товаров (работ, услуг), имущественных прав в случае, предусмотренном пунктом 3.1 статьи 169 Налогового кодекса Российской Федерации, а также получение указанного счета-фактуры налогоплательщиком');
	
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 21, 650, to_date('01.07.2016', 'DD.MM.YYYY'), 0);	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6501, '28');	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6502, 'Составление счета-фактуры на основании двух и более счетов-фактур при получении оплаты, частичной оплаты в счет предстоящих поставок товаров (работ, услуг), имущественных прав, в случае, предусмотренном пунктом 3.1 статьи 169 Налогового кодекса Российской Федерации, а также получение указанного счета-фактуры налогоплательщиком');
	
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 22, 650, to_date('01.07.2016', 'DD.MM.YYYY'), 0);	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6501, '29');	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6502, 'Корректировка реализации товаров (работ, услуг), передачи имущественных прав, предприятия в целом как имущественного комплекса на основании пункта 6 статьи 105.3 Налогового кодекса Российской Федерации (Собрание законодательства Российской Федерации, 1998, N 31, ст. 3824; 2016, N 1, ст. 6)');
	
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 23, 650, to_date('01.07.2016', 'DD.MM.YYYY'), 0);	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6501, '30');	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6502, 'Отгрузка товаров, в отношении которых при таможенном декларировании был исчислен НДС в соответствии с абзацем первым подпункта 1.1 пункта 1 статьи 151 Налогового кодекса Российской Федерации');
	
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 24, 650, to_date('01.07.2016', 'DD.MM.YYYY'), 0);	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6501, '31');	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6502, 'Операция по уплате сумм НДС, исчисленных при таможенном декларировании товаров в случаях, предусмотренных абзацем вторым подпункта 1.1 пункта 1 статьи 151 Налогового кодекса Российской Федерации');
	
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 25, 650, to_date('01.07.2016', 'DD.MM.YYYY'), 0);	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6501, '32');	
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6502, 'Принятие к вычету сумм налога на добавленную стоимость, уплаченных или подлежащих уплате в случаях, предусмотренных пунктом 14 статьи 171 Налогового кодекса Российской Федерации');
	
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
--https://jira.aplana.com/browse/SBRFACCTAX-16278: 1.1 РнРнтб. Реализовать справочник "Группы видов обязательств"
declare l_task_name varchar2(128) := 'RefBook Block #11 (SBRFACCTAX-16278 - Labilities groups))';
begin	
	INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (608,'Группы видов обязательств',1,0,0,null);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (6081, 608, 'Код','CODE',2,1,null,null,1,0,5,1,1,null,null,0,2);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (6082, 608, 'Наименование группы','NAME',1,2,null,null,1,null,25,1,2,null,null,0,256);
	
	INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 608, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		INSERT INTO ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6081, 1);		
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6082, 'Гарантия (поручительство) в пользу таможенного органа / налогового органа');	

	INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 608, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		INSERT INTO ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 6081, 2);	
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6082, 'Иные гарантии и аккредитивы');	
	
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
--https://jira.aplana.com/browse/SBRFACCTAX-16277: 1.1 РнРнтб. Реализовать справочник "Виды обязательств"
declare l_task_name varchar2(128) := 'RefBook Block #12 (SBRFACCTAX-16277 - Labilities types))';
begin	
	INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (609,'Виды обязательств',1,0,0,null);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (6091, 609, 'Наименование вида обязательств', 'NAME',1,1,null,null,1,null,40,1,1,null,null,0,500);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (6092, 609, 'Группа видов обязательств', 'GROUP_CODE',4,2,608,6081,1,null,10,1,0,null,null,0,null);
	
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'Гарантия в пользу налоговых органов - прочее (см. столбец  "комментарии").');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=1;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'Гарантия в пользу Росалкогольрегулирования');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=2;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'Гарантия в пользу Рособоронэкспорт');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=2;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 4, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'Гарантия в пользу таможенных органов');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=1;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 5, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'гарантия возврата авансового платежа');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=2;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 6, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'Гарантия встречного обеспечения');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=2;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 7, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'гарантия исполнения обязательств по государственному/ муниципальному контракту');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=2;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 8, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'гарантия исполнения обязательств по государственному/ муниципальному контракту (в рамках закона №44-ФЗ)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=2;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 9, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'гарантия исполнения обязательств по договору/ контракту');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=2;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 10, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'гарантия исполнения обязательств по договору/ контракту (кроме гарантий исполнения обязательств по кредитному договору/ договору займа  и  гарантий в рамках закона №44-ФЗ)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=2;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 11, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'гарантия исполнения обязательств по контракту, заключаемому по результатам конкурса');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=2;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 12, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'гарантия исполнения обязательств по кредитному договору/ договору займа');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=2;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 13, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'Гарантия исполнения предложения по выкупу ценных бумаг');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=2;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 14, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'Договорная гарантия - суть гарантируемых обязательств: -гарантия возврата авансового платежа.');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=2;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 15, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'Договорная гарантия - суть гарантируемых обязательств: -гарантия возврата авансового платежа; -гарантия качества (исполнение обязательств в гарантийный период).');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=2;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 16, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'Договорная гарантия - суть гарантируемых обязательств: -гарантия исполнения обязательств по договору/контракту.');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=2;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 17, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'Договорная гарантия - суть гарантируемых обязательств: -гарантия исполнения обязательств по договору/контракту; -гарантия возврата авансового платежа.');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=2;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 18, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'Договорная гарантия - суть гарантируемых обязательств: -гарантия исполнения обязательств по договору/контракту; -гарантия возврата авансового платежа; -гарантия качества (исполнение обязательств в гарантийный период).');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=2;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 19, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'Договорная гарантия - суть гарантируемых обязательств: -гарантия исполнения обязательств по договору/контракту; -гарантия качества (исполнение обязательств в гарантийный период).');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=2;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 20, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'Договорная гарантия - суть гарантируемых обязательств: -гарантия качества (исполнение обязательств в гарантийный период).');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=2;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 21, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'договорная гарантия (гарантия исполнения гарантийных обязательств Принципала -кроме   гарантий в рамках закона №44-фз, кроме пакетных гарантий)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=2;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 22, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'договорная гарантия (гарантия исполнения обязательств по государственному/ муниципальному контракту в рамках закона №44-фз в рамках пакетного предложения)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=2;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 23, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'договорная гарантия (гарантия исполнения обязательств по государственному/ муниципальному контракту в рамках закона №44-фз, кроме пакетных гарантий)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=2;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 24, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'договорная гарантия (гарантия исполнения обязательств по договору -кроме   гарантий в рамках закона №44-фз, кроме пакетных гарантий, кроме гарантий исполнения гарантийных обязательств)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=2;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 25, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'договорная пакетная гарантия (гарантия исполнения обязательств по договору в рамках пакетного предложения-кроме   гарантий в рамках закона №44-фз)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=2;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 26, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'Налоговая акцизная гарантия');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=1;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 27, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'Налоговая гарантия в рамках заявительного порядка возмещения НДС');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=1;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 28, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'Налоговая гарантия освобождения от уплаты авансового платежа акциза по алкогольной продукции');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=1;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 29, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'Налоговая гарантия/поручительство исполнения обязательств по инвестиционному налоговому кредиту');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=1;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 30, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'ПВН- репутационная гарантия');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=2;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 31, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'Поручительство за кредитную организацию');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=2;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 32, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'Прочее');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=2;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 33, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'Тендерная гарантия');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=2;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 34, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'тендерная гарантия (независимо от наличия и вида обеспечения)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=2;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 35, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'Финансовая гарантия - гарантия исполнения обязательств по кредитному договору/ договору займа');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=2;

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 36, 609, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6091, 'Финансовая гарантия - кроме гарантий  исполнения обязательств по кредитному договору/ договору займа');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6092, record_id from ref_book_value where attribute_id = 6081 and number_value=2;
	
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
--https://jira.aplana.com/browse/SBRFACCTAX-16479: 1.1 ЗемНалог. Реализовать справочник "Категории земли"
declare l_task_name varchar2(128) := 'RefBook Block #13 (SBRFACCTAX-16479 - Land types))';
begin
	
	INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (702,'Категории земли',1,1,0,null);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7021, 702, 'Код', 'CODE',1,1,null,null,1,null,10,1,1,null,null,0,12);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7022, 702, 'Наименование категории земли', 'NAME',1,2,null,null,1,null,10,1,0,null,null,0,1000);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7023, 702, 'Код родительской записи', 'PARENT_ID',4,3,702,7021,1,null,10,0,0,null,null,0,null);
	
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 702, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7021, '003001000000');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7022, 'ЗЕМЛИ СЕЛЬСКОХОЗЯЙСТВЕННОГО НАЗНАЧЕНИЯ');
		
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 702, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7021, '003001000010');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7022, 'Сельскохозяйственные угодья');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7023, record_id from ref_book_value where attribute_id = 7021  and string_value='003001000000';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 702, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7021, '003001000020');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7022, 'Земли, занятые внутрихозяйственными дорогами, коммуникациями, лесными насаждениями, предназначенными для обеспечения защиты земель от воздействия негативных (вредных) природных, антропогенных и техногенных явлений, водными объектами, а также занятые зданиями, строениями, сооружениями, используемыми для производства, хранения и переработки сельскохозяйственной продукции');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7023, record_id from ref_book_value where attribute_id = 7021  and string_value='003001000000';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 4, 702, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7021, '003001000030');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7022, 'Прочие земли сельскохозяйственного назначения');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7023, record_id from ref_book_value where attribute_id = 7021  and string_value='003001000000';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 5, 702, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7021, '003002000000');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7022, 'ЗЕМЛИ НАСЕЛЕННЫХ ПУНКТОВ');
		
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 6, 702, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7021, '003002000010');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7022, 'Земли в пределах населенных пунктов, отнесенные к территориальным зонам сельскохозяйственного использования');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7023, record_id from ref_book_value where attribute_id = 7021  and string_value='003002000000';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 7, 702, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7021, '003002000020');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7022, 'Земельные участки, занятые жилищным фондом и объектами инженерной инфраструктуры жилищно-коммунального комплекса');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7023, record_id from ref_book_value where attribute_id = 7021  and string_value='003002000000';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 8, 702, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7021, '003002000030');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7022, 'Земельные участки, предоставленные для жилищного строительства');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7023, record_id from ref_book_value where attribute_id = 7021  and string_value='003002000000';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 9, 702, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7021, '003002000040');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7022, 'Земельные участки, приобретенные в собственность юридическими и физическими лицами на условиях осуществления на них жилищного строительства (за исключением индивидуального жилищного строительства)');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7023, record_id from ref_book_value where attribute_id = 7021  and string_value='003002000000';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 10, 702, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7021, '003002000050');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7022, 'Земельные участки, приобретенные в собственность физическими лицами на условиях осуществления на них индивидуального жилищного строительства');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7023, record_id from ref_book_value where attribute_id = 7021  and string_value='003002000000';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 11, 702, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7021, '003002000060');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7022, 'Земельные участки, предоставленные для ведения личного подсобного хозяйства, садоводства и огородничества или животноводства, а также дачного хозяйства');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7023, record_id from ref_book_value where attribute_id = 7021  and string_value='003002000000';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 12, 702, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7021, '003002000070');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7022, 'Земельные участки, предоставленные юридическим лицам для ведения садоводства и огородничества или животноводства, а также дачного хозяйства');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7023, record_id from ref_book_value where attribute_id = 7021  and string_value='003002000000';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 13, 702, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7021, '003002000080');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7022, 'Земельные участки, предоставленные физическим лицам для личного подсобного хозяйства, садоводства и огородничества или животноводства, а также дачного хозяйства');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7023, record_id from ref_book_value where attribute_id = 7021  and string_value='003002000000';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 14, 702, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7021, '003002000090');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7022, 'Земли в пределах населенных пунктов, отнесенные к производственным территориальным зонам и зонам инженерных и транспортных инфраструктур');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7023, record_id from ref_book_value where attribute_id = 7021  and string_value='003002000000';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 15, 702, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7021, '003002000100');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7022, 'Прочие земельные участки');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7023, record_id from ref_book_value where attribute_id = 7021  and string_value='003002000000';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 16, 702, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7021, '003003000000');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7022, 'ЗЕМЛИ ПРОМЫШЛЕННОСТИ, ЭНЕРГЕТИКИ, ТРАНСПОРТА, СВЯЗИ, РАДИОВЕЩАНИЯ, ТЕЛЕВИДЕНИЯ, ИНФОРМАТИКИ, ЗЕМЛИ ДЛЯ ОБЕСПЕЧЕНИЯ КОСМИЧЕСКОЙ ДЕЯТЕЛЬНОСТИ, ЗЕМЛИ ОБОРОНЫ, БЕЗОПАСНОСТИ И ЗЕМЛИ ИНОГО СПЕЦИАЛЬНОГО НАЗНАЧЕНИЯ');
		
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 17, 702, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7021, '003003000010');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7022, 'Земли промышленности');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7023, record_id from ref_book_value where attribute_id = 7021  and string_value='003003000000';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 18, 702, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7021, '003003000020');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7022, 'Земли энергетики');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7023, record_id from ref_book_value where attribute_id = 7021  and string_value='003003000000';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 19, 702, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7021, '003003000030');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7022, 'Земли транспорта');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7023, record_id from ref_book_value where attribute_id = 7021  and string_value='003003000000';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 20, 702, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7021, '003003000040');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7022, 'Земли связи, радиовещания, телевидения, информатики');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7023, record_id from ref_book_value where attribute_id = 7021  and string_value='003003000000';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 21, 702, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7021, '003003000050');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7022, 'Прочие земли');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7023, record_id from ref_book_value where attribute_id = 7021  and string_value='003003000000';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 22, 702, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7021, '003003000060');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7022, 'Земли обороны');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7023, record_id from ref_book_value where attribute_id = 7021  and string_value='003003000000';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 23, 702, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7021, '003003000070');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7022, 'Земли безопасности');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7023, record_id from ref_book_value where attribute_id = 7021  and string_value='003003000000';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 24, 702, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7021, '003003000080');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7022, 'Земли иного специального назначения');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7023, record_id from ref_book_value where attribute_id = 7021  and string_value='003003000000';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 25, 702, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7021, '003004000000');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7022, 'ЗЕМЛИ ОСОБО ОХРАНЯЕМЫХ ТЕРРИТОРИЙ И ОБЪЕКТОВ');
		
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 26, 702, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7021, '003005000000');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7022, 'ЗЕМЛИ ЛЕСНОГО ФОНДА');
		
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 27, 702, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7021, '003006000000');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7022, 'ЗЕМЛИ ВОДНОГО ФОНДА');
		
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 28, 702, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7021, '003007000000');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7022, 'ЗЕМЛИ ЗАПАСА');
		
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 29, 702, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7021, '003008000000');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7022, 'Прочие земли');
	
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
--https://jira.aplana.com/browse/SBRFACCTAX-16478: 1.1 ЗемНалог. Реализовать справочник "Коды бюджетной классификации земельного налога"
declare l_task_name varchar2(128) := 'RefBook Block #14 (SBRFACCTAX-16478 - Budget classification codes(L)))';
begin
	
	INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (703,'Коды бюджетной классификации земельного налога',1,0,0,null);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7031, 703, 'Код', 'CODE',1,1,null,null,1,null,5,1,1,null,null,0,20);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7032, 703, 'Наименование', 'NAME',1,2,null,null,1,null,20,1,0,null,null,0,256);
	
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 703, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7031, '10606031030000110');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7032, 'Земельный налог с организаций, обладающих земельным участком, расположенным в границах внутригородских муниципальных образований городов федерального значения');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 703, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7031, '10606032040000110');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7032, 'Земельный налог с организаций, обладающих земельным участком, расположенным в границах городских округов');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 703, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7031, '10606032110000110');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7032, 'Земельный налог с организаций, обладающих земельным участком, расположенным в границах городских округов с внутригородским делением');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 4, 703, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7031, '10606032120000110');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7032, 'Земельный налог с организаций, обладающих земельным участком, расположенным в границах внутригородских районов');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 5, 703, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7031, '10606033050000110');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7032, 'Земельный налог с организаций, обладающих земельным участком, расположенным в границах межселенных территорий');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 6, 703, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7031, '10606033100000110');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7032, 'Земельный налог с организаций, обладающих земельным участком, расположенным в границах сельских поселений');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 7, 703, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7031, '10606033130000110');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7032, 'Земельный налог с организаций, обладающих земельным участком, расположенным в границах городских поселений');
	
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
--https://jira.aplana.com/browse/SBRFACCTAX-16477: 1.1 ЗемНалог. Реализовать справочник "Коды налоговых льгот земельного налога"
declare l_task_name varchar2(128) := 'RefBook Block #15 (SBRFACCTAX-16477 - Tax concession codes(L))';
begin
	
	INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (704,'Коды налоговых льгот земельного налога',1,1,0,null);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7041, 704, 'Код', 'CODE',1,1,null,null,1,null,10,1,1,null,null,0,7);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7042, 704, 'Наименование льготы', 'NAME',1,2,null,null,1,null,30,1,0,null,null,0,2000);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7043, 704, 'Основание', 'BASE',1,3,null,null,1,null,10,1,0,null,null,0,500);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7044, 704, 'Код родительской записи', 'PARENT_ID',4,4,704,7041,1,null,10,0,0,null,null,0,null);
	
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 704, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7041, '3021000');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7042, 'НАЛОГОВЫЕ ЛЬГОТЫ, ПРЕДОСТАВЛЯЕМЫЕ НА ФЕДЕРАЛЬНОМ УРОВНЕ');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7043, 'Глава 31 "Земельный налог" Налогового кодекса');
		
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 704, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7041, '3021100');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7042, 'НАЛОГОВЫЕ ЛЬГОТЫ, ПРЕДУСМАТРИВАЮЩИЕ ОСВОБОЖДЕНИЕ ОТ НАЛОГООБЛОЖЕНИЯ');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7043, 'Статья 395 Налогового кодекса');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7044, record_id from ref_book_value where attribute_id = 7041  and string_value='3021000';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 704, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7041, '3021110');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7042, 'Организации и учреждения уголовно-исполнительной системы Министерства юстиции Российской Федерации - в отношении земельных участков, предоставленных для непосредственного выполнения возложенных на эти организации и учреждения функций');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7043, 'Пункт 1 статьи 395 Налогового кодекса');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7044, record_id from ref_book_value where attribute_id = 7041  and string_value='3021100';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 4, 704, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7041, '3021120');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7042, 'Организации - в отношении земельных участков, занятых государственными автомобильными дорогами общего пользования');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7043, 'Пункт 2 статьи 395 Налогового кодекса');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7044, record_id from ref_book_value where attribute_id = 7041  and string_value='3021100';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 5, 704, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7041, '3021160');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7042, 'Религиозные организации - в отношении принадлежащих им земельных участков, на которых расположены здания, строения и сооружения религиозного и благотворительного назначения');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7043, 'Пункт 4 статьи 395 Налогового кодекса');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7044, record_id from ref_book_value where attribute_id = 7041  and string_value='3021100';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 6, 704, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7041, '3021170');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7042, 'Общероссийские общественные организации инвалидов (в том числе созданные как союзы общественных организаций инвалидов), среди членов которых инвалиды и их законные представители составляют не менее 80 процентов, - в отношении земельных участков, используемых ими для осуществления уставной деятельности');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7043, 'Абзац 1 пункта 5 статьи 395 Налогового кодекса');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7044, record_id from ref_book_value where attribute_id = 7041  and string_value='3021100';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 7, 704, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7041, '3021180');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7042, 'Организации, уставный капитал которых полностью состоит из вкладов указанных общероссийских общественных организаций инвалидов, если среднесписочная численность инвалидов среди их работников составляет не менее 50 процентов, а их доля в фонде оплаты труда - не менее 25 процентов, - в отношении земельных участков, используемых ими для производства и (или) реализации товаров (за исключением подакцизных товаров, минерального сырья и иных полезных ископаемых, а также иных товаров по перечню, утверждаемому Правительством Российской Федерации по согласованию с общероссийскими общественными организациями инвалидов), работ и услуг (за исключением брокерских и иных посреднических услуг)');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7043, 'Абзац 2 пункта 5 статьи 395 Налогового кодекса');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7044, record_id from ref_book_value where attribute_id = 7041  and string_value='3021100';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 8, 704, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7041, '3021190');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7042, 'Учреждения, единственными собственниками имущества которых являются указанные общероссийские общественные организации инвалидов, - в отношении земельных участков, используемых ими для достижения образовательных, культурных, лечебно-оздоровительных, физкультурно-спортивных, научных, информационных и иных целей социальной защиты и реабилитации инвалидов, а также для оказания правовой и иной помощи инвалидам, детям-инвалидам и их родителям');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7043, 'Абзац 3 пункта 5 статьи 395 Налогового кодекса');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7044, record_id from ref_book_value where attribute_id = 7041  and string_value='3021100';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 9, 704, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7041, '3021191');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7042, 'Организации народных художественных промыслов - в отношении земельных участков, находящихся в местах традиционного бытования народных художественных промыслов и используемых для производства и реализации изделий народных художественных промыслов');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7043, 'Пункт 6 статьи 395 Налогового кодекса');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7044, record_id from ref_book_value where attribute_id = 7041  and string_value='3021100';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 10, 704, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7041, '3021192');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7042, 'Физические лица, относящиеся к коренным малочисленным народам Севера, Сибири и Дальнего Востока Российской Федерации, а также общины таких народов - в отношении земельных участков, используемых для сохранения и развития их традиционного образа жизни, хозяйствования и промыслов');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7043, 'Пункт 7 статьи 395 Налогового кодекса');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7044, record_id from ref_book_value where attribute_id = 7041  and string_value='3021100';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 11, 704, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7041, '3021194');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7042, 'Организации - резиденты особой экономической зоны - в отношении земельных участков, расположенных на территории особой экономической зоны, сроком на пять лет с момента возникновения права собственности на каждый земельный участок');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7043, 'Пункт 9 статьи 395 Налогового кодекса');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7044, record_id from ref_book_value where attribute_id = 7041  and string_value='3021100';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 12, 704, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7041, '3021195');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7042, 'Организации, признаваемые управляющими компаниями в соответствии с Федеральным законом от 28 сентября 2010 г. N 244-ФЗ "Об инновационном центре "Сколково" (Собрание законодательства Российской Федерации, 2010, N 40, ст. 4970; N 52, ст. 7000; 2011, N 29, ст. 4291, ст. 4300), - в отношении земельных участков, предоставленных для непосредственного выполнения возложенных на эти организации функций в соответствии с указанным Федеральным законом');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7043, 'Пункт 10 статьи 395 Налогового кодекса');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7044, record_id from ref_book_value where attribute_id = 7041  and string_value='3021100';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 13, 704, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7041, '3021200');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7042, 'НАЛОГОВЫЕ ЛЬГОТЫ, УМЕНЬШАЮЩИЕ ВЕЛИЧИНУ НАЛОГОВОЙ БАЗЫ, ПРЕДОСТАВЛЕННЫЕ В ВИДЕ НЕ ОБЛАГАЕМОЙ НАЛОГОМ СУММЫ');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7043, 'Пункт 5 статьи 391 Налогового кодекса');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7044, record_id from ref_book_value where attribute_id = 7041  and string_value='3021000';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 14, 704, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7041, '3021210');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7042, 'Герои Советского Союза, Герои Российской Федерации, полные кавалеры ордена Славы');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7043, 'Подпункт 1 пункта 5 статьи 391 Налогового кодекса');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7044, record_id from ref_book_value where attribute_id = 7041  and string_value='3021200';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 15, 704, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7041, '3021220');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7042, 'Инвалиды, имеющие I группу инвалидности, а также лица, имеющие II группу инвалидности, установленную до 1 января 2004 года');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7043, 'Подпункт 2 пункта 5 статьи 391 Налогового кодекса');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7044, record_id from ref_book_value where attribute_id = 7041  and string_value='3021200';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 16, 704, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7041, '3021230');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7042, 'Инвалиды с детства');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7043, 'Подпункт 3 пункта 5 статьи 391 Налогового кодекса');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7044, record_id from ref_book_value where attribute_id = 7041  and string_value='3021200';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 17, 704, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7041, '3021240');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7042, 'Ветераны и инвалиды Великой Отечественной войны, а также ветераны и инвалиды боевых действий');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7043, 'Подпункт 4 пункта 5 статьи 391 Налогового кодекса');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7044, record_id from ref_book_value where attribute_id = 7041  and string_value='3021200';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 18, 704, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7041, '3021250');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7042, 'Физические лица, имеющие право на получение социальной поддержки в соответствии с Законом Российской Федерации от 15 мая 1991 г. N 1244-1 "О социальной защите граждан, подвергшихся воздействию катастрофы на Чернобыльской АЭС" (в редакции Закона Российской Федерации от 18 июня 1992 г. N 3061-1) (Ведомости Съезда народных депутатов и Верховного Совета РСФСР, 1991, N 21, ст. 699; Ведомости Съезда народных депутатов и Верховного Совета Российской Федерации, 1992, N 32, ст. 1861), в соответствии с Федеральным законом от 26 ноября 1998 г. N 175-ФЗ "О социальной защите граждан Российской Федерации, подвергшихся воздействию радиации вследствие аварии в 1957 году на производственном объединении "Маяк" и сбросов радиоактивных отходов в реку Теча" (Собрание законодательства Российской Федерации, 1998, N 48, ст. 5850; 2000, N 33, ст. 3348; 2004, N 35, ст. 3607; 2008, N 30, ст. 3616; 2011, N 1, ст. 26) и в соответствии с Федеральным законом от 10 января 2002 г. N 2-ФЗ "О социальных гарантиях гражданам, подвергшимся радиационному воздействию вследствие ядерных испытаний на Семипалатинском полигоне" (Собрание законодательства Российской Федерации, 2002, N 2, ст. 128; 2004, N 12, ст. 1035; N 35, ст. 3607; 2008, N 9, ст. 817, N 29, ст. 3410, N 30, ст. 3616, N 52, ст. 6224, ст. 6236; 2009, N 18, ст. 2152, N 30, ст. 3739, N 52, ст. 6452)');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7043, 'Подпункт 5 пункта 5 статьи 391 Налогового кодекса');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7044, record_id from ref_book_value where attribute_id = 7041  and string_value='3021200';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 19, 704, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7041, '3021260');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7042, 'Физические лица, принимавшие в составе подразделений особого риска непосредственное участие в испытаниях ядерного и термоядерного оружия, ликвидации аварий ядерных установок на средствах вооружения и военных объектах');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7043, 'Подпункт 6 пункта 5 статьи 391 Налогового кодекса');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7044, record_id from ref_book_value where attribute_id = 7041  and string_value='3021200';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 20, 704, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7041, '3021270');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7042, 'Физические лица, получившие или перенесшие лучевую болезнь или ставшие инвалидами в результате испытаний, учений и иных работ, связанных с любыми видами ядерных установок, включая ядерное оружие и космическую технику');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7043, 'Подпункт 7 пункта 5 статьи 391 Налогового кодекса');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7044, record_id from ref_book_value where attribute_id = 7041  and string_value='3021200';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 21, 704, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7041, '3022000');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7042, 'ЛЬГОТЫ ПО ЗЕМЕЛЬНОМУ НАЛОГУ, УСТАНАВЛИВАЕМЫЕ НОРМАТИВНЫМИ ПРАВОВЫМИ АКТАМИ ПРЕДСТАВИТЕЛЬНЫХ ОРГАНОВ МУНИЦИПАЛЬНЫХ ОБРАЗОВАНИЙ (ЗАКОНАМИ ГОРОДОВ ФЕДЕРАЛЬНОГО ЗНАЧЕНИЯ МОСКВЫ И САНКТ- ПЕТЕРБУРГА)');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7043, 'Пункт 2 статьи 387 Налогового кодекса');
		
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 22, 704, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7041, '3022100');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7042, 'Льготы по земельному налогу, предоставляемые в виде не облагаемой налогом суммы');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7043, 'Пункт 2 статьи 387 Налогового кодекса');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7044, record_id from ref_book_value where attribute_id = 7041  and string_value='3022000';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 23, 704, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7041, '3022200');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7042, 'Льготы по земельному налогу, уменьшающие исчисленную сумму налога');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7043, 'Пункт 2 статьи 387 Налогового кодекса');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7044, record_id from ref_book_value where attribute_id = 7041  and string_value='3022000';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 24, 704, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7041, '3022300');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7042, 'Льготы по земельному налогу в виде доли необлагаемой площади земельного участка');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7043, 'Пункт 2 статьи 387 Налогового кодекса');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7044, record_id from ref_book_value where attribute_id = 7041  and string_value='3022000';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 25, 704, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7041, '3022400');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7042, 'Льготы по земельному налогу в виде освобождения от налогообложения');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7043, 'Пункт 2 статьи 387 Налогового кодекса');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7044, record_id from ref_book_value where attribute_id = 7041  and string_value='3022000';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 26, 704, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7041, '3022500');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7042, 'Льготы по земельному налогу в виде снижения налоговой ставки');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7043, 'Пункт 2 статьи 387 Налогового кодекса');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 7044, record_id from ref_book_value where attribute_id = 7041  and string_value='3022000';

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 27, 704, to_date('28.10.2011', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7041, '3029000');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7042, 'ЛЬГОТЫ (ОСВОБОЖДЕНИЕ) ПО ЗЕМЕЛЬНОМУ НАЛОГУ, ПРЕДУСМОТРЕННЫЕ МЕЖДУНАРОДНЫМИ ДОГОВОРАМИ РОССИЙСКОЙ ФЕДЕРАЦИИ');
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 7043, 'Статья 7 Налогового кодекса');

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
--https://jira.aplana.com/browse/SBRFACCTAX-16552: 1.1. Создать справочники для формы настроек подразделений ЗемНалога
declare l_task_name varchar2(128) := 'RefBook Block #16 (SBRFACCTAX-16552 - Department settings (L)))';
begin
		
	INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (700, 'Параметры подразделения по земельному налогу',0,0,0,null);
	INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (710, 'Параметры подразделения по земельному налогу (таблица)',0,0,0,null);
	
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7001,700,'Идентификатор (первичный ключ)','DEPARTMENT_ID',4,0,30,161,1,null,10,0,0,null,null,0,null);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7002,700,'ИНН','INN',1,1,null,null,1,null,10,0,0,null,null,0,10);
	
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7100, 710, 'Ссылка на родительскую запись', 'LINK', 4, 0, 700, 7001, 0, null, 10, 1, 0, null, null, 0, null);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7101, 710, 'Порядок следования', 'ROW_ORD', 2, 1, null, null, 0, 0, 10, 1, 0, null, null, 0, 4);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7102, 710, 'Код налогового органа (кон.)', 'TAX_ORGAN_CODE', 1, 2, null, null, 1, null, 5, 0, 1, null, null, 0, 4);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7119, 710, 'Код налогового органа (пром.)','TAX_ORGAN_CODE_PROM', 1, 3, null, null, 1, null, 5, 0, 0, null, null,0, 4);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7103, 710, 'КПП', 'KPP', 1, 4, null, null, 1, null, 5, 0, 1, null, null, 0, 9);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7104, 710, 'Код по месту нахождения (учета)', 'TAX_PLACE_TYPE_CODE', 4, 5, 2, 3, 1, null, 6, 0, 0, null, null, 0, null);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7105, 710, 'Наименование (налогоплательщик)', 'NAME', 1, 6, null, null, 1, null, 20, 0, 0, null, null, 0, 1000);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7106, 710, 'ОКВЭД', 'OKVED_CODE', 4, 7, 34, 210, 1, null, 5, 0, 0, null, null, 0, null);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7107, 710, 'Номер контактного телефона', 'PHONE', 1, 8, null, null, 1, null, 10, 0, 0, null, null, 0, 20);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7108, 710, 'Обязанность по уплате авансовых платежей', 'PREPAYMENT', 4, 9, 38, 250, 1, null, 6, 0, 0, null, null, 0, null);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7109, 710, 'Код формы реорганизации и ликвидации', 'REORG_FORM_CODE', 4, 10, 5, 13, 1, null, 10, 0, 0, null, null, 0, null);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7110, 710, 'ИНН реорг. организации', 'REORG_INN', 1, 11, null, null, 1, null, 10, 0, 0, null, null, 0, 10);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7111, 710, 'КПП реорг. организации', 'REORG_KPP', 1, 12, null, null, 1, null, 10, 0, 0, null, null, 0, 9);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7112, 710, 'Признак лица подписавшего документ', 'SIGNATORY_ID', 4, 13, 35, 212, 1, null, 10, 0, 0, null, null, 0, null);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7113, 710, 'Фамилия', 'SIGNATORY_SURNAME', 1, 14, null, null, 1, null, 10, 0, 0, null, null, 0, 60);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7114, 710, 'Имя', 'SIGNATORY_FIRSTNAME', 1, 15, null, null, 1, null, 10, 0, 0, null, null, 0, 60);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7115, 710, 'Отчество', 'SIGNATORY_LASTNAME', 1, 16, null, null, 1, null, 10, 0, 0, null, null, 0, 60);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7116, 710, 'Наименование документа представителя', 'APPROVE_DOC_NAME', 1, 17, null, null, 1, null, 30, 0, 0, null, null, 0, 120);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7117, 710, 'Наименование организации представителя', 'APPROVE_ORG_NAME', 1, 18, null, null, 1, null, 30, 0, 0, null, null, 0, 1000);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (7118, 710, 'Код подразделения', 'DEPARTMENT_ID', 4, 19, 30, 161, 1, null, 10, 1, 0, null, null, 0, null);
	
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