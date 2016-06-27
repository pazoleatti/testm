set serveroutput on size 1000000;
---------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-15922: 1.1 РнРнтб. Реализовать справочник "Классы кредитоспособности"
declare l_task_name varchar2(128) := 'DML Block #1 (SBRFACCTAX-15922 - Credit quality rates))';
begin
	
	INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (601,'Классы кредитоспособности',1,0,0,null);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (6011, 601, 'Класс кредитоспособности', 'CREDIT_QUALITY_CLASS',1,1,null,null,1,null,10,1,1,null,null,0,20);
	
	INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 601, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6011, '1 КЛАСС');		

	INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 601, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6011, '2 КЛАСС');	

	INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 601, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		INSERT INTO ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6011, '3 КЛАСС');			
	
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
declare l_task_name varchar2(128) := 'DML Block #2 (SBRFACCTAX-15924 - S'||chr(38)||'P credit ratings))';
begin
	
	INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (602,'Международные кредитные рейтинги по шкале S'||chr(38)||'P',1,0,0,null);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (6021, 602, 'Международный кредитный рейтинг', 'INTERNATIONAL_CREDIT_RATING',1,1,null,null,1,null,10,1,1,null,null,0,20);
	
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'AAA / A+');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'A');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'A-');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 4, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'BBB+');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 5, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'BBB');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 6, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'BBB-');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 7, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'BB+');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 8, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'BB');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 9, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'BB-');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 10, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'B+');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 11, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'B');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 12, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'B-');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 13, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'CCC+');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 14, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'CCC');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 15, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'CCC-');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 16, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'CC');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 17, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6021, 'C');

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 18, 602, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
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
declare l_task_name varchar2(128) := 'DML Block #3 (SBRFACCTAX-15921 - Credit ratings))';
begin
	
	INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (603,'Кредитные рейтинги',1,0,0,null);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (6031, 603, 'Кредитный рейтинг', 'CREDIT_RATING',1,1,null,null,1,null,10,1,1,null,null,0,20);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (6032, 603, 'Класс кредитоспособности', 'CREDIT_QUALITY_CLASS',4,2,601,6011,1,null,10,1,0,null,null,0,null);
	INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (6033, 603, 'Международный кредитный рейтинг по шкале S'||chr(38)||'P', 'INTERNATIONAL_CREDIT_RATING',4,3,602,6021,1,null,10,0,0,null,null,0,null);
	
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 1');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '1 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'AAA / A+' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 2');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '1 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'A' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 3');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '1 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'A-' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 4, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 4');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '1 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'A-' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 5, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 5');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '1 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'BBB+' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 6, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 6');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '1 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'BBB+' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 7, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 7');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '1 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'BBB' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 8, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 8');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '1 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'BBB-' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 9, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 9');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '1 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'BBB-' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 10, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 10');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '1 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'BB+' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 11, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 11');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '1 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'BB+' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 12, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 12');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '2 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'BB' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 13, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 13');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '2 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'BB-' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 14, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 14');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '2 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'BB-' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 15, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 15');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '2 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'B+' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 16, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 16');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '2 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'B' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 17, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 17');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '2 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'B' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 18, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 18');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '2 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'B-' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 19, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 19');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '2 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'CCC+' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 20, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 20');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '2 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'CCC+' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 21, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 21');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '2 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'CCC' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 22, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 22');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '2 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'CCC-' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 23, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 23');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '3 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'CC' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 24, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 24');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '3 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'C' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 25, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 25');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '3 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'C' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 26, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'Рейтинг 26');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '3 КЛАСС' ;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6033, record_id from ref_book_value where attribute_id = 6021 and string_value = 'D' ;

	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 27, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'RTD');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '3 КЛАСС' ;
		
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 28, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'D');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '3 КЛАСС' ;
		
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 29, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'RD');
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 6032, record_id from ref_book_value where attribute_id = 6011 and string_value = '3 КЛАСС' ;
		
	insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 30, 603, to_date('01.01.2016', 'DD.MM.YYYY'), 0);
		insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 6031, 'НУ');
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
---------------------------------------------------------------------------
COMMIT;
EXIT;