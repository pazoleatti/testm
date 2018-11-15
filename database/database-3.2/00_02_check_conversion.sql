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

select count(*) into :v_count from ref_book_person where start_date is not null;

spool &1/00_ref_book_person-start_date.txt;
select 'Counter ref_book_person.start_date not null = '||to_char(count(*)) from ref_book_person where start_date is not null;
spool off;

exit;
