declare 
  v_count number;
  l_null user_tab_columns.nullable%type;
BEGIN
  delete from ref_book_attribute where ref_book_id in (960,961);
  delete from ref_book where id in (960,961);
  update ref_book_attribute set required=0 where id in (9501,9513);
  
  select count(1) into v_count from user_tables where table_name='REF_BOOK_FOND_DETAIL';
  IF v_count>0 THEN
	EXECUTE IMMEDIATE 'drop table REF_BOOK_FOND_DETAIL';
  END IF;
  
  select count(1) into v_count from user_tables where table_name='REF_BOOK_FOND_DETAIL';
  IF v_count>0 THEN
	EXECUTE IMMEDIATE 'drop table REF_BOOK_FOND';
  END IF;
  
  select nullable into l_null from user_tab_columns
  where table_name = 'REF_BOOK_NDFL_DETAIL' and column_name = 'DEPARTMENT_ID';
  
  if l_null = 'N' then
	EXECUTE IMMEDIATE 'ALTER TABLE REF_BOOK_NDFL_DETAIL MODIFY department_id NUMBER(18,0) NULL';
	EXECUTE IMMEDIATE 'alter table REF_BOOK_NDFL_DETAIL add constraint CHK_REF_BOOK_NDFL_DET_DEP check ((status=0 AND department_id is not null) OR status!=0)';
  end if;
  
  update REF_BOOK_NDFL_DETAIL set department_id=null where department_id=-1;
  update REF_BOOK_NDFL set department_id=null where department_id=-1;
  
  delete FROM DEPARTMENT_REPORT_PERIOD WHERE DEPARTMENT_ID=-1;
  
  FOR rec IN (
    select DISTINCT b.ID, b.NAME, b.PARENT_ID, b.TYPE, b.SHORTNAME, b.TB_INDEX, b.SBRF_CODE, 
      b.REGION_ID, b.IS_ACTIVE, b.CODE, b.GARANT_USE, b.SUNR_USE FROM (
    select RECEIVER_DEPARTMENT_ID FROM NOTIFICATION
    UNION ALL
    select SENDER_DEPARTMENT_ID FROM NOTIFICATION
    UNION ALL
    select DEPARTMENT_ID FROM CONFIGURATION
    UNION all
    select DEPARTMENT_ID FROM DEPARTMENT_DECLARATION_TYPE
    UNION all
    select PERFORMER_DEP_ID FROM DEPARTMENT_DECL_TYPE_PERFORMER
    UNION all
    select DEPARTMENT_ID FROM DEPARTMENT_REPORT_PERIOD
    UNION all
    select DEPARTMENT_ID FROM SEC_USER
    UNION all
    select DEPARTMENT_ID FROM DEPARTMENT_FORM_TYPE
    UNION all
    select PERFORMER_DEP_ID FROM DEPARTMENT_FORM_TYPE_PERFORMER
    UNION all
    select PRINT_DEPARTMENT_ID FROM FORM_DATA_PERFORMER
    UNION all
    select DEPARTMENT_ID FROM REF_BOOK_NDFL
    UNION all
    select DEPARTMENT_ID FROM REF_BOOK_NDFL_DETAIL
    ) a JOIN DEPARTMENT b ON a.RECEIVER_DEPARTMENT_ID=b.id
    WHERE NOT EXISTS(SELECT 1 FROM TAX.DEPARTMENT WHERE ID=b.id) AND b.ID!=-1
  )
  LOOP
    INSERT INTO TAX.DEPARTMENT(ID, NAME, PARENT_ID, TYPE, SHORTNAME, TB_INDEX, SBRF_CODE, REGION_ID, IS_ACTIVE, CODE, GARANT_USE, SUNR_USE)
    VALUES (rec.ID, rec.NAME, rec.PARENT_ID, rec.TYPE, rec.SHORTNAME, rec.TB_INDEX, rec.SBRF_CODE, rec.REGION_ID, rec.IS_ACTIVE, rec.CODE, rec.GARANT_USE, rec.SUNR_USE);
  END LOOP;
  
  select count(1) into v_count from user_synonyms where synonym_name='DEPARTMENT';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'alter table NOTIFICATION drop constraint NOTIFICATION_FK_RECEIVER';
    EXECUTE IMMEDIATE 'alter table NOTIFICATION drop constraint NOTIFICATION_FK_SENDER';
    EXECUTE IMMEDIATE 'alter table CONFIGURATION drop constraint CONFIGURATION_FK';
    EXECUTE IMMEDIATE 'alter table DEPARTMENT_DECLARATION_TYPE drop constraint DEPT_DECL_TYPE_FK_DEPT';
    EXECUTE IMMEDIATE 'alter table DEPARTMENT_DECL_TYPE_PERFORMER drop constraint FK_DEPT_DECL_TYPE_PERF_PERF';
    EXECUTE IMMEDIATE 'alter table DEPARTMENT_FORM_TYPE drop constraint DEPT_FORM_TYPE_FK_DEP_ID';
    EXECUTE IMMEDIATE 'alter table DEPARTMENT_FORM_TYPE_PERFORMER drop constraint DEPT_FORM_TYPE_PERF_FK_PERF';
    EXECUTE IMMEDIATE 'alter table DEPARTMENT_REPORT_PERIOD drop constraint DEP_REP_PER_FK_DEPARTMENT_ID';
    EXECUTE IMMEDIATE 'alter table FORM_DATA_PERFORMER drop constraint FORMDATA_PERFORMER_FK_DEPT';
    --EXECUTE IMMEDIATE 'alter table REF_BOOK_FOND drop constraint FK_REF_BOOK_FOND_DEPART';
    --EXECUTE IMMEDIATE 'alter table REF_BOOK_FOND_DETAIL drop constraint FK_REF_BOOK_FOND_DET_DEPART';
    EXECUTE IMMEDIATE 'alter table REF_BOOK_NDFL drop constraint FK_REF_BOOK_NDFL_DEPART';
    EXECUTE IMMEDIATE 'alter table REF_BOOK_NDFL_DETAIL drop constraint FK_REF_BOOK_NDFL_DET_DEPART';
    EXECUTE IMMEDIATE 'alter table SEC_USER drop constraint SEC_USER_FK_DEP_ID';

    EXECUTE IMMEDIATE 'alter table DEPARTMENT rename to DEPARTMENT_';
    EXECUTE IMMEDIATE 'CREATE SYNONYM DEPARTMENT FOR TAX.DEPARTMENT';

    EXECUTE IMMEDIATE 'alter table NOTIFICATION add constraint NOTIFICATION_FK_RECEIVER foreign key (RECEIVER_DEPARTMENT_ID) references DEPARTMENT(ID)';
    EXECUTE IMMEDIATE 'alter table NOTIFICATION add constraint NOTIFICATION_FK_SENDER foreign key (SENDER_DEPARTMENT_ID) references DEPARTMENT(ID)';
    EXECUTE IMMEDIATE 'alter table CONFIGURATION add constraint CONFIGURATION_FK foreign key (DEPARTMENT_ID) references DEPARTMENT(ID)';
    EXECUTE IMMEDIATE 'alter table DEPARTMENT_DECLARATION_TYPE add constraint DEPT_DECL_TYPE_FK_DEPT foreign key (DEPARTMENT_ID) references DEPARTMENT(ID)';
    EXECUTE IMMEDIATE 'alter table DEPARTMENT_DECL_TYPE_PERFORMER add constraint FK_DEPT_DECL_TYPE_PERF_PERF foreign key (PERFORMER_DEP_ID) references DEPARTMENT(ID)';
    EXECUTE IMMEDIATE 'alter table DEPARTMENT_FORM_TYPE add constraint DEPT_FORM_TYPE_FK_DEP_ID foreign key (DEPARTMENT_ID) references DEPARTMENT(ID)';
    EXECUTE IMMEDIATE 'alter table DEPARTMENT_FORM_TYPE_PERFORMER add constraint DEPT_FORM_TYPE_PERF_FK_PERF foreign key (PERFORMER_DEP_ID) references DEPARTMENT(ID)';
    EXECUTE IMMEDIATE 'alter table DEPARTMENT_REPORT_PERIOD add constraint DEP_REP_PER_FK_DEPARTMENT_ID foreign key (DEPARTMENT_ID) references DEPARTMENT(ID)';
    EXECUTE IMMEDIATE 'alter table FORM_DATA_PERFORMER add constraint FORMDATA_PERFORMER_FK_DEPT foreign key (PRINT_DEPARTMENT_ID) references DEPARTMENT(ID)';
    --EXECUTE IMMEDIATE 'alter table REF_BOOK_FOND add constraint FK_REF_BOOK_FOND_DEPART foreign key (DEPARTMENT_ID) references DEPARTMENT(ID)';
    --EXECUTE IMMEDIATE 'alter table REF_BOOK_FOND_DETAIL add constraint FK_REF_BOOK_FOND_DET_DEPART foreign key (DEPARTMENT_ID) references DEPARTMENT(ID)';
    EXECUTE IMMEDIATE 'alter table REF_BOOK_NDFL add constraint FK_REF_BOOK_NDFL_DEPART foreign key (DEPARTMENT_ID) references DEPARTMENT(ID)';
    EXECUTE IMMEDIATE 'alter table REF_BOOK_NDFL_DETAIL add constraint FK_REF_BOOK_NDFL_DET_DEPART foreign key (DEPARTMENT_ID) references DEPARTMENT(ID)';
    EXECUTE IMMEDIATE 'alter table SEC_USER add constraint SEC_USER_FK_DEP_ID foreign key (DEPARTMENT_ID) references DEPARTMENT(ID)';
  END IF;
END;
/