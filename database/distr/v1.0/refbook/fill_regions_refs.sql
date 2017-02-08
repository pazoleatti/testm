merge into ref_book_region t
using (select r.id,r.code,r.name,r.okato_definition,trim(rpad(r.okato_definition,11,'0')) okato_code,
             r.oktmo_definition,trim(rpad(r.oktmo_definition,8,'0')) okmto_code,
             m.id oktmo_id
        from ref_book_region r left join ref_book_oktmo m on (m.code=trim(rpad(r.oktmo_definition,8,'0')))
             ) v
  on (t.id=v.id)
when matched then update
 set t.oktmo=v.oktmo_id;