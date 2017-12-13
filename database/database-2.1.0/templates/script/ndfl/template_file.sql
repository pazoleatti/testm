INSERT INTO declaration_template_file (blob_data_id,declaration_template_id) (
    SELECT '4b84692c-7f70-4d11-844d-e61e81655336' blob_data_id, 100 declaration_template_id FROM DUAL UNION ALL
    SELECT '64536272-05bc-42e8-82c5-347faf22da20' blob_data_id, 101 declaration_template_id FROM DUAL );

COMMIT;
EXIT;