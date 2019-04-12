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

select '"'||d.name||'";"'||dc.KPP||'";"'||to_char(dc.OKTMO)||'";"'||to_char(dc.VERSION,'dd.mm.yyyy')||'";"'||to_char(dc.VERSION_END,'dd.mm.yyyy')||'"' 
from 
VW_DEPARTMENT_CONFIG dc,
department d
where
d.id = dc.DEPARTMENT_ID
order by
d.name, OKTMO, KPP, VERSION;

spool off;

exit;