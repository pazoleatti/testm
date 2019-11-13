SET DATABASE REFERENTIAL INTEGRITY FALSE;

create global temporary table tmp_cons_data(
				operation_id varchar2(100 char),
				asnu_id number(18),
				inp varchar2(25 char),
				year number(4),
				period_code varchar2(2 char),
				correction_date date)
			on commit delete rows;

insert into declaration_template(id, name, version, jrxml, declaration_type_id, status, form_kind, form_type)
values (100, 'РНУ НДФЛ (первичная)', date '2016-01-01', null, 100, 0, '1', 2);

insert into department_report_period(id, department_id, report_period_id, is_active) values (1000, 7, 100, 1),
(1010, 7, 110, 1),
(1020, 7, 120, 1),
(1030, 7, 130, 1),
(1100, 7, 200, 1),
(1110, 7, 210, 1),
(1120, 7, 220, 1),
(1130, 7, 230, 1),
(1200, 7, 300, 1),
(1210, 7, 310, 1),
(1220, 7, 320, 1),
(1230, 7, 330, 1),

(2000, 2, 100, 1),
(2010, 2, 110, 1),
(2020, 2, 120, 1),
(2030, 2, 130, 1),
(2100, 2, 200, 1),
(2110, 2, 210, 1),
(21120, 2, 220, 1),
(2130, 2, 230, 1),
(2200, 2, 300, 1),
(2210, 2, 310, 1),
(2220, 2, 320, 1),
(2230, 2, 330, 1);

insert into declaration_data(id, declaration_template_id, department_report_period_id, state, created_by, tax_organ_code, kpp, note, asnu_id) values (1, 1, 102, 3, 1, 'CD12', '123456789', 'Первичка по', 8);
insert into declaration_data(id, declaration_template_id, department_report_period_id, state, created_by, asnu_id) values (2, 1, 204, 1, 1, 8);
insert into declaration_data(id, declaration_template_id, department_report_period_id, state, created_by, asnu_id) values (3, 1, 303, 3, 1, 8);
insert into declaration_data(id, declaration_template_id, department_report_period_id, state, created_by, asnu_id) values (4, 1, 401, 1, 1, 8);
insert into declaration_data(id, declaration_template_id, department_report_period_id, state, created_by, asnu_id) values (5, 1, 501, 3, 1, 8);
insert into declaration_data(id, declaration_template_id, department_report_period_id, state, created_by, asnu_id) values (7, 1, 605, 3, 1, 8);

insert into declaration_data(id, declaration_template_id, department_report_period_id, state, created_by) values (100, 100, 1010, 3, 1);

insert into declaration_data(id, declaration_template_id, department_report_period_id, state, created_by, asnu_id) values (20161, 100, 1000, 3, 1, 8);
insert into declaration_data(id, declaration_template_id, department_report_period_id, state, created_by, asnu_id) values (20162, 100, 1010, 3, 1, 8);
insert into declaration_data(id, declaration_template_id, department_report_period_id, state, created_by, asnu_id) values (20163, 100, 1020, 3, 1, 8);
insert into declaration_data(id, declaration_template_id, department_report_period_id, state, created_by, asnu_id) values (201631, 101, 1020, 3, 1, 8);
insert into declaration_data(id, declaration_template_id, department_report_period_id, state, created_by, asnu_id) values (20164, 100, 1030, 3, 1, 8);

insert into declaration_data(id, declaration_template_id, department_report_period_id, state, created_by, asnu_id) values (20171, 100, 1000, 3, 1, 8);
insert into declaration_data(id, declaration_template_id, department_report_period_id, state, created_by, asnu_id) values (20172, 100, 1010, 3, 1, 8);
insert into declaration_data(id, declaration_template_id, department_report_period_id, state, created_by, asnu_id) values (20173, 100, 1020, 3, 1, 8);
insert into declaration_data(id, declaration_template_id, department_report_period_id, state, created_by, asnu_id) values (20174, 100, 1030, 3, 1, 8);

insert into declaration_data(id, declaration_template_id, department_report_period_id, state, created_by, asnu_id) values (20181, 100, 1000, 3, 1, 8);
insert into declaration_data(id, declaration_template_id, department_report_period_id, state, created_by, asnu_id) values (20182, 100, 1010, 3, 1, 8);

insert into declaration_data(id, declaration_template_id, department_report_period_id, state, created_by, asnu_id) values (50000, 100, 1010, 3, 1, 8);

insert into NDFL_PERSON (id, declaration_data_id, inp, FIRST_NAME, LAST_NAME, BIRTH_DAY, CITIZENSHIP, ID_DOC_TYPE, ID_DOC_NUMBER, STATUS, PERSON_ID, ASNU_ID) values
(101, 1, '100500', 'Ivan', 'Ivanov', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '001', '11', '0000', '1', 1, 8),
(102, 1, 1234567890, 'Ivan', 'Ivanov', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123', '22', '0000', '2',12122, 8),
(103, 1, 1234567890, 'ФЕДОР', 'ИВАНОВ', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123', '22', '0000', '2',15155, 8);

Insert into NDFL_PERSON_INCOME (ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) values
('1036','101',null,null,'1','0000','00','111222333','2222',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-10-2005','DD-MM-YYYY'),'100','200','2','0','13',to_date('01-11-2010','DD-MM-YYYY'),'1','10','20','0','30',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-11-2010','DD-MM-YYYY'),'0','0', 8),
('1037','101',null,null,'2','1010','00','111222333','2222',to_date('02-10-2005','DD-MM-YYYY'),to_date('02-10-2005','DD-MM-YYYY'),'101','201','3','1','13',to_date('01-11-2010','DD-MM-YYYY'),'2','11','21','1','31',to_date('02-10-2005','DD-MM-YYYY'),to_date('02-11-2010','DD-MM-YYYY'),'0','1', 8),
('1038','102',null,null,'3','1010','00','111222333','2222',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-10-2005','DD-MM-YYYY'),'100','200','2','0','9',to_date('01-11-2010','DD-MM-YYYY'),'1','10','20','0',null,to_date('01-10-2005','DD-MM-YYYY'),to_date('01-11-2011','DD-MM-YYYY'),'0','0', 8);

insert INTO ndfl_person_deduction (id, ndfl_person_id, source_id, row_num, operation_id, type_code, notif_type, notif_date, notif_num, notif_source, notif_summ, income_accrued, income_code, income_summ, period_prev_date, period_prev_summ, period_curr_date, period_curr_summ, ASNU_ID) VALUES
(1, 101, NULL, null, '1', '100', '01', to_date('30-10-2005','DD-MM-YYYY'), '00001', '110001', 2, to_date('01-10-2005','DD-MM-YYYY'), '0000', 100, to_date('30-10-2005','DD-MM-YYYY'), null, to_date('31-10-2005','DD-MM-YYYY'), null, 8),
(2, 101, NULL, null, '2', '100', '02', to_date('31-10-2005','DD-MM-YYYY'), '00002', '110002', 3, to_date('01-10-2005','DD-MM-YYYY'), '0000', 101, to_date('31-10-2005','DD-MM-YYYY'), null, to_date('31-10-2005','DD-MM-YYYY'), null, 8);

insert into ndfl_person_prepayment (id, ndfl_person_id, source_id, row_num, operation_id, summ, notif_num, notif_date, notif_source, ASNU_ID) VALUES
(1, 101, null, null, '1', 100, '1', to_date('01-10-2005','DD-MM-YYYY'), '01', 8),
(2, 101, null, null, '2', 101, '1', to_date('01-10-2005','DD-MM-YYYY'), '01', 8);

--Test UpdatePersonRefBookReferences
insert into NDFL_PERSON (id, declaration_data_id, inp, snils, FIRST_NAME, LAST_NAME, MIDDLE_NAME, BIRTH_DAY, CITIZENSHIP, INN_NP, INN_FOREIGN, ID_DOC_TYPE, ID_DOC_NUMBER, STATUS, ROW_NUM, REGION_CODE, POST_INDEX, AREA, CITY, LOCALITY, STREET, HOUSE, BUILDING, FLAT, PERSON_ID, ASNU_ID) values
(201, 2, '100500', '123456760', 'IVAN', 'IVANOV', 'ПЕТРОВИЧ', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '001', '123456780', '123456770', '11', '0000', '1', 1, '01', '123456', 'Область1', 'Город1', 'Район1', 'Улица1', '1', '2', '3', 2, 8),
(202, 2, 1234567890, '123456760', 'IVAN', 'IVANOV', 'ПЕТРОВИЧ', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123', '123456780', '123456770', '22', '0000', '2', 2, '02', '923456', 'Область2', 'Город2', 'Район3', 'Улица2', '2', '3', '4', 3, 8),
(203, 2, 1234567890, '123456760', 'Федор', 'ИВАНОВ', 'Петрович', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123','123456780', '123456770', '22', '0000', '2', 3, '02', '923456', 'Область2', 'Город2', 'Район3', 'Улица3', '2', '3', '4', 4, 8),
(204, 2, 1234567890, '123456760', 'Федор', 'ИВАНОВ', 'Петрович', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123','123456780', '123456770', '22', '0000', '2', 4, '02', '923456', 'Область2', 'Город2', 'Район3', 'Улица4', '2', '3', '4', 4, 8),
(205, 2, 1234567890, '123456760', 'Федор', 'ИВАНОВ', 'Петрович', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123','123456780', '123456770', '22', '0000', '2', 5, '02', '923456', 'Область2', 'Город2', 'Район3', 'Улица4', '2', '3', '4', 4, 8),
(206, 2, 1234567890, '123456760', 'Федор', 'ИВАНОВ', 'Петрович', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123','123456780', '123456770', '22', '0000', '2', 6, '02', '923456', 'Область2', 'Город2', 'Район3', 'Улица4', '2', '3', '4', 4, 8),
(207, 2, 1234567890, '123456760', 'Федор', 'ИВАНОВ', 'Петрович', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123','123456780', '123456770', '22', '0000', '2', 7, '02', '923456', 'Область2', 'Город2', 'Район3', 'Улица4', '2', '3', '4', 4, 8),
(208, 2, 1234567890, '123456761', 'Федор', 'ИВАНОВ', 'Петрович', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123','123456780', '123456770', '22', '0000', '2', 87, '02', '923456', 'Область2', 'Город2', 'Район3', 'Улица4', '2', '3', '4', 4, 8),
(209, 2, 1234567890, '123456761', 'Федор', 'ИВАНОВ', 'Петрович', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123','123456780', '123456771', '22', '0000', '2', 21, '02', '923456', 'Область2', 'Город2', 'Район3', 'Улица4', '2', '3', '4', 4, 8),
(210, 2, 1234567890, '123456761', 'Федор', 'ИВАНОВ', 'Петрович', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123','123456781', '123456771', '22', '0000', '2', 44, '02', '923456', 'Область2', 'Город2', 'Район3', 'Улица4', '2', '3', '4', 4, 8);

insert into NDFL_PERSON (id, declaration_data_id) values
(301, 20161),
(302, 20162),
(303, 20163),
  /* ФЛ Из КНФ*/
(3031, 201631),
(304, 20164),
(305, 20171),
(306, 20172),
(307, 20173),
(308, 20174),
(309, 20181);

/* КПП/ОКТМО другого ТБ 1-й кв 2016*/
insert into ndfl_person_income(ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) VALUES
(3010, 301, null, 1, '2016_1', null, null, '7', '000000003', null, null, null, null, null, null, null, null, null, null, null, null, null, TO_DATE('01-02-2016', 'DD-MM-YYYY'), TO_DATE('01-02-2018', 'DD-MM-YYYY'), 1, 1200, 8);
insert into ndfl_person_income(ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) VALUES
(3020, 301, null, 2, '2016_1', '2000', '05', '7', '000000003', null, TO_DATE('31-01-2016', 'DD-MM-YYYY'), null, 11000, null, null, 13, TO_DATE('31-01-2016', 'DD-MM-YYYY'), null, 1200, null, null, null, null, null, null, null, 8);
insert into ndfl_person_income(ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) VALUES
(3030, 301, null, 3, '2016_1', '2000', '05', '7', '000000003', TO_DATE('31-01-2016', 'DD-MM-YYYY'), null, 11000, null, 1000, 10000, 13, TO_DATE('31-01-2016', 'DD-MM-YYYY'), 1300, null, null, null, null, null, null, null, null, 8);

/* КПП/ОКТМО текущего ТБ вариант 1. 2-й кв. 2016. Недействующая версия*/
insert into ndfl_person_income(ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) VALUES
  (3040, 302, null, 1, '2016_2', null, null, '1', '000000001', null, null, null, null, null, null, null, null, null, null, null, null, null, TO_DATE('01-05-2016', 'DD-MM-YYYY'), TO_DATE('01-05-2016', 'DD-MM-YYYY'), 1, 1200, 8);
insert into ndfl_person_income(ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) VALUES
  (3050, 302, null, 2, '2016_2', '2000', '05', '1', '000000001', null, TO_DATE('30-04-2016', 'DD-MM-YYYY'), null, 11000, null, null, 13, TO_DATE('30-01-2018', 'DD-MM-YYYY'), null, 1200, null, null, null, null, null, null, null, 8);
insert into ndfl_person_income(ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) VALUES
  (3060, 302, null, 3, '2016_2', '2000', '05', '1', '000000001', TO_DATE('30-04-2016', 'DD-MM-YYYY'), null, 11000, null, 1000, 10000, 13, TO_DATE('30-04-2016', 'DD-MM-YYYY'), 1300, null, null, null, null, null, null, null, null, 8);

/* КПП/ОКТМО текущего ТБ вариант 2. 3-й кв. 2016 2*/
insert into ndfl_person_income(ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) VALUES
  (3070, 303, null, 1, '2016_3', null, null, '2', '000000002', null, null, null, null, null, null, null, null, null, null, null, null, null, TO_DATE('01-08-2016', 'DD-MM-YYYY'), TO_DATE('01-08-2016', 'DD-MM-YYYY'), 1, 1200, 8);
insert into ndfl_person_income(ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) VALUES
  (3080, 303, null, 2, '2016_3', '2000', '05', '2', '000000002', null, TO_DATE('31-01-2018', 'DD-MM-YYYY'), null, 11000, null, null, 13, TO_DATE('31-07-2016', 'DD-MM-YYYY'), null, 1200, null, null, null, null, null, null, null, 8);
insert into ndfl_person_income(ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) VALUES
  (3090, 303, null, 3, '2016_3', '2000', '05', '2', '000000002', TO_DATE('31-07-2016', 'DD-MM-YYYY'), null, 11000, null, 1000, 10000, 13, TO_DATE('31-07-2016', 'DD-MM-YYYY'), 1300, null, null, null, null, null, null, null, null, 8);

/* операции из КНФ 3-й кв 2016*/
insert into ndfl_person_income(ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) VALUES
  (3100, 3031, 3070, 1, '2016_3', null, null, '2', '000000002', null, null, null, null, null, null, null, null, null, null, null, null, null, TO_DATE('01-01-2018', 'DD-MM-YYYY'), TO_DATE('01-08-2016', 'DD-MM-YYYY'), 1, 1200, 8);
insert into ndfl_person_income(ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) VALUES
  (3110, 3031, 3080, 2, '2016_3', '2000', '05', '2', '000000002', null, TO_DATE('31-07-2016', 'DD-MM-YYYY'), null, 11000, null, null, 13, TO_DATE('31-07-2016', 'DD-MM-YYYY'), null, 1200, null, null, null, null, null, null, null, 8);
insert into ndfl_person_income(ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) VALUES
  (3120, 3031, 3090, 3, '2016_3', '2000', '05', '2', '000000002', TO_DATE('31-01-2018', 'DD-MM-YYYY'), null, 11000, null, 1000, 10000, 13, TO_DATE('31-07-2016', 'DD-MM-YYYY'), 1300, null, null, null, null, null, null, null, null, 8);

/* КПП/ОКТМО текущего ТБ новая настройка*/
insert into ndfl_person_income(ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) VALUES
  (3130, 305, null, 1, '2017_1', null, null, '1', '000000004', null, null, null, null, null, null, null, null, null, null, null, null, null, TO_DATE('01-02-2018', 'DD-MM-YYYY'), TO_DATE('01-02-2017', 'DD-MM-YYYY'), 1, 1200, 8);
insert into ndfl_person_income(ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) VALUES
  (3140, 305, null, 2, '2017_1', '2000', '05', '1', '000000004', null, TO_DATE('31-01-2017', 'DD-MM-YYYY'), null, 11000, null, null, 13, TO_DATE('31-01-2017', 'DD-MM-YYYY'), null, 1200, null, null, null, null, null, null, null, 8);
insert into ndfl_person_income(ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) VALUES
  (3150, 305, null, 3, '2017_1', '2000', '05', '1', '000000004', TO_DATE('31-01-2017', 'DD-MM-YYYY'), null, 11000, null, 1000, 10000, 13, TO_DATE('31-01-2017', 'DD-MM-YYYY'), 1300, null, null, null, null, null, null, null, null, 8);

/* КПП/ОКТМО из недействующей версии*/
insert into ndfl_person_income(ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) VALUES
  (3160, 306, null, 1, '2017_2', null, null, '1', '000000001', null, null, null, null, null, null, null, null, null, null, null, null, null, TO_DATE('01-05-2017', 'DD-MM-YYYY'), TO_DATE('01-05-2017', 'DD-MM-YYYY'), 1, 1200, 8);
insert into ndfl_person_income(ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) VALUES
  (3170, 306, null, 2, '2017_2', '2000', '05', '1', '000000001', null, TO_DATE('30-01-2018', 'DD-MM-YYYY'), null, 11000, null, null, 13, TO_DATE('30-04-2017', 'DD-MM-YYYY'), null, 1200, null, null, null, null, null, null, null, 8);
insert into ndfl_person_income(ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) VALUES
  (3180, 306, null, 3, '2017_2', '2000', '05', '1', '000000001', TO_DATE('30-04-2017', 'DD-MM-YYYY'), null, 11000, null, 1000, 10000, 13, TO_DATE('30-04-2017', 'DD-MM-YYYY'), 1300, null, null, null, null, null, null, null, null, 8);

/* Текущий период*/
/* КПП/ОКТМО другого ТБ*/
insert into ndfl_person_income(ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) VALUES
  (3190, 309, null, 1, '2018_1', null, null, '7', '000000003', null, null, null, null, null, null, null, null, null, null, null, null, null, TO_DATE('01-02-2018', 'DD-MM-YYYY'), TO_DATE('01-02-2018', 'DD-MM-YYYY'), 1, 1200, 8);
insert into ndfl_person_income(ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) VALUES
  (3200, 309, null, 2, '2018_1', '2000', '05', '7', '000000003', null, TO_DATE('31-01-2018', 'DD-MM-YYYY'), null, 11000, null, null, 13, TO_DATE('31-01-2018', 'DD-MM-YYYY'), null, 1200, null, null, null, null, null, null, null, 8);
insert into ndfl_person_income(ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) VALUES
  (3210, 309, null, 3, '2018_1', '2000', '05', '7', '000000003', TO_DATE('31-01-2018', 'DD-MM-YYYY'), null, 11000, null, 1000, 10000, 13, TO_DATE('31-01-2018', 'DD-MM-YYYY'), 1300, null, null, null, null, null, null, null, null, 8);

/* КПП/ОКТМО текущего ТБ вариант 1*/
insert into ndfl_person_income(ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) VALUES
  (3220, 309, null, 1, '2018_2', null, null, '1', '000000004', null, null, null, null, null, null, null, null, null, null, null, null, null, TO_DATE('02-02-2018', 'DD-MM-YYYY'), TO_DATE('02-02-2018', 'DD-MM-YYYY'), 1, 1200, 8);
insert into ndfl_person_income(ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) VALUES
  (3230, 309, null, 2, '2018_2', '2000', '05', '1', '000000004', null, TO_DATE('01-02-2018', 'DD-MM-YYYY'), null, 11000, null, null, 13, TO_DATE('01-02-2018', 'DD-MM-YYYY'), null, 1200, null, null, null, null, null, null, null, 8);
insert into ndfl_person_income(ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) VALUES
  (3240, 309, null, 3, '2018_2', '2000', '05', '1', '000000004', TO_DATE('01-02-2018', 'DD-MM-YYYY'), null, 11000, null, 1000, 10000, 13, TO_DATE('01-02-2018', 'DD-MM-YYYY'), 1300, null, null, null, null, null, null, null, null, 8);

/* КПП/ОКТМО текущего ТБ вариант 2*/
insert into ndfl_person_income(ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) VALUES
  (3250, 309, null, 1, '2018_3', null, null, '2', '000000002', null, null, null, null, null, null, null, null, null, null, null, null, null, TO_DATE('03-02-2018', 'DD-MM-YYYY'), TO_DATE('03-02-2018', 'DD-MM-YYYY'), 1, 1200, 8);
insert into ndfl_person_income(ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) VALUES
  (3260, 309, null, 2, '2018_3', '2000', '05', '2', '000000002', null, TO_DATE('02-02-2018', 'DD-MM-YYYY'), null, 11000, null, null, 13, TO_DATE('02-02-2018', 'DD-MM-YYYY'), null, 1200, null, null, null, null, null, null, null, 8);
insert into ndfl_person_income(ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) VALUES
  (3270, 309, null, 3, '2018_3', '2000', '05', '2', '000000002', TO_DATE('02-02-2018', 'DD-MM-YYYY'), null, 11000, null, 1000, 10000, 13, TO_DATE('02-02-2018', 'DD-MM-YYYY'), 1300, null, null, null, null, null, null, null, null, 8);

/* КПП/ОКТМО из недействующей версии*/
insert into ndfl_person_income(ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) VALUES
  (3280, 309, null, 1, '2018_4', null, null, '1', '000000001', null, null, null, null, null, null, null, null, null, null, null, null, null, TO_DATE('04-02-2018', 'DD-MM-YYYY'), TO_DATE('04-02-2018', 'DD-MM-YYYY'), 1, 1200, 8);
insert into ndfl_person_income(ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) VALUES
  (3290, 309, null, 2, '2018_4', '2000', '05', '1', '000000001', null, TO_DATE('03-02-2018', 'DD-MM-YYYY'), null, 11000, null, null, 13, TO_DATE('03-02-2018', 'DD-MM-YYYY'), null, 1200, null, null, null, null, null, null, null, 8);
insert into ndfl_person_income(ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) VALUES
  (3330, 309, null, 3, '2018_4', '2000', '05', '1', '000000001', TO_DATE('03-02-2018', 'DD-MM-YYYY'), null, 11000, null, 1000, 10000, 13, TO_DATE('03-02-2018', 'DD-MM-YYYY'), 1300, null, null, null, null, null, null, null, null, 8);

/* Проверка общего фильтра */
insert into NDFL_PERSON (id, declaration_data_id, inp, FIRST_NAME, LAST_NAME, BIRTH_DAY, CITIZENSHIP, ID_DOC_TYPE, ID_DOC_NUMBER, STATUS, PERSON_ID) values
(50001, 50000, 1111111111, 'Фам1', 'Имя1', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '050', '11', '0000', '1', 2),
(50002, 50000, 2222222222, 'Фам2', 'Имя2', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '050', '11', '0000', '1', 3),
(50003, 50000, 3333333333, 'Фам3', 'Имя3', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '050', '11', '0000', '1', 4);

Insert into NDFL_PERSON_INCOME (ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) values
(50001,50001,null,1,'1','1111','11','1','1111',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-10-2005','DD-MM-YYYY'),'100','200','2','0','13',to_date('01-11-2010','DD-MM-YYYY'),'1','10','20','0','30',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-11-2010','DD-MM-YYYY'),'0','0', 8),
(50002,50001,null,2,'1','2222','11','1','2222',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-10-2005','DD-MM-YYYY'),'100','200','2','0','13',to_date('01-11-2010','DD-MM-YYYY'),'1','10','20','0','30',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-11-2010','DD-MM-YYYY'),'0','0', 8),
(50003,50001,null,3,'1','3333','11','1','3333',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-10-2005','DD-MM-YYYY'),'100','200','2','0','13',to_date('01-11-2010','DD-MM-YYYY'),'1','10','20','0','30',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-11-2011','DD-MM-YYYY'),'0','0', 8),

(50004,50002,null,4,'2','1111','22','1','4444',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-10-2005','DD-MM-YYYY'),'100','200','2','0','13',to_date('01-11-2010','DD-MM-YYYY'),'1','10','20','0','30',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-11-2010','DD-MM-YYYY'),'0','0', 8),
(50005,50002,null,5,'2','2222','22','1','5555',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-10-2005','DD-MM-YYYY'),'100','200','2','0','13',to_date('01-11-2010','DD-MM-YYYY'),'1','10','20','0','30',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-11-2010','DD-MM-YYYY'),'0','0', 8),
(50006,50002,null,6,'2','3333','22','1','6666',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-10-2005','DD-MM-YYYY'),'100','200','2','0','13',to_date('01-11-2010','DD-MM-YYYY'),'1','10','20','0','30',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-11-2011','DD-MM-YYYY'),'0','0', 8),

(50007,50003,null,7,'3','1111','33','2','7777',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-10-2005','DD-MM-YYYY'),'100','200','2','0','13',to_date('01-11-2010','DD-MM-YYYY'),'1','10','20','0','30',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-11-2010','DD-MM-YYYY'),'0','0', 8),
(50008,50003,null,8,'3','2222','33','2','8888',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-10-2005','DD-MM-YYYY'),'100','200','2','0','13',to_date('01-11-2010','DD-MM-YYYY'),'1','10','20','0','30',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-11-2010','DD-MM-YYYY'),'0','0', 8),
(50009,50003,null,9,'3','3333','33','2','9999',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-10-2005','DD-MM-YYYY'),'100','200','2','0','13',to_date('01-11-2010','DD-MM-YYYY'),'1','10','20','0','30',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-11-2011','DD-MM-YYYY'),'0','0', 8);

insert INTO ndfl_person_deduction (id, ndfl_person_id, source_id, row_num, operation_id, type_code, notif_type, notif_date, notif_num, notif_source, notif_summ, income_accrued, income_code, income_summ, period_prev_date, period_prev_summ, period_curr_date, period_curr_summ, ASNU_ID) VALUES
(50001, 50001, NULL, 1, '1', '111', '11', to_date('30-10-2005','DD-MM-YYYY'), '11111', '110001', 2, to_date('01-10-2005','DD-MM-YYYY'), '0000', 100, to_date('30-10-2005','DD-MM-YYYY'), null, to_date('31-10-2005','DD-MM-YYYY'), null, 8),
(50002, 50001, NULL, 2, '1', '222', '11', to_date('30-10-2005','DD-MM-YYYY'), '22222', '110001', 2, to_date('01-10-2005','DD-MM-YYYY'), '0000', 100, to_date('30-10-2005','DD-MM-YYYY'), null, to_date('31-10-2005','DD-MM-YYYY'), null, 8),
(50003, 50001, NULL, 3, '1', '333', '11', to_date('30-10-2005','DD-MM-YYYY'), '33333', '110001', 2, to_date('01-10-2005','DD-MM-YYYY'), '0000', 100, to_date('30-10-2005','DD-MM-YYYY'), null, to_date('31-10-2005','DD-MM-YYYY'), null, 8),

(50004, 50002, NULL, 4, '2', '111', '22', to_date('30-10-2005','DD-MM-YYYY'), '44444', '110001', 2, to_date('01-10-2005','DD-MM-YYYY'), '0000', 100, to_date('30-10-2005','DD-MM-YYYY'), null, to_date('31-10-2005','DD-MM-YYYY'), null, 8),
(50005, 50002, NULL, 5, '2', '222', '22', to_date('30-10-2005','DD-MM-YYYY'), '55555', '110001', 2, to_date('01-10-2005','DD-MM-YYYY'), '0000', 100, to_date('30-10-2005','DD-MM-YYYY'), null, to_date('31-10-2005','DD-MM-YYYY'), null, 8),
(50006, 50002, NULL, 6, '2', '333', '22', to_date('30-10-2005','DD-MM-YYYY'), '66666', '110001', 2, to_date('01-10-2005','DD-MM-YYYY'), '0000', 100, to_date('30-10-2005','DD-MM-YYYY'), null, to_date('31-10-2005','DD-MM-YYYY'), null, 8),

(50007, 50003, NULL, 7, '3', '111', '33', to_date('30-10-2005','DD-MM-YYYY'), '77777', '110001', 2, to_date('01-10-2005','DD-MM-YYYY'), '0000', 100, to_date('30-10-2005','DD-MM-YYYY'), null, to_date('31-10-2005','DD-MM-YYYY'), null, 8),
(50008, 50003, NULL, 8, '3', '222', '33', to_date('30-10-2005','DD-MM-YYYY'), '88888', '110001', 2, to_date('01-10-2005','DD-MM-YYYY'), '0000', 100, to_date('30-10-2005','DD-MM-YYYY'), null, to_date('31-10-2005','DD-MM-YYYY'), null, 8),
(50009, 50003, NULL, 9, '3', '333', '33', to_date('30-10-2005','DD-MM-YYYY'), '99999', '110001', 2, to_date('01-10-2005','DD-MM-YYYY'), '0000', 100, to_date('30-10-2005','DD-MM-YYYY'), null, to_date('31-10-2005','DD-MM-YYYY'), null, 8);

insert into ndfl_person_prepayment (id, ndfl_person_id, source_id, row_num, operation_id, summ, notif_num, notif_date, notif_source, ASNU_ID) VALUES
(50001, 50001, null, 1, '1', 100, '1', to_date('01-10-2005','DD-MM-YYYY'), '11', 8),
(50002, 50001, null, 2, '1', 100, '2', to_date('02-10-2005','DD-MM-YYYY'), '22', 8),
(50003, 50001, null, 3, '1', 100, '3', to_date('03-10-2005','DD-MM-YYYY'), '33', 8),

(50004, 50002, null, 4, '2', 100, '1', to_date('04-10-2005','DD-MM-YYYY'), '11', 8),
(50005, 50002, null, 5, '2', 100, '2', to_date('05-10-2005','DD-MM-YYYY'), '22', 8),
(50006, 50002, null, 6, '2', 100, '3', to_date('06-10-2005','DD-MM-YYYY'), '33', 8);

------------------------------
insert into NDFL_PERSON (id, declaration_data_id, inp, FIRST_NAME, LAST_NAME, BIRTH_DAY, CITIZENSHIP, ID_DOC_TYPE, ID_DOC_NUMBER, STATUS, PERSON_ID, ASNU_ID) values
(1001, 100, '100500', 'Ivan', 'Ivanov', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '001', '11', '0000', '1', 1, 8),
(1002, 100, 1234567890, 'Ivan', 'Ivanov', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123', '22', '0000', '2',12122, 8);

Insert into NDFL_PERSON_INCOME (ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM, ASNU_ID) values
(1001,1001,null,null,'1','0000','00','111222333','99222',to_date('02-10-2005','DD-MM-YYYY'),to_date('01-10-2005','DD-MM-YYYY'),'100','200','2','0','13',to_date('01-11-2010','DD-MM-YYYY'),'1','10','20','0','30',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-11-2010','DD-MM-YYYY'),'0','0', 8),
(1002,1001,null,null,'2','1010','00','111222333','99222',to_date('02-10-2005','DD-MM-YYYY'),to_date('01-10-2005','DD-MM-YYYY'),'100','200','2','0','13',to_date('01-11-2010','DD-MM-YYYY'),'1','10','20','0','30',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-11-2010','DD-MM-YYYY'),'0','0', 8),
(1003,1001,null,null,'2','1010','00','111222333','99222',to_date('30-10-2005','DD-MM-YYYY'),to_date('01-10-2005','DD-MM-YYYY'),'100','200','2','0','13',to_date('01-11-2010','DD-MM-YYYY'),'1','10','20','0','30',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-11-2010','DD-MM-YYYY'),'0','0', 8),
(1004,1002,null,null,'3','1010','00','111222333','88444',to_date('30-10-2005','DD-MM-YYYY'),to_date('01-10-2005','DD-MM-YYYY'),'100','200','2','0','13',to_date('01-11-2010','DD-MM-YYYY'),'1','10','20','0','30',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-11-2010','DD-MM-YYYY'),'0','0', 8),
(1005,1002,null,null,'4','1010','00','111222333','99333',to_date('02-10-2005','DD-MM-YYYY'),to_date('01-10-2005','DD-MM-YYYY'),'100','200','2','0','13',to_date('01-11-2010','DD-MM-YYYY'),'1','10','20','0','30',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-11-2010','DD-MM-YYYY'),'0','0', 8);

insert INTO ndfl_person_deduction (id, ndfl_person_id, source_id, row_num, operation_id, type_code, notif_type, notif_date, notif_num, notif_source, notif_summ, income_accrued, income_code, income_summ, period_prev_date, period_prev_summ, period_curr_date, period_curr_summ, ASNU_ID) VALUES
(1001, 1001, NULL, 1, '1', '111', '11', to_date('30-10-2005','DD-MM-YYYY'), '11111', '110001', 2, to_date('01-10-2005','DD-MM-YYYY'), '0000', 100, to_date('30-10-2005','DD-MM-YYYY'), null, to_date('31-10-2005','DD-MM-YYYY'), null, 8),
(1002, 1001, NULL, 2, '1', '222', '11', to_date('30-10-2005','DD-MM-YYYY'), '22222', '110001', 2, to_date('01-10-2005','DD-MM-YYYY'), '0000', 100, to_date('30-10-2005','DD-MM-YYYY'), null, to_date('31-10-2005','DD-MM-YYYY'), null, 8),
(1003, 1002, NULL, 3, '3', '333', '11', to_date('30-10-2005','DD-MM-YYYY'), '33333', '110001', 2, to_date('01-10-2005','DD-MM-YYYY'), '0000', 100, to_date('30-10-2005','DD-MM-YYYY'), null, to_date('31-10-2005','DD-MM-YYYY'), null, 8)

insert into ndfl_person_prepayment (id, ndfl_person_id, source_id, row_num, operation_id, summ, notif_num, notif_date, notif_source, ASNU_ID) VALUES
(1001, 1001, null, null, '1', 100, '1', to_date('01-10-2005','DD-MM-YYYY'), '01', 8),
(1002, 1001, null, null, '2', 101, '1', to_date('01-10-2005','DD-MM-YYYY'), '01', 8),
(1003, 1001, null, null, '2', 103, '1', to_date('01-10-2005','DD-MM-YYYY'), '01', 8),
(1004, 1002, null, null, '3', 104, '1', to_date('01-10-2005','DD-MM-YYYY'), '01', 8),
(1005, 1002, null, null, '4', 104, '1', to_date('01-10-2005','DD-MM-YYYY'), '01', 8),
(1006, 1002, null, null, '5', 104, '1', to_date('01-10-2005','DD-MM-YYYY'), '01', 8);