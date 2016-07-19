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
--https://jira.aplana.com/browse/SBRFACCTAX-15923: 1.1 РнРнтб. Реализовать справочник "Кредитные рейтинги и классы кредитоспособности"
declare l_task_name varchar2(128) := 'DML Block #4 (SBRFACCTAX-15923 - Credit ratings and credit quality classes))';
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
declare l_task_name varchar2(128) := 'DML Block #5 (SBRFACCTAX-15925 - Organizational legal forms))';
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
declare l_task_name varchar2(128) := 'DML Block #6 (SBRFACCTAX-16012 - Collateral types))';
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
declare l_task_name varchar2(128) := 'DML Block #7 (SBRFACCTAX-16013 - Excludable credit types))';
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
declare l_task_name varchar2(128) := 'DML Block #8 (SBRFACCTAX-16273 - Construction phases))';
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
COMMIT;
EXIT;