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
alter table lock_data add state varchar2(500);
alter table lock_data add state_date date;
alter table lock_data add description varchar2(4000);
alter table lock_data add queue varchar2(100);

comment on column lock_data.state is 'Статус выполнения асинхронной задачи, связанной с блокировкой';
comment on column lock_data.state_date is 'Дата последнего изменения статуса';
comment on column lock_data.description is 'Описание блокировки';
comment on column lock_data.queue is 'Очередь, в которой находится связанная асинхронная задача';
---------------------------------------------------------------------------------------------
--Оптимизация производительности
create index i_lock_data_subscr on lock_data_subscribers(lock_key);

---------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-11292: Каскадное удаление blob для таблиц отчетов
alter table log_system_report 	drop constraint 	log_system_report_fk_blob_data;
alter table log_system_report 	add constraint 		log_system_report_fk_blob_data foreign key (blob_data_id) references blob_data (id) on delete cascade;

alter table declaration_report	drop constraint decl_report_fk_blob_data;
alter table declaration_report 	add constraint decl_report_fk_blob_data foreign key (blob_data_id) references blob_data (id) on delete cascade;


---------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-11339: Таблица для конфигов асинхронных задач
create table configuration_async
(
id number(9) not null,
type varchar2(400) not null,
limit_kind varchar2(400) not null,
limit number(9),
short_limit number(9)
);

alter table configuration_async add constraint configuration_async_pk primary key(id);

comment on table configuration_async is 'Настройки асинхронных задач';
comment on column configuration_async.id is 'Идентификатор записи';
comment on column configuration_async.type is 'Тип задания';
comment on column configuration_async.limit_kind is 'Вид ограничения';
comment on column configuration_async.limit is 'Ограничение на выполнение задания';
comment on column configuration_async.short_limit is 'Ограничение на выполнение задания в очереди быстрых заданий';

insert all
      into configuration_async (id, type, limit_kind, limit, short_limit) values (1, 'Формирование XLSM-файла налоговой формы', 'Количество ячеек таблицы формы = Количество строк * Количество граф', 0, 0)
      into configuration_async (id, type, limit_kind, limit, short_limit) values (2, 'Формирование CSV-файла налоговой формы', 'Количество ячеек таблицы формы = Количество строк * Количество граф', 0, 0)
      into configuration_async (id, type, limit_kind, limit, short_limit) values (3, 'Загрузка ТФ налоговой формы с локального компьютера', 'Размер ТФ (Мбайт)', 0, 0)
      into configuration_async (id, type, limit_kind, limit, short_limit) values (4, 'Загрузка ТФ налоговой формы из каталога загрузки', 'Размер ТФ (Мбайт)', 0, 0)
      into configuration_async (id, type, limit_kind, limit, short_limit) values (5, 'Загрузка XLS-файла с формы экземпляра налоговой формы', 'Размер xls/xlsm/xlsx-файла (Мбайт)', 0, 0)
      into configuration_async (id, type, limit_kind, limit, short_limit) values (6, 'Загрузка ТФ с формы экземпляра налоговой формы (РНУ 25, 26, 27, 31)', 'Размер ТФ (Мбайт)', 0, 0)
      into configuration_async (id, type, limit_kind, limit, short_limit) values (7, 'Расчет налоговой формы', 'Количество ячеек таблицы формы = Количество строк * Количество граф', 0, 0)
      into configuration_async (id, type, limit_kind, limit, short_limit) values (8, 'Проверка налоговой формы', 'Количество ячеек таблицы формы = Количество строк * Количество граф', 0, 0)
      into configuration_async (id, type, limit_kind, limit, short_limit) values (9, 'Консолидация налоговой формы', 'Сумма количества ячеек таблицы формы по всем формам источникам. Количество ячеек таблицы формы источника = Количество строк * Количество граф', 0, 0)
      into configuration_async (id, type, limit_kind, limit, short_limit) values (10, 'Формирование XLSX-файла декларации/уведомления', 'Размер XML-файла декларации/уведомления (Мбайт)', 0, 0)
      into configuration_async (id, type, limit_kind, limit, short_limit) values (11, 'Расчет декларации/уведомления (формирование XML-файла)', 'Размер XML-файла декларации/уведомления (Мбайт)', 0, 0)
      into configuration_async (id, type, limit_kind, limit, short_limit) values (12, 'Формирование PDF-файла декларации/уведомления', 'Размер XML-файла декларации/уведомления (Мбайт)', 0, 0)
      into configuration_async (id, type, limit_kind, limit, short_limit) values (13, 'Проверка декларации/уведомления', 'Размер XML-файла декларации/уведомления (Мбайт)', 0, 0)
      into configuration_async (id, type, limit_kind, limit, short_limit) values (14, 'Формирование отчетности для МСФО', 'Общий размер файлов форм МСФО (налоговая форма - XLSM, декларация/уведомление - XLSX', 0, 0)
select * from dual;  

--

INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id, table_name) VALUES (401, 'Настройки асинхронных задач', 1, 0, 0, null, 'CONFIGURATION_ASYNC');
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (4100,401,'№','ID', 2,0,null,null,1,0,10,1,1,1,null,1,9);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (4101,401,'Тип задания','TYPE',1,1,null,null,1,null,30,0,0,null,null,1,200);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (4102,401,'Вид ограничения','LIMIT_KIND',1,2,null,null,1,null,30,0,0,null,null,0,200);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (4103,401,'Ограничение на выполнение задания','LIMIT',2,3,null,null,1,0,10,0,0,null,null,1,9);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (4104,401,'Ограничение на выполнение задания в очереди быстрых заданий','SHORT_LIMIT',2,4,null,null,1,0,10,0,0,null,null,1,9);

---------------------------------------------------------------------------------------------
commit;
end;