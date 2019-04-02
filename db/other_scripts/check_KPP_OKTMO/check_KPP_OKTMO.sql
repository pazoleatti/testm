set heading off;
set feedback off;
set verify off;
set termout off;
set linesize 2000;
set trimspool on;
set NEWP NONE;

column filename new_val filename

SELECT '_logs\KPP-OTMO_'||to_char(sysdate, 'yyyymmdd_hhmiss')||'.csv' filename FROM dual;

spool &filename


select '"Наличие";"КПП";"ОКТМО";"№ формы";"Подразделение";"Период";' from dual;

select '"'||exists_flag||'";"'||kpp||'";"'||oktmo||'";"'||to_char(declaration_data_id)||'";"'||dep_name||'";"'||rep_name||'"'
from (
select 
distinct 
kpp, oktmo, declaration_data_id, dep_name, rep_name, exists_flag
from (
select dd.kpp, dd.oktmo, dd.id as declaration_data_id, drp.department_id, d.name as dep_name, rp.id, to_char(tp.year)||' '||rp.name as rep_name, det.detail_id, det.start_date, det.end_date
,case when det.detail_id is null then 'отсутствует' else 'неактуальна' end exists_flag
from 
DECLARATION_DATA dd
,DEPARTMENT_REPORT_PERIOD drp
,department d
,REPORT_PERIOD rp
,tax_period tp
,(
select rbnd.id as detail_id, rbnd.record_id, rbnd.kpp, rbo.code as oktmo, rbnd.version as start_date
,(select min(rbnd2.version) from ref_book_ndfl_detail rbnd2 where rbnd2.status <> -1 and rbnd2.record_id = rbnd.record_id and rbnd2.version>rbnd.version) as end_date
from 
ref_book_ndfl_detail rbnd
,REF_BOOK_OKTMO rbo
where
rbo.id=rbnd.oktmo
and rbnd.status=0
) det
where
dd.kpp is not null and 
dd.oktmo is not null
and drp.id = dd.department_report_period_id
and d.id = drp.department_id
and rp.id=drp.report_period_id
and tp.id=rp.tax_period_id
and det.kpp(+) = dd.kpp
and det.oktmo(+) = dd.oktmo
)
where 
(detail_id is null)
or
(detail_id is not null and ((trunc(start_date) > trunc(sysdate)) or (trunc(nvl(end_date, sysdate+1)) < trunc(sysdate))))
)
order by kpp, oktmo, declaration_data_id;

spool off;

exit;