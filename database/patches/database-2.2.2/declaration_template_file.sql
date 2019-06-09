PROMPT Fill DECLARATION_TEMPLATE_FILE... 

declare v_count number;
begin
	select count(1) into v_count from declaration_template_file dtf join blob_data bd on dtf.blob_data_id=bd.id
	where dtf.declaration_template_id=100 and bd.name='excel_template_dec.xlsx';
	
	if v_count=0 then
		insert into declaration_template_file(declaration_template_id, blob_data_id)
		values (100, '3b4eb6e0-3ce3-48ba-b8e5-0b9c9174b5fe');
	end if;
	
	select count(1) into v_count from declaration_template_file dtf join blob_data bd on dtf.blob_data_id=bd.id
	where dtf.declaration_template_id=101 and bd.name='excel_template_dec.xlsx';
	
	if v_count=0 then
		insert into declaration_template_file(declaration_template_id, blob_data_id)
		values (101, 'bfcccac4-7a42-4760-a462-98e2633b2392');
	end if;

	commit;

	exception when others then
	   rollback;
	   dbms_output.put_line(sqlerrm);
	   commit;
	   raise_application_error(-20999,'Error fill table DECL_TEMPLATE_EVENT_SCRIPT.');
end;   
/

PROMPT OK.