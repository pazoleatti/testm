declare 
  v_count number;
BEGIN

  select count(1) into v_count from user_constraints where constraint_name='ASYNC_T_SUBSCR_FK_SEC_USER' AND table_name='ASYNC_TASK_SUBSCRIBERS';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table ASYNC_TASK_SUBSCRIBERS drop constraint ASYNC_T_SUBSCR_FK_SEC_USER';
  END IF;
  
  select count(1) into v_count from user_constraints where constraint_name='LOCK_DATA_FK_USER_ID' AND table_name='LOCK_DATA';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table LOCK_DATA drop constraint LOCK_DATA_FK_USER_ID';
  END IF;
  
  select count(1) into v_count from user_constraints where constraint_name='FK_LOG_USER' AND table_name='LOG';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table LOG drop constraint FK_LOG_USER';
  END IF;
  
  select count(1) into v_count from user_constraints where constraint_name='NOTIFICATION_FK_NOTIFY_USER' AND table_name='NOTIFICATION';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table NOTIFICATION drop constraint NOTIFICATION_FK_NOTIFY_USER';
  END IF;
  
  select count(1) into v_count from user_constraints where constraint_name='TEMPLATE_CHANGES_FK_USER_ID' AND table_name='TEMPLATE_CHANGES';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table TEMPLATE_CHANGES drop constraint TEMPLATE_CHANGES_FK_USER_ID';
  END IF;
  
  EXECUTE IMMEDIATE 'drop synonym SEC_USER';
  EXECUTE IMMEDIATE 'CREATE SYNONYM SEC_USER FOR TAX_1_9.SEC_USER';
  
  select count(1) into v_count from user_constraints where constraint_name='ASYNC_T_SUBSCR_FK_SEC_USER' AND table_name='ASYNC_TASK_SUBSCRIBERS';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'alter table ASYNC_TASK_SUBSCRIBERS add constraint ASYNC_T_SUBSCR_FK_SEC_USER foreign key (USER_ID) references SEC_USER(ID)';
  END IF;
  
  select count(1) into v_count from user_constraints where constraint_name='LOCK_DATA_FK_USER_ID' AND table_name='LOCK_DATA';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'alter table LOCK_DATA add constraint LOCK_DATA_FK_USER_ID foreign key (USER_ID) references SEC_USER(ID)';
  END IF;
  
  select count(1) into v_count from user_constraints where constraint_name='FK_LOG_USER' AND table_name='LOG';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'alter table LOG add constraint FK_LOG_USER foreign key (USER_ID) references SEC_USER(ID)';
  END IF;
  
  select count(1) into v_count from user_constraints where constraint_name='NOTIFICATION_FK_NOTIFY_USER' AND table_name='NOTIFICATION';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'alter table NOTIFICATION add constraint NOTIFICATION_FK_NOTIFY_USER foreign key (USER_ID) references SEC_USER(ID)';
  END IF;
  
  select count(1) into v_count from user_constraints where constraint_name='TEMPLATE_CHANGES_FK_USER_ID' AND table_name='TEMPLATE_CHANGES';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'alter table TEMPLATE_CHANGES add constraint TEMPLATE_CHANGES_FK_USER_ID foreign key (AUTHOR) references SEC_USER(ID)';
  END IF;

END;

