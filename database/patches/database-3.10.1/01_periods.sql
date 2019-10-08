-- 3.10.1-skononova-1

declare 
  v_task_name varchar2(128):='ddl_dml_periods block #1 - drop constraint'; 
  v_run_condition number(1);
begin
	select count ( * ) into v_run_condition from user_constraints where
		TABLE_NAME='REPORT_PERIOD' and CONSTRAINT_NAME='REPORT_PERIOD_UNIQ_TAX_DICT';
	
	IF v_run_condition>=1 THEN
		EXECUTE IMMEDIATE 'ALTER TABLE report_period DROP  CONSTRAINT report_period_uniq_tax_dict DROP INDEX';
	END IF;

	dbms_output.put_line(v_task_name||'[INFO (REPORT_PERIOD_UNIQ_TAX_DICT)]:'||' Success');

EXCEPTION
  when OTHERS then 
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;


declare 
  v_task_name varchar2(128):='ddl_dml_periods block #2 - update report periods';  
begin
	UPDATE report_period SET form_type_id=5 where form_type_id is null;		
	
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
  v_task_name varchar2(128):='ddl_dml_periods block #3 - create periods for 2-NDFL';  
begin
	INSERT INTO report_period (id, name, tax_period_id, dict_tax_period_id, start_date, end_date, calendar_start_date, form_type_id)
	    SELECT
        	seq_report_period.NEXTVAL,
	        name,
	        tax_period_id,
                dict_tax_period_id,
	        start_date,
	        end_date,
                calendar_start_date,
	        form_type
	    FROM
                (
	            SELECT DISTINCT
	                rp.name,
                        rp.tax_period_id,
	                rp.dict_tax_period_id,
	                rp.start_date,
                        rp.end_date,
	                rp.calendar_start_date,
	                decode(declaration_template_id, 102, 3, 104, 4, 105, 3,
                               NULL) AS form_type
	            FROM
	                report_period              rp
                        JOIN department_report_period   drp ON drp.report_period_id = rp.id
	                JOIN declaration_data           dd ON dd.department_report_period_id = drp.id
	                LEFT JOIN report_period              rp2 ON rp2.tax_period_id = rp.tax_period_id
                                                       AND rp2.dict_tax_period_id = rp.dict_tax_period_id
	                                               AND rp2.form_type_id = decode(declaration_template_id, 102, 3, 104, 4, 105, 3, 
	                                                                             NULL)
                    WHERE
	                (dd.declaration_template_id IN (
	                    102,
                            104
	                ) or (dd.declaration_template_id=105 AND NOT EXISTS 
				(SELECT 1 FROM declaration_data_consolidation ddc WHERE 
				ddc.target_declaration_data_id = dd.id
				AND ddc.source_declaration_data_id is not null)) ) 
	                AND rp2.id IS NULL
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


declare 
  v_task_name varchar2(128):='ddl_dml_periods block #4 - create department periods for 2-NDFL';  
begin

        INSERT INTO department_report_period (
            id,
            department_id,
            report_period_id,
            is_active,
            correction_date
        )
            SELECT
                seq_department_report_period.NEXTVAL,
                department_id,
                report_period_id,
                is_active,
                correction_date
            FROM
                (
                    SELECT DISTINCT
                        drp.department_id,
                        new_rp.id AS report_period_id,
                        drp.is_active,
                        drp.correction_date
                    FROM
                        department_report_period   drp
                        JOIN declaration_data           dd ON dd.department_report_period_id = drp.id
                        JOIN report_period              old_rp ON old_rp.id = drp.report_period_id
                        JOIN report_period              new_rp ON new_rp.tax_period_id = old_rp.tax_period_id
                                                     AND new_rp.dict_tax_period_id = old_rp.dict_tax_period_id
                                                     AND new_rp.form_type_id = decode(declaration_template_id, 102, 3, 104, 4,105, 3,
                                                                                              NULL)
                        LEFT JOIN department_report_period   drp2 ON drp2.department_id = drp.department_id
                                                                   AND drp2.report_period_id = new_rp.id
                                                                   AND drp2.is_active = drp.is_active
                                                                   AND ( drp2.correction_date IS NULL
	                                                                 AND drp.correction_date IS NULL
        	                                                         OR drp2.correction_date = drp.correction_date 
									)
                    WHERE
	                (dd.declaration_template_id IN (
	                    102,
                            104
	                ) or (dd.declaration_template_id=105 AND NOT EXISTS 
				(SELECT 1 FROM declaration_data_consolidation ddc WHERE 
				ddc.target_declaration_data_id = dd.id
				AND ddc.source_declaration_data_id is not null)) ) 
			and drp2.id is null
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

declare 
  v_task_name varchar2(128):='ddl_dml_periods block #5 - update periods for 2-NDFL';  
begin

        UPDATE declaration_data d_d
        SET
            department_report_period_id = (
                SELECT
                    new_drp.id
                FROM
                    declaration_data           dd
                    JOIN department_report_period   drp ON drp.id = dd.department_report_period_id
                    JOIN report_period              rp ON rp.id = drp.report_period_id
                    JOIN report_period              new_rp ON rp.tax_period_id = new_rp.tax_period_id
                                                 AND rp.dict_tax_period_id = new_rp.dict_tax_period_id
                                                 AND new_rp.form_type_id = decode(declaration_template_id, 102, 3, 104, 4, 105, 3, 
                                                                                  NULL)
                    JOIN department_report_period   new_drp ON drp.department_id = new_drp.department_id
                                                             AND new_drp.report_period_id = new_rp.id
                                                             AND drp.is_active = new_drp.is_active
                                                             AND ( drp.correction_date IS NULL
                                                                   AND new_drp.correction_date IS NULL
                                                                   OR drp.correction_date = new_drp.correction_date )
                        WHERE
                    d_d.id = dd.id
            )
        WHERE
	                (d_d.declaration_template_id IN (
	                    102,
                            104
	                ) or (d_d.declaration_template_id=105 AND NOT EXISTS 
				(SELECT 1 FROM declaration_data_consolidation ddc WHERE 
				ddc.target_declaration_data_id = d_d.id
				AND ddc.source_declaration_data_id is not null)) ) 

                    AND NOT EXISTS (
                SELECT
                    1
                FROM
                    department_report_period   drp
                    JOIN report_period              rp ON drp.report_period_id = rp.id
                WHERE
                    drp.id = d_d.department_report_period_id
                    AND rp.form_type_id = decode(declaration_template_id, 102, 3, 104, 4, 105, 3, 
                                                         NULL)
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

declare 
  v_task_name varchar2(128):='ddl_dml_periods block #6 - update periods for 2-NDFL (FL)';  
begin

	UPDATE declaration_data dd
	SET
	    department_report_period_id = (
	        SELECT
	            dd2.department_report_period_id
	        FROM
            declaration_data_consolidation   ddc
	            JOIN declaration_data                 dd2 ON dd2.id = ddc.source_declaration_data_id
	        WHERE
	            dd.id = ddc.target_declaration_data_id
	    )
	WHERE
	    dd.declaration_template_id = 105 and exists (SELECT 1 FROM declaration_data_consolidation ddc WHERE 
				ddc.target_declaration_data_id = dd.id
				AND ddc.source_declaration_data_id is not null);

	
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
  v_task_name varchar2(128):='ddl_dml_periods block #7 - add constraint'; 
  v_run_condition number(1);
begin
	select count ( * ) into v_run_condition from user_constraints where
		TABLE_NAME='REPORT_PERIOD' and CONSTRAINT_NAME='REP_PER_UNIQ_TAX_DICT_FRM_TYPE';
	
	IF v_run_condition>=1 THEN
		EXECUTE IMMEDIATE 'ALTER TABLE report_period ADD CONSTRAINT rep_per_uniq_tax_dict_frm_type UNIQUE (tax_period_id, dict_tax_period_id, form_type_id)';
		dbms_output.put_line(v_task_name||'[INFO (REP_PER_UNIQ_TAX_DICT_FRM_TYPE)]:'||' Success');
	else

	dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');

	END IF ;

EXCEPTION
  when OTHERS then 
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

