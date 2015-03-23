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
--http://jira.aplana.com/browse/SBRFACCTAX-9273: заполнить поле "Наименования для МСФО" макетов НФ, деклараций
UPDATE form_type SET is_ifrs=1, ifrs_name='РНУ-25' WHERE id=324;
UPDATE form_type SET is_ifrs=1, ifrs_name='РНУ-26' WHERE id=325;
UPDATE form_type SET is_ifrs=1, ifrs_name='РНУ-27' WHERE id=326;
UPDATE form_type SET is_ifrs=1, ifrs_name='РСД' WHERE id=614;
UPDATE form_type SET is_ifrs=1, ifrs_name='Доходы простые' WHERE id=301;
UPDATE form_type SET is_ifrs=1, ifrs_name='Расходы простые' WHERE id=304;
UPDATE form_type SET is_ifrs=1, ifrs_name='Доходы сложные' WHERE id=302;
UPDATE form_type SET is_ifrs=1, ifrs_name='Расходы сложные' WHERE id=303;

UPDATE declaration_type SET is_ifrs=1, ifrs_name='Декларация' WHERE id=2;
----------------------------------------------------------------------------------------------------------------------------
INSERT INTO tax_type (id, name) VALUES ('I', 'Прибыль');
INSERT INTO tax_type (id, name) VALUES ('P', 'Имущество');
----------------------------------------------------------------------------------------------------------------------------
-- новые виды деклараций
INSERT INTO declaration_type (id, tax_type, name, status, is_ifrs, ifrs_name) VALUES (8, 'P', 'Расчет по авансовому платежу', 0, 0, NULL);
INSERT INTO declaration_type (id, tax_type, name, status, is_ifrs, ifrs_name) VALUES (9, 'I', 'Декларация по налогу на прибыль (Банк) (new)', 0, 0, NULL);
INSERT INTO declaration_type (id, tax_type, name, status, is_ifrs, ifrs_name) VALUES (10, 'I', 'Декларация по налогу на прибыль (ОП) (new)', 0, 0, NULL);
----------------------------------------------------------------------------------------------------------------------------
-- коды для видов НФ
UPDATE form_type SET code = '787' || CHR(47) || '2' || CHR(47) || '1' WHERE id = 200;
UPDATE form_type SET code = '1084-3.1' WHERE id = 201;
UPDATE form_type SET code = '1084-3.2' WHERE id = 202;
UPDATE form_type SET code = '724.1' WHERE id = 600;
UPDATE form_type SET code = '724.2.1' WHERE id = 601;
UPDATE form_type SET code = '724.2.2' WHERE id = 602;
UPDATE form_type SET code = '724.4' WHERE id = 603;
UPDATE form_type SET code = '724.6' WHERE id = 604;
UPDATE form_type SET code = '724.7' WHERE id = 605;
UPDATE form_type SET code = '937.1' WHERE id = 606;
UPDATE form_type SET code = '937.1.13' WHERE id = 607;
UPDATE form_type SET code = '937.2' WHERE id = 608;
UPDATE form_type SET code = '937.2.13' WHERE id = 609;
----------------------------------------------------------------------------------------------------------------------------
-- новые виды НФ
INSERT INTO form_type (id, name, tax_type, status, code, is_ifrs, ifrs_name) VALUES (309, 'Остатки по начисленным авансовым платежам', 'I', 0, NULL, 0, NULL);
INSERT INTO form_type (id, name, tax_type, status, code, is_ifrs, ifrs_name) VALUES (411, 'Сведения для расчёта налога с доходов в виде дивидендов (new)', 'I', 0, NULL, 0, NULL);
INSERT INTO form_type (id, name, tax_type, status, code, is_ifrs, ifrs_name) VALUES (412, 'Сумма налога, подлежащая уплате в бюджет, по данным налогоплательщика (new)', 'I', 0, NULL, 0, NULL);
INSERT INTO form_type (id, name, tax_type, status, code, is_ifrs, ifrs_name) VALUES (413, 'Сведения о дивидендах, выплаченных в отчетном квартале (new)', 'I', 0, NULL, 0, NULL);
INSERT INTO form_type (id, name, tax_type, status, code, is_ifrs, ifrs_name) VALUES (610, 'Данные бухгалтерского учета для расчета налога на имущество', 'P', 0, '945.1', 0, NULL);
INSERT INTO form_type (id, name, tax_type, status, code, is_ifrs, ifrs_name) VALUES (611, 'Данные о кадастровой стоимости объектов недвижимости для расчета налога на имущество', 'P', 0, '945.2', 0, NULL);
INSERT INTO form_type (id, name, tax_type, status, code, is_ifrs, ifrs_name) VALUES (612, 'Расчёт налога на имущество по кадастровой стоимости', 'P', 0, '945.4', 0, NULL);
INSERT INTO form_type (id, name, tax_type, status, code, is_ifrs, ifrs_name) VALUES (613, 'Расчёт налога на имущество по средней' || CHR(47) || 'среднегодовой стоимости', 'P', 0, '945.3', 0, NULL);
INSERT INTO form_type (id, name, tax_type, status, code, is_ifrs, ifrs_name) VALUES (614, '(РСД) Расчет резерва по сомнительным долгам в целях налогообложения', 'I', 0, NULL, 1, 'РСД');
INSERT INTO form_type (id, name, tax_type, status, code, is_ifrs, ifrs_name) VALUES (615, 'Сводная форма данных бухгалтерского учета для расчета налога на имущество', 'P', 0, '945.5', 0, NULL);
----------------------------------------------------------------------------------------------------------------------------
INSERT INTO ASYNC_TASK_TYPE (ID, NAME, HANDLER_JNDI) VALUES (3, 'Генерация xlsm-файла', 'ejb/taxaccounting/async-task.jar/XlsmGeneratorAsyncTask#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote');
INSERT INTO ASYNC_TASK_TYPE (ID, NAME, HANDLER_JNDI) VALUES (4, 'Генерация csv-файла', 'ejb/taxaccounting/async-task.jar/CsvGeneratorAsyncTask#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote');
INSERT INTO ASYNC_TASK_TYPE (ID, NAME, HANDLER_JNDI) VALUES (5, 'Генерация xlsx-файла', 'ejb/taxaccounting/async-task.jar/XlsxGeneratorAsyncTask#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote');
INSERT INTO ASYNC_TASK_TYPE (ID, NAME, HANDLER_JNDI) VALUES (6, 'Генерация xml-файла', 'ejb/taxaccounting/async-task.jar/XmlGeneratorAsyncTask#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote');
INSERT INTO ASYNC_TASK_TYPE (ID, NAME, HANDLER_JNDI) VALUES (9, 'Генерация отчетности для МСФО', 'ejb/taxaccounting/async-task.jar/IfrsGeneratorAsyncTask#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote');
----------------------------------------------------------------------------------------------------------------------------
COMMIT;
EXIT;
