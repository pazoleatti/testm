
DELETE FROM declaration_template  WHERE id                                       		IN (100, 101, 102, 103, 104);
DELETE FROM decl_template_event_script where declaration_template_id                    IN (100, 101, 102, 103, 104);

COMMIT;
EXIT;
