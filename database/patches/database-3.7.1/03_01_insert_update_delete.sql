-- 3.7.1-skononova-1 https://jira.aplana.com/browse/SBRFNDFL-7852 Добавить прочерк в поле Наименование для вновь добавленных статей дохода
declare 
  v_task_name varchar2(128):='insert_update_delete block #1 - merge into ref_book_income_kind';  
begin
	merge into ref_book_income_kind a using
	 (select (select id from (select id,version from ref_book_income_type where code='2520' and status=0 order by version desc) where rownum=1) as income_type_id, 
	        '00' as mark, '-' as name, to_date('01.01.2016','DD.MM.YYYY') as version from dual
	 union
	 select (select id from (select id,version from ref_book_income_type where code='2720' and status=0 order by version desc) where rownum=1) as income_type_id, 
	        '00' as mark, '-' as name, to_date('01.01.2016','DD.MM.YYYY') as version from dual
	 union
	 select (select id from (select id,version from ref_book_income_type where code='2740' and status=0 order by version desc) where rownum=1) as income_type_id, 
	        '00' as mark, '-' as name, to_date('01.01.2016','DD.MM.YYYY') as version from dual
	 union
	 select (select id from (select id,version from ref_book_income_type where code='2750' and status=0 order by version desc) where rownum=1) as income_type_id, 
	        '00' as mark, '-' as name, to_date('01.01.2016','DD.MM.YYYY') as version from dual
	 union
	 select (select id from (select id,version from ref_book_income_type where code='2790' and status=0 order by version desc) where rownum=1) as income_type_id, 
	        '00' as mark, '-' as name, to_date('01.01.2016','DD.MM.YYYY') as version from dual
	) b
	on (a.income_type_id=b.income_type_id and a.mark=b.mark and a.version=b.version)
	when not matched then
		insert (id,record_id,income_type_id,mark,name,version)
		values (seq_ref_book_record.nextval,seq_ref_book_record_row_id.nextval,b.income_type_id,b.mark,b.name,b.version)
	when matched then
		update set a.name = b.name where a.name is null;

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

-- 3.7.1-skononova-2 https://jira.aplana.com/browse/SBRFNDFL-7912 Добавить новые ОКТМО в справочник ОКТМО
-- + изменение ссылок на ОКТМО в Параметрах подразделений 
declare 
  v_task_name varchar2(128):='insert_update_delete block #2 - merge into ref_book_oktmo';  
begin
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

-- 3.7.1-skononova-3  изменение ссылок на ОКТМО в Параметрах подразделений.
declare 
  v_task_name varchar2(128):='insert_update_delete block #3 - change department_config';  
  cnt_u number := 0;
  cnt_i number := 0; 
    procedure convert_department_config (old_oktmo_code in varchar, new_oktmo_code in varchar, cnt_upd in out number, cnt_ins in out number) is
        cnt_r number;
    begin
        update department_config set end_date=to_date ('30.04.2019 23:59:59','dd.mm.yyyy hh24:mi:ss') where end_date is null
          and oktmo_id in (select id from ref_book_oktmo where code=old_oktmo_code);
        cnt_r := sql%rowcount;
        cnt_upd := cnt_upd + cnt_r;
        if  cnt_r>0 then
            insert into department_config (ID,KPP,OKTMO_ID,START_DATE,END_DATE,DEPARTMENT_ID,TAX_ORGAN_CODE,TAX_ORGAN_CODE_MID,PRESENT_PLACE_ID,NAME,
                                   PHONE,REORGANIZATION_ID,REORG_INN,REORG_KPP,SIGNATORY_ID,SIGNATORY_SURNAME,SIGNATORY_FIRSTNAME,SIGNATORY_LASTNAME,
                                   APPROVE_DOC_NAME,APPROVE_ORG_NAME,REORG_SUCCESSOR_KPP)
            select seq_department_config.nextval,KPP,(select id from ref_book_oktmo where code=new_oktmo_code),to_date('01.05.2019','dd.mm.yyyy'),null,DEPARTMENT_ID,TAX_ORGAN_CODE,
                   TAX_ORGAN_CODE_MID,PRESENT_PLACE_ID,NAME,PHONE,REORGANIZATION_ID,REORG_INN,REORG_KPP,SIGNATORY_ID,SIGNATORY_SURNAME,SIGNATORY_FIRSTNAME,
                   SIGNATORY_LASTNAME,APPROVE_DOC_NAME,APPROVE_ORG_NAME,REORG_SUCCESSOR_KPP from department_config 
            where trunc(end_date)=to_date ('30.04.2019','dd.mm.yyyy') 
            and oktmo_id in (select id from ref_book_oktmo where code=old_oktmo_code);
	    
	    cnt_ins := cnt_ins + sql%rowcount;	
        end if;  
    end;

begin
    convert_department_config(96612419,96612101,cnt_u, cnt_i);
    convert_department_config(96612419101,96612101001,cnt_u, cnt_i);
    convert_department_config(76636152,76636402,cnt_u, cnt_i);
    convert_department_config(76636152051,76636402101,cnt_u, cnt_i);
    convert_department_config(76636152106,76636402106,cnt_u, cnt_i);
    convert_department_config(76636158,76636452,cnt_u, cnt_i);
    convert_department_config(76636158051,76636452101,cnt_u, cnt_i);
    dbms_output.put_line(v_task_name ||': updated '||to_char (cnt_u) || ' records, inserted ' || to_char (cnt_i) || ' records');	
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/

COMMIT;
/

declare 
  v_task_name varchar2(128):='insert_update_delete block #4 - merge into ref_book_oktmo';  
begin
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

