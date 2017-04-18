insert into configuration_scheduler (id, task_name, active, schedule, modification_date, last_fire_date) values (1, 'Очистка файлового хранилища', 1, '0 15 22 * * ?', date '2013-03-31', date '2015-03-31');
insert into configuration_scheduler (id, task_name, active, schedule, modification_date) values (2, 'Удаление истекших блокировок', 0, '0 10 22 * * ?', sysdate);
insert into configuration_scheduler (id, task_name, active, schedule, modification_date) values (3, 'Очистка каталога временных файлов', 1, '0 5 22 * * ?', sysdate);

insert into configuration_scheduler_param (id, param_name, task_id, type, ord, value) values (20, 'Время жизни блокировки (секунд)', 2, 2, 1, '172800');
