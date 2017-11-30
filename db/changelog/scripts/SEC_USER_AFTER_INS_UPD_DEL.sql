create or replace 
trigger SEC_USER_AFTER_INS_UPD_DEL
  after insert or update or delete on sec_user
  for each row
declare
  pragma autonomous_transaction;
  vCurrentID number(18) := :new.id;
  vFormerID number(18) := :old.id;
begin
  if updating then 
     insert into log_un_changes(table_name, oper_name, id)
     values ('SEC_USER','update', vCurrentID);
  end if;
  
  if deleting then 
     insert into log_un_changes(table_name, oper_name, id)
     values ('SEC_USER','delete', vFormerID);
  end if;
  
  if inserting then 
     insert into log_un_changes(table_name, oper_name, id)
     values ('SEC_USER','insert', vCurrentID);
  end if;
  
  commit;
end SEC_USER_AFTER_INS_UPD_DEL;