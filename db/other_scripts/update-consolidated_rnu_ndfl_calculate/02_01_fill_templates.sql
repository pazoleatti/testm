PROMPT Fill DECL_TEMPLATE_EVENT_SCRIPT... 

begin
	merge into DECL_TEMPLATE_EVENT_SCRIPT a using
	(select id, script from DECL_TEMPLATE_EVENT_SCRIPT_TMP) b
	on (a.id = b.id)
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