set heading off;
set feedback off;
set verify off;
set termout off;
set linesize 2000;
set trimspool on;
set NEWP NONE;
spool &1

select '"parent_id";"parent_name";"parent_report_period";"parent_correction_date";"child_id";"child_name"' from dual;

select '"'||to_char(d_parent.id)||'";"'||to_char(d_parent.name)||'";"'||to_char(drp_parent.report_period_id)||'";"'||to_char(drp_parent.correction_date,'DD.MM.YYYY')||'";"'||to_char(d_child.id)||'";"'||to_char(d_child.name)||'"' from department_child_view dcv
join department d_parent on dcv.parent_id=d_parent.id and d_parent.type=2
join department_report_period drp_parent on drp_parent.department_id=d_parent.id
join department d_child on dcv.id=d_child.id
where not exists (select * from department_report_period drp_child 
                               where drp_child.department_id=d_child.id and drp_child.report_period_id=drp_parent.report_period_id and 
                                           (drp_child.correction_date=drp_parent.correction_date or (drp_child.correction_date is null and drp_parent.correction_date is null)))
order by d_parent.id,drp_parent.report_period_id,drp_parent.correction_date,d_child.id;
	
spool off;

exit;	