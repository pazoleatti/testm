--------------------------------------------------------
--  File created - пятница-апреля-21-2017   
--------------------------------------------------------
---------------------------
--Changed TABLE
--REF_BOOK_TARIFF_PAYER
---------------------------
COMMENT ON COLUMN REF_BOOK_TARIFF_PAYER.FOR_OPS_DOP IS 'Используется для доп. тарифов ОПС';

---------------------------
--Changed TABLE
--REF_BOOK_REGION
---------------------------
-- ALTER TABLE REF_BOOK_REGION DROP CONSTRAINT CHK_REF_BOOK_REGION_OKTMO_DEF; -- *** Существует в дистрибутиве

---------------------------
--Changed TABLE
--REF_BOOK_PERSON
---------------------------
-- ALTER TABLE REF_BOOK_PERSON DROP CONSTRAINT CHK_REF_BOOK_PERSON_EMPLOYEE; -- *** Существует в дистрибутиве
-- ALTER TABLE REF_BOOK_PERSON DROP CONSTRAINT CHK_REF_BOOK_PERSON_MEDICAL; -- *** Существует в дистрибутиве
-- ALTER TABLE REF_BOOK_PERSON DROP CONSTRAINT CHK_REF_BOOK_PERSON_PENSION; -- *** Существует в дистрибутиве
-- ALTER TABLE REF_BOOK_PERSON DROP CONSTRAINT CHK_REF_BOOK_PERSON_SEX; -- *** Существует в дистрибутиве
-- ALTER TABLE REF_BOOK_PERSON DROP CONSTRAINT CHK_REF_BOOK_PERSON_SOCIAL; -- *** Существует в дистрибутиве

---------------------------
--Changed TABLE
--REF_BOOK_ID_DOC
---------------------------
-- ALTER TABLE REF_BOOK_ID_DOC DROP CONSTRAINT CHK_REF_BOOK_ID_DOC_REP; -- *** Существует в дистрибутиве

---------------------------
--Changed TABLE
--REF_BOOK_FORM_TYPE
---------------------------
ALTER TABLE REF_BOOK_FORM_TYPE MODIFY CONSTRAINT CHK_REF_BOOK_FORM_TYPE_TAXKIND ENABLE;

---------------------------
--Changed TABLE
--REF_BOOK_CALENDAR
---------------------------
-- ALTER TABLE REF_BOOK_CALENDAR DROP CONSTRAINT CHK_REF_BOOK_CAL_WORK; -- *** Существует в дистрибутиве
-- ALTER TABLE REF_BOOK_CALENDAR ADD CONSTRAINT CHK_REF_BOOK_CAL_CTYPE CHECK (ctype in (0,1)) ENABLE;  -- *** Существует в дистрибутиве
COMMENT ON COLUMN REF_BOOK_CALENDAR.CTYPE IS 'Рабочий/Выходной (0 - рабочий, 1 - выходной)';

---------------------------
--Changed TABLE
--REF_BOOK_ADDRESS
---------------------------
-- ALTER TABLE REF_BOOK_ADDRESS DROP CONSTRAINT CHK_REF_BOOK_ADDRESS_ADDR_N_RF; -- *** Существует в дистрибутиве
-- ALTER TABLE REF_BOOK_ADDRESS DROP CONSTRAINT CHK_REF_BOOK_ADDRESS_ADDR_RF; -- *** Существует в дистрибутиве

---------------------------
--Changed TABLE
--NDFL_PERSON_PREPAYMENT
---------------------------
ALTER TABLE NDFL_PERSON_PREPAYMENT DROP CONSTRAINT NDFL_PP_FK_S;
ALTER TABLE NDFL_PERSON_PREPAYMENT ADD CONSTRAINT NDFL_PP_FK_S FOREIGN KEY (SOURCE_ID) REFERENCES NDFL_PERSON_PREPAYMENT(ID) ON DELETE SET NULL ENABLE;

---------------------------
--Changed TABLE
--NDFL_PERSON_DEDUCTION
---------------------------
ALTER TABLE NDFL_PERSON_DEDUCTION DROP CONSTRAINT NDFL_PD_FK_S;
ALTER TABLE NDFL_PERSON_DEDUCTION ADD CONSTRAINT NDFL_PD_FK_S FOREIGN KEY (SOURCE_ID) REFERENCES NDFL_PERSON_DEDUCTION(ID) ON DELETE SET NULL ENABLE;

---------------------------
--New INDEX
--DECL_DATA_FILE_FK_DECL_DATA
---------------------------
--  CREATE INDEX DECL_DATA_FILE_FK_DECL_DATA ON DECLARATION_DATA_FILE (DECLARATION_DATA_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--FORM_DATA_SOURCE_FK_DEP_ID
---------------------------
  -- CREATE INDEX FORM_DATA_SOURCE_FK_DEP_ID ON FORM_DATA_SOURCE (DEPARTMENT_FORM_TYPE_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--FORM_DATA_REP_FK_FORM_DATA_ID
---------------------------
--  CREATE INDEX FORM_DATA_REP_FK_FORM_DATA_ID ON FORM_DATA_REPORT (FORM_DATA_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--SRCH_FSOCRBASE_LEV_SCNAME_N
---------------------------
--  CREATE INDEX SRCH_FSOCRBASE_LEV_SCNAME_N ON FIAS_SOCRBASE (LEV,TRIM(LOWER(SCNAME||'.'))); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--FK_RASCHSV_VYPLAT_IT_425_SUM
---------------------------
--  CREATE INDEX FK_RASCHSV_VYPLAT_IT_425_SUM ON RASCHSV_VYPLAT_IT_425 (RASCHSV_SV_SUM1_TIP_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--DECL_REPORT_FK_DECL_SUBREPORT
---------------------------
--  CREATE INDEX DECL_REPORT_FK_DECL_SUBREPORT ON DECLARATION_REPORT (SUBREPORT_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--FK_RASCHSV_OPS_OMS_R_SUM_TIP
---------------------------
--  CREATE INDEX FK_RASCHSV_OPS_OMS_R_SUM_TIP ON RASCHSV_OPS_OMS_RASCH_SUM (RASCHSV_SV_SUM1_TIP_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--FK_RASCHSV_SV_INO_GRAZD_SUM
---------------------------
--  CREATE INDEX FK_RASCHSV_SV_INO_GRAZD_SUM ON RASCHSV_SV_INO_GRAZD (RASCHSV_SV_SUM1_TIP_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--DECL_SUBREP_FK_DECL_TEMPLATE
---------------------------
--  CREATE INDEX DECL_SUBREP_FK_DECL_TEMPLATE ON DECLARATION_SUBREPORT (DECLARATION_TEMPLATE_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--DEPT_FORM_TYPE_PERF_FK_ID
---------------------------
--  CREATE INDEX DEPT_FORM_TYPE_PERF_FK_ID ON DEPARTMENT_FORM_TYPE_PERFORMER (DEPARTMENT_FORM_TYPE_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--FK_DEPT_DECL_TYPE_PERF_ID
---------------------------
--  CREATE INDEX FK_DEPT_DECL_TYPE_PERF_ID ON DEPARTMENT_DECL_TYPE_PERFORMER (DEPARTMENT_DECL_TYPE_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--DECL_REPORT_FK_DECL_DATA
---------------------------
--  CREATE INDEX DECL_REPORT_FK_DECL_DATA ON DECLARATION_REPORT (DECLARATION_DATA_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--SRCH_FSOCRBASE_LEV_SCNAME
---------------------------
--  CREATE INDEX SRCH_FSOCRBASE_LEV_SCNAME ON FIAS_SOCRBASE (LEV,TRIM(LOWER(SCNAME))); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--FK_RASCHSV_SVED_P_TARIF9_427
---------------------------
--  CREATE INDEX FK_RASCHSV_SVED_P_TARIF9_427 ON RASCHSV_SVED_PATENT (RASCHSV_SV_PRIM_TARIF9_427_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--FK_RASCHSV_OSS_VNM_KOL_TIP
---------------------------
--  CREATE INDEX FK_RASCHSV_OSS_VNM_KOL_TIP ON RASCHSV_OSS_VNM_KOL (RASCHSV_KOL_LIC_TIP_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--FK_RASCHSV_SV_P_M_KOL_TIP
---------------------------
--  CREATE INDEX FK_RASCHSV_SV_P_M_KOL_TIP ON RASCHSV_OPS_OMS_RASCH_KOL (RASCHSV_KOL_LIC_TIP_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--I_DECL_DATA_CONSOLIDATION_UNQ
---------------------------
--  CREATE UNIQUE INDEX I_DECL_DATA_CONSOLIDATION_UNQ ON DECLARATION_DATA_CONSOLIDATION (CASE  WHEN SOURCE_DECLARATION_DATA_ID IS NOT NULL THEN TARGET_DECLARATION_DATA_ID END ,SOURCE_DECLARATION_DATA_ID); -- *** Существует в дистрибутиве
-- 777
---------------------------
--New INDEX
--FK_PERS_SV_STRAH_FACE_DECLARAT
---------------------------
--  CREATE INDEX FK_PERS_SV_STRAH_FACE_DECLARAT ON RASCHSV_PERS_SV_STRAH_LIC (DECLARATION_DATA_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--NDFL_PD_FK_NP
---------------------------
--  CREATE INDEX NDFL_PD_FK_NP ON NDFL_PERSON_DEDUCTION (NDFL_PERSON_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--FORM_DATA_REF_BOOK_FK_REFBOOK
---------------------------
--  CREATE INDEX FORM_DATA_REF_BOOK_FK_REFBOOK ON FORM_DATA_REF_BOOK (REF_BOOK_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--IDX_FIAS_ADDROBJ_REG_LSTAT
---------------------------
--  CREATE INDEX IDX_FIAS_ADDROBJ_REG_LSTAT ON FIAS_ADDROBJ (REGIONCODE,LIVESTATUS); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--FORM_DATA_SOURCE_FK_SRC_DEP_ID
---------------------------
--  CREATE INDEX FORM_DATA_SOURCE_FK_SRC_DEP_ID ON FORM_DATA_SOURCE (SRC_DEPARTMENT_FORM_TYPE_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--NDFL_PP_FK_NP
---------------------------
--  CREATE INDEX NDFL_PP_FK_NP ON NDFL_PERSON_PREPAYMENT (NDFL_PERSON_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--FK_RASCHSV_VYPLAT_TARIF3_422
---------------------------
--  CREATE INDEX FK_RASCHSV_VYPLAT_TARIF3_422 ON RASCHSV_VYPLAT_IT_422 (RASCHSV_SV_PRIM_TARIF1_422_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--REF_BOOK_VALUE_FK_RECORD_ID
---------------------------
--  CREATE INDEX REF_BOOK_VALUE_FK_RECORD_ID ON REF_BOOK_VALUE (RECORD_ID); -- *** Существует в дистрибутиве
---------------------------
--Changed INDEX
--IDX_FIAS_SOCRBASE_LEV
---------------------------
DROP INDEX IDX_FIAS_SOCRBASE_LEV;
  CREATE INDEX IDX_FIAS_SOCRBASE_LEV ON FIAS_SOCRBASE (LEV,INSTR(SCNAME,'.'));
---------------------------
--New INDEX
--FK_RASCHSV_OSS_VNM_KOL_OSS
---------------------------
--  CREATE INDEX FK_RASCHSV_OSS_VNM_KOL_OSS ON RASCHSV_OSS_VNM_KOL (RASCHSV_OSS_VNM_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--FK_RASCHSV_VYPLAT_TARIF2_425
---------------------------
--  CREATE INDEX FK_RASCHSV_VYPLAT_TARIF2_425 ON RASCHSV_VYPLAT_IT_425 (RASCHSV_SV_PRIM_TARIF2_425_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--LOCK_DATA_SUBSCR_FK_SEC_USER
---------------------------
--  CREATE INDEX LOCK_DATA_SUBSCR_FK_SEC_USER ON LOCK_DATA_SUBSCRIBERS (USER_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--FK_RASCHSV_VYPLAT_TARIF9_427
---------------------------
--  CREATE INDEX FK_RASCHSV_VYPLAT_TARIF9_427 ON RASCHSV_VYPLAT_IT_427 (RASCHSV_SV_PRIM_TARIF9_427_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--FK_RASCHSV_VYPLAT_IT_427_SUM
---------------------------
--  CREATE INDEX FK_RASCHSV_VYPLAT_IT_427_SUM ON RASCHSV_VYPLAT_IT_427 (RASCHSV_SV_SUM1_TIP_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--FORM_DATA_REF_BOOK_FK_FORMDATA
---------------------------
--  CREATE INDEX FORM_DATA_REF_BOOK_FK_FORMDATA ON FORM_DATA_REF_BOOK (FORM_DATA_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--DECL_SOURCE_FK_DEPT_FORMTYPE
---------------------------
--  CREATE INDEX DECL_SOURCE_FK_DEPT_FORMTYPE ON DECLARATION_SOURCE (SRC_DEPARTMENT_FORM_TYPE_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--FK_RASCHSV_I_GRAZD_TARIF2_425
---------------------------
--  CREATE INDEX FK_RASCHSV_I_GRAZD_TARIF2_425 ON RASCHSV_SV_INO_GRAZD (RASCHSV_SV_PRIM_TARIF2_425_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--NDFL_PERSON_FK_D
---------------------------
--  CREATE INDEX NDFL_PERSON_FK_D ON NDFL_PERSON (DECLARATION_DATA_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--DECL_SOURCE_FK_DEPT_DECLTYPE
---------------------------
--  CREATE INDEX DECL_SOURCE_FK_DEPT_DECLTYPE ON DECLARATION_SOURCE (DEPARTMENT_DECLARATION_TYPE_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--FK_RASCHSV_SVED_PATENT_SUM
---------------------------
--  CREATE INDEX FK_RASCHSV_SVED_PATENT_SUM ON RASCHSV_SVED_PATENT (RASCHSV_SV_SUM1_TIP_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--FK_RASCHSV_OSS_VNM_SUM_OSS
---------------------------
--  CREATE INDEX FK_RASCHSV_OSS_VNM_SUM_OSS ON RASCHSV_OSS_VNM_SUM (RASCHSV_OSS_VNM_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--FK_RASCHSV_OSS_VNM_SUM_TIP
---------------------------
--  CREATE INDEX FK_RASCHSV_OSS_VNM_SUM_TIP ON RASCHSV_OSS_VNM_SUM (RASCHSV_SV_SUM1_TIP_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--FORM_DATA_FILE_FK_FORM_DATA
---------------------------
--  CREATE INDEX FORM_DATA_FILE_FK_FORM_DATA ON FORM_DATA_FILE (FORM_DATA_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--CONFIGURATION_FK
---------------------------
--  CREATE INDEX CONFIGURATION_FK ON CONFIGURATION (DEPARTMENT_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--FK_RASCHSV_SV_OPS_OMS_KOL
---------------------------
--  CREATE INDEX FK_RASCHSV_SV_OPS_OMS_KOL ON RASCHSV_OPS_OMS_RASCH_KOL (RASCHSV_OPS_OMS_RASCH_KOL_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--FK_RASCHSV_SV_OPS_OMS_SUM
---------------------------
--  CREATE INDEX FK_RASCHSV_SV_OPS_OMS_SUM ON RASCHSV_OPS_OMS_RASCH_SUM (RASCHSV_OPS_OMS_RASCH_SUM_ID); -- *** Существует в дистрибутиве
---------------------------
--New INDEX
--FK_RASCHSV_VYPLAT_IT_422_SUM
---------------------------
--  CREATE INDEX FK_RASCHSV_VYPLAT_IT_422_SUM ON RASCHSV_VYPLAT_IT_422 (RASCHSV_SV_SUM1_TIP_ID); -- *** Существует в дистрибутиве
---------------------------
--Changed PACKAGE BODY
--FIAS_PKG
---------------------------
create or replace package fias_pkg as
-- Пакет для поиска адресов в справочнике ФИАС

  cursor fias_addrs(c_region varchar2,c_area varchar2,c_city varchar2,c_locality varchar2,c_street varchar2,
                    c_area_type varchar2,c_city_type varchar2,c_locality_type varchar2,c_street_type varchar2,
                    c_index varchar2) is
    /*поиск по всем параметрам*/
    select distinct c.id,c.regioncode,c.formalname,c.shortname,c.aolevel,c.currstatus,c.postalcode,
           substr(sys_connect_by_path(formalname,','),2) full_addr,
           connect_by_isleaf isleaf
      from fias_addrobj c
     where c.currstatus=0 --c.livestatus=1
       and c.regioncode=c_region
       and replace(lower(c.formalname),' ','')=nvl(replace(lower(c_street),' ',''),replace(lower(nvl(c_locality,c_city)),' ',''))
       and (nvl(trim(lower(c_street_type)),trim(lower(nvl(c_locality_type,c_city_type)))) is null or 
            trim(lower(c.shortname))=nvl(trim(lower(c_street_type)),trim(lower(nvl(c_locality_type,c_city_type)))))
       and (c.postalcode=c_index)
    connect by prior c.aoid=c.parentguid
      start with c.currstatus=0 --c.livestatus=1
        and c.regioncode=c_region
        and replace(lower(c.formalname),' ','')=nvl(replace(lower(c_area),' ',''),replace(lower(c_city),' ',''))
        and (nvl(replace(lower(c_area_type),' ',''),replace(lower(c_city_type),' ','')) is null or 
             trim(lower(c.shortname))=nvl(replace(lower(c_area_type),' ',''),replace(lower(c_city_type),' ','')))
    union
    /*поиск без учета почтового индекса*/
    select distinct c.id,c.regioncode,c.formalname,c.shortname,c.aolevel,c.currstatus,c.postalcode,
           substr(sys_connect_by_path(formalname,','),2) full_addr,
           connect_by_isleaf isleaf
      from fias_addrobj c
     where c.currstatus=0 --c.livestatus=1
       and c.regioncode=c_region
       and replace(lower(c.formalname),' ','')=nvl(replace(lower(c_street),' ',''),replace(lower(nvl(c_locality,c_city)),' ',''))
       and (nvl(trim(lower(c_street_type)),trim(lower(nvl(c_locality_type,c_city_type)))) is null or 
            trim(lower(c.shortname))=nvl(trim(lower(c_street_type)),trim(lower(nvl(c_locality_type,c_city_type)))))
    connect by prior c.aoid=c.parentguid
      start with c.currstatus=0 --c.livestatus=1
        and c.regioncode=c_region
        and replace(lower(c.formalname),' ','')=nvl(replace(lower(c_area),' ',''),replace(lower(c_city),' ',''))
        and (nvl(replace(lower(c_area_type),' ',''),replace(lower(c_city_type),' ','')) is null or 
             trim(lower(c.shortname))=nvl(replace(lower(c_area_type),' ',''),replace(lower(c_city_type),' ','')))
    union
    /*поиск без учета типов объектов*/
    select distinct c.id,c.regioncode,c.formalname,c.shortname,c.aolevel,c.currstatus,c.postalcode,
           substr(sys_connect_by_path(formalname,','),2) full_addr,
           connect_by_isleaf isleaf
      from fias_addrobj c
     where c.currstatus=0 --c.livestatus=1
       and c.regioncode=c_region
       and replace(lower(c.formalname),' ','')=nvl(replace(lower(c_street),' ',''),replace(lower(nvl(c_locality,c_city)),' ',''))
    connect by prior c.aoid=c.parentguid
      start with c.currstatus=0 --c.livestatus=1
        and c.regioncode=c_region
        and replace(lower(c.formalname),' ','')=nvl(replace(lower(c_area),' ',''),replace(lower(c_city),' ',''))
        and (nvl(replace(lower(c_area_type),' ',''),replace(lower(c_city_type),' ','')) is null or 
             trim(lower(c.shortname))=nvl(replace(lower(c_area_type),' ',''),replace(lower(c_city_type),' ','')));
  
  cursor fs_fias_addrs(c_region varchar2,c_area varchar2,c_city varchar2,c_locality varchar2,c_street varchar2,
                    c_area_type varchar2,c_city_type varchar2,c_locality_type varchar2,c_street_type varchar2,
                    c_index varchar2) is
        select street.id street_id,street.regioncode,street.formalname street_fname,street.shortname street_type,
               locality.id locality_id,locality.formalname locality_fname,
               city.id city_id,city.formalname city_fname,
               area.id area_id,area.formalname area_fname,
               nvl2(area.formalname,area.formalname||',','')||nvl2(city.formalname,city.formalname||',','')||
               nvl2(locality.formalname,locality.formalname||',','')||nvl2(street.formalname,street.formalname||',','') full_addr
          from mv_fias_street_act street left join mv_fias_locality_act locality on (locality.aoid=street.parentguid 
                                                                                     and locality.regioncode=c_region 
                                                                                     and locality.fname=c_locality
                                                                                     and locality.ftype=c_locality_type
                                                                                    )
                                   left join mv_fias_city_act city on (city.aoid=nvl2(c_locality,locality.parentguid,street.parentguid) 
                                                                       and city.regioncode=c_region 
                                                                       and city.fname=c_city
                                                                       and city.ftype=nvl(c_city_type,'г')
                                                                      )
                                   left join mv_fias_area_act area on (area.aoid=nvl(nvl(locality.parentguid,city.parentguid),street.parentguid) 
                                                                       and area.regioncode=c_region 
                                                                       and area.fname=c_area
                                                                       and area.ftype=c_area_type
                                                                      )
         where street.regioncode=c_region
           and street.fname=nvl(c_street,c_locality)
           and street.ftype=nvl(c_street_type,c_locality_type)
           and street.has_child=0;
                    
                    
  type ref_cursor is ref cursor;
  
  type TCheckAddrByFias is record
  (
    id ndfl_person.id%type,
    post_index ndfl_person.post_index%type,
    region_code ndfl_person.region_code%type,
    area ndfl_person.area%type,
    city ndfl_person.city%type,
    locality ndfl_person.locality%type,
    street ndfl_person.street%type,
    ndfl_full_addr    varchar2(2000 char),
    area_type         varchar2(10 char),
    area_fname        varchar2(200 char),
    city_type         varchar2(10 char),
    city_fname        varchar2(200 char),
    loc_type          varchar2(10 char),
    loc_fname         varchar2(200 char),
    street_type       varchar2(10 char),
    street_fname      varchar2(200 char),
    fias_id           fias_addrobj.id%type,
    fias_index        fias_addrobj.postalcode%type,
    fias_street       fias_addrobj.formalname%type,
    fias_street_type  fias_addrobj.shortname%type,
    fias_city_id      fias_addrobj.id%type,
    fias_city_name    fias_addrobj.formalname%type,
    chk_index         number,
    chk_region        number,
    chk_area          number,
    chk_city          number,
    chk_loc           number,
    chk_street        number
  );

  type TCheckExistsAddrByFias is record
  (
    id              ndfl_person.id%type,
    post_index      ndfl_person.post_index%type,
    region_code     ndfl_person.region_code%type,
    area            ndfl_person.area%type,
    city            ndfl_person.city%type,
    locality        ndfl_person.locality%type,
    street          ndfl_person.street%type,
    ndfl_full_addr  varchar2(2000 char),
    area_type       varchar2(10 char),
    area_fname      varchar2(200 char),
    city_type       varchar2(10 char),
    city_fname      varchar2(200 char),
    loc_type        varchar2(10 char),
    loc_fname       varchar2(200 char),
    street_type     varchar2(10 char),
    street_fname    varchar2(200 char),
    chk_index       number,
    chk_region      number,
    chk_area        number,
    chk_city        number,
    chk_loc         number,
    chk_street      number
  );

  type TTblFiasAddr is table of fias_addrs%rowtype;
  type TTblFiasAddrFS is table of fs_fias_addrs%rowtype;
  type TTblCheckAddrByFias is table of TCheckAddrByFias;
  type TTblCheckExistsAddrByFias is table of TCheckExistsAddrByFias;
  --------------------------------------------------------------------------------------------------------------
  -- внутренние функции
  --------------------------------------------------------------------------------------------------------------
  -- Распарсить название элемента адреса
  procedure ParseElement(p_lev number,p_name_src varchar2,p_add_lev number,p_type out varchar2,p_name out varchar2);
  -- Получить наименование элемента
  function GetParseName(p_lev number,p_name_src varchar2,p_add_lev number default 0) return varchar2;
  -- Получить Тип элемента
  function GetParseType(p_lev number,p_name_src varchar2,p_add_lev number default 0) return varchar2;
  --------------------------------------------------------------------------------------------------------------
  -- внешние функции
  --------------------------------------------------------------------------------------------------------------
  -- Получить все подходящие адреса из ФИАС
  function GetFiasAddrs(p_region varchar2,p_area varchar2,p_city varchar2,p_locality varchar2,p_street varchar2,
                        p_area_type varchar2,p_city_type varchar2,p_locality_type varchar2,p_street_type varchar2,
                        p_post_index varchar2) return TTblFiasAddr pipelined;

  -- Получить все подходящие адреса из ФИАС фиксированной структуры, без иерархии
  function GetFiasAddrsFS(p_region varchar2,p_area varchar2,p_city varchar2,p_locality varchar2,p_street varchar2,
                        p_area_type varchar2,p_city_type varchar2,p_locality_type varchar2,p_street_type varchar2,
                        p_post_index varchar2) return TTblFiasAddrFS pipelined;
  
  -- Проверить существование элемента адреса с учетом родительского элемента
  -- p_check_type: AREA,CITY,LOCALITY,STREET
  function CheckAddrElement(p_region varchar2,p_check_element varchar2,p_parent_element varchar2,p_check_type varchar2 default '',p_leaf number default 1) return number;

  -- Выполнить проверку на полное совпадение с ФИАС адресов, указанных в декларации
  -- возвращается курсор на таблицу типа TTblCheckAddrByFias
  -- для записей, по которым не установлен адрес в ФИАС, выполняется проверка наличия/отсутствия в ФИАС элементов адресов
  function CheckAddrByFiasR(p_declaration number,p_check_type number default 0) return ref_cursor;

  -- Выполнить проверку на полное совпадение с ФИАС адресов, указанных в декларации
  -- возвращается курсор на таблицу типа TTblCheckAddrByFias
  -- для записей, по которым не установлен адрес в ФИАС, выполняется проверка наличия/отсутствия в ФИАС элементов адресов
  -- проверка выполняется по функции GetFiasAddrsFS
  -- p_check_type(тип проверки наличия элементов адреса в ФИАС): 1 - проверяется полная цепочка родительских элементов, 0 - проверяется только непосредственный родитель
  function CheckAddrByFias(p_declaration number,p_check_type number default 0) return ref_cursor;

  -- Выполнить проверку на наличие/отсутствие в ФИАС элементов адресов, указанных в декларации
  -- возвращается курсор на таблицу типа TTblCheckExistsAddrByFias
  function CheckExistsAddrByFias(p_declaration number,p_check_type number default 0) return ref_cursor;
  
  -- Обновление мат. представлений
  procedure RefreshViews;
  
end fias_pkg;
/
show errors;

create or replace package body fias_pkg as
  
  cursor ndfl_rec(c_ndfl number) is
    select n.id,
           n.post_index,n.region_code,n.area,n.city,n.locality,n.street,
           case when instr(lower(n.area)||' ','р-н. ')>0 then substr(n.area||' ',instr(lower(n.area)||' ','р-н. '),4)
                else ''
           end area_type,
           case when instr(lower(n.area),' р-н.')>0 then trim(substr(n.area,1,instr(lower(n.area),'р-н.')-1))
                else n.city
           end area_fname,
           case when instr(lower(n.city)||' ','г ')>0 then substr(n.city||' ',instr(lower(n.city)||' ','г '),2) 
                when instr(lower(n.city)||' ','тер. ')>0 then substr(n.city||' ',instr(lower(n.city)||' ','тер. '),4)
                else ''
           end city_type,
           case when instr(lower(n.city),'г ')>0 then trim(substr(n.city,instr(lower(n.city),'г '),length(n.city)-2))
                when instr(lower(n.city),' г')>0 then trim(substr(n.city,1,instr(lower(n.city),' г')-1))
                when instr(lower(n.city),'тер. ')>0 then trim(substr(n.city,instr(lower(n.city),'тер. ')+4))
                else n.city
           end city_fname,
           case when instr(lower(n.street)||' ','ул ')>0 then substr(n.street||' ',instr(lower(n.street)||' ','ул '),3) 
                when instr(lower(n.street)||' ','ул. ')>0 then substr(n.street||' ',instr(lower(n.street)||' ','ул. '),4) 
                else ''
           end street_type,
           case when instr(lower(n.street),'ул ')>0 then trim(substr(n.street,instr(lower(n.street),'ул ')+2))
                when instr(lower(n.street),' ул')>0 then trim(substr(n.street,1,instr(lower(n.street),' ул')-1))
                when instr(lower(n.street),'ул. ')>0 then trim(substr(n.street,instr(lower(n.street),'ул. ')+3))
                when instr(lower(n.street),' ул.')>0 then trim(substr(n.street,1,instr(lower(n.street),' ул.')-1))
                else n.street
           end street_fname,
           case when instr(lower(n.locality)||' ','г ')>0 then substr(n.locality||' ',instr(lower(n.locality)||' ','г '),2) 
                when instr(lower(n.locality)||' ','тер. ')>0 then substr(n.locality||' ',instr(lower(n.locality)||' ','тер. '),4)
                when instr(lower(n.locality)||' ','аул. ')>0 then substr(n.locality||' ',instr(lower(n.locality)||' ','аул. '),5)
                else ''
           end loc_type,
           case when instr(lower(n.locality),'г ')>0 then trim(substr(n.locality,instr(lower(n.locality),'г '),length(n.locality)-2))
                when instr(lower(n.locality),' г')>0 then trim(substr(n.locality,1,instr(lower(n.locality),' г')-1))
                when instr(lower(n.locality),'тер. ')>0 then trim(substr(n.locality,instr(lower(n.locality),'тер. ')+4))
                when instr(lower(n.locality),'аул. ')>0 then trim(substr(n.locality,instr(lower(n.locality),'аул. ')+4))
                else n.locality
           end loc_fname
      from ndfl_person n
     where n.id=c_ndfl;

    v_check_path boolean:=false;
  --------------------------------------------------------------------------------------------------------------
  -- внутренние функции
  --------------------------------------------------------------------------------------------------------------
  -- Распарсить название элемента адреса
  --------------------------------------------------------------------------------------------------------------
  procedure ParseElement(p_lev number,p_name_src varchar2,p_add_lev number,p_type out varchar2,p_name out varchar2)
  is
    v_name_src varchar2(500 char):=p_name_src;
    v_name varchar2(500 char):='';
    v_type varchar2(10 char):='';
    v_char varchar2(1 char):=' ';
    v_str varchar2(200 char);
    v_srch varchar2(200 char);
    p number:=1;
    pp number:=1;
begin
  if v_name_src is not null then
      -- делим строку по пробелам
      p:=instr(v_name_src||v_char,v_char);
      while p>0 loop
        v_str:=substr(v_name_src||v_char,pp,p-pp);
        -- каждую часть сравниваем с типами элементов адреса
        begin
          if substr(v_str,-1)='.' then
            v_srch:=substr(v_str,1,length(v_str)-1);
          else
            v_srch:=v_str;
          end if;
          select trim(lower(v_str)) into v_type from fias_socrbase scr where scr.lev in (p_lev,p_add_lev) and trim(lower(scr.scname))=trim(lower(v_srch)) and rownum=1;
          if (instr(lower(v_name_src),' '||lower(v_type))>0) then
            v_name:=trim(substr(v_name_src,1,instr(lower(v_name_src),' '||lower(v_type))-1));
          elsif (instr(lower(v_name_src),lower(v_type)||' ')>0) then
            v_name:=trim(substr(v_name_src,instr(lower(v_name_src),lower(v_type)||' ')+length(v_type||' ')-1));
          end if;
          exit;
        exception when no_data_found then
          null;
        end;
        pp:=p;
        p:=instr(v_name_src||v_char,v_char,p+1);
      end loop;
    end if;
    p_type:=v_type;
    p_name:=v_name;
  exception when others then
    p_type:='';
    p_name:='';
  end;
  --------------------------------------------------------------------------------------------------------------
  -- Получить наименование элемента
  --------------------------------------------------------------------------------------------------------------
  function GetParseName(p_lev number,p_name_src varchar2,p_add_lev number default 0) return varchar2
  is 
    v_name varchar2(200 char);
    v_type varchar2(10 char);
  begin
    ParseElement(p_lev,p_name_src,p_add_lev,v_type,v_name);
    return nvl(v_name,p_name_src);
  end;
  --------------------------------------------------------------------------------------------------------------
  -- Получить Тип элемента
  --------------------------------------------------------------------------------------------------------------
  function GetParseType(p_lev number,p_name_src varchar2,p_add_lev number default 0) return varchar2
  is
    v_name varchar2(200 char);
    v_type varchar2(10 char);
  begin
    ParseElement(p_lev,p_name_src,p_add_lev,v_type,v_name);
    return v_type;
  end;
  --------------------------------------------------------------------------------------------------------------
  -- внешние функции
  --------------------------------------------------------------------------------------------------------------
  -- Получить все подходящие адреса из ФИАС
  -------------------------------------------------------------------------------------------------------------
  function GetFiasAddrs(p_region varchar2,p_area varchar2,p_city varchar2,p_locality varchar2,p_street varchar2,
                        p_area_type varchar2,p_city_type varchar2,p_locality_type varchar2,p_street_type varchar2,
                        p_post_index varchar2) return TTblFiasAddr pipelined 
  is
    tbl TTblFiasAddr:=TTblFiasAddr();
  begin
    if fias_addrs%isopen then
      close fias_addrs;
    end if;
    open fias_addrs(p_region,p_area,p_city,p_locality,p_street,
                    p_area_type,p_city_type,p_locality_type,p_street_type,p_post_index);
    fetch fias_addrs bulk collect into tbl;
    close fias_addrs;
    
    if tbl.count>0 then
      for i in 1..tbl.count loop
        if tbl.exists(i) then
          pipe row (tbl(i));
        end if;
      end loop;
    end if;
   end GetFiasAddrs;

  --------------------------------------------------------------------------------------------------------------
  -- Получить все подходящие адреса из ФИАС фиксированной структуры, без иерархии
  -------------------------------------------------------------------------------------------------------------
  function GetFiasAddrsFS(p_region varchar2,p_area varchar2,p_city varchar2,p_locality varchar2,p_street varchar2,
                        p_area_type varchar2,p_city_type varchar2,p_locality_type varchar2,p_street_type varchar2,
                        p_post_index varchar2) return TTblFiasAddrFS pipelined 
  is
    tbl TTblFiasAddrFS:=TTblFiasAddrFS();
  begin
    if fs_fias_addrs%isopen then
      close fs_fias_addrs;
    end if;
    open fs_fias_addrs(p_region,p_area,p_city,p_locality,p_street,
                    p_area_type,p_city_type,p_locality_type,p_street_type,p_post_index);
    fetch fs_fias_addrs bulk collect into tbl;
    close fs_fias_addrs;
    
    if tbl.count>0 then
      for i in 1..tbl.count loop
        if tbl.exists(i) then
          pipe row (tbl(i));
        end if;
      end loop;
    end if;
   end GetFiasAddrsFS;

  -------------------------------------------------------------------------------------------------------------
  -- Проверить существование элемента адреса с учетом родительского элемента
  -- p_check_type: AREA,CITY,LOCALITY,STREET
  -------------------------------------------------------------------------------------------------------------
  function CheckAddrElement(p_region varchar2,p_check_element varchar2,p_parent_element varchar2,
                            p_check_type varchar2 default '',p_leaf number default 1) return number
  is
    v_result number:=0;
  begin
    if v_check_path then
      select decode(count(*),0,0,1) into v_result
        from (
              select c.id,c.regioncode,c.formalname,c.shortname,c.aolevel,c.currstatus,c.postalcode,
                     substr(sys_connect_by_path(formalname,';'),2) full_addr,
                     connect_by_isleaf isleaf
                from (select * from fias_addrobj t
                      where t.regioncode=p_region
                        and t.livestatus=1
                     ) c
               start with c.parentguid is null 
             connect by prior c.aoid=c.parentguid
             ) f 
       where replace(lower(f.formalname),' ','')=replace(lower(p_check_element),' ','')
         and instr(lower(full_addr),lower(p_parent_element))>0
         and isleaf=p_leaf;
    else
      if (p_check_type='AREA') then
        select decode(count(*),0,0,1) into v_result
          from mv_fias_area_act a
         where a.regioncode=p_region
           and a.fname=nvl(replace(lower(p_check_element),' ',''),'-')
           and a.has_child=decode(p_leaf,1,0,1);
      elsif (p_check_type='CITY') then
        select decode(count(*),0,0,1) into v_result
          from mv_fias_city_act c left join mv_fias_area_act a on (a.parentguid=c.aoid)
         where c.regioncode=p_region
           and c.fname=nvl(replace(lower(p_check_element),' ',''),'-')
           and (nvl(replace(lower(p_parent_element),' ',''),'-')='-' or 
                a.fname=nvl(replace(lower(p_parent_element),' ',''),'-'))
           and c.has_child=decode(p_leaf,1,0,1);
      elsif (p_check_type='LOCALITY') then
        select decode(sum(cnt),0,0,1) into v_result
          from (
                select count(*) cnt
                  from mv_fias_locality_act l left join mv_fias_city_act c on (c.parentguid=l.aoid)
                 where l.regioncode=p_region
                   and l.fname=nvl(replace(lower(p_check_element),' ',''),'-')
                   and c.fname=nvl(replace(lower(p_parent_element),' ',''),'-')
                   and l.has_child=decode(p_leaf,1,0,1)
                union 
                select count(*) cnt
                  from mv_fias_locality_act l left join mv_fias_area_act a on (a.parentguid=l.aoid)
                 where l.regioncode=p_region
                   and l.fname=nvl(replace(lower(p_check_element),' ',''),'-')
                   and a.fname=nvl(replace(lower(p_parent_element),' ',''),'-')
                   and l.has_child=decode(p_leaf,1,0,1)
              );
      elsif (p_check_type='STREET') then
        select decode(sum(cnt),0,0,1) into v_result
          from (
                select count(*) cnt
                  from mv_fias_street_act s left join mv_fias_city_act c on (c.parentguid=s.aoid)
                 where s.regioncode=p_region
                   and s.fname=nvl(replace(lower(p_check_element),' ',''),'-')
                   and c.fname=nvl(replace(lower(p_parent_element),' ',''),'-')
                   and s.has_child=decode(p_leaf,1,0,1)
                union 
                select count(*) cnt
                  from mv_fias_street_act s left join mv_fias_locality_act l on (l.parentguid=s.aoid)
                 where s.regioncode=p_region
                   and s.fname=nvl(replace(lower(p_check_element),' ',''),'-')
                   and l.fname=nvl(replace(lower(p_parent_element),' ',''),'-')
                   and s.has_child=decode(p_leaf,1,0,1)
                union 
                select count(*) cnt
                  from mv_fias_street_act s left join mv_fias_area_act a on (a.parentguid=s.aoid)
                 where s.regioncode=p_region
                   and s.fname=nvl(replace(lower(p_check_element),' ',''),'-')
                   and a.fname=nvl(replace(lower(p_parent_element),' ',''),'-')
                   and s.has_child=decode(p_leaf,1,0,1)
              );
      end if;
      /*select decode(count(*),0,0,1) into v_result
        from fias_addrobj f left join fias_addrobj p on (p.aoid=f.parentguid and p.currstatus=0 and p.regioncode=p_region)
       where f.currstatus=0
         and f.regioncode=p_region
         and replace(lower(f.formalname),' ','')=replace(lower(p_check_element),' ','')
         and ((nvl(replace(lower(p_parent_element),' ',''),'-')='-' and f.parentguid is null) or 
              (nvl(replace(lower(p_parent_element),' ',''),'-')<>'-' and replace(lower(p.formalname),' ','')=replace(lower(p_parent_element),' ',''))
             )
         and (p_leaf=0 and exists(select 1 from fias_addrobj c where c.parentguid=f.aoid) or
              p_leaf=1 and not exists(select 1 from fias_addrobj c where c.parentguid=f.aoid) 
             );*/
    end if;
    return v_result;
  end;

  -------------------------------------------------------------------------------------------------------------
  -- Выполнить проверку на полное совпадение с ФИАС адресов, указанных в декларации
  -- возвращается курсор на таблицу типа TTblCheckAddrByFias
  -------------------------------------------------------------------------------------------------------------
  function CheckAddrByFiasR(p_declaration number,p_check_type number default 0) return ref_cursor
  is
    v_ref ref_cursor;
  begin
    v_check_path:=(p_check_type=1);
    open v_ref for
      select n.id,n.post_index,n.region_code,n.area,n.city,n.locality,n.street,
             n.ndfl_full_addr,n.area_type,n.area_fname,n.city_type,n.city_fname,n.loc_type,n.loc_fname,n.street_type,n.street_fname,
             f.id fias_id,
             f.postalcode fias_index,
             f.formalname fias_street,
             f.shortname fias_street_type,
             fc.id fias_city_id,
             fc.formalname fias_city_name,
             (select decode(count(*),0,0,1) 
                from fias_addrobj f 
               where f.regioncode=n.region_code 
                 and replace(f.postalcode,' ','')=n.post_index) chk_index,
             (select decode(count(*),0,0,1) 
                from fias_addrobj f 
               where f.regioncode=n.region_code) chk_region,
             case when n.fa_id is null then fias_pkg.CheckAddrElement(n.region_code,n.area_fname,',','AREA',0) 
                  else 1
             end chk_area,
             case when n.fa_id is null then fias_pkg.CheckAddrElement(n.region_code,n.city_fname,decode(p_check_type,1,nvl2(n.area_fname,n.area_fname||';',';'),n.area_fname),'CITY',n.city_leaf) 
                  else 1
             end chk_city,
             case when n.fa_id is null then fias_pkg.CheckAddrElement(n.region_code,n.loc_fname,decode(p_check_type,1,nvl2(n.area_fname,n.area_fname||';',';')||nvl2(n.city_fname,n.city_fname||';',''),nvl(n.city_fname,n.area_fname)),'LOCALITY',n.loc_leaf) 
                  else 1
             end chk_loc,
             case when n.fa_id is null then fias_pkg.CheckAddrElement(n.region_code,n.street_fname,decode(p_check_type,1,nvl2(n.area_fname,n.area_fname||';','')||nvl2(n.city_fname,n.city_fname||';','')||nvl2(n.loc_fname,n.loc_fname||';',''),nvl(n.loc_fname,n.city_fname)),'STREET',1) 
                  else 1
             end chk_street
        from (
              select tab.*,
                     nvl2(tab.area_fname,tab.area_fname||',','')||nvl2(tab.city_fname,tab.city_fname||',','')||nvl2(tab.loc_fname,tab.loc_fname||',','')||
                     nvl2(tab.street_fname,tab.street_fname||',','') ndfl_full_addr,
                     (select min(id)
                        from table(fias_pkg.GetFiasAddrs(tab.region_code,trim(lower(tab.area_fname)),trim(lower(tab.city_fname)),trim(lower(tab.loc_fname)),trim(lower(tab.street_fname)),
                                                         trim(lower(tab.area_type)),trim(lower(tab.city_type)),trim(lower(tab.loc_type)),trim(lower(tab.street_type)),tab.post_index)) f
                       where lower(f.full_addr||',')=lower(nvl2(tab.area_fname,tab.area_fname||',','')||nvl2(tab.city_fname,tab.city_fname||',','')||nvl2(tab.loc_fname,tab.loc_fname||',','')||
                                                           nvl2(tab.street_fname,tab.street_fname||',',''))
                         and f.isleaf=1
                         ) fa_id
                from (
                      select n.id,
                             n.post_index,n.region_code,n.area,n.city,n.locality,n.street,
                             fias_pkg.GetParseType(3,n.area) area_type,
                             fias_pkg.GetParseName(3,n.area) area_fname,
                             case when n.city is null and n.region_code='77' then 'г'
                                  when n.city is null and n.region_code='78' then 'г'
                                  when n.city is null and n.region_code='92' then 'г'
                                  when n.city is null and n.region_code='99' then 'г'
                                  when n.region_code='78' and upper(n.city)='САНКТ-ПЕТЕРБУРГ' then 'г'
                                  else fias_pkg.GetParseType(4,n.city)
                             end  city_type,
                             case when n.city is null and n.region_code='77' then 'Москва'
                                  when n.city is null and n.region_code='78' then 'Санкт-Петербург'
                                  when n.city is null and n.region_code='92' then 'Севастополь'
                                  when n.city is null and n.region_code='99' then 'Байконур'
                                  else fias_pkg.GetParseName(4,n.city)
                             end city_fname,
                             fias_pkg.GetParseType(7,n.street) street_type,
                             fias_pkg.GetParseName(7,n.street) street_fname,
                             fias_pkg.GetParseType(6,n.locality) loc_type,
                             fias_pkg.GetParseName(6,n.locality) loc_fname,
                             case when n.street is null and n.city is not null then 1
                                  else 0
                             end city_leaf,
                             case when n.street is null and n.locality is not null then 1
                                  else 0
                             end loc_leaf       
                        from ndfl_person n
                       where n.declaration_data_id=p_declaration
                         --and n.id between p_start_id and p_start_id+999
                      ) tab
                ) n left join fias_addrobj f on (f.id=n.fa_id) left join fias_addrobj fc on (fc.id=f.parentguid);
    
    return v_ref;
  end;

  -------------------------------------------------------------------------------------------------------------
  -- Выполнить проверку на полное совпадение с ФИАС адресов, указанных в декларации
  -- возвращается курсор на таблицу типа TTblCheckAddrByFias
  -- для записей, по которым не установлен адрес в ФИАС, выполняется проверка наличия/отсутствия в ФИАС элементов адресов
  -- проверка выполняется по функции GetFiasAddrsFS
  -------------------------------------------------------------------------------------------------------------
  function CheckAddrByFias(p_declaration number,p_check_type number default 0) return ref_cursor
  is
    v_ref ref_cursor;
  begin
    v_check_path:=(p_check_type=1);
    open v_ref for
      select n.id,n.post_index,n.region_code,n.area,n.city,n.locality,n.street,
             n.ndfl_full_addr,n.area_type,n.area_fname,n.city_type,n.city_fname,n.loc_type,n.loc_fname,n.street_type,n.street_fname,
             f.id fias_id,
             f.postalcode fias_index,
             f.formalname fias_street,
             f.shortname fias_street_type,
             fc.id fias_city_id,
             fc.formalname fias_city_name,
             (select decode(count(*),0,0,1) 
                from fias_addrobj f 
               where f.regioncode=n.region_code 
                 and replace(f.postalcode,' ','')=n.post_index) chk_index,
             (select decode(count(*),0,0,1) 
                from fias_addrobj f 
               where f.regioncode=n.region_code) chk_region,
             case when n.fa_id is null then fias_pkg.CheckAddrElement(n.region_code,n.area_fname,',','AREA',0) 
                  else 1
             end chk_area,
             case when n.fa_id is null then fias_pkg.CheckAddrElement(n.region_code,n.city_fname,decode(p_check_type,1,nvl2(n.area_fname,n.area_fname||';',';'),n.area_fname),'CITY',n.city_leaf) 
                  else 1
             end chk_city,
             case when n.fa_id is null then fias_pkg.CheckAddrElement(n.region_code,n.loc_fname,decode(p_check_type,1,nvl2(n.area_fname,n.area_fname||';',';')||nvl2(n.city_fname,n.city_fname||';',''),nvl(n.city_fname,n.area_fname)),'LOCALITY',n.loc_leaf) 
                  else 1
             end chk_loc,
             case when n.fa_id is null then fias_pkg.CheckAddrElement(n.region_code,n.street_fname,decode(p_check_type,1,nvl2(n.area_fname,n.area_fname||';','')||nvl2(n.city_fname,n.city_fname||';','')||nvl2(n.loc_fname,n.loc_fname||';',''),nvl(n.loc_fname,n.city_fname)),'STREET',1) 
                  else 1
             end chk_street
        from (
              select tab.*,
                     nvl2(tab.area_fname,tab.area_fname||',','')||nvl2(tab.city_fname,tab.city_fname||',','')||nvl2(tab.loc_fname,tab.loc_fname||',','')||
                     nvl2(tab.street_fname,tab.street_fname||',','') ndfl_full_addr,
                     (select min(f.street_id)
                        from table(fias_pkg.GetFiasAddrsFS(tab.region_code,replace(lower(tab.area_fname),' ',''),replace(lower(tab.city_fname),' ',''),replace(lower(tab.loc_fname),' ',''),replace(lower(tab.street_fname),' ',''),
                                                           trim(lower(tab.area_type)),trim(lower(tab.city_type)),trim(lower(tab.loc_type)),trim(lower(tab.street_type)),tab.post_index)) f
                       where lower(f.full_addr)=lower(nvl2(tab.area_fname,tab.area_fname||',','')||nvl2(tab.city_fname,tab.city_fname||',','')||nvl2(tab.loc_fname,tab.loc_fname||',','')||
                                                           nvl2(tab.street_fname,tab.street_fname||',',''))
                     ) fa_id
                from (
                      select n.id,
                             n.post_index,n.region_code,n.area,n.city,n.locality,n.street,
                             fias_pkg.GetParseType(3,n.area) area_type,
                             fias_pkg.GetParseName(3,n.area) area_fname,
                             case when n.city is null and n.region_code='77' then 'г'
                                  when n.city is null and n.region_code='78' then 'г'
                                  when n.city is null and n.region_code='92' then 'г'
                                  when n.city is null and n.region_code='99' then 'г'
                                  when n.region_code='78' and upper(n.city)='САНКТ-ПЕТЕРБУРГ' then 'г'
                                  else fias_pkg.GetParseType(4,n.city)
                             end  city_type,
                             case when n.city is null and n.region_code='77' then 'Москва'
                                  when n.city is null and n.region_code='78' then 'Санкт-Петербург'
                                  when n.city is null and n.region_code='92' then 'Севастополь'
                                  when n.city is null and n.region_code='99' then 'Байконур'
                                  else fias_pkg.GetParseName(4,n.city)
                             end city_fname,
                             fias_pkg.GetParseType(7,n.street) street_type,
                             fias_pkg.GetParseName(7,n.street) street_fname,
                             fias_pkg.GetParseType(6,n.locality) loc_type,
                             fias_pkg.GetParseName(6,n.locality) loc_fname,
                             case when n.street is null and n.city is not null then 1
                                  else 0
                             end city_leaf,
                             case when n.street is null and n.locality is not null then 1
                                  else 0
                             end loc_leaf       
                        from ndfl_person n
                       where n.declaration_data_id=p_declaration
                         --and n.id between p_start_id and p_start_id+999
                      ) tab
                ) n left join fias_addrobj f on (f.id=n.fa_id) left join fias_addrobj fc on (fc.id=f.parentguid);
    
    return v_ref;
  end;

  -------------------------------------------------------------------------------------------------------------
  -- Выполнить проверку на наличие/отсутствие в ФИАС элементов адресов, указанных в декларации
  -- возвращается курсор на таблицу типа TTblCheckExistsAddrByFias
  -------------------------------------------------------------------------------------------------------------
  function CheckExistsAddrByFias(p_declaration number,p_check_type number default 0) return ref_cursor
  is
    v_ref ref_cursor;
  begin
    v_check_path:=(p_check_type=1);
    open v_ref for
      select tab.id,tab.post_index,tab.region_code,tab.area,tab.city,tab.locality,tab.street,
             nvl2(tab.area_fname,tab.area_fname||',','')||nvl2(tab.city_fname,tab.city_fname||',','')||
             nvl2(tab.loc_fname,tab.loc_fname||',','')||nvl2(tab.street_fname,tab.street_fname||',','') ndfl_full_addr,
             tab.area_type,tab.area_fname,tab.city_type,tab.city_fname,
             tab.loc_type,tab.loc_fname,tab.street_type,tab.street_fname,
             (select decode(count(*),0,0,1) 
                from fias_addrobj f 
               where f.regioncode=tab.region_code 
                 and f.postalcode=tab.post_index) chk_index,
             (select decode(count(*),0,0,1) 
                from fias_addrobj f 
               where f.regioncode=tab.region_code) chk_region,
             fias_pkg.CheckAddrElement(tab.region_code,tab.area_fname,',','',0) chk_area,
             fias_pkg.CheckAddrElement(tab.region_code,tab.city_fname,decode(p_check_type,1,nvl2(tab.area_fname,tab.area_fname||';',';'),tab.area_fname),'',tab.city_leaf) chk_city,
             fias_pkg.CheckAddrElement(tab.region_code,tab.loc_fname,decode(p_check_type,1,nvl2(tab.area_fname,tab.area_fname||';',';'),tab.area_fname),'',tab.loc_leaf) chk_loc,
             fias_pkg.CheckAddrElement(tab.region_code,tab.street_fname,decode(p_check_type,1,nvl2(tab.area_fname,tab.area_fname||';','')||nvl2(tab.city_fname,tab.city_fname||';','')||nvl2(tab.loc_fname,tab.loc_fname||';',''),nvl(tab.loc_fname,tab.city_fname)),'',1) chk_street
        from (select n.id,
                     n.post_index,n.region_code,n.area,n.city,n.locality,n.street,
                     fias_pkg.GetParseType(3,n.area) area_type,
                     fias_pkg.GetParseName(3,n.area) area_fname,
                     case when n.city is null and n.region_code='77' then 'г'
                          when n.city is null and n.region_code='78' then 'г'
                          when n.city is null and n.region_code='92' then 'г'
                          when n.city is null and n.region_code='99' then 'г'
                          when n.region_code='78' and upper(n.city)='САНКТ-ПЕТЕРБУРГ' then 'г'
                          else fias_pkg.GetParseType(4,n.city)
                     end  city_type,
                     case when n.city is null and n.region_code='77' then 'Москва'
                          when n.city is null and n.region_code='78' then 'Санкт-Петербург'
                          when n.city is null and n.region_code='92' then 'Севастополь'
                          when n.city is null and n.region_code='99' then 'Байконур'
                          else fias_pkg.GetParseName(4,n.city)
                     end city_fname,
                     fias_pkg.GetParseType(7,n.street) street_type,
                     fias_pkg.GetParseName(7,n.street) street_fname,
                     fias_pkg.GetParseType(6,n.locality) loc_type,
                     fias_pkg.GetParseName(6,n.locality) loc_fname,
                     case when n.street is null and n.city is not null then 1
                          else 0
                     end city_leaf,
                     case when n.street is null and n.locality is not null then 1
                          else 0
                     end loc_leaf
                from ndfl_person n
               where n.declaration_data_id=p_declaration) tab;

    return v_ref;
  end;

  -------------------------------------------------------------------------------------------------------------
  -- Обновление мат. представлений
  -------------------------------------------------------------------------------------------------------------
  procedure RefreshViews
  is
  begin
    dbms_mview.refresh('MV_FIAS_AREA_ACT', 'C');
    dbms_mview.refresh('MV_FIAS_CITY_ACT', 'C');
    dbms_mview.refresh('MV_FIAS_LOCALITY_ACT', 'C');
    dbms_mview.refresh('MV_FIAS_STREET_ACT', 'C');
  end;

end fias_pkg;
/
show errors;
exit;
