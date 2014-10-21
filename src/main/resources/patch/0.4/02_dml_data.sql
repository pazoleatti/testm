---------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-9170 - Добавить роль "Оператор гарантий"
INSERT INTO sec_role (id, ALIAS, name) VALUES (7, 'ROLE_GARANT', 'Оператор Гарантий');

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
-- http://jira.aplana.com/browse/SBRFACCTAX-8416: Изменения в стилях для граф "Наименование подразделения в декларации"

-- (Приложение 5) Сведения для расчета налога на прибыль
MERGE INTO cell_style USING (WITH set_style AS
                              (SELECT fty.id AS form_type_id,
                                      fs1.id AS control_sum,
                                      fs2.id AS auto_fill
                               FROM form_type fty
                               JOIN form_template fte ON fte.type_id = fty.id AND fty.id IN (372)
                               JOIN form_style fs1 ON fs1.form_template_id = fte.id AND fs1.alias = 'Контрольные суммы'
                               JOIN form_style fs2 ON fs2.form_template_id = fte.id
                               AND fs2.alias = 'Автозаполняемая')
                             SELECT dr.id AS row_id,
                                    fc.id AS column_id,
                                    CASE
                                        WHEN dr.alias IS NULL THEN ss.auto_fill
                                        ELSE ss.control_sum
                                    END AS style_id
                             FROM form_type fty
                             JOIN form_template fte ON fte.type_id = fty.id AND fty.id IN (372) AND fte.status = 0
                             JOIN form_column fc ON fc.form_template_id = fte.id AND fc.alias = 'divisionName'
                             JOIN form_data fd ON fd.form_template_id = fte.id
                             JOIN data_row dr ON dr.form_data_id = fd.id
                             JOIN set_style ss ON ss.form_type_id = fty.id) new_styles 
ON (cell_style.row_id = new_styles.row_id AND cell_style.column_id = new_styles.column_id) 
WHEN MATCHED THEN UPDATE SET cell_style.style_id = new_styles.style_id 
WHEN NOT MATCHED THEN INSERT (cell_style.row_id, cell_style.column_id, cell_style.style_id) VALUES (new_styles.row_id, new_styles.column_id, new_styles.style_id); 

-- Расчёт распределения авансовых платежей и налога на прибыль по обособленным подразделениям организации
MERGE INTO cell_style USING (WITH set_style AS
                              (SELECT fty.id AS form_type_id,
                                      fs1.id AS total,
                                      fs2.id AS auto_fill
                               FROM form_type fty
                               JOIN form_template fte ON fte.type_id = fty.id AND fty.id IN (500)
                               JOIN form_style fs1 ON fs1.form_template_id = fte.id AND fs1.alias = 'Итоговая'
                               JOIN form_style fs2 ON fs2.form_template_id = fte.id AND fs2.alias = 'Автозаполняемая')
                             SELECT dr.id AS row_id,
                                    fc.id AS column_id,
                                    CASE
                                        WHEN dr.alias IS NULL THEN ss.auto_fill
                                        ELSE ss.total
                                    END AS style_id
                             FROM form_type fty
                             JOIN form_template fte ON fte.type_id = fty.id AND fty.id IN (500) AND fte.status = 0
                             JOIN form_column fc ON fc.form_template_id = fte.id AND fc.alias = 'divisionName'
                             JOIN form_data fd ON fd.form_template_id = fte.id
                             JOIN data_row dr ON dr.form_data_id = fd.id
                             JOIN set_style ss ON ss.form_type_id = fty.id) new_styles 
ON (cell_style.row_id = new_styles.row_id AND cell_style.column_id = new_styles.column_id) 
WHEN MATCHED THEN UPDATE SET cell_style.style_id = new_styles.style_id 
WHEN NOT MATCHED THEN INSERT (cell_style.row_id, cell_style.column_id, cell_style.style_id) VALUES (new_styles.row_id, new_styles.column_id, new_styles.style_id);

----------------------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-9014: Новые стили для корректировок для макетов НФ
MERGE INTO form_style fs USING
 (SELECT id form_template_id,'Корректировка-добавлено' ALIAS, 0 font_color, 12 back_color, 0 italic, 0 bold FROM form_template
  UNION ALL 
  SELECT id form_template_id,'Корректировка-удалено' ALIAS, 0 font_color, 8 back_color, 0 italic, 0 bold FROM form_template
  UNION ALL 
  SELECT id form_template_id, 'Корректировка-изменено' ALIAS, 10 font_color, 4 back_color, 0 italic, 1 bold FROM form_template
  UNION ALL 
  SELECT id form_template_id, 'Корректировка-без изменений' ALIAS, 0 font_color, 6 back_color, 0 italic, 0 bold FROM form_template) fs2 
ON (fs.alias=fs2.alias AND fs.form_template_id=fs2.form_template_id) 
WHEN MATCHED THEN
	UPDATE
	SET fs.font_color=fs2.font_color,
		fs.back_color=fs2.back_color,
		fs.italic=fs2.italic,
		fs.bold=fs2.bold 
WHEN NOT MATCHED THEN
	INSERT (fs.id, fs.alias, fs.form_template_id, fs.font_color, fs.back_color, fs.italic, fs.bold)
	VALUES (seq_form_style.nextval, fs2.alias, fs2.form_template_id, fs2.font_color, fs2.back_color, fs2.italic, fs2.bold);

----------------------------------------------------------------------------------------------------------------------------

COMMIT;
EXIT;
