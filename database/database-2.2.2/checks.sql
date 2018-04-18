set heading off;
set feedback off;
set verify off;
set termout off;
set linesize 200;
set trimspool on;
set NEWP NONE;
spool &6

select '"id";"record_id";"name";"tax_organ_code";"kpp";"present_place";"oktmo";"signatory_id";"signatory_surname";"signatory_firstname";"approve_doc_name"' from dual;

select '"'||to_char(id)||'";"'||to_char(record_id)||'";"'||name||'";"'||to_char(tax_organ_code)||'";"'||to_char(kpp)||'";"'||to_char(present_place)||'";"'||to_char(oktmo)||
'";"'||to_char(signatory_id)||'";"'||to_char(signatory_surname)||'";"'||to_char(signatory_firstname)||'";"'||to_char(approve_doc_name)||'"'
  from ref_book_ndfl_detail r where 
name is null or tax_organ_code is null or kpp is null or present_place is null or oktmo is null or signatory_id is null or signatory_surname is null or signatory_firstname is null or approve_doc_name is null;

	
spool off;

spool &7

select '"id";"record_id";"name";"tax_organ_code";"kpp";"present_place";"oktmo";"signatory_id";"signatory_surname";"signatory_firstname";"approve_doc_name"' from dual;

select '"'||to_char(id)||'";"'||to_char(record_id)||'";"'||r.name||'";"'||to_char(r.tax_organ_code)||'";"'||to_char(r.kpp)||'";"'||to_char(present_place)||'";"'||to_char(a.oktmo_name)||
'";"'||to_char(signatory_id)||'";"'||to_char(signatory_surname)||'";"'||to_char(signatory_firstname)||'";"'||to_char(approve_doc_name)||'"'
  from ref_book_ndfl_detail r join 
  (select d.name, d.kpp, d.tax_organ_code, d.oktmo, o.code as oktmo_name, count(1) cc from ref_book_ndfl_detail d join ref_book_oktmo o on d.oktmo=o.id
  group by d.name, d.kpp, d.tax_organ_code, d.oktmo, o.code
  having count(1)>1) a on r.name=a.name and r.kpp=a.kpp and r.tax_organ_code=a.tax_organ_code and r.oktmo=a.oktmo;

	
spool off;

exit;	