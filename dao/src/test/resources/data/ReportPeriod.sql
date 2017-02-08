INSERT INTO report_period_type (id,code,name,start_date,end_date,calendar_start_date) VALUES (21, '21', 'первый квартал', DATE '1970-01-01', DATE '1970-03-31', DATE '1970-01-11');
INSERT INTO report_period_type (id,code,name,start_date,end_date,calendar_start_date) VALUES (22, '22', 'второй квартал', DATE '1970-04-01', DATE '1970-06-30', DATE '1970-04-04');
INSERT INTO report_period_type (id,code,name,start_date,end_date,calendar_start_date) VALUES (23, '23', 'третий квартал', DATE '1970-07-01', DATE '1970-09-30', DATE '1970-07-07');

INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) VALUES (1, 'первый квартал',  1, 21, date '2013-01-01', date '2013-03-31', date '2013-01-01');
INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) VALUES (2, 'полугодие',  1, 22, date '2013-04-01', date '2013-06-30', date '2013-04-01');
INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) VALUES (3, 'год', 21, 21, date '2013-01-01', date '2013-03-31', date '2013-01-01');
INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) VALUES (4, 'Income report period 1', 31, 21, date '2014-01-01', date '2014-03-31', date '2014-01-01');

INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) VALUES (11, 'первый квартал', 10, 21, date '2013-01-01', date '2013-03-31', date '2013-01-01');
INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) VALUES (12, 'первый квартал', 10, 22, date '2013-04-01', date '2013-06-30', date '2013-04-01');
INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) VALUES (13, 'Transport report period 13', 10, 23, date '2013-07-01', date '2013-09-30', date '2013-07-01');
INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) VALUES (14, 'Deal report period 14', 12, 22, date '2013-01-01', date '2013-03-31', date '2013-01-01');
INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) VALUES (15, 'Deal report period 15', 12, 23, date '2013-04-01', date '2013-06-30', date '2013-04-01');

INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) VALUES (20, 'первый квартал', 100, 21, date '2014-01-01', date '2014-03-31', date '2014-01-01');
INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) VALUES (21, 'полугодие', 100, 22, date '2014-04-01', date '2014-06-30', date '2014-04-01');
INSERT INTO REPORT_PERIOD (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date) VALUES (22, '9 месяцев', 100, 23, date '2014-07-01', date '2014-09-30', date '2014-07-01');
