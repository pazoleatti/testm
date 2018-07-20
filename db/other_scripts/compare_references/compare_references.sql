set heading off;
set feedback off;
set verify off;
set termout off;
set linesize 2000;
set trimspool on;
set NEWP NONE;
spool &1

select 'REF_BOOK_ASNU' from dual; 
select '"id";"code";"name";"type";"role_alias";"role_name";"priority"' from dual;
select 'Строки, которых нет в эталоне' from dual;

select '"'||to_char(b.id)||'";"'||to_char(b.code)||'";"'||to_char(b.name)||'";"'||to_char(b.type)||'";"'||to_char(b.role_alias)||'";"'||to_char(b.role_name)||'";"'||to_char(b.priority)||'"'
from &3..REF_BOOK_ASNU a
full outer join &2..REF_BOOK_ASNU b 
on a.code=b.code and a.name=b.name and a.type=b.type and 
(a.role_alias=b.role_alias or (a.role_alias is null and b.role_alias is null)) and 
(a.role_name=b.role_name or (a.role_name is null and b.role_name is null)) and 
(a.priority=b.priority or (a.priority is null and b.priority is null))
where a.id is null
order by b.code, b.name, b.priority;

select 'Строки, которые есть в эталоне, но отсутствуют в схеме' from dual; 

select '"'||to_char(a.id)||'";"'||to_char(a.code)||'";"'||to_char(a.name)||'";"'||to_char(a.type)||'";"'||to_char(a.role_alias)||'";"'||to_char(a.role_name)||'";"'||to_char(a.priority)||'"'
from &3..REF_BOOK_ASNU a
full outer join &2..REF_BOOK_ASNU b 
on a.code=b.code and a.name=b.name and a.type=b.type and 
(a.role_alias=b.role_alias or (a.role_alias is null and b.role_alias is null)) and 
(a.role_name=b.role_name or (a.role_name is null and b.role_name is null)) and 
(a.priority=b.priority or (a.priority is null and b.priority is null))
where b.id is null
order by a.code, a.name, a.priority;

select '**********************************************************************************************************************************' from dual;

select 'REF_BOOK_INCOME_KIND' from dual; 
select '"id";"income_type_version";"income_type_status";"income_type_code";"income_type_name";"mark";"name";"version";"status";"record_id"' from dual;
select 'Строки, которых нет в эталоне' from dual;

select '"'||to_char(t2.id)||'";"'||to_char(t2.income_type_version,'DD.MM.YYYY')||'";"'||to_char(t2.income_type_status)||'";"'||to_char(t2.income_type_code)||'";"'||
to_char(t2.income_type_name)||'";"'||to_char(t2.mark)||'";"'||to_char(t2.name)||'";"'||to_char(t2.version,'DD.MM.YYYY')||'";"'||to_char(t2.status)||'";"'||to_char(t2.record_id)||'"'
from 
(select a1.id, b1.version income_type_version, b1.status income_type_status, b1.code income_type_code, 
b1.name income_type_name,a1.mark, a1.name, a1.version, a1.status, a1.record_id from &3..REF_BOOK_INCOME_KIND a1 
join &3..ref_book_income_type b1 on a1.income_type_id=b1.id) t1
full outer join 
(select a2.id, b2.version income_type_version, b2.status income_type_status, b2.code income_type_code, 
b2.name income_type_name,a2.mark, a2.name, a2.version, a2.status, a2.record_id from &2..REF_BOOK_INCOME_KIND a2
join &2..ref_book_income_type b2 on a2.income_type_id=b2.id) t2
on t1.income_type_version=t2.income_type_version and t1.income_type_status=t2.income_type_status and t1.income_type_code=t2.income_type_code and
t1.income_type_name=t2.income_type_name and t1.mark=t2.mark and 
(t1.name=t2.name or (t1.name is null and t2.name is null)) and
t1.version=t2.version and t1.status=t2.status
where t1.id is null
order by t2.mark, t2.version, t2.status, t2.name;

select 'Строки, которые есть в эталоне, но отсутствуют в схеме' from dual; 

select '"'||to_char(t1.id)||'";"'||to_char(t1.income_type_version,'DD.MM.YYYY')||'";"'||to_char(t1.income_type_status)||'";"'||to_char(t1.income_type_code)||'";"'||
to_char(t1.income_type_name)||'";"'||to_char(t1.mark)||'";"'||to_char(t1.name)||'";"'||to_char(t1.version,'DD.MM.YYYY')||'";"'||to_char(t1.status)||'";"'||to_char(t1.record_id)||'"'
from 
(select a1.id, b1.version income_type_version, b1.status income_type_status, b1.code income_type_code, 
b1.name income_type_name,a1.mark, a1.name, a1.version, a1.status, a1.record_id from &3..REF_BOOK_INCOME_KIND a1 
join &3..ref_book_income_type b1 on a1.income_type_id=b1.id) t1
full outer join 
(select a2.id, b2.version income_type_version, b2.status income_type_status, b2.code income_type_code, 
b2.name income_type_name,a2.mark, a2.name, a2.version, a2.status, a2.record_id from &2..REF_BOOK_INCOME_KIND a2
join &2..ref_book_income_type b2 on a2.income_type_id=b2.id) t2
on t1.income_type_version=t2.income_type_version and t1.income_type_status=t2.income_type_status and t1.income_type_code=t2.income_type_code and
t1.income_type_name=t2.income_type_name and t1.mark=t2.mark and 
(t1.name=t2.name or (t1.name is null and t2.name is null)) and
t1.version=t2.version and t1.status=t2.status
where t2.id is null
order by t1.mark, t1.version, t1.status, t1.name;

select '**********************************************************************************************************************************' from dual;

select 'REF_BOOK_CALENDAR' from dual; 
select '"cdate";"ctype";"id"' from dual;
select 'Строки, которых нет в эталоне' from dual;

select '"'||to_char(b.cdate,'DD.MM.YYYY')||'";"'||to_char(b.ctype)||'";"'||to_char(b.id)||'"'
from &3..REF_BOOK_CALENDAR a
full outer join &2..REF_BOOK_CALENDAR b 
on a.cdate=b.cdate and a.ctype=b.ctype
where a.id is null
order by b.cdate;

select 'Строки, которые есть в эталоне, но отсутствуют в схеме' from dual; 

select '"'||to_char(a.cdate,'DD.MM.YYYY')||'";"'||to_char(a.ctype)||'";"'||to_char(a.id)||'"'
from &3..REF_BOOK_CALENDAR a
full outer join &2..REF_BOOK_CALENDAR b 
on a.cdate=b.cdate and a.ctype=b.ctype
where b.id is null
order by a.cdate;

select '**********************************************************************************************************************************' from dual;

select 'REF_BOOK_DEDUCTION_TYPE' from dual; 
select '"id";"record_id";"version";"status";"code";"name";"mark_version";"mark_status";"mark_code";"mark_name"' from dual;
select 'Строки, которых нет в эталоне' from dual;

select '"'||to_char(t2.id)||'";"'||to_char(t2.record_id)||'";"'||to_char(t2.version,'DD.MM.YYYY')||'";"'||to_char(t2.status)||'";"'||to_char(t2.code)||'";"'||to_char(t2.name)||'";"'
||to_char(t2.mark_version,'DD.MM.YYYY')||'";"'||to_char(t2.mark_status)||'";"'||to_char(t2.mark_code)||'";"'||to_char(t2.mark_name)||'"'
from 
(select a1.id, a1.record_id, a1.version, a1.status, a1.code, a1.name, b1.version as mark_version, b1.status as mark_status,
b1.code as mark_code, b1.name as mark_name from &3..REF_BOOK_DEDUCTION_TYPE a1
join &3..ref_book_deduction_mark b1 on a1.deduction_mark=b1.id) t1 
full outer join
(select a2.id, a2.record_id, a2.version, a2.status, a2.code, a2.name, b2.version as mark_version, b2.status as mark_status,
b2.code as mark_code, b2.name as mark_name from &2..REF_BOOK_DEDUCTION_TYPE a2
join &2..ref_book_deduction_mark b2 on a2.deduction_mark=b2.id) t2 
on t1.version=t2.version and t1.status=t2.status and t1.code=t2.code and t1.name=t2.name and t1.mark_version=t2.mark_version and t1.mark_status=t2.mark_status and 
t1.mark_code=t2.mark_code and t1.mark_name=t2.mark_name
where t1.id is null
order by t2.code, t2.version, t2.status, t2.name;

select 'Строки, которые есть в эталоне, но отсутствуют в схеме' from dual; 

select '"'||to_char(t1.id)||'";"'||to_char(t1.record_id)||'";"'||to_char(t1.version,'DD.MM.YYYY')||'";"'||to_char(t1.status)||'";"'||to_char(t1.code)||'";"'||to_char(t1.name)||'";"'
||to_char(t1.mark_version,'DD.MM.YYYY')||'";"'||to_char(t1.mark_status)||'";"'||to_char(t1.mark_code)||'";"'||to_char(t1.mark_name)||'"'
from
(select a1.id, a1.record_id, a1.version, a1.status, a1.code, a1.name, b1.version as mark_version, b1.status as mark_status,
b1.code as mark_code, b1.name as mark_name from &3..REF_BOOK_DEDUCTION_TYPE a1
join &3..ref_book_deduction_mark b1 on a1.deduction_mark=b1.id) t1 
full outer join
(select a2.id, a2.record_id, a2.version, a2.status, a2.code, a2.name, b2.version as mark_version, b2.status as mark_status,
b2.code as mark_code, b2.name as mark_name from &2..REF_BOOK_DEDUCTION_TYPE a2
join &2..ref_book_deduction_mark b2 on a2.deduction_mark=b2.id) t2 
on t1.version=t2.version and t1.status=t2.status and t1.code=t2.code and t1.name=t2.name and t1.mark_version=t2.mark_version and t1.mark_status=t2.mark_status and 
t1.mark_code=t2.mark_code and t1.mark_name=t2.mark_name
where t2.id is null
order by t1.code, t1.version, t1.status, t1.name;

select '**********************************************************************************************************************************' from dual;

select 'REF_BOOK_INCOME_TYPE' from dual; 
select '"id";"record_id";"version";"status";"code";"name"' from dual;
select 'Строки, которых нет в эталоне' from dual; 

select '"'||to_char(b.id)||'";"'||to_char(b.record_id)||'";"'||to_char(b.version,'DD.MM.YYYY')||'";"'||to_char(b.status)||'";"'||to_char(b.code)||'";"'||to_char(b.name)||'"'
from &3..REF_BOOK_INCOME_TYPE a 
full outer join &2..REF_BOOK_INCOME_TYPE b 
on a.code=b.code and a.name=b.name and a.status=b.status and a.version=b.version
where a.id is null
order by b.code, b.version, b.status, b.name;

select 'Строки, которые есть в эталоне, но отсутствуют в схеме' from dual; 

select '"'||to_char(a.id)||'";"'||to_char(a.record_id)||'";"'||to_char(a.version,'DD.MM.YYYY')||'";"'||to_char(a.status)||'";"'||to_char(a.code)||'";"'||to_char(a.name)||'"'
from &3..REF_BOOK_INCOME_TYPE a 
full outer join &2..REF_BOOK_INCOME_TYPE b 
on a.code=b.code and a.name=b.name and a.status=b.status and a.version=b.version
where b.id is null
order by a.code, a.version, a.status, a.name;

select '**********************************************************************************************************************************' from dual;

select 'REF_BOOK_DOC_TYPE' from dual; 
select '"id";"record_id";"status";"version";"code";"name";"priority"' from dual;
select 'Строки, которых нет в эталоне' from dual;

select '"'||to_char(b.id)||'";"'||to_char(b.record_id)||'";"'||to_char(b.status)||'";"'||to_char(b.version,'DD.MM.YYYY')||'";"'||to_char(b.code)||'";"'||to_char(b.name)||'";"'||to_char(b.priority)||'"'
from &3..REF_BOOK_DOC_TYPE a
full outer join &2..REF_BOOK_DOC_TYPE b 
on a.status=b.status and a.version=b.version and a.code=b.code and a.name=b.name and 
(a.priority=b.priority or (a.priority is null and b.priority is null))
where a.id is null
order by b.code, b.version, b.status, b.name;

select 'Строки, которые есть в эталоне, но отсутствуют в схеме' from dual; 

select '"'||to_char(a.id)||'";"'||to_char(a.record_id)||'";"'||to_char(a.status)||'";"'||to_char(a.version,'DD.MM.YYYY')||'";"'||to_char(a.code)||'";"'||to_char(a.name)||'";"'||to_char(a.priority)||'"'
from &3..REF_BOOK_DOC_TYPE a
full outer join &2..REF_BOOK_DOC_TYPE b 
on a.status=b.status and a.version=b.version and a.code=b.code and a.name=b.name and 
(a.priority=b.priority or (a.priority is null and b.priority is null))
where b.id is null
order by a.code, a.version, a.status, a.name;

select '**********************************************************************************************************************************' from dual;

select 'REF_BOOK_PRESENT_PLACE' from dual; 
select '"id";"record_id";"version";"status";"code";"name"' from dual;
select 'Строки, которых нет в эталоне' from dual; 

select '"'||to_char(b.id)||'";"'||to_char(b.record_id)||'";"'||to_char(b.version,'DD.MM.YYYY')||'";"'||to_char(b.status)||'";"'||to_char(b.code)||'";"'||to_char(b.name)||'"'
from &3..REF_BOOK_PRESENT_PLACE a 
full outer join &2..REF_BOOK_PRESENT_PLACE b 
on a.version=b.version and a.status=b.status and a.code=b.code and a.name=b.name
where a.id is null
order by b.code, b.version, b.status, b.name;

select 'Строки, которые есть в эталоне, но отсутствуют в схеме' from dual; 

select '"'||to_char(a.id)||'";"'||to_char(a.record_id)||'";"'||to_char(a.version,'DD.MM.YYYY')||'";"'||to_char(a.status)||'";"'||to_char(a.code)||'";"'||to_char(a.name)||'"'
from &3..REF_BOOK_PRESENT_PLACE a 
full outer join &2..REF_BOOK_PRESENT_PLACE b 
on a.version=b.version and a.status=b.status and a.code=b.code and a.name=b.name
where b.id is null
order by a.code, a.version, a.status, a.name;

select '**********************************************************************************************************************************' from dual;

select 'REPORT_PERIOD_TYPE' from dual; 
select '"id";"code";"name";"start_date";"end_date";"calendar_start_date";"version";"status";"record_id"' from dual;
select 'Строки, которых нет в эталоне' from dual; 

select '"'||to_char(b.id)||'";"'||to_char(b.code)||'";"'||to_char(b.name)||'";"'||to_char(b.start_date,'DD.MM.YYYY')||'";"'||to_char(b.end_date,'DD.MM.YYYY')||'";"'||
to_char(b.calendar_start_date,'DD.MM.YYYY')||'";"'||to_char(b.version,'DD.MM.YYYY')||'";"'||to_char(b.status)||'";"'||to_char(b.record_id)||'"'
from &3..REPORT_PERIOD_TYPE a 
full outer join &2..REPORT_PERIOD_TYPE b 
on a.code=b.code and a.name=b.name and 
(a.start_date=b.start_date or (a.start_date is null and b.start_date is null)) and
(a.end_date=b.end_date or (a.end_date is null and b.end_date is null)) and 
(a.calendar_start_date=b.calendar_start_date or (a.calendar_start_date is null and b.calendar_start_date is null))  and 
a.version=b.version and a.status=b.status
where a.id is null
order by b.code, b.version, b.status, b.name;

select 'Строки, которые есть в эталоне, но отсутствуют в схеме' from dual; 

select '"'||to_char(a.id)||'";"'||to_char(a.code)||'";"'||to_char(a.name)||'";"'||to_char(a.start_date,'DD.MM.YYYY')||'";"'||to_char(a.end_date,'DD.MM.YYYY')||'";"'||
to_char(a.calendar_start_date,'DD.MM.YYYY')||'";"'||to_char(a.version,'DD.MM.YYYY')||'";"'||to_char(a.status)||'";"'||to_char(a.record_id)||'"'
from &3..REPORT_PERIOD_TYPE a 
full outer join &2..REPORT_PERIOD_TYPE b 
on a.code=b.code and a.name=b.name and 
(a.start_date=b.start_date or (a.start_date is null and b.start_date is null)) and
(a.end_date=b.end_date or (a.end_date is null and b.end_date is null)) and 
(a.calendar_start_date=b.calendar_start_date or (a.calendar_start_date is null and b.calendar_start_date is null))  and 
a.version=b.version and a.status=b.status
where b.id is null
order by a.code, a.version, a.status, a.name;

select '**********************************************************************************************************************************' from dual;

select 'REF_BOOK_REGION' from dual; 
select '"id";"record_id";"version";"status";"code";"name";"okato_definition";"oktmo_code";"oktmo_name";"oktmo_version";"oktmo_status";"oktmo_razd";"oktmo_definition"' from dual;
select 'Строки, которых нет в эталоне' from dual; 

select '"'||to_char(t2.id)||'";"'||to_char(t2.record_id)||'";"'||to_char(t2.version,'DD.MM.YYYY')||'";"'||to_char(t2.status)||'";"'||to_char(t2.code)||'";"'||to_char(t2.name)||
'";"'||to_char(t2.okato_definition)||'";"'||to_char(t2.oktmo_code)||'";"'||to_char(t2.oktmo_name)||'";"'||to_char(t2.oktmo_version,'DD.MM.YYYY')||'";"'||to_char(t2.oktmo_status)||
'";"'||to_char(t2.oktmo_razd)||'";"'||to_char(t2.oktmo_definition)||'"'
from 
(select a1.id, a1.record_id, a1.version, a1.status, a1.code, a1.name, a1.okato_definition,
b1.code as oktmo_code, b1.name as oktmo_name, b1.version as oktmo_version, b1.status as oktmo_status,
b1.razd as oktmo_razd, a1.oktmo_definition from &3..REF_BOOK_REGION a1
left join &3..ref_book_oktmo b1 on a1.oktmo=b1.id) t1 
full outer join
(select a2.id, a2.record_id, a2.version, a2.status, a2.code, a2.name, a2.okato_definition,
b2.code as oktmo_code, b2.name as oktmo_name, b2.version as oktmo_version, b2.status as oktmo_status,
b2.razd as oktmo_razd, a2.oktmo_definition  from &2..REF_BOOK_REGION a2
left join &2..ref_book_oktmo b2 on a2.oktmo=b2.id) t2 
on t1.version=t2.version and t1.status=t2.status and t1.code=t2.code and t1.name=t2.name and 
(t1.okato_definition=t2.okato_definition or (t1.okato_definition is null and t2.okato_definition is null)) and
(t1.oktmo_code=t2.oktmo_code or (t1.oktmo_code is null and t2.oktmo_code is null)) and 
(t1.oktmo_name=t2.oktmo_name or (t1.oktmo_name is null and t2.oktmo_name is null)) and 
(t1.oktmo_version=t2.oktmo_version or (t1.oktmo_version is null and t2.oktmo_version is null)) and 
(t1.oktmo_status=t2.oktmo_status or (t1.oktmo_status is null and t2.oktmo_status is null)) and   
(t1.oktmo_razd=t2.oktmo_razd or (t1.oktmo_razd is null and t2.oktmo_razd is null)) and
(t1.oktmo_definition=t2.oktmo_definition or (t1.oktmo_definition is null and t2.oktmo_definition is null))
where t1.id is null
order by t2.code, t2.version, t2.name;

select 'Строки, которые есть в эталоне, но отсутствуют в схеме' from dual; 

select '"'||to_char(t1.id)||'";"'||to_char(t1.record_id)||'";"'||to_char(t1.version,'DD.MM.YYYY')||'";"'||to_char(t1.status)||'";"'||to_char(t1.code)||'";"'||to_char(t1.name)||
'";"'||to_char(t1.okato_definition)||'";"'||to_char(t1.oktmo_code)||'";"'||to_char(t1.oktmo_name)||'";"'||to_char(t1.oktmo_version,'DD.MM.YYYY')||'";"'||to_char(t1.oktmo_status)||
'";"'||to_char(t1.oktmo_razd)||'";"'||to_char(t1.oktmo_definition)||'"'
from 
(select a1.id, a1.record_id, a1.version, a1.status, a1.code, a1.name, a1.okato_definition,
b1.code as oktmo_code, b1.name as oktmo_name, b1.version as oktmo_version, b1.status as oktmo_status,
b1.razd as oktmo_razd, a1.oktmo_definition from &3..REF_BOOK_REGION a1
left join &3..ref_book_oktmo b1 on a1.oktmo=b1.id) t1 
full outer join
(select a2.id, a2.record_id, a2.version, a2.status, a2.code, a2.name, a2.okato_definition,
b2.code as oktmo_code, b2.name as oktmo_name, b2.version as oktmo_version, b2.status as oktmo_status,
b2.razd as oktmo_razd, a2.oktmo_definition  from &2..REF_BOOK_REGION a2
left join &2..ref_book_oktmo b2 on a2.oktmo=b2.id) t2 
on t1.version=t2.version and t1.status=t2.status and t1.code=t2.code and t1.name=t2.name and 
(t1.okato_definition=t2.okato_definition or (t1.okato_definition is null and t2.okato_definition is null)) and
(t1.oktmo_code=t2.oktmo_code or (t1.oktmo_code is null and t2.oktmo_code is null)) and 
(t1.oktmo_name=t2.oktmo_name or (t1.oktmo_name is null and t2.oktmo_name is null)) and 
(t1.oktmo_version=t2.oktmo_version or (t1.oktmo_version is null and t2.oktmo_version is null)) and 
(t1.oktmo_status=t2.oktmo_status or (t1.oktmo_status is null and t2.oktmo_status is null)) and   
(t1.oktmo_razd=t2.oktmo_razd or (t1.oktmo_razd is null and t2.oktmo_razd is null)) and
(t1.oktmo_definition=t2.oktmo_definition or (t1.oktmo_definition is null and t2.oktmo_definition is null))
where t2.id is null
order by t1.code, t1.version, t1.name;

select '**********************************************************************************************************************************' from dual;

select 'REF_BOOK_REORGANIZATION' from dual; 
select '"id";"record_id";"version";"status";"code";"name"' from dual;
select 'Строки, которых нет в эталоне' from dual; 

select '"'||to_char(b.id)||'";"'||to_char(b.record_id)||'";"'||to_char(b.version,'DD.MM.YYYY')||'";"'||to_char(b.status)||'";"'||to_char(b.code)||'";"'||to_char(b.name)||'"'
from &3..REF_BOOK_REORGANIZATION a 
full outer join &2..REF_BOOK_REORGANIZATION b 
on a.version=b.version and a.status=b.status and a.code=b.code and a.name=b.name
where a.id is null
order by b.code, b.version, b.status, b.name;

select 'Строки, которые есть в эталоне, но отсутствуют в схеме' from dual; 

select '"'||to_char(a.id)||'";"'||to_char(a.record_id)||'";"'||to_char(a.version,'DD.MM.YYYY')||'";"'||to_char(a.status)||'";"'||to_char(a.code)||'";"'||to_char(a.name)||'"'
from &3..REF_BOOK_REORGANIZATION a 
full outer join &2..REF_BOOK_REORGANIZATION b 
on a.version=b.version and a.status=b.status and a.code=b.code and a.name=b.name
where b.id is null
order by a.code, a.version, a.status, a.name;

select '**********************************************************************************************************************************' from dual;

select 'REF_BOOK_DEDUCTION_MARK' from dual; 
select '"id";"record_id";"version";"status";"code";"name"' from dual;
select 'Строки, которых нет в эталоне' from dual; 

select '"'||to_char(b.id)||'";"'||to_char(b.record_id)||'";"'||to_char(b.version,'DD.MM.YYYY')||'";"'||to_char(b.status)||'";"'||to_char(b.code)||'";"'||to_char(b.name)||'"'
from &3..REF_BOOK_DEDUCTION_MARK a 
full outer join &2..REF_BOOK_DEDUCTION_MARK b 
on a.version=b.version and a.status=b.status and a.code=b.code and a.name=b.name
where a.id is null
order by b.code, b.version, b.status, b.name;

select 'Строки, которые есть в эталоне, но отсутствуют в схеме' from dual; 

select '"'||to_char(a.id)||'";"'||to_char(a.record_id)||'";"'||to_char(a.version,'DD.MM.YYYY')||'";"'||to_char(a.status)||'";"'||to_char(a.code)||'";"'||to_char(a.name)||'"'
from &3..REF_BOOK_DEDUCTION_MARK a 
full outer join &2..REF_BOOK_DEDUCTION_MARK b 
on a.version=b.version and a.status=b.status and a.code=b.code and a.name=b.name
where b.id is null
order by a.code, a.version, a.status, a.name;

select '**********************************************************************************************************************************' from dual;

select 'REF_BOOK_SIGNATORY_MARK' from dual; 
select '"id";"record_id";"status";"version";"code";"name"' from dual;
select 'Строки, которых нет в эталоне' from dual; 

select '"'||to_char(b.id)||'";"'||to_char(b.record_id)||'";"'||to_char(b.status)||'";"'||to_char(b.version,'DD.MM.YYYY')||'";"'||to_char(b.code)||'";"'||to_char(b.name)||'"'
from &3..REF_BOOK_SIGNATORY_MARK a 
full outer join &2..REF_BOOK_SIGNATORY_MARK b 
on a.status=b.status and a.version=b.version and a.code=b.code and a.name=b.name
where a.id is null
order by b.code, b.version, b.status, b.name;

select 'Строки, которые есть в эталоне, но отсутствуют в схеме' from dual; 

select '"'||to_char(a.id)||'";"'||to_char(a.record_id)||'";"'||to_char(a.status)||'";"'||to_char(a.version,'DD.MM.YYYY')||'";"'||to_char(a.code)||'";"'||to_char(a.name)||'"'
from &3..REF_BOOK_SIGNATORY_MARK a 
full outer join &2..REF_BOOK_SIGNATORY_MARK b 
on a.status=b.status and a.version=b.version and a.code=b.code and a.name=b.name
where b.id is null
order by a.code, a.version, a.status, a.name;

select '**********************************************************************************************************************************' from dual;
	
spool off;

exit;