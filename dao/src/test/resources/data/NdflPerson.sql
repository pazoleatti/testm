SET DATABASE REFERENTIAL INTEGRITY FALSE;

insert into declaration_data(id, declaration_template_id, department_report_period_id, state, tax_organ_code, kpp, note) values (1, 1, 102, 3, 'CD12', '123456789', 'Первичка по');
insert into declaration_data(id, declaration_template_id, department_report_period_id, state) values (2, 1, 204, 1);
insert into declaration_data(id, declaration_template_id, department_report_period_id, state) values (3, 1, 303, 3);
insert into declaration_data(id, declaration_template_id, department_report_period_id, state) values (4, 1, 401, 1);
insert into declaration_data(id, declaration_template_id, department_report_period_id, state) values (5, 1, 501, 3);
insert into declaration_data(id, declaration_template_id, department_report_period_id, state) values (7, 1, 605, 3);


insert into NDFL_PERSON (id, declaration_data_id, inp, FIRST_NAME, LAST_NAME, BIRTH_DAY, CITIZENSHIP, ID_DOC_TYPE, ID_DOC_NUMBER, STATUS, PERSON_ID) values
(101, 1, '100500', 'Ivan', 'Ivanov', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '001', '11', '0000', '1', 1),
(102, 1, 1234567890, 'Ivan', 'Ivanov', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123', '22', '0000', '2',12122),
(103, 1, 1234567890, 'Федор', 'Иванов', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123', '22', '0000', '2',15155);

Insert into NDFL_PERSON_INCOME (ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM) values 
('1036','101',null,null,'1','0000','00','111222333','2222',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-10-2005','DD-MM-YYYY'),'100','200','2','0','13',to_date('01-11-2010','DD-MM-YYYY'),'1','10','20','0','30',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-11-2010','DD-MM-YYYY'),'0','0'),
('1037','101',null,null,'2','1010','00','111222333','2222',to_date('02-10-2005','DD-MM-YYYY'),to_date('02-10-2005','DD-MM-YYYY'),'101','201','3','1','13',to_date('01-11-2010','DD-MM-YYYY'),'2','11','21','1','31',to_date('02-10-2005','DD-MM-YYYY'),to_date('02-11-2010','DD-MM-YYYY'),'0','1'),
('1038','102',null,null,'3','1010','00','111222333','2222',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-10-2005','DD-MM-YYYY'),'100','200','2','0','9',to_date('01-11-2010','DD-MM-YYYY'),'1','10','20','0','30',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-11-2011','DD-MM-YYYY'),'0','0');

insert INTO ndfl_person_deduction (id, ndfl_person_id, source_id, row_num, operation_id, type_code, notif_type, notif_date, notif_num, notif_source, notif_summ, income_accrued, income_code, income_summ, period_prev_date, period_prev_summ, period_curr_date, period_curr_summ) VALUES
(1, 101, NULL, null, '1', '100', '01', to_date('30-10-2005','DD-MM-YYYY'), '00001', '110001', 2, to_date('01-10-2005','DD-MM-YYYY'), '0000', 100, to_date('30-10-2005','DD-MM-YYYY'), null, to_date('31-10-2005','DD-MM-YYYY'), null),
(2, 101, NULL, null, '2', '100', '02', to_date('31-10-2005','DD-MM-YYYY'), '00002', '110002', 3, to_date('01-10-2005','DD-MM-YYYY'), '0000', 101, to_date('31-10-2005','DD-MM-YYYY'), null, to_date('31-10-2005','DD-MM-YYYY'), null);

insert into ndfl_person_prepayment (id, ndfl_person_id, source_id, row_num, operation_id, summ, notif_num, notif_date, notif_source) VALUES
(1, 101, null, null, '1', 100, '1', to_date('01-10-2005','DD-MM-YYYY'), '01'),
(2, 101, null, null, '2', 101, '1', to_date('01-10-2005','DD-MM-YYYY'), '01');

--Test UpdatePersonRefBookReferences
insert into NDFL_PERSON (id, declaration_data_id, inp, snils, FIRST_NAME, LAST_NAME, MIDDLE_NAME, BIRTH_DAY, CITIZENSHIP, INN_NP, INN_FOREIGN, ID_DOC_TYPE, ID_DOC_NUMBER, STATUS, ROW_NUM, REGION_CODE, POST_INDEX, AREA, CITY, LOCALITY, STREET, HOUSE, BUILDING, FLAT) values
(201, 2, '100500', '123456760', 'Ivan', 'Ivanov', 'Петрович', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '001', '123456780', '123456770', '11', '0000', '1', 1, '01', '123456', 'Область1', 'Город1', 'Район1', 'Улица1', '1', '2', '3'),
(202, 2, 1234567890, '123456760', 'Ivan', 'Ivanov', 'Петрович', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123', '123456780', '123456770', '22', '0000', '2', 2, '02', '923456', 'Область2', 'Город2', 'Район3', 'Улица2', '2', '3', '4'),
(203, 2, 1234567890, '123456760', 'Федор', 'Иванов', 'Петрович', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123','123456780', '123456770', '22', '0000', '2', 3, '02', '923456', 'Область2', 'Город2', 'Район3', 'Улица3', '2', '3', '4'),
(204, 2, 1234567890, '123456760', 'Федор', 'Иванов', 'Петрович', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123','123456780', '123456770', '22', '0000', '2', 4, '02', '923456', 'Область2', 'Город2', 'Район3', 'Улица4', '2', '3', '4'),
(205, 2, 1234567890, '123456760', 'Федор', 'Иванов', 'Петрович', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123','123456780', '123456770', '22', '0000', '2', 5, '02', '923456', 'Область2', 'Город2', 'Район3', 'Улица4', '2', '3', '4'),
(206, 2, 1234567890, '123456760', 'Федор', 'Иванов', 'Петрович', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123','123456780', '123456770', '22', '0000', '2', 6, '02', '923456', 'Область2', 'Город2', 'Район3', 'Улица4', '2', '3', '4'),
(207, 2, 1234567890, '123456760', 'Федор', 'Иванов', 'Петрович', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123','123456780', '123456770', '22', '0000', '2', 7, '02', '923456', 'Область2', 'Город2', 'Район3', 'Улица4', '2', '3', '4'),
(208, 2, 1234567890, '123456761', 'Федор', 'Иванов', 'Петрович', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123','123456780', '123456770', '22', '0000', '2', 87, '02', '923456', 'Область2', 'Город2', 'Район3', 'Улица4', '2', '3', '4'),
(209, 2, 1234567890, '123456761', 'Федор', 'Иванов', 'Петрович', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123','123456780', '123456771', '22', '0000', '2', 21, '02', '923456', 'Область2', 'Город2', 'Район3', 'Улица4', '2', '3', '4'),
(210, 2, 1234567890, '123456761', 'Федор', 'Иванов', 'Петрович', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123','123456781', '123456771', '22', '0000', '2', 44, '02', '923456', 'Область2', 'Город2', 'Район3', 'Улица4', '2', '3', '4');

