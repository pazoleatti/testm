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
from REF_BOOK_ID_DOC a 
where 
exists (select 1 from REF_BOOK_PERSON p where p.status<>0 and p.id=a.person_id)
and record_id is not null
and version is not null
and status = 0
and duplicate_record_id is not null;

spool &1

select '"id";"record_id";"version";"status";"person_id";"doc_id";"doc_number";"inc_rep";"duplicate_record_id"' from dual;

select '"'||to_char(id)||'";"'||to_char(record_id)||'";"'||to_char(version,'DD.MM.YYYY')||'";"'||to_char(status)||'";"'||to_char(person_id)||'";"'||to_char(doc_id)||'";"'||doc_number||'";"'||to_char(inc_rep)||'";"'||to_char(duplicate_record_id)||'"' 
from REF_BOOK_ID_DOC a 
where 
exists (select 1 from REF_BOOK_PERSON p where p.status<>0 and p.id=a.person_id);

spool off;

select count(*) into :v_count 
from DECLARATION_DATA_PERSON b 
where 
exists (select 1 from REF_BOOK_PERSON p where p.status<>0 and p.id=b.person_id);

spool &2

select '"declaration_data_id";"person_id"' from dual;

select '"'||to_char(declaration_data_id)||'";"'||to_char(person_id)||'"' 
from DECLARATION_DATA_PERSON b 
where 
exists (select 1 from REF_BOOK_PERSON p where p.status<>0 and p.id=b.person_id);
	
spool off;

select count(*) into :v_count 
from NDFL_PERSON c
where 
exists (select 1 from REF_BOOK_PERSON p where p.status<>0 and p.id=c.person_id);

spool &3

select '"ID";"DECLARATION_DATA_ID";"PERSON_ID";"ROW_NUM";"INP";"SNILS";"LAST_NAME";"FIRST_NAME";"MIDDLE_NAME";"BIRTH_DAY";"CITIZENSHIP";"INN_NP";"INN_FOREIGN";"ID_DOC_TYPE";"ID_DOC_NUMBER";"STATUS";"POST_INDEX";"REGION_CODE";"AREA";"CITY";"LOCALITY";"STREET";"HOUSE";"BUILDING";"FLAT";"COUNTRY_CODE";"ADDRESS";"ADDITIONAL_DATA";"MODIFIED_DATE";"MODIFIED_BY";"ASNU_ID"' from dual;

select 
'"'||to_char(ID)||'";"'||to_char(DECLARATION_DATA_ID)||'";"'||to_char(PERSON_ID)||'";"'||to_char(ROW_NUM)||'";"'||INP||'";"'||SNILS||'";"'||LAST_NAME||'";"'||FIRST_NAME
||'";"'||MIDDLE_NAME||'";"'||to_char(BIRTH_DAY,'DD.MM.YYYY')||'";"'||CITIZENSHIP||'";"'||INN_NP||'";"'||INN_FOREIGN||'";"'||ID_DOC_TYPE||'";"'||ID_DOC_NUMBER||'";"'||STATUS
||'";"'||POST_INDEX||'";"'||REGION_CODE||'";"'||AREA||'";"'||CITY||'";"'||LOCALITY||'";"'||STREET||'";"'||HOUSE||'";"'||BUILDING||'";"'||FLAT||'";"'||COUNTRY_CODE||'";"'||ADDRESS
||'";"'||ADDITIONAL_DATA||'";"'||to_char(MODIFIED_DATE,'DD.MM.YYYY')||'";"'||MODIFIED_BY||'";"'||to_char(ASNU_ID)||'"'
from NDFL_PERSON c
where 
exists (select 1 from REF_BOOK_PERSON p where p.status<>0 and p.id=c.person_id);

spool off;

select count(*) into :v_count 
from NDFL_REFERENCES d 
where 
exists (select 1 from REF_BOOK_PERSON p where p.status<>0 and p.id=d.person_id);

spool &4

select '"id";"record_id";"version";"status";"declaration_data_id";"person_id";"num";"surname";"name";"lastname";"birthday";"errtext";"ndfl_person_id"' from dual;

select 
'"'||to_char(id)||'";"'||to_char(record_id)||'";"'||to_char(version,'DD.MM.YYYY')||'";"'||to_char(status)||'";"'||to_char(declaration_data_id)||'";"'||to_char(person_id)
||'";"'||to_char(num)||'";"'||surname||'";"'||name||'";"'||lastname||'";"'||to_char(birthday,'DD.MM.YYYY')||'";"'||errtext||'";"'||to_char(ndfl_person_id)||'"'
from NDFL_REFERENCES d 
where 
exists (select 1 from REF_BOOK_PERSON p where p.status<>0 and p.id=d.person_id);

spool off;

select count(*) into :v_count 
from REF_BOOK_ID_TAX_PAYER e 
where 
exists (select 1 from REF_BOOK_PERSON p where p.status<>0 and p.id=e.person_id)
and record_id is not null
and version is not null
and status = 0;


spool &5

select '"id";"record_id";"version";"status";"person_id";"inp";"as_nu"' from dual;

select 
'"'||to_char(id)||'";"'||to_char(record_id)||'";"'||to_char(version,'DD.MM.YYYY')||'";"'||to_char(status)||'";"'||to_char(person_id)||'";"'||inp||'";"'||to_char(as_nu)||'"'
from REF_BOOK_ID_TAX_PAYER e 
where 
exists (select 1 from REF_BOOK_PERSON p where p.status<>0 and p.id=e.person_id);

spool off;

select count(*) into :v_count 
from REF_BOOK_PERSON_TB f 
where 
exists (select 1 from REF_BOOK_PERSON p where p.status<>0 and p.id=f.person_id)
and record_id is not null
and version is not null
and status = 0;

spool &6

select '"id";"record_id";"version";"status";"person_id";"tb_department_id";"import_date"' from dual;

select 
'"'||to_char(id)||'";"'||to_char(record_id)||'";"'||to_char(version,'DD.MM.YYYY')||'";"'||to_char(status)||'";"'||to_char(person_id)||'";"'||to_char(tb_department_id)||'";"'||to_char(import_date,'DD.MM.YYYY HH:MI:SS')||'"'
from REF_BOOK_PERSON_TB f 
where 
exists (select 1 from REF_BOOK_PERSON p where p.status<>0 and p.id=f.person_id);

spool off;

exit;	