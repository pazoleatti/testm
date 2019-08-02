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
