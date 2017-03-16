create or replace package person_pkg as
/* Пакет для идентификации физических лиц*/
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
            n.additional_data,
            to_number(null) sex, 
            to_number(null) pension, 
            to_number(null) medical, 
            to_number(null) social, 
            to_char(null) correct_num, 
            to_char(null) period, 
            to_char(null) rep_period, 
            to_char(null) num, 
            null sv_date
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
           person.version as person_version,
           person.status as person_status,
           person.record_id as person_record_id,
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
           doc.issued_by,
           doc.issued_date,
           doc.inc_rep,
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
             person.version as person_version,
             person.status as person_status,
             person.record_id as person_record_id,   
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
             doc.issued_by,
             doc.issued_date,
             doc.inc_rep,
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
             person.version as person_version,
             person.status as person_status,
             person.record_id as person_record_id,   
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
             doc.issued_by,
             doc.issued_date,
             doc.inc_rep,
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
             person.version as person_version,
             person.status as person_status,
             person.record_id as person_record_id,   
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
             doc.issued_by,
             doc.issued_date,
             doc.inc_rep,
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
             person.version as person_version,
             person.status as person_status,
             person.record_id as person_record_id,   
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
             doc.issued_by,
             doc.issued_date,
             doc.inc_rep,
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
             person.version as person_version,
             person.status as person_status,
             person.record_id as person_record_id,   
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
             doc.issued_by,
             doc.issued_date,
             doc.inc_rep,
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
             person.version as person_version,
             person.status as person_status,
             person.record_id as person_record_id,   
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
             doc.issued_by,
             doc.issued_date,
             doc.inc_rep,
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
  
  procedure FillRecordVersions(p_date date default trunc(sysdate));
  
  -- Получение курсоров для идентификации НДФЛ
  function GetPersonForIns(p_declaration number) return ref_cursor;
  function GetPersonForUpd(p_declaration number) return ref_cursor;
  function GetPersonForCheck(p_declaration number,p_asnu number default 1) return ref_cursor;
  -- Получение курсоров для идентификации 115
  function GetPersonForIns115(p_declaration number) return ref_cursor;
  function GetPersonForUpd115(p_declaration number) return ref_cursor;
  function GetPersonForCheck115(p_declaration number,p_asnu number default 1) return ref_cursor;
  
end;
/
show errors;

create or replace package body person_pkg as
  
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

  -- Получение курсоров для идентификации НДФЛ
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
              n.additional_data,
              null sex, 
              null pension, 
              null medical, 
              null social, 
              null correct_num, 
              null period, 
              null rep_period, 
              null num, 
              null sv_date
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
               person.version as person_version,
               person.status as person_status,
               person.record_id as person_record_id,
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
               doc.issued_by,
               doc.issued_date,
               doc.inc_rep,
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
                and exists(select 1 from tmp_version t where t.calc_date = v_date and t.version = fv.version and t.record_id = fv.record_id)
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
             person.version as person_version,
             person.status as person_status,
             person.record_id as person_record_id,   
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
             doc.issued_by,
             doc.issued_date,
             doc.inc_rep,
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
         and exists(select 1 from tmp_version t where t.calc_date = v_date and t.version = person.version and t.record_id = person.record_id)
      union
       /*по СНИЛСУ*/
      select t.id as person_id,
             person.id as ref_book_person_id,
             person.version as person_version,
             person.status as person_status,
             person.record_id as person_record_id,   
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
             doc.issued_by,
             doc.issued_date,
             doc.inc_rep,
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
        from ndfl_person t join ref_book_person person on (replace(replace(person.snils,' ',''),'-','') = replace(replace(t.snils, ' ', ''), '-', ''))
                           left join ref_book_id_doc doc on (doc.person_id=person.id)
                           left join ref_book_id_tax_payer tax on (tax.person_id = person.id)
                           left join ref_book_address addr on (addr.id=person.address)
       where t.declaration_data_id=p_declaration
         and t.person_id is null
         and exists(select 1 from tmp_version t where t.calc_date = v_date and t.version = person.version and t.record_id = person.record_id)
      union 
      /*По ИННу*/
      select t.id as person_id,
             person.id as ref_book_person_id,
             person.version as person_version,
             person.status as person_status,
             person.record_id as person_record_id,   
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
             doc.issued_by,
             doc.issued_date,
             doc.inc_rep,
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
        from ndfl_person t join ref_book_person person on (replace(person.inn,' ','') = replace(t.inn_np,' ',''))
                           left join ref_book_id_doc doc on (doc.person_id=person.id)
                           left join ref_book_id_tax_payer tax on (tax.person_id = person.id)
                           left join ref_book_address addr on (addr.id=person.address)
       where t.declaration_data_id=p_declaration
         and t.person_id is null
         and exists(select 1 from tmp_version t where t.calc_date = v_date and t.version = person.version and t.record_id = person.record_id)
      union
      /*По ИННу иностранного государства*/
      select t.id as person_id,
             person.id as ref_book_person_id,
             person.version as person_version,
             person.status as person_status,
             person.record_id as person_record_id,   
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
             doc.issued_by,
             doc.issued_date,
             doc.inc_rep,
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
        from ndfl_person t join ref_book_person person on (replace(person.inn_foreign,' ','') = replace(t.inn_foreign,' ',''))
                           left join ref_book_id_doc doc on (doc.person_id=person.id)
                           left join ref_book_id_tax_payer tax on (tax.person_id = person.id)
                           left join ref_book_address addr on (addr.id=person.address)
       where t.declaration_data_id=p_declaration
         and t.person_id is null
         and exists(select 1 from tmp_version t where t.calc_date = v_date and t.version = person.version and t.record_id = person.record_id)
      union
      /*По ДУЛ*/
      select t.id as person_id,
             person.id as ref_book_person_id,
             person.version as person_version,
             person.status as person_status,
             person.record_id as person_record_id,   
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
             doc.issued_by,
             doc.issued_date,
             doc.inc_rep,
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
                           left join ref_book_id_doc doc on (doc.doc_id=dt.id and replace(lower(doc.doc_number),' ','') = replace(lower(t.id_doc_number),' ',''))
                           left join ref_book_person person on (person.id = doc.person_id)
                           left join ref_book_id_tax_payer tax on (tax.person_id = person.id)
                           left join ref_book_address addr on (addr.id=person.address)
       where t.declaration_data_id=p_declaration
         and t.person_id is null
         and exists(select 1 from tmp_version t where t.calc_date = v_date and t.version = person.version and t.record_id = person.record_id)
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
             doc.issued_by,
             doc.issued_date,
             doc.inc_rep,
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
        from ndfl_person t join ref_book_id_tax_payer tax on (tax.as_nu = p_asnu and lower(tax.inp)=lower(t.inp))
                           left join ref_book_person person on (person.id = tax.person_id)
                           left join ref_book_address addr on (addr.id=person.address)
                           left join ref_book_id_doc doc on (doc.person_id=person.id)
       where t.declaration_data_id=p_declaration
         and t.person_id is null
         and exists(select 1 from tmp_version t where t.calc_date = v_date and t.version = person.version and t.record_id = person.record_id);
  
    return v_ref;
  end;

  -- Получение курсоров для идентификации 115
  function GetPersonForIns115(p_declaration number) return ref_cursor
  is
    v_ref ref_cursor;
  begin
    open v_ref for     
       select n.id,
              n.person_id,
              null inp,
              n.snils,
              n.familia last_name,
              n.imya first_name,
              n.otchestvo middle_name,
              n.data_rozd birth_day,
              n.grazd citizenship,
              n.innfl inn_np,
              null inn_foreign,
              n.kod_vid_doc id_doc_type,
              n.ser_nom_doc id_doc_number,
              null status,
              null post_index,
              null region_code,
              null area,
              null city,
              null locality,
              null street,
              null house,
              null building,
              null flat,
              null country_code,
              null address,
              null additional_data,
              n.pol sex, 
              n.priz_ops pension, 
              n.priz_oms medical, 
              n.priz_oss social, 
              n.nom_korr correct_num, 
              n.period period, 
              n.otchet_god rep_period, 
              n.nomer num, 
              n.sv_data sv_date
         from raschsv_pers_sv_strah_lic n
        where n.declaration_data_id=p_declaration
          and not exists(select 1 from ref_book_person p where replace(lower(p.last_name),' ','') = replace(lower(n.familia),' ','')
                                                           and replace(lower(p.first_name),' ','') = replace(lower(n.imya),' ','')
                                                           and replace(lower(p.middle_name),' ','') = replace(lower(n.otchestvo),' ','')
                                                           and p.birth_date=n.data_rozd)
          and not exists(select 1 from ref_book_person p where replace(replace(p.snils,' ',''),'-','') = replace(replace(n.snils, ' ', ''), '-', ''))
          and not exists(select 1 from ref_book_person p where replace(p.inn,' ','') = replace(n.innfl,' ',''));
  
    return v_ref;
  end;
  
  function GetPersonForUpd115(p_declaration number) return ref_cursor
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
               doc.issued_by,
               doc.issued_date,
               doc.inc_rep,
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
               from  raschsv_pers_sv_strah_lic n join ref_book_person fv on (replace(lower(nvl(fv.last_name,'empty')),' ','') = replace(lower(nvl(n.familia,'empty')),' ','')
                                                                             and replace(lower(nvl(fv.first_name,'empty')),' ','') = replace(lower(nvl(n.imya,'empty')),' ','')
                                                                             and replace(lower(nvl(fv.middle_name,'empty')),' ','') = replace(lower(nvl(n.otchestvo,'empty')),' ','')
                                                                             and fv.birth_date=n.data_rozd
                                                                             and replace(replace(nvl(fv.snils,'empty'),' ',''),'-','') = replace(replace(nvl(n.snils,'empty'), ' ', ''), '-', '')
                                                                             and replace(nvl(fv.inn,'empty'),' ','') = replace(nvl(n.innfl,'empty'),' ','')
                                                                             )
              where n.declaration_data_id=p_declaration
                and exists (select 1 from ref_book_person c
                             where replace(lower(nvl(c.last_name,'empty')),' ','') = replace(lower(nvl(n.familia,'empty')),' ','')
                               and replace(lower(nvl(c.first_name,'empty')),' ','') = replace(lower(nvl(n.imya,'empty')),' ','')
                               and replace(lower(nvl(c.middle_name,'empty')),' ','') = replace(lower(nvl(n.otchestvo,'empty')),' ','')
                               and c.birth_date=n.data_rozd
                               and replace(replace(nvl(c.snils,'empty'),' ',''),'-','') = replace(replace(nvl(n.snils,'empty'), ' ', ''), '-', '')
                               and replace(nvl(c.inn,'empty'),' ','') = replace(nvl(n.innfl,'empty'),' ','')
                               )
                and exists(select 1 from tmp_version t where t.calc_date = v_date and t.version = fv.version and t.record_id = fv.record_id)
                ) fv join ref_book_person person on (person.id=fv.ref_person_id)
                     left join ref_book_id_tax_payer tax on (tax.person_id=person.id)
                     left join ref_book_id_doc doc on (doc.person_id=person.id)
                     left join ref_book_address addr on (addr.id=person.address);
  
    return v_ref;
  end;
  
  function GetPersonForCheck115(p_declaration number,p_asnu number default 1) return ref_cursor
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
             doc.issued_by,
             doc.issued_date,
             doc.inc_rep,
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
        from raschsv_pers_sv_strah_lic t join ref_book_person person on (replace(lower(person.last_name),' ','') = replace(lower(t.familia),' ','') and
                                                                         replace(lower(person.first_name),' ','') = replace(lower(t.imya),' ','') and
                                                                         replace(lower(person.middle_name),' ','') = replace(lower(t.otchestvo),' ','') and
                                                                         person.birth_date=t.data_rozd
                                                                        )
                           left join ref_book_id_doc doc on (doc.person_id=person.id)
                           left join ref_book_id_tax_payer tax on (tax.person_id = person.id)
                           left join ref_book_address addr on (addr.id=person.address)
       where t.declaration_data_id=p_declaration
         and t.person_id is null
         and exists(select 1 from tmp_version t where t.calc_date = v_date and t.version = person.version and t.record_id = person.record_id)
      union
       /*по СНИЛСУ*/
      select t.id as person_id,
             person.id as ref_book_person_id,
             person.version as person_version,
             person.status as person_status,
             person.record_id as person_record_id, 
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
             doc.issued_by,
             doc.issued_date,
             doc.inc_rep,
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
        from raschsv_pers_sv_strah_lic t join ref_book_person person on (replace(replace(person.snils,' ',''),'-','') = replace(replace(t.snils, ' ', ''), '-', ''))
                           left join ref_book_id_doc doc on (doc.person_id=person.id)
                           left join ref_book_id_tax_payer tax on (tax.person_id = person.id)
                           left join ref_book_address addr on (addr.id=person.address)
       where t.declaration_data_id=p_declaration
         and t.person_id is null
         and exists(select 1 from tmp_version t where t.calc_date = v_date and t.version = person.version and t.record_id = person.record_id)
      union 
      /*По ИННу*/
      select t.id as person_id,
             person.id as ref_book_person_id,
             person.version as person_version,
             person.status as person_status,
             person.record_id as person_record_id, 
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
             doc.issued_by,
             doc.issued_date,
             doc.inc_rep,
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
        from raschsv_pers_sv_strah_lic t join ref_book_person person on (replace(person.inn,' ','') = replace(t.innfl,' ',''))
                           left join ref_book_id_doc doc on (doc.person_id=person.id)
                           left join ref_book_id_tax_payer tax on (tax.person_id = person.id)
                           left join ref_book_address addr on (addr.id=person.address)
       where t.declaration_data_id=p_declaration
         and t.person_id is null
         and exists(select 1 from tmp_version t where t.calc_date = v_date and t.version = person.version and t.record_id = person.record_id)
      union
      /*По ДУЛ*/
      select t.id as person_id,
             person.id as ref_book_person_id,
             person.version as person_version,
             person.status as person_status,
             person.record_id as person_record_id, 
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
             doc.issued_by,
             doc.issued_date,
             doc.inc_rep,
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
        from raschsv_pers_sv_strah_lic t left join ref_book_doc_type dt on (dt.code=t.kod_vid_doc)
                           left join ref_book_id_doc doc on (doc.doc_id=dt.id and replace(lower(doc.doc_number),' ','') = replace(lower(t.ser_nom_doc),' ',''))
                           left join ref_book_person person on (person.id = doc.person_id)
                           left join ref_book_id_tax_payer tax on (tax.person_id = person.id)
                           left join ref_book_address addr on (addr.id=person.address)
       where t.declaration_data_id=p_declaration
         and t.person_id is null
         and exists(select 1 from tmp_version t where t.calc_date = v_date and t.version = person.version and t.record_id = person.record_id);
  
    return v_ref;
  end;

end person_pkg;
/
show errors;

create or replace package fias_pkg
/* Пакет для работы со справочником ФИАС */
as
  
  cursor fias_addrs(c_region varchar2,c_area varchar2,c_city varchar2,c_locality varchar2,c_street varchar2,
                    c_area_type varchar2,c_city_type varchar2,c_locality_type varchar2,c_street_type varchar2,
                    c_index varchar2) is
    /*поиск по всем параметрам*/
    select c.id,c.regioncode,c.formalname,c.shortname,c.aolevel,c.currstatus,c.postalcode,
           substr(sys_connect_by_path(formalname,','),2) full_addr
      from fias_addrobj c
     where c.livestatus=1
       and c.regioncode=c_region
       and replace(lower(c.formalname),' ','')=nvl(replace(lower(c_street),' ',''),replace(lower(nvl(c_locality,c_city)),' ',''))
       and (nvl(trim(lower(c_street_type)),trim(lower(nvl(c_locality_type,c_city_type)))) is null or 
            trim(lower(c.shortname))=nvl(trim(lower(c_street_type)),trim(lower(nvl(c_locality_type,c_city_type)))))
       and (c.postalcode=c_index)
    connect by prior c.aoid=c.parentguid
      start with c.livestatus=1
        and c.regioncode=c_region
        and replace(lower(c.formalname),' ','')=nvl(replace(lower(c_area),' ',''),replace(lower(c_city),' ',''))
        and (nvl(replace(lower(c_area_type),' ',''),replace(lower(c_city_type),' ','')) is null or 
             trim(lower(c.shortname))=nvl(replace(lower(c_area_type),' ',''),replace(lower(c_city_type),' ','')))
    union
    /*поиск без учета почтового индекса*/
    select c.id,c.regioncode,c.formalname,c.shortname,c.aolevel,c.currstatus,c.postalcode,
           substr(sys_connect_by_path(formalname,','),2) full_addr
      from fias_addrobj c
     where c.livestatus=1
       and c.regioncode=c_region
       and replace(lower(c.formalname),' ','')=nvl(replace(lower(c_street),' ',''),replace(lower(nvl(c_locality,c_city)),' ',''))
       and (nvl(trim(lower(c_street_type)),trim(lower(nvl(c_locality_type,c_city_type)))) is null or 
            trim(lower(c.shortname))=nvl(trim(lower(c_street_type)),trim(lower(nvl(c_locality_type,c_city_type)))))
    connect by prior c.aoid=c.parentguid
      start with c.livestatus=1
        and c.regioncode=c_region
        and replace(lower(c.formalname),' ','')=nvl(replace(lower(c_area),' ',''),replace(lower(c_city),' ',''))
        and (nvl(replace(lower(c_area_type),' ',''),replace(lower(c_city_type),' ','')) is null or 
             trim(lower(c.shortname))=nvl(replace(lower(c_area_type),' ',''),replace(lower(c_city_type),' ','')))
    union
    /*поиск без учета типов объектов*/
    select c.id,c.regioncode,c.formalname,c.shortname,c.aolevel,c.currstatus,c.postalcode,
           substr(sys_connect_by_path(formalname,','),2) full_addr
      from fias_addrobj c
     where c.livestatus=1
       and c.regioncode=c_region
       and replace(lower(c.formalname),' ','')=nvl(replace(lower(c_street),' ',''),replace(lower(nvl(c_locality,c_city)),' ',''))
    connect by prior c.aoid=c.parentguid
      start with c.livestatus=1
        and c.regioncode=c_region
        and replace(lower(c.formalname),' ','')=nvl(replace(lower(c_area),' ',''),replace(lower(c_city),' ',''));
  
  type ref_cursor is ref cursor;
  
  type TCheckAddrByFias is record
  (
    id ndfl_person.id%type,
    post_index ndfl_person.post_index%type,
    region_code ndfl_person.region_code%type,
    area ndfl_person.area%type,
    city ndfl_person.city%type,
    locality ndfl_person.locality%type,
    street ndfl_person.street%type,
    ndfl_full_addr varchar2(2000 char),
    area_type varchar2(10 char),
    area_fname varchar2(200 char),
    city_type varchar2(10 char),
    city_fname varchar2(200 char),
    loc_type varchar2(10 char),
    loc_fname varchar2(200 char),
    street_type varchar2(10 char),
    street_fname varchar2(200 char),
    fias_id fias_addrobj.id%type,
    fias_index fias_addrobj.postalcode%type,
    fias_street  fias_addrobj.formalname%type,
    fias_street_type  fias_addrobj.shortname%type,
    fias_city_id  fias_addrobj.id%type,
    fias_city_name  fias_addrobj.formalname%type
  );
  type TTblFiasAddr is table of fias_addrs%rowtype;
  type TTblCheckAddrByFias is table of TCheckAddrByFias;
  
  function GetFiasAddrs(p_region varchar2,p_area varchar2,p_city varchar2,p_locality varchar2,p_street varchar2,
                        p_area_type varchar2,p_city_type varchar2,p_locality_type varchar2,p_street_type varchar2,
                        p_post_index varchar2) return TTblFiasAddr pipelined;
  
  function CheckAddrByFias(p_declaration number) return ref_cursor;
  
end fias_pkg;
/
show errors;

create or replace package body fias_pkg as
  
  cursor ndfl_rec(c_ndfl number) is
    select n.id,
           n.post_index,n.region_code,n.area,n.city,n.locality,n.street,
           case when instr(lower(n.area)||' ','р-н. ')>0 then substr(n.area||' ',instr(lower(n.area)||' ','р-н. '),4)
                else ''
           end area_type,
           case when instr(lower(n.area),' р-н.')>0 then trim(substr(n.area,1,instr(lower(n.area),'р-н.')-1))
                else n.city
           end area_fname,
           case when instr(lower(n.city)||' ','г ')>0 then substr(n.city||' ',instr(lower(n.city)||' ','г '),2) 
                when instr(lower(n.city)||' ','тер. ')>0 then substr(n.city||' ',instr(lower(n.city)||' ','тер. '),4)
                else ''
           end city_type,
           case when instr(lower(n.city),'г ')>0 then trim(substr(n.city,instr(lower(n.city),'г '),length(n.city)-2))
                when instr(lower(n.city),' г')>0 then trim(substr(n.city,1,instr(lower(n.city),' г')-1))
                when instr(lower(n.city),'тер. ')>0 then trim(substr(n.city,instr(lower(n.city),'тер. ')+4))
                else n.city
           end city_fname,
           case when instr(lower(n.street)||' ','ул ')>0 then substr(n.street||' ',instr(lower(n.street)||' ','ул '),3) 
                when instr(lower(n.street)||' ','ул. ')>0 then substr(n.street||' ',instr(lower(n.street)||' ','ул. '),4) 
                else ''
           end street_type,
           case when instr(lower(n.street),'ул ')>0 then trim(substr(n.street,instr(lower(n.street),'ул ')+2))
                when instr(lower(n.street),' ул')>0 then trim(substr(n.street,1,instr(lower(n.street),' ул')-1))
                when instr(lower(n.street),'ул. ')>0 then trim(substr(n.street,instr(lower(n.street),'ул. ')+3))
                when instr(lower(n.street),' ул.')>0 then trim(substr(n.street,1,instr(lower(n.street),' ул.')-1))
                else n.street
           end street_fname,
           case when instr(lower(n.locality)||' ','г ')>0 then substr(n.locality||' ',instr(lower(n.locality)||' ','г '),2) 
                when instr(lower(n.locality)||' ','тер. ')>0 then substr(n.locality||' ',instr(lower(n.locality)||' ','тер. '),4)
                when instr(lower(n.locality)||' ','аул. ')>0 then substr(n.locality||' ',instr(lower(n.locality)||' ','аул. '),5)
                else ''
           end loc_type,
           case when instr(lower(n.locality),'г ')>0 then trim(substr(n.locality,instr(lower(n.locality),'г '),length(n.locality)-2))
                when instr(lower(n.locality),' г')>0 then trim(substr(n.locality,1,instr(lower(n.locality),' г')-1))
                when instr(lower(n.locality),'тер. ')>0 then trim(substr(n.locality,instr(lower(n.locality),'тер. ')+4))
                when instr(lower(n.locality),'аул. ')>0 then trim(substr(n.locality,instr(lower(n.locality),'аул. ')+4))
                else n.locality
           end loc_fname
      from ndfl_person n
     where n.id=c_ndfl;

                 
  function GetFiasAddrs(p_region varchar2,p_area varchar2,p_city varchar2,p_locality varchar2,p_street varchar2,
                        p_area_type varchar2,p_city_type varchar2,p_locality_type varchar2,p_street_type varchar2,
                        p_post_index varchar2) return TTblFiasAddr pipelined 
  is
    tbl TTblFiasAddr:=TTblFiasAddr();
  begin
    if fias_addrs%isopen then
      close fias_addrs;
    end if;
    open fias_addrs(p_region,p_area,p_city,p_locality,p_street,
                    p_area_type,p_city_type,p_locality_type,p_street_type,p_post_index);
    fetch fias_addrs bulk collect into tbl;
    close fias_addrs;
    
    if tbl.count>0 then
      for i in 1..tbl.count loop
        if tbl.exists(i) then
          pipe row (tbl(i));
        end if;
      end loop;
    end if;
   end GetFiasAddrs;

  function CheckAddrByFias(p_declaration number) return ref_cursor
  is
    v_ref ref_cursor;
  begin
    open v_ref for
      select n.id,n.post_index,n.region_code,n.area,n.city,n.locality,n.street,
             n.ndfl_full_addr,n.area_type,n.area_fname,n.city_type,n.city_fname,n.loc_type,n.loc_fname,n.street_type,n.street_fname,
             f.id fias_id,
             f.postalcode fias_index,
             f.formalname fias_street,
             f.shortname fias_street_type,
             fc.id fias_city_id,
             fc.formalname fias_city_name
        from (
              select tab.*,
                     nvl2(tab.area_fname,tab.area_fname||',','')||nvl2(tab.city_fname,tab.city_fname||',','')||
                     nvl2(tab.loc_fname,tab.loc_fname||',','')||nvl2(tab.street_fname,tab.street_fname||',','') ndfl_full_addr,
                     (select id
                        from table(fias_pkg.GetFiasAddrs(tab.region_code,trim(lower(tab.area_fname)),trim(lower(tab.city_fname)),trim(lower(tab.loc_fname)),trim(lower(tab.street_fname)),
                                                         trim(lower(tab.area_type)),trim(lower(tab.city_type)),trim(lower(tab.loc_type)),trim(lower(tab.street_type)),tab.post_index)) f
                       where lower(f.full_addr||',')=lower(nvl2(tab.area_fname,tab.area_fname||',','')||nvl2(tab.city_fname,tab.city_fname||',','')||
                                                           nvl2(tab.loc_fname,tab.loc_fname||',','')||nvl2(tab.street_fname,tab.street_fname||',',''))
                         ) fa_id
                from (
                      select n.id,
                             n.post_index,n.region_code,n.area,n.city,n.locality,n.street,
                             case when instr(lower(n.area)||' ','р-н. ')>0 then substr(n.area||' ',instr(lower(n.area)||' ','р-н. '),4)
                                  else ''
                             end area_type,
                             case when instr(lower(n.area),' р-н.')>0 then trim(substr(n.area,1,instr(lower(n.area),'р-н.')-1))
                                  else n.area
                             end area_fname,
                             case when instr(lower(n.city)||' ','г ')>0 then substr(n.city||' ',instr(lower(n.city)||' ','г '),2) 
                                  when instr(lower(n.city)||' ','тер. ')>0 then substr(n.city||' ',instr(lower(n.city)||' ','тер. '),4)
                                  else ''
                             end city_type,
                             case when instr(lower(n.city),'г ')>0 then trim(substr(n.city,instr(lower(n.city),'г '),length(n.city)-2))
                                  when instr(lower(n.city),' г')>0 then trim(substr(n.city,1,instr(lower(n.city),' г')-1))
                                  when instr(lower(n.city),'тер. ')>0 then trim(substr(n.city,instr(lower(n.city),'тер. ')+4))
                                  else n.city
                             end city_fname,
                             case when instr(lower(n.street)||' ','ул ')>0 then substr(n.street||' ',instr(lower(n.street)||' ','ул '),3) 
                                  when instr(lower(n.street)||' ','ул. ')>0 then substr(n.street||' ',instr(lower(n.street)||' ','ул. '),4) 
                                  else ''
                             end street_type,
                             case when instr(lower(n.street),'ул ')>0 then trim(substr(n.street,instr(lower(n.street),'ул ')+2))
                                  when instr(lower(n.street),' ул')>0 then trim(substr(n.street,1,instr(lower(n.street),' ул')-1))
                                  when instr(lower(n.street),'ул. ')>0 then trim(substr(n.street,instr(lower(n.street),'ул. ')+3))
                                  when instr(lower(n.street),' ул.')>0 then trim(substr(n.street,1,instr(lower(n.street),' ул.')-1))
                                  else n.street
                             end street_fname,
                             case when instr(lower(n.locality)||' ','г ')>0 then substr(n.locality||' ',instr(lower(n.locality)||' ','г '),2) 
                                  when instr(lower(n.locality)||' ','тер. ')>0 then substr(n.locality||' ',instr(lower(n.locality)||' ','тер. '),4)
                                  when instr(lower(n.locality)||' ','аул. ')>0 then substr(n.locality||' ',instr(lower(n.locality)||' ','аул. '),5)
                                  else ''
                             end loc_type,
                             case when instr(lower(n.locality),'г ')>0 then trim(substr(n.locality,instr(lower(n.locality),'г '),length(n.locality)-2))
                                  when instr(lower(n.locality),' г')>0 then trim(substr(n.locality,1,instr(lower(n.locality),' г')-1))
                                  when instr(lower(n.locality),'тер. ')>0 then trim(substr(n.locality,instr(lower(n.locality),'тер. ')+4))
                                  when instr(lower(n.locality),'аул. ')>0 then trim(substr(n.locality,instr(lower(n.locality),'аул. ')+4))
                                  else n.locality
                             end loc_fname
                        from ndfl_person n
                       where n.declaration_data_id=p_declaration
                      ) tab
                ) n left join fias_addrobj f on (f.id=n.fa_id) left join fias_addrobj fc on (fc.id=f.parentguid);
    
    return v_ref;
  end;

end fias_pkg;
/
show errors;
