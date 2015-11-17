--http://jira.aplana.com/browse/SBRFACCTAX-13365: Средняя стоимость транспортных средств (2015)
set serveroutput on size 30000;
declare
	seq_initial_position number(18);
    no_reference_found_exception exception;
begin
	select seq_ref_book_record.nextval into seq_initial_position from dual;
	
insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Q7 quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2967');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'TT RS plus Coupe quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2480');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'TT RS (2.5 TFSI)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2480');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 4, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'S5 Cabrio quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 5, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'TT RS plus Coupe quattro Roadster');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2480');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 6, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Q7 (3.0 TFSI)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 7, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Q7 quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 8, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'S6 Limousine quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 9, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'S6');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, 'любой');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин/дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 10, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'S6 Avant quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 11, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'RS4 Avant quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4163');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 12, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'RS4');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, 'любой');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин/дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 13, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'RS5');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4163');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 14, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Q7 (4.2 TDI)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4134');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 15, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'RS5 Coupe quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4163');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 16, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Q7 quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4134');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 17, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'RS4 Avant quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3967');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 18, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'S7 Sportback quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 19, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'S7');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 20, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'A8 Limousine hybrid');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '1984');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 21, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'A8 (2.0 Hybrid)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '1984');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 22, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'A8 Limousine quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 23, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'A8 (3.0 TDI)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2967');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 24, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'A8 (3.0 TFSI)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 25, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'A8 Long Limousine hybrid');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '1984');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 26, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'A8 Long Limousine quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '1984');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 27, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'A8 Long Limousine quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2967');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 28, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'A8 Long Limousine quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2968');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 29, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'A8 Long Limousine quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 30, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'A8 Limousine quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2967');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 31, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'A8 Limousine quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '1984');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 32, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'A8 Limousine quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2968');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 33, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'RS5 Cabriolet quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4163');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 34, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'A8 (4.0 TFSI)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 35, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'A8 (4.2 TDI)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4134');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 36, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'X6 xDrive35i');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2979');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 37, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '550i');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4395');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 38, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Z4 sDrive35is');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2979');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 39, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'X5 xDrive30d');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 40, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '535d xDrive');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 41, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '550i хDrive');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4395');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 42, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '535d xDrive Гран Туризмо');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 43, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'М3 Седан');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2979');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 44, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'М3');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2979/3999');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 45, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '550i xDrive Седан');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4395');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 46, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'М4 Купе');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2979');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 47, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'X5 xDrive40d');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 48, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'X6 xDrive30d');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 49, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'X5 M');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4395');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 50, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '730i');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2996');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 51, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '730Li');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2996');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 52, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'X6 xDrive40d');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 53, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'M550d xDrive');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 54, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '640d xDrive');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 55, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '640i');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2979/2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 56, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '640i xDrive');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 57, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '550i xDrive Гран Туризмо');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4395');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 58, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'M550d xDrive Седан');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 59, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '640i xDrive Купе');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2979');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 60, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '640d xDrive Купе');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 61, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'X5 xDrive50i');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4395');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 62, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'М4 Кабриолет');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2979');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 63, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '640i Кабриолет');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2979');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 64, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '730d xDrive');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 65, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '640i xDrive Гран Купе');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2979');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 66, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '640d xDrive Гран Купе');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 67, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'X6 xDrive50i');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4395');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 68, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '640i xDrive Кабриолет');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2979');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 69, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '650i');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4395');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 70, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '740d xDrive');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 71, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'X5 xDriveM50d');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 72, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'X5 M50d');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 73, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '740Li xDrive');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2979');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 74, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '650i xDrive');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4395');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 75, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '650i xDrive Купе');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4395');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 76, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'X6 xDrive M50D');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 77, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'М5');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4395');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 78, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'M5 Седан');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4395');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 79, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '750d xDrive');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 80, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '750i xDrive');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4395');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 81, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '650i Кабриолет');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4395');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 82, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '750Ld xDrive');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 83, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '650i xDrive Кабриолет');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4395');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 84, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '650i xDrive Гран Купе');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4395');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 85, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '750Li xDrive');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4395');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 86, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Cadillac');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Escalade 6.2');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '6162');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 87, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Cadillac');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Escalade');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '6162/6200');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 88, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Cadillac');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'CTS-V');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '6162');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 89, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Cadillac');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Escalade Hybrid');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5999');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 90, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Chevrolet');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Tahoe');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '6164');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 91, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Chevrolet');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Camaro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3564');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 92, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Chevrolet');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Camaro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '6164');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 93, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Chevrolet');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Corvette');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '6164');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 94, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Hyundai');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Equus 5.0 V8 GDI');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5038');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 95, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Hyundai');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Equus Royal');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5038');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 96, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Hyundai');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Equus Limousine');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5038');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 97, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Infiniti');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Q70');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5552');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 98, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Infiniti');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'FX50 (5.0 S)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5026');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 99, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Infiniti');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'QX70');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5026');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 100, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Infiniti');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'QX80');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5552');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 101, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'XF 3.0L S/C 340HP AWD JT3 Premium Luxury');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 102, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'XF 3.0L S/C 340HP AWD JT2 R-Sport');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 103, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'XF 3.0L S/C 340HP AWD JT5 Portfolio');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 104, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'XJ (2.0 i4)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '1999');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 105, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'F-Type (3.0 V6)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 106, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'XJ 2.0 i4;
 240PS Petrol SWB Luxury');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '1999');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 107, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'F-TYPE 3.0 V6 S/C Coupe 340PS (ST1)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 108, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'XJ 2.0 i4;
 240PS Petrol SWB Premium Luxury');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '1999');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 109, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'XJ 2.0 i4;
 240PS Petrol LWB Luxury');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '1999');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 110, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'XJ 2.0 i4;
 240PS Petrol LWB Premium Luxury');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '1999');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 111, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'XJ (3.0 V6)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 112, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'XJ 3.0 V6;
 275PS Diesel SWB Premium Luxury');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 113, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'F-TYPE 3.0 V6 S/C Convertible 340PS (ST1)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 114, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'XJ 3.0 V6;
 340PS Petrol S/C AWD SWB Luxury');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 115, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'F-TYPE S 3.0 V6 S/C Coupe 380PS (ST2)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 116, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'XJ 3.0 V6;
 275PS Diesel LWB Premium Luxury');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 117, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'XJ 3.0 V6;
 340PS Petrol S/C AWD LWB Luxury');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 118, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'XFR (5.0)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5000');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 119, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'XJ 3.0 V6;
 340PS Petrol S/C AWD SWB Premium Luxury');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 120, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'XJ 3.0 V6;
 275PS Diesel SWB Portfolio');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 121, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'XFR 5.0L S/C 510 HP JT4 XFR');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5000');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 122, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'F-TYPE S 3.0 V6 S/C Convertible 380PS (ST2)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 123, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'XJ 3.0 V6;
 340PS Petrol S/C AWD LWB Premium Luxury');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 124, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'XJ 3.0 V6;
 275PS Diesel LWB Portfolio');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 125, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jeep');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Grand Cherokee SRT8');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '6417');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 126, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Discovery 4 V8 5.0 HSE');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4999');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 127, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Discovery 4 V6 3.0 S/C 250kW (340hp) HSE');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2999');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 128, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover Evoque 2.2 SD4 5 Door - Autobiography');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2179');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 129, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Discovery 4 TDV6 183kW (211hp) 3.0 HSE');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 130, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Discovery 4 SDV6 183kW (249hp) 3.0 HSE');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 131, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover Evoque 2.2 Si4 5 Door - Autobiography');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '1999');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 132, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover Sport V6 S/C 3.0 S 14.5MY');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 133, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Discovery 4 SDV6 183kW (249hp) 3.0 XXV');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 134, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover Sport 3.0 V6');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 135, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover Sport TDV6 3.0');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 136, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Discovery 4 SDV6 183kW (249hp) 3.0 HSE Luxury');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 137, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Discovery 4 V6 3.0 S/C 250kW (340hp) HSE Luxury');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2999');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 138, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover Sport TDV6 3.0 SE 14.5MY');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 139, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover Sport V6 S/C 3.0 SE 14.5MY');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 140, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover Sport SDV6 3.0');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 141, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover Sport SDV6 3.0 SE 14.5MY');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 142, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover Sport V6 S/C 3.0 HSE DYN 14.5MY');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 143, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover Sport TDV6 3.0 HSE 14.5MY');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 144, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover Sport SDV6 3.0 HSE 14.5MY');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 145, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover 3.0 V6');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 146, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover 3.0L TDV6 HSE-TL3');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 147, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover 3.0 Litre V6 S/C Petrol HSE-TL3');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 148, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover Sport SDV8 4.4');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4367');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 149, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover Sport 5.0 V8');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4999');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 150, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover 3.0 TDV6');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 151, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover Sport SDV6 3.0 AB 14.5MY');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 152, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover Sport SDV8 4.4 HSE DYN 14.5MY');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4367');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 153, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover Sport V8 S/C 5.0 HSE DYN 14.5MY');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4999,7(5000)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 154, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover 3.0L TDV6 Vogue- TL4');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 155, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover 3.0 Litre V6 S/C Petrol Vogue-TL4');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 156, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover LWB 3.0L TDV6 Vogue -TL4');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 157, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Lexus');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'LX 570 (5.7 570 Premium auto)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5663');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 158, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Lexus');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'LX 570');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5663');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 159, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Lexus');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'LS 460');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4608');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 160, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Lexus');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'LS 460 (4.6 460 executive auto)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4608');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 161, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Maserati');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Ghibli Diesel');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2987');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 162, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Maserati');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Ghibli');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2979/2987');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 163, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Maserati');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Ghibli S');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2979');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 164, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Maserati');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Ghibli S Q4');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2979');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 165, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'ML 350');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2987/3498');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин/дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 166, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'CLS 350 BlueTec 4MATIC');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2987');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 167, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes-Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'CLS 350 BlueTec 4MATIC (Shooting Brake)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2987');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 168, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'CLS 400 4MATIC');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2996');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 169, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'CLS 400 4MATIC (Shooting Brake)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2996');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 170, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'E 300 кабриолет');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3498');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 171, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'SLK 55 AMG');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5461');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 172, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'С 63 AMG купе');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '6208');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 173, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'GL 350');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2987');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин/дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 174, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'E 500');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4663/5461');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 175, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'E 500 4MATIC седан');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4663');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 176, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'GL 400 4MATIC');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2996');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 177, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'GL 350 BlueTec 4MATIC');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2987');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 178, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'CLS 500');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4663/5461');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 179, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'CLS 500 4MATIC');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4663');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 180, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'CLS 500 4MATIC (Shooting Brake)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4663');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 181, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'ML 500');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4663/4966/5461');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 182, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'ML 500 4MATIC');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4663');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 183, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'S 400');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3498/3996');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин/дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 184, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'G 350');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2987');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин/дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 185, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'S 400 длинная база');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2996');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 186, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'S 400 HYBRID длинная база');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3498');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 187, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'G 350 BlueTec 5-ти дверный');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2987');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 188, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'SL 350');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3498');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 189, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2987');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 190, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'E 63 AMG');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5461');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 191, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'E 63 AMG 4MATIC седан');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5461');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 192, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'S 400 4MATIC длинная база');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2996');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 193, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'SL 400');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2996');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 194, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5461');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 195, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'CLS 63 AMG');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5461');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 196, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'ML 63 AMG');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5461');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 197, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'GL 500');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4663');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 198, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'GL 500 4MATIC');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4663');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 199, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Nissan');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Patrol 5.6 V8');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5552');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 200, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Nissan');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'GT-R (R35G)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3799');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 201, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Nissan');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Patrol (Base)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5552');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 202, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Nissan');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'GT-R');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3799/3800');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 203, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Cayman');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3436');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 204, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Cayman S');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2706/2893');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин/дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 205, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Cayenne');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3598');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 206, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Cayenne');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2967/3598');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин/дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 207, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Cayenne');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2967');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 208, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Macan');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3604');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 209, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Panamera');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2967');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 210, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Cayenne');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4806');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 211, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Cayenne S');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4134/4806');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин/дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 212, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Panamera');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3605');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин/дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 213, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Cayenne');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4134');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 214, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '911');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3436');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 215, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Cayenne');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 216, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Cayenne S Hybrid');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'гибрид');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 217, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Panamera 4');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3605/4806');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 218, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Carrera');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5733');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 219, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Cayenne GTS');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 220, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Toyota');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'LANDCRUISER 200');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, 'любой');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин/дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 221, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Volkswagen');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Multivan Highline');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '1968');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 222, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Volkswagen');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Multivan Highline');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '1984');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 223, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Volkswagen');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'California Comforline');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '1968');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 224, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Volkswagen');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Touareg  V8 FSI');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4163/4200');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин/дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 225, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Volkswagen');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'California Comforline');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '1984');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 226, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Volkswagen');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Touareg  V8 TDI');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4134/4200');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин/дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 227, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Volkswagen');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Touareg');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4163');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 228, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Volkswagen');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Touareg');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4134');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 229, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Volkswagen');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Touareg Hybrid TSI');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'гибрид');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 230, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Volkswagen');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Touareg');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 231, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Volkswagen');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Phaeton');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2967');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 232, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Volkswagen');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Phaeton V8');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, 'любой');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин/дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 233, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Volkswagen');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Phaeton V6 TDI');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2967');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 3);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 234, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Volkswagen');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Multivan Business');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, 'любой');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'любой');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 235, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Volkswagen');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Phaeton');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4172');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 236, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Volkswagen');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Phaeton');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3597');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 237, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Volkswagen');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Phaeton');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2967');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин/дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 238, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Volkswagen');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Phaeton дл. База');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4172');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин/дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 239, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '0';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Volkswagen');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Phaeton дл. База');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2967');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин/дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 240, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Aston Martin');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'V8 Vantage Coupe');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4735');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 241, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Aston Martin');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'V8 Vantage Roadster');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4735');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 242, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Aston Martin');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'V8 Vantage S Coupe');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4735');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 243, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'A8 Long Limousine quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4134');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин/дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 244, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'RS6 Avant quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 245, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'RS6');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 246, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'A8 Limousine quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 247, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'A8 Limousine quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4134');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 248, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'A8 Long Limousine quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 249, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'RS7 Sportback quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 250, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'RS7');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 251, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Q7 quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5934');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 252, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Q7 (6.0 TDI)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5934');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин/дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 253, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'S8');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 254, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'R8 (4.2 FSI V8)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4163');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 255, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'S8 Limousine quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 256, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'R8 Coupe quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4163');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 257, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'R8 Spyder (4.2 FSI V8)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4163');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 258, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'R8 (5.2 V10)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5204');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 259, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'R8 Spyder quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4163');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 260, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'A8 (6.3 FSI )');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '6299/6300');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 261, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'R8 Coupe quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5204');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 262, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'A8 Long Limousine quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '6299');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 263, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'R8 Spyder (5.2 V10)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5204');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 264, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Audi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'R8 Spyder quattro');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5204');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 265, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Bentley');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'GT V8');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 266, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Bentley');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'New Continental GT V8');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 267, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Bentley');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Flying Spur V8');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 268, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '750Ld xDrive');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 269, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'X6 M');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993/4395');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'любой');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 270, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '760 Li');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5972');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 271, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'М6');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4395');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 272, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'M6 Купе');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4395');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 273, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'M6 Кабриолет');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4395');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 274, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'BMW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'M6 Гран Купе');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4395');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 275, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'XJ 3.0 V6;
 340PS Petrol S/C AWD SWB Portfolio');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 276, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'XJ 3.0 V6;
 340PS Petrol S/C AWD LWB Portfolio');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 277, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'XFR-S 5.0L S/C 550HP JT7 XFR-S');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5000');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 278, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'F-Type (5.0 V8)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5000');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 279, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'F-TYPE R 5.0 V8 S/C Coupe 550PS (ST4)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5000');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 280, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'F-TYPE S 5.0 V8 S/C Convertible 495PS (ST3)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5000');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 281, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'XJ (5.0 V8)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5000');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 282, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'XJ 5.0 V8;
 510PS S/C SWB Supersport');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5000');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 283, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Jaguar');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'XJ 5.0 V8;
 510PS S/C LWB Supersport');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5000');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 284, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover 4.4 SDV8');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4367');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин/дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 285, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover Sport SDV8 4.4 AB 14.5MY');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4367');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 286, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover LWB 3.0 Litre V6 S/C Petrol Vogue-TL4');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 287, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover 4.4L SDV8 Vogue - TL4');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4367');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 288, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover 3.0L TDV6 Vogue SE- TL5');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 289, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover 3.0 Litre V6 S/C Petrol Vogue SE-TL5');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 290, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover Sport V8 S/C 5.0 AB DYN 14.5MY');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4999,7 (5000)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 291, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover 5.0 V8');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4999');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'любой');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 292, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover 4.4L SDV8 Vogue SE- TL5');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4367');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 293, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover 5.0L V8 S/C Vogue SE -TL5');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4999');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 294, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover LWB 4.4L SDV8 Vogue SE-TL5');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4367');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 295, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover 4.4L SDV8 Autobiography-TL6');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4367');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 296, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover 5.0L V8 S/C Autobiography-TL6');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4999');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 297, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover LWB 4.4L SDV8 Autobiography-TL6');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4367');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 298, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover 5.0L LWB V8 S/C Autobiography-TL6');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4999');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 299, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover 5.0L V8 S/C Autobiography Black-TL7');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4999');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 300, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover LWB 4.4L SDV8 Autobiography Black-TL7');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4367');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 301, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Land Rover');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Range Rover 5.0L LWB V8 S/C Autobiography Black-TL7');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4999');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 302, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Lexus');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'LS 460L');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4608');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 303, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Lexus');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'LS 600h');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4969');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 304, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Lexus');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'LS 600H (5.0 600H F-sport luxury auto)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4969');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 305, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Lexus');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'LS 600h L');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4969');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 306, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Maserati');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Quattroporte');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, 'любой');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 307, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Maserati');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'GranTurismo');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4244/4691');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 308, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Maserati');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'GranTurismo Sport');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4691');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 309, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Maserati');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Ghibli');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2979');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 310, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Maserati');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Ghibli S Q4');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2979');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 311, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Maserati');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'GranCabrio');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4691');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 312, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Maserati');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Quattroporte Diesel');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2987');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'дизель');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 313, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Maserati');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Quattroporte S Q4');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2979');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 314, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Maserati');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Quattroporte GTS');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3799');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 315, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Maserati');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'GranCabrio MC');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4691');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 316, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Maserati');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'GranCabrio Sport');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4691');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 317, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Maserati');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'GranTurismo MC Stradale');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4691');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 318, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'CLS 63 AMG 4MATIC');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5461');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 319, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'S 500');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4663');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 320, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'E 63 AMG S 4MATIC седан');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5461');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 321, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'S 500 длинная база');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4663');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 322, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'G 500');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4966/5461');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 323, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'CLS 63 AMG S 4MATIC');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5461');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 324, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'CLS 63 AMG S 4MATIC (Shooting Brake)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5461');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 325, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'S 500 4MATIC длинная база');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4663');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 326, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'G 500  5-ти дверный');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5461');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 327, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'CL 500');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4663/5461');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 328, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'SL 500');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4663');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 329, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'S 500 4MATIC купе');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4663');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 330, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'GL 63 AMG');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5461');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 331, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'S 63 4MATIC длинная база');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5461');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 332, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'G 63 AMG');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5461');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 333, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'CL 63 AMG');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5461/6208');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 334, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'G 63 AMG 5-ти дверный');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5461');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 335, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'SL 63 AMG');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5461');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 336, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'S 63 AMG');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5461');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 337, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'S 600 длинная база');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5980');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 338, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'S 63 AMG 4MATIC купе');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5461');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 339, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Carrera 4');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3614');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 340, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Panamera');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2997');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 341, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Panamera S');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4806');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 342, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '911');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3800');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 343, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Panamera 4S');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4806');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 344, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Carrera S');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3800');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 345, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Panamera');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 346, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Panamera S e-Hybrid');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '2995');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин/гибрид');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 347, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Carrera 4S');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3800');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 348, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Panamera');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4806');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 349, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Panamera GTS');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4806');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 350, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Cayenne Turbo');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4806');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 351, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '911');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3799');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 352, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '911 GT3');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3799');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 353, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Cayenne Turbo S');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4806');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 354, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '911 Turbo');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3800');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 355, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '1';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Panamera Turbo');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4806');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 5);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 356, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Aston Martin');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'DB9');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5935');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 10);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 357, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Aston Martin');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'V8 Vantage S Roadster');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4735');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 358, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Aston Martin');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Rapide');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5935');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 10);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 359, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Aston Martin');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'V12 Vantage S');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5935');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 10);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 360, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Aston Martin');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'V8 Vantage');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4280/4735');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 10);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 361, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Aston Martin');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'V8 Vantage S');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4735');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 10);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 362, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Aston Martin');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Virage');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5935');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 10);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 363, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Aston Martin');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'V12 Vantage S Coupe');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5935');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 364, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Aston Martin');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'DB9 Coupe');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5935');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 365, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Aston Martin');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'V12 Vantage S Roadster');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5935');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 366, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Aston Martin');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'DB9 Volante');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5935');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 367, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Aston Martin');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Vanquish');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5935');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 10);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 368, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Aston Martin');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Rapide S');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5935');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 15);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 369, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Aston Martin');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'DBS Coupe');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5935');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 370, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Bentley');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'New Continental GT V8S');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 371, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Bentley');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'GTC V8');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3993/5998');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 10);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 372, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Bentley');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'GT W12');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3993/5998');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 10);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 373, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Bentley');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'New Continental GTC V8');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 374, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Bentley');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Flying Spur W12');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5998/6000');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 10);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 375, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Bentley');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'New Continental GT W12');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '6000');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 376, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Bentley');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Flying Spur W12 (new)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '6000');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 377, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Bentley');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'New Continental GTC V8S');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 378, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Bentley');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'GTC W12');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5998');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 10);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 379, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Bentley');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'GT Speed');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5998');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 10);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 380, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Bentley');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'New Continental GTC W12');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '6000');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 381, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Bentley');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'New Continental GT Speed');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '6000');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 382, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Bentley');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'GTC Speed');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '6000');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 10);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 383, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Bentley');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'New Continental GTC Speed');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '6000');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 384, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Ferrari');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'California');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4300');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 10);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 385, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Ferrari');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '458 Italia');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4497');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 10);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 386, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Ferrari');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '458 Spider');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4497');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 387, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Ferrari');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '458 Speciale');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4497');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 388, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Lamborghini');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Huracan');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5204');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 389, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Lamborghini');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Gallardo LP 560-4');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5204');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 10);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 390, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Lamborghini');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Gallardo LP 550-2');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5204');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 10);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 391, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Lamborghini');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Gallardo LP 570-4');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5204');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 10);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 392, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Maserati');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Quattroporte GTS');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3799');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 393, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'SLS AMG GT купе');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '6208');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 394, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'SLS AMG');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '6208');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 10);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 395, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'SLS AMG GT родстер');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '6208');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 396, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'CL 65 AMG');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5980');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 10);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 397, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'SL 65 AMG');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5980');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 10);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 398, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'S 65 4MATIC длинная база');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5980');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 399, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'S 65 AMG купе');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5980');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 400, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'G 65 AMG');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5980');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 10);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 401, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Porsche');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '911 Turbo S');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3800');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 10);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 402, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '2';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Rolls-Royce');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Ghost');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '6592');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 10);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 403, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '3';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Aston Martin');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'DBS');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5935');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 20);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 404, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '3';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Aston Martin');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Vanquish Coupe');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5935');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 405, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '3';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Aston Martin');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Vanquish Volante');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5935');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 406, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '3';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Bentley');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Mulsanne');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '6752');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 20);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 407, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '3';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Bugatti');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Veyron');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '7993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 20);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 408, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '3';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Bugatti');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Veyron Grand Sport Vitesse');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '7993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 20);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 409, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '3';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Bugatti');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Veyron Super Sport');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '7993');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 20);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 410, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '3';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Ferrari');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'F12 Berlinetta');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '6262');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 20);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 411, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '3';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Ferrari');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'California T');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '3855');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 412, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '3';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Ferrari');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '458 Italia');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4497');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 413, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '3';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Ferrari');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'FF');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '6262');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 20);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 414, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '3';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Ferrari');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '458 Spider');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4497');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 20);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 415, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '3';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Ferrari');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'LaFerrari');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '6262');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 20);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 416, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '3';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Ferrari');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, '458 Speciale');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '4497');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 20);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 417, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '3';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Lamborghini');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Aventador Coupe');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '6498');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 418, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '3';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Lamborghini');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Aventador LP 700-4');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '6498');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 2);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 20);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 419, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '3';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Lamborghini');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Aventador Roadster');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '6498');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 420, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '3';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Mercedes- Benz');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'G 65 AMG 5-ти дверный');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '5980');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 421, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '3';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Rolls-Royce');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Wraith');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '6592');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 20);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 422, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '3';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Rolls-Royce');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Phantom');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '6749');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 20);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 423, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '3';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Rolls-Royce');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Phantom Coupe');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '6749');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 424, 218, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	 insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 2181, record_id from ref_book_value where attribute_id = 2110 and string_value = '3';
	IF SQL%NOTFOUND THEN RAISE no_reference_found_exception; END IF;
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2182, 'Rolls-Royce');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2183, 'Phantom Drophead Coupe');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2184, '6749');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 2185, 'бензин');
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2186, 0);
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 2187, 2);
	
EXCEPTION  
   WHEN no_reference_found_exception THEN
	  dbms_output.put_line('NO_REF_FOUND exception for sequence element '||(seq_ref_book_record.currval - seq_initial_position)/100 || ' - Rollback everything.');
	  ROLLBACK;
   WHEN OTHERS THEN  
	  dbms_output.put_line('Sabotage detected! ('|| SQLCODE ||' - '||SQLERRM || ' ) - Rollback everything.');
      ROLLBACK;
END; 	
/	

COMMIT;
EXIT;