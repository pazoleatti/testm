declare 
  v_count number;
BEGIN
  select count(1) into v_count from user_indexes where index_name='IDX_FIAS_ADDR_CURRST_AOLEV';
  if v_count>0 then
    execute immediate 'drop index IDX_FIAS_ADDR_CURRST_AOLEV';
  end if;
END;
/