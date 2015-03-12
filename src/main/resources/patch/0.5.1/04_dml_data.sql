--http://jira.aplana.com/browse/SBRFACCTAX-10638: Ошибка при обращении(расчете в декларациях) к автоматически созданным формам "Сведения о суммах налога на прибыль, уплаченного Банком за рубежом"
update data_row set alias = 'SUM_TAX' where alias = 'R1' and form_data_id in (select id from form_data where form_template_id = 417);
update data_row set alias = 'SUM_DIVIDENDS' where alias = 'R2' and form_data_id in (select id from form_data where form_template_id = 417);


--http://jira.aplana.com/browse/SBRFACCTAX-10199: 0.5 Модифицировать проверки и алгоритмы формирования атрибутов первичной "Приложение 5" и сводной "Расчет распределения" прибыли (в связи реализацией табличного представления и удаления "сумм за пределами РФ" формы настроек)
--запрос для смены типа графы
update form_column set width = 11, type = 'S', precision = null, max_length = 19 where form_template_id = 500 and alias = 'baseTaxOf';  
--запрос для переделки ячеек
update data_cell set svalue = REPLACE(CAST(nvalue as VARCHAR2(2000)),',','.'), nvalue = null where column_id in (select id from form_column where form_template_id = 500 and alias = 'baseTaxOf') and nvalue is not null;

COMMIT;
EXIT;