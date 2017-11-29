create or replace 
trigger DEP_AFTER_INS_UPD_DEL
  after insert or update or delete on department
  for each row
declare
  pragma autonomous_transaction;
  vCurrentID number(18) := :new.id;
  vFormerID number(18) := :old.id;
begin
  if updating then 
     insert into log_un_changes(table_name, oper_name, id)
     values ('DEPARTMENT','update', vCurrentID);
  end if;
  
  if deleting then 
     insert into log_un_changes(table_name, oper_name, id)
     values ('DEPARTMENT','delete', vFormerID);
  end if;
  
  if inserting then 
     insert into log_un_changes(table_name, oper_name, id)
     values ('DEPARTMENT','insert', vCurrentID);
  end if;
  
  commit;
end DEP_AFTER_INS_UPD_DEL;