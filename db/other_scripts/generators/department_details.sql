set heading off;
set feedback off;
set verify off;
set termout off;
set linesize 200;
set trimspool on;
set NEWP NONE;

spool 'department_details.csv'

select '"'||dep.sbrf_code||'","'||rnd.kpp||'","'||ro.code||'"'
from (select dc.* from department_config dc) rnd
left join ref_book_oktmo ro on ro.id = rnd.oktmo_id
left join department dep on rnd.department_id = dep.id
where rnd.end_date is null or to_date('31.12.2019','dd.mm.yyyy') <= rnd.end_date;

	
spool off;

exit;	