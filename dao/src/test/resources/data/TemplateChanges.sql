insert into tax_period(id, tax_type, year) values (1, 'T', 2013);
insert into tax_period(id, tax_type, year) values (11, 'T', 2012);
insert into tax_period(id, tax_type, year) values (21, 'V', 2013);

INSERT INTO report_period_type (id,code,name,start_date,end_date,calendar_start_date) VALUES (21, '21', 'первый квартал', DATE '1970-01-01', DATE '1970-03-31', DATE '1970-01-11');
INSERT INTO report_period_type (id,code,name,start_date,end_date,calendar_start_date) VALUES (22, '22', 'второй квартал', DATE '1970-04-01', DATE '1970-06-30', DATE '1970-04-04');
INSERT INTO report_period_type (id,code,name,start_date,end_date,calendar_start_date) VALUES (23, '23', 'третий квартал', DATE '1970-07-01', DATE '1970-09-30', DATE '1970-07-07');

insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) values (1, 'Transport report period 1',  1, 21, date '2013-01-01', date '2013-03-31', date '2013-01-01');
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) values (2, 'Transport report period 2',  1, 22, date '2013-04-01', date '2013-06-30', date '2013-04-01');
insert into report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) values (3, 'VAT report period 1'      , 21, 21, date '2013-01-01', date '2013-03-31', date '2013-01-01');

insert into declaration_type (id, name, tax_type, status) values (1, 'Вид налоговой формы 1', 'T', 1);
insert into declaration_type (id, name, tax_type) values (2, 'Вид налоговой формы 2', 'T');
insert into declaration_type (id, name, tax_type) values (3, 'Вид налоговой формы 3', 'V');

insert into declaration_template(id, name, version, jrxml, declaration_type_id, status, form_kind, form_type)
  values (1, 'Налоговая форма 1', date '2013-01-01', null, 1, 0, 3, 1);
insert into declaration_template(id, name, version, jrxml, declaration_type_id, status, form_kind, form_type)
  values (2, 'Налоговая форма 2', date '2013-05-01', null, 1, 1, 3, 1);
insert into declaration_template(id, name, version, jrxml, declaration_type_id, status, form_kind, form_type)
  values (3, 'Налоговая форма 3', date '2013-01-01', null, 2, 1, 3, 1);

insert into form_template (id, type_id, data_rows, version, fixed_rows, name, fullname, header, status)
  values (1, 1, null, to_date('01.01.2013 12.01.01', 'DD.MM.YY HH.MI.SS'), 1, 'name_1', 'fullname_1', 'header_1', 0);
insert into form_template (id, type_id, data_rows, version, fixed_rows, name, fullname, header, status)
  values (2, 2, null, date '2013-01-01', 0, 'name_2', 'fullname_2', 'header_2', 0);

insert into template_changes(id, event, date_event, author, form_template_id, declaration_template_id)
  values (1, 701, date '2013-01-01', 1, 1, null);
insert into template_changes(id, event, date_event, author, form_template_id, declaration_template_id)
  values (2, 701, date '2013-01-01', 1, null , 1);
insert into template_changes(id, event, date_event, author, form_template_id, declaration_template_id)
  values (3, 701, date '2013-01-01', 1, null , 2);