declare 
	v_run_condition number(1);
	v_task_name varchar2(128):='drop_sequences block #1 - drop sequence SEQ_DEPARTMENT (SBRFNDFL-5294)';  
begin
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_DEPARTMENT';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_DEPARTMENT';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #2 - drop sequence SEQ_DEPARTMENT_FORM_TYPE (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_DEPARTMENT_FORM_TYPE';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_DEPARTMENT_FORM_TYPE';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #3 - drop sequence SEQ_FORM_COLUMN (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_FORM_COLUMN';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_FORM_COLUMN';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #4 - drop sequence SEQ_FORM_DATA (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_FORM_DATA';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_FORM_DATA';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #5 - drop sequence SEQ_FORM_DATA_NNN (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_FORM_DATA_NNN';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_FORM_DATA_NNN';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #6 - drop sequence SEQ_FORM_DATA_SIGNER (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_FORM_DATA_SIGNER';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_FORM_DATA_SIGNER';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #7 - drop sequence SEQ_FORM_SEARCH_RESULT (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_FORM_SEARCH_RESULT';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_FORM_SEARCH_RESULT';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #8 - drop sequence SEQ_FORM_STYLE (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_FORM_STYLE';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_FORM_STYLE';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #9 - drop sequence SEQ_FORM_TEMPLATE (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_FORM_TEMPLATE';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_FORM_TEMPLATE';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #10 - drop sequence SEQ_FORM_TYPE (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_FORM_TYPE';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_FORM_TYPE';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #11 - drop sequence SEQ_LOG_ENTRY (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_LOG_ENTRY';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_LOG_ENTRY';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #12 - drop sequence SEQ_LOG_QUERY (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_LOG_QUERY';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_LOG_QUERY';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #13 - drop sequence SEQ_LOG_QUERY_SESSION (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_LOG_QUERY_SESSION';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_LOG_QUERY_SESSION';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #14 - drop sequence SEQ_SEC_USER (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_SEC_USER';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_SEC_USER';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #15 - drop sequence SEQ_SEC_USER_ASNU_ID (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_SEC_USER_ASNU_ID';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_SEC_USER_ASNU_ID';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #16 - drop sequence SEQ_TASK_CONTEXT (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_TASK_CONTEXT';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_TASK_CONTEXT';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #17 - drop sequence SEQ_RASCHSV_KOL_LIC_TIP (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_RASCHSV_KOL_LIC_TIP';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_RASCHSV_KOL_LIC_TIP';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #18 - drop sequence SEQ_RASCHSV_OBYAZ_PLAT_SV (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_RASCHSV_OBYAZ_PLAT_SV';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_RASCHSV_OBYAZ_PLAT_SV';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #19 - drop sequence SEQ_RASCHSV_OSS_VNM (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_RASCHSV_OSS_VNM';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_RASCHSV_OSS_VNM';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #20 - drop sequence SEQ_RASCHSV_PERS_SV_STRAH_LIC (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_RASCHSV_PERS_SV_STRAH_LIC';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_RASCHSV_PERS_SV_STRAH_LIC';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #21 - drop sequence SEQ_RASCHSV_PRAV_TARIF3_1_427 (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_RASCHSV_PRAV_TARIF3_1_427';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_RASCHSV_PRAV_TARIF3_1_427';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #22 - drop sequence SEQ_RASCHSV_PRAV_TARIF5_1_427 (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_RASCHSV_PRAV_TARIF5_1_427';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_RASCHSV_PRAV_TARIF5_1_427';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #23 - drop sequence SEQ_RASCHSV_PRAV_TARIF7_1_427 (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_RASCHSV_PRAV_TARIF7_1_427';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_RASCHSV_PRAV_TARIF7_1_427';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #24 - drop sequence SEQ_RASCHSV_RASH_OSS_ZAK (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_RASCHSV_RASH_OSS_ZAK';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_RASCHSV_RASH_OSS_ZAK';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #25 - drop sequence SEQ_RASCHSV_RASH_OSS_ZAK_RASH (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_RASCHSV_RASH_OSS_ZAK_RASH';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_RASCHSV_RASH_OSS_ZAK_RASH';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #26 - drop sequence SEQ_RASCHSV_RASH_VYPL (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_RASCHSV_RASH_VYPL';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_RASCHSV_RASH_VYPL';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #27 - drop sequence SEQ_RASCHSV_SVED_OBUCH (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_RASCHSV_SVED_OBUCH';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_RASCHSV_SVED_OBUCH';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');  
	END IF;

	v_task_name:='drop_sequences block #28 - drop sequence SEQ_RASCHSV_SVNP_PODPISANT (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_RASCHSV_SVNP_PODPISANT';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_RASCHSV_SVNP_PODPISANT';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #29 - drop sequence SEQ_RASCHSV_SV_OPS_OMS (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_RASCHSV_SV_OPS_OMS';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_RASCHSV_SV_OPS_OMS';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #30 - drop sequence SEQ_RASCHSV_SV_OPS_OMS_RASCH (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_RASCHSV_SV_OPS_OMS_RASCH';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_RASCHSV_SV_OPS_OMS_RASCH';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #31 - drop sequence SEQ_RASCHSV_SV_PRIM_TARIF1_422 (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_RASCHSV_SV_PRIM_TARIF1_422';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_RASCHSV_SV_PRIM_TARIF1_422';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #32 - drop sequence SEQ_RASCHSV_SV_PRIM_TARIF2_425 (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_RASCHSV_SV_PRIM_TARIF2_425';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_RASCHSV_SV_PRIM_TARIF2_425';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #33 - drop sequence SEQ_RASCHSV_SV_PRIM_TARIF9_427 (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_RASCHSV_SV_PRIM_TARIF9_427';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_RASCHSV_SV_PRIM_TARIF9_427';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #34 - drop sequence SEQ_RASCHSV_SV_REESTR_MDO (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_RASCHSV_SV_REESTR_MDO';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_RASCHSV_SV_REESTR_MDO';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #35 - drop sequence SEQ_RASCHSV_SV_SUM_1TIP (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_RASCHSV_SV_SUM_1TIP';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_RASCHSV_SV_SUM_1TIP';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #36 - drop sequence SEQ_RASCHSV_SV_VYPL (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_RASCHSV_SV_VYPL';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_RASCHSV_SV_VYPL';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #37 - drop sequence SEQ_RASCHSV_SV_VYPL_MK (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_RASCHSV_SV_VYPL_MK';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_RASCHSV_SV_VYPL_MK';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #38 - drop sequence SEQ_RASCHSV_UPL_PER (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_RASCHSV_UPL_PER';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_RASCHSV_UPL_PER';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #39 - drop sequence SEQ_RASCHSV_UPL_PREV_OSS (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_RASCHSV_UPL_PREV_OSS';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_RASCHSV_UPL_PREV_OSS';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #40 - drop sequence SEQ_RASCHSV_UPL_SV_PREV (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_RASCHSV_UPL_SV_PREV';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_RASCHSV_UPL_SV_PREV';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #41 - drop sequence SEQ_RASCHSV_VYPL_FIN_FB (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_RASCHSV_VYPL_FIN_FB';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_RASCHSV_VYPL_FIN_FB';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #42 - drop sequence SEQ_RASCHSV_VYPL_PRICHINA (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_RASCHSV_VYPL_PRICHINA';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_RASCHSV_VYPL_PRICHINA';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #43 - drop sequence SEQ_RASCHSV_VYPL_SV_DOP (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_RASCHSV_VYPL_SV_DOP';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_RASCHSV_VYPL_SV_DOP';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

	v_task_name:='drop_sequences block #44 - drop sequence SEQ_RASCHSV_VYPL_SV_DOP_MT (SBRFNDFL-5294)';  
	select count(*) into v_run_condition from user_sequences where sequence_name='SEQ_RASCHSV_VYPL_SV_DOP_MT';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'drop sequence SEQ_RASCHSV_VYPL_SV_DOP_MT';

		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
	COMMIT;
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
end;
/
