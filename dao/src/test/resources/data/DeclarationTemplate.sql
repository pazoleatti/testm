insert into declaration_template(id, edition, name, version, is_active, jrxml, declaration_type_id, status)
  values (1, 1, 'Декларация 1', to_date('01.01.2013 12.01.01', 'DD.MM.YY HH.MI.SS') , 0, 'test-jrxml', 1, 0);
insert into declaration_template(id, edition, name, version, is_active, jrxml, declaration_type_id, status)
  values (2, 2, 'Декларация 2', date '2014-01-01', 1, 'test-jrxml', 1, 1);
insert into declaration_template(id, edition, name, version, is_active, jrxml, declaration_type_id, status)
  values (3, 3, 'Декларация 3', date '2015-01-01', 1, 'test-jrxml', 2, 1);
insert into declaration_template(id, edition, name, version, is_active, jrxml, declaration_type_id, status)
  values (4, 1, 'Декларация 4', date '2016-01-01', 1, 'test-jrxml', 2, 0);
insert into declaration_template(id, edition, name, version, is_active, jrxml, declaration_type_id, status)
  values (5, 1, 'Декларация 5', date '2017-01-01', 0, 'test-jrxml', 3, 0);
insert into declaration_template(id, edition, name, version, is_active, jrxml, declaration_type_id, status)
  values (6, 1, 'Декларация 6', date '2017-12-31', 0, 'test-jrxml', 3, 2);
