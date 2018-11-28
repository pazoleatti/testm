declare 
    v_task_name varchar2(128):='ref_book_person_end_date #1  - update ref_book_person end_date'; 
    v_rows number;
	v_exist_status number(1) := 0;
	v_exist_version number(1) := 0;
	v_exist_end_date number(1) := 0;
    v_run_condition number(1) := 0;
    v_plsql_block VARCHAR2(32767);
begin
  
  select count(*) into v_exist_status from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='status';
  select count(*) into v_exist_version from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='version';
  select count(*) into v_exist_end_date from user_tab_columns where lower(table_name)='ref_book_person' and lower(column_name)='end_date';
  
  IF v_exist_status=1 AND v_exist_version=1 AND v_exist_end_date=1 THEN
  
	  v_rows := -1;
	  v_plsql_block := '
	  BEGIN
	  :v_count := 0;
	  FOR c1 IN (
				  select 
				  id,
				  record_id,
				  old_id,
				  version,
				  status,
				  next_version,
				  (trunc(next_version) - interval ''1'' day) end_date
				  from (  
				  select 
				  r.id,
				  r.record_id,
				  r.old_id,
				  r.version,
				  r.status,
				  lead(r.version) over (partition by r.record_id, r.old_id order by r.version asc, status desc) next_version
				  from ref_book_person r 
				  where 
				  r.status in (0,2)
				  and r.record_id in (select record_id from (select record_id, old_id, count(id) cnt from ref_book_person group by record_id, old_id having count(id) > 1))
				  order by r.record_id, r.old_id, r.version asc, status desc
				))
	  LOOP          
		IF c1.status = 0 and c1.end_date is not null THEN
			update ref_book_person r
			set r.end_date = c1.end_date
			where
			r.id = c1.id and r.record_id = c1.record_id and r.old_id = c1.old_id;
			:v_count := :v_count + 1;
		END IF;
	  END LOOP;
	  END;';
	  
	  EXECUTE IMMEDIATE v_plsql_block USING IN OUT v_rows;
		
		CASE v_rows 
		WHEN -1 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' Updating not executed');
		ELSE 
			CASE v_rows 
			WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
			ELSE
				dbms_output.put_line(v_task_name||'[INFO]:'||' Success. Updated '||to_char(v_rows)||' rows');
			END CASE;

			v_task_name :='ref_book_person_end_date block #2 - update REF_BOOK_ID_DOC person_id = null';  
			begin
				EXECUTE IMMEDIATE 'update REF_BOOK_ID_DOC a 
				set a.person_id = null
				where 
				exists (select 1 from REF_BOOK_PERSON p where p.status=2 and p.id=a.person_id)';
				
				CASE SQL%ROWCOUNT 
				WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
				ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
				END CASE; 
			EXCEPTION
			  when OTHERS then
				dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
			end;

			v_task_name :='ref_book_person_end_date block #3 - update DECLARATION_DATA_PERSON person_id = null';  
			begin
				select count(*) into v_run_condition from user_tables where lower(table_name)='declaration_data_person';
				IF v_run_condition=1 THEN
					EXECUTE IMMEDIATE 'update DECLARATION_DATA_PERSON b 
					set b.person_id = null
					where 
					exists (select 1 from REF_BOOK_PERSON p where p.status=2 and p.id=b.person_id)';
					
					CASE SQL%ROWCOUNT 
					WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
					ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
					END CASE; 
				ELSE
					dbms_output.put_line(v_task_name||'[WARNING]:'||' table "declaration_data_person" not found');
				END IF; 
			EXCEPTION
			  when OTHERS then
				dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
			end;

			v_task_name :='ref_book_person_end_date block #4 - update NDFL_PERSON person_id = null';  
			begin
				EXECUTE IMMEDIATE 'update NDFL_PERSON c
				set c.person_id = null
				where 
				c.status=0 and 
				exists (select 1 from REF_BOOK_PERSON p where p.status=2 and p.id=c.person_id)';
				
				CASE SQL%ROWCOUNT 
				WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
				ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
				END CASE; 
			EXCEPTION
			  when OTHERS then
				dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
			end;

			v_task_name :='ref_book_person_end_date block #5 - update NDFL_REFERENCES person_id = null';  
			begin
				EXECUTE IMMEDIATE 'update NDFL_REFERENCES d  
				set d.person_id = null
				where 
				d.status=0 and 
				exists (select 1 from REF_BOOK_PERSON p where p.status=2 and p.id=d.person_id)';
				
				CASE SQL%ROWCOUNT 
				WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
				ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
				END CASE; 
			EXCEPTION
			  when OTHERS then
				dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
			end;

			v_task_name :='ref_book_person_end_date block #6 - update REF_BOOK_ID_TAX_PAYER person_id = null';  
			begin
				select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_id_tax_payer' and lower(column_name)='status';
				IF v_run_condition=1 THEN
					EXECUTE IMMEDIATE 'update REF_BOOK_ID_TAX_PAYER e 
					set e.person_id = null
					where 
					e.status=0 and 
					exists (select 1 from REF_BOOK_PERSON p where p.status=2 and p.id=e.person_id)';
					
					CASE SQL%ROWCOUNT 
					WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
					ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
					END CASE; 
				ELSE
					dbms_output.put_line(v_task_name||'[WARNING]:'||' column "ref_book_id_tax_payer.status" not found');
				END IF; 
			EXCEPTION
			  when OTHERS then
				dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
			end;

			v_task_name :='ref_book_person_end_date block #7 - update REF_BOOK_PERSON_TB person_id = null';  
			begin
				select count(*) into v_run_condition from user_tab_columns where lower(table_name)='ref_book_person_tb' and lower(column_name)='status';
				IF v_run_condition=1 THEN
					EXECUTE IMMEDIATE 'update REF_BOOK_PERSON_TB f  
					set f.person_id = null
					where 
					f.status=0 and 
					exists (select 1 from REF_BOOK_PERSON p where p.status=2 and p.id=f.person_id)';
					
					CASE SQL%ROWCOUNT 
					WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
					ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
					END CASE; 
				ELSE
					dbms_output.put_line(v_task_name||'[WARNING]:'||' column "ref_book_person_tb.status" not found');
				END IF; 
			EXCEPTION
			  when OTHERS then
				dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
			end;

			v_task_name :='ref_book_person_end_date #8  - delete ref_book_person status=2'; 
			begin		
			  EXECUTE IMMEDIATE 'delete from ref_book_person where status = 2';
			  CASE SQL%ROWCOUNT 
			  WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
			  ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
			  END CASE; 
			EXCEPTION
			  when OTHERS then
				dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
			end;
		END CASE; 

	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||CASE WHEN v_exist_status=0 THEN ' Column "status" not found' ELSE '' END
		||CASE WHEN v_exist_version=0 THEN ' Column "version" not found' ELSE '' END
		||CASE WHEN v_exist_end_date=0 THEN ' Column "end_date" not found' ELSE '' END);
	END IF;
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;
