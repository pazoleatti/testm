@ECHO OFF

SET AUTH=system/system_password@host:port/service_name
SET SCHEMA=NDFL_DEV
SET SHOW_OTHER_SCHEMAS=Y

SET ORA_BIN=C:\app\client\user\product\12.2.0\client_1\bin
SET LOG_DIR=_logs
SET BAD_DIR=_bad
SET nls_lang=AMERICAN_AMERICA.AL32UTF8

ECHO ## DB: %AUTH%

MKDIR %LOG_DIR%

DEL /s /q /f %LOG_DIR%\*

ECHO ## Object Space
"%ORA_BIN%\sqlplus" %AUTH% @"Object_Space.sql" %LOG_DIR%/Object_Space.csv %SCHEMA% %SHOW_OTHER_SCHEMAS%

PAUSE