DECLARE
	v_count number;
BEGIN

	select count(1) into v_count from user_constraints where table_name='DEPARTMENT_DECLARATION_TYPE' and constraint_name='DEPT_DECL_TYPE_FK_DEPT';
	IF v_count=0 THEN
		dbms_output.put_line('Constraint DEPT_DECL_TYPE_FK_DEPT on table DEPARTMENT_DECLARATION_TYPE not exists');
		dbms_output.put_line('Add Constraint DEPT_DECL_TYPE_FK_DEPT on table DEPARTMENT_DECLARATION_TYPE');
		EXECUTE IMMEDIATE 'ALTER TABLE DEPARTMENT_DECLARATION_TYPE ADD CONSTRAINT DEPT_DECL_TYPE_FK_DEPT FOREIGN KEY (DEPARTMENT_ID) REFERENCES DEPARTMENT (ID) ENABLE';
		
		select count(1) into v_count from user_constraints where table_name='DEPARTMENT_DECLARATION_TYPE' and constraint_name='DEPT_DECL_TYPE_FK_DEPT';
		IF v_count>0 THEN
			dbms_output.put_line('Constraint DEPT_DECL_TYPE_FK_DEPT on table DEPARTMENT_DECLARATION_TYPE was created');
		END IF;
	else
		dbms_output.put_line('Constraint DEPT_DECL_TYPE_FK_DEPT on table DEPARTMENT_DECLARATION_TYPE already exists');
	END IF; 
	
	dbms_output.put_line('---------------------------------');
	
	select count(1) into v_count from user_constraints where table_name='DEPARTMENT_REPORT_PERIOD' and constraint_name='DEP_REP_PER_FK_DEPARTMENT_ID';
	IF v_count=0 THEN
		dbms_output.put_line('Constraint DEP_REP_PER_FK_DEPARTMENT_ID on table DEPARTMENT_REPORT_PERIOD not exists');
		dbms_output.put_line('Add Constraint DEP_REP_PER_FK_DEPARTMENT_ID on table DEPARTMENT_REPORT_PERIOD');
		EXECUTE IMMEDIATE 'ALTER TABLE DEPARTMENT_REPORT_PERIOD ADD CONSTRAINT DEP_REP_PER_FK_DEPARTMENT_ID FOREIGN KEY (DEPARTMENT_ID) REFERENCES DEPARTMENT (ID) ENABLE';
		
		select count(1) into v_count from user_constraints where table_name='DEPARTMENT_REPORT_PERIOD' and constraint_name='DEP_REP_PER_FK_DEPARTMENT_ID';
		IF v_count>0 THEN
			dbms_output.put_line('Constraint DEP_REP_PER_FK_DEPARTMENT_ID on table DEPARTMENT_REPORT_PERIOD was created');
		END IF;
	else
		dbms_output.put_line('Constraint DEP_REP_PER_FK_DEPARTMENT_ID on table DEPARTMENT_REPORT_PERIOD already exists');
	END IF; 
	
	dbms_output.put_line('---------------------------------');
	
	select count(1) into v_count from user_constraints where table_name='NOTIFICATION' and constraint_name='NOTIFICATION_FK_RECEIVER';
	IF v_count=0 THEN
		dbms_output.put_line('Constraint NOTIFICATION_FK_RECEIVER on table NOTIFICATION not exists');
		dbms_output.put_line('Add Constraint NOTIFICATION_FK_RECEIVER on table NOTIFICATION');
		EXECUTE IMMEDIATE 'ALTER TABLE NOTIFICATION ADD CONSTRAINT NOTIFICATION_FK_RECEIVER FOREIGN KEY (RECEIVER_DEPARTMENT_ID) REFERENCES DEPARTMENT (ID) ENABLE';
		
		select count(1) into v_count from user_constraints where table_name='NOTIFICATION' and constraint_name='NOTIFICATION_FK_RECEIVER';
		IF v_count>0 THEN
			dbms_output.put_line('Constraint NOTIFICATION_FK_RECEIVER on table NOTIFICATION was created');
		END IF;
	else
		dbms_output.put_line('Constraint NOTIFICATION_FK_RECEIVER on table NOTIFICATION already exists');
	END IF; 
	
	dbms_output.put_line('---------------------------------');
	
	select count(1) into v_count from user_constraints where table_name='NOTIFICATION' and constraint_name='NOTIFICATION_FK_SENDER';
	IF v_count=0 THEN
		dbms_output.put_line('Constraint NOTIFICATION_FK_SENDER on table NOTIFICATION not exists');
		dbms_output.put_line('Add Constraint NOTIFICATION_FK_SENDER on table NOTIFICATION');
		EXECUTE IMMEDIATE 'ALTER TABLE NOTIFICATION ADD CONSTRAINT NOTIFICATION_FK_SENDER FOREIGN KEY (SENDER_DEPARTMENT_ID) REFERENCES DEPARTMENT (ID) ENABLE';
		
		select count(1) into v_count from user_constraints where table_name='NOTIFICATION' and constraint_name='NOTIFICATION_FK_SENDER';
		IF v_count>0 THEN
			dbms_output.put_line('Constraint NOTIFICATION_FK_SENDER on table NOTIFICATION was created');
		END IF;
	else
		dbms_output.put_line('Constraint NOTIFICATION_FK_SENDER on table NOTIFICATION already exists');
	END IF; 
	
	dbms_output.put_line('---------------------------------');
	
	select count(1) into v_count from user_constraints where table_name='NOTIFICATION' and constraint_name='NOTIFICATION_FK_NOTIFY_ROLE';
	IF v_count=0 THEN
		dbms_output.put_line('Constraint NOTIFICATION_FK_NOTIFY_ROLE on table NOTIFICATION not exists');
		dbms_output.put_line('Add Constraint NOTIFICATION_FK_NOTIFY_ROLE on table NOTIFICATION');
		EXECUTE IMMEDIATE 'ALTER TABLE NOTIFICATION ADD CONSTRAINT NOTIFICATION_FK_NOTIFY_ROLE FOREIGN KEY (ROLE_ID) REFERENCES SEC_ROLE (ID) ENABLE';
		
		select count(1) into v_count from user_constraints where table_name='NOTIFICATION' and constraint_name='NOTIFICATION_FK_NOTIFY_ROLE';
		IF v_count>0 THEN
			dbms_output.put_line('Constraint NOTIFICATION_FK_NOTIFY_ROLE on table NOTIFICATION was created');
		END IF;
	else
		dbms_output.put_line('Constraint NOTIFICATION_FK_NOTIFY_ROLE on table NOTIFICATION already exists');
	END IF; 
	
	dbms_output.put_line('---------------------------------');
	
	select count(1) into v_count from user_constraints where table_name='NOTIFICATION' and constraint_name='NOTIFICATION_FK_NOTIFY_USER';
	IF v_count=0 THEN
		dbms_output.put_line('Constraint NOTIFICATION_FK_NOTIFY_USER on table NOTIFICATION not exists');
		dbms_output.put_line('Add Constraint NOTIFICATION_FK_NOTIFY_USER on table NOTIFICATION');
		EXECUTE IMMEDIATE 'ALTER TABLE NOTIFICATION ADD CONSTRAINT NOTIFICATION_FK_NOTIFY_USER FOREIGN KEY (USER_ID) REFERENCES SEC_USER (ID) ENABLE';
		
		select count(1) into v_count from user_constraints where table_name='NOTIFICATION' and constraint_name='NOTIFICATION_FK_NOTIFY_USER';
		IF v_count>0 THEN
			dbms_output.put_line('Constraint NOTIFICATION_FK_NOTIFY_USER on table NOTIFICATION was created');
		END IF;
	else
		dbms_output.put_line('Constraint NOTIFICATION_FK_NOTIFY_USER on table NOTIFICATION already exists');
	END IF; 
	
	dbms_output.put_line('---------------------------------');
	
	select count(1) into v_count from user_constraints where table_name='TEMPLATE_CHANGES' and constraint_name='TEMPLATE_CHANGES_FK_USER_ID';
	IF v_count=0 THEN
		dbms_output.put_line('Constraint TEMPLATE_CHANGES_FK_USER_ID on table TEMPLATE_CHANGES not exists');
		dbms_output.put_line('Add Constraint TEMPLATE_CHANGES_FK_USER_ID on table TEMPLATE_CHANGES');
		EXECUTE IMMEDIATE 'ALTER TABLE TEMPLATE_CHANGES ADD CONSTRAINT TEMPLATE_CHANGES_FK_USER_ID FOREIGN KEY (AUTHOR) REFERENCES SEC_USER (ID) ENABLE';
		
		select count(1) into v_count from user_constraints where table_name='TEMPLATE_CHANGES' and constraint_name='TEMPLATE_CHANGES_FK_USER_ID';
		IF v_count>0 THEN
			dbms_output.put_line('Constraint TEMPLATE_CHANGES_FK_USER_ID on table TEMPLATE_CHANGES was created');
		END IF;
	else
		dbms_output.put_line('Constraint TEMPLATE_CHANGES_FK_USER_ID on table TEMPLATE_CHANGES already exists');
	END IF; 
	
	dbms_output.put_line('---------------------------------');
	
	select count(1) into v_count from user_constraints where table_name='LOCK_DATA' and constraint_name='LOCK_DATA_FK_USER_ID';
	IF v_count=0 THEN
		dbms_output.put_line('Constraint LOCK_DATA_FK_USER_ID on table LOCK_DATA not exists');
		dbms_output.put_line('Add Constraint LOCK_DATA_FK_USER_ID on table LOCK_DATA');
		EXECUTE IMMEDIATE 'ALTER TABLE LOCK_DATA ADD CONSTRAINT LOCK_DATA_FK_USER_ID FOREIGN KEY (USER_ID) REFERENCES SEC_USER (ID) ENABLE';
		
		select count(1) into v_count from user_constraints where table_name='LOCK_DATA' and constraint_name='LOCK_DATA_FK_USER_ID';
		IF v_count>0 THEN
			dbms_output.put_line('Constraint LOCK_DATA_FK_USER_ID on table LOCK_DATA was created');
		END IF;
	else
		dbms_output.put_line('Constraint LOCK_DATA_FK_USER_ID on table LOCK_DATA already exists');
	END IF; 
	
	dbms_output.put_line('---------------------------------');
	
	select count(1) into v_count from user_constraints where table_name='ASYNC_TASK_SUBSCRIBERS' and constraint_name='ASYNC_T_SUBSCR_FK_SEC_USER';
	IF v_count=0 THEN
		dbms_output.put_line('Constraint ASYNC_T_SUBSCR_FK_SEC_USER on table ASYNC_TASK_SUBSCRIBERS not exists');
		dbms_output.put_line('Add Constraint ASYNC_T_SUBSCR_FK_SEC_USER on table ASYNC_TASK_SUBSCRIBERS');
		EXECUTE IMMEDIATE 'ALTER TABLE ASYNC_TASK_SUBSCRIBERS ADD CONSTRAINT ASYNC_T_SUBSCR_FK_SEC_USER FOREIGN KEY (USER_ID) REFERENCES SEC_USER (ID) ENABLE';
		
		select count(1) into v_count from user_constraints where table_name='ASYNC_TASK_SUBSCRIBERS' and constraint_name='ASYNC_T_SUBSCR_FK_SEC_USER';
		IF v_count>0 THEN
			dbms_output.put_line('Constraint ASYNC_T_SUBSCR_FK_SEC_USER on table ASYNC_TASK_SUBSCRIBERS was created');
		END IF;
	else
		dbms_output.put_line('Constraint ASYNC_T_SUBSCR_FK_SEC_USER on table ASYNC_TASK_SUBSCRIBERS already exists');
	END IF; 
	
	dbms_output.put_line('---------------------------------');
	
	select count(1) into v_count from user_constraints where table_name='LOG' and constraint_name='FK_LOG_USER';
	IF v_count=0 THEN
		dbms_output.put_line('Constraint FK_LOG_USER on table LOG not exists');
		dbms_output.put_line('Add Constraint FK_LOG_USER on table LOG');
		EXECUTE IMMEDIATE 'ALTER TABLE LOG ADD CONSTRAINT FK_LOG_USER FOREIGN KEY (USER_ID) REFERENCES SEC_USER (ID) ENABLE';
		
		select count(1) into v_count from user_constraints where table_name='LOG' and constraint_name='FK_LOG_USER';
		IF v_count>0 THEN
			dbms_output.put_line('Constraint FK_LOG_USER on table LOG was created');
		END IF;
	else
		dbms_output.put_line('Constraint FK_LOG_USER on table LOG already exists');
	END IF; 
	
	dbms_output.put_line('---------------------------------');
	
	select count(1) into v_count from user_constraints where table_name='DEPARTMENT_DECL_TYPE_PERFORMER' and constraint_name='FK_DEPT_DECL_TYPE_PERF_PERF';
	IF v_count=0 THEN
		dbms_output.put_line('Constraint FK_DEPT_DECL_TYPE_PERF_PERF on table DEPARTMENT_DECL_TYPE_PERFORMER not exists');
		dbms_output.put_line('Add Constraint FK_DEPT_DECL_TYPE_PERF_PERF on table DEPARTMENT_DECL_TYPE_PERFORMER');
		EXECUTE IMMEDIATE 'ALTER TABLE DEPARTMENT_DECL_TYPE_PERFORMER ADD CONSTRAINT FK_DEPT_DECL_TYPE_PERF_PERF FOREIGN KEY (PERFORMER_DEP_ID) REFERENCES DEPARTMENT (ID) ENABLE';
		
		select count(1) into v_count from user_constraints where table_name='DEPARTMENT_DECL_TYPE_PERFORMER' and constraint_name='FK_DEPT_DECL_TYPE_PERF_PERF';
		IF v_count>0 THEN
			dbms_output.put_line('Constraint FK_DEPT_DECL_TYPE_PERF_PERF on table DEPARTMENT_DECL_TYPE_PERFORMER was created');
		END IF;
	else
		dbms_output.put_line('Constraint FK_DEPT_DECL_TYPE_PERF_PERF on table DEPARTMENT_DECL_TYPE_PERFORMER already exists');
	END IF; 
	
	dbms_output.put_line('---------------------------------');
	
	select count(1) into v_count from user_constraints where table_name='REF_BOOK_ASNU' and constraint_name='FK_REF_BOOK_ASNU_ROLE_ALIAS';
	IF v_count=0 THEN
		dbms_output.put_line('Constraint FK_REF_BOOK_ASNU_ROLE_ALIAS on table REF_BOOK_ASNU not exists');
		dbms_output.put_line('Add Constraint FK_REF_BOOK_ASNU_ROLE_ALIAS on table REF_BOOK_ASNU');
		EXECUTE IMMEDIATE 'ALTER TABLE REF_BOOK_ASNU ADD CONSTRAINT FK_REF_BOOK_ASNU_ROLE_ALIAS FOREIGN KEY (ROLE_ALIAS) REFERENCES SEC_ROLE (ID) ENABLE';
		
		select count(1) into v_count from user_constraints where table_name='REF_BOOK_ASNU' and constraint_name='FK_REF_BOOK_ASNU_ROLE_ALIAS';
		IF v_count>0 THEN
			dbms_output.put_line('Constraint FK_REF_BOOK_ASNU_ROLE_ALIAS on table REF_BOOK_ASNU was created');
		END IF;
	else
		dbms_output.put_line('Constraint FK_REF_BOOK_ASNU_ROLE_ALIAS on table REF_BOOK_ASNU already exists');
	END IF; 
	
	dbms_output.put_line('---------------------------------');
	
	select count(1) into v_count from user_constraints where table_name='REF_BOOK_ASNU' and constraint_name='FK_REF_BOOK_ASNU_ROLE_NAME';
	IF v_count=0 THEN
		dbms_output.put_line('Constraint FK_REF_BOOK_ASNU_ROLE_NAME on table REF_BOOK_ASNU not exists');
		dbms_output.put_line('Add Constraint FK_REF_BOOK_ASNU_ROLE_NAME on table REF_BOOK_ASNU');
		EXECUTE IMMEDIATE 'ALTER TABLE REF_BOOK_ASNU ADD CONSTRAINT FK_REF_BOOK_ASNU_ROLE_NAME FOREIGN KEY (ROLE_NAME) REFERENCES SEC_ROLE (ID) ENABLE';
		
		select count(1) into v_count from user_constraints where table_name='REF_BOOK_ASNU' and constraint_name='FK_REF_BOOK_ASNU_ROLE_NAME';
		IF v_count>0 THEN
			dbms_output.put_line('Constraint FK_REF_BOOK_ASNU_ROLE_NAME on table REF_BOOK_ASNU was created');
		END IF;
	else
		dbms_output.put_line('Constraint FK_REF_BOOK_ASNU_ROLE_NAME on table REF_BOOK_ASNU already exists');
	END IF; 
	
	dbms_output.put_line('---------------------------------');
	
	select count(1) into v_count from user_constraints where table_name='REF_BOOK_NDFL' and constraint_name='FK_REF_BOOK_NDFL_DEPART';
	IF v_count=0 THEN
		dbms_output.put_line('Constraint FK_REF_BOOK_NDFL_DEPART on table REF_BOOK_NDFL not exists');
		dbms_output.put_line('Add Constraint FK_REF_BOOK_NDFL_DEPART on table REF_BOOK_NDFL');
		EXECUTE IMMEDIATE 'ALTER TABLE REF_BOOK_NDFL ADD CONSTRAINT FK_REF_BOOK_NDFL_DEPART FOREIGN KEY (DEPARTMENT_ID) REFERENCES DEPARTMENT (ID) ENABLE';
		
		select count(1) into v_count from user_constraints where table_name='REF_BOOK_NDFL' and constraint_name='FK_REF_BOOK_NDFL_DEPART';
		IF v_count>0 THEN
			dbms_output.put_line('Constraint FK_REF_BOOK_NDFL_DEPART on table REF_BOOK_NDFL was created');
		END IF;
	else
		dbms_output.put_line('Constraint FK_REF_BOOK_NDFL_DEPART on table REF_BOOK_NDFL already exists');
	END IF; 
	
	dbms_output.put_line('---------------------------------');
	
	select count(1) into v_count from user_constraints where table_name='REF_BOOK_NDFL_DETAIL' and constraint_name='FK_REF_BOOK_NDFL_DET_DEPART';
	IF v_count=0 THEN
		dbms_output.put_line('Constraint FK_REF_BOOK_NDFL_DET_DEPART on table REF_BOOK_NDFL_DETAIL not exists');
		dbms_output.put_line('Add Constraint FK_REF_BOOK_NDFL_DET_DEPART on table REF_BOOK_NDFL_DETAIL');
		EXECUTE IMMEDIATE 'ALTER TABLE REF_BOOK_NDFL_DETAIL ADD CONSTRAINT FK_REF_BOOK_NDFL_DET_DEPART FOREIGN KEY (DEPARTMENT_ID) REFERENCES DEPARTMENT (ID) ENABLE';
		
		select count(1) into v_count from user_constraints where table_name='REF_BOOK_NDFL_DETAIL' and constraint_name='FK_REF_BOOK_NDFL_DET_DEPART';
		IF v_count>0 THEN
			dbms_output.put_line('Constraint FK_REF_BOOK_NDFL_DET_DEPART on table REF_BOOK_NDFL_DETAIL was created');
		END IF;
	else
		dbms_output.put_line('Constraint FK_REF_BOOK_NDFL_DET_DEPART on table REF_BOOK_NDFL_DETAIL already exists');
	END IF; 
	
	dbms_output.put_line('---------------------------------');
	
	select count(1) into v_count from user_constraints where table_name='CONFIGURATION' and constraint_name='CONFIGURATION_FK';
	IF v_count=0 THEN
		dbms_output.put_line('Constraint CONFIGURATION_FK on table CONFIGURATION not exists');
		dbms_output.put_line('Add Constraint CONFIGURATION_FK on table CONFIGURATION');
		EXECUTE IMMEDIATE 'ALTER TABLE CONFIGURATION ADD CONSTRAINT CONFIGURATION_FK FOREIGN KEY (DEPARTMENT_ID) REFERENCES DEPARTMENT (ID) ENABLE';
		
		select count(1) into v_count from user_constraints where table_name='CONFIGURATION' and constraint_name='CONFIGURATION_FK';
		IF v_count>0 THEN
			dbms_output.put_line('Constraint CONFIGURATION_FK on table CONFIGURATION was created');
		END IF;
	else
		dbms_output.put_line('Constraint CONFIGURATION_FK on table CONFIGURATION already exists');
	END IF; 

END;
/