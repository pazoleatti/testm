/*
drop materialized view mv_fias_area_act;
drop materialized view mv_fias_city_act;
drop materialized view mv_fias_locality_act;
drop materialized view mv_fias_street_act;
*/

create materialized view mv_fias_area_act
refresh complete on demand
as
select distinct f.*,
       replace(lower(f.formalname),' ','') fname,
       trim(lower(f.shortname)) ftype,
       nvl2(c.id,1,0) has_child
  from fias_addrobj f left join fias_addrobj c on (c.parentguid=f.aoid and c.currstatus=0)
 where f.currstatus=0
   and (f.aolevel=3 or
        f.aolevel=1 and replace(lower(f.formalname),' ','') not in ('москва','санкт-петербург','севастополь','байконур')
        );
   
comment on materialized view mv_fias_area_act is 'Актуальные районы из справочника ФИАС';
   
create index idx_mv_fias_area_aoid on mv_fias_area_act (aoid asc);
create index idx_mv_fias_area_fname on mv_fias_area_act (fname asc);
create index idx_mv_fias_area_pguid on mv_fias_area_act (parentguid asc);
create index srch_mv_fias_area_reg_fn_tp on mv_fias_area_act (regioncode,fname,ftype,has_child);

create materialized view mv_fias_city_act
refresh complete on demand
as
select distinct f.*,
       replace(lower(f.formalname),' ','') fname,
       trim(lower(f.shortname)) ftype,
        nvl2(c.id,1,0) has_child
  from fias_addrobj f left join fias_addrobj c on (c.parentguid=f.aoid and c.currstatus=0)
 where f.currstatus=0
   and (f.aolevel=4 or
        f.aolevel=1 and replace(lower(f.formalname),' ','') in ('москва','санкт-петербург','севастополь','байконур')
       );

comment on materialized view mv_fias_city_act is 'Актуальные города из справочника ФИАС';
   
create index idx_mv_fias_city_aoid on mv_fias_city_act (aoid asc);
create index idx_mv_fias_city_fname on mv_fias_city_act (fname asc);
create index idx_mv_fias_city_pguid on mv_fias_city_act (parentguid asc);
create index srch_mv_fias_city_reg_fn_tp on mv_fias_city_act (regioncode,fname,ftype,has_child);

create materialized view mv_fias_locality_act
refresh complete on demand
as
select distinct f.*,
       replace(lower(f.formalname),' ','') fname,
       trim(lower(f.shortname)) ftype,
       nvl2(c.id,1,0) has_child
  from fias_addrobj f left join fias_addrobj c on (c.parentguid=f.aoid and c.currstatus=0)
 where f.currstatus=0
   and f.aolevel in (6,90);

comment on materialized view mv_fias_locality_act is 'Актуальные населенные пункты из справочника ФИАС';
   
create index idx_mv_fias_locality_aoid on mv_fias_locality_act (aoid asc);
create index idx_mv_fias_locality_fname on mv_fias_locality_act (fname asc);
create index idx_mv_fias_locality_pguid on mv_fias_locality_act (parentguid asc);
create index srch_mv_fias_local_reg_fn_tp on mv_fias_locality_act (regioncode,fname,ftype,has_child);

create materialized view mv_fias_street_act
refresh complete on demand
as
select distinct f.*,
       replace(lower(f.formalname),' ','') fname,
       trim(lower(f.shortname)) ftype,
       nvl2(c.id,1,0) has_child
  from fias_addrobj f left join fias_addrobj c on (c.parentguid=f.aoid and c.currstatus=0)
 where f.currstatus=0
   and f.aolevel in (7,91);

comment on materialized view mv_fias_street_act is 'Актуальные улицы из справочника ФИАС';
   
create index idx_mv_fias_street_aoid on mv_fias_street_act (aoid asc);
create index idx_mv_fias_street_fname on mv_fias_street_act (fname asc);
create index idx_mv_fias_street_pguid on mv_fias_street_act (parentguid asc);
create index srch_mv_fias_street_reg_fn_tp on mv_fias_street_act (regioncode,fname,ftype,has_child);

alter table mv_fias_area_act add constraint pk_mv_fias_area_act primary key (id);
alter table mv_fias_city_act add constraint pk_mv_fias_city_act primary key (id);
alter table mv_fias_locality_act add constraint pk_mv_fias_locality_act primary key (id);
alter table mv_fias_street_act add constraint pk_mv_fias_street_act primary key (id);

create index idx_mv_fias_street_post on mv_fias_street_act(postalcode);

exit;