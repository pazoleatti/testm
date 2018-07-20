@ECHO OFF

SET AUTH=system/oracle@172.19.214.45:1521/orcl.aplana.local

SET SCHEME=NDFL_UNSTABLE

SET ORA_BIN=c:\app\client\akorobko\product\12.1.0\client_1\BIN
SET LOG_DIR=_logs
SET BAD_DIR=_bad
SET nls_lang=AMERICAN_AMERICA.AL32UTF8

ECHO ## DB: %AUTH%

MKDIR %LOG_DIR%

DEL /s /q /f %LOG_DIR%\*.csv

ECHO ## compare_references
"%ORA_BIN%\sqlplus" %AUTH% @"compare_references.sql" %LOG_DIR%/compare_references.csv %SCHEME%


PAUSE