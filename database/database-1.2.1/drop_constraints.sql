declare 
  v_count number;
BEGIN
  select count(1) into v_count from user_constraints where constraint_name='FK_FIAS_ADDROBJ_PARENTID';
  if v_count>0 then
    execute immediate 'alter table fias_addrobj drop constraint FK_FIAS_ADDROBJ_PARENTID';
  end if;
END;
/
