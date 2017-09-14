declare 
  v_count number;
BEGIN
	select count(1) into v_count from user_scheduler_jobs where job_name='REFRESH_FIAS_VIEWS';
	if v_count=0 then
		DBMS_SCHEDULER.CREATE_JOB (
		job_name           =>  'refresh_fias_views',
		job_type           =>  'PLSQL_BLOCK',
		job_action         =>  'BEGIN RefreshFiasViews; END;',
		repeat_interval    =>  'FREQ=DAILY;BYHOUR=15',
		comments           =>  'refresh fias views',
		enabled            =>  TRUE);
	end if;
	commit;
END;
/