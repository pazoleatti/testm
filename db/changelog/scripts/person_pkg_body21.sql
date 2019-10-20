create or replace PACKAGE BODY                  "PERSON_PKG" as

  v_max_end_date date := to_date(5373484, 'J'); -- Максимально возможная дата окончания действия =31.12.9999

  /*
  Процедура подготовки актуальных версий ФЛ на заданную дату.
  Актуальные версии помещаются во временную таблицу, которая затем используется остальными функциями.
  */
  procedure FillRecordVersions(p_date date default trunc(sysdate))
  is
    p_temp_date DATE;
  begin

    p_temp_date := p_date;

  end;

  /*
  Функция возвращает ссылку на АСНУ хранящуюся в декларации
  */
  function Get_ASNU_Id(p_declaration number) return number
  is
    v_asnu number;
  begin
    begin
      select dd.asnu_id into v_asnu
      from declaration_data dd
      where dd.id = p_declaration;
    exception
      when no_data_found then
        v_asnu := null;
    end;
    if v_asnu is null then -- если ссылка на АСНУ не определена, то взять значение по умолчанию
        begin
          select min(id) into v_asnu from REF_BOOK_ASNU where id > 0;
        exception
          when
            others then v_asnu := 1;
        end;
    end if;
    return v_asnu;
  exception
    when others then return 1;
  end;

  /*
  Выборка ФЛ, которые есть в справочнике ФЛ, для их дальнейшего обновления
  */
  function GetPersonForUpd(p_declaration number) return ref_cursor
  is
    v_ref ref_cursor;
    v_asnu number;
  begin
    v_asnu := Get_ASNU_Id(p_declaration); -- ссылка на АСНУ

    open v_ref for
        select fv.person_id,
               person.id as ref_book_person_id,
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
               person.country_id,
               person.region_code,
               person.postal_code,
               person.district,
               person.city,
               person.locality,
               person.street,
               person.house,
               person.build,
               person.appartment,
               person.address_foreign,
               person.start_date,
               person.end_date,
               tax.id as book_id_tax_payer_id,
               tax.inp,
               tax.as_nu,
               doc.id as ref_book_id_doc_id,
               doc.doc_id,
               doc.doc_number,
               tb.id as ref_book_person_tb_id,
               tb.tb_department_id,
               tb.import_date
          from (
             select /*+ INDEX (fv  IDX_REF_BOOK_PERSON_SRCH_FIO)*/  distinct n.id as person_id,first_value(fv.id) over(partition by n.id order by fv.id) ref_person_id
               from  ndfl_person n join ref_book_person fv on (fv.search_last_name = n.search_last_name
                                                               and fv.search_first_name =  n.search_first_name
                                                               and fv.search_middle_name =  n.search_middle_name
                                                               and fv.birth_date=n.birth_day
                                                               and fv.search_snils =  n.search_snils
                                                               and fv.search_inn =  n.search_inn
                                                               and fv.search_inn_foreign =  n.search_inn_foreign
															   and fv.start_date <= sysdate and ( (fv.end_date is null) or (fv.end_date >= sysdate) ) and fv.record_id = fv.old_id
                                                               )
              where n.declaration_data_id=p_declaration
                and exists (-- Существуют записи ИНП, привязанные ко всем версиям ФЛ с одинаковым "Идентификатором ФЛ", включая версии дубликатов
                             select 1 from ref_book_person c join ref_book_person c1 on (c.record_id=c1.record_id) left join ref_book_id_tax_payer t on (t.person_id=c1.id)
                             where  c.search_last_name = n.search_last_name
                               and c.search_first_name = n.search_first_name
                               and c.search_middle_name = n.search_middle_name
                               and c.birth_date=n.birth_day
                               and c.search_snils = n.search_snils
                               and c.search_inn = n.search_inn
                               and c.search_inn_foreign = n.search_inn_foreign
                               and t.as_nu=v_asnu and (t.inp)=(n.inp)
                               )
                and fv.record_id=fv.old_id -- не относится к дубликатам
                ) fv join ref_book_person person on (person.id=fv.ref_person_id)
                     left join ref_book_id_tax_payer tax on (tax.person_id in (select id from ref_book_person where record_id=person.record_id))
                     left join ref_book_id_doc doc on (doc.person_id in (select id from ref_book_person where record_id=person.record_id))
                     left join ref_book_person_tb tb on (tb.person_id in (select id from ref_book_person where record_id=person.record_id));

    return v_ref;
  end;

  /*
  Выборка ФЛ для их дальнейшей проверки с вычислением весов
  */
  function GetPersonForCheck(p_declaration number) return ref_cursor
  is
    v_ref ref_cursor;
    v_asnu number;
  begin
    v_asnu := Get_ASNU_Id(p_declaration); -- ссылка на АСНУ
    open v_ref for
      with t as (select * from ndfl_person where declaration_data_id=p_declaration and person_id is null)
      select person2.person_id,
             person3.id as ref_book_person_id,
             person3.record_id as person_record_id,
             person3.last_name,
             person3.first_name,
             person3.middle_name,
             person3.inn,
             person3.inn_foreign,
             person3.snils,
             person3.taxpayer_state,
             person3.birth_date,
             person3.birth_place,
             person3.citizenship,
             person3.record_id,
             person3.source_id,
             person3.old_id,
             person3.report_doc,
             person3.vip,
             person3.country_id,
             person3.region_code,
             person3.postal_code,
             person3.district,
             person3.city,
             person3.locality,
             person3.street,
             person3.house,
             person3.build,
             person3.appartment,
             person3.address_foreign,
             person3.start_date,
             person3.end_date,
             tax.id as book_id_tax_payer_id,
             tax.inp,
             tax.as_nu,
             doc.id as ref_book_id_doc_id,
             doc.doc_id,
             doc.doc_number,
             tb.id as ref_book_person_tb_id,
             tb.tb_department_id,
             tb.import_date
      from (
             /*По ФИО*/
            select  t.id as person_id,
                   person.id as ref_book_person_id,
                   doc.id as ref_book_id_doc_id,
                   tax.id as book_id_tax_payer_id
              from t join ref_book_person person on (person.search_last_name = replace((t.last_name),' ','') and
                                                                 person.search_first_name= replace((t.first_name),' ','') and
                                                                 person.search_middle_name= replace((t.middle_name),' ','') and
                                                                 person.birth_date=t.birth_day and
                                                                 person.start_date <= sysdate and ( (person.end_date is null) or (person.end_date >= sysdate) ) and person.record_id = person.old_id
                                                                     )
                                 left join ref_book_id_doc doc on (doc.person_id in (select id from ref_book_person where record_id=person.record_id))
                                 left join ref_book_id_tax_payer tax on (tax.person_id in (select id from ref_book_person where record_id=person.record_id))
             where person.record_id=person.old_id
            union
             /*по СНИЛСУ*/
            select  t.id as person_id,
                   person.id as ref_book_person_id,
                   doc.id as ref_book_id_doc_id,
                   tax.id as book_id_tax_payer_id
              from  t join ref_book_person person on (person.search_snils = replace(replace(t.snils, ' ', ''), '-', '') and
                                                                 person.start_date <= sysdate and ( (person.end_date is null) or (person.end_date >= sysdate) ) and person.record_id = person.old_id
																)
                                 left join ref_book_id_doc doc on (doc.person_id in (select id from ref_book_person where record_id=person.record_id))
                                 left join ref_book_id_tax_payer tax on (tax.person_id in (select id from ref_book_person where record_id=person.record_id))
             where  person.record_id=person.old_id
            union
            /*По ИННу*/
            select  t.id as person_id,
                   person.id as ref_book_person_id,
                   doc.id as ref_book_id_doc_id,
                   tax.id as book_id_tax_payer_id
              from  t join ref_book_person person on (person.search_inn = replace(t.inn_np,' ','') and
                                                                 person.start_date <= sysdate and ( (person.end_date is null) or (person.end_date >= sysdate) ) and person.record_id = person.old_id
																)
                                 left join ref_book_id_doc doc on (doc.person_id in (select id from ref_book_person where record_id=person.record_id))
                                 left join ref_book_id_tax_payer tax on (tax.person_id in (select id from ref_book_person where record_id=person.record_id))
             where person.record_id=person.old_id
            union
            /*По ИННу иностранного государства*/
            select  t.id as person_id,
                   person.id as ref_book_person_id,
                   doc.id as ref_book_id_doc_id,
                   tax.id as book_id_tax_payer_id
              from  t join ref_book_person person on (person.search_inn_foreign = replace(t.inn_foreign,' ','') and
                                                                 person.start_date <= sysdate and ( (person.end_date is null) or (person.end_date >= sysdate) ) and person.record_id = person.old_id
																)
                                 left join ref_book_id_doc doc on (doc.person_id in (select id from ref_book_person where record_id=person.record_id))
                                 left join ref_book_id_tax_payer tax on (tax.person_id in (select id from ref_book_person where record_id=person.record_id))
             where person.record_id=person.old_id
            union
            /*По ДУЛ*/
            /*select  t.id as person_id,
                   person.id as ref_book_person_id,
                   doc.id as ref_book_id_doc_id,
                   tax.id as book_id_tax_payer_id
              from t join ref_book_doc_type dt on (dt.code=t.id_doc_type)
                                 join ref_book_id_doc doc on (doc.doc_id=dt.id and search_doc_number = regexp_replace((t.id_doc_number),'[^0-9A-Za-zА-Яа-я]',''))
                                 join (select distinct r1.id,r2.id id1 from ref_book_person r1 join ref_book_person r2 on r1.record_id=r2.record_id) a on (a.id1 = doc.person_id)
                                 join ref_book_person person on (person.id = a.id and
                                                                 person.start_date <= sysdate and ( (person.end_date is null) or (person.end_date >= sysdate) ) and person.record_id = person.old_id
																)
                                 left join ref_book_id_tax_payer tax on (tax.person_id in (select id from ref_book_person where record_id=person.record_id))
             where person.record_id=person.old_id
            union*/
            /*По ИНП*/
            select  t.id as person_id,
                   person.id as ref_book_person_id,
                   doc.id as ref_book_id_doc_id,
                   tax.id as book_id_tax_payer_id
              from t join ref_book_id_tax_payer tax on (tax.as_nu = v_asnu and (tax.inp)=(t.inp))
                                 join (select distinct r1.id,r2.id id1 from ref_book_person r1 join ref_book_person r2 on r1.record_id=r2.record_id) a on (a.id1 = tax.person_id)
                                 join ref_book_person person on (person.id=a.id and
                                                                 person.start_date <= sysdate and ( (person.end_date is null) or (person.end_date >= sysdate) ) and person.record_id = person.old_id
																)
                                 left join ref_book_id_doc doc on (doc.person_id in (select id from ref_book_person where record_id=person.record_id))
             where person.record_id=person.old_id
      ) person2
               join ref_book_person person3 on (person3.id=person2.ref_book_person_id)
               left join ref_book_id_doc doc on (doc.id=person2.ref_book_id_doc_id)
               left join ref_book_id_tax_payer tax on (tax.id=person2.book_id_tax_payer_id)
               left join ref_book_person_tb tb on (tb.person_id in (select id from ref_book_person where record_id=person3.record_id));

    return v_ref;
  end;

end PERSON_PKG ;