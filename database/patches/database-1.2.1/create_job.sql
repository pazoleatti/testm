set serveroutput on;

declare 
  v_count number;
BEGIN
	select count(1) into v_count from user_scheduler_jobs where job_name='REFRESH_FIAS_VIEWS';
	if v_count=0 then
		DBMS_SCHEDULER.CREATE_JOB (
		job_name           =>  'refresh_fias_views',
		job_type           =>  'PLSQL_BLOCK',
		job_action         =>  'BEGIN 
  DBMS_MVIEW.REFRESH(list=>''MV_FIAS_CITY_ACT'',method => ''C'', atomic_refresh => FALSE);
  DBMS_MVIEW.REFRESH(list=>''MV_FIAS_AREA_ACT'',method => ''C'', atomic_refresh => FALSE);
  DBMS_MVIEW.REFRESH(list=>''MV_FIAS_LOCALITY_ACT'',method => ''C'', atomic_refresh => FALSE);
  DBMS_MVIEW.REFRESH(list=>''MV_FIAS_STREET_ACT'',method => ''C'', atomic_refresh => FALSE);
END;',
		repeat_interval    =>  'FREQ=DAILY;BYHOUR=1',
		comments           =>  'refresh fias views',
		enabled            =>  TRUE);
		dbms_output.put_line('job created');
	else
		DBMS_SCHEDULER.SET_ATTRIBUTE (
			name         =>  'REFRESH_FIAS_VIEWS',
			attribute    =>  'job_action',
			value        =>  
'BEGIN 
  DBMS_MVIEW.REFRESH(list=>''MV_FIAS_CITY_ACT'',method => ''C'', atomic_refresh => FALSE);
  DBMS_MVIEW.REFRESH(list=>''MV_FIAS_AREA_ACT'',method => ''C'', atomic_refresh => FALSE);
  DBMS_MVIEW.REFRESH(list=>''MV_FIAS_LOCALITY_ACT'',method => ''C'', atomic_refresh => FALSE);
  DBMS_MVIEW.REFRESH(list=>''MV_FIAS_STREET_ACT'',method => ''C'', atomic_refresh => FALSE);
END;'
		);
		dbms_output.put_line('job altered');
	end if;
	commit;
END;
/