set feedback off;
set verify off;
set serveroutput on;
set linesize 115
spool &1._&2..txt

var NDFL_USER VARCHAR2(50);

begin
   :NDFL_USER := upper('&2');
end;
/

begin 
  dbms_output.put_line (chr(13));
  dbms_output.put_line ('----User quotas----');
  dbms_output.put_line (chr(13));
end;
/
select tablespace_name, username, bytes/1024/1024 "SIZE", max_bytes, blocks, max_blocks from dba_ts_quotas 
where USERNAME=upper(:NDFL_USER);

begin 
  dbms_output.put_line (chr(13));
  dbms_output.put_line ('----Size of tablespaces----');
  dbms_output.put_line (chr(13));
end;
/

select tablespace_name, sum (bytes)/1024/1024 "SIZE" from dba_data_files where tablespace_name in 
(select tablespace_name from dba_ts_quotas where USERNAME=upper(:NDFL_USER))
group by tablespace_name;

begin 
  dbms_output.put_line (chr(13));
  dbms_output.put_line ('----Free space----');
  dbms_output.put_line ('Please wait...');
end;
/

select tablespace_name, sum(bytes)/1024/1024 "FREE SPACE" from dba_free_space where tablespace_name in 
(select tablespace_name from dba_ts_quotas where USERNAME=upper(:NDFL_USER)) group by tablespace_name;


begin 
  dbms_output.put_line (chr(13));
  dbms_output.put_line ('----Gather statistics of NDFL_PERSON----');
  dbms_output.put_line ('Please wait...');
end;
/
begin
  dbms_stats.gather_table_stats(:NDFL_USER,'NDFL_PERSON');
  dbms_output.put_line ('----Gather statistics of NDFL_PERSON_INCOME----');
  dbms_output.put_line ('Please wait...');
end;
/
begin
  dbms_stats.gather_table_stats(:NDFL_USER,'NDFL_PERSON_INCOME');
  dbms_output.put_line (chr(13));
  dbms_output.put_line ('----Size of tables----');
  dbms_output.put_line (chr(13));
end;
/

SELECT TABLE_NAME, NUM_ROWS*AVG_ROW_LEN/1024/1024 "SIZE", BLOCKS*8/1024  "REAL SIZE"
 FROM DBA_TABLES
  where owner=:NDFL_USER and TABLE_NAME = 'NDFL_PERSON';

begin 
  dbms_output.put_line (chr(13));
end;
/

SELECT TABLE_NAME, ROUND((NUM_ROWS*AVG_ROW_LEN/1024),2)/1024 "SIZE",BLOCKS*8/1024 "REAL SIZE"
 FROM DBA_TABLES
  where owner=:NDFL_USER and TABLE_NAME = 'NDFL_PERSON_INCOME';


exit;
/
                              
