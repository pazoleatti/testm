set heading off;
set feedback off;
set verify off;
set termout off;
set linesize 2000;
set trimspool on;
set NEWP NONE;
WHENEVER SQLERROR EXIT;

spool &1

select '"DECLARATION_DATA_ID";"FILE_NAME";"BAD_R3";"BAD_R4"' from dual;

select
'"'||to_char(declaration_data_id)||'";"'||name||'";"'||to_char(nvl(cnt_r3, 0))||'";"'||to_char(nvl(cnt_r4, 0))||'"'
from
(
select
ddf.declaration_data_id,
bd.name
,r3.cnt_r3
,r4.cnt_r4
from
DECLARATION_DATA_FILE ddf,
REF_BOOK_ATTACH_FILE_TYPE aft,
BLOB_DATA bd
,(
select 
np.declaration_data_id, count(npd.id) cnt_r3
from 
NDFL_PERSON np,
NDFL_PERSON_DEDUCTION npd
where 
npd.ndfl_person_id = np.id
and not exists (select 1 from NDFL_PERSON_INCOME npi where npi.operation_id = npd.operation_id and npi.ndfl_person_id = npd.ndfl_person_id)
group by np.declaration_data_id
) r3
,(
select 
np.declaration_data_id, count(npp.id) cnt_r4
from 
NDFL_PERSON np,
NDFL_PERSON_PREPAYMENT npp
where 
npp.ndfl_person_id = np.id
and not exists (select 1 from NDFL_PERSON_INCOME npi where npi.operation_id = npp.operation_id and npi.ndfl_person_id = npp.ndfl_person_id)
group by np.declaration_data_id
) r4
where
bd.id = ddf.blob_data_id and
aft.id = ddf.file_type_id and
aft.code = 1 
and r3.declaration_data_id(+) = ddf.declaration_data_id
and r4.declaration_data_id(+) = ddf.declaration_data_id
)
where
cnt_r3 is not null or cnt_r4 is not null;

spool off;

exit;	