update ref_book_attribute set is_unique = 1 where id in (3305,3304);

--http://jira.aplana.com/browse/SBRFACCTAX-13269: Удаление всех текущих блокировок
delete from lock_data;

--http://jira.aplana.com/browse/SBRFACCTAX-12605: 0.8 Реализовать возможность создания форм типа "Расчетная"
INSERT INTO form_kind (id, name) VALUES (6, 'Расчетная');

--http://jira.aplana.com/browse/SBRFACCTAX-12866: справочники для табличных частей настроек  - неверсионными
update ref_book set IS_VERSIONED = 0 where id in (206, 310, 330);

--http://jira.aplana.com/browse/SBRFACCTAX-13181: Асинхронная задача Обновление формы
insert into async_task_type (id, name, handler_jndi, limit_kind) 
  values (21, 'Обновление формы', 'ejb/taxaccounting/async-task.jar/RefreshFormDataAsyncTaskImpl#com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote', 'Количество ячеек таблицы формы = Количество строк * Количество граф');
  
--http://jira.aplana.com/browse/SBRFACCTAX-13378: Исправить настройки граф согласно ограничениям на размерность
update form_column set max_length = 17 + precision where max_length - precision > 17 and type = 'N';

--http://jira.aplana.com/browse/SBRFACCTAX-13356: Переименование событий в таблице событий
update event set name = 'Архивация журнала аудита' where id = 601;
update event set name = 'Версия макета создана' where id = 701;
update event set name = 'Версия макета изменена' where id = 702;
update event set name = 'Версия макета введена в действие' where id = 703;
update event set name = 'Версия макета выведена из действия' where id = 704;
update event set name = 'Версия макета удалена' where id = 705;

COMMIT;
EXIT;