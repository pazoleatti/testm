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
/
show errors;
create or replace 
package body fias_pkg as

    v_check_path boolean:=true;
    
  procedure CheckPackage
  IS
  BEGIN
    NULL;
    EXCEPTION
      WHEN OTHERS
        THEN NULL;
  END CheckPackage;
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
  CheckPackage;
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
    CheckPackage;
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
    CheckPackage;    
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
    CheckPackage;
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
    CheckPackage;
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
    CheckPackage;
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
          from mv_fias_city_act c join mv_fias_area_act a on (a.aoid=c.parentguid)
         where c.regioncode=p_region
           and c.fname=nvl(replace(lower(p_check_element),' ',''),'-')
           and a.fname=nvl(replace(lower(p_parent_element),' ',''),'-')
           and c.has_child=decode(p_leaf,1,0,1);
      elsif (p_check_type='LOCALITY') then
        select decode(sum(cnt),0,0,1) into v_result
          from (
                select count(*) cnt
                  from mv_fias_locality_act l left join mv_fias_city_act c on (c.aoid=l.parentguid)
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
                  from mv_fias_street_act s left join mv_fias_city_act c on (c.aoid=s.parentguid)
                 where s.regioncode=p_region
                   and s.fname=nvl(replace(lower(p_check_element),' ',''),'-')
                   and c.fname=nvl(replace(lower(p_parent_element),' ',''),'-')
                   and s.has_child=decode(p_leaf,1,0,1)
                union
                select count(*) cnt
                  from mv_fias_street_act s left join mv_fias_locality_act l on (l.aoid=s.parentguid)
                 where s.regioncode=p_region
                   and s.fname=nvl(replace(lower(p_check_element),' ',''),'-')
                   and l.fname=nvl(replace(lower(p_parent_element),' ',''),'-')
                   and s.has_child=decode(p_leaf,1,0,1)
                union
                select count(*) cnt
                  from mv_fias_street_act s left join mv_fias_area_act a on (a.aoid=s.parentguid)
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
  -- Проверить существование элемента адреса с учетом родительского элемента
  -- и вернуть минимальный идентификатор элемента адреса
  -- p_check_type: AREA,CITY,LOCALITY,STREET
  -------------------------------------------------------------------------------------------------------------
  function CheckAddrElementRetID(p_region varchar2,p_check_element varchar2,p_check_ftype varchar2,p_parent_id number default null,
                                 p_check_type varchar2 default '',p_leaf number default 1) return number
  is
    v_result number:=0;
  begin
    CheckPackage;
    if (p_check_type='AREA') then
      select min(a.id) into v_result
        from mv_fias_area_act a
       where a.regioncode=p_region
         and (a.fname=nvl(replace(lower(p_check_element),' ',''),'-'))
         and (p_check_ftype is null or p_check_ftype is not null and a.ftype=nvl(lower(p_check_ftype),'-'))
         and a.has_child=decode(p_leaf,1,0,1);
    elsif (p_check_type='CITY') then
      select min(c.id) into v_result
        from mv_fias_city_act c left join mv_fias_area_act a on (a.aoid=c.parentguid)
       where c.regioncode=p_region
         and c.fname=nvl(replace(lower(p_check_element),' ',''),'-')
         and c.ftype=nvl(lower(p_check_ftype),'г')
         and (p_parent_id is null /*and c.parentguid is null*/ or p_parent_id is not null and a.id=p_parent_id)
         --and c.has_child=decode(p_leaf,1,0,1)
		 ;
    elsif (p_check_type='LOCALITY') then
      select min(id) into v_result
        from (
              select l.id
                from mv_fias_locality_act l left join mv_fias_city_act c on (c.aoid=l.parentguid)
               where l.regioncode=p_region
                 and l.fname=nvl(replace(lower(p_check_element),' ',''),'-')
                 and (p_check_ftype is null or p_check_ftype is not null and l.ftype=nvl(lower(p_check_ftype),'-'))
                 and (p_parent_id is null /*and c.parentguid is null*/ or p_parent_id is not null and c.id=p_parent_id)
                 and l.has_child=decode(p_leaf,1,0,1)
              union
              select l.id
                from mv_fias_locality_act l left join mv_fias_area_act a on (a.aoid=l.parentguid)
               where l.regioncode=p_region
                 and l.fname=nvl(replace(lower(p_check_element),' ',''),'-')
                 and (p_check_ftype is null or p_check_ftype is not null and l.ftype=nvl(lower(p_check_ftype),'-'))
                 and (p_parent_id is null /*and l.parentguid is null*/ or p_parent_id is not null and a.id=p_parent_id)
                 and l.has_child=decode(p_leaf,1,0,1)
            );
    elsif (p_check_type='STREET') then
      select min(id) into v_result
        from (
              select s.id
                from mv_fias_street_act s left join mv_fias_city_act c on (c.aoid=s.parentguid)
               where s.regioncode=p_region
                 and s.fname=nvl(replace(lower(p_check_element),' ',''),'-')
                 and (p_check_ftype is null or p_check_ftype is not null and s.ftype=nvl(lower(p_check_ftype),'ул'))
                 and (p_parent_id is null and s.parentguid is null or p_parent_id is not null and c.id=p_parent_id)
                 and s.has_child=decode(p_leaf,1,0,1)
              union
              select s.id
                from mv_fias_street_act s left join mv_fias_locality_act l on (l.aoid=s.parentguid)
               where s.regioncode=p_region
                 and s.fname=nvl(replace(lower(p_check_element),' ',''),'-')
                 and (p_check_ftype is null or p_check_ftype is not null and s.ftype=nvl(lower(p_check_ftype),'ул'))
                 and (p_parent_id is null and s.parentguid is null or p_parent_id is not null and l.id=p_parent_id)
                 and s.has_child=decode(p_leaf,1,0,1)
              union
              select s.id
                from mv_fias_street_act s left join mv_fias_area_act a on (a.aoid=s.parentguid)
               where s.regioncode=p_region
                 and s.fname=nvl(replace(lower(p_check_element),' ',''),'-')
                 and (p_check_ftype is null or p_check_ftype is not null and s.ftype=nvl(lower(p_check_ftype),'ул'))
                 and (p_parent_id is null and s.parentguid is null or p_parent_id is not null and a.id=p_parent_id)
                 and s.has_child=decode(p_leaf,1,0,1)
            );
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
    CheckPackage;
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
  -- проверка наличия,отсутствия элементов адресов выполняется функцией CheckAddrElementRetID,
  -- которая в качестве результата возвращает минимальный ID найденного элемента адреса
  -------------------------------------------------------------------------------------------------------------
  function CheckAddrByFias(p_declaration number,p_check_type number default 0) return ref_cursor
  is
    v_ref ref_cursor;
  begin
    CheckPackage;
    v_check_path:=false;
    open v_ref for
    select n.id,n.post_index,n.region_code,n.area,n.city,n.locality,n.street,
           n.ndfl_full_addr,n.area_type,n.area_fname,n.city_type,n.city_fname,n.loc_type,n.loc_fname,n.street_type,n.street_fname,
           f.id fias_id,
           f.postalcode fias_index,
           f.formalname fias_street,
           f.shortname fias_street_type,
           nvl(fl.id,fc.id) fias_city_id,
           nvl(fl.formalname,fc.formalname) fias_city_name,
           n.chk_index,
           n.chk_region,
           nvl2(n.area_id,1,0) chk_area,
           nvl2(n.city_id,1,0) chk_city,
           nvl2(n.loc_id,1,0) chk_loc,
           nvl2(n.street_id,1,0) chk_street
  from (
        select t4.*,
               fias_pkg.CheckAddrElementRetID(t4.region_code,t4.street_fname,t4.street_type,nvl(t4.loc_id,t4.city_id),'STREET',1) street_id
          from (
                select t3.*,
                       fias_pkg.CheckAddrElementRetID(t3.region_code,t3.loc_fname,t3.loc_type,nvl(t3.city_id,t3.area_id),'LOCALITY',-1/*t3.loc_leaf*/) loc_id
                  from (
                        select t2.*,
                               case when t2.region_code in ('77','78','92','99') and t2.city is null then fias_pkg.CheckAddrElementRetID(t2.region_code,t2.city_fname,t2.city_type,null,'AREA',0)
                                    else fias_pkg.CheckAddrElementRetID(t2.region_code,t2.city_fname,t2.city_type,t2.area_id,'CITY',t2.city_leaf) end city_id
                          from (
                                select t1.*,
                                       (select decode(count(*),0,0,1) from mv_fias_street_act f
                                         where f.regioncode=t1.region_code and f.postalcode=t1.post_index) chk_index,
                                       (select decode(count(*),0,0,1) from mv_fias_street_act f
                                         where f.regioncode=t1.region_code) chk_region,
                                       fias_pkg.CheckAddrElementRetID(t1.region_code,t1.area_fname,t1.area_type,null,'AREA',0)  area_id,
                                       nvl2(t1.area_fname,t1.area_fname||',','')||nvl2(t1.city_fname,t1.city_fname||',','')||nvl2(t1.loc_fname,t1.loc_fname||',','')||
                                       nvl2(t1.street_fname,t1.street_fname||',','') ndfl_full_addr
                                  from (
                                        select n.id,
                                               n.post_index,n.region_code,n.area,n.city,n.locality,n.street,
                                               fias_pkg.GetParseType(3,n.area,1) area_type,
                                               fias_pkg.GetParseName(3,n.area,1) area_fname,
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
                                         where n.declaration_data_id=p_declaration) t1
                               ) t2
                       ) t3
               ) t4
       ) n left join (select * from mv_fias_street_act
                      union
                      select * from mv_fias_locality_act) f on (f.id=nvl(n.street_id,n.loc_id))
           left join mv_fias_locality_act fl on (fl.aoid=f.parentguid)
           left join mv_fias_city_act fc on (fc.aoid=f.parentguid);

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
    CheckPackage;
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

  ------------------------------------------------------------------------------
  -- Обновить материализованные представления
  ------------------------------------------------------------------------------
  procedure RefreshViews
  is
    PRAGMA AUTONOMOUS_TRANSACTION;
  begin
    CheckPackage;
    DBMS_MVIEW.REFRESH(list=>'MV_FIAS_CITY_ACT',method => 'C', atomic_refresh => FALSE);
    DBMS_MVIEW.REFRESH(list=>'MV_FIAS_AREA_ACT',method => 'C', atomic_refresh => FALSE);
    DBMS_MVIEW.REFRESH(list=>'MV_FIAS_LOCALITY_ACT',method => 'C', atomic_refresh => FALSE);
    DBMS_MVIEW.REFRESH(list=>'MV_FIAS_STREET_ACT',method => 'C', atomic_refresh => FALSE);
  end;

  ------------------------------------------------------------------------------
  -- Переключить внешние ключи
  -- p_mode: DISABLE - отключить, ENABLE - включить
  ------------------------------------------------------------------------------
  procedure TurnForeignKeys(p_mode varchar2)
  is
    cursor foreign_keys is
      select fk.table_name,fk.constraint_name
        from user_constraints pk left join user_constraints fk on (fk.r_constraint_name=pk.constraint_name)
       where pk.table_name='FIAS_ADDROBJ'
         and pk.constraint_type='P';
    v_mode varchar2(8 char):=lower(p_mode);
  begin
    CheckPackage;
    if v_mode not in ('disable','enable') then
      return;
    end if;

    for c in foreign_keys loop
      execute immediate 'alter table '||lower(c.table_name)||' '||v_mode||' constraint '||lower(c.constraint_name);
    end loop;

  end;

  ------------------------------------------------------------------------------
  -- Очистить таблицу FIAS_ADDROBJ
  ------------------------------------------------------------------------------
  procedure ClearFiasAddrObj
  is
  begin
    CheckPackage;
    TurnForeignKeys('disable');

    delete from fias_addrobj;

    TurnForeignKeys('enable');

  end;

  ------------------------------------------------------------------------------
  -- Очистка таблицы перед загрузкой
  ------------------------------------------------------------------------------
  procedure BeforeImport
  is
    PRAGMA AUTONOMOUS_TRANSACTION;
    v_count number;
  begin
    CheckPackage;
    /*select count(1) into v_count from user_indexes where index_name='IDX_FIAS_ADDR_CURRST_AOLEV';
    if v_count>0 then
      execute immediate 'drop index IDX_FIAS_ADDR_CURRST_AOLEV';
    end if;*/

    /*for c in (select fk.table_name,fk.constraint_name
        from user_constraints pk left join user_constraints fk on fk.r_constraint_name=pk.constraint_name
       where pk.table_name='FIAS_ADDROBJ'
         and pk.constraint_type='P')
    loop
      execute immediate 'alter table '||lower(c.table_name)||' disable constraint '||lower(c.constraint_name);
    end loop;*/

    delete from fias_addrobj;

    /*for c in (select fk.table_name,fk.constraint_name
        from user_constraints pk left join user_constraints fk on fk.r_constraint_name=pk.constraint_name
       where pk.table_name='FIAS_ADDROBJ'
         and pk.constraint_type='P')
    loop
      execute immediate 'alter table '||lower(c.table_name)||' enable constraint '||lower(c.constraint_name);
    end loop;*/
	commit;
  end BeforeImport;

  ------------------------------------------------------------------------------
  -- Процедура после загрузки
  ------------------------------------------------------------------------------
  procedure AfterImport
  is
    PRAGMA AUTONOMOUS_TRANSACTION;
    v_count number;
  begin
    CheckPackage;
    select count(1) into v_count from user_indexes where index_name='IDX_FIAS_ADDR_CURRST_AOLEV';
   /* if v_count=0 then
      execute immediate 'CREATE INDEX IDX_FIAS_ADDR_CURRST_AOLEV ON FIAS_ADDROBJ (CURRSTATUS ASC, AOLEVEL ASC, REPLACE(LOWER(FORMALNAME), '' '', '''') ASC) ';
    end if;*/
  end AfterImport;

  ------------------------------------------------------------------------------
  -- Удаление индекса
  ------------------------------------------------------------------------------
  procedure DropIndex
  is
    v_count number;
  begin
    CheckPackage;
    select count(1) into v_count from user_indexes where index_name='IDX_FIAS_ADDR_CURRST_AOLEV';
    if v_count>0 then
      execute immediate 'drop index IDX_FIAS_ADDR_CURRST_AOLEV';
    end if;
  end DropIndex;

  ------------------------------------------------------------------------------
  -- Пересоздание индекса
  ------------------------------------------------------------------------------
  procedure CreateIndex
  is
    v_count number;
  begin
    CheckPackage;
    select count(1) into v_count from user_indexes where index_name='IDX_FIAS_ADDR_CURRST_AOLEV';
    if v_count=0 then
      execute immediate 'CREATE INDEX IDX_FIAS_ADDR_CURRST_AOLEV ON FIAS_ADDROBJ (CURRSTATUS ASC, AOLEVEL ASC, REPLACE(LOWER(FORMALNAME), '' '', '''') ASC) ';
    end if;
  end CreateIndex;

end fias_pkg;
/
show errors;