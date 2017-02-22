insert into ref_book_ndfl(department_id, inn, version, record_id, status, id)
select tab.*,seq_ref_book_record_row_id.nextval,0,seq_ref_book_record.nextval
  from (select d.dep_id,first_value(t.inn) over(partition by t.depcode) inn,
               to_date('01.01.2016','dd.mm.yyyy')
          from tmp_dep_params t join tmp_depart d on (d.code=t.depcode)
         where d.dep_id is not null
           /*and t.depcode='ЮЗБ'*/
        group by d.dep_id,t.depcode,t.inn) tab;
        
insert into ref_book_ndfl_detail(department_id, id, ref_book_ndfl_id,record_id, version, status, row_ord,
          tax_organ_code, kpp, present_place, name, oktmo, phone, 
          signatory_id, signatory_surname, signatory_firstname, signatory_lastname, approve_doc_name, approve_org_name, reorg_form_code)
select d.dep_id, seq_ref_book_record.nextval,n.id ndfl_id,seq_ref_book_record_row_id.nextval,to_date('01.01.2016','dd.mm.yyyy'),0,row_number() over (partition by t.depcode order by t.rowid) rn,
       t.tax_end,t.kpp,pp.id place_id,t.titname,
              o.mid oktmo_id,t.phone,s.id sign_id,t.surname,t.name,t.lastname,t.docname,t.orgname,ro.id reorg_id 
          from tmp_dep_params t join tmp_depart d on (d.code=t.depcode)
                                         left join ref_book_ndfl n on (n.department_id=d.dep_id and n.version=to_date('01.01.2016','dd.mm.yyyy'))
                                         left join ref_book_present_place pp on (pp.code=t.place and to_date('01.01.2016','dd.mm.yyyy') between pp.version and to_date('01.01.2016','dd.mm.yyyy'))
                                         left join (select code,max(id) mid from ref_book_oktmo group by code) o on (o.code=t.oktmo)
                                         left join ref_book_signatory_mark s on (s.code=t.sign and to_date('01.01.2016','dd.mm.yyyy') between s.version and to_date('01.01.2016','dd.mm.yyyy'))
                                         left join ref_book_reorganization ro on (ro.code=t.reorgcode and to_date('01.01.2016','dd.mm.yyyy') between ro.version and to_date('01.01.2016','dd.mm.yyyy'))
/*where t.depcode='ЮЗБ'*/;