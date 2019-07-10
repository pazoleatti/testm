set long 32767
set pagesize 0
set linesize 4000
set feedback off
set echo off
set verify off
set trims on
set heading off
--set termout off
set serveroutput on size 1000000;
spool &1;

delete from plan_table;

explain plan for SELECT DISTINCT operation_id FROM ndfl_person_income WHERE operation_id IN (:operationIdList);

SELECT  lpad(' ',level-1)||operation||' '||
       options||' '||object_name "Plan", cardinality "Rows", cost, bytes, cpu_cost, io_cost
  FROM PLAN_TABLE
CONNECT BY prior id = parent_id
        AND prior plan_id = plan_id
  START WITH id = 0        
 ORDER BY id;



exit;
