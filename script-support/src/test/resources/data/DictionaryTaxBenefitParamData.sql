Insert into DICT_REGION (CODE,NAME,OKATO,OKATO_DEFINITION) values ('02','Республика Башкортостан','80000000000','80');

insert into DICT_TAX_BENEFIT(code,name) values('20200','Льготы по транспортному налогу, устанавливаемые законами субъектов Российской Федерации, из них');
insert into DICT_TAX_BENEFIT(code,name) values('20210',' - льготы в виде освобождения от налогообложения по транспортному налогу');

Insert into DICT_TAX_BENEFIT_PARAM (ID, DICT_REGION_ID,TAX_BENEFIT_ID,SECTION,ITEM,SUBITEM,PERCENT,RATE) values (1, '02','20200',null,null,null,null,null);
Insert into DICT_TAX_BENEFIT_PARAM (ID, DICT_REGION_ID,TAX_BENEFIT_ID,SECTION,ITEM,SUBITEM,PERCENT,RATE) values (2, '02','20210',null,null,null,null,null);
