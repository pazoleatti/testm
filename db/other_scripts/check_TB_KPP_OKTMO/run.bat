@ECHO OFF

SET AUTH=ndfl_schema/schema_password@host:port/service_name
SET ORA_BIN=c:\app\client\user\product\12.2.0\client_1\BIN
SET LOG_DIR=_logs
SET NLS_LANG=AMERICAN_AMERICA.CL8MSWIN1251

ECHO ## DB: %AUTH%

MKDIR %LOG_DIR%

DEL /s /q /f %LOG_DIR%\*.csv

ECHO ## check_TB_KPP_OKTMO
"%ORA_BIN%\sqlplus" %AUTH% @"check_TB_KPP_OKTMO.sql"


PAUSE