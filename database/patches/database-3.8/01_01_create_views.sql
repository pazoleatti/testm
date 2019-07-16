--3.8-skononova-1
begin
	dbms_output.put_line ('Create vw_department_config');
end;
/
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
--3.8-skononova- 3
--статус формы
begin
	dbms_output.put_line ('Create vw_state');
end;
/
create or replace view vw_state as select id, name from state;
grant select on vw_state to &2;
grant references on state to &2;

comment on column vw_state.id is 'Идентификатор';
comment on column vw_state.name is 'Наименование';
comment on table vw_state is 'Статус формы';  

--виды деклараций
begin
	dbms_output.put_line ('Create vw_declaration_type');
end;
/
create or replace view vw_declaration_type as select id, name, status from declaration_type;
grant select on vw_declaration_type to &2;
grant references on declaration_type to &2;

comment on table vw_declaration_type is 'Виды деклараций';
comment on column vw_declaration_type.id is 'Идентификатор';
comment on column vw_declaration_type.name is 'Наименование';
comment on column vw_declaration_type.status is 'Статус';

--шаблоны
begin
	dbms_output.put_line ('Create vw_declaration_template');
end;
/
create or replace view vw_declaration_template as
select id, status, version, name, create_script, jrxml, declaration_type_id, xsd,form_kind,form_type  from declaration_template;
grant select on vw_declaration_template to &2;
grant references on declaration_template to &2;

COMMENT ON COLUMN vw_declaration_template.id IS 'Идентификатор (первичный ключ)';
COMMENT ON COLUMN vw_declaration_template.status IS 'Статус версии (значения (-1, 0, 1, 2))';
COMMENT ON COLUMN vw_declaration_template.version IS 'Версия';
COMMENT ON COLUMN vw_declaration_template.name IS 'Наименование версии макета';
COMMENT ON COLUMN vw_declaration_template.create_script IS 'Скрипт формирования налоговой формы';
COMMENT ON COLUMN vw_declaration_template.jrxml IS 'Макет JasperReports для формирования печатного представления формы';
COMMENT ON COLUMN vw_declaration_template.declaration_type_id IS 'Вид налоговой формы';
COMMENT ON COLUMN vw_declaration_template.xsd IS 'XSD-схема';
COMMENT ON COLUMN vw_declaration_template.form_kind IS 'Вид налоговой формы';
COMMENT ON COLUMN vw_declaration_template.form_type IS 'Тип налоговой формы';
COMMENT ON TABLE  vw_declaration_template IS 'Шаблон налоговой формы';

--файловое хранилище
begin
	dbms_output.put_line ('Create vw_blob_data');                                                                         
end;
/
create or replace view vw_blob_data as
select id, name, data, creation_date from blob_data;
grant select on vw_blob_data to &2;
grant references on blob_data to &2;

comment on column vw_blob_data.id is 'Идентификатор';
comment on column vw_blob_data.name is 'Название файла';
comment on column vw_blob_data.data is 'Бинарные данные';
comment on column vw_blob_data.creation_date is 'Дата создания';
comment on table vw_blob_data is 'Файловое хранилище';

--
begin
	dbms_output.put_line ('Create vw_declaration_report');                                                                         
end;
/
create or replace view vw_declaration_report as
select declaration_data_id, blob_data_id, type, subreport_id from declaration_report;
grant select on vw_declaration_report to &2;
grant references on declaration_report to &2;

comment on column vw_declaration_report.declaration_data_id is 'Идентификатор налоговой формы';
comment on column vw_declaration_report.blob_data_id is 'Идентификатор отчета';
comment on column vw_declaration_report.type is 'Тип отчета (0 - Excel, 1 - XML, 2 - PDF, 3 - Jasper, 4 - Спец.отчет)';
comment on column vw_declaration_report.subreport_id is 'Идентификатор спец. отчета';
comment on table vw_declaration_report is 'Отчеты по налоговым формам';

--налоговые периоды
begin
	dbms_output.put_line ('Create vw_tax_period');                                                                         
end;
/
create or replace view vw_tax_period as
select id, tax_type, year from tax_period;
grant select on vw_tax_period to &2;
grant references on tax_period to &2;

comment on column vw_tax_period.id is 'Идентификатор';
comment on column vw_tax_period.tax_type is 'Вид налога';
comment on column vw_tax_period.year is 'Год';
comment on table vw_tax_period is 'Налоговые периоды';

--отчетные периоды
begin
	dbms_output.put_line ('Create vw_report_period');                                                                         
end;
/
create or replace view vw_report_period as
select id, name, tax_period_id, (select code from report_period_type where report_period_type.id=dict_tax_period_id) tax_period_code, start_date, end_date, calendar_start_date from report_period;
grant select on vw_report_period to &2;
grant references on report_period to &2;

comment on column vw_report_period.id is 'Идентификатор';
comment on column vw_report_period.name is 'Наименование';
comment on column vw_report_period.tax_period_id is 'Налоговый период';
comment on column vw_report_period.tax_period_code is 'Код налогового периода (двухзначный)';
comment on column vw_report_period.start_date is 'Дата начала отчетного перида';
comment on column vw_report_period.end_date is 'Дата окончания отчетного периода';
comment on column vw_report_period.calendar_start_date is 'Календарная дата начала отчетного периода';
comment on table vw_report_period is 'Отчетные периоды';

--привязка отчетных периодов к подразделениям
begin
	dbms_output.put_line ('Create vw_department_report_period');                                                                         
end;
/
create or replace view vw_department_report_period as
select id, department_id,  report_period_id ,  is_active, correction_date from department_report_period;
grant select on vw_department_report_period to &2;
grant references on department_report_period to &2;

comment on column vw_department_report_period.id is 'Идентификатор';
comment on column vw_department_report_period.department_id is 'Код подразделения';
comment on column vw_department_report_period.report_period_id is 'Код отчетного периода';
comment on column vw_department_report_period.is_active is 'Признак активности (0 - период закрыт, 1 - период открыт)';
comment on column vw_department_report_period.correction_date is 'Период сдачи корректировки';
comment on table vw_department_report_period is 'Привязка отчетных периодов к подразделениям';

--
begin
	dbms_output.put_line ('Create vw_declaration_data');                                                                         
end;
/
