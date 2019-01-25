set serveroutput on;
set verify off;
set termout off;
spool &1;

BEGIN

  MERGE INTO ref_book_calendar a USING
  (
   select cdate + interval '15' year as cdate, c.ctype from ref_book_calendar c where c.cdate>=to_date('20180101','yyyymmdd') and c.cdate<to_date('20190301','yyyymmdd') order by c.cdate
  ) b
  ON (a.cdate=b.cdate)
  when NOT MATCHED THEN
  INSERT (cdate, ctype)
  VALUES (b.cdate, b.ctype);

  dbms_output.put_line('Added ' || to_char(SQL%ROWCOUNT) || ' rows');
  
  merge into ref_book_calendar a 
  using (select cdate,ctype,id, row_number() over (ORDER by id, cdate) as rn from ref_book_calendar) b on (a.cdate=b.cdate)
  when matched then update set a.id=b.rn;

  commit;

END;
/
exit;
