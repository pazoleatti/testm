declare 
  v_count number;
BEGIN
  select count(1) into v_count from user_constraints where constraint_name='NOTIFICATION_FK_RECEIVER' AND table_name='NOTIFICATION';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table NOTIFICATION drop constraint NOTIFICATION_FK_RECEIVER';
	  dbms_output.put_line('Block 1. alter table NOTIFICATION drop constraint NOTIFICATION_FK_RECEIVER:'||' Success');
  END IF;  

  select count(1) into v_count from user_constraints where constraint_name='NOTIFICATION_FK_SENDER' AND table_name='NOTIFICATION';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table NOTIFICATION drop constraint NOTIFICATION_FK_SENDER';
	  dbms_output.put_line('Block 2. lter table NOTIFICATION drop constraint NOTIFICATION_FK_SENDER:'||' Success');
  END IF;  

  select count(1) into v_count from user_constraints where constraint_name='CONFIGURATION_FK' AND table_name='CONFIGURATION';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table CONFIGURATION drop constraint CONFIGURATION_FK';
	  dbms_output.put_line('Block 3. alter table CONFIGURATION drop constraint CONFIGURATION_FK:'||' Success');
  END IF;  

  select count(1) into v_count from user_constraints where constraint_name='DEPT_DECL_TYPE_FK_DEPT' AND table_name='DEPARTMENT_DECLARATION_TYPE';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table DEPARTMENT_DECLARATION_TYPE drop constraint DEPT_DECL_TYPE_FK_DEPT';
	  dbms_output.put_line('Block 4. alter table DEPARTMENT_DECLARATION_TYPE drop constraint DEPT_DECL_TYPE_FK_DEPT:'||' Success');
  END IF;  

  select count(1) into v_count from user_constraints where constraint_name='FK_DEPT_DECL_TYPE_PERF_PERF' AND table_name='DEPARTMENT_DECL_TYPE_PERFORMER';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table DEPARTMENT_DECL_TYPE_PERFORMER drop constraint FK_DEPT_DECL_TYPE_PERF_PERF';
	  dbms_output.put_line('Block 5. alter table DEPARTMENT_DECL_TYPE_PERFORMER drop constraint FK_DEPT_DECL_TYPE_PERF_PERF:'||' Success');
  END IF;  

  select count(1) into v_count from user_constraints where constraint_name='DEP_REP_PER_FK_DEPARTMENT_ID' AND table_name='DEPARTMENT_REPORT_PERIOD';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table DEPARTMENT_REPORT_PERIOD drop constraint DEP_REP_PER_FK_DEPARTMENT_ID';
	  dbms_output.put_line('Block 6. alter table DEPARTMENT_REPORT_PERIOD drop constraint DEP_REP_PER_FK_DEPARTMENT_ID:'||' Success');
  END IF;  

  select count(1) into v_count from user_constraints where constraint_name='FK_REF_BOOK_NDFL_DET_DEPART' AND table_name='REF_BOOK_NDFL_DETAIL';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table REF_BOOK_NDFL_DETAIL drop constraint FK_REF_BOOK_NDFL_DET_DEPART';
	  dbms_output.put_line('Block 7. alter table REF_BOOK_NDFL_DETAIL drop constraint FK_REF_BOOK_NDFL_DET_DEPART:'||' Success');
  END IF;  

  select count(1) into v_count from user_constraints where constraint_name='PERSON_TB_FK_DEPARTMENT' AND table_name='REF_BOOK_PERSON_TB';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table REF_BOOK_PERSON_TB drop constraint PERSON_TB_FK_DEPARTMENT';
	  dbms_output.put_line('Block 8. alter table REF_BOOK_PERSON_TB drop constraint PERSON_TB_FK_DEPARTMENT:'||' Success');
  END IF;  

  select count(1) into v_count from user_constraints where constraint_name='TB_PERSON_FK_DEPARTMENT' AND table_name='REF_BOOK_TB_PERSON';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table REF_BOOK_TB_PERSON drop constraint TB_PERSON_FK_DEPARTMENT';
	  dbms_output.put_line('Block 9. alter table REF_BOOK_TB_PERSON drop constraint TB_PERSON_FK_DEPARTMENT:'||' Success');
  END IF;  

  select count(1) into v_count from user_synonyms	us where us.SYNONYM_NAME='DEPARTMENT' and us.TABLE_OWNER='TAX_1_9' and us.TABLE_NAME='DEPARTMENT';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'DROP SYNONYM DEPARTMENT';
	  dbms_output.put_line('Block 10. DROP SYNONYM DEPARTMENT:'||' Success');
  END IF;  
  select count(1) into v_count from user_synonyms	us where us.SYNONYM_NAME='DEPARTMENT' and us.TABLE_OWNER='TAX_1_9_1' and us.TABLE_NAME='DEPARTMENT';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'CREATE SYNONYM DEPARTMENT FOR TAX_1_9_1.DEPARTMENT';
	  dbms_output.put_line('Block 11. CREATE SYNONYM DEPARTMENT FOR TAX_1_9_1.DEPARTMENT:'||' Success');
  END IF;  

  select count(1) into v_count from user_constraints where constraint_name='NOTIFICATION_FK_RECEIVER' AND table_name='NOTIFICATION';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'alter table NOTIFICATION add constraint NOTIFICATION_FK_RECEIVER foreign key (RECEIVER_DEPARTMENT_ID) references DEPARTMENT(ID)';
	  dbms_output.put_line('Block 12. add constraint NOTIFICATION_FK_RECEIVER:'||' Success');
  END IF;  

  select count(1) into v_count from user_constraints where constraint_name='NOTIFICATION_FK_SENDER' AND table_name='NOTIFICATION';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'alter table NOTIFICATION add constraint NOTIFICATION_FK_SENDER foreign key (SENDER_DEPARTMENT_ID) references DEPARTMENT(ID)';
	  dbms_output.put_line('Block 13. add constraint NOTIFICATION_FK_SENDER:'||' Success');
  END IF;  

  select count(1) into v_count from user_constraints where constraint_name='CONFIGURATION_FK' AND table_name='CONFIGURATION';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'alter table CONFIGURATION add constraint CONFIGURATION_FK foreign key (DEPARTMENT_ID) references DEPARTMENT(ID)';
	  dbms_output.put_line('Block 14. add constraint CONFIGURATION_FK:'||' Success');
  END IF;  

  select count(1) into v_count from user_constraints where constraint_name='DEPT_DECL_TYPE_FK_DEPT' AND table_name='DEPARTMENT_DECLARATION_TYPE';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'alter table DEPARTMENT_DECLARATION_TYPE add constraint DEPT_DECL_TYPE_FK_DEPT foreign key (DEPARTMENT_ID) references DEPARTMENT(ID)';
	  dbms_output.put_line('Block 15. add constraint DEPT_DECL_TYPE_FK_DEPT:'||' Success');
  END IF;  

  select count(1) into v_count from user_constraints where constraint_name='FK_DEPT_DECL_TYPE_PERF_PERF' AND table_name='DEPARTMENT_DECL_TYPE_PERFORMER';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'alter table DEPARTMENT_DECL_TYPE_PERFORMER add constraint FK_DEPT_DECL_TYPE_PERF_PERF foreign key (PERFORMER_DEP_ID) references DEPARTMENT(ID)';
	  dbms_output.put_line('Block 16. add constraint FK_DEPT_DECL_TYPE_PERF_PERF:'||' Success');
  END IF;  

  select count(1) into v_count from user_constraints where constraint_name='DEP_REP_PER_FK_DEPARTMENT_ID' AND table_name='DEPARTMENT_REPORT_PERIOD';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'alter table DEPARTMENT_REPORT_PERIOD add constraint DEP_REP_PER_FK_DEPARTMENT_ID foreign key (DEPARTMENT_ID) references DEPARTMENT(ID)';
	  dbms_output.put_line('Block 17. add constraint DEP_REP_PER_FK_DEPARTMENT_ID:'||' Success');
  END IF;  

  select count(1) into v_count from user_constraints where constraint_name='FK_REF_BOOK_NDFL_DET_DEPART' AND table_name='REF_BOOK_NDFL_DETAIL';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'alter table REF_BOOK_NDFL_DETAIL add constraint FK_REF_BOOK_NDFL_DET_DEPART foreign key (DEPARTMENT_ID) references DEPARTMENT(ID)';
	  dbms_output.put_line('Block 18. add constraint FK_REF_BOOK_NDFL_DET_DEPART:'||' Success');
  END IF;  

  select count(1) into v_count from user_constraints where constraint_name='PERSON_TB_FK_DEPARTMENT' AND table_name='REF_BOOK_PERSON_TB';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'alter table REF_BOOK_PERSON_TB add constraint PERSON_TB_FK_DEPARTMENT foreign key (TB_DEPARTMENT_ID) references DEPARTMENT(ID)';
	  dbms_output.put_line('Block 19.'||' Success');
  END IF;  

  select count(1) into v_count from user_constraints where constraint_name='TB_PERSON_FK_DEPARTMENT' AND table_name='REF_BOOK_TB_PERSON';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'alter table REF_BOOK_TB_PERSON add constraint TB_PERSON_FK_DEPARTMENT foreign key (TB_DEPARTMENT_ID) references DEPARTMENT(ID)';
	  dbms_output.put_line('Block 20. add constraint PERSON_TB_FK_DEPARTMENT:'||' Success');
  END IF;  
	
  select count(1) into v_count from user_constraints where constraint_name='NOTIFICATION_FK_NOTIFY_ROLE' AND table_name='NOTIFICATION';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table NOTIFICATION drop constraint NOTIFICATION_FK_NOTIFY_ROLE';
	  dbms_output.put_line('Block 21. alter table NOTIFICATION drop constraint NOTIFICATION_FK_NOTIFY_ROLE:'||' Success');
  END IF;  
  select count(1) into v_count from user_constraints where constraint_name='FK_REF_BOOK_ASNU_ROLE_ALIAS' AND table_name='REF_BOOK_ASNU';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table REF_BOOK_ASNU drop constraint FK_REF_BOOK_ASNU_ROLE_ALIAS';
	  dbms_output.put_line('Block 22. alter table REF_BOOK_ASNU drop constraint FK_REF_BOOK_ASNU_ROLE_ALIAS:'||' Success');
  END IF;  
  select count(1) into v_count from user_constraints where constraint_name='FK_REF_BOOK_ASNU_ROLE_NAME' AND table_name='REF_BOOK_ASNU';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'alter table REF_BOOK_ASNU drop constraint FK_REF_BOOK_ASNU_ROLE_NAME';
	  dbms_output.put_line('Block 23. alter table REF_BOOK_ASNU drop constraint FK_REF_BOOK_ASNU_ROLE_NAME:'||' Success');
  END IF;  

  select count(1) into v_count from user_synonyms	us where us.SYNONYM_NAME='SEC_ROLE' and us.TABLE_OWNER='TAX_1_9' and us.TABLE_NAME='SEC_ROLE';
  IF v_count>0 THEN
     EXECUTE IMMEDIATE 'drop synonym SEC_ROLE';
	  dbms_output.put_line('Block 24. drop synonym SEC_ROLE:'||' Success');
  END IF;  
  select count(1) into v_count from user_synonyms	us where us.SYNONYM_NAME='SEC_ROLE' and us.TABLE_OWNER='TAX_1_9_1' and us.TABLE_NAME='SEC_ROLE';
  IF v_count=0 THEN
     EXECUTE IMMEDIATE 'CREATE SYNONYM SEC_ROLE FOR TAX_1_9_1.SEC_ROLE';
	  dbms_output.put_line('Block 25. CREATE SYNONYM SEC_ROLE FOR TAX_1_9_1.SEC_ROLE:'||' Success');
  END IF;  

  select count(1) into v_count from user_synonyms	us where us.SYNONYM_NAME='SEC_USER_ROLE' and us.TABLE_OWNER='TAX_1_9' and us.TABLE_NAME='SEC_USER_ROLE';
  IF v_count>0 THEN
     EXECUTE IMMEDIATE 'drop synonym SEC_USER_ROLE';
	  dbms_output.put_line('Block 26. drop synonym SEC_USER_ROLE:'||' Success');
  END IF;  
  select count(1) into v_count from user_synonyms	us where us.SYNONYM_NAME='SEC_USER_ROLE' and us.TABLE_OWNER='TAX_1_9_1' and us.TABLE_NAME='SEC_USER_ROLE';
  IF v_count=0 THEN
     EXECUTE IMMEDIATE 'CREATE SYNONYM SEC_USER_ROLE FOR TAX_1_9_1.SEC_USER_ROLE';
	  dbms_output.put_line('Block 27. CREATE SYNONYM SEC_USER_ROLE FOR TAX_1_9_1.SEC_USER_ROLE:'||' Success');
  END IF;  

  select count(1) into v_count from user_constraints where constraint_name='NOTIFICATION_FK_NOTIFY_ROLE' AND table_name='NOTIFICATION';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'alter table NOTIFICATION add constraint NOTIFICATION_FK_NOTIFY_ROLE foreign key (ROLE_ID) references SEC_ROLE(ID)';
	  dbms_output.put_line('Block 28. add constraint NOTIFICATION_FK_NOTIFY_ROLE:'||' Success');
  END IF;
  select count(1) into v_count from user_constraints where constraint_name='FK_REF_BOOK_ASNU_ROLE_ALIAS' AND table_name='REF_BOOK_ASNU';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'alter table REF_BOOK_ASNU add constraint FK_REF_BOOK_ASNU_ROLE_ALIAS foreign key (ROLE_ALIAS) references SEC_ROLE(ID)';
	  dbms_output.put_line('Block 29. add constraint FK_REF_BOOK_ASNU_ROLE_ALIAS:'||' Success');
  END IF;
  select count(1) into v_count from user_constraints where constraint_name='FK_REF_BOOK_ASNU_ROLE_NAME' AND table_name='REF_BOOK_ASNU';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'alter table REF_BOOK_ASNU add constraint FK_REF_BOOK_ASNU_ROLE_NAME foreign key (ROLE_NAME) references SEC_ROLE(ID)';
	  dbms_output.put_line('Block 30. add constraint FK_REF_BOOK_ASNU_ROLE_NAME:'||' Success');
  END IF;

  select count(1) into v_count from user_synonyms	us where us.SYNONYM_NAME='ADD_LOG_SYSTEM_NDFL' and us.TABLE_OWNER='TAX_1_9' and us.TABLE_NAME='ADD_LOG_SYSTEM_NDFL';
  IF v_count>0 THEN
     EXECUTE IMMEDIATE 'drop synonym ADD_LOG_SYSTEM_NDFL';
	  dbms_output.put_line('Block 31. drop synonym ADD_LOG_SYSTEM_NDFL:'||' Success');
  END IF;
  select count(1) into v_count from user_synonyms	us where us.SYNONYM_NAME='ADD_LOG_SYSTEM_NDFL' and us.TABLE_OWNER='TAX_1_9_1' and us.TABLE_NAME='ADD_LOG_SYSTEM_NDFL';
  IF v_count=0 THEN
     EXECUTE IMMEDIATE 'CREATE SYNONYM ADD_LOG_SYSTEM_NDFL FOR TAX_1_9_1.ADD_LOG_SYSTEM_NDFL';
	  dbms_output.put_line('Block 32. CREATE SYNONYM ADD_LOG_SYSTEM_NDFL FOR TAX_1_9_1.ADD_LOG_SYSTEM_NDFL:'||' Success');
  END IF;

  select count(1) into v_count from user_synonyms	us where us.SYNONYM_NAME='SUBSYSTEM_ROLE' and us.TABLE_OWNER='TAX_1_9' and us.TABLE_NAME='SUBSYSTEM_ROLE';
  IF v_count>0 THEN
     EXECUTE IMMEDIATE 'drop synonym SUBSYSTEM_ROLE';
	  dbms_output.put_line('Block 33. drop synonym SUBSYSTEM_ROLE:'||' Success');
  END IF;
  select count(1) into v_count from user_synonyms	us where us.SYNONYM_NAME='SUBSYSTEM_ROLE' and us.TABLE_OWNER='TAX_1_9_1' and us.TABLE_NAME='SUBSYSTEM_ROLE';
  IF v_count=0 THEN
     EXECUTE IMMEDIATE 'CREATE SYNONYM SUBSYSTEM_ROLE FOR TAX_1_9_1.SUBSYSTEM_ROLE';
	  dbms_output.put_line('Block 34. CREATE SYNONYM SUBSYSTEM_ROLE FOR TAX_1_9_1.SUBSYSTEM_ROLE:'||' Success');
  END IF;

  select count(1) into v_count from user_synonyms	us where us.SYNONYM_NAME='VW_LOG_TABLE_CHANGE' and us.TABLE_OWNER='TAX_1_9' and us.TABLE_NAME='VW_LOG_TABLE_CHANGE';
  IF v_count>0 THEN
     EXECUTE IMMEDIATE 'drop synonym VW_LOG_TABLE_CHANGE';
	  dbms_output.put_line('Block 35. drop synonym VW_LOG_TABLE_CHANGE:'||' Success');
  END IF;
  select count(1) into v_count from user_synonyms	us where us.SYNONYM_NAME='VW_LOG_TABLE_CHANGE' and us.TABLE_OWNER='TAX_1_9_1' and us.TABLE_NAME='VW_LOG_TABLE_CHANGE';
  IF v_count=0 THEN
      EXECUTE IMMEDIATE 'CREATE SYNONYM VW_LOG_TABLE_CHANGE FOR TAX_1_9_1.VW_LOG_TABLE_CHANGE';
	  dbms_output.put_line('Block 36. CREATE SYNONYM VW_LOG_TABLE_CHANGE FOR TAX_1_9_1.VW_LOG_TABLE_CHANGE:'||' Success');
  END IF;
  
END;

