drop index i_ref_book_oktmo_code;
drop index i_ref_book_oktmo_record_id;
drop index i_ref_book_value_string;
drop index i_form_data_signer_formdataid;
drop index i_form_data_kind;
drop index i_form_data_department_id;
drop index i_form_data_form_template_id;
drop index i_form_data_report_period_id;
drop index i_data_row_form_data_id;
drop index i_department_parent_id;
drop sequence seq_department_report_period;
drop sequence seq_task_context;
drop sequence seq_log_system;
drop sequence seq_log_business;
drop table report;
drop table template_changes;
drop sequence seq_template_changes;
drop table task_context;
drop table department_report_period;
drop table log_system;
drop table log_business;
drop table cell_span_info;
drop table sec_user_role;
drop table notification;
drop table sec_role;
drop sequence seq_sec_user;
drop sequence seq_notification;
drop table lock_data_subscribers;
drop table lock_data;
drop table sec_user;
drop table form_data_source;
drop table declaration_source;
drop sequence seq_department_form_type;
drop table department_form_type;
drop table date_value;
drop table string_value;
drop table numeric_value;
drop table cell_editable;
drop table cell_style;
drop table data_cell;
drop sequence seq_data_row;
drop table data_row;
drop table form_data_performer;
drop sequence seq_form_data_signer;
drop table form_data_signer;
drop sequence seq_form_data;
drop table form_data;
drop sequence seq_declaration_data;
drop table declaration_data;
drop sequence seq_declaration_template;
drop table declaration_template;
drop sequence seq_dept_declaration_type;
drop table department_declaration_type;
drop table declaration_type;
drop sequence seq_declaration_type;
drop sequence seq_income_102;
drop table income_102;
drop sequence seq_income_101;
drop table income_101;
drop sequence seq_report_period;
drop table report_period;
drop sequence seq_department;
drop sequence seq_form_column;
drop table form_column;
drop table ref_book_value;
drop sequence seq_ref_book_record_row_id;
drop sequence seq_ref_book_record;
drop index i_ref_book_record_refbookid;
drop table ref_book_record;
alter table ref_book drop constraint ref_book_fk_region;
drop table ref_book_attribute;
drop table ref_book;
drop table blob_data;
drop sequence seq_form_style;
drop table form_style;
drop sequence seq_form_template;
drop table form_template;
drop sequence seq_tax_period;
drop table tax_period;
drop table form_type;
drop sequence seq_form_type;
drop table configuration;
drop table ref_book_oktmo;
drop sequence seq_ref_book_oktmo;
drop table form_kind;
drop table event;
drop table department;
drop table department_type;
drop table async_task_type;