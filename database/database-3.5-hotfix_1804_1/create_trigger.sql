CREATE OR REPLACE TRIGGER LOCK_DATA_BEFORE_INS
BEFORE INSERT on lock_data
  FOR EACH ROW 

DECLARE
  vCurrentKey varchar2(1000) := :new.key;
  vKeyID varchar2(1000);
  vDeclarationTemplateID number;
begin

  vKeyID := regexp_replace(vCurrentKey,'[^[[:digit:]]]*');
  IF 'DECLARATION_DATA_'||vKeyID||'_CONSOLIDATE' = vCurrentKey THEN
     BEGIN
       SELECT dd.declaration_template_id INTO vDeclarationTemplateID FROM declaration_data dd WHERE dd.id = vKeyID;
     EXCEPTION
       WHEN no_data_found THEN null;
     END;
     IF vDeclarationTemplateID = 100 THEN
        raise_application_error(-20001, 'Нельзя создать блокировку к ПНФ ('||vCurrentKey||')');
     END IF;
  END IF;

END LOCK_DATA_BEFORE_INS;
/

exit;