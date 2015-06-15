--http://jira.aplana.com/browse/SBRFACCTAX-10485: Таблица для отчетов по журналу аудита
create table log_system_report
(
blob_data_id varchar2(36) not null,
type number(1) not null,
sec_user_id number(9)
);

comment on table log_system_report is 'Выгрузки журнала аудита';
comment on column log_system_report.blob_data_id is 'Идентификатор таблицы BLOB_DATA, в которой хранятся файлы';
comment on column log_system_report.type is 'Тип выгрузки (0 - архивация журнала аудита, 1 - генерация zip-файла для журнала аудита)';
comment on column log_system_report.sec_user_id is 'Идентификатор пользователя, инициировавшего выгрузку типа 1';

alter table log_system_report add constraint log_system_report_fk_blob_data foreign key (blob_data_id) references blob_data (id);
alter table log_system_report add constraint log_system_report_fk_sec_user foreign key (sec_user_id) references sec_user (id) on delete cascade;
alter table log_system_report add constraint log_system_report_chk_type check (type in (0, 1));

create index i_log_system_rep_blob_data_id on log_system_report(blob_data_id);

---------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-11292: Каскадное удаление blob для таблиц отчетов
alter table log_system_report 	drop constraint 	log_system_report_fk_blob_data;
alter table log_system_report 	add constraint 		log_system_report_fk_blob_data foreign key (blob_data_id) references blob_data (id) on delete cascade;

alter table declaration_report	drop constraint decl_report_fk_blob_data;
alter table declaration_report 	add constraint decl_report_fk_blob_data foreign key (blob_data_id) references blob_data (id) on delete cascade;

--http://jira.aplana.com/browse/SBRFACCTAX-11294: Ограничение уникальности на log_system_report.seq_user_id
alter table log_system_report add constraint log_system_report_unq_sec_user unique(sec_user_id);

---------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-10492: Удаление временной таблицы
drop table data_row_temp;

---------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-10446: Индекс в таблицу DATA_ROW
create index i_data_row_fdata_manual_type on data_row(form_data_id, manual, type);
---------------------------------------------------------------------------------------------

--http://jira.aplana.com/browse/SBRFACCTAX-10547: Удаление констрейнта на DATA_ROW, деактивированного в 0.5
drop index data_row_uniq_form_data_order;
alter table data_row drop constraint data_row_uniq_form_data_order;

---------------------------------------------------------------------------------------------
--Вынесение check-констрейнтов на tax_type в отдельную сущность
create table tax_type
(
id char(1) not null,
name varchar2(256) not null
);

alter table tax_type add constraint tax_type_pk primary key(id);

comment on table tax_type is 'Справочник типов налогов';
comment on column tax_type.id is 'Символьный идентификатор типа налога';
comment on column tax_type.name is 'Тип налога';

insert all
  into tax_type (id, name) values ('I', 'Прибыль')
  into tax_type (id, name) values ('P', 'Имущество')
  into tax_type (id, name) values ('T', 'Транспортный')
  into tax_type (id, name) values ('V', 'НДС')
  into tax_type (id, name) values ('D', 'ТЦО')
select * from dual;

alter table form_type drop constraint form_type_chk_taxtype;
alter table tax_period drop constraint tax_period_chk_taxtype;
alter table declaration_type drop constraint declaration_type_chk_tax_type;

alter table form_type add constraint form_type_fk_taxtype foreign key (tax_type) references tax_type(id);
alter table declaration_type add constraint declaration_type_fk_taxtype foreign key (tax_type) references tax_type(id);
alter table tax_period add constraint tax_period_fk_taxtype foreign key (tax_type) references tax_type(id);

---------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-10451: Таблицы для консолидации

create table form_data_consolidation
(
source_form_data_id number(9) not null,
target_form_data_id number(9) not null
);

comment on table form_data_consolidation is 'Сведения о консолидации налоговых форм в налоговые формы';
comment on column form_data_consolidation.source_form_data_id is 'Идентификатор НФ источника';
comment on column form_data_consolidation.target_form_data_id is 'Идентификатор НФ приемника';

create table declaration_data_consolidation
(
source_form_data_id number(9) not null,
target_declaration_data_id number(9) not null
);

comment on table declaration_data_consolidation is 'Сведения о консолидации налоговых форм в декларации';
comment on column declaration_data_consolidation.source_form_data_id is 'Идентификатор НФ источника';
comment on column declaration_data_consolidation.target_declaration_data_id is 'Идентификатор декларации приемника';

alter table form_data_consolidation add constraint form_data_consolidation_fk_src foreign key (source_form_data_id) references form_data(id);
alter table form_data_consolidation add constraint form_data_consolidation_fk_tgt foreign key (target_form_data_id) references form_data(id) on delete cascade;
create unique index i_form_data_consolidation_unq on form_data_consolidation (case when source_form_data_id is not null then target_form_data_id end, source_form_data_id);

alter table declaration_data_consolidation add constraint decl_data_consolidation_fk_src foreign key (source_form_data_id) references form_data(id);
alter table declaration_data_consolidation add constraint decl_data_consolidation_fk_tgt foreign key (target_declaration_data_id) references declaration_data(id) on delete cascade;
create unique index i_decl_data_consolidation_unq on declaration_data_consolidation (case when source_form_data_id is not null then target_declaration_data_id end, source_form_data_id);


---------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-10729: FORM_DATA_PERFORMER:NAME сделать поле необязательным для заполнения
alter table form_data_performer modify name null;

---------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-6078:  Возможность отключения поля "Дата актуальности" для неверсионных справочников
alter table ref_book add (is_versioned number(1) default 1 not null);
comment on column ref_book.is_versioned is 'Версионный справочник (0 - нет, 1 - да)';
alter table ref_book add constraint ref_book_chk_versioned check (is_versioned in (0, 1));

update ref_book set is_versioned = 0 where id in (30, 93, 207, 95, 74, 103, 94, 105, 104, 108, 204, 205, 400);
---------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-11209: Расширение полей LOCK_DATA
-- + http://jira.aplana.com/browse/SBRFACCTAX-11540
alter table lock_data add state varchar2(500);
alter table lock_data add state_date date;
alter table lock_data add description varchar2(4000);
alter table lock_data add queue number(9) default 0 not null;

comment on column lock_data.state is 'Статус выполнения асинхронной задачи, связанной с блокировкой';
comment on column lock_data.state_date is 'Дата последнего изменения статуса';
comment on column lock_data.description is 'Описание блокировки';
comment on column lock_data.queue is 'Очередь, в которой находится связанная асинхронная задача';
---------------------------------------------------------------------------------------------
--Оптимизация производительности
create index i_lock_data_subscr on lock_data_subscribers(lock_key);

---------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-11339: Таблица для конфигов асинхронных задач
--совместно с http://jira.aplana.com/browse/SBRFACCTAX-11160
alter table async_task_type add short_queue_limit number(18);
comment on column async_task_type.task_limit is 'Ограничение на выполнение задачи';

alter table async_task_type add task_limit number(18);
comment on column async_task_type.short_queue_limit is 'Ограничение на выполнение задачи в очереди быстрых задач';

alter table async_task_type add limit_kind varchar2(400);
comment on column async_task_type.limit_kind is 'Вид ограничения';

ALTER TABLE async_task_type ADD dev_mode NUMBER(1) DEFAULT 0 NOT NULL;
ALTER TABLE async_task_type ADD CONSTRAINT async_task_type_chk_dev_mode CHECK (dev_mode in (0, 1));
COMMENT ON COLUMN async_task_type.dev_mode IS 'Признак задачи для dev-мода';

INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id, table_name, is_versioned) VALUES (401, 'Настройки асинхронных задач', 0, 0, 0, null, 'ASYNC_TASK_TYPE', 0);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (4101, 401, '№', 'ID', 2, 1, null, null, 1, 0, 10, 1, 1, 1, null, 0, 18);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (4102, 401, 'Название типа задачи', 'NAME', 1, 2, null, null, 1, null, 10, 1, 0, null, null, 0, 100);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (4103, 401, 'JNDI имя класса-обработчика', 'HANDLER_JNDI', 1, 3, null, null, 1, null, 10, 1, 0, null, null, 0, 500);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (4104, 401, 'Ограничение на выполнение задачи в очереди быстрых задач', 'SHORT_QUEUE_LIMIT', 2, 4, null, null, 1, 0, 10, 1, 0, null, null, 0, 18);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (4105, 401, 'Ограничение на выполнение задачи', 'TASK_LIMIT', 2, 5, null, null, 1, 0, 10, 1, 0, null, null, 0, 18);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (4106, 401, 'Вид ограничения', 'LIMIT_KIND', 1, 6, null, null, 1, null, 10, 0, 0, null, null, 0, 400);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (4107, 401, 'Признак задачи для dev-мода', 'DEV_MODE', 2, 7, null, null, 1, 0, 10, 1, 0, null, null, 0, 1);

---------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-11083: логирование действий настройщика в ЖА (создание/изменение/удаление версии макета НФ/декларации)
alter table log_system drop constraint log_system_chk_rp;
alter table log_system drop constraint log_system_chk_dcl_form;

alter table log_system add constraint log_system_chk_rp check (event_id in (7, 11, 401, 402, 501, 502, 503, 601, 650, 901, 902, 903, 810, 811, 812, 813, 820, 821, 830, 831, 832, 840, 841, 842, 850, 860, 701, 702, 703, 704, 705) or report_period_name is not null);
alter table log_system add constraint log_system_chk_dcl_form check (event_id in (7, 11, 401, 402, 501, 502, 503, 601, 650, 901, 902, 903, 810, 811, 812, 813, 820, 821, 830, 831, 832, 840, 841, 842, 850, 860, 701, 702, 703, 704, 705) or declaration_type_name is not null or (form_type_name is not null and form_kind_id is not null));
---------------------------------------------------------------------------------------------

commit;
end;