@ECHO OFF

SET SCHEME=NDFL_UNSTABLE
SET ETALON_SCHEME=TAXNSI

SET AUTH=system/oracle@172.19.214.45:1521/orcl.aplana.local
SET ORA_BIN=c:\app\client\user\product\12.2.0\client_1\BIN
SET LOG_DIR=_logs
SET BAD_DIR=_bad
SET nls_lang=AMERICAN_AMERICA.AL32UTF8

ECHO ## DB: %AUTH%

MKDIR %LOG_DIR%

DEL /s /q /f %LOG_DIR%\*.csv

ECHO ## compare_REF_BOOK_OKTMO
"%ORA_BIN%\sqlplus" %AUTH% @"compare_REF_BOOK_OKTMO.sql" %LOG_DIR%/compare_REF_BOOK_OKTMO.csv %SCHEME% %ETALON_SCHEME%


PAUSE