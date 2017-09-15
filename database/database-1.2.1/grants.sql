grant create job to &1;
grant select on pending_trans$ to &1;
grant select on dba_2pc_pending to &1;
grant select on dba_pending_transactions to &1;
grant execute on dbms_system to &1; 
grant execute on dbms_xa to &1;


commit;
exit;
