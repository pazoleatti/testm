---------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8728 - Внести изменения по данным "Сделки РЕПО", "Реализация и приобретение ценных бумаг"

-- "Сделки РЕПО"
MERGE INTO numeric_value tgt 
USING
  (SELECT nv.row_id,
           nv.column_id
   FROM form_template ft
   JOIN form_column fc ON fc.form_template_id = ft.id
   JOIN numeric_value nv ON nv.column_id = fc.id
   JOIN ref_book_record rbr ON rbr.id = nv.value
   JOIN ref_book_value rbv ON rbv.record_id = rbr.id
   AND rbv.attribute_id = 62
   WHERE ft.type_id in (383) 
     AND ft.version = to_date('01.01.2013', 'DD.MM.YYYY')
     AND fc.alias = 'dealsMode'
     AND rbv.number_value = 1 ) src 
ON (tgt.row_id = src.row_id AND tgt.column_id = src.column_id) 
WHEN matched THEN UPDATE SET tgt.value = NULL;

-- "Реализация и приобретение ценных бумаг"
MERGE INTO numeric_value tgt 
USING
  (SELECT nv.row_id,
           nv.column_id
   FROM form_template ft
   JOIN form_column fc ON fc.form_template_id = ft.id
   JOIN numeric_value nv ON nv.column_id = fc.id
   JOIN ref_book_record rbr ON rbr.id = nv.value
   JOIN ref_book_value rbv ON rbv.record_id = rbr.id
   AND rbv.attribute_id = 62
   WHERE ft.type_id in (384) 
     AND ft.version = to_date('01.01.2013', 'DD.MM.YYYY')
     AND fc.alias = 'transactionMode'
     AND rbv.number_value = 1 ) src 
ON (tgt.row_id = src.row_id AND tgt.column_id = src.column_id) 
WHEN matched THEN UPDATE SET tgt.value = NULL;

UPDATE ref_book_record rbr
SET rbr.status = -1
WHERE rbr.ref_book_id = 14 AND EXISTS (SELECT 1 FROM ref_book_value rbv WHERE rbv.record_id = rbr.id AND rbv.attribute_id = 62 AND rbv.number_value = 1);

DELETE FROM ref_book_record rbr WHERE rbr.ref_book_id = 14 AND STATUS = -1 AND EXISTS (SELECT 1 FROM ref_book_value rbv WHERE rbv.record_id = rbr.id AND rbv.attribute_id = 62 AND rbv.number_value = 1);

---------------------------------------------------------------------------------------------------
COMMIT;
EXIT;
