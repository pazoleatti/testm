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
from NDFL_REFERENCES nr 
where person_id is null and status=status;

spool &2

select '"id";"record_id";"version";"status";"declaration_data_id";"person_id";"num";"surname";"name";"lastname";"birthday";"errtext";"ndfl_person_id"' from dual;

select 
'"'||to_char(id)||'";"'||to_char(record_id)||'";"'||to_char(version,'DD.MM.YYYY')||'";"'||to_char(status)||'";"'||to_char(declaration_data_id)||'";"'||to_char(person_id)
||'";"'||to_char(num)||'";"'||surname||'";"'||name||'";"'||lastname||'";"'||to_char(birthday,'DD.MM.YYYY')||'";"'||errtext||'";"'||to_char(ndfl_person_id)||'"'
from NDFL_REFERENCES nr 
where person_id is null and status=status;

spool off;

select count(*) into :v_count 
from REF_BOOK_ID_TAX_PAYER
where person_id is null and status=status;

spool &3

select '"id";"record_id";"version";"status";"person_id";"inp";"as_nu"' from dual;

select 
'"'||to_char(id)||'";"'||to_char(record_id)||'";"'||to_char(version,'DD.MM.YYYY')||'";"'||to_char(status)||'";"'||to_char(person_id)||'";"'||inp||'";"'||to_char(as_nu)||'"'
from REF_BOOK_ID_TAX_PAYER
where person_id is null;

spool off;

select count(*) into :v_count 
from REF_BOOK_PERSON_TB 
where person_id is null and status=status;

spool &4

select '"ID";"RECORD_ID";"VERSION";"STATUS";"PERSON_ID";"TB_DEPARTMENT_ID";"IMPORT_DATE"' from dual;

select 
'"'||to_char(ID)||'";"'||to_char(RECORD_ID)||'";"'||to_char(VERSION,'DD.MM.YYYY')||'";"'||to_char(STATUS)||'";"'||to_char(PERSON_ID)||'";"'||to_char(TB_DEPARTMENT_ID)||'";"'||to_char(IMPORT_DATE,'DD.MM.YYYY HH:MI:SS')||'"'
from REF_BOOK_PERSON_TB 
where person_id is null;

spool off;

select count(*) into :v_count 
from REF_BOOK_PERSON 
where start_date is null and status=status;

spool &5

select 
'"ID";"LAST_NAME";"FIRST_NAME";"MIDDLE_NAME";"INN";"INN_FOREIGN";"SNILS";"TAXPAYER_STATE";"BIRTH_DATE";"BIRTH_PLACE";"CITIZENSHIP";"ADDRESS";"RECORD_ID";"VERSION";"STATUS";"SOURCE_ID";"OLD_ID";"REPORT_DOC";"VIP";"start_date";"end_date"'
from dual;

select 
'"'||to_char(ID)||'";"'||LAST_NAME||'";"'||FIRST_NAME||'";"'||MIDDLE_NAME||'";"'||INN||'";"'||INN_FOREIGN||'";"'||SNILS||'";"'||to_char(TAXPAYER_STATE)
||'";"'||to_char(BIRTH_DATE,'DD.MM.YYYY')||'";"'||BIRTH_PLACE||'";"'||to_char(CITIZENSHIP)||'";"'||to_char(ADDRESS)||'";"'||to_char(RECORD_ID)||'";"'||to_char(VERSION,'DD.MM.YYYY')
||'";"'||to_char(STATUS)||'";"'||to_char(SOURCE_ID)||'";"'||to_char(OLD_ID)||'";"'||to_char(REPORT_DOC)||'";"'||to_char(VIP)||'";"'||to_char(start_date,'DD.MM.YYYY')||'";"'||to_char(end_date,'DD.MM.YYYY')||'"'
from REF_BOOK_PERSON 
where start_date is null;

spool off;

select count(*) into :v_count 
from REF_BOOK_ID_DOC 
where person_id is null;

spool &6

select '"ID";"PERSON_ID";"DOC_ID";"DOC_NUMBER";"INC_REP"' from dual;

select
'"'||to_char(ID)||'";"'||to_char(PERSON_ID)||'";"'||to_char(DOC_ID)||'";"'||DOC_NUMBER||'";"'||to_char(INC_REP)||'"'
from REF_BOOK_ID_DOC 
where person_id is null;

spool off;

exit;	