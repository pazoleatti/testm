create table general_taxperiod(
code varchar2(2) not null,
name varchar2(510) not null
);
alter table general_taxperiod add constraint general_taxperiod_pk primary key (code);

comment on table general_taxperiod is 'Коды, определяющие налоговый (отчётный) период';
comment on column general_taxperiod.code is 'код';
comment on column general_taxperiod.name is 'наименование';

create table general_subject_rf(
code varchar2(2) not null,
name varchar2(510) not null
);
alter table general_subject_rf add constraint general_subject_rf_pk primary key (code);

comment on table general_subject_rf is 'Коды субъектов Российской Федерации';
comment on column general_subject_rf.code is 'код';
comment on column general_subject_rf.name is 'наименование';