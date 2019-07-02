create or replace view vw_department_config (id, department_id, kpp, oktmo, tax_organ_code, reorg_successor_kpp, start_date, end_date) as
select id,  department_id, kpp, (select code from ref_book_oktmo rk where rk.id=oktmo_id) as oktmo, tax_organ_code, 
REORG_SUCCESSOR_KPP,start_date, end_date from department_config;

grant select on vw_department_config to &1 with grant option;

comment on table vw_department_config is 'Настройки подразделений (представление для НСИ)';
comment on column vw_department_config.id is 'Идентификатор';
comment on column vw_department_config.department_id is 'Подразделение';
comment on column vw_department_config.kpp is 'КПП';
comment on column vw_department_config.oktmo is 'ОКТМО';
comment on column vw_department_config.tax_organ_code is 'Код налогового органа (конечного)';
comment on column vw_department_config.REORG_SUCCESSOR_KPP is 'КПП подразделения правопреемника';
comment on column vw_department_config.start_date is 'Дата начала актуальности';
comment on column vw_department_config.end_date is 'Дата окончания актуальности';

/

