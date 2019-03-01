set heading off;
set feedback off;
set verify off;
set termout off;
set linesize 2000;
set trimspool on;
set NEWP NONE;
WHENEVER SQLERROR EXIT;

variable v_user_id number;
variable v_begin_date varchar2(10);
variable v_end_date varchar2(10);
variable v_file_name varchar2(100);

exec :v_user_id := &1;
exec :v_begin_date:='&2';
exec :v_end_date:='&3';
exec :v_file_name:='_logs\NOTIFICATIONS_20190301-1140.csv';

--spool '_logs\Оповещения_'||to_char(systimestamp,'yyyymmdd-hhmiss')||'.csv'
--spool '_logs\Оповещения_.csv'
--spool &:v_file_name
--spool '_logs\NOTIFICATIONS_20190301-1140.csv'


--spool '_logs\1.csv'

spool &4

select '"Дата-время";"Содержание"' from dual;

select '"'||to_char(Date_Time,'dd.mm.yyyy hh:mi:ss')||'";"'||replace(Text_Mess,'"','""')||'"'
from (
select
le.creation_date as Date_Time, 
n.text as Text_Mess
,n.user_id
from
notification n,
log_entry le
where
le.log_id=n.log_id
and n.user_id=:v_user_id
and n.create_date between to_date(:v_begin_date,'dd.mm.yyyy') and to_date(:v_end_date,'dd.mm.yyyy')
);

spool off;

exit;	