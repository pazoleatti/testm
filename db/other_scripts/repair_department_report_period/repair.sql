set serveroutput on;

begin
	insert into department_report_period(id,department_id,report_period_id,is_active,correction_date)
	select seq_department_report_period.nextval,d_child.id,drp_parent.report_period_id,drp_parent.is_active,drp_parent.correction_date from department_child_view dcv
	join department d_parent on dcv.parent_id=d_parent.id and d_parent.type=2
	join department_report_period drp_parent on drp_parent.department_id=d_parent.id
	join department d_child on dcv.id=d_child.id
	where not exists (select * from department_report_period drp_child 
			where drp_child.department_id=d_child.id and drp_child.report_period_id=drp_parent.report_period_id and 
				(drp_child.correction_date=drp_parent.correction_date or (drp_child.correction_date is null and drp_parent.correction_date is null)));

	DBMS_OUTPUT.PUT_LINE('Inserted ' || SQL%ROWCOUNT || ' rows into DEPARTMENT_REPORT_PERIOD.');
	
	commit;
end;
/

exit;
