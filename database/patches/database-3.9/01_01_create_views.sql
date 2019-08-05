--3.9-skononova-4
begin
	dbms_output.put_line ('Create vw_declaration_data_file');
end;
/
create or replace view vw_declaration_data_file as 
select declaration_data_id, blob_data_id, user_name, user_department_name, note, file_type_id 
from declaration_data_file;

grant select on vw_declaration_data_file to &2 ;
grant references on declaration_data_file to &2 ;

comment on table vw_declaration_data_file is 'Файлы налоговой формы';
comment on column vw_declaration_data_file.declaration_data_id is 'Идентификатор экземпляра налоговой формы';
comment on column vw_declaration_data_file.blob_data_id is 'Файл налоговой формы';
comment on column vw_declaration_data_file.user_name is 'Пользователь, прикрепивший файл';
comment on column vw_declaration_data_file.user_department_name is 'Подразделение пользователя, прикрепившего файл';
comment on column vw_declaration_data_file.note is 'Комментарий к файлу';
comment on column vw_declaration_data_file.file_type_id is 'Категория файла';

/

--3.9-skononova-5
begin
	dbms_output.put_line ('Create vw_ref_book_attach_file_type');
end;
/
create or replace view vw_ref_book_attach_file_type as 
select id, code, name from ref_book_attach_file_type;

grant select on vw_ref_book_attach_file_type to &2 ;
grant references on ref_book_attach_file_type to &2 ;

comment on table vw_ref_book_attach_file_type is 'Категории прикрепленных файлов';
comment on column vw_ref_book_attach_file_type.id is 'Идентификатор';
comment on column vw_ref_book_attach_file_type.code is 'Код';
comment on column vw_ref_book_attach_file_type.name is 'Наименование';

/

--3.9-skononova-6
begin
	dbms_output.put_line ('Create vw_department_declaration_type');
end;
/

create or replace view vw_department_declaration_type as 
select id, department_id, declaration_type_id from department_declaration_type;
grant select on  vw_department_declaration_type to &2;
grant references on  department_declaration_type to &2;		  

comment on table vw_department_declaration_type is 'Сведения о налоговых формах, с которыми можно работать в подразделении';
comment on column vw_department_declaration_type.id is 'Идентификатор'; 
comment on column vw_department_declaration_type.department_id is 'Идентификатор прдразделения';
comment on column vw_department_declaration_type.declaration_type_id is 'Вид налоговой формы';

begin
	dbms_output.put_line ('Create vw_department_decl_type_perf');
end;
/

create or replace view vw_department_decl_type_perf as 
select department_decl_type_id, performer_dep_id from department_decl_type_performer;
grant select on vw_department_decl_type_perf to &2;
grant references on vw_department_decl_type_perf to &2;		

comment on table vw_department_decl_type_perf is 'Назначения нескольких исполнителей для связки НФ-подразделение';
comment on column vw_department_decl_type_perf.department_decl_type_id is 'Идентификатор связи подразделения с формой'; 
comment on column vw_department_decl_type_perf.performer_dep_id is 'Исполнитель';

begin
	dbms_output.put_line ('Create vw_department_declaration_data');
end;
/
create or replace view vw_declaration_data as
select id, declaration_template_id, tax_organ_code, kpp, oktmo,department_report_period_id, state, last_data_modified, correction_num, created_date,file_name  from declaration_data;
grant select on vw_declaration_data to &2;
grant references on declaration_data to &2;
comment on column vw_declaration_data.id is 'Идентификатор';
comment on column vw_declaration_data.declaration_template_id is 'Ссылка на шаблон налоговой формы';
comment on column vw_declaration_data.tax_organ_code is 'Налоговый орган';
comment on column vw_declaration_data.kpp is 'КПП';
comment on column vw_declaration_data.oktmo is 'ОКТМО';
comment on column vw_declaration_data.department_report_period_id is 'Идентификатор отчетного периода подразделения';
comment on column vw_declaration_data.state is 'Статус (состояние формы)';
comment on column vw_declaration_data.file_name is 'Имя файла';
comment on column vw_declaration_data.last_data_modified is 'Дата последних изменений данных формы';
comment on column vw_declaration_data.created_date is 'Дата создания формы';
comment on column vw_declaration_data.correction_num is 'Номер коррекции';
comment on table vw_declaration_data is 'Налоговые формы';
