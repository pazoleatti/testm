--http://jira.aplana.com/browse/SBRFACCTAX-11016: Справочник "Классификатор ДОХОДОВ/РАСХОДОВ ОАО «Сбербанк России» для целей налогового учёта". Поле "Номер". Изменить алгоритм заполнения поля "Номер" + сделать поле "Номер видимым" 
update ref_book_attribute set visible = 1, read_only = 1 where id in (350, 360);
update ref_book_value set string_value = replace(string_value, '.', '') where attribute_id in (350, 360);

COMMIT;
EXIT;