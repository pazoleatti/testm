-- 3.7.1-skononova-7 
declare 
  v_task_name varchar2(128):='insert_update_delete block #1 - merge into ref_book_oktmo';  
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
  v_task_name varchar2(128):='insert_update_delete block #2 - change department_config';  
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
--3.7.1-skononova-9
declare 
  v_task_name varchar2(128):='insert_update_delete block #3 - merge into ref_book_oktmo';  
begin
	merge into ref_book_oktmo dst using 
	    (select  '04614156' as code, 'поселок Памяти 13 Борцов' as name, '1' as razd, to_date ('01.06.2019','dd.mm.yyyy') as version, 2 as status from dual union
	     select '04614156051','рп Памяти 13 Борцов','2', to_date ('01.06.2019','dd.mm.yyyy'), 2 from dual union
	     select '04614156106','д Малый Кемчуг','2', to_date ('01.06.2019','dd.mm.yyyy'), 2 from dual union
	     select '04639160','поселок Тинской','1', to_date ('01.06.2019','dd.mm.yyyy'), 2 from dual union
	     select '04639160051','рп Тинской','2', to_date ('01.06.2019','dd.mm.yyyy'), 2 from dual 
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

