create or replace 
package body person_pkg as

  v_date date:=trunc(sysdate);

  procedure FillRecordVersions(p_date date default trunc(sysdate))
  is
  begin
    v_date:=p_date;

    delete from tmp_version;
    insert into tmp_version
    select max(version) version,
           record_id,
           p_date    calc_date
      from ref_book_person r
     where status = 0 and version <= p_date
       and not exists(select 1
                        from ref_book_person r2
                       where r2.record_id = r.record_id and r2.status != -1
                         and r2.version between r.version + interval '1' day and p_date)
       group by record_id;
  end;

  function GetPersonForUpd(p_declaration number,p_asnu number default 1) return ref_cursor
  is
    v_ref ref_cursor;
  begin
    open v_ref for
        select fv.person_id,
               person.id as ref_book_person_id,
               person.version as person_version,
               person.status as person_status,
               person.record_id as person_record_id,
               person.last_name,
               person.first_name,
               person.middle_name,
               person.inn,
               person.inn_foreign,
               person.snils,
               person.taxpayer_state,
               person.birth_date,
               person.birth_place,
               person.citizenship,
               person.record_id,
               person.source_id,
               person.old_id,
               person.report_doc,
               person.vip,
               tax.id as book_id_tax_payer_id,
               tax.version as tax_version,
               tax.status as tax_status,
               tax.record_id as tax_record_id,
               tax.inp,
               tax.as_nu,
               doc.id as ref_book_id_doc_id,
               doc.version as doc_version,
               doc.status as doc_status,
               doc.record_id as doc_record_id,
               doc.doc_id,
               doc.doc_number,
               doc.inc_rep,
               tb.id as ref_book_person_tb_id,
               tb.version as tb_version,
               tb.status as tb_status,
               tb.record_id as tb_record_id,
               tb.tb_department_id,
               tb.import_date,
               addr.id as ref_book_address_id,
               addr.version as addr_version,
               addr.status as addr_status,
               addr.record_id as addr_record_id,
               addr.country_id,
               addr.region_code,
               addr.postal_code,
               addr.district,
               addr.city,
               addr.locality,
               addr.street,
               addr.house,
               addr.build,
               addr.appartment,
               addr.address_type,
               addr.address
          from (
             select distinct n.id as person_id,first_value(fv.id) over(partition by n.id order by fv.id) ref_person_id
               from  ndfl_person n join ref_book_person fv on (replace(lower(nvl(fv.last_name,'empty')),' ','') = replace(lower(nvl(n.last_name,'empty')),' ','')
                                                               and replace(lower(nvl(fv.first_name,'empty')),' ','') = replace(lower(nvl(n.first_name,'empty')),' ','')
                                                               and replace(lower(nvl(fv.middle_name,'empty')),' ','') = replace(lower(nvl(n.middle_name,'empty')),' ','')
                                                               and fv.birth_date=n.birth_day
                                                               and replace(replace(nvl(fv.snils,'empty'),' ',''),'-','') = replace(replace(nvl(n.snils,'empty'), ' ', ''), '-', '')
                                                               and replace(nvl(fv.inn,'empty'),' ','') = replace(nvl(n.inn_np,'empty'),' ','')
                                                               and replace(nvl(fv.inn_foreign,'empty'),' ','') = replace(nvl(n.inn_foreign,'empty'),' ','')
                                                               )
              join (select distinct calc_date,version,record_id from tmp_version) tv on tv.calc_date = TRUNC(SYSDATE) and tv.version = fv.version and tv.record_id = fv.record_id                                                               
              where n.declaration_data_id=p_declaration
                and exists (select 1 from ref_book_person c join ref_book_person c1 on (c.record_id=c1.record_id) left join ref_book_id_tax_payer t on (t.person_id=c1.id)
                             where replace(lower(nvl(c.last_name,'empty')),' ','') = replace(lower(nvl(n.last_name,'empty')),' ','')
                               and replace(lower(nvl(c.first_name,'empty')),' ','') = replace(lower(nvl(n.first_name,'empty')),' ','')
                               and replace(lower(nvl(c.middle_name,'empty')),' ','') = replace(lower(nvl(n.middle_name,'empty')),' ','')
                               and c.birth_date=n.birth_day
                               and replace(replace(nvl(c.snils,'empty'),' ',''),'-','') = replace(replace(nvl(n.snils,'empty'), ' ', ''), '-', '')
                               and replace(nvl(c.inn,'empty'),' ','') = replace(nvl(n.inn_np,'empty'),' ','')
                               and replace(nvl(c.inn_foreign,'empty'),' ','') = replace(nvl(n.inn_foreign,'empty'),' ','')
                               and t.as_nu=p_asnu and lower(t.inp)=lower(n.inp)
                               )
                --and exists(select 1 from tmp_version t where t.calc_date = v_date and t.version = fv.version and t.record_id = fv.record_id)
                and fv.record_id=fv.old_id
                ) fv join ref_book_person person on (person.id=fv.ref_person_id)
                     left join ref_book_id_tax_payer tax on (tax.person_id in (select id from ref_book_person where record_id=person.record_id) and tax.status=0)
                     left join ref_book_id_doc doc on (doc.person_id in (select id from ref_book_person where record_id=person.record_id) and doc.status=0)
                     left join ref_book_person_tb tb on (tb.person_id in (select id from ref_book_person where record_id=person.record_id) and tb.status=0)                     
                     left join ref_book_address addr on (addr.id=person.address and addr.status=0);

    return v_ref;
  end;

  function GetPersonForCheck(p_declaration number,p_asnu number default 1) return ref_cursor
  is
    v_ref ref_cursor;
  begin
    open v_ref for
       /*По ФИО*/
      select t.id as person_id,
             person.id as ref_book_person_id,
             person.version as person_version,
             person.status as person_status,
             person.record_id as person_record_id,
             person.last_name,
             person.first_name,
             person.middle_name,
             person.inn,
             person.inn_foreign,
             person.snils,
             person.taxpayer_state,
             person.birth_date,
             person.birth_place,
             person.citizenship,
             person.record_id,
             person.source_id,
             person.old_id,
             person.report_doc,
             person.vip,
             tax.id as book_id_tax_payer_id,
             tax.version as tax_version,
             tax.status as tax_status,
             tax.record_id as tax_record_id,
             tax.inp,
             tax.as_nu,
             doc.id as ref_book_id_doc_id,
             doc.version as doc_version,
             doc.status as doc_status,
             doc.record_id as doc_record_id,
             doc.doc_id,
             doc.doc_number,
             doc.inc_rep,
             tb.id as ref_book_person_tb_id,
             tb.version as tb_version,
             tb.status as tb_status,
             tb.record_id as tb_record_id,
             tb.tb_department_id,
             tb.import_date,
             addr.id as ref_book_address_id,
             addr.version as addr_version,
             addr.status as addr_status,
             addr.record_id as addr_record_id,
             addr.country_id,
             addr.region_code,
             addr.postal_code,
             addr.district,
             addr.city,
             addr.locality,
             addr.street,
             addr.house,
             addr.build,
             addr.appartment,
             addr.address_type,
             addr.address
        from ndfl_person t join ref_book_person person on (replace(lower(nvl(person.last_name,'empty')),' ','') = replace(lower(t.last_name),' ','') and
                                                           replace(lower(nvl(person.first_name,'empty')),' ','') = replace(lower(t.first_name),' ','') and
                                                           replace(lower(nvl(person.middle_name,'empty')),' ','') = replace(lower(t.middle_name),' ','') and
                                                           person.birth_date=t.birth_day
                                                               )
                           left join ref_book_id_doc doc on (doc.person_id in (select id from ref_book_person where record_id=person.record_id) and doc.status=0)
                           left join ref_book_id_tax_payer tax on (tax.person_id in (select id from ref_book_person where record_id=person.record_id) and tax.status=0)
                           left join ref_book_person_tb tb on (tb.person_id in (select id from ref_book_person where record_id=person.record_id) and tb.status=0)                     
                           left join ref_book_address addr on (addr.id=person.address and addr.status=0)
       where t.declaration_data_id=p_declaration
         and t.person_id is null
         and exists(select 1 from tmp_version tv where tv.calc_date = v_date and tv.version = person.version and tv.record_id = person.record_id)
         and person.status=0 and person.record_id=person.old_id
      union
       /*по СНИЛСУ*/
      select /*+ use_hash(t person) parallel(t 4) parallel(person 4)*/ t.id as person_id,
             person.id as ref_book_person_id,
             person.version as person_version,
             person.status as person_status,
             person.record_id as person_record_id,
             person.last_name,
             person.first_name,
             person.middle_name,
             person.inn,
             person.inn_foreign,
             person.snils,
             person.taxpayer_state,
             person.birth_date,
             person.birth_place,
             person.citizenship,
             person.record_id,
             person.source_id,
             person.old_id,
             person.report_doc,
             person.vip,
             tax.id as book_id_tax_payer_id,
             tax.version as tax_version,
             tax.status as tax_status,
             tax.record_id as tax_record_id,
             tax.inp,
             tax.as_nu,
             doc.id as ref_book_id_doc_id,
             doc.version as doc_version,
             doc.status as doc_status,
             doc.record_id as doc_record_id,
             doc.doc_id,
             doc.doc_number,
             doc.inc_rep,
             tb.id as ref_book_person_tb_id,
             tb.version as tb_version,
             tb.status as tb_status,
             tb.record_id as tb_record_id,
             tb.tb_department_id,
             tb.import_date,
             addr.id as ref_book_address_id,
             addr.version as addr_version,
             addr.status as addr_status,
             addr.record_id as addr_record_id,
             addr.country_id,
             addr.region_code,
             addr.postal_code,
             addr.district,
             addr.city,
             addr.locality,
             addr.street,
             addr.house,
             addr.build,
             addr.appartment,
             addr.address_type,
             addr.address
        from ndfl_person t join ref_book_person person on (replace(replace(nvl(person.snils,'empty'),' ',''),'-','') = replace(replace(t.snils, ' ', ''), '-', ''))
                           left join ref_book_id_doc doc on (doc.person_id in (select id from ref_book_person where record_id=person.record_id) and doc.status=0)
                           left join ref_book_id_tax_payer tax on (tax.person_id in (select id from ref_book_person where record_id=person.record_id) and tax.status=0)
                           left join ref_book_person_tb tb on (tb.person_id in (select id from ref_book_person where record_id=person.record_id) and tb.status=0)                                                
                           left join ref_book_address addr on (addr.id=person.address and addr.status=0)
       where t.declaration_data_id=p_declaration
         and t.person_id is null
         and exists(select 1 from tmp_version tv where tv.calc_date = v_date and tv.version = person.version and tv.record_id = person.record_id)
         and person.status=0 and person.record_id=person.old_id
      union
      /*По ИННу*/
      select /*+ use_hash(t person) parallel(t 4) parallel(person 4)*/ t.id as person_id,
             person.id as ref_book_person_id,
             person.version as person_version,
             person.status as person_status,
             person.record_id as person_record_id,
             person.last_name,
             person.first_name,
             person.middle_name,
             person.inn,
             person.inn_foreign,
             person.snils,
             person.taxpayer_state,
             person.birth_date,
             person.birth_place,
             person.citizenship,
             person.record_id,
             person.source_id,
             person.old_id,
             person.report_doc,
             person.vip,
             tax.id as book_id_tax_payer_id,
             tax.version as tax_version,
             tax.status as tax_status,
             tax.record_id as tax_record_id,
             tax.inp,
             tax.as_nu,
             doc.id as ref_book_id_doc_id,
             doc.version as doc_version,
             doc.status as doc_status,
             doc.record_id as doc_record_id,
             doc.doc_id,
             doc.doc_number,
             doc.inc_rep,
             tb.id as ref_book_person_tb_id,
             tb.version as tb_version,
             tb.status as tb_status,
             tb.record_id as tb_record_id,
             tb.tb_department_id,
             tb.import_date,
             addr.id as ref_book_address_id,
             addr.version as addr_version,
             addr.status as addr_status,
             addr.record_id as addr_record_id,
             addr.country_id,
             addr.region_code,
             addr.postal_code,
             addr.district,
             addr.city,
             addr.locality,
             addr.street,
             addr.house,
             addr.build,
             addr.appartment,
             addr.address_type,
             addr.address
        from ndfl_person t join ref_book_person person on (replace(nvl(person.inn,'empty'),' ','') = replace(t.inn_np,' ',''))
                           left join ref_book_id_doc doc on (doc.person_id in (select id from ref_book_person where record_id=person.record_id) and doc.status=0)
                           left join ref_book_id_tax_payer tax on (tax.person_id in (select id from ref_book_person where record_id=person.record_id) and tax.status=0)
                           left join ref_book_person_tb tb on (tb.person_id in (select id from ref_book_person where record_id=person.record_id) and tb.status=0)                                                
                           left join ref_book_address addr on (addr.id=person.address and addr.status=0)
       where t.declaration_data_id=p_declaration
         and t.person_id is null
         and exists(select 1 from tmp_version tv where tv.calc_date = v_date and tv.version = person.version and tv.record_id = person.record_id)
         and person.status=0 and person.record_id=person.old_id
      union
      /*По ИННу иностранного государства*/
      select /*+ use_hash(t person) parallel(t 4) parallel(person 4)*/ t.id as person_id,
             person.id as ref_book_person_id,
             person.version as person_version,
             person.status as person_status,
             person.record_id as person_record_id,
             person.last_name,
             person.first_name,
             person.middle_name,
             person.inn,
             person.inn_foreign,
             person.snils,
             person.taxpayer_state,
             person.birth_date,
             person.birth_place,
             person.citizenship,
             person.record_id,
             person.source_id,
             person.old_id,
             person.report_doc,
             person.vip,
             tax.id as book_id_tax_payer_id,
             tax.version as tax_version,
             tax.status as tax_status,
             tax.record_id as tax_record_id,
             tax.inp,
             tax.as_nu,
             doc.id as ref_book_id_doc_id,
             doc.version as doc_version,
             doc.status as doc_status,
             doc.record_id as doc_record_id,
             doc.doc_id,
             doc.doc_number,
             doc.inc_rep,
             tb.id as ref_book_person_tb_id,
             tb.version as tb_version,
             tb.status as tb_status,
             tb.record_id as tb_record_id,
             tb.tb_department_id,
             tb.import_date,             
             addr.id as ref_book_address_id,
             addr.version as addr_version,
             addr.status as addr_status,
             addr.record_id as addr_record_id,
             addr.country_id,
             addr.region_code,
             addr.postal_code,
             addr.district,
             addr.city,
             addr.locality,
             addr.street,
             addr.house,
             addr.build,
             addr.appartment,
             addr.address_type,
             addr.address
        from ndfl_person t join ref_book_person person on (replace(nvl(person.inn_foreign,'empty'),' ','') = replace(t.inn_foreign,' ',''))
                           left join ref_book_id_doc doc on (doc.person_id in (select id from ref_book_person where record_id=person.record_id) and doc.status=0)
                           left join ref_book_id_tax_payer tax on (tax.person_id in (select id from ref_book_person where record_id=person.record_id) and tax.status=0)
                           left join ref_book_person_tb tb on (tb.person_id in (select id from ref_book_person where record_id=person.record_id) and tb.status=0)                                                
                           left join ref_book_address addr on (addr.id=person.address and addr.status=0)
       where t.declaration_data_id=p_declaration
         and t.person_id is null
         and exists(select 1 from tmp_version tv where tv.calc_date = v_date and tv.version = person.version and tv.record_id = person.record_id)
         and person.status=0 and person.record_id=person.old_id
      union
      /*По ДУЛ*/
      select /*+ ordered first_rows(1)*/ t.id as person_id,
             person.id as ref_book_person_id,
             person.version as person_version,
             person.status as person_status,
             person.record_id as person_record_id,
             person.last_name,
             person.first_name,
             person.middle_name,
             person.inn,
             person.inn_foreign,
             person.snils,
             person.taxpayer_state,
             person.birth_date,
             person.birth_place,
             person.citizenship,
             person.record_id,
             person.source_id,
             person.old_id,
             person.report_doc,
             person.vip,             
             tax.id as book_id_tax_payer_id,
             tax.version as tax_version,
             tax.status as tax_status,
             tax.record_id as tax_record_id,
             tax.inp,
             tax.as_nu,
             doc.id as ref_book_id_doc_id,
             doc.version as doc_version,
             doc.status as doc_status,
             doc.record_id as doc_record_id,
             doc.doc_id,
             doc.doc_number,
             doc.inc_rep,
             tb.id as ref_book_person_tb_id,
             tb.version as tb_version,
             tb.status as tb_status,
             tb.record_id as tb_record_id,
             tb.tb_department_id,
             tb.import_date,             
             addr.id as ref_book_address_id,
             addr.version as addr_version,
             addr.status as addr_status,
             addr.record_id as addr_record_id,
             addr.country_id,
             addr.region_code,
             addr.postal_code,
             addr.district,
             addr.city,
             addr.locality,
             addr.street,
             addr.house,
             addr.build,
             addr.appartment,
             addr.address_type,
             addr.address
        from ndfl_person t left join ref_book_doc_type dt on (dt.code=t.id_doc_type)
                           left join ref_book_id_doc doc on (doc.doc_id=dt.id and regexp_replace(lower(doc.doc_number),'[^0-9A-Za-zА-Яа-я]','') = regexp_replace(lower(t.id_doc_number),'[^0-9A-Za-zА-Яа-я]','') and doc.status=0)
                           join (select distinct r1.id,r2.id id1 from ref_book_person r1 join ref_book_person r2 on r1.record_id=r2.record_id) a on (a.id1 = doc.person_id)
                           join ref_book_person person on (person.id = a.id)
                           left join ref_book_id_tax_payer tax on (tax.person_id in (select id from ref_book_person where record_id=person.record_id) and tax.status=0)
                           left join ref_book_person_tb tb on (tb.person_id in (select id from ref_book_person where record_id=person.record_id) and tb.status=0)                                                
                           left join ref_book_address addr on (addr.id=person.address and addr.status=0)
                           join (select distinct calc_date,version,record_id from tmp_version) tv on tv.calc_date = trunc(sysdate) and tv.version = person.version and tv.record_id = person.record_id
       where t.declaration_data_id=p_declaration
         and t.person_id is null
         --and exists(select 1 from tmp_version tv where tv.calc_date = v_date and tv.version = person.version and tv.record_id = person.record_id)
         and person.status=0 and person.record_id=person.old_id
      union
      /*По ИНП*/
      select t.id as person_id,
             person.id as ref_book_person_id,
             person.version as person_version,
             person.status as person_status,
             person.record_id as person_record_id,
             person.last_name,
             person.first_name,
             person.middle_name,
             person.inn,
             person.inn_foreign,
             person.snils,
             person.taxpayer_state,
             person.birth_date,
             person.birth_place,
             person.citizenship,
             person.record_id,
             person.source_id,
             person.old_id,
             person.report_doc,
             person.vip,             
             tax.id as book_id_tax_payer_id,
             tax.version as tax_version,
             tax.status as tax_status,
             tax.record_id as tax_record_id,
             tax.inp,
             tax.as_nu,
             doc.id as ref_book_id_doc_id,
             doc.version as doc_version,
             doc.status as doc_status,
             doc.record_id as doc_record_id,
             doc.doc_id,
             doc.doc_number,
             doc.inc_rep,
             tb.id as ref_book_person_tb_id,
             tb.version as tb_version,
             tb.status as tb_status,
             tb.record_id as tb_record_id,
             tb.tb_department_id,
             tb.import_date,             
             addr.id as ref_book_address_id,
             addr.version as addr_version,
             addr.status as addr_status,
             addr.record_id as addr_record_id,
             addr.country_id,
             addr.region_code,
             addr.postal_code,
             addr.district,
             addr.city,
             addr.locality,
             addr.street,
             addr.house,
             addr.build,
             addr.appartment,
             addr.address_type,
             addr.address
        from ndfl_person t join ref_book_id_tax_payer tax on (tax.as_nu = p_asnu and lower(tax.inp)=lower(t.inp) and tax.status=0)
                           join (select distinct r1.id,r2.id id1 from ref_book_person r1 join ref_book_person r2 on r1.record_id=r2.record_id) a on (a.id1 = tax.person_id)
                           join ref_book_person person on (person.id=a.id)
                           left join ref_book_id_doc doc on (doc.person_id in (select id from ref_book_person where record_id=person.record_id) and doc.status=0)
                           left join ref_book_person_tb tb on (tb.person_id in (select id from ref_book_person where record_id=person.record_id) and tb.status=0)                                                
                           left join ref_book_address addr on (addr.id=person.address and addr.status=0)
       where t.declaration_data_id=p_declaration
         and t.person_id is null
         and exists(select 1 from tmp_version tv where tv.calc_date = v_date and tv.version = person.version and tv.record_id = person.record_id)
         and person.status=0 and person.record_id=person.old_id;
    return v_ref;
  end;

end;
/