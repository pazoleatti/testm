DECLARE
	v_id number;
BEGIN
	update report_period_type set name='первый квартал при реорганизации (ликвидации) организации' where code='51';
	update report_period_type set name='полугодие при реорганизации (ликвидации) организации' where code='52';
	update report_period_type set name='9 месяцев при реорганизации (ликвидации) организации' where code='53';
	update report_period_type set calendar_start_date=to_date('01.01.1970','DD.MM.YYYY') where code='90';
	
END;
/