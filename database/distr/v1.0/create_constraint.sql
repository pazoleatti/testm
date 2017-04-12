--primary keys
alter table form_kind add constraint form_kind_pk primary key (id);
alter table tax_type add constraint tax_type_pk primary key(id);
alter table form_type add constraint form_type_pk primary key (id);
alter table tax_period add constraint tax_period_pk primary key (id);
alter table form_template add constraint form_template_pk primary key (id);
alter table color add constraint color_pk primary key(id);
alter table form_style add constraint form_style_pk primary key (id);
alter table blob_data add constraint blob_data_pk primary key(id);
alter table ref_book add constraint ref_book_pk primary key (id);
alter table ref_book_attribute add constraint ref_book_attr_pk primary key (id);
alter table ref_book_record add constraint ref_book_record_pk primary key (id);
alter table ref_book_value add constraint ref_book_value_pk primary key (record_id, attribute_id);
alter table form_column add constraint form_column_pk primary key (id);
alter table department_type add constraint department_type_pk primary key (id);
alter table department add constraint department_pk primary key (id);
alter table configuration add constraint configuration_pk primary key (code, department_id);
alter table report_period add constraint report_period_pk primary key(id);
alter table declaration_type add constraint declaration_type_pk primary key (id);
alter table department_declaration_type add constraint dept_decl_type_pk primary key (id);
alter table declaration_template add constraint declaration_template_pk primary key (id);
alter table department_report_period add constraint department_report_period_pk primary key(id);
alter table declaration_data add constraint declaration_data_pk primary key (id);
alter table form_data add constraint form_data_pk primary key (id);
alter table form_data_file add constraint form_data_file_pk primary key (blob_data_id, form_data_id);
alter table form_data_ref_book add constraint form_data_ref_book_pk primary key (form_data_id, ref_book_id, record_id);
alter table form_data_signer add constraint form_data_signer_pk primary key (id);
alter table form_data_performer add constraint form_data_performer_pk primary key (form_data_id);
alter table department_form_type add constraint dept_form_type_pk primary key (id);
alter table department_form_type_performer add constraint department_form_type_perf_pk primary key (department_form_type_id, performer_dep_id);
alter table declaration_source add constraint declaration_source_pk primary key (department_declaration_type_id,src_department_form_type_id, period_start);
alter table form_data_source add constraint form_data_source_pk primary key (department_form_type_id, src_department_form_type_id, period_start);
alter table sec_user add constraint sec_user_pk primary key (id);
alter table sec_role add constraint sec_role_pk primary key (id);
alter table sec_user_role add constraint sec_user_role_pk primary key (user_id, role_id);
alter table log_business add constraint log_business_pk primary key (id);
alter table task_context add constraint task_context_pk primary key (id);
alter table notification add constraint notification_pk primary key (id);
alter table event add constraint event_pk primary key (id);
alter table log_clob_query add constraint log_clob_query_pk primary key (id);
alter table role_event add constraint role_event_pk primary key (event_id, role_id);
alter table template_changes add constraint template_changes_pk primary key (id);
alter table audit_form_type add constraint audit_form_type_pk primary key (id);
alter table log_system add constraint log_system_pk primary key (id);
alter table lock_data add constraint lock_data_pk primary key (key);
alter table lock_data_subscribers add constraint lock_data_subscribers_pk primary key (lock_key, user_id);
alter table async_task_type add constraint async_task_type_pk primary key (id);
alter table form_data_report add constraint form_data_rep_pk primary key (form_data_id,type, manual,checking,absolute);
alter table declaration_subreport add constraint decl_subrep_pk primary key(id);
alter table ifrs_data add constraint ifrs_data_pk primary key (report_period_id);
alter table configuration_email add constraint configuration_email_pk primary key (id);
alter table form_search_result add constraint form_search_result_pk primary key (id);
alter table department_change add constraint dep_change_pk primary key (department_id, log_date);
alter table declaration_data_file add constraint decl_data_file_pk primary key (blob_data_id, declaration_data_id);
alter table declaration_subreport_params add constraint pk_decl_subrep_params primary key (id);
alter table state add constraint pk_state primary key(id);
alter table state_change add constraint pk_state_change primary key(id);
alter table declaration_template_file add constraint pk_declaration_template_file primary key (blob_data_id,declaration_template_id);
alter table department_decl_type_performer add constraint pk_department_decl_type_perf primary key (department_decl_type_id, performer_dep_id);

--unique
alter table tax_period add constraint tax_period_uniq_taxtype_year unique (tax_type, year);
alter table color add constraint color_unq_name unique(name);
alter table color add constraint color_unq_rgb unique (r,g,b);
alter table color add constraint color_unq_hex unique (hex);
alter table form_style add constraint form_style_uniq_alias unique (form_template_id, alias);
alter table ref_book_attribute add constraint ref_book_attribute_uniq_ord unique (ref_book_id, ord);
alter table ref_book_attribute add constraint ref_book_attribute_uniq_alias unique (ref_book_id, alias);
alter table form_column add constraint form_column_uniq_alias unique(form_template_id, alias);
alter table report_period add constraint report_period_uniq_tax_dict unique (tax_period_id, dict_tax_period_id);
alter table department_declaration_type add constraint dept_decl_type_uniq_decl unique (department_id, declaration_type_id);
alter table department_form_type add constraint dept_form_type_uniq_form unique (department_id, form_type_id, kind);
alter table sec_user add constraint sec_user_uniq_login_active unique (login);
alter table sec_role add constraint sec_role_uniq_alias unique (alias);
alter table task_context add constraint task_context_uniq_task_id unique (task_id);
alter table task_context add constraint task_context_uniq_task_name unique (task_name);
alter table declaration_subreport add constraint decl_subrep_unq_combo unique (declaration_template_id, alias);
alter table declaration_report add constraint declaration_report_unq_combo unique (declaration_data_id, type, subreport_id);
alter table configuration_email add constraint configuration_email_unqname unique (name);
alter table log_system_report add constraint log_system_report_unq_sec_user unique(sec_user_id);

--foreign keys
alter table form_type add constraint form_type_fk_taxtype foreign key (tax_type) references tax_type(id);
alter table tax_period add constraint tax_period_fk_taxtype foreign key (tax_type) references tax_type(id);
alter table form_template add constraint form_template_fk_type_id foreign key (type_id) references form_type(id);
alter table form_style add constraint form_style_fk_font_color foreign key(font_color) references color(id);
alter table form_style add constraint form_style_fk_back_color foreign key(back_color) references color(id);
alter table form_style add constraint form_style_fk_form_template_id foreign key (form_template_id) references form_template(id) on delete cascade;
alter table ref_book add constraint ref_book_fk_script_id foreign key (script_id) references blob_data(id);
alter table ref_book_attribute add constraint ref_book_attr_fk_ref_book_id foreign key (ref_book_id) references ref_book(id);
alter table ref_book_attribute add constraint ref_book_attr_fk_reference_id foreign key (reference_id) references ref_book(id);
alter table ref_book_attribute add constraint ref_book_attr_fk_attribute_id foreign key (attribute_id) references ref_book_attribute(id);
alter table ref_book add constraint ref_book_fk_region foreign key (region_attribute_id) references ref_book_attribute(id);
alter table ref_book_record add constraint ref_book_record_fk_ref_book_id foreign key (ref_book_id) references ref_book(id);
alter table ref_book_value add constraint ref_book_value_fk_record_id foreign key (record_id) references ref_book_record(id) on delete cascade;
alter table ref_book_value add constraint ref_book_value_fk_attribute_id foreign key (attribute_id) references ref_book_attribute(id);
alter table form_column add constraint form_column_fk_form_templ_id foreign key (form_template_id) references form_template(id) on delete cascade;
alter table form_column add constraint form_column_fk_attribute_id foreign key (attribute_id) references ref_book_attribute(id);
alter table form_column add constraint form_column_fk_attribute_id2 foreign key (attribute_id2) references ref_book_attribute(id);
alter table form_column add constraint form_column_fk_parent_id foreign key (parent_column_id) references form_column(id);
alter table department add constraint dept_fk_parent_id foreign key (parent_id) references department(id);
alter table department add constraint department_fk_type foreign key(type) references department_type(id);
alter table configuration add constraint configuration_fk foreign key (department_id) references department(id) on delete cascade;
alter table report_period add constraint report_period_fk_taxperiod foreign key (tax_period_id) references tax_period(id);
alter table declaration_type add constraint declaration_type_fk_taxtype foreign key (tax_type) references tax_type(id);
alter table department_declaration_type add constraint dept_decl_type_fk_dept foreign key (department_id) references department(id);
alter table department_declaration_type add constraint dept_decl_type_fk_decl_type foreign key (declaration_type_id) references declaration_type(id);
alter table declaration_template add constraint declaration_template_fk_dtype foreign key (declaration_type_id) references declaration_type (id);
alter table declaration_template add constraint declaration_tem_fk_blob_data foreign key (XSD) references blob_data(id);
alter table declaration_template add constraint dec_tem_fk_blob_data_jrxml foreign key (jrxml) references blob_data(id);
alter table department_report_period add constraint dep_rep_per_fk_department_id foreign key (department_id) references department(id) on delete cascade;
alter table department_report_period add constraint dep_rep_per_fk_rep_period_id foreign key (report_period_id) references report_period(id) on delete cascade;
alter table declaration_data add constraint declaration_data_fk_decl_t_id foreign key (declaration_template_id) references declaration_template(id);
alter table declaration_data add constraint decl_data_fk_dep_rep_per_id foreign key (department_report_period_id) references department_report_period (id);
alter table form_data add constraint form_data_fk_form_templ_id foreign key (form_template_id) references form_template(id);
alter table form_data add constraint form_data_fk_dep_rep_per_id foreign key (department_report_period_id) references department_report_period (id);
alter table form_data add constraint form_data_fk_co_dep_rep_per_id foreign key (comparative_dep_rep_per_id) references department_report_period (id);
alter table form_data add constraint form_data_fk_kind foreign key (kind) references form_kind(id);
alter table form_data_file add constraint form_data_file_fk_form_data foreign key (form_data_id) references form_data(id) on delete cascade;
alter table form_data_file add constraint form_data_file_fk_blob_data foreign key (blob_data_id) references blob_data(id);
alter table form_data_ref_book add constraint form_data_ref_book_fk_formdata foreign key (form_data_id) references form_data(id) on delete cascade;
alter table form_data_ref_book add constraint form_data_ref_book_fk_refbook foreign key (ref_book_id) references ref_book(id) on delete cascade;
alter table form_data_signer add constraint form_data_signer_fk_formdata foreign key (form_data_id) references form_data(id) on delete cascade;
alter table form_data_performer add constraint formdata_performer_fk_formdata foreign key (form_data_id) references form_data(id) on delete cascade;
alter table form_data_performer add constraint formdata_performer_fk_dept foreign key (print_department_id) references department(id);
alter table department_form_type add constraint dept_form_type_fk_dep_id foreign key (department_id) references department(id);
alter table department_form_type add constraint dept_form_type_fk_type_id foreign key (form_type_id) references form_type(id);
alter table department_form_type add constraint dept_form_type_fk_kind foreign key (kind) references form_kind(id);
alter table department_form_type_performer add constraint dept_form_type_perf_fk_perf foreign key (performer_dep_id) references department (id);
alter table department_form_type_performer add constraint dept_form_type_perf_fk_id foreign key (department_form_type_id) references department_form_type (id) on delete cascade; 
alter table declaration_source add constraint decl_source_fk_dept_decltype foreign key (department_declaration_type_id) references department_declaration_type(id) on delete cascade;
alter table declaration_source add constraint decl_source_fk_dept_formtype foreign key (src_department_form_type_id) references department_form_type (id) on delete cascade;
alter table form_data_source add constraint form_data_source_fk_dep_id foreign key (department_form_type_id) references department_form_type(id) on delete cascade;
alter table form_data_source add constraint form_data_source_fk_src_dep_id foreign key (src_department_form_type_id) references department_form_type(id) on delete cascade;
alter table sec_user_role add constraint sec_user_role_fk_user_id foreign key (user_id) references sec_user(id);
alter table sec_user_role add constraint sec_user_role_fk_role_id foreign key (role_id) references sec_role(id);
alter table sec_user add constraint sec_user_fk_dep_id foreign key (department_id) references department(id);
alter table log_business add constraint log_business_fk_declaration_id foreign key (declaration_data_id) references declaration_data(id) on delete cascade;
alter table log_business add constraint log_business_fk_form_data_id foreign key (form_data_id) references form_data(id) on delete cascade;
alter table task_context add constraint task_context_fk_user_id foreign key (user_id) references sec_user(id);
alter table notification add constraint notification_fk_report_period foreign key (report_period_id) references report_period (id) on delete cascade;
alter table notification add constraint notification_fk_sender foreign key (sender_department_id) references department (id) on delete cascade;
alter table notification add constraint notification_fk_receiver foreign key (receiver_department_id) references department (id) on delete cascade;
alter table notification add constraint notification_fk_notify_user foreign key (user_id) references sec_user(id);
alter table notification add constraint notification_fk_notify_role foreign key (role_id) references sec_role(id);
alter table notification add constraint notification_fk_report_id foreign key (report_id) references blob_data (id) on delete set null;
alter table log_business add constraint log_business_fk_event_id foreign key (event_id) references event(id);
alter table role_event add constraint role_event_fk_event_id foreign key (event_id) references event(id);
alter table role_event add constraint role_event_fk_role_id foreign key (role_id) references sec_role(id);
alter table template_changes add constraint template_changes_fk_user_id foreign key (author) references sec_user(id);
alter table template_changes add constraint template_changes_fk_event foreign key (event) references event(id);
alter table template_changes add constraint template_changes_fk_dec_t foreign key (declaration_template_id) references declaration_template(id) on delete cascade;
alter table log_system add constraint log_system_fk_event_id foreign key (event_id) references event(id);
alter table log_system add constraint log_system_fk_kind foreign key (form_kind_id) references form_kind(id);
alter table log_system add constraint log_system_fk_user_login foreign key (user_login) references sec_user(login);
alter table log_system add constraint log_system_fk_audit_form_type foreign key (audit_form_type_id) references audit_form_type (id);
alter table lock_data add constraint lock_data_fk_user_id foreign key (user_id) references sec_user(id) on delete cascade;
alter table lock_data_subscribers add constraint lock_data_subscr_fk_lock_data foreign key (lock_key) references lock_data(key) on delete cascade;
alter table lock_data_subscribers add constraint lock_data_subscr_fk_sec_user foreign key (user_id) references sec_user(id) on delete cascade;
alter table form_data_report add constraint form_data_rep_fk_form_data_id foreign key (form_data_id) references form_data(id) on delete cascade;
alter table form_data_report add constraint form_data_rep_fk_blob_data_id foreign key (blob_data_id) references blob_data(id);
alter table declaration_subreport add constraint decl_subrep_fk_decl_template foreign key (declaration_template_id) references declaration_template(id) on delete cascade;
alter table declaration_subreport add constraint decl_subrep_fk_blob_data foreign key (blob_data_id) references blob_data(id);
alter table declaration_report add constraint decl_report_fk_decl_data foreign key(declaration_data_id) references declaration_data(id) on delete cascade;
alter table declaration_report add constraint decl_report_fk_blob_data foreign key(blob_data_id) references blob_data(id) on delete cascade;
alter table declaration_report add constraint decl_report_fk_decl_subreport foreign key (subreport_id) references declaration_subreport(id) on delete cascade; 
alter table ifrs_data add constraint ifrs_data_fk_report_period foreign key (report_period_id) references report_period(id);
alter table ifrs_data add constraint ifrs_data_fk_blob_data foreign key (blob_data_id) references blob_data(id);
alter table form_data_consolidation add constraint form_data_consolidation_fk_src foreign key (source_form_data_id) references form_data(id);
alter table form_data_consolidation add constraint form_data_consolidation_fk_tgt foreign key (target_form_data_id) references form_data(id) on delete cascade;
alter table declaration_data_consolidation add constraint decl_data_consolidation_fk_tgt foreign key (target_declaration_data_id) references declaration_data(id)  on delete cascade;
alter table declaration_data_consolidation add constraint decl_data_consolidation_fk_src foreign key (source_declaration_data_id) references declaration_data(id) on delete cascade;
alter table log_system_report add constraint log_system_report_fk_blob_data foreign key (blob_data_id) references blob_data (id) on delete cascade;
alter table log_system_report add constraint log_system_report_fk_sec_user foreign key (sec_user_id) references sec_user (id) on delete cascade;
alter table form_search_result add constraint form_search_result_fk_formdata foreign key (form_data_id) references form_data(id) on delete cascade;
alter table declaration_data_file add constraint decl_data_file_fk_decl_data foreign key (declaration_data_id) references declaration_data(id) on delete cascade;
alter table declaration_data_file add constraint decl_data_file_fk_blob_data foreign key (blob_data_id) references blob_data(id);
alter table state_change add constraint fk_state_change_from foreign key(from_id) references state(id);
alter table state_change add constraint fk_state_change_to foreign key(to_id) references state(id);
alter table declaration_data add constraint fk_declaration_data_state foreign key(state) references state(id);
alter table declaration_template_file add constraint fk_decl_templ_file_template foreign key (declaration_template_id) references declaration_template(id);
alter table declaration_template_file add constraint fk_decl_templ_file_blob foreign key (blob_data_id) references blob_data(id);
alter table department_decl_type_performer add constraint fk_dept_decl_type_perf_perf foreign key (performer_dep_id) references department (id);
alter table department_decl_type_performer add constraint fk_dept_decl_type_perf_id foreign key (department_decl_type_id) references department_declaration_type (id) on delete cascade; 

--checks
alter table form_type add constraint form_type_check_status check (status in (-1, 0, 1, 2));
alter table form_type add constraint form_type_chk_is_ifrs check ((is_ifrs in (0,1) and tax_type='I') or (is_ifrs = 0 and tax_type<>'I'));
alter table form_template add constraint form_template_chk_fixed_rows check(fixed_rows in (0, 1));
alter table form_template add constraint form_template_check_status check (status in (-1, 0, 1, 2));
alter table form_template add constraint form_template_chk_monthly check (monthly in (0, 1));
alter table form_template add constraint form_template_chk_comparative check (comparative in (0, 1));
alter table form_template add constraint form_template_chk_accruing check (accruing in (0, 1));
alter table form_template add constraint form_template_chk_updating check (updating in (0, 1));
alter table color add constraint color_chk_rgb_limits check ((r between 0 and 255) and (g between 0 and 255) and (b between 0 and 255));
alter table form_style add constraint form_style_chk_font_color check (font_color in (0,1,2,3,4,5,6,7,8,9,10,11,12,13));
alter table form_style add constraint form_style_chk_back_color check (back_color in (0,1,2,3,4,5,6,7,8,9,10,11,12,13));
alter table form_style add constraint form_style_chk_italic check (italic in (0,1));
alter table form_style add constraint form_style_chk_bold check (bold in (0,1));
alter table ref_book add constraint ref_book_chk_type check (type in (0, 1));
alter table ref_book add constraint ref_book_chk_read_only check (read_only in (0, 1));
alter table ref_book add constraint ref_book_chk_versioned check (is_versioned in (0, 1));
alter table ref_book_attribute add constraint ref_book_attr_chk_visible check (visible in (0, 1));
alter table ref_book_attribute add constraint ref_book_attr_chk_type check (type in (1, 2, 3, 4));
alter table ref_book_attribute add constraint ref_book_attr_chk_alias check ((not lower(alias) in ('row_number_over', 'record_version_from', 'record_version_to')) or (lower(alias)='record_id' and read_only=1));
alter table ref_book_attribute add constraint ref_book_attr_chk_precision check (precision >= 0 and precision <=19);
alter table ref_book_attribute add constraint ref_book_attr_chk_number_type check ((type <> 2 and precision is null) or (type = 2 and not (precision is null)));
alter table ref_book_attribute add constraint ref_book_attr_chk_ref check ((type <> 4 and reference_id is null) or (type = 4 and not (reference_id is null)));
alter table ref_book_attribute add constraint ref_book_attr_chk_ref_attr check ((type <> 4 and attribute_id is null) or (type = 4 and not (attribute_id is null)));
alter table ref_book_attribute add constraint ref_book_attribute_chk_format check (format in (0,1,2,3,4,5,6));
alter table ref_book_attribute add constraint ref_book_attr_chk_read_only check (read_only in (0, 1));
alter table ref_book_attribute add constraint ref_book_attr_chk_max_length check ((type=1 and max_length is not null and max_length between 1 and 2000) or (type=2 and max_length is not null and max_length between 1 and 38 and max_length - precision<=19) or (type in (3,4) and max_length is null));
alter table ref_book_record add constraint ref_book_record_chk_status check (status in (0, -1, 1 , 2));
alter table form_column add constraint form_column_chk_type check(type in ('N', 'S', 'D', 'R', 'A'));
alter table form_column add constraint form_column_chk_precision check((type = 'N' and precision is not null and precision >=0 and precision <= 19) or (type <> 'N' and precision is null));
alter table form_column add constraint form_column_chk_max_length check ((type = 'S' and max_length is not null and max_length > 0 and max_length <= 2000) or (type = 'N' and max_length is not null and max_length > 0 and max_length <= 38 and max_length - precision<=19) or ((type ='D' or type ='R' or type='A') and max_length is null));
alter table form_column add constraint form_column_chk_checking check (checking in (0, 1));
alter table form_column add constraint form_column_chk_attribute_id check ((type = 'R' and attribute_id is not null) or (type <> 'R' and attribute_id is null));
alter table form_column add constraint form_column_chk_width check (not width is null);
alter table form_column add constraint form_column_chk_filt_parent check ((type='R' and ((parent_column_id is null) and (filter is not null)) or ((parent_column_id is not null) and (filter is null)) or ((parent_column_id is null) and (filter is null))) or (type<>'R'));
alter table form_column add constraint form_column_chk_numrow check (numeration_row in (0, 1) or type <> 'A');
alter table department add constraint department_chk_is_active check (is_active in (0, 1));
alter table department add constraint department_chk_garant_use check (garant_use in (0, 1));
alter table department add constraint department_chk_sunr_use check (sunr_use in (0, 1));
alter table report_period add constraint report_period_chk_date check (end_date >= start_date);
alter table declaration_type add constraint declaration_type_chk_status check (status in (-1, 0, 1, 2));
alter table declaration_type add constraint declaration_type_chk_is_ifrs check ((is_ifrs in (0,1) and tax_type='I') or (is_ifrs = 0 and tax_type<>'I'));
alter table declaration_template add constraint dec_template_check_status check (status in (-1, 0, 1, 2));
alter table declaration_template add constraint chk_declaration_template_fkind check ((status in (0,1) and form_kind is not null) or status not in (0,1));
alter table declaration_template add constraint chk_declaration_template_ftype check ((status in (0,1) and form_type is not null) or status not in (0,1));
alter table department_report_period add constraint dep_rep_per_chk_is_active check (is_active in (0, 1));
alter table department_report_period add constraint dep_rep_per_chk_is_balance_per check (is_balance_period in (0, 1));
alter table form_data add constraint form_data_chk_state check(state in (1,2,3,4));
alter table form_data add constraint form_data_chk_return_sign check(return_sign in (0,1));
alter table form_data add constraint form_data_chk_period_order check(period_order in (1,2,3,4,5,6,7,8,9,10,11,12));
alter table form_data add constraint form_data_chk_manual check (manual in (0, 1));
alter table form_data add constraint form_data_chk_sorted check (sorted in (0, 1));
alter table form_data add constraint form_data_chk_accruing check (accruing in (0, 1));
alter table form_data add constraint form_data_chk_edited check (edited in (0, 1));
alter table form_data add constraint form_data_chk_sorted_backup check (sorted_backup in (0, 1));
alter table sec_user add constraint sec_user_chk_is_active check (is_active in (0, 1));
alter table log_business add constraint log_business_chk_event_id check (event_id in (1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 26, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 203, 204, 205, 206, 207, 208, 209, 210, 301, 302, 303, 401));
alter table log_business add constraint log_business_chk_frm_dcl_ev check (form_data_id is not null or declaration_data_id is not null);
alter table notification add constraint notification_chk_isread check (is_read in (0, 1));
alter table notification add constraint notification_chk_type check (type in (0, 1) and ((type = 0 and report_id is null) or type = 1));
alter table template_changes add constraint template_changes_chk_event check (event in (701, 702, 703, 704, 705, 904));
alter table template_changes add constraint template_changes_chk_template check ((form_template_id is not null and declaration_template_id is null and ref_book_id is null) or (form_template_id is null and declaration_template_id is not null and ref_book_id is null) or (form_template_id is null and declaration_template_id is null and ref_book_id is not null));
alter table log_system add constraint log_system_chk_dcl_form check (event_id in (7, 11, 401, 402, 501, 502, 503, 504,  601, 650, 901, 902, 903, 810, 811, 812, 813, 820, 821, 830, 831, 832, 840, 841, 842, 850, 860, 701, 702, 703, 704, 705, 904, 951, 960) or declaration_type_name is not null or (form_type_name is not null and form_kind_id is not null));
alter table log_system add constraint log_system_chk_rp check (event_id in (7, 11, 401, 402, 501, 502, 503, 504, 601, 650, 901, 902, 903, 810, 811, 812, 813, 820, 821, 830, 831, 832, 840, 841, 842, 850, 860, 701, 702, 703, 704, 705, 904, 951, 960) or report_period_name is not null);
alter table log_system add constraint log_system_chk_aft check (audit_form_type_id = 1 and not event_id in (701,702,703,704,705,904) and form_type_name is not null and department_name is not null or audit_form_type_id = 2 and not event_id in (701,702,703,704,705,904) and declaration_type_name is not null and department_name is not null or audit_form_type_id = 3 and event_id in (701,702,703,704,705,904) and form_type_name is not null and department_name is null or audit_form_type_id = 4 and event_id in (701,702,703,704,705,904) and declaration_type_name is not null and department_name is null or audit_form_type_id in (5,6) and event_id in (7) and form_type_name is null and declaration_type_name is null or audit_form_type_id is null or event_id in (402));
alter table log_system add constraint log_system_chk_is_error check (is_error in (0, 1));
alter table async_task_type add constraint async_task_type_chk_dev_mode check (dev_mode in (0, 1));
alter table form_data_report add constraint form_data_rep_chk_manual check (manual in (0,1));
alter table form_data_report add constraint form_data_rep_chk_checking check (checking in (0,1));
alter table form_data_report add constraint form_data_rep_chk_absolute check (absolute in (0,1));
alter table declaration_subreport add constraint chk_decl_subrep_sel_record check(select_record in (0,1));
alter table declaration_report add constraint decl_report_chk_type check (type in (0, 1, 2, 3, 4));
alter table declaration_report add constraint decl_report_chk_subreport_id check ((type = 4 and subreport_id is not null) or (type in (0, 1, 2, 3) and subreport_id is null));
alter table log_system_report add constraint log_system_report_chk_type check (type in (0, 1));
alter table department_change add constraint dep_change_chk_op_type check ((operationtype in (0,1) and hier_level is not null and name is not null and type is not null and is_active is not null and garant_use is not null and sunr_use is not null and code is not null) or (operationtype = 2 and hier_level is null and name is null and type is null and is_active is null and garant_use is null and sunr_use is null and code is null));
alter table department_change add constraint dep_change_chk_is_active check (is_active in (0, 1));
alter table department_change add constraint dep_change_chk_garant_use check (garant_use in (0, 1));
alter table department_change add constraint dep_change_chk_sunr_use check (sunr_use in (0, 1));
alter table declaration_subreport_params add constraint chk_decl_subrep_pars_type check (type in ('S','N','D','R'));
alter table declaration_subreport_params add constraint chk_decl_subrep_pars__attr_r  check ((type<>'R' and attribute_id is null) or (type='R' and attribute_id is not null));
alter table declaration_subreport_params add constraint chk_decl_subrep_pars_filter_r check ((type<>'R' and filter is null) or (type='R'));

--------------------------------------------------------------------------------------------------------------------------
-- Справочники физических лиц и статусов налогоплательщиков
--------------------------------------------------------------------------------------------------------------------------
--primary keys
alter table ref_book_taxpayer_state add constraint pk_ref_book_taxpayer_state primary key (id);
alter table ref_book_person add constraint pk_ref_book_person primary key (id);
alter table ref_book_id_doc add constraint pk_ref_book_id_doc primary key (id);
alter table ref_book_address add constraint pk_ref_book_address primary key (id);
alter table ref_book_id_tax_payer add constraint pk_ref_book_id_tax_payer primary key (id);

--foreign keys
alter table ref_book_person add constraint fk_ref_book_person_address foreign key (address) references ref_book_address(id);
alter table ref_book_person add constraint fk_ref_book_person_taxpayer_st foreign key (taxpayer_state) references ref_book_taxpayer_state(id);
alter table ref_book_id_doc add constraint fk_ref_book_id_doc_person foreign key (person_id) references ref_book_person(id);
alter table ref_book_id_tax_payer add constraint fk_ref_book_id_tax_payer_pers foreign key (person_id) references ref_book_person (id);

--checks
alter table ref_book_person add constraint chk_ref_book_person_status check (status between -1 and 2);
alter table ref_book_person add constraint chk_ref_book_person_old_st check (old_status is null or old_status between -1 and 2);
alter table ref_book_address add constraint chk_ref_book_address_status check (status in (-1, 0, 1, 2));
alter table ref_book_id_doc add constraint rb_id_doc_chk_status check (status in (-1, 0, 1, 2));
alter table ref_book_id_tax_payer add constraint rb_tax_payer_chk_status check (status in (-1, 0, 1, 2));

------------------------------------------------------------------------------------------------------
--      Расчет по страховым взносам 1151111
------------------------------------------------------------------------------------------------------
--primary keys
alter table raschsv_kol_lic_tip add constraint pk_rasch_kol_lic_tip primary key (id);
alter table raschsv_sv_sum_1tip add constraint pk_rasch_sv_sum1_tip primary key (id);
alter table raschsv_svnp_podpisant add constraint pk_raschsv_svnp_podpisant primary key (id);
alter table raschsv_obyaz_plat_sv add constraint pk_raschsv_obyaz_plat_sv primary key (id);
alter table raschsv_upl_per add constraint pk_raschsv_upl_per primary key (id);
alter table raschsv_upl_prev_oss add constraint pk_raschsv_upl_prev_oss primary key (id);
alter table raschsv_sv_ops_oms add constraint pk_rasch_sv_ops_oms primary key (id);
alter table raschsv_sv_ops_oms_rasch add constraint pk_raschsv_sv_ops_oms_rasch primary key (id);
alter table raschsv_ops_oms_rasch_sum add constraint pk_raschsv_ops_oms_rasch_sum primary key (raschsv_ops_oms_rasch_sum_id, raschsv_sv_sum1_tip_id);
alter table raschsv_ops_oms_rasch_kol add constraint pk_raschsv_ops_oms_rasch_kol primary key (raschsv_ops_oms_rasch_kol_id, raschsv_kol_lic_tip_id);
alter table raschsv_oss_vnm add constraint pk_raschsv_oss_vnm primary key (id);
alter table raschsv_upl_sv_prev add constraint pk_raschsv_oss_vnm_upl primary key (id);
alter table raschsv_oss_vnm_kol add constraint pk_raschsv_oss_vnm_kol primary key (raschsv_oss_vnm_id, raschsv_kol_lic_tip_id);
alter table raschsv_oss_vnm_sum add constraint pk_raschsv_oss_vnm_sum primary key (raschsv_oss_vnm_id, raschsv_sv_sum1_tip_id);
alter table raschsv_rash_oss_zak add constraint pk_raschsv_rash_oss_zak primary key (id);
alter table raschsv_rash_oss_zak_rash add constraint pk_raschsv_rash_oss_zak_rash primary key (id);
alter table raschsv_vypl_fin_fb add constraint pk_raschsv_vypl_fin_fb primary key (id);
alter table raschsv_vypl_prichina add constraint pk_raschsv_vypl_prichina primary key (id);
alter table raschsv_rash_vypl add constraint pk_raschsv_rash_vypl primary key (id);
alter table raschsv_prav_tarif3_1_427 add constraint pk_raschsv_prav_tarif3_1_427 primary key (id);
alter table raschsv_prav_tarif5_1_427 add constraint pk_raschsv_prav_tarif5_1_427 primary key (id);
alter table raschsv_prav_tarif7_1_427 add constraint pk_raschsv_prav_tarif7_1_427 primary key (id);
alter table raschsv_sv_prim_tarif9_1_427 add constraint pk_raschsv_prim_tarif9_1_427 primary key (id);
alter table raschsv_vyplat_it_427 add constraint pk_raschsv_vyplat_it_427 primary key (raschsv_sv_prim_tarif9_427_id, raschsv_sv_sum1_tip_id);
alter table raschsv_sved_patent add constraint pk_raschsv_sved_patent primary key (raschsv_sv_prim_tarif9_427_id, raschsv_sv_sum1_tip_id);
alter table raschsv_sv_prim_tarif2_2_425 add constraint pk_raschsv_sv_prim_tarif2_425 primary key (id);
alter table raschsv_vyplat_it_425 add constraint pk_raschsv_vyplat_it_425 primary key (raschsv_sv_prim_tarif2_425_id, raschsv_sv_sum1_tip_id);
alter table raschsv_sv_ino_grazd add constraint pk_raschsv_sv_ino_grazd primary key (raschsv_sv_prim_tarif2_425_id, raschsv_sv_sum1_tip_id);
alter table raschsv_sv_prim_tarif1_3_422 add constraint pk_raschsv_prim_tarif1_3_422 primary key (id);
alter table raschsv_sved_obuch add constraint pk_raschsv_sved_obuch primary key (id);
alter table raschsv_sv_reestr_mdo add constraint pk_raschsv_sv_reestr_mdo primary key (id);
alter table raschsv_vyplat_it_422 add constraint pk_raschsv_vyplat_it_422 primary key (raschsv_sv_prim_tarif1_422_id, raschsv_sv_sum1_tip_id);
alter table raschsv_pers_sv_strah_lic add constraint pk_pers_sv_strah_face primary key (id);
alter table raschsv_sv_vypl add constraint pk_raschsv_sv_vypl primary key (id);
alter table raschsv_sv_vypl_mk add constraint pk_raschv_vypl_mk primary key (id);
alter table raschsv_vypl_sv_dop add constraint pk_raschv_vypl_sv_dop primary key (id);
alter table raschsv_vypl_sv_dop_mt add constraint pk_raschv_vypl_sv_dop_mt primary key (id);
alter table raschsv_itog_strah_lic add constraint pk_raschsv_itog_strah_lic primary key(id);
alter table raschsv_itog_vypl add constraint pk_raschsv_itog_vypl primary key(id);
alter table raschsv_itog_vypl_dop add constraint pk_raschsv_itog_vypl_dop primary key(id);

--foreign keys
alter table raschsv_svnp_podpisant add constraint fk_raschsv_svnp_podp_declarat foreign key (declaration_data_id) references declaration_data (id) on delete cascade;
alter table raschsv_obyaz_plat_sv add constraint fk_raschsv_obyaz_plat_declarat foreign key (declaration_data_id) references declaration_data (id) on delete cascade;
alter table raschsv_upl_per add constraint fk_raschsv_upl_per_ob_plat_sv foreign key (raschsv_obyaz_plat_sv_id) references raschsv_obyaz_plat_sv (id) on delete cascade;
alter table raschsv_upl_prev_oss add constraint fk_raschsv_upl_prev_ob_plat foreign key (raschsv_obyaz_plat_sv_id) references raschsv_obyaz_plat_sv (id) on delete cascade;
alter table raschsv_sv_ops_oms add constraint fk_rasch_sv_ops_oms_ob_plat_sv foreign key (raschsv_obyaz_plat_sv_id) references raschsv_obyaz_plat_sv (id) on delete cascade;
alter table raschsv_sv_ops_oms_rasch add constraint fk_raschsv_sv_ops_oms_rasch foreign key (raschsv_sv_ops_oms_id) references raschsv_sv_ops_oms (id) on delete cascade;
alter table raschsv_ops_oms_rasch_sum add constraint fk_raschsv_ops_oms_r_sum_tip foreign key (raschsv_sv_sum1_tip_id) references raschsv_sv_sum_1tip (id) on delete cascade;
alter table raschsv_ops_oms_rasch_sum add constraint fk_raschsv_sv_ops_oms_sum foreign key (raschsv_ops_oms_rasch_sum_id) references raschsv_sv_ops_oms_rasch (id) on delete cascade;
alter table raschsv_ops_oms_rasch_kol add constraint fk_raschsv_sv_p_m_kol_tip foreign key (raschsv_kol_lic_tip_id) references raschsv_kol_lic_tip (id) on delete cascade;
alter table raschsv_ops_oms_rasch_kol add constraint fk_raschsv_sv_ops_oms_kol foreign key (raschsv_ops_oms_rasch_kol_id) references raschsv_sv_ops_oms_rasch (id) on delete cascade;
alter table raschsv_oss_vnm add constraint fk_raschsv_oss_vnm_ob_plat_sv foreign key (raschsv_obyaz_plat_sv_id) references raschsv_obyaz_plat_sv (id) on delete cascade;
alter table raschsv_upl_sv_prev add constraint fk_raschsv_upl_sv_prev_oss foreign key (raschsv_oss_vnm_id) references raschsv_oss_vnm (id) on delete cascade;
alter table raschsv_oss_vnm_kol add constraint fk_raschsv_oss_vnm_kol_oss foreign key (raschsv_oss_vnm_id) references raschsv_oss_vnm (id) on delete cascade;
alter table raschsv_oss_vnm_kol add constraint fk_raschsv_oss_vnm_kol_tip foreign key (raschsv_kol_lic_tip_id) references raschsv_kol_lic_tip (id) on delete cascade;
alter table raschsv_oss_vnm_sum add constraint fk_raschsv_oss_vnm_sum_oss foreign key (raschsv_oss_vnm_id) references raschsv_oss_vnm (id) on delete cascade;
alter table raschsv_oss_vnm_sum add constraint fk_raschsv_oss_vnm_sum_tip foreign key (raschsv_sv_sum1_tip_id) references raschsv_sv_sum_1tip (id) on delete cascade;
alter table raschsv_rash_oss_zak add constraint fk_raschsv_oss_zak_ob_plat_sv foreign key (raschsv_obyaz_plat_sv_id) references raschsv_obyaz_plat_sv (id) on delete cascade;
alter table raschsv_rash_oss_zak_rash add constraint fk_raschsv_rash_oss_zak_rash foreign key (raschsv_rash_oss_zak_id) references raschsv_rash_oss_zak (id) on delete cascade;
alter table raschsv_vypl_fin_fb add constraint fk_raschsv_vypl_fin_fb_ob_pl foreign key (raschsv_obyaz_plat_sv_id) references raschsv_obyaz_plat_sv (id) on delete cascade;
alter table raschsv_vypl_prichina add constraint fk_raschsv_vypl_fin_fb foreign key (raschsv_vypl_fin_fb_id) references raschsv_vypl_fin_fb (id) on delete cascade;
alter table raschsv_rash_vypl add constraint fk_raschsv_rash_vypl_prichina foreign key (raschsv_vypl_prichina_id) references raschsv_vypl_prichina (id) on delete cascade;
alter table raschsv_prav_tarif3_1_427 add constraint fk_raschsv_tarif3_427_ob_pl foreign key (raschsv_obyaz_plat_sv_id) references raschsv_obyaz_plat_sv (id) on delete cascade;
alter table raschsv_prav_tarif5_1_427 add constraint fk_raschsv_tarif5_427_ob_pl foreign key (raschsv_obyaz_plat_sv_id) references raschsv_obyaz_plat_sv (id) on delete cascade;
alter table raschsv_prav_tarif7_1_427 add constraint fk_raschsv_tarif7_427_ob_pl foreign key (raschsv_obyaz_plat_sv_id) references raschsv_obyaz_plat_sv (id) on delete cascade;
alter table raschsv_sv_prim_tarif9_1_427 add constraint fk_raschsv_tarif9_427 foreign key (raschsv_obyaz_plat_sv_id) references raschsv_obyaz_plat_sv (id) on delete cascade;
alter table raschsv_vyplat_it_427 add constraint fk_raschsv_vyplat_it_427_sum foreign key (raschsv_sv_sum1_tip_id) references raschsv_sv_sum_1tip (id) on delete cascade;
alter table raschsv_vyplat_it_427 add constraint fk_raschsv_vyplat_tarif9_427 foreign key (raschsv_sv_prim_tarif9_427_id) references raschsv_sv_prim_tarif9_1_427 (id) on delete cascade;
alter table raschsv_sved_patent add constraint fk_raschsv_sved_p_tarif9_427 foreign key (raschsv_sv_prim_tarif9_427_id) references raschsv_sv_prim_tarif9_1_427 (id) on delete cascade;
alter table raschsv_sved_patent add constraint fk_raschsv_sved_patent_sum foreign key (raschsv_sv_sum1_tip_id) references raschsv_sv_sum_1tip (id) on delete cascade;
alter table raschsv_sv_prim_tarif2_2_425 add constraint fk_raschsv_tarif2_425_ob_pl foreign key (raschsv_obyaz_plat_sv_id) references raschsv_obyaz_plat_sv (id) on delete cascade;
alter table raschsv_vyplat_it_425 add constraint fk_raschsv_vyplat_it_425_sum foreign key (raschsv_sv_sum1_tip_id) references raschsv_sv_sum_1tip (id) on delete cascade;
alter table raschsv_vyplat_it_425 add constraint fk_raschsv_vyplat_tarif2_425 foreign key (raschsv_sv_prim_tarif2_425_id) references raschsv_sv_prim_tarif2_2_425 (id) on delete cascade;
alter table raschsv_sv_ino_grazd add constraint fk_raschsv_i_grazd_tarif2_425 foreign key (raschsv_sv_prim_tarif2_425_id) references raschsv_sv_prim_tarif2_2_425 (id) on delete cascade;
alter table raschsv_sv_ino_grazd add constraint fk_raschsv_sv_ino_grazd_sum foreign key (raschsv_sv_sum1_tip_id) references raschsv_sv_sum_1tip (id) on delete cascade;
alter table raschsv_sv_prim_tarif1_3_422 add constraint fk_raschsv_tarif1_3_422_ob_pl foreign key (raschsv_obyaz_plat_sv_id) references raschsv_obyaz_plat_sv (id) on delete cascade;
alter table raschsv_sved_obuch add constraint fk_raschsv_sved_ob_tarif1_422 foreign key (raschsv_sv_prim_tarif1_422_id) references raschsv_sv_prim_tarif1_3_422 (id) on delete cascade;
alter table raschsv_sved_obuch add constraint fk_raschsv_sved_obuch_sum foreign key (raschsv_sv_sum1_tip_id) references raschsv_sv_sum_1tip (id) on delete cascade;
alter table raschsv_sv_reestr_mdo add constraint fk_raschsv_sv_reestr_mdo_ob foreign key (raschsv_sved_obuch_id) references raschsv_sved_obuch (id) on delete cascade;
alter table raschsv_vyplat_it_422 add constraint fk_raschsv_vyplat_it_422_sum foreign key (raschsv_sv_sum1_tip_id) references raschsv_sv_sum_1tip (id) on delete cascade;
alter table raschsv_vyplat_it_422 add constraint fk_raschsv_vyplat_tarif3_422 foreign key (raschsv_sv_prim_tarif1_422_id) references raschsv_sv_prim_tarif1_3_422 (id) on delete cascade;
alter table raschsv_pers_sv_strah_lic add constraint fk_pers_sv_strah_face_declarat foreign key (declaration_data_id) references declaration_data (id) on delete cascade;
alter table raschsv_sv_vypl add constraint fk_raschsv_sv_vypl_strah_lic foreign key (raschsv_pers_sv_strah_lic_id) references raschsv_pers_sv_strah_lic (id) on delete cascade;
alter table raschsv_sv_vypl_mk add constraint fk_raschsv_sv_vypl_mk_sv_vypl foreign key (raschsv_sv_vypl_id) references raschsv_sv_vypl (id) on delete cascade;
alter table raschsv_vypl_sv_dop add constraint fk_raschsv_vypl_sv_dop_lic foreign key (raschsv_pers_sv_strah_lic_id) references raschsv_pers_sv_strah_lic (id) on delete cascade;
alter table raschsv_vypl_sv_dop_mt add constraint fk_raschsv_vsv_dop_mt_vsv_dop foreign key (raschsv_vypl_sv_dop_id) references raschsv_vypl_sv_dop (id) on delete cascade;
alter table declaration_subreport_params add constraint fk_decl_subrep_pars_attrib_id foreign key (attribute_id) references ref_book_attribute (id);
alter table declaration_subreport_params add constraint fk_decl_subrep_pars_subrep_id foreign key (declaration_subreport_id) references declaration_subreport (id) on delete cascade;
alter table raschsv_pers_sv_strah_lic add constraint fk_rsv_p_sv_strah_lic_person foreign key(person_id) references ref_book_person(id);
alter table raschsv_itog_vypl add constraint fk_raschsv_itog_vypl_strah foreign key(raschsv_itog_strah_lic_id) references raschsv_itog_strah_lic(id);
alter table raschsv_itog_vypl_dop add constraint fk_raschsv_itog_vypl_dop_strah foreign key(raschsv_itog_strah_lic_id) references raschsv_itog_strah_lic(id);

------------------------------------------------------------------------------------------------------------------------------
-- ФИАС
------------------------------------------------------------------------------------------------------------------------------
-- primary keys
alter table fias_addrobj add constraint pk_fias_addrobj primary key (id);

-- foreign keys
alter table fias_addrobj add constraint fk_fias_addrobj_parentid foreign key (parentguid) references fias_addrobj (id) on delete cascade deferrable initially deferred;

-- checks
alter table fias_addrobj add constraint chk_fias_addrobj_centstatus check (centstatus between 0 and 4);
alter table fias_addrobj add constraint chk_fias_addrobj_currstatus check (currstatus between 0 and 99);
alter table fias_addrobj add constraint chk_fias_addrobj_divtype check (divtype in (0,1,2));
alter table fias_addrobj add constraint chk_fias_addrobj_livestatus check (livestatus in (0,1));

--------------------------------------------------------------------------------------------------------
--                                      ФП "НДФЛ"
--------------------------------------------------------------------------------------------------------
--primary keys
alter table ndfl_person add constraint ndfl_person_pk primary key (id);
alter table ndfl_person_income add constraint ndfl_person_i_pk primary key (id);
alter table ndfl_person_deduction add constraint ndfl_pd_pk primary key (id);
alter table ndfl_person_prepayment add constraint ndfl_pp_pk primary key (id);

--foreign keys
alter table ndfl_person add constraint ndfl_person_fk_d foreign key (declaration_data_id) references declaration_data(id) on delete cascade;
alter table ndfl_person add constraint ndfl_person_fk_person_id foreign key (person_id) references ref_book_person(id);
alter table ndfl_person_income add constraint ndfl_person_i_fk_np foreign key (ndfl_person_id) references ndfl_person(id) on delete cascade;
alter table ndfl_person_income add constraint ndfl_person_i_fk_s foreign key (source_id) references ndfl_person_income(id) on delete set null;
alter table ndfl_person_deduction add constraint ndfl_pd_fk_np foreign key (ndfl_person_id) references ndfl_person(id) on delete cascade;
alter table ndfl_person_deduction add constraint ndfl_pd_fk_s foreign key (source_id) references ndfl_person_deduction(id) on delete set null;
alter table ndfl_person_prepayment add constraint ndfl_pp_fk_np foreign key (ndfl_person_id) references ndfl_person(id) on delete cascade;
alter table ndfl_person_prepayment add constraint ndfl_pp_fk_s foreign key (source_id) references ndfl_person_prepayment(id) on delete set null;

--------------------------------------------------------------------------------------------------------------------------
-- НДФЛ Реестр справок
--------------------------------------------------------------------------------------------------------------------------
--primary keys
alter table ndfl_references add constraint pk_ndfl_references primary key(id);

--foreign keys
alter table ndfl_references add constraint fk_ndfl_refers_decl_data foreign key(declaration_data_id) references declaration_data(id) on delete cascade;
alter table ndfl_references add constraint fk_ndfl_refers_person foreign key(person_id) references ref_book_person(id);
alter table ndfl_references add constraint fk_ndfl_person_id foreign key (ndfl_person_id) references ndfl_person(id) on delete set null;

--------------------------------------------------------------------------------------------------------------------------
-- Журналирование действий пользователей
--------------------------------------------------------------------------------------------------------------------------
--primary keys
alter table log add constraint pk_log primary key(id);
alter table log_entry add constraint pk_log_entry primary key(log_id,ord);

--foreign keys
alter table log add constraint fk_log_user foreign key(user_id) references sec_user(id);
alter table log_entry add constraint fk_log_entry_log foreign key(log_id) references log(id) on delete cascade;
alter table notification add constraint fk_notification_log foreign key(log_id) references log(id);
alter table log_system add constraint fk_log_system_log foreign key(log_id) references log(id);

--checks
alter table log_entry add constraint chk_log_entry_lev check(log_level in (0,1,2));
