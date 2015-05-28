--http://jira.aplana.com/browse/SBRFACCTAX-11016: Справочник "Классификатор ДОХОДОВ/РАСХОДОВ ОАО «Сбербанк России» для целей налогового учёта". Поле "Номер". Изменить алгоритм заполнения поля "Номер" + сделать поле "Номер видимым" 
update ref_book_attribute set visible = 1, read_only = 1 where id in (350, 360);
update ref_book_value set string_value = replace(string_value, '.', '') where attribute_id in (350, 360);

--http://jira.aplana.com/browse/SBRFACCTAX-11010: Справочник "Курсы валют"(22) сделать нередактируемыми буквенный код и наименование валюты + исправить записи
update ref_book_attribute set read_only = 1 where id in (82, 83);

merge into ref_book_value tgt
using (
  select rbv.record_id, rbv.attribute_id, rbv80.reference_value
  from ref_book_value rbv
  join ref_book_value rbv80 on rbv80.attribute_id = 80 and rbv.attribute_id in (82, 83) and rbv.record_id = rbv80.record_id and rbv.reference_value <> rbv80.reference_value) src
on (tgt.record_id = src.record_id and tgt.attribute_id = src.attribute_id)  
when matched then
     update set tgt.reference_value = src.reference_value;

--http://jira.aplana.com/browse/SBRFACCTAX-11406: КПП в справочнике Организации-участники контролируемых сделок
update ref_book_attribute set type = 1, precision = null  where id = 38;
update ref_book_value set string_value = number_value, number_value = null where attribute_id = 38 and number_value is not null;	 
	 
COMMIT;
EXIT;

