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
                                                           and p.birth_date=n.birth_day
                                                           and p.status=0)
          and not exists(select 1 from ref_book_person p where replace(replace(p.snils,' ',''),'-','') = replace(replace(n.snils, ' ', ''), '-', '') and p.status=0)
          and not exists(select 1 from ref_book_person p where replace(p.inn,' ','') = replace(n.inn_np,' ','') and p.status=0)
          and not exists(select 1 from ref_book_person p where replace(p.inn_foreign,' ','') = replace(n.inn_foreign,' ','') and p.status=0);
  
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
                                                           and p.birth_date=n.data_rozd
                                                           and p.status=0)
          and not exists(select 1 from ref_book_person p where replace(replace(p.snils,' ',''),'-','') = replace(replace(n.snils, ' ', ''), '-', '') and p.status=0)
          and not exists(select 1 from ref_book_person p where replace(p.inn,' ','') = replace(n.innfl,' ','') and p.status=0);
  
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
-- Пакет для поиска адресов в справочнике ФИАС
as
  
  cursor fias_addrs(c_region varchar2,c_area varchar2,c_city varchar2,c_locality varchar2,c_street varchar2,
                    c_area_type varchar2,c_city_type varchar2,c_locality_type varchar2,c_street_type varchar2,
                    c_index varchar2) is
    /*поиск по всем параметрам*/
    select distinct c.id,c.regioncode,c.formalname,c.shortname,c.aolevel,c.currstatus,c.postalcode,
           substr(sys_connect_by_path(formalname,','),2) full_addr,
           connect_by_isleaf isleaf
      from fias_addrobj c
     where c.currstatus=0 --c.livestatus=1
       and c.regioncode=c_region
       and replace(lower(c.formalname),' ','')=nvl(replace(lower(c_street),' ',''),replace(lower(nvl(c_locality,c_city)),' ',''))
       and (nvl(trim(lower(c_street_type)),trim(lower(nvl(c_locality_type,c_city_type)))) is null or 
            trim(lower(c.shortname))=nvl(trim(lower(c_street_type)),trim(lower(nvl(c_locality_type,c_city_type)))))
       and (c.postalcode=c_index)
    connect by prior c.aoid=c.parentguid
      start with c.currstatus=0 --c.livestatus=1
        and c.regioncode=c_region
        and replace(lower(c.formalname),' ','')=nvl(replace(lower(c_area),' ',''),replace(lower(c_city),' ',''))
        and (nvl(replace(lower(c_area_type),' ',''),replace(lower(c_city_type),' ','')) is null or 
             trim(lower(c.shortname))=nvl(replace(lower(c_area_type),' ',''),replace(lower(c_city_type),' ','')))
    union
    /*поиск без учета почтового индекса*/
    select distinct c.id,c.regioncode,c.formalname,c.shortname,c.aolevel,c.currstatus,c.postalcode,
           substr(sys_connect_by_path(formalname,','),2) full_addr,
           connect_by_isleaf isleaf
      from fias_addrobj c
     where c.currstatus=0 --c.livestatus=1
       and c.regioncode=c_region
       and replace(lower(c.formalname),' ','')=nvl(replace(lower(c_street),' ',''),replace(lower(nvl(c_locality,c_city)),' ',''))
       and (nvl(trim(lower(c_street_type)),trim(lower(nvl(c_locality_type,c_city_type)))) is null or 
            trim(lower(c.shortname))=nvl(trim(lower(c_street_type)),trim(lower(nvl(c_locality_type,c_city_type)))))
    connect by prior c.aoid=c.parentguid
      start with c.currstatus=0 --c.livestatus=1
        and c.regioncode=c_region
        and replace(lower(c.formalname),' ','')=nvl(replace(lower(c_area),' ',''),replace(lower(c_city),' ',''))
        and (nvl(replace(lower(c_area_type),' ',''),replace(lower(c_city_type),' ','')) is null or 
             trim(lower(c.shortname))=nvl(replace(lower(c_area_type),' ',''),replace(lower(c_city_type),' ','')))
    union
    /*поиск без учета типов объектов*/
    select distinct c.id,c.regioncode,c.formalname,c.shortname,c.aolevel,c.currstatus,c.postalcode,
           substr(sys_connect_by_path(formalname,','),2) full_addr,
           connect_by_isleaf isleaf
      from fias_addrobj c
     where c.currstatus=0 --c.livestatus=1
       and c.regioncode=c_region
       and replace(lower(c.formalname),' ','')=nvl(replace(lower(c_street),' ',''),replace(lower(nvl(c_locality,c_city)),' ',''))
    connect by prior c.aoid=c.parentguid
      start with c.currstatus=0 --c.livestatus=1
        and c.regioncode=c_region
        and replace(lower(c.formalname),' ','')=nvl(replace(lower(c_area),' ',''),replace(lower(c_city),' ',''))
        and (nvl(replace(lower(c_area_type),' ',''),replace(lower(c_city_type),' ','')) is null or 
             trim(lower(c.shortname))=nvl(replace(lower(c_area_type),' ',''),replace(lower(c_city_type),' ','')));
  
  cursor fs_fias_addrs(c_region varchar2,c_area varchar2,c_city varchar2,c_locality varchar2,c_street varchar2,
                    c_area_type varchar2,c_city_type varchar2,c_locality_type varchar2,c_street_type varchar2,
                    c_index varchar2) is
        select street.id street_id,street.regioncode,street.formalname street_fname,street.shortname street_type,
               locality.id locality_id,locality.formalname locality_fname,
               city.id city_id,city.formalname city_fname,
               area.id area_id,area.formalname area_fname,
               nvl2(area.formalname,area.formalname||',','')||nvl2(city.formalname,city.formalname||',','')||
               nvl2(locality.formalname,locality.formalname||',','')||nvl2(street.formalname,street.formalname||',','') full_addr
          from mv_fias_street_act street left join mv_fias_locality_act locality on (locality.aoid=street.parentguid 
                                                                                     and locality.regioncode=c_region 
                                                                                     and locality.fname=c_locality
                                                                                     and locality.ftype=c_locality_type
                                                                                    )
                                   left join mv_fias_city_act city on (city.aoid=nvl2(c_locality,locality.parentguid,street.parentguid) 
                                                                       and city.regioncode=c_region 
                                                                       and city.fname=c_city
                                                                       and city.ftype=nvl(c_city_type,'г')
                                                                      )
                                   left join mv_fias_area_act area on (area.aoid=nvl(nvl(locality.parentguid,city.parentguid),street.parentguid) 
                                                                       and area.regioncode=c_region 
                                                                       and area.fname=c_area
                                                                       and area.ftype=c_area_type
                                                                      )
         where street.regioncode=c_region
           and street.fname=nvl(c_street,c_locality)
           and street.ftype=nvl(c_street_type,c_locality_type)
           and street.has_child=0;
                    
                    
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
    ndfl_full_addr    varchar2(2000 char),
    area_type         varchar2(10 char),
    area_fname        varchar2(200 char),
    city_type         varchar2(10 char),
    city_fname        varchar2(200 char),
    loc_type          varchar2(10 char),
    loc_fname         varchar2(200 char),
    street_type       varchar2(10 char),
    street_fname      varchar2(200 char),
    fias_id           fias_addrobj.id%type,
    fias_index        fias_addrobj.postalcode%type,
    fias_street       fias_addrobj.formalname%type,
    fias_street_type  fias_addrobj.shortname%type,
    fias_city_id      fias_addrobj.id%type,
    fias_city_name    fias_addrobj.formalname%type,
    chk_index         number,
    chk_region        number,
    chk_area          number,
    chk_city          number,
    chk_loc           number,
    chk_street        number
  );

  type TCheckExistsAddrByFias is record
  (
    id              ndfl_person.id%type,
    post_index      ndfl_person.post_index%type,
    region_code     ndfl_person.region_code%type,
    area            ndfl_person.area%type,
    city            ndfl_person.city%type,
    locality        ndfl_person.locality%type,
    street          ndfl_person.street%type,
    ndfl_full_addr  varchar2(2000 char),
    area_type       varchar2(10 char),
    area_fname      varchar2(200 char),
    city_type       varchar2(10 char),
    city_fname      varchar2(200 char),
    loc_type        varchar2(10 char),
    loc_fname       varchar2(200 char),
    street_type     varchar2(10 char),
    street_fname    varchar2(200 char),
    chk_index       number,
    chk_region      number,
    chk_area        number,
    chk_city        number,
    chk_loc         number,
    chk_street      number
  );

  type TTblFiasAddr is table of fias_addrs%rowtype;
  type TTblFiasAddrFS is table of fs_fias_addrs%rowtype;
  type TTblCheckAddrByFias is table of TCheckAddrByFias;
  type TTblCheckExistsAddrByFias is table of TCheckExistsAddrByFias;
  --------------------------------------------------------------------------------------------------------------
  -- внутренние функции
  --------------------------------------------------------------------------------------------------------------
  -- Распарсить название элемента адреса
  procedure ParseElement(p_lev number,p_name_src varchar2,p_add_lev number,p_type out varchar2,p_name out varchar2);
  -- Получить наименование элемента
  function GetParseName(p_lev number,p_name_src varchar2,p_add_lev number default 0) return varchar2;
  -- Получить Тип элемента
  function GetParseType(p_lev number,p_name_src varchar2,p_add_lev number default 0) return varchar2;
  --------------------------------------------------------------------------------------------------------------
  -- внешние функции
  --------------------------------------------------------------------------------------------------------------
  -- Получить все подходящие адреса из ФИАС
  function GetFiasAddrs(p_region varchar2,p_area varchar2,p_city varchar2,p_locality varchar2,p_street varchar2,
                        p_area_type varchar2,p_city_type varchar2,p_locality_type varchar2,p_street_type varchar2,
                        p_post_index varchar2) return TTblFiasAddr pipelined;

  -- Получить все подходящие адреса из ФИАС фиксированной структуры, без иерархии
  function GetFiasAddrsFS(p_region varchar2,p_area varchar2,p_city varchar2,p_locality varchar2,p_street varchar2,
                        p_area_type varchar2,p_city_type varchar2,p_locality_type varchar2,p_street_type varchar2,
                        p_post_index varchar2) return TTblFiasAddrFS pipelined;
  
  -- Проверить существование элемента адреса с учетом родительского элемента
  -- p_check_type: AREA,CITY,LOCALITY,STREET
  function CheckAddrElement(p_region varchar2,p_check_element varchar2,p_parent_element varchar2,p_check_type varchar2 default '',p_leaf number default 1) return number;

  -- Выполнить проверку на полное совпадение с ФИАС адресов, указанных в декларации
  -- возвращается курсор на таблицу типа TTblCheckAddrByFias
  -- для записей, по которым не установлен адрес в ФИАС, выполняется проверка наличия/отсутствия в ФИАС элементов адресов
  function CheckAddrByFiasR(p_declaration number,p_check_type number default 0) return ref_cursor;

  -- Выполнить проверку на полное совпадение с ФИАС адресов, указанных в декларации
  -- возвращается курсор на таблицу типа TTblCheckAddrByFias
  -- для записей, по которым не установлен адрес в ФИАС, выполняется проверка наличия/отсутствия в ФИАС элементов адресов
  -- проверка выполняется по функции GetFiasAddrsFS
  -- p_check_type(тип проверки наличия элементов адреса в ФИАС): 1 - проверяется полная цепочка родительских элементов, 0 - проверяется только непосредственный родитель
  function CheckAddrByFias(p_declaration number,p_check_type number default 0) return ref_cursor;

  -- Выполнить проверку на наличие/отсутствие в ФИАС элементов адресов, указанных в декларации
  -- возвращается курсор на таблицу типа TTblCheckExistsAddrByFias
  function CheckExistsAddrByFias(p_declaration number,p_check_type number default 0) return ref_cursor;
  
  -- Обновление мат. представлений
  procedure RefreshViews;
  
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

    v_check_path boolean:=false;
  --------------------------------------------------------------------------------------------------------------
  -- внутренние функции
  --------------------------------------------------------------------------------------------------------------
  -- Распарсить название элемента адреса
  --------------------------------------------------------------------------------------------------------------
  procedure ParseElement(p_lev number,p_name_src varchar2,p_add_lev number,p_type out varchar2,p_name out varchar2)
  is
    v_name_src varchar2(500 char):=p_name_src;
    v_name varchar2(500 char):='';
    v_type varchar2(10 char):='';
    v_char varchar2(1 char):=' ';
    v_str varchar2(200 char);
    v_srch varchar2(200 char);
    p number:=1;
    pp number:=1;
begin
  if v_name_src is not null then
      -- делим строку по пробелам
      p:=instr(v_name_src||v_char,v_char);
      while p>0 loop
        v_str:=substr(v_name_src||v_char,pp,p-pp);
        -- каждую часть сравниваем с типами элементов адреса
        begin
          if substr(v_str,-1)='.' then
            v_srch:=substr(v_str,1,length(v_str)-1);
          else
            v_srch:=v_str;
          end if;
          select trim(lower(v_str)) into v_type from fias_socrbase scr where scr.lev in (p_lev,p_add_lev) and trim(lower(scr.scname))=trim(lower(v_srch)) and rownum=1;
          if (instr(lower(v_name_src),' '||lower(v_type))>0) then
            v_name:=trim(substr(v_name_src,1,instr(lower(v_name_src),' '||lower(v_type))-1));
          elsif (instr(lower(v_name_src),lower(v_type)||' ')>0) then
            v_name:=trim(substr(v_name_src,instr(lower(v_name_src),lower(v_type)||' ')+length(v_type||' ')-1));
          end if;
          exit;
        exception when no_data_found then
          null;
        end;
        pp:=p;
        p:=instr(v_name_src||v_char,v_char,p+1);
      end loop;
    end if;
    p_type:=v_type;
    p_name:=v_name;
  exception when others then
    p_type:='';
    p_name:='';
  end;
  --------------------------------------------------------------------------------------------------------------
  -- Получить наименование элемента
  --------------------------------------------------------------------------------------------------------------
  function GetParseName(p_lev number,p_name_src varchar2,p_add_lev number default 0) return varchar2
  is 
    v_name varchar2(200 char);
    v_type varchar2(10 char);
  begin
    ParseElement(p_lev,p_name_src,p_add_lev,v_type,v_name);
    return nvl(v_name,p_name_src);
  end;
  --------------------------------------------------------------------------------------------------------------
  -- Получить Тип элемента
  --------------------------------------------------------------------------------------------------------------
  function GetParseType(p_lev number,p_name_src varchar2,p_add_lev number default 0) return varchar2
  is
    v_name varchar2(200 char);
    v_type varchar2(10 char);
  begin
    ParseElement(p_lev,p_name_src,p_add_lev,v_type,v_name);
    return v_type;
  end;
  --------------------------------------------------------------------------------------------------------------
  -- внешние функции
  --------------------------------------------------------------------------------------------------------------
  -- Получить все подходящие адреса из ФИАС
  -------------------------------------------------------------------------------------------------------------
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

  --------------------------------------------------------------------------------------------------------------
  -- Получить все подходящие адреса из ФИАС фиксированной структуры, без иерархии
  -------------------------------------------------------------------------------------------------------------
  function GetFiasAddrsFS(p_region varchar2,p_area varchar2,p_city varchar2,p_locality varchar2,p_street varchar2,
                        p_area_type varchar2,p_city_type varchar2,p_locality_type varchar2,p_street_type varchar2,
                        p_post_index varchar2) return TTblFiasAddrFS pipelined 
  is
    tbl TTblFiasAddrFS:=TTblFiasAddrFS();
  begin
    if fs_fias_addrs%isopen then
      close fs_fias_addrs;
    end if;
    open fs_fias_addrs(p_region,p_area,p_city,p_locality,p_street,
                    p_area_type,p_city_type,p_locality_type,p_street_type,p_post_index);
    fetch fs_fias_addrs bulk collect into tbl;
    close fs_fias_addrs;
    
    if tbl.count>0 then
      for i in 1..tbl.count loop
        if tbl.exists(i) then
          pipe row (tbl(i));
        end if;
      end loop;
    end if;
   end GetFiasAddrsFS;

  -------------------------------------------------------------------------------------------------------------
  -- Проверить существование элемента адреса с учетом родительского элемента
  -- p_check_type: AREA,CITY,LOCALITY,STREET
  -------------------------------------------------------------------------------------------------------------
  function CheckAddrElement(p_region varchar2,p_check_element varchar2,p_parent_element varchar2,
                            p_check_type varchar2 default '',p_leaf number default 1) return number
  is
    v_result number:=0;
  begin
    if v_check_path then
      select decode(count(*),0,0,1) into v_result
        from (
              select c.id,c.regioncode,c.formalname,c.shortname,c.aolevel,c.currstatus,c.postalcode,
                     substr(sys_connect_by_path(formalname,';'),2) full_addr,
                     connect_by_isleaf isleaf
                from (select * from fias_addrobj t
                      where t.regioncode=p_region
                        and t.livestatus=1
                     ) c
               start with c.parentguid is null 
             connect by prior c.aoid=c.parentguid
             ) f 
       where replace(lower(f.formalname),' ','')=replace(lower(p_check_element),' ','')
         and instr(lower(full_addr),lower(p_parent_element))>0
         and isleaf=p_leaf;
    else
      if (p_check_type='AREA') then
        select decode(count(*),0,0,1) into v_result
          from mv_fias_area_act a
         where a.regioncode=p_region
           and a.fname=nvl(replace(lower(p_check_element),' ',''),'-')
           and a.has_child=decode(p_leaf,1,0,1);
      elsif (p_check_type='CITY') then
        select decode(count(*),0,0,1) into v_result
          from mv_fias_city_act c left join mv_fias_area_act a on (a.parentguid=c.aoid)
         where c.regioncode=p_region
           and c.fname=nvl(replace(lower(p_check_element),' ',''),'-')
           and (nvl(replace(lower(p_parent_element),' ',''),'-')='-' or 
                a.fname=nvl(replace(lower(p_parent_element),' ',''),'-'))
           and c.has_child=decode(p_leaf,1,0,1);
      elsif (p_check_type='LOCALITY') then
        select decode(sum(cnt),0,0,1) into v_result
          from (
                select count(*) cnt
                  from mv_fias_locality_act l left join mv_fias_city_act c on (c.parentguid=l.aoid)
                 where l.regioncode=p_region
                   and l.fname=nvl(replace(lower(p_check_element),' ',''),'-')
                   and c.fname=nvl(replace(lower(p_parent_element),' ',''),'-')
                   and l.has_child=decode(p_leaf,1,0,1)
                union 
                select count(*) cnt
                  from mv_fias_locality_act l left join mv_fias_area_act a on (a.parentguid=l.aoid)
                 where l.regioncode=p_region
                   and l.fname=nvl(replace(lower(p_check_element),' ',''),'-')
                   and a.fname=nvl(replace(lower(p_parent_element),' ',''),'-')
                   and l.has_child=decode(p_leaf,1,0,1)
              );
      elsif (p_check_type='STREET') then
        select decode(sum(cnt),0,0,1) into v_result
          from (
                select count(*) cnt
                  from mv_fias_street_act s left join mv_fias_city_act c on (c.parentguid=s.aoid)
                 where s.regioncode=p_region
                   and s.fname=nvl(replace(lower(p_check_element),' ',''),'-')
                   and c.fname=nvl(replace(lower(p_parent_element),' ',''),'-')
                   and s.has_child=decode(p_leaf,1,0,1)
                union 
                select count(*) cnt
                  from mv_fias_street_act s left join mv_fias_locality_act l on (l.parentguid=s.aoid)
                 where s.regioncode=p_region
                   and s.fname=nvl(replace(lower(p_check_element),' ',''),'-')
                   and l.fname=nvl(replace(lower(p_parent_element),' ',''),'-')
                   and s.has_child=decode(p_leaf,1,0,1)
                union 
                select count(*) cnt
                  from mv_fias_street_act s left join mv_fias_area_act a on (a.parentguid=s.aoid)
                 where s.regioncode=p_region
                   and s.fname=nvl(replace(lower(p_check_element),' ',''),'-')
                   and a.fname=nvl(replace(lower(p_parent_element),' ',''),'-')
                   and s.has_child=decode(p_leaf,1,0,1)
              );
      end if;
      /*select decode(count(*),0,0,1) into v_result
        from fias_addrobj f left join fias_addrobj p on (p.aoid=f.parentguid and p.currstatus=0 and p.regioncode=p_region)
       where f.currstatus=0
         and f.regioncode=p_region
         and replace(lower(f.formalname),' ','')=replace(lower(p_check_element),' ','')
         and ((nvl(replace(lower(p_parent_element),' ',''),'-')='-' and f.parentguid is null) or 
              (nvl(replace(lower(p_parent_element),' ',''),'-')<>'-' and replace(lower(p.formalname),' ','')=replace(lower(p_parent_element),' ',''))
             )
         and (p_leaf=0 and exists(select 1 from fias_addrobj c where c.parentguid=f.aoid) or
              p_leaf=1 and not exists(select 1 from fias_addrobj c where c.parentguid=f.aoid) 
             );*/
    end if;
    return v_result;
  end;

  -------------------------------------------------------------------------------------------------------------
  -- Выполнить проверку на полное совпадение с ФИАС адресов, указанных в декларации
  -- возвращается курсор на таблицу типа TTblCheckAddrByFias
  -------------------------------------------------------------------------------------------------------------
  function CheckAddrByFiasR(p_declaration number,p_check_type number default 0) return ref_cursor
  is
    v_ref ref_cursor;
  begin
    v_check_path:=(p_check_type=1);
    open v_ref for
      select n.id,n.post_index,n.region_code,n.area,n.city,n.locality,n.street,
             n.ndfl_full_addr,n.area_type,n.area_fname,n.city_type,n.city_fname,n.loc_type,n.loc_fname,n.street_type,n.street_fname,
             f.id fias_id,
             f.postalcode fias_index,
             f.formalname fias_street,
             f.shortname fias_street_type,
             fc.id fias_city_id,
             fc.formalname fias_city_name,
             (select decode(count(*),0,0,1) 
                from fias_addrobj f 
               where f.regioncode=n.region_code 
                 and replace(f.postalcode,' ','')=n.post_index) chk_index,
             (select decode(count(*),0,0,1) 
                from fias_addrobj f 
               where f.regioncode=n.region_code) chk_region,
             case when n.fa_id is null then fias_pkg.CheckAddrElement(n.region_code,n.area_fname,',','AREA',0) 
                  else 1
             end chk_area,
             case when n.fa_id is null then fias_pkg.CheckAddrElement(n.region_code,n.city_fname,decode(p_check_type,1,nvl2(n.area_fname,n.area_fname||';',';'),n.area_fname),'CITY',n.city_leaf) 
                  else 1
             end chk_city,
             case when n.fa_id is null then fias_pkg.CheckAddrElement(n.region_code,n.loc_fname,decode(p_check_type,1,nvl2(n.area_fname,n.area_fname||';',';')||nvl2(n.city_fname,n.city_fname||';',''),nvl(n.city_fname,n.area_fname)),'LOCALITY',n.loc_leaf) 
                  else 1
             end chk_loc,
             case when n.fa_id is null then fias_pkg.CheckAddrElement(n.region_code,n.street_fname,decode(p_check_type,1,nvl2(n.area_fname,n.area_fname||';','')||nvl2(n.city_fname,n.city_fname||';','')||nvl2(n.loc_fname,n.loc_fname||';',''),nvl(n.loc_fname,n.city_fname)),'STREET',1) 
                  else 1
             end chk_street
        from (
              select tab.*,
                     nvl2(tab.area_fname,tab.area_fname||',','')||nvl2(tab.city_fname,tab.city_fname||',','')||nvl2(tab.loc_fname,tab.loc_fname||',','')||
                     nvl2(tab.street_fname,tab.street_fname||',','') ndfl_full_addr,
                     (select min(id)
                        from table(fias_pkg.GetFiasAddrs(tab.region_code,trim(lower(tab.area_fname)),trim(lower(tab.city_fname)),trim(lower(tab.loc_fname)),trim(lower(tab.street_fname)),
                                                         trim(lower(tab.area_type)),trim(lower(tab.city_type)),trim(lower(tab.loc_type)),trim(lower(tab.street_type)),tab.post_index)) f
                       where lower(f.full_addr||',')=lower(nvl2(tab.area_fname,tab.area_fname||',','')||nvl2(tab.city_fname,tab.city_fname||',','')||nvl2(tab.loc_fname,tab.loc_fname||',','')||
                                                           nvl2(tab.street_fname,tab.street_fname||',',''))
                         and f.isleaf=1
                         ) fa_id
                from (
                      select n.id,
                             n.post_index,n.region_code,n.area,n.city,n.locality,n.street,
                             fias_pkg.GetParseType(3,n.area) area_type,
                             fias_pkg.GetParseName(3,n.area) area_fname,
                             case when n.city is null and n.region_code='77' then 'г'
                                  when n.city is null and n.region_code='78' then 'г'
                                  when n.city is null and n.region_code='92' then 'г'
                                  when n.city is null and n.region_code='99' then 'г'
                                  when n.region_code='78' and upper(n.city)='САНКТ-ПЕТЕРБУРГ' then 'г'
                                  else fias_pkg.GetParseType(4,n.city)
                             end  city_type,
                             case when n.city is null and n.region_code='77' then 'Москва'
                                  when n.city is null and n.region_code='78' then 'Санкт-Петербург'
                                  when n.city is null and n.region_code='92' then 'Севастополь'
                                  when n.city is null and n.region_code='99' then 'Байконур'
                                  else fias_pkg.GetParseName(4,n.city)
                             end city_fname,
                             fias_pkg.GetParseType(7,n.street) street_type,
                             fias_pkg.GetParseName(7,n.street) street_fname,
                             fias_pkg.GetParseType(6,n.locality) loc_type,
                             fias_pkg.GetParseName(6,n.locality) loc_fname,
                             case when n.street is null and n.city is not null then 1
                                  else 0
                             end city_leaf,
                             case when n.street is null and n.locality is not null then 1
                                  else 0
                             end loc_leaf       
                        from ndfl_person n
                       where n.declaration_data_id=p_declaration
                         --and n.id between p_start_id and p_start_id+999
                      ) tab
                ) n left join fias_addrobj f on (f.id=n.fa_id) left join fias_addrobj fc on (fc.id=f.parentguid);
    
    return v_ref;
  end;

  -------------------------------------------------------------------------------------------------------------
  -- Выполнить проверку на полное совпадение с ФИАС адресов, указанных в декларации
  -- возвращается курсор на таблицу типа TTblCheckAddrByFias
  -- для записей, по которым не установлен адрес в ФИАС, выполняется проверка наличия/отсутствия в ФИАС элементов адресов
  -- проверка выполняется по функции GetFiasAddrsFS
  -------------------------------------------------------------------------------------------------------------
  function CheckAddrByFias(p_declaration number,p_check_type number default 0) return ref_cursor
  is
    v_ref ref_cursor;
  begin
    v_check_path:=(p_check_type=1);
    open v_ref for
      select n.id,n.post_index,n.region_code,n.area,n.city,n.locality,n.street,
             n.ndfl_full_addr,n.area_type,n.area_fname,n.city_type,n.city_fname,n.loc_type,n.loc_fname,n.street_type,n.street_fname,
             f.id fias_id,
             f.postalcode fias_index,
             f.formalname fias_street,
             f.shortname fias_street_type,
             fc.id fias_city_id,
             fc.formalname fias_city_name,
             (select decode(count(*),0,0,1) 
                from fias_addrobj f 
               where f.regioncode=n.region_code 
                 and replace(f.postalcode,' ','')=n.post_index) chk_index,
             (select decode(count(*),0,0,1) 
                from fias_addrobj f 
               where f.regioncode=n.region_code) chk_region,
             case when n.fa_id is null then fias_pkg.CheckAddrElement(n.region_code,n.area_fname,',','AREA',0) 
                  else 1
             end chk_area,
             case when n.fa_id is null then fias_pkg.CheckAddrElement(n.region_code,n.city_fname,decode(p_check_type,1,nvl2(n.area_fname,n.area_fname||';',';'),n.area_fname),'CITY',n.city_leaf) 
                  else 1
             end chk_city,
             case when n.fa_id is null then fias_pkg.CheckAddrElement(n.region_code,n.loc_fname,decode(p_check_type,1,nvl2(n.area_fname,n.area_fname||';',';')||nvl2(n.city_fname,n.city_fname||';',''),nvl(n.city_fname,n.area_fname)),'LOCALITY',n.loc_leaf) 
                  else 1
             end chk_loc,
             case when n.fa_id is null then fias_pkg.CheckAddrElement(n.region_code,n.street_fname,decode(p_check_type,1,nvl2(n.area_fname,n.area_fname||';','')||nvl2(n.city_fname,n.city_fname||';','')||nvl2(n.loc_fname,n.loc_fname||';',''),nvl(n.loc_fname,n.city_fname)),'STREET',1) 
                  else 1
             end chk_street
        from (
              select tab.*,
                     nvl2(tab.area_fname,tab.area_fname||',','')||nvl2(tab.city_fname,tab.city_fname||',','')||nvl2(tab.loc_fname,tab.loc_fname||',','')||
                     nvl2(tab.street_fname,tab.street_fname||',','') ndfl_full_addr,
                     (select min(f.street_id)
                        from table(fias_pkg.GetFiasAddrsFS(tab.region_code,replace(lower(tab.area_fname),' ',''),replace(lower(tab.city_fname),' ',''),replace(lower(tab.loc_fname),' ',''),replace(lower(tab.street_fname),' ',''),
                                                           trim(lower(tab.area_type)),trim(lower(tab.city_type)),trim(lower(tab.loc_type)),trim(lower(nvl(tab.street_type,'ул'))),tab.post_index)) f
                       where lower(f.full_addr)=lower(nvl2(tab.area_fname,tab.area_fname||',','')||nvl2(tab.city_fname,tab.city_fname||',','')||nvl2(tab.loc_fname,tab.loc_fname||',','')||
                                                           nvl2(tab.street_fname,tab.street_fname||',',''))
                     ) fa_id
                from (
                      select n.id,
                             n.post_index,n.region_code,n.area,n.city,n.locality,n.street,
                             fias_pkg.GetParseType(3,n.area) area_type,
                             fias_pkg.GetParseName(3,n.area) area_fname,
                             case when n.city is null and n.region_code='77' then 'г'
                                  when n.city is null and n.region_code='78' then 'г'
                                  when n.city is null and n.region_code='92' then 'г'
                                  when n.city is null and n.region_code='99' then 'г'
                                  when n.region_code='78' and upper(n.city)='САНКТ-ПЕТЕРБУРГ' then 'г'
                                  else fias_pkg.GetParseType(4,n.city)
                             end  city_type,
                             case when n.city is null and n.region_code='77' then 'Москва'
                                  when n.city is null and n.region_code='78' then 'Санкт-Петербург'
                                  when n.city is null and n.region_code='92' then 'Севастополь'
                                  when n.city is null and n.region_code='99' then 'Байконур'
                                  else fias_pkg.GetParseName(4,n.city)
                             end city_fname,
                             fias_pkg.GetParseType(7,n.street) street_type,
                             fias_pkg.GetParseName(7,n.street) street_fname,
                             fias_pkg.GetParseType(6,n.locality) loc_type,
                             fias_pkg.GetParseName(6,n.locality) loc_fname,
                             case when n.street is null and n.city is not null then 1
                                  else 0
                             end city_leaf,
                             case when n.street is null and n.locality is not null then 1
                                  else 0
                             end loc_leaf       
                        from ndfl_person n
                       where n.declaration_data_id=p_declaration
                         --and n.id between p_start_id and p_start_id+999
                      ) tab
                ) n left join fias_addrobj f on (f.id=n.fa_id) left join fias_addrobj fc on (fc.id=f.parentguid);
    
    return v_ref;
  end;

  -------------------------------------------------------------------------------------------------------------
  -- Выполнить проверку на наличие/отсутствие в ФИАС элементов адресов, указанных в декларации
  -- возвращается курсор на таблицу типа TTblCheckExistsAddrByFias
  -------------------------------------------------------------------------------------------------------------
  function CheckExistsAddrByFias(p_declaration number,p_check_type number default 0) return ref_cursor
  is
    v_ref ref_cursor;
  begin
    v_check_path:=(p_check_type=1);
    open v_ref for
      select tab.id,tab.post_index,tab.region_code,tab.area,tab.city,tab.locality,tab.street,
             nvl2(tab.area_fname,tab.area_fname||',','')||nvl2(tab.city_fname,tab.city_fname||',','')||
             nvl2(tab.loc_fname,tab.loc_fname||',','')||nvl2(tab.street_fname,tab.street_fname||',','') ndfl_full_addr,
             tab.area_type,tab.area_fname,tab.city_type,tab.city_fname,
             tab.loc_type,tab.loc_fname,tab.street_type,tab.street_fname,
             (select decode(count(*),0,0,1) 
                from fias_addrobj f 
               where f.regioncode=tab.region_code 
                 and f.postalcode=tab.post_index) chk_index,
             (select decode(count(*),0,0,1) 
                from fias_addrobj f 
               where f.regioncode=tab.region_code) chk_region,
             fias_pkg.CheckAddrElement(tab.region_code,tab.area_fname,',','',0) chk_area,
             fias_pkg.CheckAddrElement(tab.region_code,tab.city_fname,decode(p_check_type,1,nvl2(tab.area_fname,tab.area_fname||';',';'),tab.area_fname),'',tab.city_leaf) chk_city,
             fias_pkg.CheckAddrElement(tab.region_code,tab.loc_fname,decode(p_check_type,1,nvl2(tab.area_fname,tab.area_fname||';',';'),tab.area_fname),'',tab.loc_leaf) chk_loc,
             fias_pkg.CheckAddrElement(tab.region_code,tab.street_fname,decode(p_check_type,1,nvl2(tab.area_fname,tab.area_fname||';','')||nvl2(tab.city_fname,tab.city_fname||';','')||nvl2(tab.loc_fname,tab.loc_fname||';',''),nvl(tab.loc_fname,tab.city_fname)),'',1) chk_street
        from (select n.id,
                     n.post_index,n.region_code,n.area,n.city,n.locality,n.street,
                     fias_pkg.GetParseType(3,n.area) area_type,
                     fias_pkg.GetParseName(3,n.area) area_fname,
                     case when n.city is null and n.region_code='77' then 'г'
                          when n.city is null and n.region_code='78' then 'г'
                          when n.city is null and n.region_code='92' then 'г'
                          when n.city is null and n.region_code='99' then 'г'
                          when n.region_code='78' and upper(n.city)='САНКТ-ПЕТЕРБУРГ' then 'г'
                          else fias_pkg.GetParseType(4,n.city)
                     end  city_type,
                     case when n.city is null and n.region_code='77' then 'Москва'
                          when n.city is null and n.region_code='78' then 'Санкт-Петербург'
                          when n.city is null and n.region_code='92' then 'Севастополь'
                          when n.city is null and n.region_code='99' then 'Байконур'
                          else fias_pkg.GetParseName(4,n.city)
                     end city_fname,
                     fias_pkg.GetParseType(7,n.street) street_type,
                     fias_pkg.GetParseName(7,n.street) street_fname,
                     fias_pkg.GetParseType(6,n.locality) loc_type,
                     fias_pkg.GetParseName(6,n.locality) loc_fname,
                     case when n.street is null and n.city is not null then 1
                          else 0
                     end city_leaf,
                     case when n.street is null and n.locality is not null then 1
                          else 0
                     end loc_leaf
                from ndfl_person n
               where n.declaration_data_id=p_declaration) tab;

    return v_ref;
  end;

  -------------------------------------------------------------------------------------------------------------
  -- Обновление мат. представлений
  -------------------------------------------------------------------------------------------------------------
  procedure RefreshViews
  is
  begin
    dbms_mview.refresh('MV_FIAS_AREA_ACT', 'C');
    dbms_mview.refresh('MV_FIAS_CITY_ACT', 'C');
    dbms_mview.refresh('MV_FIAS_LOCALITY_ACT', 'C');
    dbms_mview.refresh('MV_FIAS_STREET_ACT', 'C');
  end;

end fias_pkg;
/
show errors;

