merge into configuration a using
(select 'CONSOLIDATION_DATA_SELECTION_DEPTH' as code, 0 as department_id, 3 as value from dual) b
on (a.code=b.code)
when not matched then 
insert (code, department_id, value)
values (b.code, b.department_id, b.value);