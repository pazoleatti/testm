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

select count(*) into :v_count from REF_BOOK_PERSON p 
where 
p.status=0 and 
p.start_date is not null and
p.end_date is not null and
p.start_date > p.end_date;

spool &1

select '"id";"last_name";"first_name";"middle_name";"inn";"inn_foreign";"snils";"taxpayer_state";"birth_date";"birth_place";"citizenship";"address";"record_id";"source_id";"old_id";"report_doc";"vip";"start_date";"end_date"' from dual;

select 
'"'||to_char(id)||'";"'||last_name||'";"'||first_name||'";"'||middle_name||'";"'||inn||'";"'||inn_foreign||'";"'||snils||'";"'||to_char(taxpayer_state)||'";"'||to_char(birth_date,'DD.MM.YYYY')
||'";"'||birth_place||'";"'||to_char(citizenship)||'";"'||to_char(address)||'";"'||to_char(record_id)||'";"'||to_char(source_id)||'";"'||to_char(old_id)||'";"'||to_char(report_doc)||'";"'||to_char(vip)||'";"'||to_char(start_date,'DD.MM.YYYY')||'";"'||to_char(end_date,'DD.MM.YYYY')||'"'
from REF_BOOK_PERSON p 
where 
p.status=0 and 
p.start_date is not null and
p.end_date is not null and
p.start_date > p.end_date;

spool off;

exit;	