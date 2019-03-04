set long 32767 pagesize 0 linesize 4000 feedback off echo off verify off trims on heading off termout off 
SET SERVEROUTPUT ON

column filename new_val filename
column start_date new_val start_date
column end_date new_val end_date
column users_list new_val users_list

-- Cписок пользователей формат ввода для одного пользователя (user_id) для нескольких пользователей (user_id1, user_id2, ..., user_idN)
SELECT '(13763,44389)' users_list from dual;

-- Дата начала периода запроса данных
SELECT '25.02.2019 00:00:00' start_date FROM dual;

-- Дата окончание периода запроса данных
SELECT '02.03.2019 00:00:00' end_date FROM dual;

-- Имя файла скрипта (не изменять)
SELECT 'NOTIFICATION_'||to_char(systimestamp,'yyyymmdd-hhmiss')||'.csv' filename FROM dual;

spool &1\&filename

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
and n.user_id IN &users_list
AND n.create_date BETWEEN TO_DATE('&start_date','DD.MM.YYYY hh24:mi:ss') AND TO_DATE('&end_date','DD.MM.YYYY hh24:mi:ss')
);

exit;	