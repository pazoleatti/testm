set heading off;
set feedback off;
set verify off;
set termout off;
set linesize 2000;
set trimspool on;
set NEWP NONE;

variable v_filename varchar2(100);
variable v_declaration number;
variable v_asnu number;

exec :v_filename:='&1';
exec :v_declaration:=&2;
exec :v_asnu:=&3;

call person_pkg.FillRecordVersions();

spool &1-1.txt;

select to_char(systimestamp, 'HH24:MI:SS.FF4')||' start part1 new version' from dual;

spool off;		 
		 
      select /*+ use_hash(t person)*/ t.id as person_id,
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
       where t.declaration_data_id=:v_declaration
         and t.person_id is null
         and exists(select 1 from tmp_version tv where tv.calc_date = trunc(sysdate) and tv.version = person.version and tv.record_id = person.record_id)
         and person.status=0 and person.record_id=person.old_id;

spool &1-2.txt;

select to_char(systimestamp, 'HH24:MI:SS.FF4')||' finish part1 new version' from dual;

select to_char(systimestamp, 'HH24:MI:SS.FF4')||' start part1 new version with parallel' from dual;

spool off;
		 
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
        from ndfl_person t join ref_book_person person on (replace(lower(nvl(person.last_name,'empty')),' ','') = replace(lower(t.last_name),' ','') and
                                                           replace(lower(nvl(person.first_name,'empty')),' ','') = replace(lower(t.first_name),' ','') and
                                                           replace(lower(nvl(person.middle_name,'empty')),' ','') = replace(lower(t.middle_name),' ','') and
                                                           person.birth_date=t.birth_day
                                                               )
                           left join ref_book_id_doc doc on (doc.person_id in (select id from ref_book_person where record_id=person.record_id) and doc.status=0)
                           left join ref_book_id_tax_payer tax on (tax.person_id in (select id from ref_book_person where record_id=person.record_id) and tax.status=0)
                           left join ref_book_person_tb tb on (tb.person_id in (select id from ref_book_person where record_id=person.record_id) and tb.status=0)                     
                           left join ref_book_address addr on (addr.id=person.address and addr.status=0)
       where t.declaration_data_id=:v_declaration
         and t.person_id is null
         and exists(select 1 from tmp_version tv where tv.calc_date = trunc(sysdate) and tv.version = person.version and tv.record_id = person.record_id)
         and person.status=0 and person.record_id=person.old_id;

spool &1-3.txt;

select to_char(systimestamp, 'HH24:MI:SS.FF4')||' finish part1 new version with parallel' from dual;

select to_char(systimestamp, 'HH24:MI:SS.FF4')||' start part6 new version' from dual;

spool off;

      select /*+ use_hash(t person)*/ t.id as person_id,
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
        from ndfl_person t join ref_book_id_tax_payer tax on (tax.as_nu = &3 and lower(tax.inp)=lower(t.inp) and tax.status=0)
                           join (select distinct r1.id,r2.id id1 from ref_book_person r1 join ref_book_person r2 on r1.record_id=r2.record_id) a on (a.id1 = tax.person_id)
                           join ref_book_person person on (person.id=a.id)
                           left join ref_book_id_doc doc on (doc.person_id in (select id from ref_book_person where record_id=person.record_id) and doc.status=0)
                           left join ref_book_person_tb tb on (tb.person_id in (select id from ref_book_person where record_id=person.record_id) and tb.status=0)                                                
                           left join ref_book_address addr on (addr.id=person.address and addr.status=0)
       where t.declaration_data_id=:v_declaration
         and t.person_id is null
         and exists(select 1 from tmp_version tv where tv.calc_date = trunc(sysdate) and tv.version = person.version and tv.record_id = person.record_id)
         and person.status=0 and person.record_id=person.old_id;

spool &1-4.txt;

select to_char(systimestamp, 'HH24:MI:SS.FF4')||' finish part6 new version' from dual;

select to_char(systimestamp, 'HH24:MI:SS.FF4')||' start part6 new version with parallel' from dual;

spool off;

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
        from ndfl_person t join ref_book_id_tax_payer tax on (tax.as_nu = &3 and lower(tax.inp)=lower(t.inp) and tax.status=0)
                           join (select distinct r1.id,r2.id id1 from ref_book_person r1 join ref_book_person r2 on r1.record_id=r2.record_id) a on (a.id1 = tax.person_id)
                           join ref_book_person person on (person.id=a.id)
                           left join ref_book_id_doc doc on (doc.person_id in (select id from ref_book_person where record_id=person.record_id) and doc.status=0)
                           left join ref_book_person_tb tb on (tb.person_id in (select id from ref_book_person where record_id=person.record_id) and tb.status=0)                                                
                           left join ref_book_address addr on (addr.id=person.address and addr.status=0)
       where t.declaration_data_id=:v_declaration
         and t.person_id is null
         and exists(select 1 from tmp_version tv where tv.calc_date = trunc(sysdate) and tv.version = person.version and tv.record_id = person.record_id)
         and person.status=0 and person.record_id=person.old_id;

spool &1-5.txt;

select to_char(systimestamp, 'HH24:MI:SS.FF4')||' finish part6 new version with parallel' from dual;
	 
spool off;

exit;