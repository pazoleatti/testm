@ECHO OFF

SET AUTH=system/oracle@172.19.214.45:1521/orcl.aplana.local

SET SCHEME=NDFL_UNSTABLE

SET ORA_BIN=c:\app\client\ghy\product\12.2.0\client_1\BIN
SET LOG_DIR=_logs
SET RECORD_ID=15165880
SET VIP_FLAG=0
SET nls_lang=AMERICAN_AMERICA.AL32UTF8

ECHO ## DB: %AUTH%

MKDIR %LOG_DIR%

DEL /s /q /f %LOG_DIR%\*.log

ECHO ## set VIP flag for person RECORD_ID
"%ORA_BIN%\sqlplus" %AUTH% @"set_vip_for_person.sql" %LOG_DIR%/set_vip_for_person.log %SCHEME% %RECORD_ID% %VIP_FLAG%


PAUSE