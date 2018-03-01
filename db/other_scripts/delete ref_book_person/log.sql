set heading off;
set feedback off;
set verify off;
set termout off;
set linesize 200;
set trimspool on;
set NEWP NONE;
spool &1

select '"id";"last_name";"first_name";"middle_name";"ndfl_person";"ndfl_references"' from dual;

select '"'||to_char(id)||'";"'||last_name||'";"'||first_name||'";"'||middle_name||'";"yes";"no"' 
  from ref_book_person r where 
not exists(select 1 from NDFL_REFERENCES where person_id=r.id)
and
exists(select 1 from NDFL_PERSON where person_id=r.id);
  
select '"'||to_char(id)||'";"'||last_name||'";"'||first_name||'";"'||middle_name||'";"no";"yes"' 
  from ref_book_person r where 
exists(select 1 from NDFL_REFERENCES where person_id=r.id)
and
not exists(select 1 from NDFL_PERSON where person_id=r.id);

select '"'||to_char(id)||'";"'||last_name||'";"'||first_name||'";"'||middle_name||'";"yes";"yes"' 
  from ref_book_person r where 
exists(select 1 from NDFL_REFERENCES where person_id=r.id)
and
exists(select 1 from NDFL_PERSON where person_id=r.id);
	
spool off;

exit;	