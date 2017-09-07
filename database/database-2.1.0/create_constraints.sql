declare 
  v_count number;
begin
	select count(1) into v_count from user_constraints where constraint_name='CONF_SCHEDULER_PK' AND table_name='CONFIGURATION_SCHEDULER';
	IF v_count=0 THEN
		EXECUTE IMMEDIATE 'alter table configuration_scheduler add constraint conf_scheduler_pk primary key (id)';
	END IF; 
	
	select count(1) into v_count from user_constraints where constraint_name='CONF_SCHEDULER_PARAM_PK' AND table_name='CONFIGURATION_SCHEDULER_PARAM';
	IF v_count=0 THEN
		EXECUTE IMMEDIATE 'alter table configuration_scheduler_param add constraint conf_scheduler_param_pk primary key (id)';
	END IF; 
	
	select count(1) into v_count from user_constraints where constraint_name='CONF_SCHEDULER_PARAM_FK_CONF' AND table_name='CONFIGURATION_SCHEDULER_PARAM';
	IF v_count=0 THEN
		EXECUTE IMMEDIATE 'alter table configuration_scheduler_param add constraint conf_scheduler_param_fk_conf foreign key (task_id) references configuration_scheduler(id) on delete cascade';
	END IF; 
	
	select count(1) into v_count from user_constraints where constraint_name='CONF_SCHEDULER_CHK_ACTIVE' AND table_name='CONFIGURATION_SCHEDULER';
	IF v_count=0 THEN
		EXECUTE IMMEDIATE 'alter table configuration_scheduler add constraint conf_scheduler_chk_active check(active in (0, 1))';
	END IF; 
	
	select count(1) into v_count from user_constraints where constraint_name='CONF_SCHEDULER_PARAM_CHK_TYPE' AND table_name='CONFIGURATION_SCHEDULER_PARAM';
	IF v_count=0 THEN
		EXECUTE IMMEDIATE 'alter table configuration_scheduler_param add constraint conf_scheduler_param_chk_type check(type in (1, 2, 3))';
	END IF; 
	
	select count(1) into v_count from user_constraints where constraint_name='ASYNC_TASK_PK' AND table_name='ASYNC_TASK';
	IF v_count=0 THEN
		EXECUTE IMMEDIATE 'alter table async_task add constraint async_task_pk primary key (id)';
	END IF; 
	
	select count(1) into v_count from user_constraints where constraint_name='ASYNC_TASK_FK_TYPE' AND table_name='ASYNC_TASK';
	IF v_count=0 THEN
		EXECUTE IMMEDIATE 'alter table async_task add constraint async_task_fk_type foreign key (type_id) references async_task_type(id)';
	END IF; 
end;
/