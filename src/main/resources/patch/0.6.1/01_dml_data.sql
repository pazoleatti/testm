--http://jira.aplana.com/browse/SBRFACCTAX-11878: 0.6.1 Параметры представления деклараций по налогу на имущество. Атрибуты КПП и ОКТМО должны быть обязательны для заполнения
update ref_book_attribute set required = 1 where id in (2004, 2005);

--http://jira.aplana.com/browse/SBRFACCTAX-11053: Установить коды для названий НФ
update form_type 
set name = '('||code||') '||name 
where tax_type = 'V' and (code like '724%' or code like '937%') and name not like '('||code||')%';

merge into form_template tgt
using (
  select ft.id, '('||t.code||') '||ft.name as name, '('||t.code||') '||ft.fullname as fullname from form_template ft 
  join form_type t on t.id = ft.type_id
  where tax_type = 'V' and (code like '724%' or code like '937%') and ft.name not like '('||t.code||')%') src
on (tgt.id = src.id)
when matched then
     update set tgt.name = src.name, tgt.fullname = src.fullname;  

COMMIT;
EXIT;