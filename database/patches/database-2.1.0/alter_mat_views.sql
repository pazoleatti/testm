declare 
	v_count number;
begin
	select count(1) into v_count from user_mviews where mview_name='MV_FIAS_AREA_ACT';
	if v_count>0 then
		execute immediate 'DROP MATERIALIZED VIEW MV_FIAS_AREA_ACT';
	end if;
			
	select count(1) into v_count from user_mviews where mview_name='MV_FIAS_AREA_ACT';
	if v_count=0 then
		execute immediate 'CREATE MATERIALIZED VIEW MV_FIAS_AREA_ACT (ID, AOID, FORMALNAME, SHORTNAME, REGIONCODE, LIVESTATUS, CURRSTATUS, AOLEVEL, POSTALCODE, PARENTGUID, FNAME, FTYPE, HAS_CHILD)
    NOLOGGING
    BUILD IMMEDIATE
    USING INDEX 
    REFRESH COMPLETE ON DEMAND
    USING DEFAULT LOCAL ROLLBACK SEGMENT
    USING ENFORCED CONSTRAINTS DISABLE QUERY REWRITE
    AS select distinct f.*,
       replace(lower(f.formalname),'' '','''') fname,
       trim(lower(f.shortname)) ftype,
       nvl2(c.id,1,0) has_child
  from fias_addrobj f left join fias_addrobj c on (c.parentguid=f.aoid and c.currstatus=0)
  where f.currstatus=0
   and (f.aolevel=3 or
        f.aolevel=1 /*and replace(lower(f.formalname),'' '','''') not in (''москва'',''санкт-петербург'',''севастополь'',''байконур'')*/
        )';
		execute immediate 'CREATE INDEX IDX_MV_FIAS_AREA_AOID ON MV_FIAS_AREA_ACT (AOID)';
		execute immediate 'CREATE INDEX IDX_MV_FIAS_AREA_FNAME ON MV_FIAS_AREA_ACT (FNAME)';
		execute immediate 'CREATE INDEX IDX_MV_FIAS_AREA_PGUID ON MV_FIAS_AREA_ACT (PARENTGUID)';
		execute immediate 'CREATE INDEX SRCH_MV_FIAS_AREA_REG_FN_TP ON MV_FIAS_AREA_ACT (REGIONCODE, FNAME, FTYPE, HAS_CHILD)';
		execute immediate 'CREATE UNIQUE INDEX PK_MV_FIAS_AREA_ACT ON MV_FIAS_AREA_ACT (ID)';
    
		execute immediate 'COMMENT ON COLUMN MV_FIAS_AREA_ACT.ID IS ''Уникальный идентификатор''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_AREA_ACT.AOID IS ''Идентификатор объекта''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_AREA_ACT.FORMALNAME IS ''Формализованное наименование''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_AREA_ACT.SHORTNAME IS ''Тип адресного объекта''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_AREA_ACT.REGIONCODE IS ''Код региона''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_AREA_ACT.LIVESTATUS IS ''Статус актуальности ФИАС: 1 - актуальная, 0 - устаревшая''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_AREA_ACT.CURRSTATUS IS ''Статус актуальности КЛАДР: 0 - актуальная, ..''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_AREA_ACT.AOLEVEL IS ''Уровень объекта''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_AREA_ACT.POSTALCODE IS ''Почтовый индекс''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_AREA_ACT.PARENTGUID IS ''Идентификатор родительского объекта''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_AREA_ACT.FNAME IS ''Наименование, адаптированное для проверки адреса (строчными буквами, без пробелов)''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_AREA_ACT.FTYPE IS ''Тип, адаптированный для проверки адреса (строчными буквами, без пробелов)''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_AREA_ACT.HAS_CHILD IS ''Наличие дочерних элементов''';
		execute immediate 'COMMENT ON MATERIALIZED VIEW MV_FIAS_AREA_ACT  IS ''Актуальные районы из справочника ФИАС''';
	end if;
				
	select count(1) into v_count from user_mviews where mview_name='MV_FIAS_CITY_ACT';
	if v_count>0 then
		execute immediate 'DROP MATERIALIZED VIEW MV_FIAS_CITY_ACT';
	end if;
				
	select count(1) into v_count from user_mviews where mview_name='MV_FIAS_CITY_ACT';
	if v_count=0 then
		execute immediate 'CREATE MATERIALIZED VIEW MV_FIAS_CITY_ACT (ID, AOID, FORMALNAME, SHORTNAME, REGIONCODE, LIVESTATUS, CURRSTATUS, AOLEVEL, POSTALCODE, PARENTGUID, FNAME, FTYPE, HAS_CHILD)
    NOLOGGING
    BUILD IMMEDIATE
    USING INDEX 
    REFRESH COMPLETE ON DEMAND
    USING DEFAULT LOCAL ROLLBACK SEGMENT
    USING ENFORCED CONSTRAINTS DISABLE QUERY REWRITE
    AS select distinct f.*,
       replace(lower(f.formalname),'' '','''') fname,
       trim(lower(f.shortname)) ftype,
        nvl2(c.id,1,0) has_child
  from fias_addrobj f left join fias_addrobj c on (c.parentguid=f.aoid and c.currstatus=0)
  where f.currstatus=0
   and (f.aolevel=4 /*or
        f.aolevel=1 and replace(lower(f.formalname),'' '','''') in (''москва'',''санкт-петербург'',''севастополь'',''байконур'')*/
       )';
    
		execute immediate 'CREATE UNIQUE INDEX PK_MV_FIAS_CITY_ACT ON MV_FIAS_CITY_ACT (ID)';
		execute immediate 'CREATE INDEX IDX_MV_FIAS_CITY_AOID ON MV_FIAS_CITY_ACT (AOID)';
		execute immediate 'CREATE INDEX IDX_MV_FIAS_CITY_FNAME ON MV_FIAS_CITY_ACT (FNAME) ';
		execute immediate 'CREATE INDEX IDX_MV_FIAS_CITY_PGUID ON MV_FIAS_CITY_ACT (PARENTGUID) ';
		execute immediate 'CREATE INDEX SRCH_MV_FIAS_CITY_REG_FN_TP ON MV_FIAS_CITY_ACT (REGIONCODE, FNAME, FTYPE, HAS_CHILD)';
    
		execute immediate 'COMMENT ON COLUMN MV_FIAS_CITY_ACT.ID IS ''Уникальный идентификатор''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_CITY_ACT.AOID IS ''Идентификатор объекта''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_CITY_ACT.FORMALNAME IS ''Формализованное наименование''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_CITY_ACT.SHORTNAME IS ''Тип адресного объекта''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_CITY_ACT.REGIONCODE IS ''Код региона''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_CITY_ACT.LIVESTATUS IS ''Статус актуальности ФИАС: 1 - актуальная, 0 - устаревшая''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_CITY_ACT.CURRSTATUS IS ''Статус актуальности КЛАДР: 0 - актуальная, ..''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_CITY_ACT.AOLEVEL IS ''Уровень объекта''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_CITY_ACT.POSTALCODE IS ''Почтовый индекс''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_CITY_ACT.PARENTGUID IS ''Идентификатор родительского объекта''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_CITY_ACT.FNAME IS ''Наименование, адаптированное для проверки адреса (строчными буквами, без пробелов)''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_CITY_ACT.FTYPE IS ''Тип, адаптированный для проверки адреса (строчными буквами, без пробелов)''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_CITY_ACT.HAS_CHILD IS ''Наличие дочерних элементов''';
		execute immediate 'COMMENT ON MATERIALIZED VIEW MV_FIAS_CITY_ACT  IS ''Актуальные города из справочника ФИАС''';
	end if;
end;
/