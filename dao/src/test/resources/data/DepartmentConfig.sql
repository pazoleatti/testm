INSERT INTO ref_book_oktmo(id, code, name, version, status, record_id) VALUES (1,'111','октмо1',to_date('01.09.2014', 'DD.MM.YYYY'),1,1);
INSERT INTO ref_book_oktmo(id, code, name, version, status, record_id) VALUES (2,'222','октмо2',to_date('01.09.2014', 'DD.MM.YYYY'),1,2);

insert into REF_BOOK_NDFL_DETAIL(id,record_id,version,status,department_id, kpp, oktmo) values
(1,1,to_date('01.01.2016', 'DD.MM.YYYY'),0,2, '000000001', 1),
(2,2,to_date('01.01.2016', 'DD.MM.YYYY'),0,2, '000000002', 1),
(3,3,to_date('01.01.2016', 'DD.MM.YYYY'),0,2, '010000003', 1),
(4,4,to_date('01.01.2016', 'DD.MM.YYYY'),0,3, '000000004', 2),
(5,5,to_date('01.01.2016', 'DD.MM.YYYY'),0,3, '000000005', 2),
(6,6,to_date('01.01.2016', 'DD.MM.YYYY'),0,7, '000000006', 2);