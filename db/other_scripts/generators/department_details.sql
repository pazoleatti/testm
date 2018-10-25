set heading off;
set feedback off;
set verify off;
set termout off;
set linesize 200;
set trimspool on;
set NEWP NONE;

spool 'department_details.csv'

select '"'||dep.sbrf_code||'","'||rnd.kpp||'","'||ro.code||'"'
from (select t.*, lead(t.version) over(partition by t.record_id order by version) - interval '1' DAY version_end from ref_book_ndfl_detail t where status != -1) rnd
left join ref_book_oktmo ro on ro.id = rnd.oktmo
left join department dep on rnd.department_id = dep.id
where rnd.status = 0 and rnd.version <= to_date('31.12.2018','dd.mm.yyyy') and (rnd.version_end is null or to_date('31.12.2018','dd.mm.yyyy') <= rnd.version_end);

	
spool off;

exit;	