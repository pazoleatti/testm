PROMPT Fill BLOB_DATA... 

begin
	insert into BLOB_ID_TMP(blob_data_id, updated)
	select blob_data_id, 0 as updated from BLOB_NAMES_TMP;
	
	for rec in (select bnt.blob_data_id, bdt.name, bdt.data, bdt.creation_date, bdt.id blob_data_id_old 
	from BLOB_DATA_TMP bdt join BLOB_NAMES_TMP bnt on bdt.declaration_template_id=bnt.id and bdt.name=bnt.name)
	loop
		update blob_data set name=rec.name, data=rec.data where id=rec.blob_data_id;
		update blob_id_tmp set updated=1, blob_data_id_old=rec.blob_data_id_old where blob_data_id=rec.blob_data_id;
	end loop;
	
	delete from BLOB_DATA_TMP where id in (select blob_data_id_old from blob_id_tmp);
	delete from BLOB_NAMES_TMP where blob_data_id in (select blob_data_id from blob_id_tmp where updated=1);

	for rec in (select bnt.blob_data_id, bdt.name, bdt.data, bdt.creation_date, bdt.id blob_data_id_old 
	from BLOB_DATA_TMP bdt join BLOB_NAMES_TMP bnt on bdt.declaration_template_id=bnt.id
	and bnt.blob_data_id in (select xsd from declaration_template) and bdt.name like '%.xsd')
	loop
		update blob_data set name=rec.name, data=rec.data where id=rec.blob_data_id;
		update blob_id_tmp set updated=1, blob_data_id_old=rec.blob_data_id_old where blob_data_id=rec.blob_data_id;
	end loop;
	
	delete from BLOB_DATA_TMP where id in (select blob_data_id_old from blob_id_tmp);
	delete from BLOB_NAMES_TMP where blob_data_id in (select blob_data_id from blob_id_tmp where updated=1);
	
	merge into BLOB_DATA a using
	(select id, name, data, creation_date from BLOB_DATA_TMP) b
	on (a.id = b.id)
	when not matched then
	insert (id, name, data, creation_date) values(b.id, b.name, b.data, b.creation_date)
	when matched then
	update set a.name=b.name, a.data=b.data; 


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

PROMPT Fill REF_BOOK_TAX_INSPECTION... 
begin
	merge into ref_book_tax_inspection dst using 
	    ( select code, name from kno_temp
	    )  src
	    on (dst.code=src.code )
	    when not matched then insert (id, code, name)
	    values (seq_ref_book_record.nextval, src.code, src.name)
	    when matched then update set dst.name=src.name where dst.name<>src.name;
	commit;	
EXCEPTION
	when others then

	   rollback;
	   dbms_output.put_line(sqlerrm);
	   commit;
	   raise_application_error(-20999,'Error fill table REF_BOOK_TAX_INSPECTION.');
end;
/
PROMPT OK.
/