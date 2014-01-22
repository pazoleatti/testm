insert into declaration_template(id, edition, version, is_active, jrxml, declaration_type_id, status)
  values (1, 1, to_date('01.01.2013 11.03.57', 'DD.MM.YYYY HH.MM.SS') , 0, 'test-jrxml', 1, 0);
insert into declaration_template(id, edition, version, is_active, jrxml, declaration_type_id, status)
  values (2, 2, date '2013-01-01', 1, 'test-jrxml', 1, 1);
insert into declaration_template(id, edition, version, is_active, jrxml, declaration_type_id, status)
  values (3, 3, date '2013-01-01', 1, 'test-jrxml', 2, 1);
insert into declaration_template(id, edition, version, is_active, jrxml, declaration_type_id) values (4, 1, date '2013-01-01', 1, 'test-jrxml', 2);
insert into declaration_template(id, edition, version, is_active, jrxml, declaration_type_id) values (5, 1, date '2013-01-01', 0, 'test-jrxml', 3);
insert into declaration_template(id, edition, version, is_active, jrxml, declaration_type_id) values (6, 1, date '2013-01-01', 0, 'test-jrxml', 3);
