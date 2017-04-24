MERGE INTO declaration_type t USING (
    SELECT 200 ID, 'F' TAX_TYPE, '1151111 (первичная)' NAME, 0 STATUS, 0 IS_IFRS, 'NULL' IFRS_NAME FROM DUAL) s ON
    (t.id = s.id)
WHEN NOT MATCHED THEN INSERT
    (t.id, t.tax_type, t.name, t.status, t.is_ifrs, t.ifrs_name)
VALUES
    (s.id, s.tax_type, s.name, s.status, s.is_ifrs, s.ifrs_name)
WHEN MATCHED THEN UPDATE SET 
    t.tax_type = s.tax_type, t.name = s.name, t.status = s.status, t.is_ifrs = s.is_ifrs, t.ifrs_name = s.ifrs_name;

COMMIT;
EXIT;