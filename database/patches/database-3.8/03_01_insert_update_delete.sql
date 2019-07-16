-- 3.8-dnovikov-1 

declare 
  v_task_name varchar2(128):='insert_update_delete block #1 - merge into ref_book_doc_state';  
begin
	merge into ref_book_doc_state dst using
	(select 8 as id, 'Отправка в ЭДО' as name from dual 
	) src
	on (src.id=dst.id)
	when not matched then
		insert (id, name)
		values (src.id, src.name)
	when matched then
		update set dst.name=src.name;
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 

EXCEPTION
  when OTHERS then 
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

-- 3.8-dnovikov-3 
declare 
  v_task_name varchar2(128):='insert_update_delete block #2 - merge into ref_book_form_type';  
begin
	merge into ref_book_form_type dst using
	(select 6 as id, '2 НДФЛ (ФЛ)' as code, '2-НДФЛ для выдачи ФЛ' as name from dual 
	) src
	on (src.id=dst.id)
	when not matched then
		insert (id, code, name)
		values (src.id, src.code, src.name);
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 

EXCEPTION
  when OTHERS then 
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

declare 
  v_task_name varchar2(128):='insert_update_delete block #3 - merge into declaration_kind';  
begin
	merge into declaration_kind dst using
	(select 8 as id, 'Отчетная ФЛ' as name from dual 
	) src
	on (src.id=dst.id)
	when not matched then
		insert (id, name)
		values (src.id, src.name);
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 

EXCEPTION
  when OTHERS then 
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

declare 
  v_task_name varchar2(128):='insert_update_delete block #4 - merge into declaration_type';  
begin
	merge into declaration_type dst using
	(select 105 as id, '2 НДФЛ (ФЛ)' as name from dual 
	) src
	on (src.id=dst.id)
	when not matched then
		insert (id, name)
		values (src.id, src.name);
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 

EXCEPTION
  when OTHERS then 
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

declare 
  v_task_name varchar2(128):='insert_update_delete block #5 - merge into state';  
begin
	merge into state dst using
	(select 4 as id, 'Выдан' as name from dual 
	) src
	on (src.id=dst.id)
	when not matched then
		insert (id, name)
		values (src.id, src.name);
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 

EXCEPTION
  when OTHERS then 
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

--3.8-dnovikov-5
declare 
  v_task_name varchar2(128):='insert_update_delete block #6 - merge into async_task_type';  
begin
	merge into async_task_type dst using
	(select 44 as id, 'Формирование ОНФ 2-НДФЛ(ФЛ)' as name, 'Create2NdflFLAsyncTask' as handler_bean from dual 
	) src
	on (src.id=dst.id)
	when not matched then
		insert (id, name, handler_bean)
		values (src.id, src.name, src.handler_bean);
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 

EXCEPTION
  when OTHERS then 
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/

COMMIT;
--3.8-ytrofimov-4
declare 
  v_task_name varchar2(128):='insert_update_delete block #7 - merge into declaration_template_file';  
begin

	merge into declaration_template_file dst using
	(select 105 as declaration_template_id, '6466aad0-90c5-4101-8002-fb97b0e32d16' as blob_data_id from dual  union
         select 105 as declaration_template_id, '5f3eb319-56cb-4dd8-b06e-b97ea3df0cf1' as blob_data_id from dual  
	) src
	on (src.declaration_template_id=dst.declaration_template_id and src.blob_data_id=dst.blob_data_id)
	when not matched then
		insert (declaration_template_id, blob_data_id)
		values (src.declaration_template_id, src.blob_data_id);
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 

EXCEPTION
  when OTHERS then 
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;
/

-- 3.7.1-skononova-7 
declare 
  v_task_name varchar2(128):='insert_update_delete block #8 - merge into ref_book_oktmo';  
begin
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

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
	

EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

-- 3.7.1-skononova-8  изменение ссылок на ОКТМО в Параметрах подразделений.
declare 
  v_task_name varchar2(128):='insert_update_delete block #9 - change department_config';  
  cnt_u number := 0;
  cnt_i number := 0; 
    procedure convert_department_config (old_oktmo_code in varchar, new_oktmo_code in varchar, cnt_upd in out number, cnt_ins in out number) is
        cnt_r number;
    begin
        update department_config set end_date=to_date ('31.05.2019 23:59:59','dd.mm.yyyy hh24:mi:ss') where end_date is null
          and oktmo_id in (select id from ref_book_oktmo where code=old_oktmo_code);
        cnt_r := sql%rowcount;
        cnt_upd := cnt_upd + cnt_r;
        if  cnt_r>0 then
            insert into department_config (ID,KPP,OKTMO_ID,START_DATE,END_DATE,DEPARTMENT_ID,TAX_ORGAN_CODE,TAX_ORGAN_CODE_MID,PRESENT_PLACE_ID,NAME,
                                   PHONE,REORGANIZATION_ID,REORG_INN,REORG_KPP,SIGNATORY_ID,SIGNATORY_SURNAME,SIGNATORY_FIRSTNAME,SIGNATORY_LASTNAME,
                                   APPROVE_DOC_NAME,APPROVE_ORG_NAME,REORG_SUCCESSOR_KPP)
            select seq_department_config.nextval,KPP,(select id from ref_book_oktmo where code=new_oktmo_code),to_date('01.06.2019','dd.mm.yyyy'),null,DEPARTMENT_ID,TAX_ORGAN_CODE,
                   TAX_ORGAN_CODE_MID,PRESENT_PLACE_ID,NAME,PHONE,REORGANIZATION_ID,REORG_INN,REORG_KPP,SIGNATORY_ID,SIGNATORY_SURNAME,SIGNATORY_FIRSTNAME,
                   SIGNATORY_LASTNAME,APPROVE_DOC_NAME,APPROVE_ORG_NAME,REORG_SUCCESSOR_KPP from department_config 
            where trunc(end_date)=to_date ('31.05.2019','dd.mm.yyyy') 
            and oktmo_id in (select id from ref_book_oktmo where code=old_oktmo_code);
	    
	    cnt_ins := cnt_ins + sql%rowcount;	
        end if;  
    end;

begin
    convert_department_config('04614156','04614452',cnt_u, cnt_i);
    convert_department_config('04614156051','04614452101',cnt_u, cnt_i);
    convert_department_config('04614156106','04614452106',cnt_u, cnt_i);
    convert_department_config('04639160','04639450',cnt_u, cnt_i);
    convert_department_config('04639160051','04639450101',cnt_u, cnt_i);
    dbms_output.put_line(v_task_name ||': updated '||to_char (cnt_u) || ' records, inserted ' || to_char (cnt_i) || ' records');	
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/

COMMIT;
/
--3.7.1-skononova-9  плюс архивация ОКТМО 76636158106 из хотфикса 3.7-hotfix-1
declare 
  v_task_name varchar2(128):='insert_update_delete block #10 - merge into ref_book_oktmo';  
begin
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

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
	

EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;
