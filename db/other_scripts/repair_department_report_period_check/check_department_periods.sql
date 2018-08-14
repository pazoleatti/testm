set heading off;
set feedback off;
set verify off;
set termout off;
set linesize 2000;
set trimspool on;
set NEWP NONE;
spool &1

select '"PARENT_DEPARTMENT_ID";"PARENT_NAME";"CHILD_ID";"CHILD_NAME";"REPORT_PERIOD_ID";"PERIOD_NAME";"PERIOD_YEAR";"IS_ACTIVE";"CORRECTION_DATE";"OPENED";"OLD_CORRECTION_DATE";"NOT_UNIQUE_DATA"' from dual;
select '"'||to_char(parent_id)||'";"'||to_char(parent_name)||'";"'||to_char(child_id)||'";"'||to_char(child_name)||'";"'||to_char(report_period_id)||'";"'||to_char(period_name)||'";"'||to_char(period_year)||'";"'||to_char(is_active)||'";"'||to_char(correction_date)||'";"'||to_char(opened)||'";"'||to_char(old_correction_date)||'";"'||to_char(not_unique_corr_date)||'"' 
from (
select parent_id,parent_name,child_id,child_name,report_period_id,period_name,period_year,is_active,correction_date,
  (case when is_active=0
   then '' 
   else (select to_char(ltrim(rtrim(xmlagg(xmlelement(e,correction_date,',').extract('//text()') order by correction_date nulls first).getclobval(),','),',')) from department_report_period
         where department_id = a.child_id and report_period_id = a.report_period_id and is_active = 1) end) as opened,
  (case when is_active=0
   then ''
   else (select to_char(ltrim(rtrim(xmlagg(xmlelement(e,correction_date,',').extract('//text()') order by correction_date nulls first).getclobval(),','),',')) from department_report_period
         where department_id = a.child_id and report_period_id = a.report_period_id and trunc(correction_date)>a.correction_date) end) as old_correction_date,
  (select to_char(ltrim(rtrim(xmlagg(xmlelement(e,correction_date,',').extract('//text()') order by correction_date nulls first).getclobval(),','),',')) from department_report_period
   where department_id = a.child_id and report_period_id = a.report_period_id and trunc(correction_date)=a.correction_date) as not_unique_corr_date         
  from (
	select d_parent.id as parent_id,d_parent.name as parent_name,d_child.id as child_id,d_child.name as child_name,drp_parent.report_period_id,rp.name as period_name,tp.year as period_year,drp_parent.is_active,drp_parent.correction_date from department_child_view dcv
	join department d_parent on dcv.parent_id=d_parent.id and d_parent.type=2
	join department_report_period drp_parent on drp_parent.department_id=d_parent.id
	join department d_child on dcv.id=d_child.id
  join report_period rp on rp.id=drp_parent.report_period_id
  join tax_period tp on tp.id=rp.tax_period_id
	where not exists (select * from department_report_period drp_child 
			where drp_child.department_id=d_child.id and drp_child.report_period_id=drp_parent.report_period_id and 
				(drp_child.correction_date=drp_parent.correction_date or (drp_child.correction_date is null and drp_parent.correction_date is null)))
	order by d_child.id,drp_parent.report_period_id,drp_parent.correction_date nulls first) a ) b;

spool off;

exit;