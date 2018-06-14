set heading off;
set feedback off;
set verify off;
set termout off;
set linesize 200;
set trimspool on;
set NEWP NONE;

column filename new_val filename
column outpath new_val outpath

select 'C:\temp\logs' outpath from dual;
select 'checks_' || user || '_' || to_char(sysdate, 'yyyymmdd')||'.csv' filename from dual;

spool &outpath\&filename;

select '"table_name";"column_name";"tax_table_name";"tax_id";"id";"name";"declaration_data_id"' from dual;

select '"DEPARTMENT_DECLARATION_TYPE";"DEPARTMENT_ID";"DEPARTMENT";"'||to_char(DEPARTMENT_ID)||'";"'||to_char(ID)||'";"";""'
  from DEPARTMENT_DECLARATION_TYPE t where DEPARTMENT_ID not in (select id from department) and DEPARTMENT_ID is not null
union all  
select '"DEPARTMENT_REPORT_PERIOD";"DEPARTMENT_ID";"DEPARTMENT";"'||to_char(t.DEPARTMENT_ID)||'";"'||to_char(t.ID)||'";"'||dpt.name||' '||tp.year||'";"'||nvl(a.declaration_data_id,' ')||'"'
  from DEPARTMENT_REPORT_PERIOD t join report_period p on t.report_period_id=p.id
  join tax_period tp on p.tax_period_id=tp.id
  join REPORT_PERIOD_TYPE dpt on p.dict_tax_period_id=dpt.id
  left join (select department_report_period_id, LISTAGG(to_char(id), ', ') WITHIN GROUP (ORDER BY id) declaration_data_id from declaration_data group by department_report_period_id) a on t.id=a.department_report_period_id
  where t.DEPARTMENT_ID not in (select id from department) and t.DEPARTMENT_ID is not null   
union all  
select '"NOTIFICATION";"RECEIVER_DEPARTMENT_ID";"DEPARTMENT";"'||to_char(RECEIVER_DEPARTMENT_ID)||'";"'||to_char(ID)||'";"";""'
  from NOTIFICATION t where RECEIVER_DEPARTMENT_ID not in (select id from department) and RECEIVER_DEPARTMENT_ID is not null
union all  
select '"NOTIFICATION";"SENDER_DEPARTMENT_ID";"DEPARTMENT";"'||to_char(SENDER_DEPARTMENT_ID)||'";"'||to_char(ID)||'";"";""'
  from NOTIFICATION t where SENDER_DEPARTMENT_ID not in (select id from department) and SENDER_DEPARTMENT_ID is not null
union all  
select '"DEPARTMENT_DECL_TYPE_PERFORMER";"PERFORMER_DEP_ID";"DEPARTMENT";"'||to_char(PERFORMER_DEP_ID)||'";"'||to_char(DEPARTMENT_DECL_TYPE_ID)||'";"";""'
  from DEPARTMENT_DECL_TYPE_PERFORMER t where PERFORMER_DEP_ID not in (select id from department) and PERFORMER_DEP_ID is not null
union all  
select '"REF_BOOK_NDFL";"DEPARTMENT_ID";"DEPARTMENT";"'||to_char(DEPARTMENT_ID)||'";"'||to_char(ID)||'";"";""'
  from REF_BOOK_NDFL t where DEPARTMENT_ID not in (select id from department) and DEPARTMENT_ID is not null
union all  
select '"REF_BOOK_NDFL_DETAIL";"DEPARTMENT_ID";"DEPARTMENT";"'||to_char(DEPARTMENT_ID)||'";"'||to_char(ID)||'";"";""'
  from REF_BOOK_NDFL_DETAIL t where DEPARTMENT_ID not in (select id from department) and DEPARTMENT_ID is not null
union all  
select '"CONFIGURATION";"DEPARTMENT_ID";"DEPARTMENT";"'||to_char(DEPARTMENT_ID)||'";"'||to_char(CODE)||'";"";""'
  from CONFIGURATION t where DEPARTMENT_ID not in (select id from department) and DEPARTMENT_ID is not null
union all  
select '"NOTIFICATION";"ROLE_ID";"SEC_ROLE";"'||to_char(ROLE_ID)||'";"'||to_char(ID)||'";"";""'
  from NOTIFICATION t where ROLE_ID not in (select id from sec_role) and ROLE_ID is not null
union all  
select '"REF_BOOK_ASNU";"ROLE_ALIAS";"SEC_ROLE";"'||to_char(ROLE_ALIAS)||'";"'||to_char(ID)||'";"";""'
  from REF_BOOK_ASNU t where ROLE_ALIAS not in (select id from sec_role) and ROLE_ALIAS is not null
union all   
select '"REF_BOOK_ASNU";"ROLE_NAME";"SEC_ROLE";"'||to_char(ROLE_NAME)||'";"'||to_char(ID)||'";"";""'
  from REF_BOOK_ASNU t where ROLE_NAME not in (select id from sec_role) and ROLE_NAME is not null
union all  
select '"NOTIFICATION";"USER_ID";"SEC_USER";"'||to_char(USER_ID)||'";"'||to_char(ID)||'";"";""'
  from NOTIFICATION t where USER_ID not in (select id from sec_user) and USER_ID is not null
union all  
select '"TEMPLATE_CHANGES";"AUTHOR";"SEC_USER";"'||to_char(AUTHOR)||'";"'||to_char(ID)||'";"";""'
  from TEMPLATE_CHANGES t where AUTHOR not in (select id from sec_user) and AUTHOR is not null
union all  
select '"LOCK_DATA";"USER_ID";"SEC_USER";"'||to_char(USER_ID)||'";"'||to_char(ID)||'";"";""'
  from LOCK_DATA t where USER_ID not in (select id from sec_user) and USER_ID is not null
union all  
select '"ASYNC_TASK_SUBSCRIBERS";"USER_ID";"SEC_USER";"'||to_char(USER_ID)||'";"'||to_char(ASYNC_TASK_ID)||'";"";""'
  from ASYNC_TASK_SUBSCRIBERS t where USER_ID not in (select id from sec_user) and USER_ID is not null
union all  
select '"LOG";"USER_ID";"SEC_USER";"'||to_char(USER_ID)||'";"'||to_char(ID)||'";"";""'
  from LOG t where USER_ID not in (select id from sec_user) and USER_ID is not null
  ;


	
spool off;

exit;	