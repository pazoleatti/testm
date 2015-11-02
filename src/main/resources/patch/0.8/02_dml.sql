update ref_book_attribute set is_unique = 1 where id in (3305,3304);

--http://jira.aplana.com/browse/SBRFACCTAX-12605: 0.8 Реализовать возможность создания форм типа "Расчетная"
INSERT INTO form_kind (id, name) VALUES (6, 'Расчетная');

--http://jira.aplana.com/browse/SBRFACCTAX-12866: справочники для табличных частей настроек  - неверсионными
update ref_book set IS_VERSIONED = 0 where id in (206, 310, 330);

--http://jira.aplana.com/browse/SBRFACCTAX-13181: Асинхронная задача Обновление формы
insert into async_task_type (id, name, handler_jndi, limit_kind) 
  values (21, 'Обновление формы', 'ejb/taxaccounting/async-task.jar/RefreshFormDataAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote', 'Количество ячеек таблицы формы = Количество строк * Количество граф');

COMMIT;
EXIT;