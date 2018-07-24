PROMPT Fill BLOB_DATA... 

begin
	insert into BLOB_ID_TMP(blob_data_id, updated)
	select blob_data_id, 0 as updated from BLOB_NAMES_TMP;
	
	for rec in (select bnt.blob_data_id, bdt.name, bdt.data, bdt.creation_date, bdt.id blob_data_id_old 
	from BLOB_DATA_TMP bdt join BLOB_NAMES_TMP bnt on bdt.declaration_template_id=bnt.id and bdt.name=bnt.name)
	loop
		update blob_data set name=rec.name, data=rec.data where id=rec.blob_data_id;
		update blob_id_tmp set updated=1, blob_data_id_old=rec.blob_data_id_old where blob_data_id=rec.blob_data_id;
		dbms_output.put_line('Updated blob_data at step 1 with id='||rec.blob_data_id);
		end loop;
	
	delete from BLOB_DATA_TMP where id in (select blob_data_id_old from blob_id_tmp);
	dbms_output.put_line('Deleted from BLOB_DATA_TMP at step 1:'||SQL%ROWCOUNT);
	delete from BLOB_NAMES_TMP where blob_data_id in (select blob_data_id from blob_id_tmp where updated=1);
	dbms_output.put_line('Deleted from BLOB_NAMES_TMP at step 1:'||SQL%ROWCOUNT);

	for rec in (select bnt.blob_data_id, bdt.name, bdt.data, bdt.creation_date, bdt.id blob_data_id_old 
	from BLOB_DATA_TMP bdt join BLOB_NAMES_TMP bnt on bdt.declaration_template_id=bnt.id
	and bnt.blob_data_id in (select xsd from declaration_template) and bdt.name like '%.xsd')
	loop
		update blob_data set name=rec.name, data=rec.data where id=rec.blob_data_id;
		update blob_id_tmp set updated=1, blob_data_id_old=rec.blob_data_id_old where blob_data_id=rec.blob_data_id;
		dbms_output.put_line('Updated blob_data at step 2 with id='||rec.blob_data_id);
	end loop;
	
	delete from BLOB_DATA_TMP where id in (select blob_data_id_old from blob_id_tmp);
	dbms_output.put_line('Deleted from BLOB_DATA_TMP at step 2:'||SQL%ROWCOUNT);
	delete from BLOB_NAMES_TMP where blob_data_id in (select blob_data_id from blob_id_tmp where updated=1);
	dbms_output.put_line('Deleted from BLOB_NAMES_TMP at step 2:'||SQL%ROWCOUNT);	
	
	merge into BLOB_DATA a using
	(select id, name, data, creation_date from BLOB_DATA_TMP) b
	on (a.id = b.id)
	when not matched then
	insert (id, name, data, creation_date) values(b.id, b.name, b.data, b.creation_date)
	when matched then
	update set a.name=b.name, a.data=b.data; 
	dbms_output.put_line('Merged into blob_data:'||SQL%ROWCOUNT);

	commit;

	exception when others then
	   rollback;
	   dbms_output.put_line(sqlerrm);
	   commit;
	   raise_application_error(-20999,'Error fill table BLOB_DATA.');
end;   
/

PROMPT OK.