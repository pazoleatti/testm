/*
Справочник:
	Шифры видов реализации (выбытия)
Список изменений:
	Первоначальное заполнение данными
Данные:	
	1;продажа
	2;списание по ветхости, ликвидация
*/
INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES
  (seq_ref_book_record.nextval, 1, 83, to_date('01.01.2012','dd.mm.yyyy'), 0);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
  (seq_ref_book_record.currval, 806, null, 1, null, null);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
  (seq_ref_book_record.currval, 807, 'продажа', null, null, null);

INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES
(seq_ref_book_record.nextval, 2, 83, to_date('01.01.2012','dd.mm.yyyy'), 0);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
(seq_ref_book_record.currval, 806, null, 2, null, null);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
(seq_ref_book_record.currval, 807, 'списание по ветхости, ликвидация', null, null, null);

commit;
exit;