--http://jira.aplana.com/browse/SBRFACCTAX-11878: 0.6.1 Параметры представления деклараций по налогу на имущество. Атрибуты КПП и ОКТМО должны быть обязательны для заполнения
update ref_book_attribute set required = 1 where id in (2004, 2005);

COMMIT;
EXIT;