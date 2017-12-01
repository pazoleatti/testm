declare 
  v_count number;
BEGIN
    EXECUTE IMMEDIATE 'alter table NOTIFICATION drop constraint NOTIFICATION_FK_RECEIVER';
    EXECUTE IMMEDIATE 'alter table NOTIFICATION drop constraint NOTIFICATION_FK_SENDER';
    EXECUTE IMMEDIATE 'alter table CONFIGURATION drop constraint CONFIGURATION_FK';
    EXECUTE IMMEDIATE 'alter table DEPARTMENT_DECLARATION_TYPE drop constraint DEPT_DECL_TYPE_FK_DEPT';
    EXECUTE IMMEDIATE 'alter table DEPARTMENT_DECL_TYPE_PERFORMER drop constraint FK_DEPT_DECL_TYPE_PERF_PERF';
    EXECUTE IMMEDIATE 'alter table DEPARTMENT_FORM_TYPE drop constraint DEPT_FORM_TYPE_FK_DEP_ID';
    EXECUTE IMMEDIATE 'alter table DEPARTMENT_FORM_TYPE_PERFORMER drop constraint DEPT_FORM_TYPE_PERF_FK_PERF';
    EXECUTE IMMEDIATE 'alter table DEPARTMENT_REPORT_PERIOD drop constraint DEP_REP_PER_FK_DEPARTMENT_ID';
    EXECUTE IMMEDIATE 'alter table FORM_DATA_PERFORMER drop constraint FORMDATA_PERFORMER_FK_DEPT';
    EXECUTE IMMEDIATE 'alter table REF_BOOK_NDFL drop constraint FK_REF_BOOK_NDFL_DEPART';
    EXECUTE IMMEDIATE 'alter table REF_BOOK_NDFL_DETAIL drop constraint FK_REF_BOOK_NDFL_DET_DEPART';

    EXECUTE IMMEDIATE 'DROP SYNONYM DEPARTMENT';
    EXECUTE IMMEDIATE 'CREATE SYNONYM DEPARTMENT FOR TAX_1_5.DEPARTMENT';

    EXECUTE IMMEDIATE 'alter table NOTIFICATION add constraint NOTIFICATION_FK_RECEIVER foreign key (RECEIVER_DEPARTMENT_ID) references DEPARTMENT(ID)';
    EXECUTE IMMEDIATE 'alter table NOTIFICATION add constraint NOTIFICATION_FK_SENDER foreign key (SENDER_DEPARTMENT_ID) references DEPARTMENT(ID)';
    EXECUTE IMMEDIATE 'alter table CONFIGURATION add constraint CONFIGURATION_FK foreign key (DEPARTMENT_ID) references DEPARTMENT(ID)';
    EXECUTE IMMEDIATE 'alter table DEPARTMENT_DECLARATION_TYPE add constraint DEPT_DECL_TYPE_FK_DEPT foreign key (DEPARTMENT_ID) references DEPARTMENT(ID)';
    EXECUTE IMMEDIATE 'alter table DEPARTMENT_DECL_TYPE_PERFORMER add constraint FK_DEPT_DECL_TYPE_PERF_PERF foreign key (PERFORMER_DEP_ID) references DEPARTMENT(ID)';
    EXECUTE IMMEDIATE 'alter table DEPARTMENT_FORM_TYPE add constraint DEPT_FORM_TYPE_FK_DEP_ID foreign key (DEPARTMENT_ID) references DEPARTMENT(ID)';
    EXECUTE IMMEDIATE 'alter table DEPARTMENT_FORM_TYPE_PERFORMER add constraint DEPT_FORM_TYPE_PERF_FK_PERF foreign key (PERFORMER_DEP_ID) references DEPARTMENT(ID)';
    EXECUTE IMMEDIATE 'alter table DEPARTMENT_REPORT_PERIOD add constraint DEP_REP_PER_FK_DEPARTMENT_ID foreign key (DEPARTMENT_ID) references DEPARTMENT(ID)';
    EXECUTE IMMEDIATE 'alter table FORM_DATA_PERFORMER add constraint FORMDATA_PERFORMER_FK_DEPT foreign key (PRINT_DEPARTMENT_ID) references DEPARTMENT(ID)';
    EXECUTE IMMEDIATE 'alter table REF_BOOK_NDFL add constraint FK_REF_BOOK_NDFL_DEPART foreign key (DEPARTMENT_ID) references DEPARTMENT(ID)';
    EXECUTE IMMEDIATE 'alter table REF_BOOK_NDFL_DETAIL add constraint FK_REF_BOOK_NDFL_DET_DEPART foreign key (DEPARTMENT_ID) references DEPARTMENT(ID)';

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
  select count(1) into v_count from user_constraints where constraint_name='ROLE_EVENT_FK_ROLE_ID' AND table_name='ROLE_EVENT';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table ROLE_EVENT drop constraint ROLE_EVENT_FK_ROLE_ID';
  END IF;  

  select count(1) into v_count from user_constraints where constraint_name='NOTIFICATION_FK_NOTIFY_USER' AND table_name='NOTIFICATION';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table NOTIFICATION drop constraint NOTIFICATION_FK_NOTIFY_USER';
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
  select count(1) into v_count from user_constraints where constraint_name='TASK_CONTEXT_FK_USER_ID' AND table_name='TASK_CONTEXT';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table TASK_CONTEXT drop constraint TASK_CONTEXT_FK_USER_ID';
  END IF;
  select count(1) into v_count from user_constraints where constraint_name='TEMPLATE_CHANGES_FK_USER_ID' AND table_name='TEMPLATE_CHANGES';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table TEMPLATE_CHANGES drop constraint TEMPLATE_CHANGES_FK_USER_ID';
  END IF;

  EXECUTE IMMEDIATE 'drop synonym SEC_ROLE';
  EXECUTE IMMEDIATE 'CREATE SYNONYM SEC_ROLE FOR TAX_1_5.SEC_ROLE';

  EXECUTE IMMEDIATE 'drop synonym SEC_USER';
  EXECUTE IMMEDIATE 'CREATE SYNONYM SEC_USER FOR TAX_1_5.SEC_USER';

  EXECUTE IMMEDIATE 'drop synonym SEC_USER_ROLE';
  EXECUTE IMMEDIATE 'CREATE SYNONYM SEC_USER_ROLE FOR TAX_1_5.SEC_USER_ROLE';


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
  select count(1) into v_count from user_constraints where constraint_name='TASK_CONTEXT_FK_USER_ID' AND table_name='TASK_CONTEXT';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'alter table TASK_CONTEXT add constraint TASK_CONTEXT_FK_USER_ID foreign key (USER_ID) references SEC_USER(ID)';
  END IF;
  select count(1) into v_count from user_constraints where constraint_name='TEMPLATE_CHANGES_FK_USER_ID' AND table_name='TEMPLATE_CHANGES';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'alter table TEMPLATE_CHANGES add constraint TEMPLATE_CHANGES_FK_USER_ID foreign key (AUTHOR) references SEC_USER(ID)';
  END IF;

  EXECUTE IMMEDIATE 'drop synonym ADD_LOG_SYSTEM_NDFL';
  EXECUTE IMMEDIATE 'CREATE SYNONYM ADD_LOG_SYSTEM_NDFL FOR TAX_1_5.ADD_LOG_SYSTEM_NDFL';
END;

