/*
Справочник:
	Признаки контрагентов
Список изменений:
	Первоначальное заполнение данными
Данные:	
	3;эмитент ценной бумаги
	4;организатор торговли
	5;прочие
*/
INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES
  (seq_ref_book_record.nextval, 1, 88, to_date('01.01.2012','dd.mm.yyyy'), 0);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
  (seq_ref_book_record.currval, 825, null, 3, null, null);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
(seq_ref_book_record.currval, 826, 'эмитент ценной бумаги', null, null, null);

INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES
(seq_ref_book_record.nextval, 2, 88, to_date('01.01.2012','dd.mm.yyyy'), 0);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
(seq_ref_book_record.currval, 825, null, 4, null, null);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
(seq_ref_book_record.currval, 826, 'организатор торговли', null, null, null);

INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES
(seq_ref_book_record.nextval, 3, 88, to_date('01.01.2012','dd.mm.yyyy'), 0);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
(seq_ref_book_record.currval, 825, null, 5, null, null);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
(seq_ref_book_record.currval, 826, 'прочие', null, null, null);

commit;
exit;