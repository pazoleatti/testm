set heading off;
set feedback off;
set verify off;
set termout off;
set linesize 2000;
set trimspool on;
set NEWP NONE;
spool &1

select 'REF_BOOK_OKTMO' from dual; 
select '"ID";"CODE";"NAME";"VERSION";"STATUS";"RECORD_ID";"RAZD"' from dual;
select 'Строки, которых нет в эталоне' from dual;

select '"'||to_char(b.id)||'";"'||b.code||'";"'||b.name||'";"'||to_char(b.version,'dd.mm.yyyy')||'";"'||to_char(b.status)||'";"'||to_char(b.record_id)||'";"'||to_char(b.razd)||'"'
from &3..REF_BOOK_OKTMO a
full outer join &2..REF_BOOK_OKTMO b 
on a.code=b.code 
-- and a.name=b.name and a.version=b.version and a.status=b.status
where a.id is null
order by b.code, b.name;

select 'Строки, которые есть в эталоне, но отсутствуют в схеме' from dual; 

select '"'||to_char(a.id)||'";"'||a.code||'";"'||a.name||'";"'||to_char(a.version,'dd.mm.yyyy')||'";"'||to_char(a.status)||'";"'||to_char(a.record_id)||'";"'||to_char(a.section)||'"'
from &3..REF_BOOK_OKTMO a
full outer join &2..REF_BOOK_OKTMO b 
on a.code=b.code
-- and a.name=b.name and a.version=b.version and a.status=b.status
where b.id is null
order by a.code, a.name;


	
spool off;

exit;