declare
  v_version date:=to_date('01.01.2016','dd.mm.yyyy');
begin
      for dep in (select depcode,count(*) cnt,max(row_num) last_num
                    from tmp_dep_params
                    group by depcode
                    minus
                    select td.code,count(*) cnt,max(row_ord) last_num
                    from ref_book_ndfl n join department d on (d.id=n.department_id)
                                         join ref_book_ndfl_detail p on (p.ref_book_ndfl_id=n.id)
                                         join tmp_depart td on (td.dep_id=d.id)
                    group by td.code 
                    order by 1) loop

    delete from ref_book_ndfl_detail where department_id in (select dep_id from tmp_depart where code=dep.depcode);
    delete from ref_book_ndfl where department_id in (select dep_id from tmp_depart where code=dep.depcode);
    
    insert into ref_book_ndfl(department_id, version, record_id, status, id)
    select tab.*,seq_ref_book_record_row_id.nextval,0,seq_ref_book_record.nextval
      from (select d.dep_id,
                   v_version
              from tmp_dep_params t join tmp_depart d on (d.code=t.depcode)
             where d.dep_id is not null
               and t.depcode=dep.depcode
            group by d.dep_id,t.depcode,t.inn) tab;
            
    insert into ref_book_ndfl_detail(department_id, id, ref_book_ndfl_id,record_id, version, status, row_ord,
              tax_organ_code, kpp, present_place, name, oktmo, phone, 
              signatory_id, signatory_surname, signatory_firstname, signatory_lastname, approve_doc_name, approve_org_name, reorg_form_code)
    select d.dep_id, seq_ref_book_record.nextval,n.id ndfl_id,seq_ref_book_record_row_id.nextval,v_version,0,row_num,
           t.tax_end,t.kpp,pp.id place_id,t.titname,
                  o.mid oktmo_id,t.phone,s.id sign_id,t.surname,t.name,t.lastname,t.docname,t.orgname,ro.id reorg_id 
              from tmp_dep_params t join tmp_depart d on (d.code=t.depcode)
                                             left join ref_book_ndfl n on (n.department_id=d.dep_id and n.version=v_version)
                                             left join ref_book_present_place pp on (pp.code=t.place and v_version between pp.version and v_version)
                                             left join (select code,max(id) mid from ref_book_oktmo group by code) o on (o.code=t.oktmo)
                                             left join ref_book_signatory_mark s on (s.code=t.sign and v_version between s.version and v_version)
                                             left join ref_book_reorganization ro on (ro.code=t.reorgcode and v_version between ro.version and v_version)
    where t.depcode=dep.depcode;
  commit;
  end loop;
end;
/

commit;
--exit;