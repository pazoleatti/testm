
DELETE FROM declaration_template  WHERE id                                       		IN (100, 101, 102, 103, 104);
DELETE FROM decl_template_event_script where declaration_template_id                    IN (100, 101, 102, 103, 104);

DELETE FROM blob_data where id in ('047f207c-113e-488d-8390-9afc248a3bc8','04d9b114-1782-4d09-ad88-729e5605c6ff','41303bf7-9765-463f-a34b-f0a280bfa7bf');
COMMIT;
EXIT;
