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

select count(*) into :v_count 
from REF_BOOK_PERSON p 
where 
exists (select 1 from REF_BOOK_ID_DOC g where g.status<>0 and g.id=p.report_doc)
and record_id is not null
and version is not null
and status = 0;

spool &7

select '"id";"last_name";"first_name";"middle_name";"inn";"inn_foreign";"snils";"taxpayer_state";"birth_date";"birth_place";"citizenship";"address";"record_id";"version";"status";"source_id";"old_id";"report_doc";"vip";"start_date";"end_date"' from dual;

select 
'"'||to_char(id)||'";"'||last_name||'";"'||first_name||'";"'||middle_name||'";"'||inn||'";"'||inn_foreign||'";"'||snils||'";"'||to_char(taxpayer_state)
||'";"'||to_char(birth_date,'DD.MM.YYYY')||'";"'||birth_place||'";"'||to_char(citizenship)||'";"'||to_char(address)||'";"'||to_char(record_id)||'";"'||to_char(version,'DD.MM.YYYY')
||'";"'||to_char(status)||'";"'||to_char(source_id)||'";"'||to_char(old_id)||'";"'||to_char(report_doc)||'";"'||to_char(vip)||'";"'||to_char(start_date,'DD.MM.YYYY')||'";"'||to_char(end_date,'DD.MM.YYYY')||'"'
from REF_BOOK_PERSON p 
where 
exists (select 1 from REF_BOOK_ID_DOC g where g.status<>0 and g.id=p.report_doc);

spool off;

exit;	