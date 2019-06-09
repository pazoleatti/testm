declare 
  v_count number;
begin
	select count(1) into v_count from user_tab_columns where lower(table_name)='async_task' and lower(column_name)='balancing_variant';
	IF v_count>0 THEN
		EXECUTE IMMEDIATE 'delete from async_task';
		EXECUTE IMMEDIATE 'alter table async_task rename column balancing_variant to queue';
		EXECUTE IMMEDIATE 'alter table async_task add user_id number(9) not null';
		EXECUTE IMMEDIATE 'comment on column async_task.user_id is ''Идентификатор пользователя, запустившего задачу''';
		EXECUTE IMMEDIATE 'alter table async_task add state number(6) default 1 not null ';
		EXECUTE IMMEDIATE 'comment on column async_task.state is ''Статус выполнения задачи''';
		EXECUTE IMMEDIATE 'alter table async_task add state_date timestamp default current_timestamp';
		EXECUTE IMMEDIATE 'comment on column async_task.state_date is ''Дата последнего изменения статуса''';
		EXECUTE IMMEDIATE 'alter table async_task add priority_node varchar2(500)';
		EXECUTE IMMEDIATE 'comment on column async_task.priority_node is ''Узел, которому принудительно будет назначена задача''';
		EXECUTE IMMEDIATE 'alter table async_task add description varchar2(4000)';
		EXECUTE IMMEDIATE 'comment on column async_task.description is ''Описание задачи''';
	END IF; 
	
	select count(1) into v_count from user_tables where lower(table_name)='lock_data_subscribers';
	IF v_count>0 THEN
		EXECUTE IMMEDIATE 'delete from lock_data_subscribers';
		EXECUTE IMMEDIATE 'alter table lock_data_subscribers rename to async_task_subscribers';
		EXECUTE IMMEDIATE 'alter table async_task_subscribers drop constraint lock_data_subscr_fk_lock_data';
		EXECUTE IMMEDIATE 'alter table async_task_subscribers drop constraint lock_data_subscribers_pk';
		EXECUTE IMMEDIATE 'alter table async_task_subscribers drop column lock_key';
		EXECUTE IMMEDIATE 'alter table async_task_subscribers add async_task_id number(18) not null';
		EXECUTE IMMEDIATE 'comment on column async_task_subscribers.async_task_id is ''Идентификатор задачи, после завершения которой, будет выполнено оповещение''';
		EXECUTE IMMEDIATE 'create index idx_async_t_subscr on async_task_subscribers(async_task_id)';
		EXECUTE IMMEDIATE 'alter table async_task_subscribers add constraint async_task_subscribers_pk primary key (async_task_id, user_id)';
		EXECUTE IMMEDIATE 'alter table async_task_subscribers add constraint async_t_subscr_fk_async_task foreign key (async_task_id) references async_task(id) on delete cascade';
		EXECUTE IMMEDIATE 'drop index lock_data_subscr_fk_sec_user';
		EXECUTE IMMEDIATE 'alter table async_task_subscribers drop constraint lock_data_subscr_fk_sec_user';
		EXECUTE IMMEDIATE 'alter table async_task_subscribers add constraint async_t_subscr_fk_sec_user foreign key (user_id) references sec_user(id) on delete cascade';
	END IF; 
	
	select count(1) into v_count from user_tab_columns where lower(table_name)='lock_data' and lower(column_name)='state';
	IF v_count>0 THEN
		EXECUTE IMMEDIATE 'delete from lock_data';
		EXECUTE IMMEDIATE 'alter table lock_data drop column state';
		EXECUTE IMMEDIATE 'alter table lock_data drop column state_date';
		EXECUTE IMMEDIATE 'alter table lock_data drop column queue';
		EXECUTE IMMEDIATE 'alter table lock_data drop column server_node';
		EXECUTE IMMEDIATE 'alter table lock_data drop constraint lock_data_pk';
		select count(1) into v_count from user_indexes where lower(index_name)='lock_data_pk';
		IF v_count>0 THEN
			EXECUTE IMMEDIATE 'drop index lock_data_pk';
		end if;
		EXECUTE IMMEDIATE 'alter table lock_data add task_id number(18) null';
		EXECUTE IMMEDIATE 'comment on column lock_data.task_id is ''Ссылка на асинхронную задачу, связанную с блокировкой''';
		EXECUTE IMMEDIATE 'alter table lock_data add id number(18) not null';
		EXECUTE IMMEDIATE 'comment on column lock_data.id is ''Идентификатор блокировки''';
		EXECUTE IMMEDIATE 'alter table lock_data add constraint lock_data_pk primary key (id)';
		EXECUTE IMMEDIATE 'create sequence seq_lock_data start with 1';
		EXECUTE IMMEDIATE 'alter table lock_data add constraint lock_data_uniq_key unique (key)';
	END IF; 
end;
/