PROMPT Fill DECLARATION_TEMPLATE...

begin
	merge into DECLARATION_TEMPLATE a using
	(select id, create_script from DECLARATION_TEMPLATE_TMP) b
	on (a.id = b.id)
	when not matched then
	insert (id, create_script) values(b.id, b.create_script)
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