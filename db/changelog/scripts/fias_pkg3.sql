create or replace 
package fias_pkg
-- Пакет для поиска адресов в справочнике ФИАС
as

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
                                                                       and city.ftype=c_city_type
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

  -- Проверить существование элемента адреса с учетом родительского элемента
  -- и вернуть минимальный идентификатор элемента адреса
  -- p_check_type: AREA,CITY,LOCALITY,STREET
  function CheckAddrElementRetID(p_region varchar2,p_check_element varchar2,p_check_ftype varchar2,p_parent_id number default null,p_check_type varchar2 default '',p_leaf number default 1) return number;

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

  -- Обновить материализованные представления
  procedure RefreshViews;

  -- Переключить внешние ключи
  -- p_mode: DISABLE - отключить, ENABLE - включить
  procedure TurnForeignKeys(p_mode varchar2);

  -- Очистить таблицу FIAS_ADDROBJ
  procedure ClearFiasAddrObj;

  -- Удаление и создание индекса IDX_FIAS_ADDR_CURRST_AOLEV
  procedure BeforeImport;
  procedure AfterImport;

  procedure DropIndex;
  procedure CreateIndex;
  procedure CheckPackage;

end fias_pkg;