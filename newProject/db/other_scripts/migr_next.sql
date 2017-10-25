declare 
  v_id number;
  v_count number;
BEGIN
  INSERT INTO tax.department(id, name, parent_id, type, shortname, tb_index, sbrf_code, region_id, is_active, code, garant_use, sunr_use)
  SELECT id, name, parent_id, type, shortname, tb_index, sbrf_code, region_id, is_active, code, garant_use, sunr_use
  FROM department where id not in (select id from tax.department) AND id!=-1;
  
  insert into tax.sec_user(id, login, name, department_id, is_active, email)
  SELECT id, login, name, department_id, is_active, email from sec_user where id not in (SELECT id from tax.sec_user);

  EXECUTE IMMEDIATE 'alter table sec_role modify name varchar(500 byte)';

  EXECUTE IMMEDIATE 'merge into sec_role a using
   (select 20 as id,
          ''N_ROLE_OPER_1000'' as alias,
          ''АС «SAP» (НДФЛ)'' as name
     from dual) b
    on (a.id = b.id)
    when not matched then
    insert ( id, alias, name)
    values ( b.id, b.alias, b.name)';
	
	EXECUTE IMMEDIATE 'merge into sec_role a using
   (select 21 as id,
          ''N_ROLE_OPER_2000'' as alias,
          ''АИС «Дивиденд» (НДФЛ)'' as name
     from dual) b
    on (a.id = b.id)
    when not matched then
    insert ( id, alias, name)
    values ( b.id, b.alias, b.name)';

	EXECUTE IMMEDIATE 'merge into sec_role a using
   (select 22 as id,
          ''N_ROLE_OPER_3000'' as alias,
          ''АС «Diasoft Custody 5NT» (НДФЛ)'' as name
     from dual) b
    on (a.id = b.id)
    when not matched then
    insert ( id, alias, name)
    values ( b.id, b.alias, b.name)';

	EXECUTE IMMEDIATE 'merge into sec_role a using
   (select 23 as id,
          ''N_ROLE_OPER_4000'' as alias,
          ''АС «Инфобанк» (НДФЛ)'' as name
     from dual) b
    on (a.id = b.id)
    when not matched then
    insert ( id, alias, name)
    values ( b.id, b.alias, b.name)';

	EXECUTE IMMEDIATE 'merge into sec_role a using
   (select 24 as id,
          ''N_ROLE_OPER_5000'' as alias,
          ''АИС «Депозитарий» (НДФЛ)'' as name
     from dual) b
    on (a.id = b.id)
    when not matched then
    insert ( id, alias, name)
    values ( b.id, b.alias, b.name)';

	EXECUTE IMMEDIATE 'merge into sec_role a using
   (select 25 as id,
          ''N_ROLE_OPER_6000'' as alias,
          ''Материальная выгода. Кредиты_АС «ЕКП» (НДФЛ)'' as name
     from dual) b
    on (a.id = b.id)
    when not matched then
    insert ( id, alias, name)
    values ( b.id, b.alias, b.name)';
	
	EXECUTE IMMEDIATE 'merge into sec_role a using
   (select 26 as id,
          ''N_ROLE_OPER_6001'' as alias,
          ''Экономическая выгода. Кредиты_АС «ЕКП» (НДФЛ)'' as name
     from dual) b
    on (a.id = b.id)
    when not matched then
    insert ( id, alias, name)
    values ( b.id, b.alias, b.name)';
	
	EXECUTE IMMEDIATE 'merge into sec_role a using
   (select 27 as id,
          ''N_ROLE_OPER_7000'' as alias,
          ''Экономическая выгода. Карты_ АС «ИПС БК» (НДФЛ)'' as name
     from dual) b
    on (a.id = b.id)
    when not matched then
    insert ( id, alias, name)
    values ( b.id, b.alias, b.name)';
	
	EXECUTE IMMEDIATE 'merge into sec_role a using
   (select 28 as id,
          ''N_ROLE_OPER_6002'' as alias,
          ''Экономическая выгода. Комиссии_АС «ЕКП» (НДФЛ)'' as name
     from dual) b
    on (a.id = b.id)
    when not matched then
    insert ( id, alias, name)
    values ( b.id, b.alias, b.name)';
	
	EXECUTE IMMEDIATE 'merge into sec_role a using
   (select 29 as id,
          ''N_ROLE_OPER_6003'' as alias,
          ''Реструктуризация валютных кредитов_АС «ЕКП» (НДФЛ)'' as name
     from dual) b
    on (a.id = b.id)
    when not matched then
    insert ( id, alias, name)
    values ( b.id, b.alias, b.name)';
	
	EXECUTE IMMEDIATE 'merge into sec_role a using
   (select 30 as id,
          ''N_ROLE_OPER_6004'' as alias,
          ''Прощение долга (амнистия). Кредиты_АС «ЕКП» (НДФЛ)'' as name
     from dual) b
    on (a.id = b.id)
    when not matched then
    insert ( id, alias, name)
    values ( b.id, b.alias, b.name)';
	
	EXECUTE IMMEDIATE 'merge into sec_role a using
   (select 31 as id,
          ''N_ROLE_OPER_6005'' as alias,
          ''Выплаты клиентам по решениям суда_АС «ЕКП» (НДФЛ)'' as name
     from dual) b
    on (a.id = b.id)
    when not matched then
    insert ( id, alias, name)
    values ( b.id, b.alias, b.name)';
	
	EXECUTE IMMEDIATE 'merge into sec_role a using
   (select 32 as id,
          ''N_ROLE_OPER_1001'' as alias,
          ''Призы, подарки клиентам_АС «SAP» (НДФЛ)'' as name
     from dual) b
    on (a.id = b.id)
    when not matched then
    insert ( id, alias, name)
    values ( b.id, b.alias, b.name)';
	
	EXECUTE IMMEDIATE 'merge into sec_role a using
   (select 33 as id,
          ''N_ROLE_OPER_8000'' as alias,
          ''АС «Back Office» (НДФЛ)'' as name
     from dual) b
    on (a.id = b.id)
    when not matched then
    insert ( id, alias, name)
    values ( b.id, b.alias, b.name)';
	
	EXECUTE IMMEDIATE 'merge into sec_role a using
   (select 34 as id,
          ''N_ROLE_OPER_9000'' as alias,
          ''АС «ЕКС» (НДФЛ)'' as name
     from dual) b
    on (a.id = b.id)
    when not matched then
    insert ( id, alias, name)
    values ( b.id, b.alias, b.name)';
	
	EXECUTE IMMEDIATE 'merge into sec_role a using
   (select 35 as id,
          ''N_ROLE_OPER_ALL'' as alias,
          ''Все АСНУ (НДФЛ)'' as name
     from dual) b
    on (a.id = b.id)
    when not matched then
    insert ( id, alias, name)
    values ( b.id, b.alias, b.name)';


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

    execute immediate 'merge into sec_user_role a using
		(select sua.user_id, rba.role_alias as role_id from sec_user_asnu sua JOIN ref_book_asnu rba on sua.asnu_id=rba.id) b
		on (a.user_id=b.user_id and a.role_id=b.role_id)
		when not matched then
			insert (user_id, role_id)
			values (b.user_id, b.role_id)';

    merge into TAX.sec_user_role a USING
    (select user_id, CASE WHEN c.ROLE_ID=14 THEN 5 ELSE c.ROLE_ID END AS role_id FROM sec_user_role c) b
    ON (a.user_id=b.user_id and a.role_id=b.role_id)
    when not matched then
			insert (user_id, role_id)
			values (b.user_id, b.role_id);
END;

