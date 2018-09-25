set serveroutput on;
set verify off;
set termout off;
spool &1;

begin
dbms_stats.gather_schema_stats(null);
end;
/
exit;
