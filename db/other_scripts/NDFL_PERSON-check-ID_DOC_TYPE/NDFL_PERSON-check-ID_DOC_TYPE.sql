set heading off;
set feedback off;
set verify off;
set termout off;
set linesize 2000;
set trimspool on;
set NEWP NONE;
WHENEVER SQLERROR EXIT;

variable v_count number;
variable v_declaration_id number;

exec :v_count:=0;
exec :v_declaration_id := &2;

select count(*) into :v_count 
from NDFL_PERSON np
where
DECLARATION_DATA_ID = :v_declaration_id and
not exists (select 1 from REF_BOOK_DOC_TYPE where code = np.ID_DOC_TYPE);

spool &1

select '"ROW_NUM";"LAST_NAME";"FIRST_NAME";"MIDDLE_NAME";"INP";"ID_DOC_TYPE";"DECLARATION_DATA_ID"' from dual;

select
'"'||to_char(row_num)||'";"'||last_name||'";"'||first_name||'";"'||middle_name||'";"'||inp||'";"'||id_doc_type||'";"'||to_char(declaration_data_id)||'"'
from ndfl_person np
where
declaration_data_id = :v_declaration_id and
not exists (select 1 from ref_book_doc_type where code = np.id_doc_type)
order by row_num;

spool off;

exit;	