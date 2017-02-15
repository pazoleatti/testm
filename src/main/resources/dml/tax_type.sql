MERGE INTO tax_type t USING (
  SELECT 'N' ID, 'НДФЛ' NAME FROM DUAL UNION ALL
  SELECT 'F' ID, 'Фонды и Сборы' NAME FROM DUAL) s ON
(t.id = s.id)
WHEN NOT MATCHED THEN
  INSERT (t.id, t.name) VALUES (s.id, s.name)
WHEN MATCHED THEN
  UPDATE SET t.name = s.name;