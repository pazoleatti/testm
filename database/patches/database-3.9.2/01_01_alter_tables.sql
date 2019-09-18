-- 3.9.2-adudenko-1
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #1 - alter table department_config';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where TABLE_NAME='DEPARTMENT_CONFIG' and COLUMN_NAME='RELATED_KPP';
	IF v_run_condition=1 THEN
           	execute immediate 'alter table department_config add related_kpp varchar2(9 char)';
                execute immediate 'comment on column department_config.related_kpp is ''Поле для формирования настройки подразделения Учитывать в КПП/ОКТМО''';
		dbms_output.put_line(v_task_name||'[INFO (related_kpp)]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING (related_kpp)]:'||' changes had already been implemented');
	END IF;

/*	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where TABLE_NAME='DEPARTMENT_CONFIG' and COLUMN_NAME='RELATED_OKTMO_ID';
	IF v_run_condition=1 THEN
            	execute immediate 'alter table department_config add related_oktmo_id varchar2(11 char)';
	        execute immediate 'comment on column department_config.related_oktmo_id is ''Поле для формирования настройки подразделения Учитывать в КПП/ОКТМО''';
		dbms_output.put_line(v_task_name||'[INFO (related_oktmo)]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING (related_oktmo)]:'||' changes had already been implemented');
	END IF;
*/	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

COMMIT;
