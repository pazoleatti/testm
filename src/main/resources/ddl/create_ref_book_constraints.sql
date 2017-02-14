-----------------------------------------------------------------------------------------------------------------------------
-- Создание ограничений для справочников
-----------------------------------------------------------------------------------------------------------------------------

-- primary keys
alter table ref_book_oktmo add constraint ref_book_oktmo_pk primary key (id);
alter table ref_book_income_type add constraint pk_ref_book_income_type primary key (id);
alter table ref_book_deduction_type add constraint pk_ref_book_deduction_type primary key (id);
alter table ref_book_region add constraint pk_ref_book_region primary key (id);
alter table ref_book_present_place add constraint pk_ref_book_present_place primary key(id);
alter table ref_book_asnu add constraint pk_ref_book_asnu primary key(id);
alter table ref_book_form_type add constraint pk_ref_book_form_type primary key(id);
alter table declaration_kind add constraint pk_declaration_kind primary key(id);
alter table ref_book_okved add constraint pk_ref_book_okved primary key(id);
alter table ref_book_deduction_mark add constraint pk_ref_book_deduction_mark primary key(id);
alter table ref_book_reorganization add constraint pk_ref_book_reorganization primary key(id);
alter table ref_book_doc_state add constraint pk_ref_book_doc_state primary key (id);
alter table ref_book_income_kind add constraint pk_ref_book_income_kind primary key (id);
alter table ref_book_attach_file_type add constraint pk_ref_book_attach_file_type primary key(id);
alter table ref_book_tax_inspection add constraint pk_ref_book_tax_inspection primary key(id);
alter table ref_book_ndfl_rate add constraint pk_ref_book_ndfl_rate primary key(id);
alter table ref_book_ndfl add constraint pk_ref_book_ndfl primary key(id);
alter table ref_book_ndfl_detail add constraint pk_ref_book_ndfl_detail primary key(id);
alter table ref_book_fond add constraint pk_ref_book_fond primary key(id);
alter table ref_book_fond_detail add constraint pk_ref_book_fond_detail primary key(id);
alter table ref_book_fill_base add constraint pk_ref_book_fill_base primary key (id);
alter table ref_book_budget_income add constraint pk_ref_book_budget_income primary key (id);
alter table ref_book_hard_work add constraint pk_ref_book_hard_work primary key (id);
alter table ref_book_tariff_payer add constraint pk_ref_book_tariff_payer primary key (id);
alter table report_period_type add constraint pk_report_period_type primary key (id);
alter table ref_book_country add constraint pk_ref_book_country primary key (id);
alter table ref_book_doc_type add constraint pk_ref_book_doc_type primary key (id);
alter table ref_book_tax_place_type add constraint pk_ref_book_tax_place_type primary key (id);
alter table ref_book_signatory_mark add constraint pk_ref_book_signatory_mark primary key (id);
alter table ref_book_person_category add constraint pk_ref_book_person_category primary key(id);

-- foreign keys
alter table ref_book_region add constraint fk_ref_book_region_oktmo foreign key(oktmo) references ref_book_oktmo(id);
alter table ref_book_person add constraint fk_ref_book_person_source foreign key(source_id) references ref_book_asnu(id);
alter table ref_book_income_kind add constraint fk_ref_book_inckind_inctype foreign key (income_type_id) references ref_book_income_type(id);
alter table declaration_data_file add constraint fk_decl_data_file_type_id foreign key (file_type_id) references ref_book_attach_file_type(id);
alter table ref_book_ndfl add constraint fk_ref_book_ndfl_depart foreign key(department_id) references department(id);
alter table ref_book_ndfl_detail add constraint fk_ref_book_ndfl_det_parent foreign key(ref_book_ndfl_id) references ref_book_ndfl(id) on delete cascade;
alter table ref_book_ndfl_detail add constraint fk_ref_book_ndfl_det_pres_pl foreign key(present_place) references ref_book_present_place(id);
alter table ref_book_ndfl_detail add constraint fk_ref_book_ndfl_det_okved foreign key(okved) references ref_book_okved(id);
alter table ref_book_ndfl_detail add constraint fk_ref_book_ndfl_det_region foreign key(region) references ref_book_region(id);
alter table ref_book_ndfl_detail add constraint fk_ref_book_ndfl_det_oblig foreign key(obligation) references ref_book_record(id);
alter table ref_book_ndfl_detail add constraint fk_ref_book_ndfl_det_type foreign key(type) references ref_book_record(id);
alter table ref_book_ndfl_detail add constraint fk_ref_book_ndfl_det_re_code foreign key(reorg_form_code) references ref_book_reorganization(id);
alter table ref_book_ndfl_detail add constraint fk_ref_book_ndfl_det_signatory foreign key(signatory_id) references ref_book_signatory_mark(id);
alter table ref_book_fond add constraint fk_ref_book_fond_depart foreign key(department_id) references department(id);
alter table ref_book_fond_detail add constraint fk_ref_book_fond_det_parent foreign key(ref_book_fond_id) references ref_book_fond(id) on delete cascade;
alter table ref_book_fond_detail add constraint fk_ref_book_fond_det_pres_pl foreign key(present_place) references ref_book_present_place(id);
alter table ref_book_fond_detail add constraint fk_ref_book_fond_det_okved foreign key(okved) references ref_book_okved(id);
alter table ref_book_fond_detail add constraint fk_ref_book_fond_det_region foreign key(region) references ref_book_region(id);
alter table ref_book_fond_detail add constraint fk_ref_book_fond_det_oblig foreign key(obligation) references ref_book_record(id);
alter table ref_book_fond_detail add constraint fk_ref_book_fond_det_type foreign key(type) references ref_book_record(id);
alter table ref_book_fond_detail add constraint fk_ref_book_fond_det_re_code foreign key(reorg_form_code) references ref_book_reorganization(id);
alter table ref_book_fond_detail add constraint fk_ref_book_fond_det_signatory foreign key(signatory_id) references ref_book_signatory_mark(id);
alter table declaration_data add constraint declaration_data_fk_asnu_id foreign key (asnu_id) references ref_book_asnu(id);
alter table declaration_template add constraint fk_declaration_template_fkind foreign key(form_kind) references declaration_kind(id);
alter table declaration_template add constraint fk_declaration_template_ftype foreign key(form_type) references ref_book_form_type(id);
alter table declaration_data add constraint fk_decl_data_doc_state foreign key(doc_state_id) references ref_book_doc_state(id);
alter table report_period add constraint report_period_fk_dtp_id foreign key(dict_tax_period_id) references report_period_type(id);
------
alter table ref_book_address add constraint fk_ref_book_address_country foreign key (country_id) references ref_book_country(id);
alter table ref_book_person add constraint fk_ref_book_person_citizenship foreign key (citizenship) references ref_book_country(id);
alter table ref_book_id_doc add constraint fk_ref_book_id_doc_doc_id foreign key (doc_id) references ref_book_doc_type(id);
alter table ref_book_id_tax_payer add constraint fk_ref_book_id_tax_payer_as_nu foreign key (as_nu) references ref_book_asnu(id);
------
-- checks
alter table ref_book_oktmo add constraint ref_book_oktmo_chk_status check (status in (0,-1,1,2));
alter table ref_book_income_type add constraint chk_ref_book_income_type_st check (status in (-1,0,1,2));
alter table ref_book_deduction_type add constraint chk_ref_book_deduc_type_status check (status in (-1,0,1,2));
alter table ref_book_region add constraint chk_ref_book_region_status check (status in (-1,0,1,2));
--alter table ref_book_region add constraint chk_ref_book_region_okato_def check (decode(translate('#'||okato_definition,'#1234567890','#'),'#','ЦИФРЫ','Буквы')='ЦИФРЫ') disable;
--alter table ref_book_region add constraint chk_ref_book_region_oktmo_def check (decode(translate('#'||oktmo_definition,'#1234567890','#'),'#','ЦИФРЫ','Буквы')='ЦИФРЫ') disable;
alter table ref_book_present_place add constraint chk_ref_book_pres_place_st check (status in (-1,0,1,2));
--alter table ref_book_asnu add constraint chk_ref_book_asnu_code check (code between '0000' and '9999');
--alter table ref_book_form_type add constraint chk_ref_book_form_type_taxkind check (tax_kind in ('F','N')) disable novalidate;
alter table ref_book_okved add constraint chk_ref_book_okved_status check (status between -1 and 2);
alter table ref_book_deduction_mark add constraint chk_ref_book_ded_mark_status check (status between -1 and 2);
alter table ref_book_reorganization add constraint chk_ref_book_reorg_status check(status between -1 and 2);
alter table ref_book_ndfl add constraint chk_ref_book_ndfl_status check (status in (-1,0,1,2));
alter table ref_book_ndfl_detail add constraint chk_ref_book_ndfl_det_status check (status in (-1,0,1,2));
alter table ref_book_fond add constraint chk_ref_book_fond_status check (status in (-1,0,1,2));
alter table ref_book_fond_detail add constraint chk_ref_book_fond_det_status check (status in (-1,0,1,2));
alter table ref_book_country add constraint chk_ref_book_countr_st check (status between -1 and 2);
alter table ref_book_doc_type add constraint chk_ref_book_doc_type_st check (status between -1 and 2);
alter table ref_book_tax_place_type add constraint chk_ref_tax_place_type_status check(status between -1 and 2);
alter table ref_book_signatory_mark add constraint chk_ref_signatory_mark check(status between -1 and 2);

--unique
--create unique index i_ref_book_oktmo_record_id on ref_book_oktmo(record_id, version);
--------------------------------------------------------------------------------------------------------------------------
--indexes
create index i_ref_book_oktmo_code on ref_book_oktmo (code);
--create index idx_ref_book_ndfl_detail_load on ref_book_ndfl_detail (ref_book_ndfl_id,tax_organ_code,kpp);
--create unique index i_ref_book_oktmo_record_id on ref_book_oktmo (record_id,version);
