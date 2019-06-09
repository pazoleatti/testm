merge into CONFIGURATION_SCHEDULER a using
(select 1 as id, 'Очистка файлового хранилища' as task_name, '0 15 22 * * ?' as schedule, 1 as active, sysdate as modification_date, sysdate as last_fire_date from dual
union all
select 2 as id, 'Удаление истекших блокировок' as task_name, '0 10 22 * * ?' as schedule, 1 as active, sysdate as modification_date, sysdate as last_fire_date from dual
union all
select 3 as id, 'Очистка хранилища временных файлов' as task_name, '0 5 22 * * ?' as schedule, 1 as active, sysdate as modification_date, sysdate as last_fire_date from dual
union all
select 4 as id, 'Мониторинг асинхронных задач' as task_name, '0/5 * * * * ?' as schedule, 1 as active, sysdate as modification_date, sysdate as last_fire_date from dual
union all
select 5 as id, 'Мониторинг появление новых событий в УН, которые требуют обработки на стороне НДФЛ' as task_name, '0/30 * * * * ?' as schedule, 1 as active, sysdate as modification_date, sysdate as last_fire_date from dual
) b
on (a.id=b.id)
when matched then 
    update set a.task_name = b.task_name, a.schedule=b.schedule, a.active=b.active, a.modification_date=b.modification_date, a.last_fire_date=b.last_fire_date
when not matched then
	insert (id, task_name, schedule, active, modification_date, last_fire_date)
	values (b.id, b.task_name, b.schedule, b.active, b.modification_date, b.last_fire_date);
	
merge into configuration_scheduler_param a using
(select 20 as id, 'Время жизни блокировки (секунд)' as param_name, 2 as task_id, 1 as ord, 2 as type, 172800 as value from dual) b
on (a.id=b.id)
when matched then 
    update set a.param_name=b.param_name, a.task_id=b.task_id, a.ord=b.ord, a.type=b.type, a.value=b.value
when not matched then
	insert (id, param_name, task_id, ord, type, value)
    values (b.id, b.param_name, b.task_id, b.ord, b.type, b.value);
      