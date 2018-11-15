--https://jira.aplana.com/browse/SBRFNDFL-5837 Конвертировать данные реестра ФЛ
declare 
	v_task_name varchar2(128):='migrate address fields to ref_book_person block #1 - update ref_book_person address fields';  
	v_address_cnt number;
	v_address_migrate number;
	v_run_condition number(1):=0;
    v_plsql_block VARCHAR2(32767);
begin
      v_address_cnt := 0;
      v_address_migrate := 0;

    v_run_condition := 0;
	select count(1) into v_run_condition from user_tables where lower(table_name)='ref_book_address';
	if v_run_condition=1 then
	  
      EXECUTE IMMEDIATE 'select count(*)  
	  from ref_book_person r
	  join ref_book_address a on (a.id = r.address) 
	  where r.address is not null' INTO v_address_cnt;
	  
	  v_plsql_block := '
	BEGIN
	  for c1 in (
				  select
				  r.id ref_book_person_id
				  ,a.REGION_CODE
				  ,a.POSTAL_CODE
				  ,a.DISTRICT
				  ,a.CITY
				  ,a.LOCALITY
				  ,a.STREET
				  ,a.HOUSE
				  ,a.BUILD
				  ,a.APPARTMENT
				  ,a.COUNTRY_ID
				  ,a.ADDRESS
				  ,a.address_type
				  ,a.id ref_book_address_id
				  from ref_book_person r
				  join ref_book_address a on (a.id = r.address) 
				  where r.address is not null
				)
	  loop
		
		 update ref_book_person p
		 set
			 p.REGION_CODE = c1.region_code
			,p.POSTAL_CODE = c1.postal_code
			,p.DISTRICT = c1.district
			,p.CITY = c1.city
			,p.LOCALITY = c1.locality
			,p.STREET = c1.street
			,p.HOUSE = c1.house
			,p.BUILD = c1.build
			,p.APPARTMENT = c1.appartment
			,p.COUNTRY_ID = c1.country_id
		 where p.id = c1.ref_book_person_id
		   and c1.ref_book_address_id is not null;

		 update ref_book_person p
		 set
			 p.ADDRESS_FOREIGN = c1.address
		 where p.id = c1.ref_book_person_id
		   and c1.address_type <> 0
		   and c1.ref_book_address_id is not null;
		   
         :v_count := :v_count + 1;

	  end loop;
	 END;';
	 
	 EXECUTE IMMEDIATE v_plsql_block USING IN OUT v_address_migrate;

	 
	  IF v_address_migrate = v_address_cnt THEN
	     dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
       	 v_task_name := 'migrate address fields to ref_book_person block #2 - drop table ref_book_address (SBRFNDFL-5837)';  
		 begin
			v_run_condition := 0;
			select count(*) into v_run_condition from user_tables where lower(table_name)='ref_book_address';
			IF v_run_condition=1 THEN
				execute immediate 'DROP TABLE ref_book_address CASCADE CONSTRAINTS';
 			    v_run_condition := 0;
				select count(1) into v_run_condition from user_tables where lower(table_name)='ref_book_address';
				if v_run_condition=0 then
					dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
			    else
					dbms_output.put_line(v_task_name||'[INFO]:'||' Failed');
				end if;
			ELSE
				dbms_output.put_line(v_task_name||'[WARNING]:'||' table not found');
			END IF;
         exception			
		  when OTHERS then
			dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
		 end;
	  ELSE
	     dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	  END IF;
	else
		dbms_output.put_line(v_task_name||'[WARNING]:'||' "ref_book_address" table not found');
	end if;
	
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;
