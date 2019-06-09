update async_task_type set name = 'Выгрузка отчетности' where id = 29; 

delete from async_task_type where id in (10, 110, 11, 111);