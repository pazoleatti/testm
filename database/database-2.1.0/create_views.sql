CREATE OR REPLACE VIEW DEPARTMENT_CHILD_VIEW AS 
select cast(connect_by_root id as number(9,0)) id, parent_id, (connect_by_root id)||'|'||parent_id view_rowid from department connect by prior parent_id = id
order by parent_id, id;


CREATE OR REPLACE VIEW DEPARTMENT_FULLPATH AS 
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
      dep_with.shortname ||'/'|| + nvl(dp.shortname,dp.name)
    FROM dep_with dep_with
    INNER JOIN department dp ON dp.parent_id = dep_with.id
  )
select id, shortname from dep_with
order by id;

CREATE OR REPLACE VIEW SEC_USER_NDFL AS 
select distinct a.* from sec_user a join sec_user_role b on a.id=b.user_id join subsystem_role c on b.role_id=c.role_id where c.subsystem_id=11 order by name, login;

CREATE OR REPLACE VIEW SEC_ROLE_NDFL AS 
select distinct a.* from sec_role a join subsystem_role c on a.id=c.role_id where c.subsystem_id=11 order by id;