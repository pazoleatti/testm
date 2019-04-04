--https://jira.aplana.com/browse/SBRFNDFL-7132 - Добавление нового Справочника Подсистем
DECLARE
	v_run_condition number(1);
BEGIN
	select count(*) into v_run_condition from user_synonyms us where us.SYNONYM_NAME='SUBSYSTEM' and us.TABLE_OWNER='TAXNSI_UNSTABLE';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'DROP SYNONYM SUBSYSTEM';
	END IF;

	select count(*) into v_run_condition from user_synonyms us where us.SYNONYM_NAME='VW_SUBSYSTEM' and us.TABLE_OWNER='TAXNSI_UNSTABLE';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'DROP SYNONYM VW_SUBSYSTEM';
	END IF;

	select count(*) into v_run_condition from USER_COL_PRIVS_RECD where table_name = 'SUBSYSTEM' and owner = 'TAXNSI_UNSTABLE';
	IF v_run_condition=1 THEN
		select decode(count(*),0,1,0) into v_run_condition from user_synonyms us where us.SYNONYM_NAME='SUBSYSTEM_SYN' and us.TABLE_OWNER='TAXNSI_UNSTABLE';
		IF v_run_condition=1 THEN
		  EXECUTE IMMEDIATE 'CREATE SYNONYM SUBSYSTEM_SYN FOR TAXNSI_UNSTABLE.SUBSYSTEM';
		END IF;
	END IF;

	select count(*) into v_run_condition from USER_TAB_PRIVS_RECD	where table_name = 'VW_SUBSYSTEM' and owner = 'TAXNSI_UNSTABLE';
	IF v_run_condition=1 THEN
		select decode(count(*),0,1,0) into v_run_condition from user_synonyms us where us.SYNONYM_NAME='VW_SUBSYSTEM_SYN' and us.TABLE_OWNER='TAXNSI_UNSTABLE';
		IF v_run_condition=1 THEN
		  EXECUTE IMMEDIATE 'CREATE SYNONYM VW_SUBSYSTEM_SYN FOR TAXNSI_UNSTABLE.VW_SUBSYSTEM';
		END IF;
	END IF;
EXCEPTION
	WHEN OTHERS THEN 
		null;
END;
