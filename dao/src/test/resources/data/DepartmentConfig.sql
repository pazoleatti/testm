INSERT INTO ref_book_oktmo(id, code, name, version, status, record_id) VALUES (1,'111','октмо1',to_date('01.09.2014', 'DD.MM.YYYY'),1,1);
INSERT INTO ref_book_oktmo(id, code, name, version, status, record_id) VALUES (2,'222','октмо2',to_date('01.09.2014', 'DD.MM.YYYY'),1,2);

insert into REF_BOOK_NDFL_DETAIL(id,record_id,version,status,department_id, kpp, oktmo, tax_organ_code) values
(10,  1, to_date('01.01.2016', 'DD.MM.YYYY'), 0, 2, '000000001', 1, '0001'),
(101, 1, to_date('01.01.2017', 'DD.MM.YYYY'), 2, 2, null, null, null),

(11,  2, to_date('01.01.2016', 'DD.MM.YYYY'), 0, 2, '000000002', 1, '0002'),
(111, 2, to_date('01.01.2017', 'DD.MM.YYYY'), 0, 2, '000000002', 1, '0003'),
(112, 2, to_date('01.01.2018', 'DD.MM.YYYY'), 2, 2, null, null, null),

(12,  3, to_date('01.01.2016', 'DD.MM.YYYY'), 0, 2, '000000003', 1, '0004'),
(121, 3, to_date('01.01.2017', 'DD.MM.YYYY'), 2, 2, null, null, null),
(122, 3, to_date('01.01.2018', 'DD.MM.YYYY'), 0, 2, '000000003', 1, '0005'),

(13,  4, to_date('01.01.2016', 'DD.MM.YYYY'), 0, 2, '000000004', 1, '0006'),
(131, 4, to_date('01.01.2017', 'DD.MM.YYYY'), 0, 2, '000000004', 1, '0007'),

(141, 5, to_date('01.01.2017', 'DD.MM.YYYY'), 0, 2, '000000005', 1, '0008'),

(15,  6, to_date('01.01.2016', 'DD.MM.YYYY'), 0, 2, '000000006', 1, '0009'),
(151, 6, to_date('01.01.2017', 'DD.MM.YYYY'), 0, 3, '000000006', 1, '0010'),

(16,  7, to_date('01.01.2016', 'DD.MM.YYYY'), 0, 3, '000000010', 2, '0011'),

(17,  8, to_date('01.01.2016', 'DD.MM.YYYY'), 0, 3, '000000011', 2, '0012'),

(18,  9, to_date('01.01.2016', 'DD.MM.YYYY'), 0, 7, '000000012', 2, '0013');