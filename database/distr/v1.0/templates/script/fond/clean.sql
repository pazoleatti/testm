DELETE FROM blob_data WHERE id IN (
	SELECT blob_data_id FROM declaration_subreport WHERE declaration_template_id IN (200) UNION
	SELECT jrxml FROM declaration_template WHERE id                              IN (200) UNION
	SELECT xsd   FROM declaration_template WHERE id                              IN (200)
);
DELETE FROM declaration_subreport WHERE declaration_template_id                  IN (200);
DELETE FROM declaration_template  WHERE id                                       IN (200);

COMMIT;
EXIT;