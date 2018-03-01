declare
	v_count number;
begin
	dbms_output.put_line('Re-create materialized view MV_FIAS_LOCALITY_ACT');
	select count(1) into v_count from user_mviews where mview_name='MV_FIAS_LOCALITY_ACT';
	if v_count>0 then
		execute immediate 'DROP MATERIALIZED VIEW MV_FIAS_LOCALITY_ACT';
	end if;
	
	select count(1) into v_count from user_mviews where mview_name='MV_FIAS_LOCALITY_ACT';
	if v_count=0 then
		dbms_output.put_line('Materialized view MV_FIAS_LOCALITY_ACT was dropped');
	end if;

	select count(1) into v_count from user_mviews where mview_name='MV_FIAS_LOCALITY_ACT';
	if v_count=0 then
		execute immediate 'CREATE MATERIALIZED VIEW MV_FIAS_LOCALITY_ACT (ID, AOID, FORMALNAME, SHORTNAME, REGIONCODE, LIVESTATUS, CURRSTATUS, AOLEVEL, POSTALCODE, PARENTGUID, FNAME, FTYPE, HAS_CHILD)
    NOLOGGING
    BUILD IMMEDIATE
    USING INDEX
    REFRESH COMPLETE ON DEMAND
    USING DEFAULT LOCAL ROLLBACK SEGMENT
    USING ENFORCED CONSTRAINTS DISABLE QUERY REWRITE
    AS select distinct f.id, f.aoid, f.formalname, f.shortname, f.regioncode, f.livestatus, f.currstatus, f.aolevel, f.postalcode, 
    nvl(d.parentguid,f.parentguid) parentguid,
       replace(lower(f.formalname),'' '','''') fname,
       trim(lower(f.shortname)) ftype,
       nvl2(c.id,1,0) has_child
    from fias_addrobj f left join fias_addrobj c on (c.parentguid=f.aoid and c.currstatus=0)
    left join fias_addrobj d on (f.parentguid=d.aoid and d.currstatus=0 and d.aolevel=5)
    where f.currstatus=0
    and f.aolevel in (6,90)';
	
		execute immediate 'CREATE INDEX IDX_MV_FIAS_LOCALITY_AOID ON MV_FIAS_LOCALITY_ACT (AOID)';
		execute immediate 'CREATE INDEX IDX_MV_FIAS_LOCALITY_FNAME ON MV_FIAS_LOCALITY_ACT (FNAME)';
		execute immediate 'CREATE INDEX IDX_MV_FIAS_LOCALITY_PGUID ON MV_FIAS_LOCALITY_ACT (PARENTGUID)';
		execute immediate 'CREATE INDEX SRCH_MV_FIAS_LOCAL_REG_FN_TP ON MV_FIAS_LOCALITY_ACT (REGIONCODE, FNAME, FTYPE, HAS_CHILD)';
		execute immediate 'CREATE UNIQUE INDEX PK_MV_FIAS_LOCALITY_ACT ON MV_FIAS_LOCALITY_ACT (ID)';

		execute immediate 'COMMENT ON COLUMN MV_FIAS_LOCALITY_ACT.ID IS ''Уникальный идентификатор''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_LOCALITY_ACT.AOID IS ''Идентификатор объекта''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_LOCALITY_ACT.FORMALNAME IS ''Формализованное наименование''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_LOCALITY_ACT.SHORTNAME IS ''Тип адресного объекта''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_LOCALITY_ACT.REGIONCODE IS ''Код региона''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_LOCALITY_ACT.LIVESTATUS IS ''Статус актуальности ФИАС: 1 - актуальная, 0 - устаревшая''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_LOCALITY_ACT.CURRSTATUS IS ''Статус актуальности КЛАДР: 0 - актуальная, ..''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_LOCALITY_ACT.AOLEVEL IS ''Уровень объекта''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_LOCALITY_ACT.POSTALCODE IS ''Почтовый индекс''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_LOCALITY_ACT.PARENTGUID IS ''Идентификатор родительского объекта''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_LOCALITY_ACT.FNAME IS ''Наименование, адаптированное для проверки адреса (строчными буквами, без пробелов)''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_LOCALITY_ACT.FTYPE IS ''Тип, адаптированный для проверки адреса (строчными буквами, без пробелов)''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_LOCALITY_ACT.HAS_CHILD IS ''Наличие дочерних элементов''';
		execute immediate 'COMMENT ON MATERIALIZED VIEW MV_FIAS_LOCALITY_ACT  IS ''Актуальные населенные пункты из справочника ФИАС''';
	end if;
	
	select count(1) into v_count from user_mviews where mview_name='MV_FIAS_LOCALITY_ACT';
	if v_count>0 then
		dbms_output.put_line('Materialized view MV_FIAS_LOCALITY_ACT was created');
	end if;
	
	dbms_output.put_line('Re-create materialized view MV_FIAS_STREET_ACT');
	select count(1) into v_count from user_mviews where mview_name='MV_FIAS_STREET_ACT';
	if v_count>0 then
		execute immediate 'DROP MATERIALIZED VIEW MV_FIAS_STREET_ACT';
	end if;
	
	select count(1) into v_count from user_mviews where mview_name='MV_FIAS_STREET_ACT';
	if v_count=0 then
		dbms_output.put_line('Materialized view MV_FIAS_STREET_ACT was dropped');
	end if;

	select count(1) into v_count from user_mviews where mview_name='MV_FIAS_STREET_ACT';
	if v_count=0 then
		execute immediate 'CREATE MATERIALIZED VIEW MV_FIAS_STREET_ACT (ID, AOID, FORMALNAME, SHORTNAME, REGIONCODE, LIVESTATUS, CURRSTATUS, AOLEVEL, POSTALCODE, PARENTGUID, FNAME, FTYPE, HAS_CHILD)
    NOLOGGING
    BUILD IMMEDIATE
    USING INDEX
    REFRESH COMPLETE ON DEMAND
    USING DEFAULT LOCAL ROLLBACK SEGMENT
    USING ENFORCED CONSTRAINTS DISABLE QUERY REWRITE
    AS select distinct f.id, f.aoid, f.formalname, f.shortname, f.regioncode, f.livestatus, f.currstatus, f.aolevel, f.postalcode, 
coalesce(e.parentguid,d.parentguid,f.parentguid) parentguid,
       replace(lower(f.formalname),'' '','''') fname,
       trim(lower(f.shortname)) ftype,
       nvl2(c.id,1,0) has_child
    from fias_addrobj f left join fias_addrobj c on (c.parentguid=f.aoid and c.currstatus=0)
    left join fias_addrobj d on (f.parentguid=d.aoid and d.currstatus=0 and d.aolevel in (5,65))
    left join fias_addrobj e on (d.parentguid=e.aoid and d.currstatus=0 and d.aolevel=65 and e.aolevel=5)
    where f.currstatus=0
    and f.aolevel in (7,91)';

		execute immediate 'CREATE UNIQUE INDEX PK_MV_FIAS_STREET_ACT ON MV_FIAS_STREET_ACT (ID)';
		execute immediate 'CREATE INDEX IDX_MV_FIAS_STREET_AOID ON MV_FIAS_STREET_ACT (AOID)';
		execute immediate 'CREATE INDEX IDX_MV_FIAS_STREET_FNAME ON MV_FIAS_STREET_ACT (FNAME)';
		execute immediate 'CREATE INDEX IDX_MV_FIAS_STREET_PGUID ON MV_FIAS_STREET_ACT (PARENTGUID)';
		execute immediate 'CREATE INDEX SRCH_MV_FIAS_STREET_REG_FN_TP ON MV_FIAS_STREET_ACT (REGIONCODE, FNAME, FTYPE, HAS_CHILD)';
		execute immediate 'CREATE INDEX IDX_MV_FIAS_STREET_POST ON MV_FIAS_STREET_ACT (POSTALCODE)';

		execute immediate 'COMMENT ON COLUMN MV_FIAS_STREET_ACT.ID IS ''Уникальный идентификатор''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_STREET_ACT.AOID IS ''Идентификатор объекта''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_STREET_ACT.FORMALNAME IS ''Формализованное наименование''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_STREET_ACT.SHORTNAME IS ''Тип адресного объекта''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_STREET_ACT.REGIONCODE IS ''Код региона''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_STREET_ACT.LIVESTATUS IS ''Статус актуальности ФИАС: 1 - актуальная, 0 - устаревшая''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_STREET_ACT.CURRSTATUS IS ''Статус актуальности КЛАДР: 0 - актуальная, ..''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_STREET_ACT.AOLEVEL IS ''Уровень объекта''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_STREET_ACT.POSTALCODE IS ''Почтовый индекс''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_STREET_ACT.PARENTGUID IS ''Идентификатор родительского объекта''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_STREET_ACT.FNAME IS ''Наименование, адаптированное для проверки адреса (строчными буквами, без пробелов)''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_STREET_ACT.FTYPE IS ''Тип, адаптированный для проверки адреса (строчными буквами, без пробелов)''';
		execute immediate 'COMMENT ON COLUMN MV_FIAS_STREET_ACT.HAS_CHILD IS ''Наличие дочерних элементов''';
		execute immediate 'COMMENT ON MATERIALIZED VIEW MV_FIAS_STREET_ACT  IS ''Актуальные улицы из справочника ФИАС''';
	end if;
	
	select count(1) into v_count from user_mviews where mview_name='MV_FIAS_STREET_ACT';
	if v_count>0 then
		dbms_output.put_line('Materialized view MV_FIAS_STREET_ACT was created');
	end if;
end;
/