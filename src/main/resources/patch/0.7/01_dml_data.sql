--http://jira.aplana.com/browse/SBRFACCTAX-11977: В справочнике "Тип подразделений" значение "Пустой" переименовать в "Прочие"
update department_type set name='Прочие' where id = 5;

--http://jira.aplana.com/browse/SBRFACCTAX-12131: Новый вид налога E
insert into tax_type (id, name) values ('E', 'Эффективная налоговая ставка');

--http://jira.aplana.com/browse/SBRFACCTAX-12177: Добавить в справочник "Коды, определяющие налоговый (отчётный) период"(id=8) новый атрибут для ЭНС
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (3000,8,'Принадлежность к ЭНС','E',2,7,null,null,0,0,10,0,0,null,6,0,1);

merge into ref_book_value tgt
using (
  select rbr.id as record_id, 3000 as attribute_id, case when rbv.string_value in ('21', '22', '23', '24') then 1 else 0 end as number_value 
  from ref_book_record rbr
  join ref_book_value rbv on rbv.record_id = rbr.id and rbv.attribute_id = 25) src
on (tgt.record_id = src.record_id and tgt.attribute_id = src.attribute_id) 
when matched then
     update set tgt.number_value = src.number_value
when not matched then 
     insert (tgt.record_id, tgt.attribute_id, tgt.number_value) values (src.record_id, src.attribute_id, src.number_value); 

COMMIT;
EXIT;