set heading off;
set feedback off;
set verify off;
set termout off;
set linesize 2000;
set trimspool on;
set NEWP NONE;
WHENEVER SQLERROR EXIT;

spool &1

select '"DECLARATION_DATA_ID";"BAD_R2"' from dual;

select '"'||to_char(np.declaration_data_id)||'";"'||to_char(count(npi.id))||'"'
from DECLARATION_DATA dd, NDFL_PERSON np, NDFL_PERSON_INCOME npi 
where
np.declaration_data_id = dd.id and
npi.ndfl_person_id = np.id and
instr(npi.operation_id, '_0_') > 0
group by np.declaration_data_id;

spool off;

exit;	