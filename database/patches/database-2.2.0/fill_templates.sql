PROMPT Fill BLOB_DATA... 

begin
merge into BLOB_DATA a using
(select id, name, data, creation_date from BLOB_DATA_TMP) b
on (a.id = b.id)
when not matched then
insert (id, name, data, creation_date) values(b.id, b.name, b.data, b.creation_date)
when matched then
update set a.data=b.data; 

commit;

exception when others then
   rollback;
   dbms_output.put_line(sqlerrm);
   commit;
   raise_application_error(-20999,'Error fill table BLOB_DATA.');
end;   
/

PROMPT OK.

PROMPT Fill DECLARATION_TEMPLATE... 

begin
merge into DECLARATION_TEMPLATE a using
(select id, status, version, name, create_script, jrxml, declaration_type_id, xsd, form_kind, form_type from DECLARATION_TEMPLATE_TMP) b
on (a.id = b.id)
when not matched then
insert (id, status, version, name, create_script, jrxml, declaration_type_id, xsd, form_kind, form_type) values (b.id, b.status, b.version, b.name, b.create_script, b.jrxml, b.declaration_type_id, b.xsd, b.form_kind, b.form_type)
when matched then
update set a.create_script=b.create_script; 

commit;

exception when others then
   rollback;
   dbms_output.put_line(sqlerrm);
   commit;
   raise_application_error(-20999,'Error fill table DECLARATION_TEMPLATE.');
end;   
/
 
PROMPT OK.

PROMPT Fill DECL_TEMPLATE_EVENT_SCRIPT... 

begin
merge into DECL_TEMPLATE_EVENT_SCRIPT a using
(select id, declaration_template_id, event_id, script from DECL_TEMPLATE_EVENT_SCRIPT_TMP) b
on (a.id = b.id)
when not matched then
insert (id, declaration_template_id, event_id, script) values (b.id, b.declaration_template_id, b.event_id, b.script)
when matched then
update set a.script=b.script; 

commit;

exception when others then
   rollback;
   dbms_output.put_line(sqlerrm);
   commit;
   raise_application_error(-20999,'Error fill table DECL_TEMPLATE_EVENT_SCRIPT.');
end;   
/

PROMPT OK.