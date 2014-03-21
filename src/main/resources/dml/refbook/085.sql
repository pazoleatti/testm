/*
Справочник:
	Виды срочных сделок
Список изменений:
	Первоначальное заполнение данными
Данные:	
	1;Своп
	2;DOP DM
	3;DF PM
*/
INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES
  (seq_ref_book_record.nextval, 1, 85, to_date('01.01.2012','dd.mm.yyyy'), 0);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
  (seq_ref_book_record.currval, 818, null, 1, null, null);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
  (seq_ref_book_record.currval, 819, 'Своп', null, null, null);

INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES
(seq_ref_book_record.nextval, 2, 85, to_date('01.01.2012','dd.mm.yyyy'), 0);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
(seq_ref_book_record.currval, 818, null, 2, null, null);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
(seq_ref_book_record.currval, 819, 'DOP DM', null, null, null);

INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES
(seq_ref_book_record.nextval, 3, 85, to_date('01.01.2012','dd.mm.yyyy'), 0);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
(seq_ref_book_record.currval, 818, null, 3, null, null);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
(seq_ref_book_record.currval, 819, 'DF PM', null, null, null);

commit;
exit;