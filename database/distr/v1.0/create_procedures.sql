create or replace package person_pkg as
  cursor persons_for_insert(c_declaration number) is
     select n.id,
            n.person_id,
            n.inp,
            n.snils,
            n.last_name,
            n.first_name,
            n.middle_name,
            n.birth_day,
            n.citizenship,
            n.inn_np,
            n.inn_foreign,
            n.id_doc_type,
            n.id_doc_number,
            n.status,
            n.post_index,
            n.region_code,
            n.area,
            n.city,
            n.locality,
            n.street,
            n.house,
            n.building,
            n.flat,
            n.country_code,
            n.address,
            n.additional_data
       from ndfl_person n
      where n.declaration_data_id=c_declaration
        and not exists(select 1 from ref_book_person p where replace(lower(p.last_name),' ','') = replace(lower(n.last_name),' ','')
                                                         and replace(lower(p.first_name),' ','') = replace(lower(n.first_name),' ','')
                                                         and replace(lower(p.middle_name),' ','') = replace(lower(n.middle_name),' ','')
                                                         and p.birth_date=n.birth_day)
        and not exists(select 1 from ref_book_person p where replace(replace(p.snils,' ',''),'-','') = replace(replace(n.snils, ' ', ''), '-', ''))
        and not exists(select 1 from ref_book_person p where replace(p.inn,' ','') = replace(n.inn_np,' ',''))
        and not exists(select 1 from ref_book_person p where replace(p.inn_foreign,' ','') = replace(n.inn_foreign,' ',''));

  cursor persons_for_update(c_declaration number) is
select fv.person_id,
       person.id as ref_book_person_id,
       person.last_name,
       person.first_name,
       person.middle_name,
       person.sex,
       person.inn,
       person.inn_foreign,
       person.snils,
       person.taxpayer_state,
       person.birth_date,
       person.birth_place,
       person.citizenship,
       person.pension,
       person.medical,
       person.social,
       person.employee,
       person.record_id,
       person.source_id,
       person.old_id,
       tax.id as book_id_tax_payer_id,
       tax.inp,
       tax.as_nu,
       doc.id as ref_book_id_doc_id,
       doc.doc_id,
       doc.doc_number,
       doc.issued_by,
       doc.issued_date,
       doc.inc_rep,
       addr.id as ref_book_address_id,
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
     select distinct n.id as person_id,first_value(fv.id) over(partition by n.id) ref_person_id
       from  ndfl_person n join ref_book_person fv on (replace(lower(nvl(fv.last_name,'empty')),' ','') = replace(lower(nvl(n.last_name,'empty')),' ','')
                                                       and replace(lower(nvl(fv.first_name,'empty')),' ','') = replace(lower(nvl(n.first_name,'empty')),' ','')
                                                       and replace(lower(nvl(fv.middle_name,'empty')),' ','') = replace(lower(nvl(n.middle_name,'empty')),' ','')
                                                       and fv.birth_date=n.birth_day
                                                       and replace(replace(nvl(fv.snils,'empty'),' ',''),'-','') = replace(replace(nvl(n.snils,'empty'), ' ', ''), '-', '')
                                                       and replace(nvl(fv.inn,'empty'),' ','') = replace(nvl(n.inn_np,'empty'),' ','')
                                                       and replace(nvl(fv.inn_foreign,'empty'),' ','') = replace(nvl(n.inn_foreign,'empty'),' ','')
                                                       )
      where n.declaration_data_id=c_declaration
                and exists (select 1 from ref_book_person c
                             where replace(lower(nvl(c.last_name,'empty')),' ','') = replace(lower(nvl(n.last_name,'empty')),' ','')
                               and replace(lower(nvl(c.first_name,'empty')),' ','') = replace(lower(nvl(n.first_name,'empty')),' ','')
                               and replace(lower(nvl(c.middle_name,'empty')),' ','') = replace(lower(nvl(n.middle_name,'empty')),' ','')
                               and c.birth_date=n.birth_day
                               and replace(replace(nvl(c.snils,'empty'),' ',''),'-','') = replace(replace(nvl(n.snils,'empty'), ' ', ''), '-', '')
                               and replace(nvl(c.inn,'empty'),' ','') = replace(nvl(n.inn_np,'empty'),' ','')
                               and replace(nvl(c.inn_foreign,'empty'),' ','') = replace(nvl(n.inn_foreign,'empty'),' ','')
                               )
        and exists(select 1 from tmp_version t where t.version = fv.version and t.record_id = fv.record_id)
        ) fv join ref_book_person person on (person.id=fv.ref_person_id)
             left join ref_book_id_tax_payer tax on (tax.person_id=person.id)
             left join ref_book_id_doc doc on (doc.person_id=person.id)
             left join ref_book_address addr on (addr.id=person.address);

  cursor persons_for_check(c_declaration number,c_asnu number) is
       /*По ФИО*/
      select t.id as person_id,
             person.id as ref_book_person_id,
             person.last_name,
             person.first_name,
             person.middle_name,
             person.sex,
             person.inn,
             person.inn_foreign,
             person.snils,
             person.taxpayer_state,
             person.birth_date,
             person.birth_place,
             person.citizenship,
             person.pension,
             person.medical,
             person.social,
             person.employee,
             person.record_id,
             person.source_id,
             person.old_id,
             tax.id as book_id_tax_payer_id,
             tax.inp,
             tax.as_nu,
             doc.id as ref_book_id_doc_id,
             doc.doc_id,
             doc.doc_number,
             doc.issued_by,
             doc.issued_date,
             doc.inc_rep,
             addr.id as ref_book_address_id,
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
        from ndfl_person t join ref_book_person person on (replace(lower(person.last_name),' ','') = replace(lower(t.last_name),' ','') and
                                                                replace(lower(person.first_name),' ','') = replace(lower(t.first_name),' ','') and
                                                                replace(lower(person.middle_name),' ','') = replace(lower(t.middle_name),' ','') and
                                                                person.birth_date=t.birth_day
                                                               )
                           left join ref_book_id_doc doc on (doc.person_id=person.id)
                           left join ref_book_id_tax_payer tax on (tax.person_id = person.id)
                           left join ref_book_address addr on (addr.id=person.address)
       where t.declaration_data_id=c_declaration
         and t.person_id is null
         and exists(select 1 from tmp_version t where t.version = person.version and t.record_id = person.record_id)
      union
       /*по СНИЛСУ*/
      select t.id as person_id,
             person.id as ref_book_person_id,
             person.last_name,
             person.first_name,
             person.middle_name,
             person.sex,
             person.inn,
             person.inn_foreign,
             person.snils,
             person.taxpayer_state,
             person.birth_date,
             person.birth_place,
             person.citizenship,
             person.pension,
             person.medical,
             person.social,
             person.employee,
             person.record_id,
             person.source_id,
             person.old_id,
             tax.id as book_id_tax_payer_id,
             tax.inp,
             tax.as_nu,
             doc.id as ref_book_id_doc_id,
             doc.doc_id,
             doc.doc_number,
             doc.issued_by,
             doc.issued_date,
             doc.inc_rep,
             addr.id as ref_book_address_id,
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
        from ndfl_person t join ref_book_person person on (replace(replace(person.snils,' ',''),'-','') = replace(replace(t.snils, ' ', ''), '-', ''))
                           left join ref_book_id_doc doc on (doc.person_id=person.id)
                           left join ref_book_id_tax_payer tax on (tax.person_id = person.id)
                           left join ref_book_address addr on (addr.id=person.address)
       where t.declaration_data_id=c_declaration
         and t.person_id is null
         and exists(select 1 from tmp_version t where t.version = person.version and t.record_id = person.record_id)
      union 
      /*По ИННу*/
      select t.id as person_id,
             person.id as ref_book_person_id,
             person.last_name,
             person.first_name,
             person.middle_name,
             person.sex,
             person.inn,
             person.inn_foreign,
             person.snils,
             person.taxpayer_state,
             person.birth_date,
             person.birth_place,
             person.citizenship,
             person.pension,
             person.medical,
             person.social,
             person.employee,
             person.record_id,
             person.source_id,
             person.old_id,
             tax.id as book_id_tax_payer_id,
             tax.inp,
             tax.as_nu,
             doc.id as ref_book_id_doc_id,
             doc.doc_id,
             doc.doc_number,
             doc.issued_by,
             doc.issued_date,
             doc.inc_rep,
             addr.id as ref_book_address_id,
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
        from ndfl_person t join ref_book_person person on (replace(person.inn,' ','') = replace(t.inn_np,' ',''))
                           left join ref_book_id_doc doc on (doc.person_id=person.id)
                           left join ref_book_id_tax_payer tax on (tax.person_id = person.id)
                           left join ref_book_address addr on (addr.id=person.address)
       where t.declaration_data_id=c_declaration
         and t.person_id is null
         and exists(select 1 from tmp_version t where t.version = person.version and t.record_id = person.record_id)
      union
      /*По ИННу иностранного государства*/
      select t.id as person_id,
             person.id as ref_book_person_id,
             person.last_name,
             person.first_name,
             person.middle_name,
             person.sex,
             person.inn,
             person.inn_foreign,
             person.snils,
             person.taxpayer_state,
             person.birth_date,
             person.birth_place,
             person.citizenship,
             person.pension,
             person.medical,
             person.social,
             person.employee,
             person.record_id,
             person.source_id,
             person.old_id,
             tax.id as book_id_tax_payer_id,
             tax.inp,
             tax.as_nu,
             doc.id as ref_book_id_doc_id,
             doc.doc_id,
             doc.doc_number,
             doc.issued_by,
             doc.issued_date,
             doc.inc_rep,
             addr.id as ref_book_address_id,
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
        from ndfl_person t join ref_book_person person on (replace(person.inn_foreign,' ','') = replace(t.inn_foreign,' ',''))
                           left join ref_book_id_doc doc on (doc.person_id=person.id)
                           left join ref_book_id_tax_payer tax on (tax.person_id = person.id)
                           left join ref_book_address addr on (addr.id=person.address)
       where t.declaration_data_id=c_declaration
         and t.person_id is null
         and exists(select 1 from tmp_version t where t.version = person.version and t.record_id = person.record_id)
      union
      /*По ДУЛ*/
      select t.id as person_id,
             person.id as ref_book_person_id,
             person.last_name,
             person.first_name,
             person.middle_name,
             person.sex,
             person.inn,
             person.inn_foreign,
             person.snils,
             person.taxpayer_state,
             person.birth_date,
             person.birth_place,
             person.citizenship,
             person.pension,
             person.medical,
             person.social,
             person.employee,
             person.record_id,
             person.source_id,
             person.old_id,
             tax.id as book_id_tax_payer_id,
             tax.inp,
             tax.as_nu,
             doc.id as ref_book_id_doc_id,
             doc.doc_id,
             doc.doc_number,
             doc.issued_by,
             doc.issued_date,
             doc.inc_rep,
             addr.id as ref_book_address_id,
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
                           left join ref_book_id_doc doc on (doc.doc_id=dt.id and replace(lower(doc.doc_number),' ','') = replace(lower(t.id_doc_number),' ',''))
                           left join ref_book_person person on (person.id = doc.person_id)
                           left join ref_book_id_tax_payer tax on (tax.person_id = person.id)
                           left join ref_book_address addr on (addr.id=person.address)
       where t.declaration_data_id=c_declaration
         and t.person_id is null
         and exists(select 1 from tmp_version t where t.version = person.version and t.record_id = person.record_id)
      union
      /*По ИНП*/
      select t.id as person_id,
             person.id as ref_book_person_id,
             person.last_name,
             person.first_name,
             person.middle_name,
             person.sex,
             person.inn,
             person.inn_foreign,
             person.snils,
             person.taxpayer_state,
             person.birth_date,
             person.birth_place,
             person.citizenship,
             person.pension,
             person.medical,
             person.social,
             person.employee,
             person.record_id,
             person.source_id,
             person.old_id,
             tax.id as book_id_tax_payer_id,
             tax.inp,
             tax.as_nu,
             doc.id as ref_book_id_doc_id,
             doc.doc_id,
             doc.doc_number,
             doc.issued_by,
             doc.issued_date,
             doc.inc_rep,
             addr.id as ref_book_address_id,
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
        from ndfl_person t join ref_book_id_tax_payer tax on (tax.as_nu = c_asnu and lower(tax.inp)=lower(t.inp))
                           left join ref_book_person person on (person.id = tax.person_id)
                           left join ref_book_address addr on (addr.id=person.address)
                           left join ref_book_id_doc doc on (doc.person_id=person.id)
       where t.declaration_data_id=c_declaration
         and t.person_id is null
         and exists(select 1 from tmp_version t where t.version = person.version and t.record_id = person.record_id);


  type ref_cursor is ref cursor;
  
  type TPersonsForInsTab is table of persons_for_insert%rowtype;
  type TPersonsForUpdTab is table of persons_for_update%rowtype;
  type TPersonsForCheckTab is table of persons_for_check%rowtype;
  
  procedure FillRecordVersions(p_date date);
  
  function GetPersonForIns(p_declaration number) return ref_cursor;
  
  function GetPersonForUpd(p_declaration number) return ref_cursor;
  
  function GetPersonForCheck(p_declaration number,p_asnu number default 1) return ref_cursor;
  
end;
/
show errors;

create or replace package body person_pkg as

  procedure FillRecordVersions(p_date date)
  is
  begin
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

  function GetPersonForIns(p_declaration number) return ref_cursor
  is
    v_ref ref_cursor;
  begin
    open v_ref for     
       select n.id,
              n.person_id,
              n.inp,
              n.snils,
              n.last_name,
              n.first_name,
              n.middle_name,
              n.birth_day,
              n.citizenship,
              n.inn_np,
              n.inn_foreign,
              n.id_doc_type,
              n.id_doc_number,
              n.status,
              n.post_index,
              n.region_code,
              n.area,
              n.city,
              n.locality,
              n.street,
              n.house,
              n.building,
              n.flat,
              n.country_code,
              n.address,
              n.additional_data
         from ndfl_person n
        where n.declaration_data_id=p_declaration
          and not exists(select 1 from ref_book_person p where replace(lower(p.last_name),' ','') = replace(lower(n.last_name),' ','')
                                                           and replace(lower(p.first_name),' ','') = replace(lower(n.first_name),' ','')
                                                           and replace(lower(p.middle_name),' ','') = replace(lower(n.middle_name),' ','')
                                                           and p.birth_date=n.birth_day)
          and not exists(select 1 from ref_book_person p where replace(replace(p.snils,' ',''),'-','') = replace(replace(n.snils, ' ', ''), '-', ''))
          and not exists(select 1 from ref_book_person p where replace(p.inn,' ','') = replace(n.inn_np,' ',''))
          and not exists(select 1 from ref_book_person p where replace(p.inn_foreign,' ','') = replace(n.inn_foreign,' ',''));
  
    return v_ref;
  end;

  function GetPersonForUpd(p_declaration number) return ref_cursor
  is
    v_ref ref_cursor;
  begin
    open v_ref for     
        select fv.person_id,
               person.id as ref_book_person_id,
               person.last_name,
               person.first_name,
               person.middle_name,
               person.sex,
               person.inn,
               person.inn_foreign,
               person.snils,
               person.taxpayer_state,
               person.birth_date,
               person.birth_place,
               person.citizenship,
               person.pension,
               person.medical,
               person.social,
               person.employee,
               person.record_id,
               person.source_id,
               person.old_id,
               tax.id as book_id_tax_payer_id,
               tax.inp,
               tax.as_nu,
               doc.id as ref_book_id_doc_id,
               doc.doc_id,
               doc.doc_number,
               doc.issued_by,
               doc.issued_date,
               doc.inc_rep,
               addr.id as ref_book_address_id,
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
             select distinct n.id as person_id,first_value(fv.id) over(partition by n.id) ref_person_id
               from  ndfl_person n join ref_book_person fv on (replace(lower(nvl(fv.last_name,'empty')),' ','') = replace(lower(nvl(n.last_name,'empty')),' ','')
                                                               and replace(lower(nvl(fv.first_name,'empty')),' ','') = replace(lower(nvl(n.first_name,'empty')),' ','')
                                                               and replace(lower(nvl(fv.middle_name,'empty')),' ','') = replace(lower(nvl(n.middle_name,'empty')),' ','')
                                                               and fv.birth_date=n.birth_day
                                                               and replace(replace(nvl(fv.snils,'empty'),' ',''),'-','') = replace(replace(nvl(n.snils,'empty'), ' ', ''), '-', '')
                                                               and replace(nvl(fv.inn,'empty'),' ','') = replace(nvl(n.inn_np,'empty'),' ','')
                                                               and replace(nvl(fv.inn_foreign,'empty'),' ','') = replace(nvl(n.inn_foreign,'empty'),' ','')
                                                               )
              where n.declaration_data_id=p_declaration
                and exists (select 1 from ref_book_person c
                             where replace(lower(nvl(c.last_name,'empty')),' ','') = replace(lower(nvl(n.last_name,'empty')),' ','')
                               and replace(lower(nvl(c.first_name,'empty')),' ','') = replace(lower(nvl(n.first_name,'empty')),' ','')
                               and replace(lower(nvl(c.middle_name,'empty')),' ','') = replace(lower(nvl(n.middle_name,'empty')),' ','')
                               and c.birth_date=n.birth_day
                               and replace(replace(nvl(c.snils,'empty'),' ',''),'-','') = replace(replace(nvl(n.snils,'empty'), ' ', ''), '-', '')
                               and replace(nvl(c.inn,'empty'),' ','') = replace(nvl(n.inn_np,'empty'),' ','')
                               and replace(nvl(c.inn_foreign,'empty'),' ','') = replace(nvl(n.inn_foreign,'empty'),' ','')
                               )
                and exists(select 1 from tmp_version t where t.version = fv.version and t.record_id = fv.record_id)
                ) fv join ref_book_person person on (person.id=fv.ref_person_id)
                     left join ref_book_id_tax_payer tax on (tax.person_id=person.id)
                     left join ref_book_id_doc doc on (doc.person_id=person.id)
                     left join ref_book_address addr on (addr.id=person.address);
  
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
             person.last_name,
             person.first_name,
             person.middle_name,
             person.sex,
             person.inn,
             person.inn_foreign,
             person.snils,
             person.taxpayer_state,
             person.birth_date,
             person.birth_place,
             person.citizenship,
             person.pension,
             person.medical,
             person.social,
             person.employee,
             person.record_id,
             person.source_id,
             person.old_id,
             tax.id as book_id_tax_payer_id,
             tax.inp,
             tax.as_nu,
             doc.id as ref_book_id_doc_id,
             doc.doc_id,
             doc.doc_number,
             doc.issued_by,
             doc.issued_date,
             doc.inc_rep,
             addr.id as ref_book_address_id,
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
        from ndfl_person t join ref_book_person person on (replace(lower(person.last_name),' ','') = replace(lower(t.last_name),' ','') and
                                                           replace(lower(person.first_name),' ','') = replace(lower(t.first_name),' ','') and
                                                           replace(lower(person.middle_name),' ','') = replace(lower(t.middle_name),' ','') and
                                                           person.birth_date=t.birth_day
                                                               )
                           left join ref_book_id_doc doc on (doc.person_id=person.id)
                           left join ref_book_id_tax_payer tax on (tax.person_id = person.id)
                           left join ref_book_address addr on (addr.id=person.address)
       where t.declaration_data_id=p_declaration
         and t.person_id is null
         and exists(select 1 from tmp_version t where t.version = person.version and t.record_id = person.record_id)
      union
       /*по СНИЛСУ*/
      select t.id as person_id,
             person.id as ref_book_person_id,
             person.last_name,
             person.first_name,
             person.middle_name,
             person.sex,
             person.inn,
             person.inn_foreign,
             person.snils,
             person.taxpayer_state,
             person.birth_date,
             person.birth_place,
             person.citizenship,
             person.pension,
             person.medical,
             person.social,
             person.employee,
             person.record_id,
             person.source_id,
             person.old_id,
             tax.id as book_id_tax_payer_id,
             tax.inp,
             tax.as_nu,
             doc.id as ref_book_id_doc_id,
             doc.doc_id,
             doc.doc_number,
             doc.issued_by,
             doc.issued_date,
             doc.inc_rep,
             addr.id as ref_book_address_id,
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
        from ndfl_person t join ref_book_person person on (replace(replace(person.snils,' ',''),'-','') = replace(replace(t.snils, ' ', ''), '-', ''))
                           left join ref_book_id_doc doc on (doc.person_id=person.id)
                           left join ref_book_id_tax_payer tax on (tax.person_id = person.id)
                           left join ref_book_address addr on (addr.id=person.address)
       where t.declaration_data_id=p_declaration
         and t.person_id is null
         and exists(select 1 from tmp_version t where t.version = person.version and t.record_id = person.record_id)
      union 
      /*По ИННу*/
      select t.id as person_id,
             person.id as ref_book_person_id,
             person.last_name,
             person.first_name,
             person.middle_name,
             person.sex,
             person.inn,
             person.inn_foreign,
             person.snils,
             person.taxpayer_state,
             person.birth_date,
             person.birth_place,
             person.citizenship,
             person.pension,
             person.medical,
             person.social,
             person.employee,
             person.record_id,
             person.source_id,
             person.old_id,
             tax.id as book_id_tax_payer_id,
             tax.inp,
             tax.as_nu,
             doc.id as ref_book_id_doc_id,
             doc.doc_id,
             doc.doc_number,
             doc.issued_by,
             doc.issued_date,
             doc.inc_rep,
             addr.id as ref_book_address_id,
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
        from ndfl_person t join ref_book_person person on (replace(person.inn,' ','') = replace(t.inn_np,' ',''))
                           left join ref_book_id_doc doc on (doc.person_id=person.id)
                           left join ref_book_id_tax_payer tax on (tax.person_id = person.id)
                           left join ref_book_address addr on (addr.id=person.address)
       where t.declaration_data_id=p_declaration
         and t.person_id is null
         and exists(select 1 from tmp_version t where t.version = person.version and t.record_id = person.record_id)
      union
      /*По ИННу иностранного государства*/
      select t.id as person_id,
             person.id as ref_book_person_id,
             person.last_name,
             person.first_name,
             person.middle_name,
             person.sex,
             person.inn,
             person.inn_foreign,
             person.snils,
             person.taxpayer_state,
             person.birth_date,
             person.birth_place,
             person.citizenship,
             person.pension,
             person.medical,
             person.social,
             person.employee,
             person.record_id,
             person.source_id,
             person.old_id,
             tax.id as book_id_tax_payer_id,
             tax.inp,
             tax.as_nu,
             doc.id as ref_book_id_doc_id,
             doc.doc_id,
             doc.doc_number,
             doc.issued_by,
             doc.issued_date,
             doc.inc_rep,
             addr.id as ref_book_address_id,
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
        from ndfl_person t join ref_book_person person on (replace(person.inn_foreign,' ','') = replace(t.inn_foreign,' ',''))
                           left join ref_book_id_doc doc on (doc.person_id=person.id)
                           left join ref_book_id_tax_payer tax on (tax.person_id = person.id)
                           left join ref_book_address addr on (addr.id=person.address)
       where t.declaration_data_id=p_declaration
         and t.person_id is null
         and exists(select 1 from tmp_version t where t.version = person.version and t.record_id = person.record_id)
      union
      /*По ДУЛ*/
      select t.id as person_id,
             person.id as ref_book_person_id,
             person.last_name,
             person.first_name,
             person.middle_name,
             person.sex,
             person.inn,
             person.inn_foreign,
             person.snils,
             person.taxpayer_state,
             person.birth_date,
             person.birth_place,
             person.citizenship,
             person.pension,
             person.medical,
             person.social,
             person.employee,
             person.record_id,
             person.source_id,
             person.old_id,
             tax.id as book_id_tax_payer_id,
             tax.inp,
             tax.as_nu,
             doc.id as ref_book_id_doc_id,
             doc.doc_id,
             doc.doc_number,
             doc.issued_by,
             doc.issued_date,
             doc.inc_rep,
             addr.id as ref_book_address_id,
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
                           left join ref_book_id_doc doc on (doc.doc_id=dt.id and replace(lower(doc.doc_number),' ','') = replace(lower(t.id_doc_number),' ',''))
                           left join ref_book_person person on (person.id = doc.person_id)
                           left join ref_book_id_tax_payer tax on (tax.person_id = person.id)
                           left join ref_book_address addr on (addr.id=person.address)
       where t.declaration_data_id=p_declaration
         and t.person_id is null
         and exists(select 1 from tmp_version t where t.version = person.version and t.record_id = person.record_id)
      union
      /*По ИНП*/
      select t.id as person_id,
             person.id as ref_book_person_id,
             person.last_name,
             person.first_name,
             person.middle_name,
             person.sex,
             person.inn,
             person.inn_foreign,
             person.snils,
             person.taxpayer_state,
             person.birth_date,
             person.birth_place,
             person.citizenship,
             person.pension,
             person.medical,
             person.social,
             person.employee,
             person.record_id,
             person.source_id,
             person.old_id,
             tax.id as book_id_tax_payer_id,
             tax.inp,
             tax.as_nu,
             doc.id as ref_book_id_doc_id,
             doc.doc_id,
             doc.doc_number,
             doc.issued_by,
             doc.issued_date,
             doc.inc_rep,
             addr.id as ref_book_address_id,
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
        from ndfl_person t join ref_book_id_tax_payer tax on (tax.as_nu = p_asnu and lower(tax.inp)=lower(t.inp))
                           left join ref_book_person person on (person.id = tax.person_id)
                           left join ref_book_address addr on (addr.id=person.address)
                           left join ref_book_id_doc doc on (doc.person_id=person.id)
       where t.declaration_data_id=p_declaration
         and t.person_id is null
         and exists(select 1 from tmp_version t where t.version = person.version and t.record_id = person.record_id);
  
    return v_ref;
  end;

end person_pkg;
/
show errors;

exit;
