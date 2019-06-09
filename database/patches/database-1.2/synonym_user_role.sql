declare 
  v_count number;
BEGIN
  select count(1) into v_count from user_constraints where constraint_name='NOTIFICATION_FK_NOTIFY_ROLE' AND table_name='NOTIFICATION';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table NOTIFICATION drop constraint NOTIFICATION_FK_NOTIFY_ROLE';
  END IF;  
  select count(1) into v_count from user_constraints where constraint_name='FK_REF_BOOK_ASNU_ROLE_ALIAS' AND table_name='REF_BOOK_ASNU';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table REF_BOOK_ASNU drop constraint FK_REF_BOOK_ASNU_ROLE_ALIAS';
  END IF;  
  select count(1) into v_count from user_constraints where constraint_name='FK_REF_BOOK_ASNU_ROLE_NAME' AND table_name='REF_BOOK_ASNU';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table REF_BOOK_ASNU drop constraint FK_REF_BOOK_ASNU_ROLE_NAME';
  END IF;  
  select count(1) into v_count from user_constraints where constraint_name='SEC_USER_ROLE_FK_ROLE_ID' AND table_name='SEC_USER_ROLE';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table SEC_USER_ROLE drop constraint SEC_USER_ROLE_FK_ROLE_ID';
  END IF;  
  select count(1) into v_count from user_constraints where constraint_name='ROLE_EVENT_FK_ROLE_ID' AND table_name='ROLE_EVENT';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table ROLE_EVENT drop constraint ROLE_EVENT_FK_ROLE_ID';
  END IF;  

  select count(1) into v_count from user_constraints where constraint_name='NOTIFICATION_FK_NOTIFY_USER' AND table_name='NOTIFICATION';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table NOTIFICATION drop constraint NOTIFICATION_FK_NOTIFY_USER';
  END IF;
  select count(1) into v_count from user_constraints where constraint_name='SEC_USER_ROLE_FK_USER_ID' AND table_name='SEC_USER_ROLE';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table SEC_USER_ROLE drop constraint SEC_USER_ROLE_FK_USER_ID';
  END IF;
  select count(1) into v_count from user_constraints where constraint_name='LOCK_DATA_FK_USER_ID' AND table_name='LOCK_DATA';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table LOCK_DATA drop constraint LOCK_DATA_FK_USER_ID';
  END IF;
  select count(1) into v_count from user_constraints where constraint_name='LOCK_DATA_SUBSCR_FK_SEC_USER' AND table_name='LOCK_DATA_SUBSCRIBERS';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table LOCK_DATA_SUBSCRIBERS drop constraint LOCK_DATA_SUBSCR_FK_SEC_USER';
  END IF;
  select count(1) into v_count from user_constraints where constraint_name='FK_LOG_USER' AND table_name='LOG';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table LOG drop constraint FK_LOG_USER';
  END IF;
  select count(1) into v_count from user_constraints where constraint_name='LOG_SYSTEM_REPORT_FK_SEC_USER' AND table_name='LOG_SYSTEM_REPORT';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table LOG_SYSTEM_REPORT drop constraint LOG_SYSTEM_REPORT_FK_SEC_USER';
  END IF;
  select count(1) into v_count from user_constraints where constraint_name='TASK_CONTEXT_FK_USER_ID' AND table_name='TASK_CONTEXT';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table TASK_CONTEXT drop constraint TASK_CONTEXT_FK_USER_ID';
  END IF;
  select count(1) into v_count from user_constraints where constraint_name='TEMPLATE_CHANGES_FK_USER_ID' AND table_name='TEMPLATE_CHANGES';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table TEMPLATE_CHANGES drop constraint TEMPLATE_CHANGES_FK_USER_ID';
  END IF;


  select count(1) into v_count from user_synonyms where synonym_name='SEC_ROLE';
  IF v_count=0 THEN
    UPDATE NOTIFICATION SET ROLE_ID=5 WHERE ROLE_ID=14;
    UPDATE REF_BOOK_ASNU SET ROLE_ALIAS=5 WHERE ROLE_ALIAS=14;
    UPDATE REF_BOOK_ASNU SET ROLE_NAME=5 WHERE ROLE_NAME=14;
    UPDATE ROLE_EVENT SET ROLE_ID=5 WHERE ROLE_ID=14;

    EXECUTE IMMEDIATE 'drop table SEC_ROLE';
    EXECUTE IMMEDIATE 'CREATE SYNONYM SEC_ROLE FOR &1..SEC_ROLE';
  END IF;

  select count(1) into v_count from user_synonyms where synonym_name='SEC_USER';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'drop table SEC_USER';
    EXECUTE IMMEDIATE 'CREATE SYNONYM SEC_USER FOR &1..SEC_USER';
  END IF;

  select count(1) into v_count from user_synonyms where synonym_name='SEC_USER_ROLE';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'drop table SEC_USER_ROLE';
    EXECUTE IMMEDIATE 'CREATE SYNONYM SEC_USER_ROLE FOR &1..SEC_USER_ROLE';
  END IF;

  select count(1) into v_count from user_constraints where constraint_name='NOTIFICATION_FK_NOTIFY_ROLE' AND table_name='NOTIFICATION';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'alter table NOTIFICATION add constraint NOTIFICATION_FK_NOTIFY_ROLE foreign key (ROLE_ID) references SEC_ROLE(ID)';
  END IF;
  select count(1) into v_count from user_constraints where constraint_name='FK_REF_BOOK_ASNU_ROLE_ALIAS' AND table_name='REF_BOOK_ASNU';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'alter table REF_BOOK_ASNU add constraint FK_REF_BOOK_ASNU_ROLE_ALIAS foreign key (ROLE_ALIAS) references SEC_ROLE(ID)';
  END IF;
  select count(1) into v_count from user_constraints where constraint_name='FK_REF_BOOK_ASNU_ROLE_NAME' AND table_name='REF_BOOK_ASNU';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'alter table REF_BOOK_ASNU add constraint FK_REF_BOOK_ASNU_ROLE_NAME foreign key (ROLE_NAME) references SEC_ROLE(ID)';
  END IF;
  select count(1) into v_count from user_constraints where constraint_name='ROLE_EVENT_FK_ROLE_ID' AND table_name='ROLE_EVENT';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'alter table ROLE_EVENT add constraint ROLE_EVENT_FK_ROLE_ID foreign key (ROLE_ID) references SEC_ROLE(ID)';
  END IF;

  select count(1) into v_count from user_constraints where constraint_name='NOTIFICATION_FK_NOTIFY_USER' AND table_name='NOTIFICATION';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'alter table NOTIFICATION add constraint NOTIFICATION_FK_NOTIFY_USER foreign key (USER_ID) references SEC_USER(ID)';
  END IF;
  select count(1) into v_count from user_constraints where constraint_name='LOCK_DATA_FK_USER_ID' AND table_name='LOCK_DATA';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'alter table LOCK_DATA add constraint LOCK_DATA_FK_USER_ID foreign key (USER_ID) references SEC_USER(ID)';
  END IF;
  select count(1) into v_count from user_constraints where constraint_name='LOCK_DATA_SUBSCR_FK_SEC_USER' AND table_name='LOCK_DATA_SUBSCRIBERS';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'alter table LOCK_DATA_SUBSCRIBERS add constraint LOCK_DATA_SUBSCR_FK_SEC_USER foreign key (USER_ID) references SEC_USER(ID)';
  END IF;
  select count(1) into v_count from user_constraints where constraint_name='FK_LOG_USER' AND table_name='LOG';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'alter table LOG add constraint FK_LOG_USER foreign key (USER_ID) references SEC_USER(ID)';
  END IF;
  select count(1) into v_count from user_tables where table_name='LOG_SYSTEM_REPORT';
  IF v_count>0 THEN
	select count(1) into v_count from user_constraints where constraint_name='LOG_SYSTEM_REPORT_FK_SEC_USER' AND table_name='LOG_SYSTEM_REPORT';
	IF v_count=0 THEN
		EXECUTE IMMEDIATE 'alter table LOG_SYSTEM_REPORT add constraint LOG_SYSTEM_REPORT_FK_SEC_USER foreign key (SEC_USER_ID) references SEC_USER(ID)';
	END IF;
  END IF;
  select count(1) into v_count from user_constraints where constraint_name='TASK_CONTEXT_FK_USER_ID' AND table_name='TASK_CONTEXT';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'alter table TASK_CONTEXT add constraint TASK_CONTEXT_FK_USER_ID foreign key (USER_ID) references SEC_USER(ID)';
  END IF;
  select count(1) into v_count from user_constraints where constraint_name='TEMPLATE_CHANGES_FK_USER_ID' AND table_name='TEMPLATE_CHANGES';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'alter table TEMPLATE_CHANGES add constraint TEMPLATE_CHANGES_FK_USER_ID foreign key (AUTHOR) references SEC_USER(ID)';
  END IF;
END;
/
