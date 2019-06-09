declare
	v_run_condition number(1);
	v_task_name varchar2(128):='insert_update_delete block #1 - insert into async_task_type';  
begin	
	select decode(count(*),0,1,0) into v_run_condition from async_task_type where id=38;
	IF v_run_condition=1 THEN
		--https://jira.aplana.com/browse/SBRFNDFL-3927 Реализовать выгрузку настроек подразделений в файл Excel
		insert into async_task_type(id,name,handler_bean,limit_kind) 
		values(38,'Выгрузка настроек подразделений в файл формата XLSX','ExcelReportDepartmentConfigsAsyncTask','количество отобранных для выгрузки в файл записей');
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_run_condition number(1);
	v_task_name varchar2(128):='insert_update_delete block #2 - insert into async_task_type';  
begin	
	select decode(count(*),0,1,0) into v_run_condition from async_task_type where id=39;
	IF v_run_condition=1 THEN
		--https://jira.aplana.com/browse/SBRFNDFL-5523 Реализовать загрузку настроек подразделений из файла Excel
		insert into async_task_type(id,name,handler_bean,limit_kind) 
		values(39,'Загрузка настроек подразделений из Excel файла','ImportExcelDepartmentConfigsAsyncTask','Размер файла (Кбайт)');
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #3 - update ref_book';  
begin	
	-- https://jira.aplana.com/browse/SBRFNDFL-5523 Реализовать загрузку настроек подразделений из файла Excel
    update ref_book set script_id = '38454eea-0595-47d5-8aff-5811a136888c' where id = 951;

	CASE SQL%ROWCOUNT 
	WHEN 1 THEN dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE dbms_output.put_line(v_task_name||'[ERROR]:'||' No changes was done');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #4 - update ref_book';  
begin	
	-- https://jira.aplana.com/browse/SBRFNDFL-5632 Убрать из списка справочников псевдосправочник "Список тербанков назначенных ФЛ"
    update ref_book set visible = 0 where id = 908;

	CASE SQL%ROWCOUNT 
	WHEN 1 THEN dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE dbms_output.put_line(v_task_name||'[ERROR]:'||' No changes was done');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;
