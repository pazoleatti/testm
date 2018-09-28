declare 
  v_count number;
BEGIN
    EXECUTE IMMEDIATE 'alter table NOTIFICATION drop constraint NOTIFICATION_FK_RECEIVER';
    EXECUTE IMMEDIATE 'alter table NOTIFICATION drop constraint NOTIFICATION_FK_SENDER';
    EXECUTE IMMEDIATE 'alter table CONFIGURATION drop constraint CONFIGURATION_FK';
    EXECUTE IMMEDIATE 'alter table DEPARTMENT_DECLARATION_TYPE drop constraint DEPT_DECL_TYPE_FK_DEPT';
    EXECUTE IMMEDIATE 'alter table DEPARTMENT_DECL_TYPE_PERFORMER drop constraint FK_DEPT_DECL_TYPE_PERF_PERF';
    EXECUTE IMMEDIATE 'alter table DEPARTMENT_REPORT_PERIOD drop constraint DEP_REP_PER_FK_DEPARTMENT_ID';
    EXECUTE IMMEDIATE 'alter table REF_BOOK_NDFL_DETAIL drop constraint FK_REF_BOOK_NDFL_DET_DEPART';
    EXECUTE IMMEDIATE 'alter table REF_BOOK_PERSON_TB drop constraint PERSON_TB_FK_DEPARTMENT';
    EXECUTE IMMEDIATE 'alter table REF_BOOK_TB_PERSON drop constraint TB_PERSON_FK_DEPARTMENT';
	
    EXECUTE IMMEDIATE 'DROP SYNONYM DEPARTMENT';
    EXECUTE IMMEDIATE 'CREATE SYNONYM DEPARTMENT FOR TAX_1_9.DEPARTMENT';

    EXECUTE IMMEDIATE 'alter table NOTIFICATION add constraint NOTIFICATION_FK_RECEIVER foreign key (RECEIVER_DEPARTMENT_ID) references DEPARTMENT(ID)';
    EXECUTE IMMEDIATE 'alter table NOTIFICATION add constraint NOTIFICATION_FK_SENDER foreign key (SENDER_DEPARTMENT_ID) references DEPARTMENT(ID)';
    EXECUTE IMMEDIATE 'alter table CONFIGURATION add constraint CONFIGURATION_FK foreign key (DEPARTMENT_ID) references DEPARTMENT(ID)';
    EXECUTE IMMEDIATE 'alter table DEPARTMENT_DECLARATION_TYPE add constraint DEPT_DECL_TYPE_FK_DEPT foreign key (DEPARTMENT_ID) references DEPARTMENT(ID)';
    EXECUTE IMMEDIATE 'alter table DEPARTMENT_DECL_TYPE_PERFORMER add constraint FK_DEPT_DECL_TYPE_PERF_PERF foreign key (PERFORMER_DEP_ID) references DEPARTMENT(ID)';
    EXECUTE IMMEDIATE 'alter table DEPARTMENT_REPORT_PERIOD add constraint DEP_REP_PER_FK_DEPARTMENT_ID foreign key (DEPARTMENT_ID) references DEPARTMENT(ID)';
    EXECUTE IMMEDIATE 'alter table REF_BOOK_NDFL_DETAIL add constraint FK_REF_BOOK_NDFL_DET_DEPART foreign key (DEPARTMENT_ID) references DEPARTMENT(ID)';
    EXECUTE IMMEDIATE 'alter table REF_BOOK_PERSON_TB add constraint PERSON_TB_FK_DEPARTMENT foreign key (TB_DEPARTMENT_ID) references DEPARTMENT(ID)';
    EXECUTE IMMEDIATE 'alter table REF_BOOK_TB_PERSON add constraint TB_PERSON_FK_DEPARTMENT foreign key (TB_DEPARTMENT_ID) references DEPARTMENT(ID)';
	
	
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

  EXECUTE IMMEDIATE 'drop synonym SEC_ROLE';
  EXECUTE IMMEDIATE 'CREATE SYNONYM SEC_ROLE FOR TAX_1_9.SEC_ROLE';

  EXECUTE IMMEDIATE 'drop synonym SEC_USER_ROLE';
  EXECUTE IMMEDIATE 'CREATE SYNONYM SEC_USER_ROLE FOR TAX_1_9.SEC_USER_ROLE';

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

  EXECUTE IMMEDIATE 'drop synonym ADD_LOG_SYSTEM_NDFL';
  EXECUTE IMMEDIATE 'CREATE SYNONYM ADD_LOG_SYSTEM_NDFL FOR TAX_1_9.ADD_LOG_SYSTEM_NDFL';

  EXECUTE IMMEDIATE 'drop synonym SUBSYSTEM_ROLE';
  EXECUTE IMMEDIATE 'CREATE SYNONYM SUBSYSTEM_ROLE FOR TAX_1_9.SUBSYSTEM_ROLE';

  EXECUTE IMMEDIATE 'drop synonym VW_LOG_TABLE_CHANGE';
  EXECUTE IMMEDIATE 'CREATE SYNONYM VW_LOG_TABLE_CHANGE FOR TAX_1_9.VW_LOG_TABLE_CHANGE';
END;

