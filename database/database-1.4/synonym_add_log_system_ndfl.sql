declare 
  v_count number;
BEGIN
  delete from event where id in (801,802);

  select count(1) into v_count from user_synonyms where synonym_name='ADD_LOG_SYSTEM_NDFL';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'CREATE SYNONYM ADD_LOG_SYSTEM_NDFL FOR &1..ADD_LOG_SYSTEM_NDFL';
  END IF;
  
  select count(1) into v_count from user_tables where table_name='LOG_SYSTEM_REPORT';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'DROP TABLE LOG_SYSTEM_REPORT';
  END IF;
  
  select count(1) into v_count from user_tables where table_name='LOG_SYSTEM';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'DROP TABLE LOG_SYSTEM';
  END IF;
    
  select count(1) into v_count from user_tables where table_name='AUDIT_FORM_TYPE';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'DROP TABLE AUDIT_FORM_TYPE';
  END IF;
  
  select count(1) into v_count from user_sequences where sequence_name='SEQ_LOG_SYSTEM';
  IF v_count>0 THEN
    EXECUTE IMMEDIATE 'DROP SEQUENCE SEQ_LOG_SYSTEM';
  END IF;
END;
/