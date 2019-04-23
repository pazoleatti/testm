set heading off;
set feedback off;
set verify off;
set termout off;
set linesize 2000;
set trimspool on;
set NEWP NONE;

column filename new_val filename

SELECT '_logs\KPP-OKTMO_'||to_char(sysdate, 'yyyymmdd_hh24miss')||'.csv' filename FROM dual;

spool &filename


select '"Наличие";"КПП";"ОКТМО";"№ формы";"Подразделение";"Период";' from dual;

select '"'||exists_flag||'";"'||kpp||'";"'||oktmo||'";"'||to_char(declaration_data_id)||'";"'||dep_name||'";"'||rep_name||'"'
from (
select 
distinct 
kpp, oktmo, declaration_data_id, dep_name, rep_name, exists_flag
from (
select npi.kpp, npi.oktmo, dd.id as declaration_data_id, drp.department_id, d.name as dep_name, rp.id, to_char(tp.year)||' '||rp.name as rep_name, det.detail_id, det.start_date, det.end_date
,case when det.detail_id is null then 'отсутствует' else 'неактуальна' end exists_flag
from 
ndfl_person_income npi,
ndfl_person np,
declaration_data dd
,department_report_period drp
,department d
,report_period rp
,tax_period tp
,(
select rbnd.id as detail_id, rbnd.record_id, rbnd.kpp, rbo.code as oktmo, rbnd.version as start_date
,(select min(rbnd2.version) from ref_book_ndfl_detail rbnd2 where rbnd2.status <> -1 and rbnd2.record_id = rbnd.record_id and rbnd2.version>rbnd.version) as end_date
from 
ref_book_ndfl_detail rbnd
,ref_book_oktmo rbo
where
rbo.id=rbnd.oktmo
and rbnd.status=0
) det
where
npi.kpp  is not null and 
npi.oktmo is not null
and np.id = npi.ndfl_person_id
and dd.id = np.declaration_data_id
and drp.id = dd.department_report_period_id
and d.id = drp.department_id
and rp.id=drp.report_period_id
and tp.id=rp.tax_period_id
and det.kpp(+) = npi.kpp
and det.oktmo(+) = npi.oktmo
)
where 
(detail_id is null)
or
(detail_id is not null and (trunc(start_date) <= trunc(sysdate, 'Y') and trunc(nvl(end_date, sysdate+1)) < trunc(sysdate, 'Y')))
)
order by kpp, oktmo, declaration_data_id;

spool off;

exit;