insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Acik Deniz Radyo ve Televizyon Iletisim Yayincilik Ticaret ve Sanayi A.S.');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '34394, Turkey, Esentepe-Sisli/Istanbul, Buyukdere Caddesi № 141, 1.Bodrum Kat ');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '792';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '754033');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('28.09.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Afelmor Overseas Limited');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Poseidonos 1, LEDRA BUSINESS CENTRE, Egkomi, 2406 Nicosia, Cyprus');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '196';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '311258');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('10.12.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'ALB EDV-Service GmbH');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Schwarzenbergplatz 3, 1010 Wien, Austria');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '040';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, 'FN 189976b');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.02.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 4, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Alfield Holdings Limited');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'P.O. Box 3175, Road Town, Tortola, British Virgin Islands');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '092';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '1807585');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('21.03.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 5, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'ALPHA Plus, s.r.o.');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Высока 9, индекс 81000, Братислава, Словакия');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '703';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '45533369');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.02.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 6, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Arimero Holding Limited');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Арх. Макариоу III, 284 Фортуна Корт Блок Б, 2 этаж, А.Я. 3105, г. Лимассол, Кипр');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '196';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '146742');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.12.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 7, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'AWAMAR HOLDINGS LIMITED');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Poseidonos, 1 Ledra Business Centre, Egkomi, 2406, Nicosia, Cyprus ');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '196';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '328582');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('21.03.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 8, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Bantas Nakit ve Kiymetli Mal Tasima ve Guvenlik Hizmetleri A.S.');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Ruzgarlibahce Mahallesi Kavak Sok. impa is Merkezi No:12 Kat 7 Kavacik-Beykoz/Istanbul');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '792';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '687191');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('28.09.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 9, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Bayworld Limited');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Poseidonos, 1 Ledra Business Centre, Egkomi, 2406, Nicosia, Cyprus ');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '196';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '147064');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('21.03.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 10, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'BEVO-Holding GmbH');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Schwarzenbergplatz 3, 1010 Wien, Austria');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '040';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, 'FN 209767g');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.02.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 11, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Castlequest Limited ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '3 Themistokli Dervi Street, Julia House, 1066 Nicosia, Cyprus');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '196';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '217947');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.09.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 12, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'CR Erdberg Eins GmbH &'||'Co KG');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Thomas-Klestil-Platz 1 1030 Vienna/ AUSTRIA');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '040';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, 'FN 222320t');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.09.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 13, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Deniz Faktoring Anonim Sirketi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '34394, Turkey, Esentepe-Sisli/Istanbul, Buyukdere Caddesi № 141, Kat:8');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '792';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '398630');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('28.09.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 14, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Deniz Finansal Kiralama Anonim Sirketi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '34394, Turkey, Esentepe-Sisli/Istanbul, Buyukdere Caddesi № 141, Kat:7');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '792';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '382904');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('28.09.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 15, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Deniz Immobilien Service GmbH');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Thomas-Klestil-Platz 1, 1030 , Wien, Austria');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '040';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, 'FN 406997a');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('19.12.2013', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 16, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Deniz Kartli Odeme Sistemleri A.S.');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '34394, Turkey, Esentepe-Sisli/Istanbul, Buyukdere Caddesi № 141, Kat:14');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '792';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '697314');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('28.09.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 17, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Deniz Portfoy Yonetimi A.S.');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '34394, Turkey, Esentepe-Sisli/Istanbul, Buyukdere Caddesi № 141, Kat:19');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '792';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '409441');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('28.09.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 18, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Deniz Yatirim Menkul Kiymetler Anonim Sirketi');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '34394, Turkey, Esentepe-Sisli/Istanbul, Buyukdere Caddesi № 141, Kat:9');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '792';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '388440');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('28.09.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 19, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Deniz Gayrimenkul Yatirim Ortakligi A.S.');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '34394, Turkey, Esentepe-Sisli/Istanbul, Buyukdere Caddesi № 141, Kat:22');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '792';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '330253');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('28.09.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 20, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'DENIZBANK AG');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Thomas Klestil Platz A-1030   Vienna / AUSTRIA');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '040';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, 'ESBKATWW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('28.09.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 21, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'DENIZBANK ANONIM SIRKETI');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '34394, Turkey, Esentepe-Sisli/Istanbul, Buyukdere Caddesi № 141');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '792';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, 'DENITRIS');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('28.09.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 22, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Denizbank Kultur Sanat Yayincilik Ticaret ve Sanayi A.S.');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '34394, Turkey, Esentepe-Sisli/Istanbul, Buyukdere Caddesi № 141, 1.Bodrum Kat');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '792';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '537440');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('28.09.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 23, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'DESTEK VARLIK YONETIM A. S. ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '34394, Turkey, Esentepe-Sisli/Istanbul, Buyukdere Caddesi № 141, Kat:8');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '792';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '867402');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('05.12.2013', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 24, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Dieze B.V.');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Клод Дебуссилиаан 26, 1082MD, Амстердам');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '528';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '34247573');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.12.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 25, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Egressy 2010 Ingatlanforgalmazo Korlatolt Felelossegu Tarsasag');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Ракосжи ут 7, 1088 Будапешт, Венгрия');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '348';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '01-09-951467');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.02.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 26, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'EKSPRES MENKUL DEGERLER A.S. ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '34394, Turkey, Esentepe-Sisli/Istanbul, Buyukdere Caddesi № 141, Kat:9');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '792';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '372660');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('28.09.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 27, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Euro Deniz International Banking Unit Limited');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '14.Serif Arzik Sokak Kosklu Ciftlik Lefkosa- K.K.T.C.');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '196';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, 'M.S. 5402');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('28.09.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 28, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Fermille Investments Limited');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'P.O. Box 3175, Road Town, Tortola, British Virgin Islands');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '092';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '1807576');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('21.03.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 29, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'First Dynasty Mines Limited');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Куриаку Матси 3, Руссос Лимассол Тауэр, 5 этаж, офис 5А, П/К 3040, Лимассол, Республика Кипр');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '196';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '320344');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.12.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 30, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'GABELLI CONSULTANCY LIMITED');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '10, Diomidous Street, Alphamega Akropolis Building, 3 floor, office 401, 2024, Nicosia, Cyprus');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '196';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '160589');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('09.06.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '12';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 31, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Garay Center Ingatlanforgalmazo es Ingatlanhasznosito Korlatolt Felelossegu Tarsasag');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Ракосжи ут 7, 1088 Будапешт, Венгрия');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '348';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '01-09-968599');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.02.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 32, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'GeoProMining Gold Canada Ltd.');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Лакович, Шиер&'||'Хоффман, Комната 300, 204 Блэк Стрит, Уайтхорс, YT Y1A2M9, Канада');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '124';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '12927 9006 RC 0002');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.12.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 33, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'GeoProMining Invesment (CYP) Limited');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Kyriakou Matsi, 3, Roussos Limassol Tower, 5th floor,  Flat|/Office 5A, P.C. 3040, Limassol, Cyprus');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '196';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '272638');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.12.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 34, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'GeoProMining Ltd. ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Дрэк Чемберс, П/Я 3321, Роуд Таун, Тортола, Британские Виргинские острова');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '092';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '438764');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.12.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 35, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'GeoProMining SEA Ltd.');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Ариас, Фабрега&'||'Фабрега, Уровень 1, Палм Гроув Хаус Уикхемс Кэй 1, Тортола, Британские Виргинские острова');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '092';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '1758537');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.12.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 36, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'GLANBURY INVESTMENTS LTD');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Kyriakou Matsi, 16, EAGLE HOUSE, 3rd Floor P.C, 1082, Nicosia, Cyprus');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '196';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '251447');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 37, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Glenston Investments Limited');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'P.O. Box 3175, Road Town, Tortola, British Virgin Islands ');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '092';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '1732647');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('10.12.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 38, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'GPM Armenia B.V.');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Клод Дебуссилиаан 26, 1082MD, Амстердам');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '528';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '33030763');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.12.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 39, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'GPM Russia B.V.');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Клод Дебуссилиаан 26, 1082MD, Амстердам');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '528';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '33273491');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.12.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 40, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Intertech Bilgi islem ve Pazarlama Ticaret A.S.');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '34394, Turkey, Esentepe-Sisli/Istanbul, Buyukdere Caddesi № 141');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '792';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '232834');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('28.09.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 41, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Karlou B.V.');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Клод Дебуссилиаан 26, 1082MD, Амстердам');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '528';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '14034376');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.12.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 42, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Karolyi Ingatlan 2011 Korlatolt Felelossegu Tarsasag');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Ракосжи ут 7, 1088 Будапешт, Венгрия');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '348';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '01-09-959502');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.02.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 43, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Kinevart Investments Limited');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Poseidonos 1, LEDRA BUSINESS CENTRE, Egkomi, 2406 Nicosia, Cyprus');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '196';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '300698');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('10.12.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 44, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'KIPARISIANA INVESTMENT LTD');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Themistokli Dervi, 48, ATHIENITIS CENTENNIAL  BUILDING, 1ST floor, Flat/Office 104 P.C. 1066, Nicosia, Cyprus');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '196';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '263416');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 45, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Mainframe Investments Limited');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'а/я 146, Роуд Таун, Тортола, Британские Виргинские Острова');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '092';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '590128');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 46, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Nikitas Brokerage Limited');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Эйч энд Джей Корпорейт  Сервисиз (Кайман) Лтд., P.O. Box 866 GT, 5 этаж, Андерсон Сквэр Билдинг, Шедден Роуд, Большой Кайман, Каймановы острова  KY1-1103');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '136';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '68634');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 47, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Nouvier Holdings Limited');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Poseidonos, 1 Ledra Business Centre, Egkomi, 2406, Nicosia, Cyprus ');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '196';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '126282');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('21.03.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 48, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Panclub Enterprises Limited');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '2-4 Arch. Makariou III,  Capital Center, 9 floor, 1065, Nicosia, Cyprus');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '196';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '326823');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.04.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 49, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'PB Norge');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'MUNKEDAMSVEIEN 62C, 0270, OSLO, NORWAY');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '578';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '892043612');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('27.08.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 50, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'PB Volga');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'THEMISTOKLI DERVI,3 JULIA HOUSE, NICOSIA, CYPRUS');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '196';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '159623');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('27.08.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 51, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Polenica Investments Limited');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '7 Пердика Стрит, Строволос, Никосия, П.К. 2057, Кипр');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '196';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '105962');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 52, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'PRIVATINVEST d.o.o.');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'ул. Дунайска честа 128а, 1000 Любляна, Словения');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '705';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '5842093000');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.02.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 53, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Pronam Nekretnine d.o.o.');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Варшавска 9, 10000, Загреб, Хорватия');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '191';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '080413774');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.02.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 54, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Rocket (Bermuda) Limited (Troika Dialog (Bermuda) Limited)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Виктория Плейс, 31 Виктория Стрит, Гамильтон HМ10, Бермудские острова');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '060';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '35164');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 55, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Roinco Enterprises Limited');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '15 Темистокли Дерви Стрит, Маргарита Хаус, а/я 1066, Никосия, Кипр');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '196';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '72221');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 56, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'SA&'||'PM (Cyprus) Limited');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '2-4 Арх. Макариоу III, Кэпитал Сентер, 9 этаж, П.К. 1065 Никосия, Кипр  ');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '196';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '108928');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 57, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Salgon Investments Limited ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Кипр, 1095 Ниокосия, Гладстонос, 31');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '196';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '218026');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.09.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 58, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'SB International S.a.r.l');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '46A, Avenue J.F. Kennedy, L-1855 Luxembourg, Grand Duchy of Luxembourg');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '442';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, 'В 161089');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 59, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'SB Leasing Cyprus Limited');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Arch. Makariou III, 2-4, CAPITAL CENTER, 9th floor, 1065, Nicosia, Cyprus');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '196';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '288760');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 60, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'SB LEASING IRELAND LIMITED');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '26 Upper Pembroke Street, Dublin 2, Ireland');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '372';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '485936');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 61, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'SB Luxembourg S.a.r.l.');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'L-1258, Luxembourg, 22, rue Jean-Pierre Brasseur R.C.S. Luxembourg ');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '442';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, 'B 165310');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('09.12.2011', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 62, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'SB Securities S.A.');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '2, boulevard Konrad Adenauer, L -1115 Luxembourg');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '442';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, 'B 171037');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('23.08.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 63, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Sberbanrk" A.D. Banja Luka ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Еврейска 71, 78 000 Баня Лука, Босния и Герцеговина');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '070';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, 'SABRBA2B ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.02.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 64, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Sberbank d.d. ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Варшавска 9, 10000 Загреб, Хорватия');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '191';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, 'VBCRHR22');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.02.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 65, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Sberbank banka d.d. ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'ул. Дунайска честа 128а, 1000 Любляна, Словения');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '705';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, 'SABRSI2X');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.02.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 66, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Sberbank BH d.d. Sarajevo');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Фра Андела Ждвиждовича 1, 71000 Сараево, Босния-Герцеговина');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '070';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, 'SABRBA22');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.02.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 67, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Sberbank Slovensko, a.s. ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Высока 9, индекс 81000, Братислава, Словакия');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '703';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, 'LUBASKBX');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.02.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 68, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, ' Sberbank Srbija a.d. Beograd ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Бульвар Михаила Пупина 165г, 11070 Белград, Сербия');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '688';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, 'SABRRSBG');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.02.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 69, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Sberbank CIB (UK) Limited');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '85 Флит Стрит, 4-й этаж, Лондон ECY4 1AE');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '826';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '4783112');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 70, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Sberbank CIB USA, Inc.');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '1013 Центр Роуд, город Уилмингтон, округ Нью-Касл-19805 Делавэр');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '840';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '2705815');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 71, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Sberbank CZ, a.s.');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'На Панкрачи 1724/129, 140 00 Прага, Чешская Республика');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '203';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, 'VBOECZ2X');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.02.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 72, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Sberbank Europe AG');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Schwarzenbergplatz 3, 1010 Wien, Austria ');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '040';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, 'SABRATWW');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.02.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 73, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Sberbank Hungary Private Company Limited by Shares ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Ракосжи ут 7, 1088 Будапешт, Венгрия');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '348';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, 'MAVOHUHB');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.02.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 74, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'SBERBANK INVESTMENTS LIMITED');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Poseidonos, 1, LEDRA BUSINESS CENTRE, Egkomi, 2406, Nicosia, Cyprus ');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '196';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '293417');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 75, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Sberbank (Switzerland) AG');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Gartenstrasse 24, 8002 Zurich, Switzerland ');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '756';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, 'SLBZCHZZ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('23.12.2011', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 76, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'SBGB CYPRUS Ltd.');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Kyriakou Matsi, 16, EAGLE HOUSE, 10th floor, Agioi Omologites, 1082, Nicosia, Cyprus');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '196';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '287481');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 77, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'SBI Voskhod Capital SICAV-SIF');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '65, Boulevard Grande-Duchesse Charlotte L-1331, Luxembourg');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '442';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, 'B 161153');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 78, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'SBTVF Limited ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '6 Иоанны Стилиану, 2 этаж, офис 202, 2003 Никосия, Кипр');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '196';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '151422');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 79, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'SIB (CYPRUS) LIMITED ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '2-4 Арх. Макариоу III, Кэпитал Сентер, 9 этаж, П.К. 1065 Никосия, Кипр   ');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '196';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '119924');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 80, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Silver Standard Operations Limited');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '2-4 Арх. Макариоса III Авеню, Кэпитал Сентер, 9 этаж, 1065 Никосия, Кипр');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '196';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '84301');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 81, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, ' "Strategy Partners Kazakhstan" ( Стратеджи Партнерс Казахстан) ТОО  ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Казахстан, 010000, г. Астана, ул. Кунаева, 12/1, офис 212  ');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '398';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '36997-1901-ТОО ');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 82, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"SUPER KARTICA" d.o.o. Beograd');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Belgrad, Bulevar Mihaila Pupina 4');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '688';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '20918314');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('27.01.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 83, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'TDAM (Bermuda) Limited');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Виктория Плейс, 31 Виктория Стрит, Гамильтон HМ10, Бермудские острова');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '060';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '35431');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 84, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'TDAM Private Equity (Bermuda) Limited');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Виктория Плейс, 31 Виктория Стрит, Гамильтон HМ10, Бермудские острова');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '060';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '37579');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 85, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'T.D. E.S.O.P. Holdings Limited');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '2-4 Арх. Макариоу III, Кэпитал Сентер, 9 этаж, П.К. 1065 Никосия, Кипр  ');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '196';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '80929');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 86, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'TD KUA Holdings Limited');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '2-4 Арх. Макариоса III Авеню, Кэпитал Сентер, 9 этаж, 1065 Никосия, Кипр');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '196';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '126364');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 87, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Troika Capital Partners Limited');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Виктория Плейс, 31 Виктория Стрит, Гамильтон HМ10, Бермудские острова');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '060';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '36659');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 88, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Troika Capital Partners Limited');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '2-4 Арх. Макариоса III Авеню, Кэпитал Сентер, 9 этаж, 1065 Никосия, Кипр');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '196';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '184848');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 89, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Troika Dialog Avto Holdings Limited  ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Виктория Плейс, 31 Виктория Стрит, Гамильтон HМ10, Бермудские острова');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '060';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '38254');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 90, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Troika Dialog Group Limited');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Эйч энд Джей Корпорейт Сервисиз (Кайман) Лтд., P.O. Box 866 GT, 5 этаж, Андерсон Сквэр Билдинг, Шедден Роуд, Большой Кайман, Каймановы острова KY1-1103');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '136';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '46622');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 91, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Troika Dialog Investments Limited');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '2-4 Арх. Макариоса III Авеню, Кэпитал Сентер, 9 этаж, 1065 Никосия, Кипр');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '196';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '112570');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 92, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Troika General Partners Limited');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '3 этаж, Иксчейндж Хаус, 54-62 Атол Стрит, Дуглас, Остров Мэн, IM1 1JD');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '833';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '108257C');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 93, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'East Site Ingatlanforgalmazo es Ingatlanhasznosito Korlatolt Felelossegu Tarsasag');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Ракосжи ут 7, 1088 Будапешт, Венгрия');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '348';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '01-09-924312');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.02.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 94, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'V-Dat Informatikai Szolgaltato es Kereskedlmi Korlatolt Felelossegu Tarsasag');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Ракосжи ут 7, 1088 Будапешт, Венгрия');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '348';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '01-09-706914');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.02.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 95, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Voskhod Capital S.a.r.l.');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '65, Boulevard Grande-Duchesse Charlotte L-1331, Luxembourg');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '442';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, 'B 160704');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('28.03.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 96, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"3D" ЗАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 125009, г. Москва, Романов пер., д.4, стр. 2');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7723173826');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770401001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 97, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"3Д Мониторы" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, Республика Татарстан, г. Казань, ул. Петербургская, д. 50');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '1655167651');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '165501001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 98, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Авиценна-Сочи" ООО ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 344116 Ростовская обл., г. Ростов-на-Дону, пр-т Стачки, д.25');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '2320067512');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '616201001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('23.11.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 99, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Автоматизированная система торгов государственного оборонного заказа" ООО ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '117393, г.Москва, ул.Профсоюзная, д.78, стр.1');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7728312865');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '772801001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.09.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 100, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"АВТОМАТИЗИРОВАННАЯ СИСТЕМА ТОРГОВ - УКРАИНА" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Украина, 03124, г. Киев, бульвар Ивана Лепсе, д. 8, корп. 64');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '804';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '38465475');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('19.11.2013', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 101, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Автомобильная компания "ДерВейс" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 369005, Карачаево-Черкесская Республика, г. Черкесск, ул. Подгорная, д.134, корп. В');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '0901050261');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '090101001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 102, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Агаракский  медно-молибденовый Комбинат" ЗАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Республика Армения, 3403, Сюникский марз, Агарак, ул. Гарегин Ниждеи, д.7');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '051';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '79.140.00036');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.12.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 103, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"АктивБизнесКоллекшн" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 117997, г. Москва, ул. Вавилова, д.19');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7736659589');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '773601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.04.2013', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 104, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"АРЗИЛ" ОАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия. 357820, Ставропольский край, г. Георгиевск, ул. Чугурина, д. 18');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '2625028532');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '262501001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 105, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"АРЗИЛ-Втормет"  ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия. 357820, Ставропольский край, г. Георгиевск, ул. Чугурина, д. 18');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '2625037054');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '262501001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 106, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Аукцион"  ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 111024, г. Москва, шоссе Энтузиастов, д.14');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7710203590');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '772201001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 107, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Байкалский горнолыжный курорт "Гора Соболиная" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '665932, Иркутская обл., Слюдянский р-он, г. Байкальск, м-он Красный Ключ, 90');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '3837002418');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '383701001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 108, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Бинотэк" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '141707, Московская обл., г. Долгопрудный, ул. Жуковского, д. 6');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7705289183 ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '500801001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.12.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 109, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'БЛАГОТВОРИТЕЛЬНАЯ ОРГАНИЗАЦИЯ "БЛАГОТВОРИТЕЛЬНЫЙ ФОНД "РЯДОМ С ТОБОЙ"');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Украина, 01034, Киев, ул. Владимирская, 46');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '804';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '38979466');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.11.2013', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 110, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"БММ Холдинг" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 115191, г. Москва, ул. 2-я Рощинская, д.4 пом. 1');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7725757552');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '772501001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.06.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 111, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Богатырская ТРОЙКА" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 123317, г.Москва, Пресненская наб., д.10 ');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7710337474');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770301001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 112, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"БПС-Лизинг" ЗАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '220013, Республика Беларусь, г. Минск, ул. Цнянская, д. 12, комн. 101-104');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '112';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '100625018');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 113, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"БПС-Сбербанк" ОАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '220005, Республика Беларусь, г. Минск, бульвар имени Мулявина, 6');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '112';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, 'BPSBBY2X');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 114, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Бумажная фабрика группы предприятий "ГОТЭК" ООО ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '301650, Тульская область, г. Новомосковск, Комсомольское шоссе, д.64а');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7116134503');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '711601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.09.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 115, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Былинные богатыри" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 123317, г.Москва, Пресненская наб., д.10');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7709297379');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770301001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 116, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"ВИДЕО ТЕХНОЛОГИИ" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '421001, Республика Татарстан,  г. Казань, ул. Чистопольская,  д. 62, стр. 5 ');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '1657193791');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '165701001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.04.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 117, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Видимакс" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, Республика Татарстан, г. Казань, ул. 2-я Юго-Западная, д.3');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '1656051716');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '165601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 118, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"ГАММА-С" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '111024, г.Москва, шоссе Энтузиастов, д.14');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7722337550');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '772201001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('21.08.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 119, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"ГАРАНТ-СВ" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '298685, Россия, Республика Крым, г. Ялта, с. Оползневое, ул. Генерала Острякова, д.9');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '9103007830');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '910301001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 120, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"ГеоПроМайнинг" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Российская Федерация, 107031, Москва, ул. Петровка, д. , 5-й этаж');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7706683947');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770701001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.12.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 121, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"ГеоПроМайнинг Голд" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Республика Армения, 0015, г. Ереван, ул. Паронян, д. 21');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '051';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '273.110.02424');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.12.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 122, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Георгиевский Арматурный Завод" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '357820, Российская Федерация, Ставропольский  край, г. Георгиевск, ул. Чугурина, д.18');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '2625800607');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '262501001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('10.06.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 123, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Гермес" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 115432, г. Москва, 2-ой Южнопортовый проезд, д. 12А,  стр. 7');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7723392970');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '772301001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 124, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Голден Оре" ООО ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Республика Армения, 0015, г. Ереван, ул. Паронян, д. 21');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '051';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '286.110.06031');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.12.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 125, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"ГОРИЗОНТ-СЕРВИС" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '298685, Республика Крым, с. Оползневое, Севастопольское шоссе, д.2');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '9103069850');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '910301001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('05.03.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 126, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Горнолыжный комплекс "Манжерок" ЗАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 649113, Республика Алтай, Майминский район, с. Манжерок, ул. Ленинская, 18');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '0408009440');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '041101001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 127, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"ГОТЭК" ЗАО ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '307170, Курская область, г. Железногорск, Промзона');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '4633000037');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '463301001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.09.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 128, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"ГОТЭК-ЛИТАР" ЗАО ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '307170, Курская область, г. Железногорск, Промзона');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '4633010853');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '463301001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.09.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 129, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"ГОТЭК-ПРИНТ" ЗАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '307170, Курская область, г. Железногорск, Промзона');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '4633010405');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '463301001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.09.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 130, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"ГОТЭК Северо-Запад" ЗАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '188681, Россия, Ленинградская область, Всеволожский район, деревня Новосаратовка - центр, участок №6');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '4703099079');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '470301001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.09.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 131, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"ГОТЭК-ЦЕНТР" ЗАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '301650, Тульская область, г. Новомосковск, Комсомольское шоссе, д.64а');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7116146114');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '711601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.09.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 132, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"ГОТЭК-Центр предоставления услуг" ООО ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '307170, Курская область, г. Железногорск, Промзона');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '4633016372');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '463301001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.09.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 133, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"ГОФРОДИВИЗИОН" ЗАО ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '307170, Курская область, г. Железногорск, Промзона');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '4633021510');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '463301001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.09.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 134, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Гранд Байкал" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 664050, г. Иркутск, ул. Байкальская, 279');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '3808079832');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '380801001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 135, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Грос" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '125167, г.Москва, Ленинградский проспект, д.37А, корп.4');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7706753665');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 136, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Грос Ритейл ООО ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 400074, г. Волгоград, ул. Баррикадная, д.8');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '3460006141');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '346001001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('28.02.2013', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 137, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Группа Химэкс" АО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '369005, г.Черкесск, ул.Шоссейная, д.15');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '6439009628');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '091701001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('09.07.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 138, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Деловая среда" АО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 117997, г. Москва, ул. Вавилова, д.19');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7736641983');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '773601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('11.04.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 139, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Денизбанк Москва" ЗАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, ' Российская Федерация,  123022, Москва, 2-ая Звенигородская улица, д. 13/42');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7705205000');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '775001001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, 'IKBARUMM');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('28.09.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 140, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"ДЕРВЕЙС АВТО" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 115088, г. Москва, 2-й Южнопортовый проезд, д, 16');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7727655940');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '772301001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 141, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Дочерний Банк Акционерное Общество "Сбербанк России" (ДБ АО "Сбербанк")');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Республика Казахстан, 050059, г. Алматы, Бостандыкский район, проспект Аль-Фараби, дом 13/1');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '398';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, 'SABRKZKA');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 142, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"ЕвроСтройПроект" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 119602. г. Москва, ул. Никулинская. д.23, корп. 2');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7729680004');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '772901001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 143, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Единая Транспортная Карта" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '141504, Российская Федерация, Московская область, г. Солнечногорск, ул. Обуховского, д. 46');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '5044092657');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '504401001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('08.12.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 144, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Единый информационно-расчетный центр Калужской области" АО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 249037, Калужская область, г. Обнинск, улица Шацкого, дом 13');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '4025437985');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '402501001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('27.11.2013', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 145, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Единый информационно-расчетный центр Регион-21" ОАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '429951, Чувашская Республика, г. Новочебоксарск, ул. Молодежная, д. 12');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '2124039325');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '212401001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('26.08.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 146, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Единый расчетный центр на территории Республики Марий Эл" ОАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 424000 , Республика Марий Эл, г. Йошкар-Ола, Ленинский проспект, д.25');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '1215181990');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '121501001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('30.06.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 147, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Единый центр процессинга и биллинга в городе Нижний Тагил" АО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 622034, Свердловская область, г. Нижний Тагил, ул. Газетная, д.72 ');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '6623104778');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '662301001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('03.09.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 148, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Единый центр процессинга и биллинга Курской области" АО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'г. Курск, пр-т. Энтузиастов, д. 1-а ');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '4632201333');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '463201001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.03.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 149, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Загорский Лакокрасочный Завод" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия. 141300, Московская обл., г. Сергиев Посад, Московское шоссе, д. 22 "а"');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '5042097177');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '504201001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 150, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Звезда" ОАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Российская Федерация, Республика Саха (Якутия), 678730, пгт Усть-Нера, ул. Ленина, д.2 ');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '1420041450');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '142001001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.12.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 151, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"ИКC" ЗАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 603005, г. Н. Новгород, ул. Октябрьская, д. 33');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '5263023906');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '526001001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 152, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Инвертика" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 660119, Красноярский край, г. Красноярск, ул. 40 лет Победы, д.4, оф. 25');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '2465223324');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '246501001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 153, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"ИНЖКОМСЕТЬ" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 119415, г.Москва, проспект Вернадского, д.51, стр. 3');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7729754908');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '772901001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('11.03.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 154, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Квартал 674-675" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 125047, г.Москва, ул. Лесная, д. 5');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7710520840');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '771001001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('10.12.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 155, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Кипарис 2" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '298685, Россия, Республика Крым, г. Ялта, с. Оползневое, ул. Генерала Острякова, д.9  ');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '9101001550');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '910301001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 156, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"КОМПАНИЯ ПО УПРАВЛЕНИЮ АКТИВАМИ - АДМИНИСТРАТОР ПЕНСИОННЫХ ФОНДОВ "ТРОЙКА ДИАЛОГ УКРАИНА" ООО ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Украина, 04050, г. Киев, ул. Дегтяревская, 4');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '804';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '35624670');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 157, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Корпоративный университет Сбербанка" АНО ДПО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 117997, г. Москва, ул. Вавилова, д.19');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7736128605');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '773601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('13.03.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 158, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"КОРУС Консалтинг СНГ" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 194100, г. Санкт-Петербург, Б. Сампсониевский пр-кт, д. 68, литер Н, помещение 1Н ');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7801392271');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '780201001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('21.03.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 159, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Красная поляна" НАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '354000, Краснодарский край, г. Сочи, ул. Северная, д. 14А');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '2320102816');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '232001001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 160, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Красная поляна Отель Менеджмент"  ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 354000, Краснодарский край, г. Сочи, Центральный район, ул. Северная, д. 14А, включающее в себя: помещение № 28 четвертого этажа здания литер А, площадью 16,0 кв.м.');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '2320208509');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '232001001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('14.12.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 161, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Крафтум-РУС" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 199155, г. Санкт-Петербург, ул. Железноводская, д.17/5, лит. Д');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7801378140');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '780101001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 162, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Лагом-Украина" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '08322, Украина, Киевская обл., Бориспольский р-он, с. Пролиски,  ул. Промышленная, д. 9');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '804';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '33053845');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.12.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 163, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Лагом-Урал" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '620097, Свердловская область, г. Екатеринбург, ул. Черняховского, д. 92, офис 213');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '6671149955');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '667401001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.12.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 164, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Ледсера" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 142432, Московская обл., Ногинский район, г. Черноголовка, ул. Лесная, д.9');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '5031099630');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '503101001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('05.06.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 165, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"ЛИЗИНГ ПРОЕКТ" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 119017, г. Москва, ул. Большая Ордынка, д. 40, стр. 4');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7706811074');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('23.06.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 166, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Миметикс Холдинг" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 115191, г. Москва, 2-я Рощинская ул., д. 4, пом. 1');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7725765426');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '772501001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.03.2013', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 167, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"МЛП-КАД" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 193149, Ленинградская область, Всеволожский  район, г.п. имени Свердлова, дер. Новосаратовка, промзона "Уткина заводь", уч. 1, Административно-бытовой корпус ');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '4703085654');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '4703001001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('21.03.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 168, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"МЛП-Подольск" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 142100, Московская обл., г. Подольск, ул. Поливановская, 9');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '5036068121');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '5036001001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('21.03.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 169, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Монолитные Системы" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 349019, Воронежская обл., г. Воронеж, ул. Газовая, д.1, корп. "А"');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '3666107810');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '366601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('28.09.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 170, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Московский городской Гольф Клуб" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 119590, г. Москва, ул. Довженко, д.1');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7729276546');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '772901001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.06.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 171, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Мосстройвозрождение" ОАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 109012, г. Москва, ул. Ильинка, д.4');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7719593458');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '771001001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 172, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"МСТ ПРОЕКТ" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '119017, г. Москва, ул. Большая Ордынка, дом 40, строение 4');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7706808924');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('23.04.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 173, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Мясокомбинат Усть-Лабинский" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, Краснодарский край, г. Усть-Лабинск, ул. Литвинова, 2');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '2309088614');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '235601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('23.11.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 174, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Негосударственный Пенсионный Фонд Сбербанка" АО (НПФ Сбербанка" АО)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 115162, г. Москва, ул. Шаболовка, д. 31Г ');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7725352740');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '772501001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('10.06.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 175, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Нейрок Оптика" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 125459, г. Москва,  проезд Донелайтиса, д.38');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7733129456');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '773301001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 176, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"НЕФТЕСЕРВИС" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '125167, г.Москва, Ленинградский проспект, д.37А, корп.4');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7706811652');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('07.07.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 177, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'НКО "Универсальная электронная карта" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '127055, город Москва, улица Образцова, дом 14 ');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7750005860');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '775001001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('28.01.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 178, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'НКО "Яндекс. Деньги" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 119021, г. Москва, ул. Тимура Фрунзе, д.11, стр. 44');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7750005725');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '775001001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('04.07.2013', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 179, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Новый Исток" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 344002, г. Ростов-на-Дону, ул. 2-я Луговая, д.24а');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '6162065394');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '616201001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('30.08.2013', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 180, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"НРК АКТИВ" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 119017, г. Москва, ул. Большая Ордынка, д.40, стр.4');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7706818400');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('11.12.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 181, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Областной Единый Информационно-Расчетный Центр" ОАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 300041, г. Тула, ул. Л. Толстого, д.114-а, д.1, офис 229');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7121500561');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '710301001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('10.09.2013', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 182, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Объединенное Кредитное Бюро" ЗАО ("ОКБ" ЗАО)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 127006, г. Москва, ул. 1-я Тверская-Ямская, д.2, стр.1');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7710561081');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '771001001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 183, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"ПанКлуб" ЗАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 115280, г. Москва, ул. Ленинская слобода, д. 19, стр. 5');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7743765161');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '772501001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.04.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 184, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Параграфикс" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 117418, г. Москва, Нахимовский проспект, д.47');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7703287046');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '772701001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 185, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"ПБ Самара" ООО ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '443099, Самарская область, Г.Самара, ул. Максима Горького, д.78 "В"');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '6317068721');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '631701001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('27.08.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 186, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"ПБ Самара III" ООО ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '443099, Самарская область, Г.Самара, ул. Максима Горького, д.78 "В"');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '6317093990');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '631701001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('27.08.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 187, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Перспективные инвестиции" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 143000, Московская обл., г. Одинцово, ул. Молодежная, д.46');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '5032218680');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '503201001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 188, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Платиус" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '109380, Российская Федерация, город Москва, Проезд Проектируемый 4586, д.4, стр. 13, эт. 4, комната 14');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7723920588');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '772301001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('03.02.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 189, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Портопункт-Адлер" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 354000, Краснодарский край, г. Сочи, Курортный проспект, 18/1');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '2320153930');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '232001001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('20.06.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 190, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Прайм Шиппинг" ООО ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '443099, Самарская область, Г.Самара, ул. Максима Горького, д.78 "В"');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '6317060306');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '631701001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('27.08.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 191, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Прайм Шиппинг Холдинг"" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '443099, Россия, г. Самара, ул. Максима Горького, 78 "В", 3 этаж');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '6317106960');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '631701001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('16.06.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 192, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"ПРГ Технолоджи" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, Республика Татарстан, г. Казань, ул. Петербургская, д. 50, к. 24, оф. 104');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '1655167644');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '165501001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 193, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Проект Огни" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 119017, г. Москва, ул. Большая Ордынка, д. 40, стр. 4');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '2463222526');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 194, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Производственная фирма "ЛАГОМ" ЗАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Российская Федерация, 644012, г. Омск,  тракт Красноярский,  д. 107');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '5501095980');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '550101001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.12.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 195, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"ПС Яндекс. Деньги" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 119021, г. Москва, ул. Льва Толстого, д. 16');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7736554890');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '773601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('04.07.2013', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 196, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, ' Публичное акционерное общество "Ви Эс Банк"');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Украина, 79000, г. Львов, ул. Храбовского, 11');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '804';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, 'ELECUA2X');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.02.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 197, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Публичное Акционерное Общество "Дочерний Банк Сбербанка России" (АО "СБЕРБАНК РОССИИ")');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Украина, 01601, Киев, ул. Владимирская, 46 ');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '804';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, 'SABRUAUK');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 198, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Региональный информационный центр Кировской области" АО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '610046, Кировская область, г. Киров, ул. Захватаева, д. 21А');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '4345401610');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '434501001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('21.11.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 199, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Региональный информационный центр Орловской области" АО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 302030, г. Орел, ул. Курская, д.34');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '5753200537');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '575101001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('04.09.2013', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 200, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Региональный расчетно-кассовый центр" ОАО ("РРКЦ" ОАО)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 308010, г. Белгород, проспект Богдана Хмельницкого, 160');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '3123100113');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '312301001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 201, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Регистр универсальных электронных карт Кемеровской области" ОАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Российская Федерация, 650002, Кемеровская область, г. Кемерово, ул. Сосновый бульвар, д.1');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '4205224136');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '420501001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.06.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 202, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Регистраторское общество "СТАТУС" АО ("СТАТУС" АО)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 109544, г. Москва, ул. Новорогожская, д.32, стр.1');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7707179242 ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770901001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 203, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"РПК ПРОМ" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '125167, г.Москва, Ленинградский проспект, д.37А, корп.4');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7706818390');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('11.12.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 204, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Рублево-Архангельское" АО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 143408, Московская область, г. Красногорск, ул. Успенская, 5');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '5024093941');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '502401001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('21.06.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 205, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Русвтормет-Центр" ЗАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 117556. г. Москва, Симферопольский бульвар, д.3Г');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7726636857');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '772601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 206, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Русские горки" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '354000, Краснодарский край, г. Сочи, ул. Северная, 14А');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '2320204590');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '232001001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('31.07.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 207, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"РУСТ" ЗАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 191124, г. Санкт-Петербург, ул. Красного Текстильщика, д.2');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7801036001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '784201001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 208, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"РуТаргет" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '199178, г.Санкт-Петербург, 18-я Линия В.О., д.29,  лит.А, пом. 1-Н');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7801579142');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '780101001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('02.03.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 209, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Сабон" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '141707, Московская область, г. Долгопрудный, ул. Жуковского, д. 6');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '5008040766');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '500801001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.12.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 210, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Садовое кольцо" ЗАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '119019, г. Москва, Филипповский пер., д. 8, стр. 1');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7704617385');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770401001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 211, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Сарылах-Сурьма" ОАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Российская Федерация, Республика Саха (Якутия), 678730, пгт Усть-Нера, ул. Полярная, д.7 ');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '1420002690');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '142001001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.12.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 212, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Сатегор" ЗАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '141707, Московская обл., г. Долгопрудный, ул. Жуковского, д. 6');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '5008041110');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '500801001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.12.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 213, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Сатурн-А" ЗАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 123424, г. Москва, ул. Летная, вл. 98');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7733050781');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '773301001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 214, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"СБ-Глобал" ЗАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '220005, Республика Беларусь, г. Минск, бульвар имени Мулявина, 6, офис 308');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '112';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '191614201');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 215, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Сбербанк-Автоматизированная система торгов" ЗАО ("Сбербанк-АСТ" ЗАО)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 127055, г. Москва, ул. Новослободская, д.24, стр.2');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7707308480');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770701001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 216, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Сбербанк Инвестиции" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 143000, Московская обл., г. Одинцово, ул. Молодежная, д.46');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '5032229441');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '503201001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 217, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Сбербанк Капитал" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 117997, г. Москва, ул. Вавилова, д.19');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7736581290');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '773601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 218, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Сбербанк КИБ" ЗАО ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 125009, г.Москва, Романов переулок, д.4  ');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7710048970');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '775001001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 219, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Сбербанк Лизинг" АО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 143002, Московская обл., г. Одинцово, ул. Молодежная, д. 21');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7707009586');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '509950001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 220, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Сбербанк Лизинг Казахстан" ТОО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Республика Казахстан, г. Алматы, пр. Аль-Фараби,  д.15, ПФЦ "Нурлы Тау", блок 4В, оф. 21');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '398';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '107792-1910-ТОО ');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 221, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Сбербанк Лизинг Норд" ЗАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 150049, г. Ярославль, ул. Рыбинская, д.46');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7604105513 ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '760401001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 222, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"СБЕРБАНК ЛИЗИНГ УКРАИНА" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '01004, Украина, город Киев, ул. Красноармейская, д.5, блок Д, офис 14');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '804';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '37241931');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 223, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Сбербанк-Сервис" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 117997, г. Москва, ул. Вавилова, д.19');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7736663049');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '773601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('08.08.2013', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 224, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Сбербанк-Технологии" ЗАО ("СберТех" ЗАО)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 117105, г. Москва, Новоданиловская наб., д.10');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7736632467');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, ' 772601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 225, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Сбербанк-Технологии" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Республика Беларусь, 220073,  г. Минск, 1-й Загородный пер., д. 20, этаж 16, каб. 30 ');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '112';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '191690785');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 226, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Сбербанк Управление Активами" АО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 123317, г. Москва, Пресненская наб., д.10');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7710183778');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '775001001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 227, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Сбербанк Факторинг" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '125284, город Москва, Ленинградский проспект, дом 31 А, строение 1, помещение 1');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7802754982');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '780201001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('21.03.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 228, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Сберегательный Капитал" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 119002, г. Москва, ул. Арбат, д.10, этаж 3');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7736589109 ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770401001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 229, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Сберключ" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 127030, г. Москва, ул. Краснопролетарская, д.9, стр. 3');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7707752230');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770701001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 230, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Сберэнергодевелопмент" ООО ("СЭД" ООО)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 117997, г. Москва, ул. Вавилова, д.19');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7736625205 ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '773601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 231, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"СБ Инвест" ООО ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 344004, г. Ростов-на-Дону, ул. Батуринская, д 13/1');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '6164300420');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '616201001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 232, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"СБК" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 117997, г. Москва, ул. Вавилова, д.19');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7736611795 ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '773601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 233, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"СБК АГРО-ИНВЕСТ " ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '101000, г. Москва, Милютинский пер. д.13, стр. 1');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7708807475');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770801001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('19.02.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 234, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"СБК АКТИВ" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '125167, г.Москва, Ленинградский проспект, д.37А, корп.4');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7706806959');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('03.03.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 235, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"СБК ГЕОФИЗИКА " ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '125167, г.Москва, Ленинградский проспект, д.37А, корп.4');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7706806973');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('03.03.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 236, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"СБК Дубинино" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '141508, Россия, Московская обл., Солнечногорский район, д. Дубинино, д. 57');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '5044078050 ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '504401001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 237, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"СБК Инвест" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 117997, г. Москва, ул. Вавилова, д.19');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7736611812 ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '773601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 238, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"СБК КАРТОН" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '125167, г.Москва, Ленинградский проспект, д.37А, корп.4');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7706811500');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('02.07.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 239, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"СБК Металл" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 117997, г. Москва, ул. Вавилова, д.19');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7736611805 ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '773601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 240, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"СБК ОБУВЬ" ООО ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '125167, г.Москва, Ленинградский проспект, д.37А, корп.4');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7714355971');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '771401001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('23.09.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 241, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"СБК ПРОЕКТ" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '125167, г.Москва, Ленинградский проспект, д.37А, корп.4');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7706805634');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('30.01.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 242, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"СБК-Ритейл"');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '119017, г. Москва, ул. Большая Ордынка, дом 40, строение 4');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7706787209');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('27.12.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 243, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"СБК СКАЙ" ООО ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '197375, г.Санкт-Петербург, ул.Репищева, д.20, литера А');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7814280335');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '781401001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('11.09.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 244, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"СБК СТЕКЛО" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '125167, г.Москва, Ленинградский проспект, д.37А, корп.4');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7706806966');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('03.03.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 245, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"СБК СТРОЙ" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '125167, г.Москва, Ленинградский проспект, д.37А, корп.4');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7706811525');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('03.07.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 246, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"СБК ТРАНС" ООО ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '125167, г.Москва, Ленинградский проспект, д.37А, корп.4');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7714355072');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '771401001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('14.09.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 247, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"СБК Уран" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '125167, г.Москва, Ленинградский проспект, д.37А, корп.4');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7706804077');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('11.12.2013', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 248, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"СВК Инвест" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 119017, г. Москва, ул. Б. Ордынка, д. 40, стр. 4, офис 702D');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7706751770 ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 249, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"СВК Холдинг" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '125167, г.Москва, Ленинградский проспект, д.37А, корп.4');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7706751788 ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 250, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Сервис Деск" ЗАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '220005, Республика Беларусь, г. Минск, бульвар имени Мулявина, 6');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '112';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '191636450');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('03.10.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 251, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Сетелем Банк" ООО (Коммерческий Банк "БНП Париба Восток" ООО)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '125040, г. Москва, ул. Правды, дом 26');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7707083893');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '775001001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, 'CETBRUMM');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('31.08.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 252, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"СИБ Финанс" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 119049, г. Москва, ул. Мытная, д.28, стр. 3, пом. 2, ком.1');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7703576175');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.03.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 253, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"СИБ Финансовый брокер" АО ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 125009, г.Москва, Романов переулок, д.4  ');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7710165634');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '775001001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 254, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"СИБ Финансовый консультант" ЗАО ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 125009, г.Москва, Романов переулок, д.4  ');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7710031856');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770301001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 255, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"СИСТЕМЫ ЛОЯЛЬНОСТИ" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '420111, Республика Татарстан, г. Казань, ул. Лобачевского, 10В');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '1655303086');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '1655001001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('17.10.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 256, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"СКЛАДСКОЙ КОМПЛЕКС" ООО ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '125167, г.Москва, Ленинградский проспект, д.37А, корп.4');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7714356647');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '771401001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.09.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 257, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Склады 104" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '142050, Московская область, Домодедовский район, г.Домодедово, микрорайон Белые Столбы, вл. "Склады 104", стр.2');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '5009049271');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '500901001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('09.06.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '12';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 258, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"СНС Софт" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 127106, г. Москва, Алтуфьевское шоссе, д.5');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7715664852');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '771501001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 259, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"СНС Холдинг" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, Республика Татарстан, 420107, г. Казань, ул. Петербургская, д. 50');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '1655146919');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '165501001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 260, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Современные технологии" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 115432, г. Москва, 2-ой Южнопортовый пр-д, д. 12а, к. 1, стр. 6');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7708229993 ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770801001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 261, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Социальные гарантии" ОАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 414000, г. Астрахань, ул. Джона Рида, 37');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '3017064696');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '302501001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('08.02.2013', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 262, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Специализированный депозитарий Сбербанка" ООО ("Спецдепозитарий Сбербанка" ООО)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, г. Москва, ул. Вавилова, д.3');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7736618039 ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, ' 772501001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 263, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"СтавСталь" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Ставропольский Край, г. Невинномысск, ул. Низяева, 1 Р');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '2631054210');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '263101001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('10.06.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 264, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"СтавЧермет" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '357107, Ставропольский край, г. Невинномысск, ул. Низяева , д.37');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '2624800450');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '263101001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('10.06.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 265, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"СтемПар Холдинг" ООО ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия,115191, г. Москва, ул. 2-я Рощинская, д.4, пом. 1');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7725757369');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '772501001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.06.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 266, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"СТИК" ЗАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 121099, г. Москва, Смоленская площадь, д. 3');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7710447491');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770401001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('21.03.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 267, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Стратеджи Партнерс Групп" ЗАО ("СПГ" ЗАО)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 115054, г. Москва, Космодамианская наб., д. 52, стр.2');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7736612855 ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770501001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 268, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Страховая компания "Сбербанк страхование" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 115093, г. Москва, ул. Павловская, д.7');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7706810747');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '772501001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('16.06.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 269, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Страховая компания "Сбербанк страхование жизни" ООО ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 115162, Москва, ул. Шаболовка, дом 31Г ');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7744002123');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '775001001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('18.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 270, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Страховой брокер Сбербанка" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 119049, г. Москва, ул. Б. Якиманка, д.42, стр. 1_2, офис 205');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7706810730');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('16.06.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 271, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Строительный консалтинг ООО ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 394019, г. Воронеж, ул. Газовая, д.1, корп.  А ');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '3665089491');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '366501001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('26.09.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 272, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Сулинская Металлургическая Компания" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 346350, Ростовская обл., г. Красный Сулин, ул. Заводская, д. 1');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '6148559560 ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '614801001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 273, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Сухонский картонно-бумажный комбинат" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '162135, Вологодская область, г. Сокол, ул. Беднякова, д.3, офис 1');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '3519003961');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '352701001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.06.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 274, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"ТАСК" ЗАСО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '220053, Республика Беларусь, г. Минск, ул. Червякова, 46');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '112';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '100003006');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 275, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"ТД софт" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 123317, г.Москва, Пресненская наб., д.10');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7704659949');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770301001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 276, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Терасенс-НС" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 125047, г. Москва, ул. Фадеева, д.7, стр.1, офис 2');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7710891241');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '771001001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 277, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Универсальная электронная карта" АО ("УЭК" АО)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 119021, г. Москва, ул. Тимура Фрунзе, д.11, стр.15');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7727718421 ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770401001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 278, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Универсальная электронная карта Иркутской области" ОАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 664025, г. Иркутск, ул. Свердлова, д.10');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '3812155469');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '380801001 ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.05.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 279, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Универсальный лизинг" ЗАО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '307170, Курская область, г. Железногорск, Промзона');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '5032050910');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '463301001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.09.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 280, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Управляющая компания группы предприятий "ГОТЭК" АО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '141400, Московская область, г.Химки, ул.Ленинградская, владение 39, стр.6.');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '4633009632');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '504701001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('15.09.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 281, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"УПРАВЛЯЮЩАЯ КОМПАНИЯ СБВК " ЗАО ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 119019, г.Москва, ул. Знаменка, д.7, корп.3');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7704654394');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '775001001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('25.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 282, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Финансовая компания Сбербанка" ООО ("Сбербанк-Финанс" ООО)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 119002, г. Москва, пер. Сивцев Вражек, д. 29/16');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7736617998 ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770401001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 283, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Формат-Нева" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '196644, г. Санкт-Петербург, поселок Саперный, территория предприятия "Балтика", д. б/н, литера "Д"');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7817310228');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '781701001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.12.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 284, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"ФОРМОВОЧНЫЕ АВТОМАТЫ" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Российская Федерация, 141707, Московская обл., г.Долгопрудный, ул. Жуковского, д. 6');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7720239606 ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '500801001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.12.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 285, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Хозяйственные товары" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 644065, г. Омск, улица Заводская, 30');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '5501058233');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '550101001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.12.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 286, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Центр исследований и консалтинга "СТРАТЕГИЯ" ТОО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Республика Казахстан, 010000, г. Астана проспект Кабанбай батыра, 19');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '398';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '46215-1901-ТОО ');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.05.2013', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
		insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 287, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Центр недвижимости от Сбербанка" ООО ("ЦНС ООО")');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '117997, г.Москва, ул.Вавилова, д.19');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7736249247');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '773601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('20.07.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 288, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Центр программ лояльности" ЗАО ("Центр ПЛ" ЗАО)');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '115114, г. Москва, 1-й Дербеневский пер., д.5, пом. № 505/506     ');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7702770003 ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '772501001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 289, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Центр экспертизы по вопросам Всемирной торговой организации" АНО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, город Москва, улица Шаболовка, дом 26');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7706471558');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '770601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('11.07.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 290, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, 'Центральный банк Российской Федерации');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 107016, г. Москва, ул. Неглинная, д. 12');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7702235133 ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '775001001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '4а';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 291, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Цифровые технологии" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, '117997, г.Москва, ул.Вавилова, д.19');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '7736252313');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '773601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('07.09.2015', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '1б';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 292, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Эс Эм Джи Пластик" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Российская Федерация, 141707, Московская обл., г. Долгопрудный, ул. Жуковского,  д. 6');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '5025023256');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '500801001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.12.2014', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 293, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Южная автомобильная группа" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия,344004, г. Ростов-на-Дону, ул. Батуринская, д.13/1');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '6162062185');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '616201001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('29.06.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 294, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"ЮжСталь" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 346350, Ростовская обл., г. Красный Сулин, ул. Заводская, д. 1');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '6148560037 ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '614801001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('01.01.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 295, 520, to_date('01.01.2015', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5200, record_id from ref_book_value where attribute_id = 5251 and string_value = 'ВЗЛ';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5201, '"Ясень-К" ООО');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5202, 'Россия, 352332, Краснодарский край, г. Усть-Лабинск, ул. Заполотняная, д.1');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5203, record_id from ref_book_value where attribute_id = 5131 and number_value = 1;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5204, record_id from ref_book_value where attribute_id = 50 and string_value = '643';
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5205, '2356014828');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5206, '235601001');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5208, '');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5209, '');
	insert into ref_book_value (record_id, attribute_id, date_value) values (seq_ref_book_record.currval, 5210, to_date('23.11.2012', 'DD.MM.YYYY'));
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5212, record_id from ref_book_value where attribute_id = 5101 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5213, record_id from ref_book_value where attribute_id = 5111 and number_value = 2;
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5214, record_id from ref_book_value where attribute_id = 5121 and string_value = '9';

--IKSR/IKKSR
insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '754033' from ref_book_record rbr where ref_book_id = 520 and record_id = 1;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '754033' from ref_book_record rbr where ref_book_id = 520 and record_id = 1;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '311258' from ref_book_record rbr where ref_book_id = 520 and record_id = 2;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '311258' from ref_book_record rbr where ref_book_id = 520 and record_id = 2;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, 'FN 189976b' from ref_book_record rbr where ref_book_id = 520 and record_id = 3;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, 'FN 189976b' from ref_book_record rbr where ref_book_id = 520 and record_id = 3;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '1807585' from ref_book_record rbr where ref_book_id = 520 and record_id = 4;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '1807585' from ref_book_record rbr where ref_book_id = 520 and record_id = 4;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '45533369' from ref_book_record rbr where ref_book_id = 520 and record_id = 5;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '45533369' from ref_book_record rbr where ref_book_id = 520 and record_id = 5;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '146742' from ref_book_record rbr where ref_book_id = 520 and record_id = 6;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '146742' from ref_book_record rbr where ref_book_id = 520 and record_id = 6;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '328582' from ref_book_record rbr where ref_book_id = 520 and record_id = 7;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '328582' from ref_book_record rbr where ref_book_id = 520 and record_id = 7;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '687191' from ref_book_record rbr where ref_book_id = 520 and record_id = 8;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '687191' from ref_book_record rbr where ref_book_id = 520 and record_id = 8;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '147064' from ref_book_record rbr where ref_book_id = 520 and record_id = 9;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '147064' from ref_book_record rbr where ref_book_id = 520 and record_id = 9;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, 'FN 209767g' from ref_book_record rbr where ref_book_id = 520 and record_id = 10;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, 'FN 209767g' from ref_book_record rbr where ref_book_id = 520 and record_id = 10;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '217947' from ref_book_record rbr where ref_book_id = 520 and record_id = 11;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '217947' from ref_book_record rbr where ref_book_id = 520 and record_id = 11;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, 'FN 222320t' from ref_book_record rbr where ref_book_id = 520 and record_id = 12;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, 'FN 222320t' from ref_book_record rbr where ref_book_id = 520 and record_id = 12;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '398630' from ref_book_record rbr where ref_book_id = 520 and record_id = 13;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '398630' from ref_book_record rbr where ref_book_id = 520 and record_id = 13;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '382904' from ref_book_record rbr where ref_book_id = 520 and record_id = 14;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '382904' from ref_book_record rbr where ref_book_id = 520 and record_id = 14;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, 'FN 406997a' from ref_book_record rbr where ref_book_id = 520 and record_id = 15;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, 'FN 406997a' from ref_book_record rbr where ref_book_id = 520 and record_id = 15;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '697314' from ref_book_record rbr where ref_book_id = 520 and record_id = 16;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '697314' from ref_book_record rbr where ref_book_id = 520 and record_id = 16;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '409441' from ref_book_record rbr where ref_book_id = 520 and record_id = 17;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '409441' from ref_book_record rbr where ref_book_id = 520 and record_id = 17;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '388440' from ref_book_record rbr where ref_book_id = 520 and record_id = 18;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '388440' from ref_book_record rbr where ref_book_id = 520 and record_id = 18;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '330253' from ref_book_record rbr where ref_book_id = 520 and record_id = 19;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '330253' from ref_book_record rbr where ref_book_id = 520 and record_id = 19;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, 'ESBKATWW' from ref_book_record rbr where ref_book_id = 520 and record_id = 20;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, 'ESBKATWW' from ref_book_record rbr where ref_book_id = 520 and record_id = 20;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, 'DENITRIS' from ref_book_record rbr where ref_book_id = 520 and record_id = 21;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, 'DENITRIS' from ref_book_record rbr where ref_book_id = 520 and record_id = 21;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '537440' from ref_book_record rbr where ref_book_id = 520 and record_id = 22;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '537440' from ref_book_record rbr where ref_book_id = 520 and record_id = 22;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '867402' from ref_book_record rbr where ref_book_id = 520 and record_id = 23;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '867402' from ref_book_record rbr where ref_book_id = 520 and record_id = 23;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '34247573' from ref_book_record rbr where ref_book_id = 520 and record_id = 24;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '34247573' from ref_book_record rbr where ref_book_id = 520 and record_id = 24;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '01-09-951467' from ref_book_record rbr where ref_book_id = 520 and record_id = 25;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '01-09-951467' from ref_book_record rbr where ref_book_id = 520 and record_id = 25;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '372660' from ref_book_record rbr where ref_book_id = 520 and record_id = 26;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '372660' from ref_book_record rbr where ref_book_id = 520 and record_id = 26;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, 'M.S. 5402' from ref_book_record rbr where ref_book_id = 520 and record_id = 27;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, 'M.S. 5402' from ref_book_record rbr where ref_book_id = 520 and record_id = 27;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '1807576' from ref_book_record rbr where ref_book_id = 520 and record_id = 28;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '1807576' from ref_book_record rbr where ref_book_id = 520 and record_id = 28;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '320344' from ref_book_record rbr where ref_book_id = 520 and record_id = 29;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '320344' from ref_book_record rbr where ref_book_id = 520 and record_id = 29;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '160589' from ref_book_record rbr where ref_book_id = 520 and record_id = 30;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '160589' from ref_book_record rbr where ref_book_id = 520 and record_id = 30;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '01-09-968599' from ref_book_record rbr where ref_book_id = 520 and record_id = 31;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '01-09-968599' from ref_book_record rbr where ref_book_id = 520 and record_id = 31;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '12927 9006 RC 0002' from ref_book_record rbr where ref_book_id = 520 and record_id = 32;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '12927 9006 RC 0002' from ref_book_record rbr where ref_book_id = 520 and record_id = 32;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '272638' from ref_book_record rbr where ref_book_id = 520 and record_id = 33;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '272638' from ref_book_record rbr where ref_book_id = 520 and record_id = 33;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '438764' from ref_book_record rbr where ref_book_id = 520 and record_id = 34;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '438764' from ref_book_record rbr where ref_book_id = 520 and record_id = 34;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '1758537' from ref_book_record rbr where ref_book_id = 520 and record_id = 35;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '1758537' from ref_book_record rbr where ref_book_id = 520 and record_id = 35;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '251447' from ref_book_record rbr where ref_book_id = 520 and record_id = 36;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '251447' from ref_book_record rbr where ref_book_id = 520 and record_id = 36;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '1732647' from ref_book_record rbr where ref_book_id = 520 and record_id = 37;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '1732647' from ref_book_record rbr where ref_book_id = 520 and record_id = 37;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '33030763' from ref_book_record rbr where ref_book_id = 520 and record_id = 38;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '33030763' from ref_book_record rbr where ref_book_id = 520 and record_id = 38;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '33273491' from ref_book_record rbr where ref_book_id = 520 and record_id = 39;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '33273491' from ref_book_record rbr where ref_book_id = 520 and record_id = 39;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '232834' from ref_book_record rbr where ref_book_id = 520 and record_id = 40;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '232834' from ref_book_record rbr where ref_book_id = 520 and record_id = 40;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '14034376' from ref_book_record rbr where ref_book_id = 520 and record_id = 41;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '14034376' from ref_book_record rbr where ref_book_id = 520 and record_id = 41;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '01-09-959502' from ref_book_record rbr where ref_book_id = 520 and record_id = 42;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '01-09-959502' from ref_book_record rbr where ref_book_id = 520 and record_id = 42;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '300698' from ref_book_record rbr where ref_book_id = 520 and record_id = 43;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '300698' from ref_book_record rbr where ref_book_id = 520 and record_id = 43;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '263416' from ref_book_record rbr where ref_book_id = 520 and record_id = 44;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '263416' from ref_book_record rbr where ref_book_id = 520 and record_id = 44;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '590128' from ref_book_record rbr where ref_book_id = 520 and record_id = 45;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '590128' from ref_book_record rbr where ref_book_id = 520 and record_id = 45;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '68634' from ref_book_record rbr where ref_book_id = 520 and record_id = 46;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '68634' from ref_book_record rbr where ref_book_id = 520 and record_id = 46;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '126282' from ref_book_record rbr where ref_book_id = 520 and record_id = 47;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '126282' from ref_book_record rbr where ref_book_id = 520 and record_id = 47;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '326823' from ref_book_record rbr where ref_book_id = 520 and record_id = 48;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '326823' from ref_book_record rbr where ref_book_id = 520 and record_id = 48;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '892043612' from ref_book_record rbr where ref_book_id = 520 and record_id = 49;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '892043612' from ref_book_record rbr where ref_book_id = 520 and record_id = 49;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '159623' from ref_book_record rbr where ref_book_id = 520 and record_id = 50;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '159623' from ref_book_record rbr where ref_book_id = 520 and record_id = 50;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '105962' from ref_book_record rbr where ref_book_id = 520 and record_id = 51;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '105962' from ref_book_record rbr where ref_book_id = 520 and record_id = 51;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '5842093000' from ref_book_record rbr where ref_book_id = 520 and record_id = 52;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '5842093000' from ref_book_record rbr where ref_book_id = 520 and record_id = 52;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '080413774' from ref_book_record rbr where ref_book_id = 520 and record_id = 53;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '080413774' from ref_book_record rbr where ref_book_id = 520 and record_id = 53;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '35164' from ref_book_record rbr where ref_book_id = 520 and record_id = 54;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '35164' from ref_book_record rbr where ref_book_id = 520 and record_id = 54;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '72221' from ref_book_record rbr where ref_book_id = 520 and record_id = 55;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '72221' from ref_book_record rbr where ref_book_id = 520 and record_id = 55;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '108928' from ref_book_record rbr where ref_book_id = 520 and record_id = 56;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '108928' from ref_book_record rbr where ref_book_id = 520 and record_id = 56;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '218026' from ref_book_record rbr where ref_book_id = 520 and record_id = 57;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '218026' from ref_book_record rbr where ref_book_id = 520 and record_id = 57;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, 'В 161089' from ref_book_record rbr where ref_book_id = 520 and record_id = 58;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, 'В 161089' from ref_book_record rbr where ref_book_id = 520 and record_id = 58;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '288760' from ref_book_record rbr where ref_book_id = 520 and record_id = 59;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '288760' from ref_book_record rbr where ref_book_id = 520 and record_id = 59;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '485936' from ref_book_record rbr where ref_book_id = 520 and record_id = 60;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '485936' from ref_book_record rbr where ref_book_id = 520 and record_id = 60;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, 'B 165310' from ref_book_record rbr where ref_book_id = 520 and record_id = 61;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, 'B 165310' from ref_book_record rbr where ref_book_id = 520 and record_id = 61;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, 'B 171037' from ref_book_record rbr where ref_book_id = 520 and record_id = 62;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, 'B 171037' from ref_book_record rbr where ref_book_id = 520 and record_id = 62;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, 'SABRBA2B ' from ref_book_record rbr where ref_book_id = 520 and record_id = 63;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, 'SABRBA2B ' from ref_book_record rbr where ref_book_id = 520 and record_id = 63;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, 'VBCRHR22' from ref_book_record rbr where ref_book_id = 520 and record_id = 64;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, 'VBCRHR22' from ref_book_record rbr where ref_book_id = 520 and record_id = 64;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, 'SABRSI2X' from ref_book_record rbr where ref_book_id = 520 and record_id = 65;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, 'SABRSI2X' from ref_book_record rbr where ref_book_id = 520 and record_id = 65;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, 'SABRBA22' from ref_book_record rbr where ref_book_id = 520 and record_id = 66;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, 'SABRBA22' from ref_book_record rbr where ref_book_id = 520 and record_id = 66;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, 'LUBASKBX' from ref_book_record rbr where ref_book_id = 520 and record_id = 67;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, 'LUBASKBX' from ref_book_record rbr where ref_book_id = 520 and record_id = 67;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, 'SABRRSBG' from ref_book_record rbr where ref_book_id = 520 and record_id = 68;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, 'SABRRSBG' from ref_book_record rbr where ref_book_id = 520 and record_id = 68;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '4783112' from ref_book_record rbr where ref_book_id = 520 and record_id = 69;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '4783112' from ref_book_record rbr where ref_book_id = 520 and record_id = 69;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '2705815' from ref_book_record rbr where ref_book_id = 520 and record_id = 70;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '2705815' from ref_book_record rbr where ref_book_id = 520 and record_id = 70;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, 'VBOECZ2X' from ref_book_record rbr where ref_book_id = 520 and record_id = 71;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, 'VBOECZ2X' from ref_book_record rbr where ref_book_id = 520 and record_id = 71;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, 'SABRATWW' from ref_book_record rbr where ref_book_id = 520 and record_id = 72;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, 'SABRATWW' from ref_book_record rbr where ref_book_id = 520 and record_id = 72;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, 'MAVOHUHB' from ref_book_record rbr where ref_book_id = 520 and record_id = 73;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, 'MAVOHUHB' from ref_book_record rbr where ref_book_id = 520 and record_id = 73;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '293417' from ref_book_record rbr where ref_book_id = 520 and record_id = 74;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '293417' from ref_book_record rbr where ref_book_id = 520 and record_id = 74;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, 'SLBZCHZZ' from ref_book_record rbr where ref_book_id = 520 and record_id = 75;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, 'SLBZCHZZ' from ref_book_record rbr where ref_book_id = 520 and record_id = 75;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '287481' from ref_book_record rbr where ref_book_id = 520 and record_id = 76;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '287481' from ref_book_record rbr where ref_book_id = 520 and record_id = 76;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, 'B 161153' from ref_book_record rbr where ref_book_id = 520 and record_id = 77;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, 'B 161153' from ref_book_record rbr where ref_book_id = 520 and record_id = 77;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '151422' from ref_book_record rbr where ref_book_id = 520 and record_id = 78;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '151422' from ref_book_record rbr where ref_book_id = 520 and record_id = 78;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '119924' from ref_book_record rbr where ref_book_id = 520 and record_id = 79;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '119924' from ref_book_record rbr where ref_book_id = 520 and record_id = 79;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '84301' from ref_book_record rbr where ref_book_id = 520 and record_id = 80;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '84301' from ref_book_record rbr where ref_book_id = 520 and record_id = 80;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '36997-1901-ТОО ' from ref_book_record rbr where ref_book_id = 520 and record_id = 81;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '36997-1901-ТОО ' from ref_book_record rbr where ref_book_id = 520 and record_id = 81;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '20918314' from ref_book_record rbr where ref_book_id = 520 and record_id = 82;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '20918314' from ref_book_record rbr where ref_book_id = 520 and record_id = 82;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '35431' from ref_book_record rbr where ref_book_id = 520 and record_id = 83;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '35431' from ref_book_record rbr where ref_book_id = 520 and record_id = 83;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '37579' from ref_book_record rbr where ref_book_id = 520 and record_id = 84;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '37579' from ref_book_record rbr where ref_book_id = 520 and record_id = 84;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '80929' from ref_book_record rbr where ref_book_id = 520 and record_id = 85;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '80929' from ref_book_record rbr where ref_book_id = 520 and record_id = 85;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '126364' from ref_book_record rbr where ref_book_id = 520 and record_id = 86;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '126364' from ref_book_record rbr where ref_book_id = 520 and record_id = 86;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '36659' from ref_book_record rbr where ref_book_id = 520 and record_id = 87;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '36659' from ref_book_record rbr where ref_book_id = 520 and record_id = 87;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '184848' from ref_book_record rbr where ref_book_id = 520 and record_id = 88;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '184848' from ref_book_record rbr where ref_book_id = 520 and record_id = 88;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '38254' from ref_book_record rbr where ref_book_id = 520 and record_id = 89;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '38254' from ref_book_record rbr where ref_book_id = 520 and record_id = 89;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '46622' from ref_book_record rbr where ref_book_id = 520 and record_id = 90;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '46622' from ref_book_record rbr where ref_book_id = 520 and record_id = 90;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '112570' from ref_book_record rbr where ref_book_id = 520 and record_id = 91;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '112570' from ref_book_record rbr where ref_book_id = 520 and record_id = 91;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '108257C' from ref_book_record rbr where ref_book_id = 520 and record_id = 92;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '108257C' from ref_book_record rbr where ref_book_id = 520 and record_id = 92;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '01-09-924312' from ref_book_record rbr where ref_book_id = 520 and record_id = 93;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '01-09-924312' from ref_book_record rbr where ref_book_id = 520 and record_id = 93;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '01-09-706914' from ref_book_record rbr where ref_book_id = 520 and record_id = 94;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '01-09-706914' from ref_book_record rbr where ref_book_id = 520 and record_id = 94;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, 'B 160704' from ref_book_record rbr where ref_book_id = 520 and record_id = 95;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, 'B 160704' from ref_book_record rbr where ref_book_id = 520 and record_id = 95;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7723173826 / 770401001' from ref_book_record rbr where ref_book_id = 520 and record_id = 96;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7723173826' from ref_book_record rbr where ref_book_id = 520 and record_id = 96;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '1655167651 / 165501001' from ref_book_record rbr where ref_book_id = 520 and record_id = 97;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '1655167651' from ref_book_record rbr where ref_book_id = 520 and record_id = 97;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '2320067512 / 616201001' from ref_book_record rbr where ref_book_id = 520 and record_id = 98;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '2320067512' from ref_book_record rbr where ref_book_id = 520 and record_id = 98;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7728312865 / 772801001' from ref_book_record rbr where ref_book_id = 520 and record_id = 99;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7728312865' from ref_book_record rbr where ref_book_id = 520 and record_id = 99;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '38465475' from ref_book_record rbr where ref_book_id = 520 and record_id = 100;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '38465475' from ref_book_record rbr where ref_book_id = 520 and record_id = 100;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '0901050261 / 090101001' from ref_book_record rbr where ref_book_id = 520 and record_id = 101;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '0901050261' from ref_book_record rbr where ref_book_id = 520 and record_id = 101;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '79.140.00036' from ref_book_record rbr where ref_book_id = 520 and record_id = 102;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '79.140.00036' from ref_book_record rbr where ref_book_id = 520 and record_id = 102;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7736659589 / 773601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 103;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7736659589' from ref_book_record rbr where ref_book_id = 520 and record_id = 103;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '2625028532 / 262501001' from ref_book_record rbr where ref_book_id = 520 and record_id = 104;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '2625028532' from ref_book_record rbr where ref_book_id = 520 and record_id = 104;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '2625037054 / 262501001' from ref_book_record rbr where ref_book_id = 520 and record_id = 105;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '2625037054' from ref_book_record rbr where ref_book_id = 520 and record_id = 105;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7710203590 / 772201001' from ref_book_record rbr where ref_book_id = 520 and record_id = 106;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7710203590' from ref_book_record rbr where ref_book_id = 520 and record_id = 106;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '3837002418 / 383701001' from ref_book_record rbr where ref_book_id = 520 and record_id = 107;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '3837002418' from ref_book_record rbr where ref_book_id = 520 and record_id = 107;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7705289183  / 500801001' from ref_book_record rbr where ref_book_id = 520 and record_id = 108;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7705289183 ' from ref_book_record rbr where ref_book_id = 520 and record_id = 108;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '38979466' from ref_book_record rbr where ref_book_id = 520 and record_id = 109;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '38979466' from ref_book_record rbr where ref_book_id = 520 and record_id = 109;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7725757552 / 772501001' from ref_book_record rbr where ref_book_id = 520 and record_id = 110;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7725757552' from ref_book_record rbr where ref_book_id = 520 and record_id = 110;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7710337474 / 770301001' from ref_book_record rbr where ref_book_id = 520 and record_id = 111;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7710337474' from ref_book_record rbr where ref_book_id = 520 and record_id = 111;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '100625018' from ref_book_record rbr where ref_book_id = 520 and record_id = 112;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '100625018' from ref_book_record rbr where ref_book_id = 520 and record_id = 112;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, 'BPSBBY2X' from ref_book_record rbr where ref_book_id = 520 and record_id = 113;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, 'BPSBBY2X' from ref_book_record rbr where ref_book_id = 520 and record_id = 113;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7116134503 / 711601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 114;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7116134503' from ref_book_record rbr where ref_book_id = 520 and record_id = 114;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7709297379 / 770301001' from ref_book_record rbr where ref_book_id = 520 and record_id = 115;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7709297379' from ref_book_record rbr where ref_book_id = 520 and record_id = 115;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '1657193791 / 165701001' from ref_book_record rbr where ref_book_id = 520 and record_id = 116;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '1657193791' from ref_book_record rbr where ref_book_id = 520 and record_id = 116;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '1656051716 / 165601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 117;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '1656051716' from ref_book_record rbr where ref_book_id = 520 and record_id = 117;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7722337550 / 772201001' from ref_book_record rbr where ref_book_id = 520 and record_id = 118;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7722337550' from ref_book_record rbr where ref_book_id = 520 and record_id = 118;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '9103007830 / 910301001' from ref_book_record rbr where ref_book_id = 520 and record_id = 119;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '9103007830' from ref_book_record rbr where ref_book_id = 520 and record_id = 119;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7706683947 / 770701001' from ref_book_record rbr where ref_book_id = 520 and record_id = 120;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7706683947' from ref_book_record rbr where ref_book_id = 520 and record_id = 120;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '273.110.02424' from ref_book_record rbr where ref_book_id = 520 and record_id = 121;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '273.110.02424' from ref_book_record rbr where ref_book_id = 520 and record_id = 121;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '2625800607 / 262501001' from ref_book_record rbr where ref_book_id = 520 and record_id = 122;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '2625800607' from ref_book_record rbr where ref_book_id = 520 and record_id = 122;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7723392970 / 772301001' from ref_book_record rbr where ref_book_id = 520 and record_id = 123;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7723392970' from ref_book_record rbr where ref_book_id = 520 and record_id = 123;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '286.110.06031' from ref_book_record rbr where ref_book_id = 520 and record_id = 124;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '286.110.06031' from ref_book_record rbr where ref_book_id = 520 and record_id = 124;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '9103069850 / 910301001' from ref_book_record rbr where ref_book_id = 520 and record_id = 125;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '9103069850' from ref_book_record rbr where ref_book_id = 520 and record_id = 125;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '0408009440 / 041101001' from ref_book_record rbr where ref_book_id = 520 and record_id = 126;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '0408009440' from ref_book_record rbr where ref_book_id = 520 and record_id = 126;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '4633000037 / 463301001' from ref_book_record rbr where ref_book_id = 520 and record_id = 127;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '4633000037' from ref_book_record rbr where ref_book_id = 520 and record_id = 127;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '4633010853 / 463301001' from ref_book_record rbr where ref_book_id = 520 and record_id = 128;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '4633010853' from ref_book_record rbr where ref_book_id = 520 and record_id = 128;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '4633010405 / 463301001' from ref_book_record rbr where ref_book_id = 520 and record_id = 129;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '4633010405' from ref_book_record rbr where ref_book_id = 520 and record_id = 129;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '4703099079 / 470301001' from ref_book_record rbr where ref_book_id = 520 and record_id = 130;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '4703099079' from ref_book_record rbr where ref_book_id = 520 and record_id = 130;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7116146114 / 711601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 131;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7116146114' from ref_book_record rbr where ref_book_id = 520 and record_id = 131;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '4633016372 / 463301001' from ref_book_record rbr where ref_book_id = 520 and record_id = 132;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '4633016372' from ref_book_record rbr where ref_book_id = 520 and record_id = 132;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '4633021510 / 463301001' from ref_book_record rbr where ref_book_id = 520 and record_id = 133;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '4633021510' from ref_book_record rbr where ref_book_id = 520 and record_id = 133;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '3808079832 / 380801001' from ref_book_record rbr where ref_book_id = 520 and record_id = 134;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '3808079832' from ref_book_record rbr where ref_book_id = 520 and record_id = 134;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7706753665 / 770601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 135;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7706753665' from ref_book_record rbr where ref_book_id = 520 and record_id = 135;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '3460006141 / 346001001' from ref_book_record rbr where ref_book_id = 520 and record_id = 136;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '3460006141' from ref_book_record rbr where ref_book_id = 520 and record_id = 136;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '6439009628 / 091701001' from ref_book_record rbr where ref_book_id = 520 and record_id = 137;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '6439009628' from ref_book_record rbr where ref_book_id = 520 and record_id = 137;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7736641983 / 773601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 138;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7736641983' from ref_book_record rbr where ref_book_id = 520 and record_id = 138;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7705205000 / 775001001' from ref_book_record rbr where ref_book_id = 520 and record_id = 139;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7705205000' from ref_book_record rbr where ref_book_id = 520 and record_id = 139;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7727655940 / 772301001' from ref_book_record rbr where ref_book_id = 520 and record_id = 140;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7727655940' from ref_book_record rbr where ref_book_id = 520 and record_id = 140;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, 'SABRKZKA' from ref_book_record rbr where ref_book_id = 520 and record_id = 141;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, 'SABRKZKA' from ref_book_record rbr where ref_book_id = 520 and record_id = 141;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7729680004 / 772901001' from ref_book_record rbr where ref_book_id = 520 and record_id = 142;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7729680004' from ref_book_record rbr where ref_book_id = 520 and record_id = 142;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '5044092657 / 504401001' from ref_book_record rbr where ref_book_id = 520 and record_id = 143;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '5044092657' from ref_book_record rbr where ref_book_id = 520 and record_id = 143;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '4025437985 / 402501001' from ref_book_record rbr where ref_book_id = 520 and record_id = 144;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '4025437985' from ref_book_record rbr where ref_book_id = 520 and record_id = 144;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '2124039325 / 212401001' from ref_book_record rbr where ref_book_id = 520 and record_id = 145;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '2124039325' from ref_book_record rbr where ref_book_id = 520 and record_id = 145;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '1215181990 / 121501001' from ref_book_record rbr where ref_book_id = 520 and record_id = 146;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '1215181990' from ref_book_record rbr where ref_book_id = 520 and record_id = 146;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '6623104778 / 662301001' from ref_book_record rbr where ref_book_id = 520 and record_id = 147;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '6623104778' from ref_book_record rbr where ref_book_id = 520 and record_id = 147;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '4632201333 / 463201001' from ref_book_record rbr where ref_book_id = 520 and record_id = 148;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '4632201333' from ref_book_record rbr where ref_book_id = 520 and record_id = 148;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '5042097177 / 504201001' from ref_book_record rbr where ref_book_id = 520 and record_id = 149;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '5042097177' from ref_book_record rbr where ref_book_id = 520 and record_id = 149;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '1420041450 / 142001001' from ref_book_record rbr where ref_book_id = 520 and record_id = 150;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '1420041450' from ref_book_record rbr where ref_book_id = 520 and record_id = 150;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '5263023906 / 526001001' from ref_book_record rbr where ref_book_id = 520 and record_id = 151;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '5263023906' from ref_book_record rbr where ref_book_id = 520 and record_id = 151;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '2465223324 / 246501001' from ref_book_record rbr where ref_book_id = 520 and record_id = 152;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '2465223324' from ref_book_record rbr where ref_book_id = 520 and record_id = 152;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7729754908 / 772901001' from ref_book_record rbr where ref_book_id = 520 and record_id = 153;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7729754908' from ref_book_record rbr where ref_book_id = 520 and record_id = 153;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7710520840 / 771001001' from ref_book_record rbr where ref_book_id = 520 and record_id = 154;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7710520840' from ref_book_record rbr where ref_book_id = 520 and record_id = 154;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '9101001550 / 910301001' from ref_book_record rbr where ref_book_id = 520 and record_id = 155;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '9101001550' from ref_book_record rbr where ref_book_id = 520 and record_id = 155;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '35624670' from ref_book_record rbr where ref_book_id = 520 and record_id = 156;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '35624670' from ref_book_record rbr where ref_book_id = 520 and record_id = 156;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7736128605 / 773601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 157;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7736128605' from ref_book_record rbr where ref_book_id = 520 and record_id = 157;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7801392271 / 780201001' from ref_book_record rbr where ref_book_id = 520 and record_id = 158;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7801392271' from ref_book_record rbr where ref_book_id = 520 and record_id = 158;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '2320102816 / 232001001' from ref_book_record rbr where ref_book_id = 520 and record_id = 159;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '2320102816' from ref_book_record rbr where ref_book_id = 520 and record_id = 159;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '2320208509 / 232001001' from ref_book_record rbr where ref_book_id = 520 and record_id = 160;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '2320208509' from ref_book_record rbr where ref_book_id = 520 and record_id = 160;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7801378140 / 780101001' from ref_book_record rbr where ref_book_id = 520 and record_id = 161;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7801378140' from ref_book_record rbr where ref_book_id = 520 and record_id = 161;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '33053845' from ref_book_record rbr where ref_book_id = 520 and record_id = 162;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '33053845' from ref_book_record rbr where ref_book_id = 520 and record_id = 162;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '6671149955 / 667401001' from ref_book_record rbr where ref_book_id = 520 and record_id = 163;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '6671149955' from ref_book_record rbr where ref_book_id = 520 and record_id = 163;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '5031099630 / 503101001' from ref_book_record rbr where ref_book_id = 520 and record_id = 164;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '5031099630' from ref_book_record rbr where ref_book_id = 520 and record_id = 164;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7706811074 / 770601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 165;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7706811074' from ref_book_record rbr where ref_book_id = 520 and record_id = 165;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7725765426 / 772501001' from ref_book_record rbr where ref_book_id = 520 and record_id = 166;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7725765426' from ref_book_record rbr where ref_book_id = 520 and record_id = 166;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '4703085654 / 4703001001' from ref_book_record rbr where ref_book_id = 520 and record_id = 167;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '4703085654' from ref_book_record rbr where ref_book_id = 520 and record_id = 167;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '5036068121 / 5036001001' from ref_book_record rbr where ref_book_id = 520 and record_id = 168;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '5036068121' from ref_book_record rbr where ref_book_id = 520 and record_id = 168;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '3666107810 / 366601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 169;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '3666107810' from ref_book_record rbr where ref_book_id = 520 and record_id = 169;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7729276546 / 772901001' from ref_book_record rbr where ref_book_id = 520 and record_id = 170;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7729276546' from ref_book_record rbr where ref_book_id = 520 and record_id = 170;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7719593458 / 771001001' from ref_book_record rbr where ref_book_id = 520 and record_id = 171;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7719593458' from ref_book_record rbr where ref_book_id = 520 and record_id = 171;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7706808924 / 770601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 172;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7706808924' from ref_book_record rbr where ref_book_id = 520 and record_id = 172;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '2309088614 / 235601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 173;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '2309088614' from ref_book_record rbr where ref_book_id = 520 and record_id = 173;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7725352740 / 772501001' from ref_book_record rbr where ref_book_id = 520 and record_id = 174;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7725352740' from ref_book_record rbr where ref_book_id = 520 and record_id = 174;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7733129456 / 773301001' from ref_book_record rbr where ref_book_id = 520 and record_id = 175;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7733129456' from ref_book_record rbr where ref_book_id = 520 and record_id = 175;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7706811652 / 770601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 176;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7706811652' from ref_book_record rbr where ref_book_id = 520 and record_id = 176;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7750005860 / 775001001' from ref_book_record rbr where ref_book_id = 520 and record_id = 177;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7750005860' from ref_book_record rbr where ref_book_id = 520 and record_id = 177;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7750005725 / 775001001' from ref_book_record rbr where ref_book_id = 520 and record_id = 178;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7750005725' from ref_book_record rbr where ref_book_id = 520 and record_id = 178;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '6162065394 / 616201001' from ref_book_record rbr where ref_book_id = 520 and record_id = 179;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '6162065394' from ref_book_record rbr where ref_book_id = 520 and record_id = 179;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7706818400 / 770601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 180;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7706818400' from ref_book_record rbr where ref_book_id = 520 and record_id = 180;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7121500561 / 710301001' from ref_book_record rbr where ref_book_id = 520 and record_id = 181;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7121500561' from ref_book_record rbr where ref_book_id = 520 and record_id = 181;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7710561081 / 771001001' from ref_book_record rbr where ref_book_id = 520 and record_id = 182;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7710561081' from ref_book_record rbr where ref_book_id = 520 and record_id = 182;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7743765161 / 772501001' from ref_book_record rbr where ref_book_id = 520 and record_id = 183;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7743765161' from ref_book_record rbr where ref_book_id = 520 and record_id = 183;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7703287046 / 772701001' from ref_book_record rbr where ref_book_id = 520 and record_id = 184;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7703287046' from ref_book_record rbr where ref_book_id = 520 and record_id = 184;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '6317068721 / 631701001' from ref_book_record rbr where ref_book_id = 520 and record_id = 185;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '6317068721' from ref_book_record rbr where ref_book_id = 520 and record_id = 185;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '6317093990 / 631701001' from ref_book_record rbr where ref_book_id = 520 and record_id = 186;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '6317093990' from ref_book_record rbr where ref_book_id = 520 and record_id = 186;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '5032218680 / 503201001' from ref_book_record rbr where ref_book_id = 520 and record_id = 187;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '5032218680' from ref_book_record rbr where ref_book_id = 520 and record_id = 187;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7723920588 / 772301001' from ref_book_record rbr where ref_book_id = 520 and record_id = 188;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7723920588' from ref_book_record rbr where ref_book_id = 520 and record_id = 188;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '2320153930 / 232001001' from ref_book_record rbr where ref_book_id = 520 and record_id = 189;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '2320153930' from ref_book_record rbr where ref_book_id = 520 and record_id = 189;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '6317060306 / 631701001' from ref_book_record rbr where ref_book_id = 520 and record_id = 190;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '6317060306' from ref_book_record rbr where ref_book_id = 520 and record_id = 190;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '6317106960 / 631701001' from ref_book_record rbr where ref_book_id = 520 and record_id = 191;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '6317106960' from ref_book_record rbr where ref_book_id = 520 and record_id = 191;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '1655167644 / 165501001' from ref_book_record rbr where ref_book_id = 520 and record_id = 192;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '1655167644' from ref_book_record rbr where ref_book_id = 520 and record_id = 192;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '2463222526 / 770601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 193;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '2463222526' from ref_book_record rbr where ref_book_id = 520 and record_id = 193;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '5501095980 / 550101001' from ref_book_record rbr where ref_book_id = 520 and record_id = 194;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '5501095980' from ref_book_record rbr where ref_book_id = 520 and record_id = 194;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7736554890 / 773601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 195;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7736554890' from ref_book_record rbr where ref_book_id = 520 and record_id = 195;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, 'ELECUA2X' from ref_book_record rbr where ref_book_id = 520 and record_id = 196;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, 'ELECUA2X' from ref_book_record rbr where ref_book_id = 520 and record_id = 196;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, 'SABRUAUK' from ref_book_record rbr where ref_book_id = 520 and record_id = 197;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, 'SABRUAUK' from ref_book_record rbr where ref_book_id = 520 and record_id = 197;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '4345401610 / 434501001' from ref_book_record rbr where ref_book_id = 520 and record_id = 198;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '4345401610' from ref_book_record rbr where ref_book_id = 520 and record_id = 198;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '5753200537 / 575101001' from ref_book_record rbr where ref_book_id = 520 and record_id = 199;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '5753200537' from ref_book_record rbr where ref_book_id = 520 and record_id = 199;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '3123100113 / 312301001' from ref_book_record rbr where ref_book_id = 520 and record_id = 200;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '3123100113' from ref_book_record rbr where ref_book_id = 520 and record_id = 200;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '4205224136 / 420501001' from ref_book_record rbr where ref_book_id = 520 and record_id = 201;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '4205224136' from ref_book_record rbr where ref_book_id = 520 and record_id = 201;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7707179242  / 770901001' from ref_book_record rbr where ref_book_id = 520 and record_id = 202;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7707179242 ' from ref_book_record rbr where ref_book_id = 520 and record_id = 202;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7706818390 / 770601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 203;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7706818390' from ref_book_record rbr where ref_book_id = 520 and record_id = 203;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '5024093941 / 502401001' from ref_book_record rbr where ref_book_id = 520 and record_id = 204;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '5024093941' from ref_book_record rbr where ref_book_id = 520 and record_id = 204;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7726636857 / 772601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 205;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7726636857' from ref_book_record rbr where ref_book_id = 520 and record_id = 205;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '2320204590 / 232001001' from ref_book_record rbr where ref_book_id = 520 and record_id = 206;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '2320204590' from ref_book_record rbr where ref_book_id = 520 and record_id = 206;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7801036001 / 784201001' from ref_book_record rbr where ref_book_id = 520 and record_id = 207;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7801036001' from ref_book_record rbr where ref_book_id = 520 and record_id = 207;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7801579142 / 780101001' from ref_book_record rbr where ref_book_id = 520 and record_id = 208;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7801579142' from ref_book_record rbr where ref_book_id = 520 and record_id = 208;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '5008040766 / 500801001' from ref_book_record rbr where ref_book_id = 520 and record_id = 209;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '5008040766' from ref_book_record rbr where ref_book_id = 520 and record_id = 209;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7704617385 / 770401001' from ref_book_record rbr where ref_book_id = 520 and record_id = 210;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7704617385' from ref_book_record rbr where ref_book_id = 520 and record_id = 210;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '1420002690 / 142001001' from ref_book_record rbr where ref_book_id = 520 and record_id = 211;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '1420002690' from ref_book_record rbr where ref_book_id = 520 and record_id = 211;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '5008041110 / 500801001' from ref_book_record rbr where ref_book_id = 520 and record_id = 212;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '5008041110' from ref_book_record rbr where ref_book_id = 520 and record_id = 212;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7733050781 / 773301001' from ref_book_record rbr where ref_book_id = 520 and record_id = 213;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7733050781' from ref_book_record rbr where ref_book_id = 520 and record_id = 213;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '191614201' from ref_book_record rbr where ref_book_id = 520 and record_id = 214;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '191614201' from ref_book_record rbr where ref_book_id = 520 and record_id = 214;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7707308480 / 770701001' from ref_book_record rbr where ref_book_id = 520 and record_id = 215;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7707308480' from ref_book_record rbr where ref_book_id = 520 and record_id = 215;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '5032229441 / 503201001' from ref_book_record rbr where ref_book_id = 520 and record_id = 216;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '5032229441' from ref_book_record rbr where ref_book_id = 520 and record_id = 216;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7736581290 / 773601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 217;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7736581290' from ref_book_record rbr where ref_book_id = 520 and record_id = 217;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7710048970 / 775001001' from ref_book_record rbr where ref_book_id = 520 and record_id = 218;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7710048970' from ref_book_record rbr where ref_book_id = 520 and record_id = 218;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7707009586 / 509950001' from ref_book_record rbr where ref_book_id = 520 and record_id = 219;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7707009586' from ref_book_record rbr where ref_book_id = 520 and record_id = 219;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '107792-1910-ТОО ' from ref_book_record rbr where ref_book_id = 520 and record_id = 220;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '107792-1910-ТОО ' from ref_book_record rbr where ref_book_id = 520 and record_id = 220;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7604105513  / 760401001' from ref_book_record rbr where ref_book_id = 520 and record_id = 221;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7604105513 ' from ref_book_record rbr where ref_book_id = 520 and record_id = 221;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '37241931' from ref_book_record rbr where ref_book_id = 520 and record_id = 222;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '37241931' from ref_book_record rbr where ref_book_id = 520 and record_id = 222;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7736663049 / 773601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 223;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7736663049' from ref_book_record rbr where ref_book_id = 520 and record_id = 223;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7736632467 /  772601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 224;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7736632467' from ref_book_record rbr where ref_book_id = 520 and record_id = 224;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '191690785' from ref_book_record rbr where ref_book_id = 520 and record_id = 225;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '191690785' from ref_book_record rbr where ref_book_id = 520 and record_id = 225;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7710183778 / 775001001' from ref_book_record rbr where ref_book_id = 520 and record_id = 226;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7710183778' from ref_book_record rbr where ref_book_id = 520 and record_id = 226;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7802754982 / 780201001' from ref_book_record rbr where ref_book_id = 520 and record_id = 227;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7802754982' from ref_book_record rbr where ref_book_id = 520 and record_id = 227;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7736589109  / 770401001' from ref_book_record rbr where ref_book_id = 520 and record_id = 228;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7736589109 ' from ref_book_record rbr where ref_book_id = 520 and record_id = 228;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7707752230 / 770701001' from ref_book_record rbr where ref_book_id = 520 and record_id = 229;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7707752230' from ref_book_record rbr where ref_book_id = 520 and record_id = 229;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7736625205  / 773601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 230;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7736625205 ' from ref_book_record rbr where ref_book_id = 520 and record_id = 230;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '6164300420 / 616201001' from ref_book_record rbr where ref_book_id = 520 and record_id = 231;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '6164300420' from ref_book_record rbr where ref_book_id = 520 and record_id = 231;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7736611795  / 773601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 232;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7736611795 ' from ref_book_record rbr where ref_book_id = 520 and record_id = 232;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7708807475 / 770801001' from ref_book_record rbr where ref_book_id = 520 and record_id = 233;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7708807475' from ref_book_record rbr where ref_book_id = 520 and record_id = 233;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7706806959 / 770601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 234;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7706806959' from ref_book_record rbr where ref_book_id = 520 and record_id = 234;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7706806973 / 770601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 235;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7706806973' from ref_book_record rbr where ref_book_id = 520 and record_id = 235;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '5044078050  / 504401001' from ref_book_record rbr where ref_book_id = 520 and record_id = 236;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '5044078050 ' from ref_book_record rbr where ref_book_id = 520 and record_id = 236;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7736611812  / 773601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 237;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7736611812 ' from ref_book_record rbr where ref_book_id = 520 and record_id = 237;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7706811500 / 770601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 238;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7706811500' from ref_book_record rbr where ref_book_id = 520 and record_id = 238;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7736611805  / 773601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 239;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7736611805 ' from ref_book_record rbr where ref_book_id = 520 and record_id = 239;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7714355971 / 771401001' from ref_book_record rbr where ref_book_id = 520 and record_id = 240;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7714355971' from ref_book_record rbr where ref_book_id = 520 and record_id = 240;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7706805634 / 770601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 241;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7706805634' from ref_book_record rbr where ref_book_id = 520 and record_id = 241;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7706787209 / 770601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 242;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7706787209' from ref_book_record rbr where ref_book_id = 520 and record_id = 242;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7814280335 / 781401001' from ref_book_record rbr where ref_book_id = 520 and record_id = 243;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7814280335' from ref_book_record rbr where ref_book_id = 520 and record_id = 243;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7706806966 / 770601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 244;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7706806966' from ref_book_record rbr where ref_book_id = 520 and record_id = 244;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7706811525 / 770601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 245;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7706811525' from ref_book_record rbr where ref_book_id = 520 and record_id = 245;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7714355072 / 771401001' from ref_book_record rbr where ref_book_id = 520 and record_id = 246;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7714355072' from ref_book_record rbr where ref_book_id = 520 and record_id = 246;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7706804077 / 770601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 247;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7706804077' from ref_book_record rbr where ref_book_id = 520 and record_id = 247;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7706751770  / 770601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 248;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7706751770 ' from ref_book_record rbr where ref_book_id = 520 and record_id = 248;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7706751788  / 770601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 249;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7706751788 ' from ref_book_record rbr where ref_book_id = 520 and record_id = 249;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '191636450' from ref_book_record rbr where ref_book_id = 520 and record_id = 250;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '191636450' from ref_book_record rbr where ref_book_id = 520 and record_id = 250;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7707083893 / 775001001' from ref_book_record rbr where ref_book_id = 520 and record_id = 251;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7707083893' from ref_book_record rbr where ref_book_id = 520 and record_id = 251;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7703576175 / 770601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 252;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7703576175' from ref_book_record rbr where ref_book_id = 520 and record_id = 252;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7710165634 / 775001001' from ref_book_record rbr where ref_book_id = 520 and record_id = 253;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7710165634' from ref_book_record rbr where ref_book_id = 520 and record_id = 253;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7710031856 / 770301001' from ref_book_record rbr where ref_book_id = 520 and record_id = 254;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7710031856' from ref_book_record rbr where ref_book_id = 520 and record_id = 254;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '1655303086 / 1655001001' from ref_book_record rbr where ref_book_id = 520 and record_id = 255;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '1655303086' from ref_book_record rbr where ref_book_id = 520 and record_id = 255;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7714356647 / 771401001' from ref_book_record rbr where ref_book_id = 520 and record_id = 256;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7714356647' from ref_book_record rbr where ref_book_id = 520 and record_id = 256;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '5009049271 / 500901001' from ref_book_record rbr where ref_book_id = 520 and record_id = 257;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '5009049271' from ref_book_record rbr where ref_book_id = 520 and record_id = 257;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7715664852 / 771501001' from ref_book_record rbr where ref_book_id = 520 and record_id = 258;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7715664852' from ref_book_record rbr where ref_book_id = 520 and record_id = 258;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '1655146919 / 165501001' from ref_book_record rbr where ref_book_id = 520 and record_id = 259;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '1655146919' from ref_book_record rbr where ref_book_id = 520 and record_id = 259;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7708229993  / 770801001' from ref_book_record rbr where ref_book_id = 520 and record_id = 260;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7708229993 ' from ref_book_record rbr where ref_book_id = 520 and record_id = 260;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '3017064696 / 302501001' from ref_book_record rbr where ref_book_id = 520 and record_id = 261;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '3017064696' from ref_book_record rbr where ref_book_id = 520 and record_id = 261;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7736618039  /  772501001' from ref_book_record rbr where ref_book_id = 520 and record_id = 262;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7736618039 ' from ref_book_record rbr where ref_book_id = 520 and record_id = 262;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '2631054210 / 263101001' from ref_book_record rbr where ref_book_id = 520 and record_id = 263;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '2631054210' from ref_book_record rbr where ref_book_id = 520 and record_id = 263;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '2624800450 / 263101001' from ref_book_record rbr where ref_book_id = 520 and record_id = 264;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '2624800450' from ref_book_record rbr where ref_book_id = 520 and record_id = 264;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7725757369 / 772501001' from ref_book_record rbr where ref_book_id = 520 and record_id = 265;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7725757369' from ref_book_record rbr where ref_book_id = 520 and record_id = 265;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7710447491 / 770401001' from ref_book_record rbr where ref_book_id = 520 and record_id = 266;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7710447491' from ref_book_record rbr where ref_book_id = 520 and record_id = 266;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7736612855  / 770501001' from ref_book_record rbr where ref_book_id = 520 and record_id = 267;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7736612855 ' from ref_book_record rbr where ref_book_id = 520 and record_id = 267;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7706810747 / 772501001' from ref_book_record rbr where ref_book_id = 520 and record_id = 268;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7706810747' from ref_book_record rbr where ref_book_id = 520 and record_id = 268;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7744002123 / 775001001' from ref_book_record rbr where ref_book_id = 520 and record_id = 269;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7744002123' from ref_book_record rbr where ref_book_id = 520 and record_id = 269;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7706810730 / 770601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 270;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7706810730' from ref_book_record rbr where ref_book_id = 520 and record_id = 270;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '3665089491 / 366501001' from ref_book_record rbr where ref_book_id = 520 and record_id = 271;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '3665089491' from ref_book_record rbr where ref_book_id = 520 and record_id = 271;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '6148559560  / 614801001' from ref_book_record rbr where ref_book_id = 520 and record_id = 272;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '6148559560 ' from ref_book_record rbr where ref_book_id = 520 and record_id = 272;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '3519003961 / 352701001' from ref_book_record rbr where ref_book_id = 520 and record_id = 273;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '3519003961' from ref_book_record rbr where ref_book_id = 520 and record_id = 273;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '100003006' from ref_book_record rbr where ref_book_id = 520 and record_id = 274;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '100003006' from ref_book_record rbr where ref_book_id = 520 and record_id = 274;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7704659949 / 770301001' from ref_book_record rbr where ref_book_id = 520 and record_id = 275;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7704659949' from ref_book_record rbr where ref_book_id = 520 and record_id = 275;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7710891241 / 771001001' from ref_book_record rbr where ref_book_id = 520 and record_id = 276;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7710891241' from ref_book_record rbr where ref_book_id = 520 and record_id = 276;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7727718421  / 770401001' from ref_book_record rbr where ref_book_id = 520 and record_id = 277;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7727718421 ' from ref_book_record rbr where ref_book_id = 520 and record_id = 277;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '3812155469 / 380801001 ' from ref_book_record rbr where ref_book_id = 520 and record_id = 278;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '3812155469' from ref_book_record rbr where ref_book_id = 520 and record_id = 278;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '5032050910 / 463301001' from ref_book_record rbr where ref_book_id = 520 and record_id = 279;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '5032050910' from ref_book_record rbr where ref_book_id = 520 and record_id = 279;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '4633009632 / 504701001' from ref_book_record rbr where ref_book_id = 520 and record_id = 280;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '4633009632' from ref_book_record rbr where ref_book_id = 520 and record_id = 280;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7704654394 / 775001001' from ref_book_record rbr where ref_book_id = 520 and record_id = 281;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7704654394' from ref_book_record rbr where ref_book_id = 520 and record_id = 281;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7736617998  / 770401001' from ref_book_record rbr where ref_book_id = 520 and record_id = 282;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7736617998 ' from ref_book_record rbr where ref_book_id = 520 and record_id = 282;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7817310228 / 781701001' from ref_book_record rbr where ref_book_id = 520 and record_id = 283;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7817310228' from ref_book_record rbr where ref_book_id = 520 and record_id = 283;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7720239606  / 500801001' from ref_book_record rbr where ref_book_id = 520 and record_id = 284;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7720239606 ' from ref_book_record rbr where ref_book_id = 520 and record_id = 284;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '5501058233 / 550101001' from ref_book_record rbr where ref_book_id = 520 and record_id = 285;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '5501058233' from ref_book_record rbr where ref_book_id = 520 and record_id = 285;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '46215-1901-ТОО ' from ref_book_record rbr where ref_book_id = 520 and record_id = 286;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '46215-1901-ТОО ' from ref_book_record rbr where ref_book_id = 520 and record_id = 286;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7736249247 / 773601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 287;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7736249247' from ref_book_record rbr where ref_book_id = 520 and record_id = 287;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7702770003  / 772501001' from ref_book_record rbr where ref_book_id = 520 and record_id = 288;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7702770003 ' from ref_book_record rbr where ref_book_id = 520 and record_id = 288;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7706471558 / 770601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 289;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7706471558' from ref_book_record rbr where ref_book_id = 520 and record_id = 289;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7702235133  / 775001001' from ref_book_record rbr where ref_book_id = 520 and record_id = 290;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7702235133 ' from ref_book_record rbr where ref_book_id = 520 and record_id = 290;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '7736252313 / 773601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 291;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '7736252313' from ref_book_record rbr where ref_book_id = 520 and record_id = 291;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '5025023256 / 500801001' from ref_book_record rbr where ref_book_id = 520 and record_id = 292;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '5025023256' from ref_book_record rbr where ref_book_id = 520 and record_id = 292;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '6162062185 / 616201001' from ref_book_record rbr where ref_book_id = 520 and record_id = 293;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '6162062185' from ref_book_record rbr where ref_book_id = 520 and record_id = 293;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '6148560037  / 614801001' from ref_book_record rbr where ref_book_id = 520 and record_id = 294;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '6148560037 ' from ref_book_record rbr where ref_book_id = 520 and record_id = 294;

insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5217, '2356014828 / 235601001' from ref_book_record rbr where ref_book_id = 520 and record_id = 295;
	insert into ref_book_value (record_id, attribute_id, string_value) select rbr.id, 5218, '2356014828' from ref_book_record rbr where ref_book_id = 520 and record_id = 295;
	
update ref_book_value set string_value = trim(string_value) where attribute_id in (select id from ref_book_attribute where ref_book_id = 520) and string_value <> trim(string_value);
--контрольный выстрел
update ref_book_record set version = to_date('01.01.2015', 'DD.MM.YYYY') where ref_book_id = 520 and record_id between 1 and 295;

COMMIT;
EXIT;
	