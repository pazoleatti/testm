--http://jira.aplana.com/browse/SBRFACCTAX-10638: Ошибка при обращении(расчете в декларациях) к автоматически созданным формам "Сведения о суммах налога на прибыль, уплаченного Банком за рубежом"
update data_row set alias = 'SUM_TAX' where alias = 'R1' and form_data_id in (select id from form_data where form_template_id = 417);
update data_row set alias = 'SUM_DIVIDENDS' where alias = 'R2' and form_data_id in (select id from form_data where form_template_id = 417);


--http://jira.aplana.com/browse/SBRFACCTAX-10199: 0.5 Модифицировать проверки и алгоритмы формирования атрибутов первичной "Приложение 5" и сводной "Расчет распределения" прибыли (в связи реализацией табличного представления и удаления "сумм за пределами РФ" формы настроек)
--запрос для смены типа графы
update form_column set width = 11, type = 'S', precision = null, max_length = 19 where form_template_id = 500 and alias = 'baseTaxOf';  
--запрос для переделки ячеек
update data_cell set svalue = REPLACE(CAST(nvalue as VARCHAR2(2000)),',','.'), nvalue = null where column_id in (select id from form_column where form_template_id = 500 and alias = 'baseTaxOf') and nvalue is not null;

--http://jira.aplana.com/browse/SBRFACCTAX-10404
INSERT INTO form_type (id, name, tax_type, code) VALUES (619, 'Итоговые данные из журнала полученных и выставленных счетов-фактур по посреднической деятельности', 'V', '937.3');

--новые виды деклараций
INSERT INTO declaration_type (id,tax_type,name,status,is_ifrs,ifrs_name) VALUES (13,'V','Декларация по НДС (раздел 8.1)',0,0,NULL);
INSERT INTO declaration_type (id,tax_type,name,status,is_ifrs,ifrs_name) VALUES (14,'V','Декларация по НДС (раздел 9)',0,0,NULL);
INSERT INTO declaration_type (id,tax_type,name,status,is_ifrs,ifrs_name) VALUES (12,'V','Декларация по НДС (раздел 8)',0,0,NULL);
INSERT INTO declaration_type (id,tax_type,name,status,is_ifrs,ifrs_name) VALUES (15,'V','Декларация по НДС (раздел 9.1)',0,0,NULL);
INSERT INTO declaration_type (id,tax_type,name,status,is_ifrs,ifrs_name) VALUES (16,'V','Декларация по НДС (раздел 10)',0,0,NULL);
INSERT INTO declaration_type (id,tax_type,name,status,is_ifrs,ifrs_name) VALUES (17,'V','Декларация по НДС (раздел 11)',0,0,NULL);
INSERT INTO declaration_type (id,tax_type,name,status,is_ifrs,ifrs_name) VALUES (18,'V','Декларация по НДС (раздел 8 без консолид. формы)',0,0,NULL);
INSERT INTO declaration_type (id,tax_type,name,status,is_ifrs,ifrs_name) VALUES (19,'I','Декларация по налогу на прибыль (ОП) (год 2014)',0,0,NULL);
UPDATE declaration_type SET name = 'Декларация по НДС (раздел 1-7)' WHERE id = 4;
UPDATE declaration_type SET name = 'Декларация по НДС (короткая, раздел 1-7)' WHERE id = 7;

INSERT INTO form_type (id, name, tax_type, status, code) VALUES (421, 'Сведения о суммах налога на прибыль, уплаченного Банком за рубежом (new)', 'I', 0, NULL);
INSERT INTO form_type (id, name, tax_type, status, code) VALUES (618, 'Сводный регистр налогового учета по формированию и использованию резерва по сомнительным долгам', 'I', 0, '1210-2');
UPDATE form_type SET name='Налоговые вычеты за прошедший налоговый период, связанные с изменением условий или расторжением договора, в случае возврата ранее реализованных товаров (отказа от услуг) или возврата соответствующих сумм авансовых платежей' WHERE id = 603;

--http://jira.aplana.com/browse/SBRFACCTAX-10884: Добавить время блокировки для задач планировщика в бд
INSERT INTO configuration_lock (key, timeout) VALUES ('SCHEDULER_TASK', 86400000);

COMMIT;
EXIT;