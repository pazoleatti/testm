-- tax period
Insert into TAX_PERIOD (ID,TAX_TYPE,START_DATE,END_DATE) values (1,'T',to_date('01.01.02','DD.MM.RR'),to_date('31.12.02','DD.MM.RR'));

-- report period
Insert into REPORT_PERIOD (ID,NAME,IS_ACTIVE,MONTHS,TAX_PERIOD_ID,ORD) values (1,'2002 - 1 квартал',1,3,1,1);
Insert into REPORT_PERIOD (ID,NAME,IS_ACTIVE,MONTHS,TAX_PERIOD_ID,ORD) values (2,'2002 - 2 квартал',0,3,1,2);

-- income101 data
Insert into INCOME_102 (REPORT_PERIOD_ID,OPU_CODE,TOTAL_SUM) values (1,'2',666);
Insert into INCOME_102 (REPORT_PERIOD_ID,OPU_CODE,TOTAL_SUM) values (2,'2',555);
Insert into INCOME_102 (REPORT_PERIOD_ID,OPU_CODE,TOTAL_SUM) values (1,'3',444);