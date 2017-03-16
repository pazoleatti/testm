
--unique indexes
create unique index i_form_type_uniq_code on form_type (upper(code));
create unique index i_ref_book_record_refbookid on ref_book_record (case when status <> -1 then ref_book_id else null end, case when status <> -1 then record_id else null end, case when status <> -1 then version else null end);
create unique index i_decl_data_consolidation_unq on declaration_data_consolidation (case when source_declaration_data_id is not null then target_declaration_data_id end, source_declaration_data_id);
create unique index i_form_data_consolidation_unq on form_data_consolidation (case when source_form_data_id is not null then target_form_data_id end, source_form_data_id);
create unique index i_search_data_result on form_search_data_result (session_id, id, ord) /*local*/;
create unique index unq_ref_book_table_name on ref_book(table_name);
create unique index unq_ref_book_oktmo_code_vers on ref_book_oktmo(code,version);
create unique index i_ref_book_oktmo_record_id on ref_book_oktmo(record_id, version);

--indexes
create index i_department_parent_id on department(parent_id);
create index i_form_data_dep_rep_per_id on form_data(department_report_period_id);
create index i_form_data_com_dep_rep_id on form_data(comparative_dep_rep_per_id);
create index i_form_data_form_template_id on form_data(form_template_id);
create index i_form_data_kind on form_data(kind);
create index i_form_data_signer_formdataid on form_data_signer(form_data_id);
create index i_ref_book_value_string on ref_book_value(string_value);
create index i_form_style_form_template_id on form_style(form_template_id);
create index i_form_column_form_template_id on form_column(form_template_id);
create index i_decl_data_dep_rep_per_id on declaration_data (department_report_period_id);
create index i_ifrs_data_blob_data_id on ifrs_data(blob_data_id);
create index i_form_data_rep_blob_data_id on form_data_report(blob_data_id);
create index i_decl_report_blob_data_id on declaration_report(blob_data_id);
create index i_ref_book_script_id on ref_book(script_id);
create index i_declaration_template_xsd on declaration_template(xsd);
create index i_declaration_template_jrxml on declaration_template(jrxml);
create index i_log_system_rep_blob_data_id on log_system_report(blob_data_id);
create index i_lock_data_subscr on lock_data_subscribers(lock_key);
create index i_decl_subrep_blob_data_id on declaration_subreport(blob_data_id);
create index i_notification_report_id on notification (report_id);
create index i_dep_rep_per_report_period_id on department_report_period (report_period_id);
create index i_form_search_result_formdata on form_search_result(form_data_id);
create index fk_decl_data_file_type_id on declaration_data_file(file_type_id);
create index idx_ndfl_person_row_num on ndfl_person (row_num);
create index idx_ref_book_id_tax_payer_pers on ref_book_id_tax_payer(person_id);
create index idx_ref_book_id_doc_pers on ref_book_id_doc(person_id);
create index idx_ref_person_status on ref_book_person(status);
create index idx_ref_book_person_rec_id on ref_book_person (record_id);
create index idx_ref_book_doc_type_code_srv on ref_book_doc_type (code, status, version);
create index idx_ref_book_doc_type_rec_vers on ref_book_doc_type (record_id, version); 
create index idx_ref_person_st_ver_rec on ref_book_person (status, version, record_id) ;
create index idx_ref_book_person_address on ref_book_person(address);
create index idx_ndfl_person_decl_data_id on ndfl_person(declaration_data_id, person_id);
create index idx_ndfl_person_inc_person on ndfl_person_income(ndfl_person_id);
create index idx_ndfl_person_inc_taxdt on ndfl_person_income(ndfl_person_id,tax_date);
create index idx_ndfl_person_inc_paymdt on ndfl_person_income(ndfl_person_id,payment_date);
create index idx_ndfl_person_inc_oktmo on ndfl_person_income(ndfl_person_id,oktmo);
create index idx_ndfl_person_inc_kpp on ndfl_person_income(ndfl_person_id,kpp);
create index srch_ndfl_pers_inc_income on ndfl_person_income(ndfl_person_id,operation_id,income_accrued_date,income_code);
create index idx_ndfl_person_inc_src on ndfl_person_income(source_id);
create index ndfl_pers_inc_kpp_oktmo on ndfl_person_income(kpp,oktmo);
create index idx_ndfl_person_ded_ppcurrdt on ndfl_person_deduction(ndfl_person_id,period_curr_date);
create index idx_ndfl_person_prep_pnotdt on ndfl_person_prepayment(ndfl_person_id,notif_date);
create index idx_ref_deduct_mark_name on ref_book_deduction_mark(name);
create index idx_ref_deduct_type_dmark on ref_book_deduction_type(deduction_mark);
create index idx_decl_data_oktmo_kpp on declaration_data(oktmo, kpp);
create index idx_ras_psv_strlic_decl_person on raschsv_pers_sv_strah_lic(declaration_data_id,person_id);
create index idx_ra_sv_vypl_parent on raschsv_sv_vypl(raschsv_pers_sv_strah_lic_id);
create index idx_ra_sv_vypl_mk_parent on raschsv_sv_vypl_mk(raschsv_sv_vypl_id);
create index idx_ra_vypl_sv_dop_parent on raschsv_vypl_sv_dop(raschsv_pers_sv_strah_lic_id);
create index idx_ra_vypl_sv_dop_mt_parent on raschsv_vypl_sv_dop_mt(raschsv_vypl_sv_dop_id);
create index idx_f_fias_addrobj_formalname on fias_addrobj (replace(lower(formalname),' ',''));
create index idx_fias_addrobj_parentguid on fias_addrobj (parentguid);
create index idx_fias_addrobj_region_status on fias_addrobj (regioncode, currstatus);
create index idx_fias_addrobj_aoid on fias_addrobj(aoid);
create index idx_log_entry_date on log_entry(creation_date);
create index idx_ref_book_cal_work on ref_book_calendar(ctype);
create index i_ref_book_oktmo_code on ref_book_oktmo (code);
create index idx_ref_book_ndfl_detail_load on ref_book_ndfl_detail (ref_book_ndfl_id,tax_organ_code,kpp);

create index srch_ref_person_name_brthd on ref_book_person(replace(lower(last_name), ' ', ''),replace(lower(first_name), ' ', ''),replace(lower(middle_name), ' ', ''),birth_date);
create index srch_ref_book_id_doc_tp_num on ref_book_id_doc(doc_id,replace(lower(doc_number), ' ', ''));
create index srch_ref_book_person_snils on ref_book_person(replace(replace(snils, ' ', ''), '-', ''));
create index srch_refb_tax_payer_inp_asnu on ref_book_id_tax_payer(as_nu,lower(inp));
create index srch_ref_book_person_inn on ref_book_person(replace(inn, ' ', ''));
create index srch_ref_book_person_inn_f on ref_book_person(replace(inn_foreign, ' ', ''));
create index srch_full_ref_pers_duble on ref_book_person (replace(lower(nvl(last_name,'empty')),' ',''), replace(lower(nvl(first_name,'empty')),' ',''), replace(lower(nvl(middle_name,'empty')),' ',''), birth_date, replace(replace(nvl(snils,'empty'),' ',''),'-',''), replace(nvl(inn,'empty'),' ',''), replace(nvl(inn_foreign,'empty'),' ',''));
create index srch_fias_addrobj_postcode on fias_addrobj(replace(postalcode,' ',''));
create index srch_fias_addrobj_regfnls on fias_addrobj(livestatus,regioncode,replace(lower(formalname),' ',''),trim(lower(shortname)));


prompt create indexes for all cascade forein keys
@@create_indexes_for_fk;
exit;