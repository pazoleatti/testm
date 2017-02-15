merge into ref_book_oktmo r
using (select distinct t.code,first_value(t.record_id) over(partition by t.code order by t.id) first_rec_id
        from ref_book_oktmo t
       /*where exists(select 1 from ref_book_oktmo d where d.code=t.code and d.status=-1)*/) nr
 on (r.code=nr.code)
when matched then update set r.record_id=nr.first_rec_id;

update ref_book_oktmo set code=substr(code,1,8) where razd=1;

commit;
exit;
