BEGIN
	DBMS_SCHEDULER.SET_ATTRIBUTE (
		name         =>  'REFRESH_FIAS_VIEWS',
		attribute    =>  'job_action',
		value        =>  'BEGIN 
  DBMS_MVIEW.REFRESH(list=>''MV_FIAS_CITY_ACT'',method => ''C'', atomic_refresh => FALSE);
  DBMS_MVIEW.REFRESH(list=>''MV_FIAS_AREA_ACT'',method => ''C'', atomic_refresh => FALSE);
  DBMS_MVIEW.REFRESH(list=>''MV_FIAS_LOCALITY_ACT'',method => ''C'', atomic_refresh => FALSE);
  DBMS_MVIEW.REFRESH(list=>''MV_FIAS_STREET_ACT'',method => ''C'', atomic_refresh => FALSE);
END;');
END;
/