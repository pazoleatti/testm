MERGE INTO declaration_type t USING (
    SELECT 100 ID, 'РНУ_НДФЛ (первичная)' NAME, 0 STATUS, 0 IS_IFRS, 'NULL' IFRS_NAME FROM DUAL UNION ALL
    SELECT 101 ID, 'РНУ_НДФЛ (консолидированная)' NAME, 0 STATUS, 0 IS_IFRS, 'NULL' IFRS_NAME FROM DUAL UNION ALL
    SELECT 102 ID, '2-НДФЛ (1)' NAME, 0 STATUS, 0 IS_IFRS, 'NULL' IFRS_NAME FROM DUAL UNION ALL
    SELECT 103 ID, '6-НДФЛ' NAME, 0 STATUS, 0 IS_IFRS, 'NULL' IFRS_NAME FROM DUAL UNION ALL
    SELECT 104 ID, '2-НДФЛ (2)' NAME, 0 STATUS, 0 IS_IFRS, 'NULL' IFRS_NAME FROM DUAL ) s ON
    (t.id = s.id)
WHEN NOT MATCHED THEN INSERT
    (t.id, t.name, t.status, t.is_ifrs, t.ifrs_name)
VALUES
    (s.id, s.name, s.status, s.is_ifrs, s.ifrs_name)
WHEN MATCHED THEN UPDATE SET 
    t.name = s.name, t.status = s.status, t.is_ifrs = s.is_ifrs, t.ifrs_name = s.ifrs_name;

COMMIT;
EXIT;