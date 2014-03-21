/*
Справочник:
	Шифры видов реализованного (выбывшего) имущества
Список изменений:
	Первоначальное заполнение данными
Данные:	
	1;амортизируемое имущество (кроме нематериальных активов)
	2;амортизируемое имущество – нематериальные активы
	3;прочее имущество, стоимость которого отнесена на расходы (РНУ5, РНУ-48)
	4;прочее имущество, стоимость которого не отнесена на расходы
	5;имущественные права в т.ч. права на земельные участки (не отвечающие требованиям статьи 264.1 НК РФ)
	6;права на земельные участки (отвечающие требованиям статьи 264.1 НК РФ)
	7;доли, паи
*/
INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES
  (seq_ref_book_record.nextval, 1, 82, to_date('01.01.2012','dd.mm.yyyy'), 0);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
  (seq_ref_book_record.currval, 804, null, 1, null, null);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
  (seq_ref_book_record.currval, 805, 'амортизируемое имущество (кроме нематериальных активов)', null, null, null);

INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES
(seq_ref_book_record.nextval, 2, 82, to_date('01.01.2012','dd.mm.yyyy'), 0);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
(seq_ref_book_record.currval, 804, null, 2, null, null);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
(seq_ref_book_record.currval, 805, 'амортизируемое имущество – нематериальные активы', null, null, null);

INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES
(seq_ref_book_record.nextval, 3, 82, to_date('01.01.2012','dd.mm.yyyy'), 0);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
(seq_ref_book_record.currval, 804, null, 3, null, null);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
(seq_ref_book_record.currval, 805, 'прочее имущество, стоимость которого отнесена на расходы (РНУ5, РНУ-48)', null, null, null);

INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES
(seq_ref_book_record.nextval, 4, 82, to_date('01.01.2012','dd.mm.yyyy'), 0);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
(seq_ref_book_record.currval, 804, null, 4, null, null);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
(seq_ref_book_record.currval, 805, 'прочее имущество, стоимость которого не отнесена на расходы', null, null, null);

INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES
(seq_ref_book_record.nextval, 5, 82, to_date('01.01.2012','dd.mm.yyyy'), 0);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
(seq_ref_book_record.currval, 804, null, 5, null, null);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
(seq_ref_book_record.currval, 805, 'имущественные права в т.ч. права на земельные участки (не отвечающие требованиям статьи 264.1 НК РФ)', null, null, null);

INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES
(seq_ref_book_record.nextval, 6, 82, to_date('01.01.2012','dd.mm.yyyy'), 0);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
(seq_ref_book_record.currval, 804, null, 6, null, null);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
(seq_ref_book_record.currval, 805, 'права на земельные участки (отвечающие требованиям статьи 264.1 НК РФ)', null, null, null);

INSERT INTO ref_book_record(id, record_id, ref_book_id, version, status) VALUES
(seq_ref_book_record.nextval, 7, 82, to_date('01.01.2012','dd.mm.yyyy'), 0);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
(seq_ref_book_record.currval, 804, null, 7, null, null);
INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
(seq_ref_book_record.currval, 805, 'доли, паи', null, null, null);

commit;
exit;