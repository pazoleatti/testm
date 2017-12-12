declare 
  v_count number;
BEGIN
  select count(1) into v_count from user_synonyms where synonym_name='SUBSYSTEM_ROLE';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'CREATE SYNONYM SUBSYSTEM_ROLE FOR &1..SUBSYSTEM_ROLE';
  END IF;
  
  select count(1) into v_count from user_synonyms where lower(synonym_name)='vw_log_table_change';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'CREATE SYNONYM vw_log_table_change FOR &1..vw_log_table_change';
  END IF;
END;
/