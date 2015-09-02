ALTER TABLE form_template MODIFY comparative DEFAULT 0;
UPDATE form_template SET comparative = 0 WHERE comparative IS NULL;

COMMIT;
EXIT;
