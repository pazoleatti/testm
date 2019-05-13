INSERT INTO ref_book_oktmo(id, code, name, version, status, record_id) VALUES (1,'111','октмо1',to_date('01.09.2014', 'DD.MM.YYYY'),1,1);
INSERT INTO ref_book_oktmo(id, code, name, version, status, record_id) VALUES (2,'222','октмо2',to_date('01.09.2014', 'DD.MM.YYYY'),1,2);

insert into ref_book_present_place(id, record_id, version, code, name) values (1, 1, to_date('01.09.2014', 'DD.MM.YYYY'), '1', 'present place');

insert into ref_book_signatory_mark(id, record_id, version, code, name) values (1, 1, to_date('01.09.2014', 'DD.MM.YYYY'), '1', 'signatory mark');

insert into department_config(id, kpp, oktmo_id, start_date, end_date, department_id, tax_organ_code, present_place_id, signatory_id) values
(10,  '000000001', 1, to_date('01.01.2016', 'DD.MM.YYYY'), to_date('31.12.2016', 'DD.MM.YYYY'), 2, '0001', 1, 1),

(11,  '000000002', 1, to_date('01.01.2016', 'DD.MM.YYYY'), to_date('31.12.2016', 'DD.MM.YYYY'), 2, '0002', 1, 1),
(111, '000000002', 1, to_date('01.01.2017', 'DD.MM.YYYY'), to_date('31.12.2017', 'DD.MM.YYYY'), 2, '0003', 1, 1),

(12,  '000000003', 1, to_date('01.01.2016', 'DD.MM.YYYY'), to_date('31.12.2017', 'DD.MM.YYYY'), 2, '0004', 1, 1),
(122, '000000003', 1, to_date('01.01.2018', 'DD.MM.YYYY'), null,                                2, '0005', 1, 1),

(13,  '000000004', 1, to_date('01.01.2016', 'DD.MM.YYYY'), to_date('31.12.2016', 'DD.MM.YYYY'), 2, '0006', 1, 1),
(131, '000000004', 1, to_date('01.01.2017', 'DD.MM.YYYY'), null,                                2, '0007', 1, 1),

(141, '000000005', 1, to_date('01.01.2017', 'DD.MM.YYYY'), null,                                2, '0008', 1, 1),

(15,  '000000006', 1, to_date('01.01.2016', 'DD.MM.YYYY'), to_date('31.12.2017', 'DD.MM.YYYY'), 2, '0009', 1, 1),
(151, '000000006', 1, to_date('01.01.2018', 'DD.MM.YYYY'), null,                                3, '0010', 1, 1),

(16,  '000000010', 2, to_date('01.01.2016', 'DD.MM.YYYY'), null,                                3, '0011', 1, 1),

(17,  '000000011', 2, to_date('01.01.2016', 'DD.MM.YYYY'), null,                                3, '0012', 1, 1),

(18,  '000000012', 2, to_date('01.01.2016', 'DD.MM.YYYY'), null,                                7, '0013', 1, 1);