set verify off;
spool &2
set lines 500
col scrname format a30
col status format a30
col version format a15

select version, scrname, status, start_time, end_time from
(select version, scrname, status, start_time, end_time
  from &1..version_history
order by end_time desc) where rownum = 1;

spool off;
exit;