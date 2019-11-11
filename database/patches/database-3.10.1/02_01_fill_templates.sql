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
	(select ID, STATUS,VERSION, NAME,CREATE_SCRIPT, JRXML, DECLARATION_TYPE_ID, XSD, FORM_KIND, FORM_TYPE from DECLARATION_TEMPLATE_TMP) b
	on (a.id = b.id)
	when not matched then
	insert (id, status,version,name,create_script,jrxml, declaration_type_id, xsd,form_kind,form_type) values
	(b.id,b.status,b.version,b.name, b.create_script,b.jrxml, b.declaration_type_id, b.xsd,b.form_kind,b.form_type)
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

PROMPT Fill REF_BOOK_OKTMO... 

begin
	INSERT INTO ref_book_oktmo (id, record_id, version, status, code, name, razd)
        SELECT seq_ref_book_record.nextval, seq_ref_book_record_row_id.nextval, tmp.version, 0, tmp.code, tmp.name, tmp.razd 
        FROM ref_book_oktmo_TMP3 tmp
        WHERE tmp.code NOT IN (SELECT code FROM ref_book_oktmo);

	INSERT INTO ref_book_oktmo (id, record_id, version, status, code, name, razd)
		    SELECT seq_ref_book_record.nextval, (select max(record_id) from ref_book_oktmo c where c.code=src.code),
			   src.version, 0, src.code, src.name, src.razd 
		    FROM ref_book_oktmo_TMP3 src left join ref_book_oktmo dst 
				on dst.code = src.code and dst.name=src.name and dst.version  <= src.version     		   
			where dst.id is null;

	merge into ref_book_oktmo dst using 
	    (select '96612101' as code, 'Курчалойское' as name, '1' as razd, to_date ('01.05.2019','dd.mm.yyyy') as version, 0 as status from dual union
	     select '96612101001','г Курчалой','2', to_date ('01.05.2019','dd.mm.yyyy'), 0 from dual union
	     select '76636402','Балягинское','1', to_date ('01.05.2019','dd.mm.yyyy'), 0 from dual union
	     select '76636402101','с Баляга','2', to_date ('01.05.2019','dd.mm.yyyy'), 0 from dual union
	     select '76636402106','с Кули','2', to_date ('01.05.2019','dd.mm.yyyy'), 0 from dual union
	     select '76636452','Тарбагатайское','1', to_date ('01.05.2019','dd.mm.yyyy'), 0 from dual union
	     select '76636452101','c Тарбагатай','2', to_date ('01.05.2019','dd.mm.yyyy'), 0 from dual union
	     select '76636452106','c Нижний Тарбагатай','2', to_date ('01.05.2019','dd.mm.yyyy'), 0 from dual 	    )  src
	    on (dst.code=src.code and dst.version=src.version and dst.status=src.status)
	    when not matched then insert (id, code, name, version, status, record_id, razd)
	    values (seq_ref_book_record.nextval, src.code, src.name, src.version, src.status, seq_ref_book_record_row_id.nextval, src.razd);

	merge into ref_book_oktmo dst using 
	    (select  '96612419' as code, 'Курчалойское' as name, '1' as razd, to_date ('01.05.2019','dd.mm.yyyy') as version, 2 as status from dual union
	     select '96612419101','c Курчалой','2', to_date ('01.05.2019','dd.mm.yyyy'), 2 from dual union
	     select '76636152','Балягинское','1', to_date ('01.05.2019','dd.mm.yyyy'), 2 from dual union
	     select '76636152051','пгт Баляга','2', to_date ('01.05.2019','dd.mm.yyyy'), 2 from dual union
	     select '76636152106','с Кули','2', to_date ('01.05.2019','dd.mm.yyyy'), 2 from dual union
	     select '76636158','Тарбагатайское','1', to_date ('01.05.2019','dd.mm.yyyy'), 2 from dual union
	     select '76636158051','пгт Тарбагатай','2', to_date ('01.05.2019','dd.mm.yyyy'), 2 from dual 
	    )  src
	    on (dst.code=src.code and dst.version=src.version)
	    when not matched then insert (id, code, name, version, status, record_id, razd)
	    values (seq_ref_book_record.nextval, src.code, src.name, src.version, src.status, 
	   (select max(record_id) from ref_book_oktmo c where c.code=src.code and c.status=0),  src.razd);

	merge into ref_book_oktmo dst using 
	    (
	     select '04614452' as code, 'сельсовет Памяти 13 Борцов' as name, '1' as razd, to_date ('01.06.2019','dd.mm.yyyy') as version, 0 as status from dual union
	     select '04614452101','п Памяти 13 Борцов','2', to_date ('01.06.2019','dd.mm.yyyy'), 0 from dual union
	     select '04614452106','д Малый Кемчуг','2', to_date ('01.06.2019','dd.mm.yyyy'), 0 from dual union
	     select '04639450','поселок Тинской','1', to_date ('01.06.2019','dd.mm.yyyy'), 0 from dual union
	     select '04639450101','п Тинской','2', to_date ('01.06.2019','dd.mm.yyyy'), 0 from dual 
	    ) src
	    on (dst.code=src.code and dst.version=src.version and dst.status=src.status)
	    when not matched then insert (id, code, name, version, status, record_id, razd)
	    values (seq_ref_book_record.nextval, src.code, src.name, src.version, src.status, seq_ref_book_record_row_id.nextval, src.razd);


	merge into ref_book_oktmo dst using 
	    (select  '04614156' as code, 'поселок Памяти 13 Борцов' as name, '1' as razd, to_date ('01.06.2019','dd.mm.yyyy') as version, 2 as status from dual union
	     select '04614156051','рп Памяти 13 Борцов','2', to_date ('01.06.2019','dd.mm.yyyy'), 2 from dual union
	     select '04614156106','д Малый Кемчуг','2', to_date ('01.06.2019','dd.mm.yyyy'), 2 from dual union
	     select '04639160','поселок Тинской','1', to_date ('01.06.2019','dd.mm.yyyy'), 2 from dual union
	     select '04639160051','рп Тинской','2', to_date ('01.06.2019','dd.mm.yyyy'), 2 from dual  union
	     select '76636158106','с Нижний Тарбагатай','2', to_date ('01.05.2019','dd.mm.yyyy'), 2 from dual
	    )  src
	    on (dst.code=src.code and dst.version=src.version)
	    when not matched then insert (id, code, name, version, status, record_id, razd)
	    values (seq_ref_book_record.nextval, src.code, src.name, src.version, src.status, 
	   (select max(record_id) from ref_book_oktmo c where c.code=src.code and c.status=0),  src.razd)
	   where (select max(record_id) from ref_book_oktmo c where c.code=src.code and c.status=0) is not null;

	
	commit;

	exception when others then
	   rollback;
	   dbms_output.put_line(sqlerrm);
	   commit;
	   raise_application_error(-20999,'Error fill table REF_BOOK_OKTMO.');
end;   
/

PROMPT OK.
