--http://jira.aplana.com/browse/SBRFACCTAX-11977: В справочнике "Тип подразделений" значение "Пустой" переименовать в "Прочие"
update department_type set name='Прочие' where id = 5;