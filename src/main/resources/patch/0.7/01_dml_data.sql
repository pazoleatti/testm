--http://jira.aplana.com/browse/SBRFACCTAX-11977: В справочнике "Тип подразделений" значение "Пустой" переименовать в "Прочие"
update department_type set name='Прочие' where id = 5;

--http://jira.aplana.com/browse/SBRFACCTAX-12131: Новый вид налога E
insert into tax_type (id, name) values ('E', 'Эффективная налоговая ставка');

COMMIT;
EXIT;