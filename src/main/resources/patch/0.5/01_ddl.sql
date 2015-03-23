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
comment on table data_row_temp is 'Временная таблица для сортировки строк';
comment on column data_row_temp.id is 'Идентификатор записи';
comment on column data_row_temp.ord is 'Число, соотвествующее номеру строки по порядку';

--http://jira.aplana.com/browse/SBRFACCTAX-10267: Добавить в БД новый тип асинхронной задачи "Генерация pdf-файла"
INSERT INTO ASYNC_TASK_TYPE (ID, NAME, HANDLER_JNDI) VALUES (7, 'Генерация pdf-файла', 'ejb/taxaccounting/async-task.jar/PdfGeneratorAsyncTask#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote');

--http://jira.aplana.com/browse/SBRFACCTAX-10284: Таблица для параметров блокировок
create table configuration_lock
(
key varchar2(100) not null,
timeout number(9) not null
);

alter table configuration_lock add constraint configuration_lock_pk primary key (key);

comment on table configuration_lock is 'Параметры блокировок';
comment on column configuration_lock.key is 'Ключ блокировки';
comment on column configuration_lock.timeout is 'Таймаут блокировки';

INSERT INTO configuration_lock (key, timeout) VALUES ('REF_BOOK', 3600000);
INSERT INTO configuration_lock (key, timeout) VALUES ('DECLARATION_DATA', 86400000);
INSERT INTO configuration_lock (key, timeout) VALUES ('DECLARATION_CREATE', 3600000);
INSERT INTO configuration_lock (key, timeout) VALUES ('FORM_DATA', 86400000);
INSERT INTO configuration_lock (key, timeout) VALUES ('FORM_DATA_IMPORT', 3600000);
INSERT INTO configuration_lock (key, timeout) VALUES ('FORM_DATA_CREATE', 3600000);
INSERT INTO configuration_lock (key, timeout) VALUES ('DECLARATION_TEMPLATE', 3600000);
INSERT INTO configuration_lock (key, timeout) VALUES ('FORM_TEMPLATE', 3600000);
INSERT INTO configuration_lock (key, timeout) VALUES ('LOG_SYSTEM_BACKUP', 3600000);
INSERT INTO configuration_lock (key, timeout) VALUES ('IFRS', 86400000);

--http://jira.aplana.com/browse/SBRFACCTAX-10294: Добавить поля в LOCK_DATA
alter table lock_data add date_lock date default current_date not null;
comment on column lock_data.date_lock is 'Дата установки блокировки';
------------------------------------------------------------------------------------------------------
-- отключить констрейнт на уникальность ORD. Ускорение сортировки, расчетов НФ.
alter table data_row disable constraint data_row_uniq_form_data_order;

COMMIT;
EXIT;

