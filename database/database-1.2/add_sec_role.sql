declare 
  v_count number;
BEGIN
  select count(1) into v_count from user_synonyms where synonym_name='SEC_ROLE';
  IF v_count=0 THEN
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
  END IF;
END;
/