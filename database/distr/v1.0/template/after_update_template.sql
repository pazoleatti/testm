--восстановить ограничения
--declaration_type
ALTER TABLE declaration_template ENABLE CONSTRAINT declaration_template_fk_dtype;
ALTER TABLE department_declaration_type ENABLE CONSTRAINT dept_decl_type_fk_decl_type;

--declaration_template
ALTER TABLE template_changes ENABLE CONSTRAINT template_changes_fk_dec_t;
ALTER TABLE declaration_data ENABLE CONSTRAINT declaration_data_fk_decl_t_id;
ALTER TABLE declaration_subreport ENABLE CONSTRAINT decl_subrep_fk_decl_template;
-- добавить FK для вложений

--declaration_subreport
ALTER TABLE declaration_report ENABLE CONSTRAINT decl_report_fk_decl_subreport;
ALTER TABLE declaration_subreport_params ENABLE CONSTRAINT fk_decl_subrep_pars_subrep_id;

--blob_data
ALTER TABLE ref_book ENABLE CONSTRAINT ref_book_fk_script_id;
ALTER TABLE declaration_template ENABLE CONSTRAINT declaration_tem_fk_blob_data;
ALTER TABLE declaration_template ENABLE CONSTRAINT dec_tem_fk_blob_data_jrxml;
ALTER TABLE notification ENABLE CONSTRAINT notification_fk_blob_data_id;
ALTER TABLE notification ENABLE CONSTRAINT notification_fk_report_id;
ALTER TABLE log_system ENABLE CONSTRAINT log_system_fk_blob_data;
ALTER TABLE log_system_report ENABLE CONSTRAINT log_system_report_fk_blob_data;
ALTER TABLE declaration_subreport ENABLE CONSTRAINT decl_subrep_fk_blob_data
ALTER TABLE declaration_report ENABLE CONSTRAINT decl_report_fk_blob_data
ALTER TABLE declaration_data_file ENABLE CONSTRAINT decl_data_file_pk;

COMMIT;
EXIT;

