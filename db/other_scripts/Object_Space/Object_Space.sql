set heading off;
set feedback off;
set verify off;
set termout off;
set linesize 2000;
set trimspool on;
set NEWP NONE;

variable v_owner varchar2(100);
variable v_other_owner varchar2(100);

exec :v_owner:='&2';
exec :v_other_owner:='&3';

spool &1;

select '"TABLESPACE NAME";"SCHEMA";"OBJECT TYPE";"OBJECT NAME";"OBJECT SIZE (Mb)";"OBJECT TYPE SIZE (Mb)";"OBJECT SCHEMA SIZE (Mb)";"TABLESPACE DATA SIZE (Mb)"' from dual;

select 
'"'||t2.tablespace_name||'";"'||t2.owner||'";"'||t2.segment_type||'";"'||t2.segment_name||'";"'||to_char(round(t2.Mbytes,2),'9999990.90')||'";"'||to_char(round(t2.SegmentTypeMbytes,2),'9999990.90')
||'";"'||to_char(round(t2.OwnerMbytes,2),'9999990.90')||'";"'||to_char(round(t2.FullMbytes,2),'9999990.90')||'"'
from (
select t.tablespace_name,
       t.owner,
       t.segment_type,
       t.segment_name,
       round((sum(t.bytes) / (1024 * 1024)), 2) Mbytes,
       sum(sum(t.bytes) / (1024 * 1024)) over (partition by t.tablespace_name, t.owner, t.segment_type) SegmentTypeMbytes,
       sum(sum(t.bytes) / (1024 * 1024)) over (partition by t.tablespace_name, t.owner) OwnerMbytes,
       sum(sum(t.bytes) / (1024 * 1024)) over (partition by t.tablespace_name) FullMbytes
  from dba_segments t
 where lower(t.tablespace_name) = (select distinct lower(ds.tablespace_name) from dba_segments ds where ds.owner=:v_owner)
 group by t.tablespace_name, t.owner, t.segment_type, t.segment_name
) t2
where
t2.segment_type in ('TABLE', 'INDEX') and
((lower(:v_other_owner)='y') or (lower(:v_other_owner)<>'y') and lower(t2.owner) = lower(:v_owner))
order by owner, segment_type desc, Mbytes desc;
	 
spool off;

exit;