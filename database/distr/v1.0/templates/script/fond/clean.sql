DELETE FROM blob_data WHERE id IN (
'8c76f33c-0ad2-4d1c-afc1-b70bc4ad4b5d',
'415b49b5-cbc9-4f95-bf5d-7f90521798f1',
'e88efec4-f3ab-4162-a36a-053b2004b14e',
'3784d4da-a6f9-41c1-aec5-932d58bc2da6'
);

DELETE FROM declaration_template WHERE id IN (200);
DELETE FROM declaration_subreport WHERE id IN (2001, 2002);

COMMIT;
EXIT;