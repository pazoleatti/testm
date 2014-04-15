/*
Справочник:
	Виды ценных бумаг
Список изменений:
	Первоначальное заполнение данными
Данные:	
	1;Купонная облигация
	2;Дисконтная облигация
	3;Акция
*/
INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES
  (seq_ref_book_record.nextval, 1, 89, to_date('01.01.2012','dd.mm.yyyy'), 0);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
  (seq_ref_book_record.currval, 827, null, 1, null, null);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
(seq_ref_book_record.currval, 828, 'Купонная облигация', null, null, null);

INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES
(seq_ref_book_record.nextval, 2, 89, to_date('01.01.2012','dd.mm.yyyy'), 0);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
(seq_ref_book_record.currval, 827, null, 2, null, null);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
(seq_ref_book_record.currval, 828, 'Дисконтная облигация', null, null, null);

INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES
(seq_ref_book_record.nextval, 3, 89, to_date('01.01.2012','dd.mm.yyyy'), 0);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
(seq_ref_book_record.currval, 827, null, 3, null, null);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
(seq_ref_book_record.currval, 828, 'Акция', null, null, null);

commit;
exit;