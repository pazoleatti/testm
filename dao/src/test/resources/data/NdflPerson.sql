SET DATABASE REFERENTIAL INTEGRITY FALSE;

insert into declaration_data(id, declaration_template_id, department_report_period_id, state, tax_organ_code, kpp, note) values (1, 1, 102, 3, 'CD12', '123456789', 'Первичка по');
insert into declaration_data(id, declaration_template_id, department_report_period_id, state) values (2, 1, 204, 1);
insert into declaration_data(id, declaration_template_id, department_report_period_id, state) values (3, 1, 303, 3);
insert into declaration_data(id, declaration_template_id, department_report_period_id, state) values (4, 1, 401, 1);
insert into declaration_data(id, declaration_template_id, department_report_period_id, state) values (5, 1, 501, 3);
insert into declaration_data(id, declaration_template_id, department_report_period_id, state) values (7, 1, 605, 3);


insert into NDFL_PERSON (id, declaration_data_id, inp, FIRST_NAME, LAST_NAME, BIRTH_DAY, CITIZENSHIP, ID_DOC_TYPE, ID_DOC_NUMBER, STATUS, PERSON_ID) values
(101, 1, '100500', 'Ivan', 'Ivanov', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '001', '11', '0000', '1', 112233),
(102, 1, 1234567890, 'Ivan', 'Ivanov', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123', '22', '0000', '2',12122),
(103, 1, 1234567890, 'Федор', 'Иванов', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123', '22', '0000', '2',15155);

Insert into NDFL_PERSON_INCOME (ID,NDFL_PERSON_ID,SOURCE_ID,ROW_NUM,OPERATION_ID,INCOME_CODE,INCOME_TYPE,OKTMO,KPP,INCOME_ACCRUED_DATE,INCOME_PAYOUT_DATE,INCOME_ACCRUED_SUMM,INCOME_PAYOUT_SUMM,TOTAL_DEDUCTIONS_SUMM,TAX_BASE,TAX_RATE,TAX_DATE,CALCULATED_TAX,WITHHOLDING_TAX,NOT_HOLDING_TAX,OVERHOLDING_TAX,REFOUND_TAX,TAX_TRANSFER_DATE,PAYMENT_DATE,PAYMENT_NUMBER,TAX_SUMM) values 
('1036','101',null,null,'1','0000','00','111222333','2222',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-10-2005','DD-MM-YYYY'),'100','200','2','0','13',to_date('01-11-2010','DD-MM-YYYY'),'1','10','20','0','30',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-11-2010','DD-MM-YYYY'),'0','0'),
('1037','101',null,null,'2','1010','00','111222333','2222',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-10-2005','DD-MM-YYYY'),'100','200','2','0','13',to_date('01-11-2010','DD-MM-YYYY'),'1','10','20','0','30',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-11-2010','DD-MM-YYYY'),'0','0'),
('1038','102',null,null,'3','1010','00','111222333','2222',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-10-2005','DD-MM-YYYY'),'100','200','2','0','9',to_date('01-11-2010','DD-MM-YYYY'),'1','10','20','0','30',to_date('01-10-2005','DD-MM-YYYY'),to_date('01-11-2011','DD-MM-YYYY'),'0','0');


--Test UpdatePersonRefBookReferences
insert into NDFL_PERSON (id, declaration_data_id, inp, FIRST_NAME, LAST_NAME, BIRTH_DAY, CITIZENSHIP, ID_DOC_TYPE, ID_DOC_NUMBER, STATUS) values
(201, 2, '100500', 'Ivan', 'Ivanov', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '001', '11', '0000', '1'),
(202, 2, 1234567890, 'Ivan', 'Ivanov', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123', '22', '0000', '2'),
(203, 2, 1234567890, 'Федор', 'Иванов', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123', '22', '0000', '2'),
(204, 2, 1234567890, 'Федор', 'Иванов', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123', '22', '0000', '2'),
(205, 2, 1234567890, 'Федор', 'Иванов', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123', '22', '0000', '2'),
(206, 2, 1234567890, 'Федор', 'Иванов', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123', '22', '0000', '2'),
(207, 2, 1234567890, 'Федор', 'Иванов', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123', '22', '0000', '2'),
(208, 2, 1234567890, 'Федор', 'Иванов', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123', '22', '0000', '2'),
(209, 2, 1234567890, 'Федор', 'Иванов', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123', '22', '0000', '2'),
(210, 2, 1234567890, 'Федор', 'Иванов', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '123', '22', '0000', '2');
