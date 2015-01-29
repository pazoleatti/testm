--http://jira.aplana.com/browse/SBRFACCTAX-10007: Обновление данных в таблице DATA_CELL
update data_cell set dvalue = to_date('01.01.1900', 'DD.MM.YYYY') where dvalue < to_date('01.01.1900', 'DD.MM.YYYY');
alter table data_cell add constraint data_cell_chk_min_dvalue check (dvalue >= to_date('01.01.1900', 'DD.MM.YYYY'));


--http://jira.aplana.com/browse/SBRFACCTAX-10063: Увеличение размерности поля
ALTER TABLE log_system MODIFY declaration_type_name VARCHAR2(1000);

--http://jira.aplana.com/browse/SBRFACCTAX-10212: Оптимизация сортировки строк НФ
create global temporary table data_row_temp
(
  ID  NUMBER(18) not null primary key,
  ORD NUMBER(18) not null
)
on commit delete rows;

--http://jira.aplana.com/browse/SBRFACCTAX-10267: Добавить в БД новый тип асинхронной задачи "Генерация pdf-файла"
INSERT INTO ASYNC_TASK_TYPE (ID, NAME, HANDLER_JNDI) VALUES (7, 'Генерация pdf-файла', 'ejb/taxaccounting/async-task.jar/PdfGeneratorAsyncTask#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote');
------------------------------------------------------------------------------------------------------
COMMIT;
EXIT;

