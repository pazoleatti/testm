INSERT INTO tax_type (id, name) VALUES ('N', 'НДФЛ');
insert into tax_period (id, tax_type, year) values (1, 'N', 2016);
insert into tax_period (id, tax_type, year) values (2, 'N', 2017);

INSERT INTO report_period_type (id, code, name, start_date, end_date, calendar_start_date)
VALUES (21, '21', 'первый квартал', DATE '1970-01-01', DATE '1970-03-31', DATE '1970-01-01');

INSERT INTO REPORT_PERIOD(id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
VALUES (1, 'первый квартал', 1, 21, date '2016-01-01', date '2016-03-31', date '2016-01-01');
INSERT INTO REPORT_PERIOD(id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date)
VALUES (2, 'первый квартал', 2, 21, date '2017-01-01', date '2017-03-31', date '2017-01-01');

INSERT INTO state (id, name) VALUES (1, 'Создана');
insert into department_report_period(id, department_id, report_period_id, is_active) values (2, 2, 2, 1);
insert into declaration_type(id, name) values (1, 'type');
insert into declaration_template(id, name, version, declaration_type_id, status) values (1, 'template', date '2013-01-01', 1, 2);
insert into declaration_data(id, declaration_template_id, department_report_period_id, created_by) values (1, 1, 2, 1);

insert into ndfl_person(id, declaration_data_id) values(1, 1);
insert into ndfl_person_income(id, ndfl_person_id, kpp, oktmo) values(1, 1, '000000001', '111');
insert into ndfl_person_income(id, ndfl_person_id, kpp, oktmo) values(2, 1, '000000002', '111');
insert into ndfl_person_income(id, ndfl_person_id, kpp, oktmo) values(3, 1, '000000003', '111');
insert into ndfl_person_income(id, ndfl_person_id, kpp, oktmo) values(4, 1, '000000004', '111');
insert into ndfl_person_income(id, ndfl_person_id, kpp, oktmo) values(5, 1, '000000006', '111');