create table dict_tax_period(
code varchar2(2) not null,
name varchar2(510) not null
);
alter table dict_tax_period add constraint dict_tax_period_pk primary key (code);

comment on table dict_tax_period is 'Коды, определяющие налоговый (отчётный) период';
comment on column dict_tax_period.code is 'код';
comment on column dict_tax_period.name is 'наименование';

create table dict_region(
code varchar2(2) not null,
name varchar2(510) not null
);
alter table dict_region add constraint dict_region_pk primary key (code);

comment on table dict_region is 'Коды субъектов Российской Федерации';
comment on column dict_region.code is 'код';
comment on column dict_region.name is 'наименование';