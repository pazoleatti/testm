CREATE OR REPLACE VIEW DEPARTMENT_CHILD_VIEW AS 
WITH dep_with (id, parent_id, shortname) AS 
  (
    SELECT 
      dp.id, 
      dp.parent_id, 
      dp.shortname
    FROM department dp 
    WHERE dp.parent_id IS NULL
    UNION ALL 
    SELECT 
      dp.id, 
      dp.parent_id, 
      dep_with.shortname ||'/'|| dp.shortname
    FROM dep_with dep_with
    INNER JOIN department dp ON dp.parent_id = dep_with.id
  )
select a.id, a.parent_id, a.view_rowid, d. shortname from (   
select cast(connect_by_root id as number(9,0)) id, parent_id, (connect_by_root id)||'|'||parent_id view_rowid from department connect by prior parent_id = id
) a left join dep_with d on a.id=d.id
order by parent_id, id;