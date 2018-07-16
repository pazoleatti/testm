set heading off;
set feedback off;
set verify off;
set termout off;
set linesize 2000;
set trimspool on;
set NEWP NONE;
spool &1

select 'REF_BOOK_INCOME_TYPE' from dual; 
select '"id";"record_id";"version";"status";"code";"name"' from dual;
select 'Строки, которых нет в эталоне' from dual; 

select '"'||to_char(b.id)||'";"'||to_char(b.record_id)||'";"'||to_char(b.version,'DD.MM.YYYY')||'";"'||to_char(b.status)||'";"'||to_char(b.code)||'";"'||to_char(b.name)||'"'
from ndfl_etalon.ref_book_income_type a 
full outer join &2..ref_book_income_type b on 
a.code=b.code and a.name=b.name and a.status=b.status and a.version=b.version
where a.id is null
order by b.code, b.version, b.status, b.name;

select 'Строки, которых есть в эталоне, но отсутсвуют в схеме' from dual; 

select '"'||to_char(a.id)||'";"'||to_char(a.record_id)||'";"'||to_char(a.version,'DD.MM.YYYY')||'";"'||to_char(a.status)||'";"'||to_char(a.code)||'";"'||to_char(a.name)||'"'
from ndfl_etalon.ref_book_income_type a 
full outer join &2..ref_book_income_type b on 
a.code=b.code and a.name=b.name and a.status=b.status and a.version=b.version
where b.id is null
order by a.code, a.version, a.status, a.name;

select '**********************************************************************************************************************************' from dual;

select 'REF_BOOK_INCOME_KIND' from dual; 
select '"id";"record_id";"version";"status";"code";"income_type_version";"income_type_status";"name";"mark"' from dual;
select 'Строки, которых нет в эталоне' from dual;

select '"'||to_char(b.id)||'";"'||to_char(b.record_id)||'";"'||to_char(b.version,'DD.MM.YYYY')||'";"'||to_char(b.status)||'";"'||to_char(b.code)||'";"'||to_char(b.income_type_version,'DD.MM.YYYY')||'";"'
||to_char(b.income_type_status)||'";"'||to_char(b.name)||'";"'||to_char(b.mark)||'"'
from (select ika.id, ita.code, ita.version income_type_version, ita.status income_type_status,
ika.mark, ika.name, ika.version, ika.status, ika.record_id from ndfl_etalon.ref_book_income_kind ika 
join ndfl_etalon.ref_book_income_type ita on ika.income_type_id=ita.id) a 
full outer join (select ikb.id, itb.code, itb.version income_type_version, itb.status income_type_status,
ikb.mark,  ikb.name, ikb.version, ikb.status, ikb.record_id from &2..ref_book_income_kind ikb 
join &2..ref_book_income_type itb on ikb.income_type_id=itb.id) b 
on a.code=b.code and a.income_type_version=b.income_type_version and a.income_type_status=b.income_type_status 
and a.mark=b.mark and a.name=b.name and a.version=b.version and a.status=b.status
where a.id is null;

select 'Строки, которых есть в эталоне, но отсутсвуют в схеме' from dual; 

select '"'||to_char(a.id)||'";"'||to_char(a.record_id)||'";"'||to_char(a.version,'DD.MM.YYYY')||'";"'||to_char(a.status)||'";"'||to_char(a.code)||'";"'||to_char(a.income_type_version,'DD.MM.YYYY')||'";"'
||to_char(a.income_type_status)||'";"'||to_char(a.name)||'";"'||to_char(a.mark)||'"'
from (select ika.id, ita.code, ita.version income_type_version, ita.status income_type_status,
ika.mark, ika.name, ika.version, ika.status, ika.record_id from ndfl_etalon.ref_book_income_kind ika 
join ndfl_etalon.ref_book_income_type ita on ika.income_type_id=ita.id) a 
full outer join (select ikb.id, itb.code, itb.version income_type_version, itb.status income_type_status,
ikb.mark,  ikb.name, ikb.version, ikb.status, ikb.record_id from &2..ref_book_income_kind ikb 
join &2..ref_book_income_type itb on ikb.income_type_id=itb.id) b 
on a.code=b.code and a.income_type_version=b.income_type_version and a.income_type_status=b.income_type_status 
and a.mark=b.mark and a.name=b.name and a.version=b.version and a.status=b.status
where b.id is null;

select '**********************************************************************************************************************************' from dual;

select 'REF_BOOK_DEDUCTION_TYPE' from dual; 
select '"id";"record_id";"version";"status";"code";"name";"mark_version";"mark_status";"mark_code";"mark_name"' from dual;
select 'Строки, которых нет в эталоне' from dual;

select '"'||to_char(t2.id)||'";"'||to_char(t2.record_id)||'";"'||to_char(t2.version,'DD.MM.YYYY')||'";"'||to_char(t2.status)||'";"'||to_char(t2.code)||'";"'||to_char(t2.name)||'";"'
||to_char(t2.mark_version,'DD.MM.YYYY')||'";"'||to_char(t2.mark_status)||'";"'||to_char(t2.mark_code)||'";"'||to_char(t2.mark_name)||'"'
from 
(select a1.id, a1.record_id, a1.version, a1.status, a1.code, a1.name, b1.version as mark_version, b1.status as mark_status,
b1.code as mark_code, b1.name as mark_name from ndfl_etalon.REF_BOOK_DEDUCTION_TYPE a1
left join ndfl_etalon.ref_book_deduction_mark b1 on a1.deduction_mark=b1.id) t1 full outer join
(select a2.id, a2.record_id, a2.version, a2.status, a2.code, a2.name, b2.version as mark_version, b2.status as mark_status,
b2.code as mark_code, b2.name as mark_name from &2..REF_BOOK_DEDUCTION_TYPE a2
left join &2..ref_book_deduction_mark b2 on a2.deduction_mark=b2.id) t2 on 
t1.version=t1.version and t1.status=t2.status and t1.code=t2.code and t1.name=t2.name and 
t1.mark_version=t2.mark_version and t1.mark_status=t2.mark_status and t1.mark_code=t2.mark_code and t1.mark_name=t2.mark_name
where t1.id is null;

select 'Строки, которых есть в эталоне, но отсутсвуют в схеме' from dual; 

select '"'||to_char(t1.id)||'";"'||to_char(t1.record_id)||'";"'||to_char(t1.version,'DD.MM.YYYY')||'";"'||to_char(t1.status)||'";"'||to_char(t1.code)||'";"'||to_char(t1.name)||'";"'
||to_char(t1.mark_version,'DD.MM.YYYY')||'";"'||to_char(t1.mark_status)||'";"'||to_char(t1.mark_code)||'";"'||to_char(t1.mark_name)||'"'
from
(select a1.id, a1.record_id, a1.version, a1.status, a1.code, a1.name, b1.version as mark_version, b1.status as mark_status,
b1.code as mark_code, b1.name as mark_name from ndfl_etalon.REF_BOOK_DEDUCTION_TYPE a1
left join ndfl_etalon.ref_book_deduction_mark b1 on a1.deduction_mark=b1.id) t1 full outer join
(select a2.id, a2.record_id, a2.version, a2.status, a2.code, a2.name, b2.version as mark_version, b2.status as mark_status,
b2.code as mark_code, b2.name as mark_name from &2..REF_BOOK_DEDUCTION_TYPE a2
left join &2..ref_book_deduction_mark b2 on a2.deduction_mark=b2.id) t2 on 
t1.version=t1.version and t1.status=t2.status and t1.code=t2.code and t1.name=t2.name and 
t1.mark_version=t2.mark_version and t1.mark_status=t2.mark_status and t1.mark_code=t2.mark_code and t1.mark_name=t2.mark_name
where t2.id is null;

select '**********************************************************************************************************************************' from dual;

select 'REF_BOOK_ASNU' from dual; 
select '"id";"code";"name";"type";"role_alias";"role_name";"priority"' from dual;
select 'Строки, которых нет в эталоне' from dual;

select '"'||to_char(b.id)||'";"'||to_char(b.code)||'";"'||to_char(b.name)||'";"'||to_char(b.role_alias)||'";"'||to_char(b.role_name)||'";"'||to_char(b.priority)||'"'
from ndfl_etalon.ref_book_asnu a
full outer join &2..ref_book_asnu b on a.code=b.code and a.name=b.name and a.type=b.type and a.role_alias=b.role_alias and a.priority=b.priority
where a.id is null;

select 'Строки, которых есть в эталоне, но отсутсвуют в схеме' from dual; 

select '"'||to_char(a.id)||'";"'||to_char(a.code)||'";"'||to_char(a.name)||'";"'||to_char(a.role_alias)||'";"'||to_char(a.role_name)||'";"'||to_char(a.priority)||'"'
from ndfl_etalon.ref_book_asnu a
full outer join &2..ref_book_asnu b on a.code=b.code and a.name=b.name and a.type=b.type and a.role_alias=b.role_alias and a.priority=b.priority
where b.id is null;

select '**********************************************************************************************************************************' from dual;

select 'REF_BOOK_CALENDAR' from dual; 
select '"id";"cdate";"ctype"' from dual;
select 'Строки, которых нет в эталоне' from dual;

select '"'||to_char(b.id)||'";"'||to_char(b.cdate,'DD.MM.YYYY')||'";"'||to_char(b.ctype)||'"'
from ndfl_etalon.REF_BOOK_CALENDAR a
full outer join &2..REF_BOOK_CALENDAR b on a.cdate=b.cdate and a.ctype=b.ctype
where a.id is null;

select 'Строки, которых есть в эталоне, но отсутсвуют в схеме' from dual; 

select '"'||to_char(a.id)||'";"'||to_char(a.cdate,'DD.MM.YYYY')||'";"'||to_char(a.ctype)||'"'
from ndfl_etalon.REF_BOOK_CALENDAR a
full outer join &2..REF_BOOK_CALENDAR b on a.cdate=b.cdate and a.ctype=b.ctype
where b.id is null;

select '**********************************************************************************************************************************' from dual;

select 'REF_BOOK_DOC_TYPE' from dual; 
select '"id";"record_id";"status";"version";"code";"name";"priority"' from dual;
select 'Строки, которых нет в эталоне' from dual;

select '"'||to_char(b.id)||'";"'||to_char(b.record_id)||'";"'||to_char(b.status)||'";"'||to_char(b.version,'DD.MM.YYYY')||'";"'||to_char(b.name)||'";"'||to_char(b.priority)||'"'
from ndfl_etalon.REF_BOOK_DOC_TYPE a
full outer join &2..REF_BOOK_DOC_TYPE b on a.status=b.status and a.version=b.version and a.code=b.code and a.name=b.name and a.priority=b.priority
where a.id is null;

select 'Строки, которых есть в эталоне, но отсутсвуют в схеме' from dual; 

select '"'||to_char(a.id)||'";"'||to_char(a.record_id)||'";"'||to_char(a.status)||'";"'||to_char(a.version,'DD.MM.YYYY')||'";"'||to_char(a.name)||'";"'||to_char(a.priority)||'"'
from ndfl_etalon.REF_BOOK_DOC_TYPE a
full outer join &2..REF_BOOK_DOC_TYPE b on a.status=b.status and a.version=b.version and a.code=b.code and a.name=b.name and a.priority=b.priority
where b.id is null;

select '**********************************************************************************************************************************' from dual;

select 'REF_BOOK_PRESENT_PLACE' from dual; 
select '"id";"record_id";"version";"status";"code";"name"' from dual;
select 'Строки, которых нет в эталоне' from dual; 

select '"'||to_char(b.id)||'";"'||to_char(b.record_id)||'";"'||to_char(b.version,'DD.MM.YYYY')||'";"'||to_char(b.status)||'";"'||to_char(b.code)||'";"'||to_char(b.name)||'"'
from ndfl_etalon.REF_BOOK_PRESENT_PLACE a 
full outer join &2..REF_BOOK_PRESENT_PLACE b on 
a.code=b.code and a.name=b.name and a.status=b.status and a.version=b.version
where a.id is null
order by b.code, b.version, b.status, b.name;

select 'Строки, которых есть в эталоне, но отсутсвуют в схеме' from dual; 

select '"'||to_char(a.id)||'";"'||to_char(a.record_id)||'";"'||to_char(a.version,'DD.MM.YYYY')||'";"'||to_char(a.status)||'";"'||to_char(a.code)||'";"'||to_char(a.name)||'"'
from ndfl_etalon.REF_BOOK_PRESENT_PLACE a 
full outer join &2..REF_BOOK_PRESENT_PLACE b on 
a.code=b.code and a.name=b.name and a.status=b.status and a.version=b.version
where b.id is null
order by a.code, a.version, a.status, a.name;

select '**********************************************************************************************************************************' from dual;

select 'REPORT_PERIOD_TYPE' from dual; 
select '"id";"record_id";"version";"status";"code";"name";"start_date";"end_date";"calendar_start_date"' from dual;
select 'Строки, которых нет в эталоне' from dual; 

select '"'||to_char(b.id)||'";"'||to_char(b.record_id)||'";"'||to_char(b.version,'DD.MM.YYYY')||'";"'||to_char(b.status)||'";"'||to_char(b.code)||'";"'||to_char(b.name)||
'";"'||to_char(b.start_date,'DD.MM.YYYY')||'";"'||to_char(b.end_date,'DD.MM.YYYY')||'";"'||to_char(b.calendar_start_date,'DD.MM.YYYY')||'"'
from ndfl_etalon.REPORT_PERIOD_TYPE a 
full outer join &2..REPORT_PERIOD_TYPE b on 
a.code=b.code and a.name=b.name and a.status=b.status and a.version=b.version and a.start_date=b.start_date and a.end_date=b.end_date and a.calendar_start_date=b.calendar_start_date
where a.id is null
order by b.code, b.version, b.status, b.name;

select 'Строки, которых есть в эталоне, но отсутсвуют в схеме' from dual; 

select '"'||to_char(a.id)||'";"'||to_char(a.record_id)||'";"'||to_char(a.version,'DD.MM.YYYY')||'";"'||to_char(a.status)||'";"'||to_char(a.code)||'";"'||to_char(a.name)||
'";"'||to_char(a.start_date,'DD.MM.YYYY')||'";"'||to_char(a.end_date,'DD.MM.YYYY')||'";"'||to_char(a.calendar_start_date,'DD.MM.YYYY')||'"'
from ndfl_etalon.REPORT_PERIOD_TYPE a 
full outer join &2..REPORT_PERIOD_TYPE b on 
a.code=b.code and a.name=b.name and a.status=b.status and a.version=b.version and a.start_date=b.start_date and a.end_date=b.end_date and a.calendar_start_date=b.calendar_start_date
where b.id is null
order by a.code, a.version, a.status, a.name;

select '**********************************************************************************************************************************' from dual;

select 'REF_BOOK_REGION' from dual; 
select '"id";"record_id";"version";"status";"code";"name";"okato_definition";"oktmo_definition";"oktmo_version";"oktmo_status";"oktmo_code";"oktmo_name";"oktmo_razd"' from dual;
select 'Строки, которых нет в эталоне' from dual; 

select '"'||to_char(t2.id)||'";"'||to_char(t2.record_id)||'";"'||to_char(t2.version,'DD.MM.YYYY')||'";"'||to_char(t2.status)||'";"'||to_char(t2.code)||'";"'||to_char(t2.name)||
'";"'||to_char(t2.okato_definition)||'";"'||to_char(t2.oktmo_definition)||'";"'||to_char(t2.oktmo_version,'DD.MM.YYYY')||'";"'||to_char(t2.oktmo_status)||
'";"'||to_char(t2.oktmo_code)||'";"'||to_char(t2.oktmo_name)||'";"'||to_char(t2.oktmo_razd)||'"'
from 
(select a1.id, a1.record_id, a1.version, a1.status, a1.code, a1.name, a1.okato_definition, a1.oktmo_definition, 
b1.version as oktmo_version, b1.status as oktmo_status,
b1.code as oktmo_code, b1.name as oktmo_name, b1.razd as oktmo_razd from ndfl_etalon.REF_BOOK_REGION a1
left join ndfl_etalon.ref_book_oktmo b1 on a1.oktmo=b1.id) t1 full outer join
(select a2.id, a2.record_id, a2.version, a2.status, a2.code, a2.name, a2.okato_definition, a2.oktmo_definition, 
b2.version as oktmo_version, b2.status as oktmo_status,
b2.code as oktmo_code, b2.name as oktmo_name, b2.razd as oktmo_razd from &2..REF_BOOK_REGION a2
left join &2..ref_book_oktmo b2 on a2.oktmo=b2.id) t2 on 
t1.version=t1.version and t1.status=t2.status and t1.code=t2.code and t1.name=t2.name and 
t1.okato_definition=t2.okato_definition and t1.oktmo_definition=t2.oktmo_definition and
t1.oktmo_version=t2.oktmo_version and t1.oktmo_status=t2.oktmo_status and t1.oktmo_code=t2.oktmo_code and t1.oktmo_name=t2.oktmo_name and t1.oktmo_razd=t2.oktmo_razd
where t1.id is null;

select 'Строки, которых есть в эталоне, но отсутсвуют в схеме' from dual; 

select '"'||to_char(t1.id)||'";"'||to_char(t1.record_id)||'";"'||to_char(t1.version,'DD.MM.YYYY')||'";"'||to_char(t1.status)||'";"'||to_char(t1.code)||'";"'||to_char(t1.name)||
'";"'||to_char(t1.okato_definition)||'";"'||to_char(t1.oktmo_definition)||'";"'||to_char(t1.oktmo_version,'DD.MM.YYYY')||'";"'||to_char(t1.oktmo_status)||
'";"'||to_char(t1.oktmo_code)||'";"'||to_char(t1.oktmo_name)||'";"'||to_char(t1.oktmo_razd)||'"'
from 
(select a1.id, a1.record_id, a1.version, a1.status, a1.code, a1.name, a1.okato_definition, a1.oktmo_definition, 
b1.version as oktmo_version, b1.status as oktmo_status,
b1.code as oktmo_code, b1.name as oktmo_name, b1.razd as oktmo_razd from ndfl_etalon.REF_BOOK_REGION a1
left join ndfl_etalon.ref_book_oktmo b1 on a1.oktmo=b1.id) t1 full outer join
(select a2.id, a2.record_id, a2.version, a2.status, a2.code, a2.name, a2.okato_definition, a2.oktmo_definition, 
b2.version as oktmo_version, b2.status as oktmo_status,
b2.code as oktmo_code, b2.name as oktmo_name, b2.razd as oktmo_razd from &2..REF_BOOK_REGION a2
left join &2..ref_book_oktmo b2 on a2.oktmo=b2.id) t2 on 
t1.version=t1.version and t1.status=t2.status and t1.code=t2.code and t1.name=t2.name and 
t1.okato_definition=t2.okato_definition and t1.oktmo_definition=t2.oktmo_definition and
t1.oktmo_version=t2.oktmo_version and t1.oktmo_status=t2.oktmo_status and t1.oktmo_code=t2.oktmo_code and t1.oktmo_name=t2.oktmo_name and t1.oktmo_razd=t2.oktmo_razd
where t2.id is null;

select '**********************************************************************************************************************************' from dual;

select 'REF_BOOK_REORGANIZATION' from dual; 
select '"id";"record_id";"version";"status";"code";"name"' from dual;
select 'Строки, которых нет в эталоне' from dual; 

select '"'||to_char(b.id)||'";"'||to_char(b.record_id)||'";"'||to_char(b.version,'DD.MM.YYYY')||'";"'||to_char(b.status)||'";"'||to_char(b.code)||'";"'||to_char(b.name)||'"'
from ndfl_etalon.REF_BOOK_REORGANIZATION a 
full outer join &2..REF_BOOK_REORGANIZATION b on 
a.code=b.code and a.name=b.name and a.status=b.status and a.version=b.version
where a.id is null
order by b.code, b.version, b.status, b.name;

select 'Строки, которых есть в эталоне, но отсутсвуют в схеме' from dual; 

select '"'||to_char(a.id)||'";"'||to_char(a.record_id)||'";"'||to_char(a.version,'DD.MM.YYYY')||'";"'||to_char(a.status)||'";"'||to_char(a.code)||'";"'||to_char(a.name)||'"'
from ndfl_etalon.REF_BOOK_REORGANIZATION a 
full outer join &2..REF_BOOK_REORGANIZATION b on 
a.code=b.code and a.name=b.name and a.status=b.status and a.version=b.version
where b.id is null
order by a.code, a.version, a.status, a.name;

select '**********************************************************************************************************************************' from dual;

select 'REF_BOOK_DEDUCTION_MARK' from dual; 
select '"id";"record_id";"version";"status";"code";"name"' from dual;
select 'Строки, которых нет в эталоне' from dual; 

select '"'||to_char(b.id)||'";"'||to_char(b.record_id)||'";"'||to_char(b.version,'DD.MM.YYYY')||'";"'||to_char(b.status)||'";"'||to_char(b.code)||'";"'||to_char(b.name)||'"'
from ndfl_etalon.REF_BOOK_DEDUCTION_MARK a 
full outer join &2..REF_BOOK_DEDUCTION_MARK b on 
a.code=b.code and a.name=b.name and a.status=b.status and a.version=b.version
where a.id is null
order by b.code, b.version, b.status, b.name;

select 'Строки, которых есть в эталоне, но отсутсвуют в схеме' from dual; 

select '"'||to_char(a.id)||'";"'||to_char(a.record_id)||'";"'||to_char(a.version,'DD.MM.YYYY')||'";"'||to_char(a.status)||'";"'||to_char(a.code)||'";"'||to_char(a.name)||'"'
from ndfl_etalon.REF_BOOK_DEDUCTION_MARK a 
full outer join &2..REF_BOOK_DEDUCTION_MARK b on 
a.code=b.code and a.name=b.name and a.status=b.status and a.version=b.version
where b.id is null
order by a.code, a.version, a.status, a.name;

select '**********************************************************************************************************************************' from dual;

select 'REF_BOOK_SIGNATORY_MARK' from dual; 
select '"id";"record_id";"version";"status";"code";"name"' from dual;
select 'Строки, которых нет в эталоне' from dual; 

select '"'||to_char(b.id)||'";"'||to_char(b.record_id)||'";"'||to_char(b.version,'DD.MM.YYYY')||'";"'||to_char(b.status)||'";"'||to_char(b.code)||'";"'||to_char(b.name)||'"'
from ndfl_etalon.REF_BOOK_SIGNATORY_MARK a 
full outer join &2..REF_BOOK_SIGNATORY_MARK b on 
a.code=b.code and a.name=b.name and a.status=b.status and a.version=b.version
where a.id is null
order by b.code, b.version, b.status, b.name;

select 'Строки, которых есть в эталоне, но отсутсвуют в схеме' from dual; 

select '"'||to_char(a.id)||'";"'||to_char(a.record_id)||'";"'||to_char(a.version,'DD.MM.YYYY')||'";"'||to_char(a.status)||'";"'||to_char(a.code)||'";"'||to_char(a.name)||'"'
from ndfl_etalon.REF_BOOK_SIGNATORY_MARK a 
full outer join &2..REF_BOOK_SIGNATORY_MARK b on 
a.code=b.code and a.name=b.name and a.status=b.status and a.version=b.version
where b.id is null
order by a.code, a.version, a.status, a.name;

select '**********************************************************************************************************************************' from dual;
	
spool off;

exit;	