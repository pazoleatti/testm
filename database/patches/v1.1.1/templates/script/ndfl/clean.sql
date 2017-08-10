
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


DELETE FROM blob_data WHERE id IN (
	SELECT blob_data_id FROM declaration_subreport WHERE declaration_template_id 		IN (100, 101, 102, 103, 104) UNION
	SELECT blob_data_id FROM declaration_template_file WHERE declaration_template_id 	IN (100, 101, 102, 103, 104) UNION
	SELECT jrxml FROM declaration_template WHERE id                              		IN (100, 101, 102, 103, 104) UNION
	SELECT xsd   FROM declaration_template WHERE id                              		IN (100, 101, 102, 103, 104)
);
DELETE FROM declaration_template_file WHERE declaration_template_id                  	IN (100, 101, 102, 103, 104);
DELETE FROM declaration_subreport WHERE declaration_template_id                  		IN (100, 101, 102, 103, 104);
DELETE FROM declaration_template  WHERE id                                       		IN (100, 101, 102, 103, 104);

DELETE FROM DECLARATION_REPORT WHERE SUBREPORT_ID in (1022, 1031, 1042); 

Update notification 
Set report_id = NULL where report_id IN (Select report_id from notification where report_id NOT IN (select id from blob_data));

COMMIT;
EXIT;
