set heading off;
set feedback off;
set verify off;
set termout off;
set linesize 2000;
set trimspool on;
set NEWP NONE;
spool &1

select name from department where id=&2;
select '"ID";"NAME";"SHORTNAME";"SBRF_CODE"' from dual;
select '"'||to_char(id)||'";"'||to_char(name)||'";"'||to_char(shortname)||'";"'||to_char(sbrf_code)||'"'
from 
(select distinct d.id,d.name,d.shortname,d.sbrf_code from
	(select id from department_child_view where parent_id=&2
	 union
	 select &2 as id from dual
	) d_id
	join department_decl_type_performer ddtp on ddtp.performer_dep_id=d_id.id
	join department_declaration_type ddt on ddt.id=ddtp.department_decl_type_id
	join department d on d.id=ddt.department_id
	order by 1
);

spool off;

exit;