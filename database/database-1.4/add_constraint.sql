declare 
  v_count number;
BEGIN
	select count(1) into v_count from user_constraints where constraint_name='FK_REF_BOOK_ASNU_ROLE_ALIAS';
	if v_count=0 then
		execute immediate 'alter table REF_BOOK_ASNU add ROLE_ALIAS number(9,0)';
		execute immediate 'update REF_BOOK_ASNU set ROLE_ALIAS=35 where code=''0000''';
		execute immediate 'update REF_BOOK_ASNU set ROLE_ALIAS=20 where code=''1000''';
		execute immediate 'update REF_BOOK_ASNU set ROLE_ALIAS=21 where code=''2000''';
		execute immediate 'update REF_BOOK_ASNU set ROLE_ALIAS=22 where code=''3000''';
		execute immediate 'update REF_BOOK_ASNU set ROLE_ALIAS=23 where code=''4000''';
		execute immediate 'update REF_BOOK_ASNU set ROLE_ALIAS=24 where code=''5000''';
		execute immediate 'update REF_BOOK_ASNU set ROLE_ALIAS=25 where code=''6000''';
		execute immediate 'update REF_BOOK_ASNU set ROLE_ALIAS=26 where code=''6001''';
		execute immediate 'update REF_BOOK_ASNU set ROLE_ALIAS=27 where code=''7000''';
		execute immediate 'update REF_BOOK_ASNU set ROLE_ALIAS=28 where code=''6002''';
		execute immediate 'update REF_BOOK_ASNU set ROLE_ALIAS=29 where code=''6003''';
		execute immediate 'update REF_BOOK_ASNU set ROLE_ALIAS=30 where code=''6004''';
		execute immediate 'update REF_BOOK_ASNU set ROLE_ALIAS=31 where code=''6005''';
		execute immediate 'update REF_BOOK_ASNU set ROLE_ALIAS=32 where code=''1001''';
		execute immediate 'update REF_BOOK_ASNU set ROLE_ALIAS=33 where code=''8000''';
		execute immediate 'update REF_BOOK_ASNU set ROLE_ALIAS=34 where code=''9000''';
		execute immediate 'alter table REF_BOOK_ASNU add constraint FK_REF_BOOK_ASNU_ROLE_ALIAS foreign key (ROLE_ALIAS) references SEC_ROLE(ID)';
		execute immediate 'alter table REF_BOOK_ASNU add constraint UC_REF_BOOK_ASNU_ROLE_ALIAS unique (ROLE_ALIAS)';
	end if;	
	
	select count(1) into v_count from user_constraints where constraint_name='FK_REF_BOOK_ASNU_ROLE_NAME';
	if v_count=0 then
		execute immediate 'alter table REF_BOOK_ASNU add ROLE_NAME number(9,0)';
		execute immediate 'update REF_BOOK_ASNU set ROLE_NAME=35 where code=''0000''';
		execute immediate 'update REF_BOOK_ASNU set ROLE_NAME=20 where code=''1000''';
		execute immediate 'update REF_BOOK_ASNU set ROLE_NAME=21 where code=''2000''';
		execute immediate 'update REF_BOOK_ASNU set ROLE_NAME=22 where code=''3000''';
		execute immediate 'update REF_BOOK_ASNU set ROLE_NAME=23 where code=''4000''';
		execute immediate 'update REF_BOOK_ASNU set ROLE_NAME=24 where code=''5000''';
		execute immediate 'update REF_BOOK_ASNU set ROLE_NAME=25 where code=''6000''';
		execute immediate 'update REF_BOOK_ASNU set ROLE_NAME=26 where code=''6001''';
		execute immediate 'update REF_BOOK_ASNU set ROLE_NAME=27 where code=''7000''';
		execute immediate 'update REF_BOOK_ASNU set ROLE_NAME=28 where code=''6002''';
		execute immediate 'update REF_BOOK_ASNU set ROLE_NAME=29 where code=''6003''';
		execute immediate 'update REF_BOOK_ASNU set ROLE_NAME=30 where code=''6004''';
		execute immediate 'update REF_BOOK_ASNU set ROLE_NAME=31 where code=''6005''';
		execute immediate 'update REF_BOOK_ASNU set ROLE_NAME=32 where code=''1001''';
		execute immediate 'update REF_BOOK_ASNU set ROLE_NAME=33 where code=''8000''';
		execute immediate 'update REF_BOOK_ASNU set ROLE_NAME=34 where code=''9000''';
		execute immediate 'alter table REF_BOOK_ASNU add constraint FK_REF_BOOK_ASNU_ROLE_NAME foreign key (ROLE_NAME) references SEC_ROLE(ID)';
		execute immediate 'alter table REF_BOOK_ASNU add constraint UC_REF_BOOK_ASNU_ROLE_NAME unique (ROLE_NAME)';
	end if;	
END;
/