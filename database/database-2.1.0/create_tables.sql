declare 
  v_count number;
begin
	select count(1) into v_count from user_tables where lower(table_name)='configuration_scheduler';
	IF v_count=0 THEN
		EXECUTE IMMEDIATE 'create table configuration_scheduler (
		id        			number(9)           not null,
		task_name 			varchar2(200 char) not null,
		schedule  			varchar2(100 char),
		active    			number(1) 			    not null,
		modification_date   	date          not null,
		last_fire_date 		date
		)';

		EXECUTE IMMEDIATE 'comment on table configuration_scheduler is ''Настройки задач планировщика''';
		EXECUTE IMMEDIATE 'comment on column configuration_scheduler.id is ''Идентификатор задачи''';
		EXECUTE IMMEDIATE 'comment on column configuration_scheduler.task_name is ''Название''';
		EXECUTE IMMEDIATE 'comment on column configuration_scheduler.schedule is ''Расписание''';
		EXECUTE IMMEDIATE 'comment on column configuration_scheduler.active is ''Признак активности''';
		EXECUTE IMMEDIATE 'comment on column configuration_scheduler.modification_date is ''Дата редактирования''';
		EXECUTE IMMEDIATE 'comment on column configuration_scheduler.last_fire_date is ''Дата последнего запуска''';
	END IF; 
	
	select count(1) into v_count from user_tables where lower(table_name)='configuration_scheduler_param';
	IF v_count=0 THEN
		EXECUTE IMMEDIATE 'create table configuration_scheduler_param(
		id        			number(9)           not null,
		param_name 			varchar2(200 char) not null,
		task_id      			number(9)           not null,
		ord        			number(9)           not null,
		type        			number(1)           not null,
		value		  			varchar2(200 char) not null
		)';

		EXECUTE IMMEDIATE 'comment on table configuration_scheduler_param is ''Параметры задач планировщика''';
		EXECUTE IMMEDIATE 'comment on column configuration_scheduler_param.id is ''Идентификатор параметра''';
		EXECUTE IMMEDIATE 'comment on column configuration_scheduler_param.task_id is ''Сслыка на задачу планирощика''';
		EXECUTE IMMEDIATE 'comment on column configuration_scheduler_param.ord is ''Порядок следования''';
		EXECUTE IMMEDIATE 'comment on column configuration_scheduler_param.type is ''Тип параметра(1 - Строка, 2 - Целое число, 3 - Число с плавающей запятой)''';
		EXECUTE IMMEDIATE 'comment on column configuration_scheduler_param.value is ''Значение''';
	END IF;
	
	select count(1) into v_count from user_tables where lower(table_name)='async_task';
	IF v_count=0 THEN
			EXECUTE IMMEDIATE 'create table async_task (
			id number(18) not null,
			type_id number(18) not null,
			create_date timestamp default current_timestamp,
			start_process_date timestamp default null,
			node varchar2(500) default null,
			balancing_variant number(1) not null,
			serialized_params blob
			)';
			
			EXECUTE IMMEDIATE 'comment on table async_task is ''Асинхронные задачи''';
			EXECUTE IMMEDIATE 'comment on column async_task.id is ''Идентификатор задачи''';
			EXECUTE IMMEDIATE 'comment on column async_task.type_id is ''Ссылка на тип задачи''';
			EXECUTE IMMEDIATE 'comment on column async_task.create_date is ''Дата создания задачи''';
			EXECUTE IMMEDIATE 'comment on column async_task.start_process_date is ''Дата начала выполнения задачи''';
			EXECUTE IMMEDIATE 'comment on column async_task.node is ''Название узла, на котором выполняется задача''';
			EXECUTE IMMEDIATE 'comment on column async_task.balancing_variant is ''Тип очереди, в которую помещена задача. 1 - короткие, 2 - длинные''';
			EXECUTE IMMEDIATE 'comment on column async_task.serialized_params is ''Сериализованные параметры, которые нужны для выполнения задачи''';
	END IF;
end;
/