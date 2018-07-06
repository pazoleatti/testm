set heading off;
set feedback off;
set verify off;
set termout off;
set linesize 200;
set trimspool on;
set NEWP NONE;

spool 'department_details.csv'

select '"'||dep.sbrf_code||'","'||rnd.kpp||'","'||ro.code||'"' from ref_book_ndfl_detail rnd left join ref_book_oktmo ro on ro.id = rnd.oktmo left join department dep on rnd.department_id = dep.id;

	
spool off;

exit;	