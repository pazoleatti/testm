-- 3.10-mchernyakov-01

declare 
  v_task_name varchar2(128):='insert_update_delete block #2 - merge into async_task_type';  
begin
	merge into async_task_type dst using
	(select 45 as id, 'Выгрузка списка источники-приемники в файл формата XLSX' as name,
	'CreateUnloadListAsyncTask' as handler_bean, '3000' as short_queue_limit,
	'1000000' as task_limit, 'количество отобранных для выгрузки в файл записей' as limit_kind from dual 
	) src
	on (src.id=dst.id)
	when not matched then
		insert (ID, NAME, HANDLER_BEAN, SHORT_QUEUE_LIMIT, TASK_LIMIT, LIMIT_KIND)
		values (src.ID, src.NAME, src.HANDLER_BEAN, src.SHORT_QUEUE_LIMIT, src.TASK_LIMIT, src.LIMIT_KIND)
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


-- 3.10-mchernyakov-02

declare 
  v_task_name varchar2(128):='insert_update_delete block #3 - merge into event';  
begin
	merge into event dst using
	(select 10015 as id, 'Выгрузка списка источники приемники из налоговой формы' as name from dual 
	) src
	on (src.id=dst.id)
	when not matched then
		insert (ID, NAME)
		values (src.ID, src.NAME)
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

--3.10 - skononova-3
begin
	dbms_output.put_line('Update Deductions. Please wait...');
end;                                 
/
declare 
  v_task_name varchar2(128):='insert_update_delete block #3 - update decuctions';  
begin

	UPDATE ndfl_person_deduction npd
	SET
	    source_id = (
	        SELECT
	            MIN(npdp.id)
	        FROM
	            ndfl_person_deduction   npdc
	            JOIN ndfl_person_income      npic ON npic.ndfl_person_id = npdc.ndfl_person_id
	                                            AND npdc.operation_id = npic.operation_id
	                                            AND npdc.asnu_id = npic.asnu_id
	            JOIN ndfl_person_income      npip ON npip.id = npic.source_id
	            JOIN ndfl_person_deduction   npdp ON npdp.ndfl_person_id = npip.ndfl_person_id
	                                               AND npdc.operation_id = npdp.operation_id
	                                               AND npdc.asnu_id = npdp.asnu_id
	                                               AND npdc.type_code = npdp.type_code
	                                               AND npdc.period_curr_summ = npdp.period_curr_summ
						       AND npdc.period_curr_date = npdp.period_curr_date     
	        WHERE
	            npd.id = npdc.id
	    )
	WHERE
	    npd.source_id IS NULL
	    AND EXISTS (
	        SELECT
	            1
	        FROM
	            ndfl_person        npc
	            JOIN declaration_data   ddc ON npc.declaration_data_id = ddc.id
	        WHERE
	            ddc.declaration_template_id = 101
	            AND npc.id = npd.ndfl_person_id
	    );

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

begin
	dbms_output.put_line('Update Prepayments. Please wait...');
end;                                 
/
declare 
  v_task_name varchar2(128):='insert_update_delete block #3 - update prepayments';  
begin

		UPDATE ndfl_person_prepayment npp
		SET
		    source_id = (
		        SELECT
	        	    MIN(nppp.id)
		        FROM
		            ndfl_person_prepayment   nppc
	        	    JOIN ndfl_person_income      npic ON npic.ndfl_person_id = nppc.ndfl_person_id
	                	                            AND nppc.operation_id = npic.operation_id
	                        	                    AND nppc.asnu_id = npic.asnu_id
		            JOIN ndfl_person_income      npip ON npip.id = npic.source_id
		            JOIN ndfl_person_prepayment   nppp ON nppp.ndfl_person_id = npip.ndfl_person_id
	        	                                       AND nppc.operation_id = nppp.operation_id
	                	                               AND nppc.asnu_id = nppp.asnu_id
	                        	                       
                                        	           AND nppc.NOTIF_NUM = nppp.NOTIF_NUM
	                                        	       AND nppc.summ = nppp.summ
	                                                   AND nppc.NOTIF_SOURCE = nppp.NOTIF_SOURCE
								AND nppc.NOTIF_DATE = nppp.NOTIF_DATE
		        WHERE
	        	    npp.id = nppc.id
		    )
		WHERE
		    npp.source_id IS NULL
		    AND EXISTS (
		        SELECT
		            1
	        	FROM
		            ndfl_person        npc
		            JOIN declaration_data   ddc ON npc.declaration_data_id = ddc.id
	        	WHERE
		            ddc.declaration_template_id = 101
		            AND npc.id = npp.ndfl_person_id
		    );

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


--3.10-avoynov-2
declare 
  v_task_name varchar2(128):='insert_update_delete block #3 - merge into ref_book_attribute';  
begin
	merge into ref_book_attribute dst using
	(select 9650 as id, 964 as ref_book_id, 'Номер корректировки' as name,
	'CORRECTION_NUM' as alias, 2 as type, 10 as ord, 0 as precision, 5 as width, 1 as required,
	0 as is_unique, 3 as max_length  from dual ) src
	on (src.id=dst.id)
	when not matched then
		insert (id, ref_book_id, name, alias, type, ord, precision, width, required, 
	is_unique, max_length)
		values (src.id, src.ref_book_id, src.name, src.alias, src.type, src.ord, src.precision, 
			src.width, src.required,  src.is_unique, src.max_length);
	
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


