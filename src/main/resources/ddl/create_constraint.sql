alter table configuration add constraint configuration_pk primary key (code);

alter table dict_region add constraint dict_region_pk primary key (code);
alter table dict_region add constraint dict_region_uniq_okato_def unique (okato_definition);

alter table form_type add constraint form_type_pk primary key (id);
alter table form_type add constraint form_type_chk_taxtype check (tax_type in ('I', 'P', 'T', 'V', 'D'));

alter table tax_period add constraint tax_period_pk primary key (id);
alter table tax_period add constraint tax_period_chk_taxtype check (tax_type in ('I', 'P', 'T', 'V', 'D'));

alter table form_template add constraint form_template_pk primary key (id);
alter table form_template add constraint form_template_fk_type_id foreign key (type_id) references form_type(id);
alter table form_template add constraint form_template_uniq_version unique(type_id, version);
alter table form_template add constraint form_template_check_active check (is_active in (0, 1));
alter table form_template add constraint form_template_chk_num_cols check (numbered_columns in (0, 1));
alter table form_template add constraint form_template_chk_fixed_rows check(fixed_rows in (0, 1));

alter table form_style add constraint form_style_pk primary key (id);
alter table form_style add constraint form_style_fk_form_template_id foreign key (form_template_id) references form_template (id);
alter table form_style add constraint form_style_chk_font_color check (font_color in (0,1,2,3,4,5,6,7,8,9,10,11,12,13));
alter table form_style add constraint form_style_chk_back_color check (back_color in (0,1,2,3,4,5,6,7,8,9,10,11,12));
alter table form_style add constraint form_style_chk_italic check (italic in (0,1));
alter table form_style add constraint form_style_chk_bold check (bold in (0,1));
alter table form_style add constraint form_style_uniq_alias unique (form_template_id, alias);

alter table blob_data add constraint blob_data_pk primary key(id);
alter table blob_data add constraint blob_data_chk_type check (type in (0, 1));

alter table ref_book add constraint ref_book_pk primary key (id);
alter table ref_book add constraint ref_book_fk_script_id foreign key (script_id) references blob_data(id);

alter table ref_book_attribute add constraint ref_book_attr_pk primary key (id);
alter table ref_book_attribute add constraint ref_book_attr_chk_visible check (visible in (0, 1));
alter table ref_book_attribute add constraint ref_book_attr_chk_type check (type in (1, 2, 3, 4));
alter table ref_book_attribute add constraint ref_book_attr_chk_alias check (lower(alias) <> 'record_id' and lower(alias) <> 'row_number_over');
alter table ref_book_attribute add constraint ref_book_attr_chk_precision check (precision >= 0 and precision <=10);
alter table ref_book_attribute add constraint ref_book_attr_chk_number_type check ((type <> 2 and precision is null) or (type = 2 and not (precision is null)));
alter table ref_book_attribute add constraint ref_book_attr_chk_ref check ((type <> 4 and reference_id is null) or (type = 4 and not (reference_id is null)));
alter table ref_book_attribute add constraint ref_book_attr_chk_ref_attr check ((type <> 4 and attribute_id is null) or (type = 4 and not (attribute_id is null)));
alter table ref_book_attribute add constraint ref_book_attribute_uniq_ord unique (ref_book_id, ord);
alter table ref_book_attribute add constraint ref_book_attribute_uniq_alias unique (ref_book_id, alias);
alter table ref_book_attribute add constraint ref_book_attr_fk_ref_book_id foreign key (ref_book_id) references ref_book (id);
alter table ref_book_attribute add constraint ref_book_attr_fk_reference_id foreign key (reference_id) references ref_book (id);
alter table ref_book_attribute add constraint ref_book_attr_fk_attribute_id foreign key (attribute_id) references ref_book_attribute (id);

alter table ref_book_record add constraint ref_book_record_pk primary key (id);
alter table ref_book_record add constraint ref_book_record_chk_status check (status in (0, -1));
alter table ref_book_record add constraint ref_book_record_fk_ref_book_id foreign key (ref_book_id) references ref_book (id);
create unique index i_ref_book_record_refbookid on ref_book_record(ref_book_id, record_id, version);

alter table ref_book_value add constraint ref_book_value_pk primary key (record_id, attribute_id);
alter table ref_book_value add constraint ref_book_value_fk_record_id foreign key (record_id) references ref_book_record (id) on delete cascade;
alter table ref_book_value add constraint ref_book_value_fk_attribute_id foreign key (attribute_id) references ref_book_attribute (id);

alter table form_column add constraint form_column_pk primary key (id);
alter table form_column add constraint form_column_fk_form_templ_id foreign key (form_template_id) references form_template(id);
alter table form_column add constraint form_column_uniq_alias unique(form_template_id, alias);
alter table form_column add constraint form_column_chk_type check(type in ('N', 'S', 'D', 'R'));
alter table form_column add constraint form_column_chk_precision check((type = 'N' and precision is not null and precision >=0 and precision < 9) or (type <> 'N' and precision is null));
alter table form_column add constraint form_column_chk_max_length check ((type = 'S' and max_length is not null and max_length > 0 and max_length <= 1000) or (type = 'N' and max_length is not null and max_length > 0 and max_length <= 27) or ((type ='D' or type ='R') and max_length is null));
alter table form_column add constraint form_column_chk_checking check (checking in (0, 1));
alter table form_column add constraint form_column_chk_attribute_id check ((type = 'R' and attribute_id is not null and precision >=0 and precision < 9) or (type <> 'R' and attribute_id is null));
alter table form_column add constraint form_column_chk_width check (not width is null);
alter table form_column add constraint form_column_fk_attribute_id foreign key (attribute_id) references ref_book_attribute (id);

alter table department add constraint department_pk primary key (id);
alter table department add constraint dept_fk_parent_id foreign key (parent_id) references department(id);
alter table department add constraint department_chk_id check ((type= 1 and id = 1) or (type <> 1 and id <> 1));
alter table department add constraint department_chk_parent_id check ((type = 1 and parent_id is null) or (type <> 1 and parent_id is not null));

alter table report_period add constraint report_period_pk primary key(id);
alter table report_period add constraint report_period_fk_taxperiod foreign key (tax_period_id) references tax_period (id);
alter table report_period add constraint report_period_fk_dtp_id foreign key (dict_tax_period_id) references ref_book_record(id);
alter table report_period add constraint report_period_uniq_tax_dict unique (tax_period_id, dict_tax_period_id);

alter table income_101 add constraint income_101_pk primary key (id);
alter table income_101 add constraint income_101_fk_report_period_id foreign key (report_period_id) references report_period(id);
alter table income_101 add constraint income_101_fk_department foreign key (department_id) references department(id);

alter table income_102 add constraint income_102_pk primary key (id);
alter table income_102 add constraint income_102_fk_report_period_id foreign key (report_period_id) references report_period(id);
alter table income_102 add constraint income_102_fk_department foreign key (department_id) references department(id);

alter table declaration_type add constraint declaration_type_pk primary key (id);
alter table declaration_type add constraint declaration_type_chk_tax_type check (tax_type in ('I', 'P', 'T', 'V', 'D'));

alter table department_declaration_type add constraint dept_decl_type_pk primary key (id);
alter table department_declaration_type add constraint dept_decl_type_fk_dept foreign key (department_id) references department (id);
alter table department_declaration_type add constraint dept_decl_type_fk_decl_type foreign key (declaration_type_id) references declaration_type (id);
alter table department_declaration_type add constraint dept_decl_type_uniq_decl unique (department_id, declaration_type_id);

alter table declaration_template add constraint declaration_template_pk primary key (id);
alter table declaration_template add constraint declaration_t_chk_is_active check (is_active in (0,1));
alter table declaration_template add constraint declaration_template_fk_dtype foreign key (declaration_type_id) references declaration_type (id);
alter table declaration_template add constraint declaration_tem_fk_blob_data foreign key (XSD) references blob_data (id);

alter table declaration_data add constraint declaration_data_pk primary key (id);
alter table declaration_data add constraint declaration_data_fk_decl_t_id foreign key (declaration_template_id) references declaration_template (id);
alter table declaration_data add constraint declaration_data_fk_rep_per_id foreign key (report_period_id) references report_period (id);
alter table declaration_data add constraint declaration_data_fk_dep_id foreign key (department_id) references department (id);
alter table declaration_data add constraint declaration_data_fk_j_print foreign key (jasper_print) references blob_data (id);
alter table declaration_data add constraint declaration_data_chk_is_accptd check (is_accepted in (0,1));
alter table declaration_data add constraint declaration_data_uniq_template unique(report_period_id, department_id, declaration_template_id);

alter table form_data add constraint form_data_pk primary key (id);
alter table form_data add constraint form_data_fk_form_templ_id foreign key (form_template_id) references form_template(id);
alter table form_data add constraint form_data_fk_dep_id foreign key (department_id) references department(id);
alter table form_data add constraint form_data_fk_period_id foreign key (report_period_id) references report_period(id);
alter table form_data add constraint form_data_chk_kind check(kind in (1,2,3,4,5));
alter table form_data add constraint form_data_chk_state check(state in (1,2,3,4));
alter table form_data add constraint form_data_chk_return_sign check(return_sign in (0,1));
alter table form_data add constraint form_data_chk_period_order check(period_order in (1,2,3,4,5,6,7,8,9,10,11,12));

alter table form_data_signer add constraint form_data_signer_pk primary key (id);
alter table form_data_signer add constraint form_data_signer_fk_formdata foreign key (form_data_id) references form_data (id) on delete cascade;

alter table form_data_performer add constraint form_data_performer_pk primary key (form_data_id);
alter table form_data_performer add constraint formdata_performer_fk_formdata foreign key (form_data_id) references form_data (id) on delete cascade;

alter table data_row add constraint data_row_pk primary key (id);
alter table data_row add constraint data_row_fk_form_data_id foreign key (form_data_id) references form_data(id) on delete cascade;
alter table data_row add constraint data_row_uniq_form_data_order unique(form_data_id, ord, type);
alter table data_row add constraint data_row_chk_type check (type in (-1, 0, 1));

alter table cell_style add constraint cell_style_pk primary key (row_id, column_id);
alter table cell_style add constraint cell_style_fk_column_id foreign key (column_id) references form_column (id);
alter table cell_style add constraint cell_style_fk_data_row foreign key (row_id) references data_row (id) on delete cascade;
alter table cell_style add constraint cell_style_fk_style_id foreign key (style_id) references form_style (id);

alter table cell_editable add constraint cell_editable_pk primary key (row_id, column_id);
alter table cell_editable add constraint cell_editable_fk_data_row foreign key (row_id) references data_row (id) on delete cascade;
alter table cell_editable add constraint cell_editable_fk_form_column foreign key (column_id) references form_column (id);

alter table numeric_value add constraint numeric_value_pk primary key (row_id, column_id);
alter table numeric_value add constraint numeric_value_fk_column_id foreign key (column_id) references form_column(id);
alter table numeric_value add constraint numeric_value_fk_row_id foreign key (row_id) references data_row(id) on delete cascade;

alter table string_value add constraint string_value_pk primary key (row_id, column_id);
alter table string_value add constraint string_value_fk_column_id foreign key (column_id) references form_column(id);
alter table string_value add constraint string_value_fk_row_id foreign key (row_id) references data_row(id) on delete cascade;

alter table date_value add constraint date_value_pk primary key (row_id, column_id);
alter table date_value add constraint date_value_fk_column_id foreign key (column_id) references form_column(id);
alter table date_value add constraint date_value_fk_row_id foreign key (row_id) references data_row(id) on delete cascade;

alter table department_form_type add constraint dept_form_type_fk_dep_id foreign key (department_id) references department(id);
alter table department_form_type add constraint dept_form_type_fk_type_id foreign key (form_type_id) references form_type(id);
alter table department_form_type add constraint dept_form_type_pk primary key (id);
alter table department_form_type add constraint dept_form_type_uniq_form unique (department_id, form_type_id, kind);
alter table department_form_type add constraint dept_form_type_chk_kind check (kind in (1,2,3,4,5));

alter table declaration_source add constraint declaration_source_pk primary key (department_declaration_type_id,src_department_form_type_id );
alter table declaration_source add constraint decl_source_fk_dept_decltype foreign key (department_declaration_type_id) references department_declaration_type (id);
alter table declaration_source add constraint decl_source_fk_dept_formtype foreign key (src_department_form_type_id) references department_form_type (id);

alter table form_data_source add constraint form_data_source_pk primary key (department_form_type_id, src_department_form_type_id);
alter table form_data_source add constraint form_data_source_fk_dep_id foreign key (department_form_type_id) references department_form_type(id);
alter table form_data_source add constraint form_data_source_fk_src_dep_id foreign key (src_department_form_type_id) references department_form_type(id);

alter table sec_user add constraint sec_user_pk primary key (id);
alter table sec_user add constraint sec_user_fk_dep_id foreign key (department_id) references department(id);
alter table sec_user add constraint sec_user_uniq_login_active unique (login);

alter table object_lock add constraint object_lock_pk primary key (object_id, class);
alter table object_lock add constraint object_lock_fk_user_id foreign key (user_id) references sec_user (id) on delete cascade;

alter table sec_role add constraint sec_role_pk primary key (id);
alter table sec_role add constraint sec_role_uniq_alias unique (alias);

alter table sec_user_role add constraint sec_user_role_pk primary key (user_id, role_id);
alter table sec_user_role add constraint sec_user_role_fk_user_id foreign key (user_id) references sec_user(id);
alter table sec_user_role add constraint sec_user_role_fk_role_id foreign key (role_id) references sec_role(id);

alter table cell_span_info add constraint cell_span_pk primary key (row_id, column_id);
alter table cell_span_info add constraint cell_span_info_fk_row_id foreign key (row_id) references data_row (id) on delete cascade;
alter table cell_span_info add constraint cell_span_info_fk_column_id foreign key (column_id) references form_column (id);
alter table cell_span_info add constraint cell_span_info_chk_span check (colspan is not null or rowspan is not null);

alter table log_business add constraint log_business_fk_user_id foreign key (user_id) references sec_user (id);
alter table log_business add constraint log_business_fk_declaration_id foreign key (declaration_data_id) references declaration_data(id) on delete cascade;
alter table log_business add constraint log_business_fk_form_data_id foreign key (form_data_id) references form_data (id) on delete cascade;
alter table log_business add constraint log_business_chk_event_id check (event_id in (1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 203, 204, 205, 206, 207, 208, 209, 210, 301, 302, 303, 401));
alter table log_business add constraint log_business_chk_frm_dcl_ev check (form_data_id is not null or declaration_data_id is not null);
alter table log_business add constraint log_business_fk_usr_departm_id foreign key (user_department_id) references department (id);

alter table log_system add constraint log_system_chk_form_kind_id check (form_kind_id in (1, 2, 3, 4, 5));
alter table log_system add constraint log_system_chk_event_id check (event_id in (1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 203, 204, 205, 206, 207, 208, 209, 210, 301, 302, 303, 401, 501, 502, 601));
alter table log_system add constraint log_system_chk_dcl_form check (event_id in (7, 501, 502, 601) or declaration_type_id is not null or (form_type_id is not null and form_kind_id is not null));
alter table log_system add constraint log_system_chk_rp check (event_id in (501, 502, 7, 601) or report_period_id is not null);
alter table log_system add constraint log_system_fk_user_id foreign key (user_id) references sec_user (id);
alter table log_system add constraint log_system_fk_department_id foreign key (department_id) references department(id);
alter table log_system add constraint log_system_fk_report_period_id foreign key (report_period_id) references report_period(id);
alter table log_system add constraint log_system_fk_decl_type_id foreign key (declaration_type_id) references declaration_type (id);
alter table log_system add constraint log_system_fk_form_type_id foreign key (form_type_id) references form_type(id);
alter table log_system add constraint log_system_fk_user_dep_id foreign key (user_department_id) references department (id);

alter table department_report_period add constraint department_report_period_pk primary key (department_id, report_period_id);
alter table department_report_period add constraint dep_rep_per_chk_is_active check (is_active in (0, 1));
alter table department_report_period add constraint dep_rep_per_chk_is_balance_per check (is_balance_period in (0, 1));
alter table department_report_period add constraint dep_rep_per_fk_department_id foreign key (department_id) references department(id) on delete cascade;
alter table department_report_period add constraint dep_rep_per_fk_rep_period_id foreign key (report_period_id) references report_period(id) on delete cascade;

alter table task_context add constraint task_context_uniq_task_id unique (task_id);
alter table task_context add constraint task_context_uniq_task_name unique (task_name);

alter table user_session add constraint user_session_uniq_session_id unique (session_id);
alter table user_session add constraint user_session_uniq_user_login unique (user_login);
------------------------------------------------------------------------------------------------------
create index i_department_parent_id on department(parent_id);
create index i_data_row_form_data_id on data_row(form_data_id);
create index i_form_data_report_period_id on form_data(report_period_id);
create index i_form_data_form_template_id on form_data(form_template_id);
create index i_form_data_department_id on form_data(department_id);
create index i_form_data_kind on form_data(kind);
create index i_form_data_signer_formdataid on form_data_signer(form_data_id);
create index i_ref_book_value_string on ref_book_value(string_value);