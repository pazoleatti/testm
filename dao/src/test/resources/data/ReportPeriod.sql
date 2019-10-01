INSERT INTO report_period_type (id,code,name,start_date,end_date,calendar_start_date) VALUES (21, '21', 'первый квартал', DATE '1970-01-01', DATE '1970-03-31', DATE '1970-01-01');
INSERT INTO report_period_type (id,code,name,start_date,end_date,calendar_start_date) VALUES (22, '22', 'второй квартал', DATE '1970-01-01', DATE '1970-06-30', DATE '1970-04-01');
INSERT INTO report_period_type (id,code,name,start_date,end_date,calendar_start_date) VALUES (23, '23', 'третий квартал', DATE '1970-01-01', DATE '1970-09-30', DATE '1970-07-01');
INSERT INTO report_period_type (id,code,name,start_date,end_date,calendar_start_date) VALUES (24, '24', 'год', DATE '1970-01-01', DATE '1970-12-31', DATE '1970-10-01');

INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date, form_type_id) VALUES (1, 'первый квартал',  1, 21, date '2013-01-01', date '2013-03-31', date '2013-01-01', 5);
INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date, form_type_id) VALUES (2, 'полугодие',  1, 22, date '2013-04-01', date '2013-06-30', date '2013-04-01', 5);
INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date, form_type_id) VALUES (3, 'год', 21, 21, date '2013-01-01', date '2013-03-31', date '2013-01-01', 5);
INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date, form_type_id) VALUES (4, 'Income report period 1', 31, 21, date '2014-01-01', date '2014-03-31', date '2014-01-01', 5);

INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date, form_type_id) VALUES (11, 'первый квартал', 10, 21, date '2013-01-01', date '2013-03-31', date '2013-01-01', 5);
INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date, form_type_id) VALUES (12, 'первый квартал', 10, 22, date '2013-04-01', date '2013-06-30', date '2013-04-01', 5);
INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date, form_type_id) VALUES (13, 'Transport report period 13', 10, 23, date '2013-07-01', date '2013-09-30', date '2013-07-01', 5);
INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date, form_type_id) VALUES (14, 'Deal report period 14', 12, 22, date '2013-01-01', date '2013-03-31', date '2013-01-01', 5);
INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date, form_type_id) VALUES (15, 'Deal report period 15', 12, 23, date '2013-04-01', date '2013-06-30', date '2013-04-01', 5);

INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date, form_type_id) VALUES (20, 'первый квартал', 100, 21, date '2014-01-01', date '2014-03-31', date '2014-01-01', 5);
INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date, form_type_id) VALUES (21, 'полугодие', 100, 22, date '2014-04-01', date '2014-06-30', date '2014-04-01', 5);
INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date, form_type_id) VALUES (22, '9 месяцев', 100, 23, date '2014-07-01', date '2014-09-30', date '2014-07-01', 5);

INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date, form_type_id) VALUES (100, 'первый квартал', 1000, 21, date '2016-01-01', date '2016-03-31', date '2014-01-01', 5);
INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date, form_type_id) VALUES (110, 'полугодие', 1000, 22, date '2016-01-01', date '2016-06-30', date '2016-04-01', 5);
INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date, form_type_id) VALUES (120, '9 месяцев', 1000, 23, date '2016-01-01', date '2016-09-30', date '2016-07-01', 5);
INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date, form_type_id) VALUES (130, 'год', 1000, 24, date '2016-01-01', date '2016-12-31', date '2016-10-01', 5);
INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date, form_type_id) VALUES (200, 'первый квартал', 1010, 21, date '2017-01-01', date '2017-03-31', date '2017-01-01', 5);
INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date, form_type_id) VALUES (210, 'полугодие', 1010, 22, date '2017-01-01', date '2017-06-30', date '2017-04-01', 5);
INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date, form_type_id) VALUES (220, '9 месяцев', 1010, 23, date '2017-01-01', date '2017-09-30', date '2017-07-01', 5);
INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date, form_type_id) VALUES (230, 'год', 1010, 24, date '2017-01-01', date '2017-12-31', date '2017-10-01', 5);
INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date, form_type_id) VALUES (300, 'первый квартал', 1020, 21, date '2018-01-01', date '2018-03-31', date '2018-01-01', 5);
INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date, form_type_id) VALUES (310, 'полугодие', 1020, 22, date '2018-01-01', date '2018-06-30', date '2018-04-01', 5);
INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date, form_type_id) VALUES (320, '9 месяцев', 1020, 23, date '2018-01-01', date '2018-09-30', date '2018-07-01', 5);
INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date, form_type_id) VALUES (330, 'год', 1020, 24, date '2018-01-01', date '2018-12-31', date '2018-10-01', 5);

