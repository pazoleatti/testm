-- Run the following commands as user SYS
grant select on pending_trans$ to TAX;
grant select on dba_2pc_pending to TAX;
grant select on dba_pending_transactions to TAX;
grant execute on dbms_xa to TAX; 