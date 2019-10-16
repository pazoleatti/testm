--3.10.1-skononova-4
begin
	dbms_output.put_line ('Create vw_declaration_data_file');
end;
/
	create or replace view vw_declaration_data as
	select id, declaration_template_id, tax_organ_code, kpp, oktmo,
	department_report_period_id, state, last_data_modified, doc_state_id, created_date  from declaration_data;
	grant select on vw_declaration_data to &2;
	grant references on declaration_data to &2;
	comment on column vw_declaration_data.id is 'Идентификатор';
	comment on column vw_declaration_data.declaration_template_id is 'Ссылка на шаблон налоговой формы';
	comment on column vw_declaration_data.tax_organ_code is 'Налоговый орган';
	comment on column vw_declaration_data.kpp is 'КПП';
	comment on column vw_declaration_data.oktmo is 'ОКТМО';
	comment on column vw_declaration_data.department_report_period_id is 'Идентификатор отчетного периода подразделения';
	comment on column vw_declaration_data.state is 'Статус (состояние формы)';
	comment on column vw_declaration_data.doc_state_id is 'Статус (состояние формы)';
	comment on column vw_declaration_data.last_data_modified is 'Дата последних изменений данных формы';
	comment on column vw_declaration_data.created_date is 'Дата создания формы';
	comment on table vw_declaration_data is 'Налоговые формы';
/


begin
	dbms_output.put_line ('Create vw_ref_book_doc_state');
end;
/
	create or replace view VW_REF_BOOK_DOC_STATE (id, knd, name) as select id, knd,name from ref_book_doc_state;
	grant select on VW_REF_BOOK_DOC_STATE to &2;
	grant references on VW_REF_BOOK_DOC_STATE to &2;
	comment on column REF_BOOK_DOC_STATE.ID IS 'Уникальный идентификатор';
	comment on column REF_BOOK_DOC_STATE.KND IS 'Код формы по КНД';
	comment on column REF_BOOK_DOC_STATE.NAME IS 'Наименование состояния';
	comment on  table REF_BOOK_DOC_STATE  IS 'Состояние ЭД';

/
