insert into ref_book_ndfl(department_id, inn, version, record_id, status, id)
select tab.*,rownum,0,rownum
  from (select d.dep_id,first_value(t.inn) over(partition by t.depcode) inn,
               trunc(sysdate,'y')
          from tmp_dep_params t join tmp_depart d on (d.code=t.depcode)
         where d.dep_id is not null
        group by d.dep_id,t.depcode,t.inn) tab;
 
insert into ref_book_ndfl_detail(department_id, id, ref_book_ndfl_id,record_id, version, status, row_ord,
          tax_organ_code, kpp, present_place, name, oktmo, phone, 
          signatory_id, signatory_surname, signatory_firstname, signatory_lastname, approve_doc_name, approve_org_name, reorg_form_code)
select d.dep_id, seq_ref_book_ndfl_detail.nextval,n.id ndfl_id,seq_ref_book_ndfl_detail.currval,trunc(sysdate,'y'),0,row_number() over (partition by t.depcode order by t.rowid) rn,
       t.tax_end,t.kpp,pp.id place_id,t.titname,
              o.id oktmo_id,t.phone,s.id sign_id,t.surname,t.name,t.lastname,t.docname,t.orgname,ro.id reorg_id 
          from tmp_dep_params t join tmp_depart d on (d.code=t.depcode)
                                left join ref_book_ndfl n on (n.department_id=d.dep_id and n.version=trunc(sysdate,'y'))
                                left join ref_book_present_place pp on (pp.code=t.place and trunc(sysdate,'y') between pp.version and trunc(sysdate,'y'))
                                left join ref_book_oktmo o on (o.code=t.oktmo and trunc(sysdate,'y') between o.version and trunc(sysdate,'y'))
                                left join (select rec.id,
                                                  rec.record_id,
                                                  rec.version,
                                                  rec.status,
                                                  vl.code,vl.name
                                            from (select record_id,
                                                          max(decode(v.attribute_id,212,v.number_value)) code,
                                                          max(decode(v.attribute_id,213,v.string_value)) name
                                                     from ref_book_value v
                                                    where v.attribute_id in (212,213)
                                                    group by record_id) vl left join ref_book_record rec on (rec.id=vl.record_id and trunc(sysdate,'y') between rec.version and trunc(sysdate,'y'))
                                            order by rec.id) s on (s.code=t.sign and trunc(sysdate,'y') between s.version and trunc(sysdate,'y'))
                                left join (select rec.id,
                                                  rec.record_id,
                                                  rec.version,
                                                  rec.status,
                                                  vl.code,vl.name
                                            from (select record_id,
                                                         max(decode(v.attribute_id,13,v.string_value)) code,
                                                         max(decode(v.attribute_id,14,v.string_value)) name
                                                    from ref_book_value v
                                                   where v.attribute_id in (13,14)
                                                   group by record_id) vl left join ref_book_record rec on (rec.id=vl.record_id and trunc(sysdate,'y') between rec.version and trunc(sysdate,'y'))
                                           order by rec.id) ro on (ro.code=t.reorgcode and trunc(sysdate,'y') between ro.version and trunc(sysdate,'y'))
/*where t.depcode='ЦЧБ'*/;     

