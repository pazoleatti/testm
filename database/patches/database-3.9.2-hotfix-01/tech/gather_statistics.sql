set verify off;
set serveroutput on;
spool &2
set lines 500

declare 
	status varchar2(2000);
begin
    DBMS_STATS.gather_SCHEMA_STATS (upper('&1'));
    dbms_output.put_line('Statistics gathered.');
exception when others then
    status := sqlerrm;
    dbms_output.put_line(status);
    raise_application_error(-20999,'Error gather statistics.');
end;
/

spool off;
exit;
