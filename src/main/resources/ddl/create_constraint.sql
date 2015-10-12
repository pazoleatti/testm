alter table form_kind add constraint form_kind_pk primary key (id);

alter table tax_type add constraint tax_type_pk primary key(id);

alter table ref_book_oktmo add constraint ref_book_oktmo_pk primary key (id);
alter table ref_book_oktmo add constraint ref_book_oktmo_fk_parent_id foreign key (parent_id) references ref_book_oktmo(id);
alter table ref_book_oktmo add constraint ref_book_oktmo_chk_status check (status in (0,-1,1,2));
create unique index i_ref_book_oktmo_record_id on ref_book_oktmo(record_id, version);

alter table form_type add constraint form_type_pk primary key (id);
alter table form_type add constraint form_type_fk_taxtype foreign key (tax_type) references tax_type(id);
alter table form_type add constraint form_type_check_status check (status in (-1, 0, 1, 2));
alter table form_type add constraint form_type_uniq_code unique(code);
alter table form_type add constraint form_type_chk_is_ifrs check ((is_ifrs in (0,1) and tax_type='I') or (is_ifrs = 0 and tax_type<>'I'));

alter table tax_period add constraint tax_period_pk primary key (id);
alter table tax_period add constraint tax_period_fk_taxtype foreign key (tax_type) references tax_type(id);
alter table tax_period add constraint tax_period_uniq_taxtype_year unique (tax_type, year);

alter table form_template add constraint form_template_pk primary key (id);
alter table form_template add constraint form_template_fk_type_id foreign key (type_id) references form_type(id);
--ограничение не работает, в таком виде оно бесполезно
--alter table form_template add constraint form_template_uniq_version unique(type_id, version);
alter table form_template add constraint form_template_chk_fixed_rows check(fixed_rows in (0, 1));
alter table form_template add constraint form_template_check_status check (status in (-1, 0, 1, 2));
alter table form_template add constraint form_template_chk_monthly check (monthly in (0, 1));
alter table form_template add constraint form_template_chk_comparative check (comparative in (0, 1));
alter table form_template add constraint form_template_chk_accruing check (accruing in (0, 1));

alter table form_style add constraint form_style_pk primary key (id);
alter table form_style add constraint form_style_fk_form_template_id foreign key (form_template_id) references form_template(id) on delete cascade;
alter table form_style add constraint form_style_chk_font_color check (font_color in (0,1,2,3,4,5,6,7,8,9,10,11,12,13));
alter table form_style add constraint form_style_chk_back_color check (back_color in (0,1,2,3,4,5,6,7,8,9,10,11,12,13));
alter table form_style add constraint form_style_chk_italic check (italic in (0,1));
alter table form_style add constraint form_style_chk_bold check (bold in (0,1));
alter table form_style add constraint form_style_uniq_alias unique (form_template_id, alias);

alter table blob_data add constraint blob_data_pk primary key(id);

alter table ref_book add constraint ref_book_pk primary key (id);
alter table ref_book add constraint ref_book_fk_script_id foreign key (script_id) references blob_data(id);
alter table ref_book add constraint ref_book_chk_type check (type in (0, 1));
alter table ref_book add constraint ref_book_chk_read_only check (read_only in (0, 1));
alter table ref_book add constraint ref_book_chk_versioned check (is_versioned in (0, 1));

alter table ref_book_attribute add constraint ref_book_attr_pk primary key (id);
alter table ref_book_attribute add constraint ref_book_attr_chk_visible check (visible in (0, 1));
alter table ref_book_attribute add constraint ref_book_attr_chk_type check (type in (1, 2, 3, 4));
alter table ref_book_attribute add constraint ref_book_attr_chk_alias check (not lower(alias) in ('record_id', 'row_number_over', 'record_version_from', 'record_version_to'));
alter table ref_book_attribute add constraint ref_book_attr_chk_precision check (precision >= 0 and precision <=10);
alter table ref_book_attribute add constraint ref_book_attr_chk_number_type check ((type <> 2 and precision is null) or (type = 2 and not (precision is null)));
alter table ref_book_attribute add constraint ref_book_attr_chk_ref check ((type <> 4 and reference_id is null) or (type = 4 and not (reference_id is null)));
alter table ref_book_attribute add constraint ref_book_attr_chk_ref_attr check ((type <> 4 and attribute_id is null) or (type = 4 and not (attribute_id is null)));
alter table ref_book_attribute add constraint ref_book_attribute_uniq_ord unique (ref_book_id, ord);
alter table ref_book_attribute add constraint ref_book_attribute_uniq_alias unique (ref_book_id, alias);
alter table ref_book_attribute add constraint ref_book_attr_fk_ref_book_id foreign key (ref_book_id) references ref_book(id);
alter table ref_book_attribute add constraint ref_book_attr_fk_reference_id foreign key (reference_id) references ref_book(id);
alter table ref_book_attribute add constraint ref_book_attr_fk_attribute_id foreign key (attribute_id) references ref_book_attribute(id);

alter table ref_book_attribute add constraint ref_book_attribute_chk_format check (format in (0,1,2,3,4,5,6));
alter table ref_book_attribute add constraint ref_book_attr_chk_read_only check (read_only in (0, 1));
alter table ref_book_attribute add constraint ref_book_attr_chk_max_length check ((type=1 and max_length is not null and max_length between 1 and 2000) or (type=2 and max_length is not null and max_length between 1 and 27) or (type in (3,4) and max_length is null));

alter table ref_book add constraint ref_book_fk_region foreign key (region_attribute_id) references ref_book_attribute(id);

alter table ref_book_record add constraint ref_book_record_pk primary key (id);
alter table ref_book_record add constraint ref_book_record_chk_status check (status in (0, -1, 1 , 2));
alter table ref_book_record add constraint ref_book_record_fk_ref_book_id foreign key (ref_book_id) references ref_book(id);
create unique index i_ref_book_record_refbookid on ref_book_record(ref_book_id, record_id, version);

alter table ref_book_value add constraint ref_book_value_pk primary key (record_id, attribute_id);
alter table ref_book_value add constraint ref_book_value_fk_record_id foreign key (record_id) references ref_book_record(id) on delete cascade;
alter table ref_book_value add constraint ref_book_value_fk_attribute_id foreign key (attribute_id) references ref_book_attribute(id);

alter table form_column add constraint form_column_pk primary key (id);
alter table form_column add constraint form_column_fk_form_templ_id foreign key (form_template_id) references form_template(id) on delete cascade;
alter table form_column add constraint form_column_uniq_alias unique(form_template_id, alias);
alter table form_column add constraint form_column_chk_type check(type in ('N', 'S', 'D', 'R', 'A'));
alter table form_column add constraint form_column_chk_precision check((type = 'N' and precision is not null and precision >=0 and precision < 9) or (type <> 'N' and precision is null));
alter table form_column add constraint form_column_chk_max_length check ((type = 'S' and max_length is not null and max_length > 0 and max_length <= 2000) or (type = 'N' and max_length is not null and max_length > 0 and max_length <= 27) or ((type ='D' or type ='R' or type='A') and max_length is null));
alter table form_column add constraint form_column_chk_checking check (checking in (0, 1));
alter table form_column add constraint form_column_chk_attribute_id check ((type = 'R' and attribute_id is not null and precision >=0 and precision < 9) or (type <> 'R' and attribute_id is null));
alter table form_column add constraint form_column_chk_width check (not width is null);
alter table form_column add constraint form_column_fk_attribute_id foreign key (attribute_id) references ref_book_attribute(id);
alter table form_column add constraint form_column_fk_attribute_id2 foreign key (attribute_id2) references ref_book_attribute(id);
alter table form_column add constraint form_column_fk_parent_id foreign key (parent_column_id) references form_column(id);
alter table form_column add constraint form_column_chk_filt_parent check ((type='R' and ((parent_column_id is null) and (filter is not null)) or ((parent_column_id is not null) and (filter is null)) or ((parent_column_id is null) and (filter is null))) or (type<>'R'));
alter table form_column add constraint form_column_chk_numrow check (numeration_row in (0, 1) or type <> 'A');

alter table department_type add constraint department_type_pk primary key (id);

alter table department add constraint department_pk primary key (id);
alter table department add constraint dept_fk_parent_id foreign key (parent_id) references department(id);
alter table department add constraint department_fk_type foreign key(type) references department_type(id);
alter table department add constraint department_chk_is_active check (is_active in (0, 1));
alter table department add constraint department_chk_garant_use check (garant_use in (0, 1));

alter table configuration add constraint configuration_pk primary key (code, department_id);
alter table configuration add constraint configuration_fk foreign key (department_id) references department(id) on delete cascade;

alter table report_period add constraint report_period_pk primary key(id);
alter table report_period add constraint report_period_fk_taxperiod foreign key (tax_period_id) references tax_period(id);
alter table report_period add constraint report_period_fk_dtp_id foreign key (dict_tax_period_id) references ref_book_record(id);
alter table report_period add constraint report_period_uniq_tax_dict unique (tax_period_id, dict_tax_period_id);
alter table report_period add constraint report_period_chk_date check (end_date >= start_date);

alter table income_101 add constraint income_101_pk primary key (id);
alter table income_101 add constraint income_101_fk_accperiod_id foreign key (account_period_id) references ref_book_record(id);

alter table income_102 add constraint income_102_pk primary key (id);
alter table income_102 add constraint income_102_fk_accperiod_id foreign key (account_period_id) references ref_book_record(id);

alter table declaration_type add constraint declaration_type_pk primary key (id);
alter table declaration_type add constraint declaration_type_fk_taxtype foreign key (tax_type) references tax_type(id);
alter table declaration_type add constraint declaration_type_chk_status check (status in (-1, 0, 1, 2));
alter table declaration_type add constraint declaration_type_chk_is_ifrs check ((is_ifrs in (0,1) and tax_type='I') or (is_ifrs = 0 and tax_type<>'I'));

alter table department_declaration_type add constraint dept_decl_type_pk primary key (id);
alter table department_declaration_type add constraint dept_decl_type_fk_dept foreign key (department_id) references department(id);
alter table department_declaration_type add constraint dept_decl_type_fk_decl_type foreign key (declaration_type_id) references declaration_type(id);
alter table department_declaration_type add constraint dept_decl_type_uniq_decl unique (department_id, declaration_type_id);

alter table declaration_template add constraint declaration_template_pk primary key (id);
alter table declaration_template add constraint declaration_template_fk_dtype foreign key (declaration_type_id) references declaration_type (id);
alter table declaration_template add constraint declaration_tem_fk_blob_data foreign key (XSD) references blob_data(id);
alter table declaration_template add constraint dec_tem_fk_blob_data_jrxml foreign key (jrxml) references blob_data(id);
alter table declaration_template add constraint dec_template_check_status check (status in (-1, 0, 1, 2));

alter table department_report_period add constraint department_report_period_pk primary key(id);
alter table department_report_period add constraint dep_rep_per_chk_is_active check (is_active in (0, 1));
alter table department_report_period add constraint dep_rep_per_chk_is_balance_per check (is_balance_period in (0, 1));
alter table department_report_period add constraint dep_rep_per_fk_department_id foreign key (department_id) references department(id) on delete cascade;
alter table department_report_period add constraint dep_rep_per_fk_rep_period_id foreign key (report_period_id) references report_period(id) on delete cascade;

alter table declaration_data add constraint declaration_data_pk primary key (id);
alter table declaration_data add constraint declaration_data_fk_decl_t_id foreign key (declaration_template_id) references declaration_template(id);
alter table declaration_data add constraint decl_data_fk_dep_rep_per_id foreign key (department_report_period_id) references department_report_period (id);
alter table declaration_data add constraint declaration_data_chk_is_accptd check (is_accepted in (0,1));
alter table declaration_data add constraint declaration_data_uniq_template unique (department_report_period_id, declaration_template_id, tax_organ_code, kpp);

alter table form_data add constraint form_data_pk primary key (id);
alter table form_data add constraint form_data_fk_form_templ_id foreign key (form_template_id) references form_template(id);
alter table form_data add constraint form_data_fk_dep_rep_per_id foreign key (department_report_period_id) references department_report_period (id);
alter table form_data add constraint form_data_fk_co_dep_rep_per_id foreign key (comparative_dep_rep_per_id) references department_report_period (id);
alter table form_data add constraint form_data_fk_kind foreign key (kind) references form_kind(id);
alter table form_data add constraint form_data_chk_state check(state in (1,2,3,4));
alter table form_data add constraint form_data_chk_return_sign check(return_sign in (0,1));
alter table form_data add constraint form_data_chk_period_order check(period_order in (1,2,3,4,5,6,7,8,9,10,11,12));
alter table form_data add constraint form_data_chk_manual check (manual in (0, 1));
alter table form_data add constraint form_data_chk_sorted check (sorted in (0, 1));
alter table form_data add constraint form_data_chk_accruing check (accruing in (0, 1));
alter table form_data add constraint form_data_chk_edited check (edited in (0, 1));
alter table form_data add constraint form_data_chk_sorted_backup check (sorted_backup in (0, 1));

alter table form_data_file add constraint form_data_file_pk primary key (blob_data_id, form_data_id);
alter table form_data_file add constraint form_data_file_fk_form_data foreign key (form_data_id) references form_data(id);
alter table form_data_file add constraint form_data_file_fk_blob_data foreign key (blob_data_id) references blob_data(id);

alter table form_data_ref_book add constraint form_data_ref_book_fk_formdata foreign key (form_data_id) references form_data(id) on delete cascade;
alter table form_data_ref_book add constraint form_data_ref_book_fk_refbook foreign key (ref_book_id) references ref_book(id) on delete cascade;

alter table form_data_signer add constraint form_data_signer_pk primary key (id);
alter table form_data_signer add constraint form_data_signer_fk_formdata foreign key (form_data_id) references form_data(id) on delete cascade;

alter table form_data_performer add constraint form_data_performer_pk primary key (form_data_id);
alter table form_data_performer add constraint formdata_performer_fk_formdata foreign key (form_data_id) references form_data(id) on delete cascade;
alter table form_data_performer add constraint formdata_performer_fk_dept foreign key (print_department_id) references department(id);

alter table department_form_type add constraint dept_form_type_fk_dep_id foreign key (department_id) references department(id);
alter table department_form_type add constraint dept_form_type_fk_perf_dep_id foreign key (performer_dep_id) references department(id);
alter table department_form_type add constraint dept_form_type_fk_type_id foreign key (form_type_id) references form_type(id);
alter table department_form_type add constraint dept_form_type_fk_kind foreign key (kind) references form_kind(id);
alter table department_form_type add constraint dept_form_type_pk primary key (id);
alter table department_form_type add constraint dept_form_type_uniq_form unique (department_id, form_type_id, kind);

alter table declaration_source add constraint declaration_source_pk primary key (department_declaration_type_id,src_department_form_type_id, period_start);
alter table declaration_source add constraint decl_source_fk_dept_decltype foreign key (department_declaration_type_id) references department_declaration_type(id) on delete cascade;
alter table declaration_source add constraint decl_source_fk_dept_formtype foreign key (src_department_form_type_id) references department_form_type (id) on delete cascade;

alter table form_data_source add constraint form_data_source_pk primary key (department_form_type_id, src_department_form_type_id, period_start);
alter table form_data_source add constraint form_data_source_fk_dep_id foreign key (department_form_type_id) references department_form_type(id) on delete cascade;
alter table form_data_source add constraint form_data_source_fk_src_dep_id foreign key (src_department_form_type_id) references department_form_type(id) on delete cascade;

alter table sec_user add constraint sec_user_pk primary key (id);
alter table sec_user add constraint sec_user_fk_dep_id foreign key (department_id) references department(id);
alter table sec_user add constraint sec_user_uniq_login_active unique (login);
alter table sec_user add constraint sec_user_chk_is_active check (is_active in (0, 1));

alter table sec_role add constraint sec_role_pk primary key (id);
alter table sec_role add constraint sec_role_uniq_alias unique (alias);

alter table sec_user_role add constraint sec_user_role_pk primary key (user_id, role_id);
alter table sec_user_role add constraint sec_user_role_fk_user_id foreign key (user_id) references sec_user(id);
alter table sec_user_role add constraint sec_user_role_fk_role_id foreign key (role_id) references sec_role(id);

alter table log_business add constraint log_business_fk_declaration_id foreign key (declaration_data_id) references declaration_data(id) on delete cascade;
alter table log_business add constraint log_business_fk_form_data_id foreign key (form_data_id) references form_data(id) on delete cascade;
alter table log_business add constraint log_business_chk_event_id check (event_id in (1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 203, 204, 205, 206, 207, 208, 209, 210, 301, 302, 303, 401));
alter table log_business add constraint log_business_chk_frm_dcl_ev check (form_data_id is not null or declaration_data_id is not null);

alter table task_context add constraint task_context_uniq_task_id unique (task_id);
alter table task_context add constraint task_context_uniq_task_name unique (task_name);
alter table task_context add constraint task_context_fk_user_id foreign key (user_id) references sec_user(id);

alter table notification add constraint notification_fk_report_period foreign key (report_period_id) references report_period (id) on delete cascade;
alter table notification add constraint notification_fk_sender foreign key (sender_department_id) references department (id) on delete cascade;
alter table notification add constraint notification_fk_receiver foreign key (receiver_department_id) references department (id) on delete cascade;
alter table notification add constraint notification_fk_notify_user foreign key (user_id) references sec_user(id);
alter table notification add constraint notification_fk_notify_role foreign key (role_id) references sec_role(id);
alter table notification add constraint notification_chk_isread check (is_read in (0, 1));
alter table notification add constraint notification_fk_blob_data_id foreign key (blob_data_id) references blob_data(id);

alter table event add constraint event_pk primary key (id);

alter table log_business add constraint log_business_fk_event_id foreign key (event_id) references event(id);

alter table role_event add constraint role_event_pk primary key (event_id, role_id);
alter table role_event add constraint role_event_fk_event_id foreign key (event_id) references event(id);
alter table role_event add constraint role_event_fk_role_id foreign key (role_id) references sec_role(id);

alter table template_changes add constraint template_changes_pk primary key (id);
alter table template_changes add constraint template_changes_fk_user_id foreign key (author) references sec_user(id);
alter table template_changes add constraint template_changes_chk_event check (event in (701, 702, 703, 704, 705));
alter table template_changes add constraint template_changes_fk_event foreign key (event) references event(id);
alter table template_changes add constraint template_changes_chk_template check ((form_template_id is not null and declaration_template_id is null) or (form_template_id is null and declaration_template_id is not null));

alter table log_system add constraint log_system_fk_event_id foreign key (event_id) references event(id);
alter table log_system add constraint log_system_chk_dcl_form check (event_id in (7, 11, 401, 402, 501, 502, 503, 601, 650, 901, 902, 903, 810, 811, 812, 813, 820, 821, 830, 831, 832, 840, 841, 842, 850, 860, 701, 702, 703, 704, 705, 904) or declaration_type_name is not null or (form_type_name is not null and form_kind_id is not null));
alter table log_system add constraint log_system_chk_rp check (event_id in (7, 11, 401, 402, 501, 502, 503, 601, 650, 901, 902, 903, 810, 811, 812, 813, 820, 821, 830, 831, 832, 840, 841, 842, 850, 860, 701, 702, 703, 704, 705, 904) or report_period_name is not null);
alter table log_system add constraint log_system_fk_kind foreign key (form_kind_id) references form_kind(id);
alter table log_system add constraint log_system_fk_user_login foreign key (user_login) references sec_user(login);
alter table log_system add constraint log_system_fk_blob_data foreign key (blob_data_id) references blob_data(id) on delete set null;
alter table log_system add constraint log_system_chk_is_error check (is_error in (0, 1));

alter table lock_data add constraint lock_data_pk primary key (key);
alter table lock_data add constraint lock_data_fk_user_id foreign key (user_id) references sec_user(id) on delete cascade;

alter table lock_data_subscribers add constraint lock_data_subscribers_pk primary key (lock_key, user_id);
alter table lock_data_subscribers add constraint lock_data_subscr_fk_lock_data foreign key (lock_key) references lock_data(key) on delete cascade;
alter table lock_data_subscribers add constraint lock_data_subscr_fk_sec_user foreign key (user_id) references sec_user(id) on delete cascade;

alter table async_task_type add constraint async_task_type_pk primary key (id);
alter table async_task_type add constraint async_task_type_chk_dev_mode check (dev_mode in (0, 1));

alter table form_data_report add constraint form_data_rep_pk primary key (form_data_id,type, manual,checking,absolute);
alter table form_data_report add constraint form_data_rep_fk_form_data_id foreign key (form_data_id) references form_data(id) on delete cascade;
alter table form_data_report add constraint form_data_rep_fk_blob_data_id foreign key (blob_data_id) references blob_data(id);
alter table form_data_report add constraint form_data_rep_chk_type check (type in (0,1,2,3));
alter table form_data_report add constraint form_data_rep_chk_manual check (manual in (0,1));
alter table form_data_report add constraint form_data_rep_chk_checking check (checking in (0,1));
alter table form_data_report add constraint form_data_rep_chk_absolute check (absolute in (0,1));

alter table declaration_report add constraint decl_report_pk primary key (declaration_data_id, type);
alter table declaration_report add constraint decl_report_fk_decl_data foreign key(declaration_data_id) references declaration_data(id) on delete cascade;
alter table declaration_report add constraint decl_report_fk_blob_data foreign key(blob_data_id) references blob_data(id) on delete cascade;
alter table declaration_report add constraint decl_report_chk_type check (type in (0, 1, 2, 3));

alter table ifrs_data add constraint ifrs_data_pk primary key (report_period_id);
alter table ifrs_data add constraint ifrs_data_fk_report_period foreign key (report_period_id) references report_period(id);
alter table ifrs_data add constraint ifrs_data_fk_blob_data foreign key (blob_data_id) references blob_data(id);

alter table configuration_email add constraint configuration_email_pk primary key (id);
alter table configuration_email add constraint configuration_email_unqname unique (name);

alter table form_data_consolidation add constraint form_data_consolidation_fk_src foreign key (source_form_data_id) references form_data(id);
alter table form_data_consolidation add constraint form_data_consolidation_fk_tgt foreign key (target_form_data_id) references form_data(id) on delete cascade;
--create unique index i_form_data_consolidation_unq on form_data_consolidation (case when source_form_data_id is not null then target_form_data_id end, source_form_data_id);

alter table declaration_data_consolidation add constraint decl_data_consolidation_fk_src foreign key (source_form_data_id) references form_data(id);
alter table declaration_data_consolidation add constraint decl_data_consolidation_fk_tgt foreign key (target_declaration_data_id) references declaration_data(id)  on delete cascade;
--create unique index i_decl_data_consolidation_unq on declaration_data_consolidation (case when source_form_data_id is not null then target_declaration_data_id end, source_form_data_id);

alter table log_system_report add constraint log_system_report_fk_blob_data foreign key (blob_data_id) references blob_data (id) on delete cascade;
alter table log_system_report add constraint log_system_report_fk_sec_user foreign key (sec_user_id) references sec_user (id) on delete cascade;
alter table log_system_report add constraint log_system_report_chk_type check (type in (0, 1));
alter table log_system_report add constraint log_system_report_unq_sec_user unique(sec_user_id);

------------------------------------------------------------------------------------------------------
create index i_department_parent_id on department(parent_id);
create index i_form_data_dep_rep_per_id on form_data(department_report_period_id);
create index i_form_data_form_template_id on form_data(form_template_id);
create index i_form_data_kind on form_data(kind);
create index i_form_data_signer_formdataid on form_data_signer(form_data_id);
create index i_ref_book_value_string on ref_book_value(string_value);
create index i_ref_book_oktmo_code on ref_book_oktmo(code);
create index i_form_style_form_template_id on form_style(form_template_id);
create index i_form_column_form_template_id on form_column(form_template_id);
create index i_decl_data_dep_rep_per_id on declaration_data (department_report_period_id);
create index i_ifrs_data_blob_data_id on ifrs_data(blob_data_id);
create index i_form_data_rep_blob_data_id on form_data_report(blob_data_id);
create index i_decl_report_blob_data_id on declaration_report(blob_data_id);
create index i_log_system_blob_data_id on log_system(blob_data_id);
create index i_ref_book_script_id on ref_book(script_id);
create index i_declaration_template_xsd on declaration_template(xsd);
create index i_declaration_template_jrxml on declaration_template(jrxml);
create index i_notification_blob_data_id on notification(blob_data_id);
create index i_log_system_rep_blob_data_id on log_system_report(blob_data_id);
create index i_lock_data_subscr on lock_data_subscribers(lock_key);
