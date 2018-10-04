create or replace view vw_depart_kpp_oktmo as
select d.id dep_id,d.name dep_name,
     sd.kpp,o.code oktmo
  from department d join ref_book_ndfl_detail sd on (sd.department_id=d.id)
          join ref_book_oktmo o on (o.id=sd.oktmo);
comment on table VW_DEPART_KPP_OKTMO is 'Пары КПП и ОКТМО в отчетных формах подразделений';
comment on column VW_DEPART_KPP_OKTMO.DEP_ID is 'Идентификатор подразделения';
comment on column VW_DEPART_KPP_OKTMO.DEP_NAME is 'Наименование подразделения';
comment on column VW_DEPART_KPP_OKTMO.KPP is 'КПП из параметров подразделения по НДФЛ';
comment on column VW_DEPART_KPP_OKTMO.OKTMO is 'ОКТМО  из параметров подразделения по НДФЛ';
