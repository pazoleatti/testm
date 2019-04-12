set heading off;
set feedback off;
set verify off;
set termout off;
set linesize 2000;
set trimspool on;
set NEWP NONE;

column filename new_val filename

SELECT '_logs\TB_KPP-OKTMO_'||to_char(sysdate, 'yyyymmdd_hh24miss')||'.csv' filename FROM dual;

spool &filename

select '"Наименование ТБ";"КПП";"ОКТМО";"Действует с";"Действует по"' from dual;

select '"'||d.name||'";"'||dc.kpp||'";"'||o.code||'";"'||to_char(dc.version,'dd.mm.yyyy')||'";"'||to_char(dc.version_end,'dd.mm.yyyy')||'"' 
from 
vw_department_config dc,
department d, 
ref_book_oktmo o
where
d.id = dc.department_id and
o.id = dc.oktmo
order by
d.name, o.code, dc.kpp, dc.version;

spool off;

exit;