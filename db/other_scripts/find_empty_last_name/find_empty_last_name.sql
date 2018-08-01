set heading off;
set feedback off;
set verify off;
set termout off;
set linesize 2000;
set trimspool on;
set NEWP NONE;
spool &1

select '"ID (RECORD_ID)";"VERSION";"LAST_NAME";"FIRST_NAME";"MIDDLE_NAME";"BIRTH_DATE";"INN"' from dual;
select '"'||to_char(record_id)||'";"'||to_char(version,'DD.MM.YYYY')||'";"'||to_char(last_name)||'";"'||
to_char(first_name)||'";"'||to_char(middle_name)||'";"'||to_char(birth_date,'DD.MM.YYYY')||'";"'||to_char(inn)||'"'
from ref_book_person where trim(replace(last_name,chr(09),' ')) is null 
order by 1;

spool off;

exit;