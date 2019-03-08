-- 3.5-dnovikov-2, 3.5-dnovikov-7 https://jira.aplana.com/browse/SBRFNDFL-6452 Реализовать изменение определения настроек подразделений ТБ при консолидации
create or replace view vw_department_config as
select * from (
	select rbnd.*, lead(version) over(partition by rbnd.record_id order by version) - interval '1' DAY version_end
	from ref_book_ndfl_detail rbnd
	where status != -1
) where status = 0;

-- 3.5-dnovikov-8 https://jira.aplana.com/browse/SBRFNDFL-6452 Реализовать изменение определения настроек подразделений ТБ при консолидации
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='create synonym department_config for vw_department_config';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_synonyms where lower(table_name)='vw_department_config';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'create synonym department_config for vw_department_config';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
COMMIT;

