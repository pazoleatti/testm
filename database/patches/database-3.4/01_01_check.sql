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
from REF_BOOK_NDFL_DETAIL t 
where phone is not null and length(phone)>20;

spool &1

select '"ID";"RECORD_ID";"DEPARTMENT_ID";"TAX_ORGAN_CODE";"KPP";"TAX_ORGAN_CODE_MID";"PRESENT_PLACE";"NAME";"OKTMO";"PHONE";"REORG_FORM_CODE";"REORG_INN";"REORG_KPP";"SIGNATORY_ID";"SIGNATORY_SURNAME";"SIGNATORY_FIRSTNAME";"SIGNATORY_LASTNAME";"APPROVE_DOC_NAME";"APPROVE_ORG_NAME"' from dual;

select 
'"'||to_char(ID)||'";"'||to_char(RECORD_ID)||'";"'||to_char(DEPARTMENT_ID)||'";"'||TAX_ORGAN_CODE||'";"'||KPP||'";"'||TAX_ORGAN_CODE_MID||'";"'||to_char(PRESENT_PLACE)||'";"'||NAME
||'";"'||to_char(OKTMO)||'";"'||PHONE||'";"'||to_char(REORG_FORM_CODE)||'";"'||REORG_INN||'";"'||REORG_KPP||'";"'||to_char(SIGNATORY_ID)||'";"'||SIGNATORY_SURNAME||'";"'||SIGNATORY_FIRSTNAME||'";"'||SIGNATORY_LASTNAME||'";"'||APPROVE_DOC_NAME||'";"'||APPROVE_ORG_NAME||'"'
from REF_BOOK_NDFL_DETAIL t 
where phone is not null and length(phone)>20;

spool off;

select count(*) into :v_count 
from ref_book_id_doc where (person_id is null) or (doc_id is null) or (doc_number is null);

spool &2

select '"id";"person_id";"doc_id";"doc_number"' from dual;

select '"'||to_char(id)||'";"'||to_char(person_id)||'";"'||to_char(doc_id)||'";"'||to_char(doc_number)||'"' from ref_book_id_doc where (person_id is null) or (doc_id is null) or (doc_number is null);


spool off;

select count(*) into :v_count 
from ref_book_person where old_id is null;

spool &3

select '"id";"record_id";"old_id"' from dual;

select '"'||to_char(id)||'";"'||to_char(record_id)||'";"'||to_char(old_id)||'"' from ref_book_person where old_id is null;

spool off;

exit;	