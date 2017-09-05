declare 
  v_count number;
BEGIN
  delete from event where id in (801,802)

  select count(1) into v_count from user_synonyms where synonym_name='ADD_LOG_SYSTEM_NDFL';
  IF v_count=0 THEN
    EXECUTE IMMEDIATE 'CREATE SYNONYM ADD_LOG_SYSTEM_NDFL FOR &1..ADD_LOG_SYSTEM_NDFL';
  END IF;
END;
/