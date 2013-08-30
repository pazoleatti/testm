insert into DEPARTMENT (id, name, parent_id, type, shortname, tb_index, sbrf_code) values (1, 'Банк', null, 1, null, null, '12');
insert into DEPARTMENT (id, name, parent_id, type, shortname, tb_index, sbrf_code) values (2, 'ТБ1', 1, 2, null, null, '23');
insert into DEPARTMENT (id, name, parent_id, type, shortname, tb_index, sbrf_code) values (3, 'ТБ2', 1, 2, null, null, null);

insert into ref_book(id, name) values (8, 'Коды, определяющие налоговый (отчётный) период');
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (21, 1, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);
insert into ref_book_record(id, record_id, ref_book_id, version, status) values (22, 2, 8, to_date('01.01.2013', 'DD.MM.YY'), 0);
