INSERT INTO declaration_template_file (blob_data_id,declaration_template_id) (
    SELECT 'b299d9b0-f9a5-40cd-9df7-f87a3859d801' blob_data_id, 200 declaration_template_id FROM DUAL UNION ALL
    SELECT 'b299d9b0-f9a5-40cd-9df7-f87a3859d802' blob_data_id, 200 declaration_template_id FROM DUAL UNION ALL
    SELECT 'b299d9b0-f9a5-40cd-9df7-f87a3859d803' blob_data_id, 200 declaration_template_id FROM DUAL UNION ALL
    SELECT 'b299d9b0-f9a5-40cd-9df7-f87a3859d804' blob_data_id, 200 declaration_template_id FROM DUAL);

COMMIT;
EXIT;