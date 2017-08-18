
--> удаление устаревших данных <--
DELETE FROM blob_data bd WHERE id NOT IN
       (
       SELECT DISTINCT id
       FROM (
            SELECT script_id id FROM ref_book
            UNION
            SELECT xsd FROM declaration_template
            UNION
            SELECT jrxml FROM declaration_template
            UNION
            SELECT blob_data_id FROM declaration_report
            UNION
            SELECT blob_data_id FROM form_data_report
            UNION
            SELECT blob_data_id FROM ifrs_data
            UNION
            SELECT blob_data_id FROM form_data_file
            UNION
            SELECT blob_data_id FROM declaration_data_file
            UNION
            SELECT blob_data_id FROM declaration_template_file
            UNION
            SELECT blob_data_id FROM declaration_subreport
            )
       WHERE id IS NOT NULL
       )
       AND (sysdate - bd.creation_date) > 1;

COMMIT;
EXIT;
