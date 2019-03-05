set heading off;
set feedback off;
set verify off;
set termout off;
set linesize 2000;
set trimspool on;
set NEWP NONE;
WHENEVER SQLERROR EXIT;

variable v_count number;

exec :v_count:=0;

select count(*) into :v_count from REF_BOOK_OKTMO;

spool &1;

select '"ID";"CODE";"NAME";"VERSION";"STATUS";"RECORD_ID";"RAZD"' from dual;

select '"'||to_char(ID)||'";"'||CODE||'";"'||replace(NAME,'"','""')||'";"'||to_char(VERSION,'dd.mm.yyyy')||'";"'||to_char(STATUS)||'";"'||to_char(RECORD_ID)||'";"'||to_char(RAZD)||'"' 
from REF_BOOK_OKTMO;

spool off;

exit;	