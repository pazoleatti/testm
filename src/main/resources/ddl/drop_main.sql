drop index i_ref_book_oktmo_code;
drop index i_ref_book_oktmo_record_id;
drop index i_ref_book_value_string;
drop index i_form_data_signer_formdataid;
drop index i_form_data_kind;
drop index i_form_data_form_template_id;
drop index i_form_data_dep_rep_per_id;
drop index i_department_parent_id;
drop index i_decl_data_dep_rep_per_id;
drop sequence seq_department_report_period;
drop sequence seq_task_context;
drop sequence seq_log_business;
drop table form_data_report;
drop table declaration_report;
drop table declaration_subreport;
drop table template_changes;
drop sequence seq_template_changes;
drop table log_business;
drop table sec_user_role;
drop table notification;
drop table sec_role;
drop sequence seq_sec_user;
drop sequence seq_notification;
drop table async_task_subscribers;
drop table lock_data;
drop table sec_user;
drop table form_data_source;
drop table declaration_source;
drop sequence seq_department_form_type;
drop table department_form_type_performer;
drop table form_data_performer;
drop sequence seq_form_data_signer;
drop table form_data_signer;
drop sequence seq_form_data;
drop table form_data_consolidation;
drop table declaration_data_consolidation;
drop table form_data_ref_book;

drop sequence seq_form_search_result;
drop table form_search_data_result;
drop table form_search_result;
drop table form_search_data_result_tmp;

begin
 for x in (select * from user_tables where regexp_like (table_name, '^FORM_DATA_[0-9]+$')) loop
     execute immediate 'DROP TABLE '||x.table_name;
 end loop;
end;
/
drop table form_data_file;
drop table ref_book_vzl_history;
drop table form_data;
drop sequence seq_declaration_data;
drop table declaration_data;
drop table department_report_period;
drop sequence seq_declaration_template;
drop table declaration_template;
drop sequence seq_dept_declaration_type;
drop table department_declaration_type;
drop table declaration_type;
drop sequence seq_declaration_type;
drop sequence seq_report_period;
drop table ifrs_data;
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
drop table color;
drop sequence seq_form_template;
drop table form_template;
drop sequence seq_tax_period;
drop table tax_period;
drop table form_type;
drop sequence seq_form_type;
drop table configuration;
drop table ref_book_oktmo;
drop sequence seq_ref_book_oktmo;
drop sequence seq_ref_book_oktmo_record_id;
drop table form_kind;
drop table event;
drop table department_change;
drop table department;
drop table department_type;
drop table async_task_type;
drop table configuration_email;
drop table tax_type;
drop sequence seq_form_data_nnn;
drop sequence seq_ref_book_vzl_history;
drop sequence seq_declaration_subreport;
drop sequence seq_log_query;
drop sequence seq_log_query_session;

drop function blob_to_clob;
drop procedure create_form_data_nnn;
drop procedure delete_form_template;
drop procedure rebuild_form_data_ref_book;
drop package body form_data_nnn;
drop package form_data_nnn;
drop package body declaration_pckg;
drop package declaration_pckg;
drop package body form_data_pckg;
drop package form_data_pckg;

--------------------------------------------------------------------------------------------------------
--                                      ???? "????????"
--------------------------------------------------------------------------------------------------------
drop table ndfl_person cascade constraints;
drop sequence seq_ndfl_person;

drop table ndfl_person_income;
drop sequence seq_ndfl_person_income;

drop table ndfl_person_deduction;
drop sequence seq_ndfl_person_deduction;

drop table ndfl_person_prepayment;
drop sequence seq_ndfl_person_prepayment;
--------------------------------------------------------------------------------------------------------------------------
-- ?????????????????????? ??????????
--------------------------------------------------------------------------------------------------------------------------

drop table configuration_scheduler cascade constraints;
drop table configuration_scheduler_param cascade constraints;
