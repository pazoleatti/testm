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

commit;
end;