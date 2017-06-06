create or replace package person_pkg as
/* Пакет для идентификации физических лиц*/
  cursor persons_for_insert(c_declaration number) is
     select n.id,
            n.person_id,
            n.row_num,
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
  function GetPersonForIns(p_declaration number,p_asnu number default 1) return ref_cursor;
  function GetPersonForUpd(p_declaration number,p_asnu number default 1) return ref_cursor;
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
  function GetPersonForIns(p_declaration number,p_asnu number default 1) return ref_cursor
  is
    v_ref ref_cursor;
  begin
    open v_ref for     
       select n.id,
              n.person_id,
              n.row_num,
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
          and not exists(select 1 from ref_book_person p where replace(p.inn_foreign,' ','') = replace(n.inn_foreign,' ',''))
          and not exists(select 1 from ref_book_id_tax_payer t where t.as_nu=p_asnu and lower(t.inp)=lower(n.inp));
  
    return v_ref;
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
                and exists (select 1 from ref_book_person c left join ref_book_id_tax_payer t on (t.person_id=c.id)
                             where replace(lower(nvl(c.last_name,'empty')),' ','') = replace(lower(nvl(n.last_name,'empty')),' ','')
                               and replace(lower(nvl(c.first_name,'empty')),' ','') = replace(lower(nvl(n.first_name,'empty')),' ','')
                               and replace(lower(nvl(c.middle_name,'empty')),' ','') = replace(lower(nvl(n.middle_name,'empty')),' ','')
                               and c.birth_date=n.birth_day
                               and replace(replace(nvl(c.snils,'empty'),' ',''),'-','') = replace(replace(nvl(n.snils,'empty'), ' ', ''), '-', '')
                               and replace(nvl(c.inn,'empty'),' ','') = replace(nvl(n.inn_np,'empty'),' ','')
                               and replace(nvl(c.inn_foreign,'empty'),' ','') = replace(nvl(n.inn_foreign,'empty'),' ','')
                               and t.as_nu=p_asnu and lower(t.inp)=lower(n.inp)
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