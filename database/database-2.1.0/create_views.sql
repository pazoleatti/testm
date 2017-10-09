CREATE OR REPLACE VIEW DEPARTMENT_CHILD_VIEW AS 
select  cast(connect_by_root id as number(9,0)) id, parent_id, (connect_by_root id)||'|'||parent_id view_rowid from department connect by prior parent_id = id
order by parent_id, id;
