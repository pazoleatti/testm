--declaration_type
ALTER TABLE declaration_template DISABLE CONSTRAINT declaration_template_fk_dtype;
ALTER TABLE department_declaration_type DISABLE CONSTRAINT dept_decl_type_fk_decl_type;

--declaration_template
ALTER TABLE template_changes DISABLE CONSTRAINT template_changes_fk_dec_t;
ALTER TABLE declaration_data DISABLE CONSTRAINT declaration_data_fk_decl_t_id;
ALTER TABLE declaration_subreport DISABLE CONSTRAINT decl_subrep_fk_decl_template;
ALTER TABLE declaration_template_file DISABLE CONSTRAINT fk_decl_templ_file_template;

--declaration_subreport
ALTER TABLE declaration_report DISABLE CONSTRAINT decl_report_fk_decl_subreport;
--ALTER TABLE declaration_subreport_params DISABLE CONSTRAINT fk_decl_subrep_pars_subrep_id; -- ����� ��������� ��������

--blob_data
ALTER TABLE ref_book DISABLE CONSTRAINT ref_book_fk_script_id;
ALTER TABLE declaration_template DISABLE CONSTRAINT declaration_tem_fk_blob_data;
ALTER TABLE declaration_template DISABLE CONSTRAINT dec_tem_fk_blob_data_jrxml;
--ALTER TABLE notification DISABLE CONSTRAINT notification_fk_blob_data_id;
ALTER TABLE notification DISABLE CONSTRAINT notification_fk_report_id;
--ALTER TABLE log_system DISABLE CONSTRAINT log_system_fk_blob_data;
ALTER TABLE declaration_subreport DISABLE CONSTRAINT decl_subrep_fk_blob_data;
ALTER TABLE declaration_report DISABLE CONSTRAINT decl_report_fk_blob_data;
ALTER TABLE declaration_data_file DISABLE CONSTRAINT decl_data_file_pk;
ALTER TABLE declaration_template_file DISABLE CONSTRAINT fk_decl_templ_file_blob;

ALTER TABLE decl_template_event_script DISABLE CONSTRAINT FK_DEC_TEMP_EVENT_SCR_DEC_TEMP;

ALTER TABLE decl_template_checks DISABLE CONSTRAINT FK_DECL_TEMPLATE_CHECKS;

COMMIT;
EXIT;

