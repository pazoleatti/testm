set heading off;
set feedback off;
set verify off;
set termout off;
set linesize 2000;
set trimspool on;
set NEWP NONE;
spool &1

select cast('Не удалено ФЛ:  '||rbp.last_name||' '||rbp.first_name||' '||rbp.middle_name||' (Идентификатор ФЛ:'||to_char(rbp.id)||'). На ФЛ ссылаются формы:  '||rtrim(xmlagg(xmlelement(f,declaration_data_id,', ').extract('//text()') order by declaration_data_id).GetClobVal(),', ')||'.' as varchar2(2000)) as txt
from REF_BOOK_PERSON rbp
join 
(select person_id,declaration_data_id from NDFL_REFERENCES nr
 union
 select person_id,declaration_data_id from NDFL_PERSON np
 union
 select person_id, declaration_data_id from DECLARATION_DATA_PERSON dd) f
on rbp.id=f.person_id
group by rbp.id,rbp.last_name,rbp.first_name,rbp.middle_name;	
spool off;

exit;	