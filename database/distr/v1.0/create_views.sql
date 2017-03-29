create or replace view vw_depart_kpp_oktmo
as
select d.id dep_id,d.name dep_name,
       sd.kpp,o.code oktmo
  from department d join ref_book_ndfl s on (s.department_id=d.id)
                    join ref_book_ndfl_detail sd on (sd.ref_book_ndfl_id=s.id)
                    join ref_book_oktmo o on (o.id=sd.oktmo);
/
exit;