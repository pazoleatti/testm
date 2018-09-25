@ECHO OFF

SET AUTH=ndfl_schema/schema_password@host:port/service_name
SET DECLARATION_ID=10000
SET ASNU=1

SET ORA_BIN=c:\app\client\user\product\12.2.0\client_1\BIN
SET LOG_DIR=_logs
SET BAD_DIR=_bad
SET nls_lang=AMERICAN_AMERICA.AL32UTF8

ECHO ## DB: %AUTH%

MKDIR %LOG_DIR%

DEL /s /q /f %LOG_DIR%\*.txt

ECHO ## GetPersonForCheck_count_speed
"%ORA_BIN%\sqlplus" %AUTH% @"GetPersonForCheck_count_speed.sql" %LOG_DIR%/GetPersonForCheck_count_speed %DECLARATION_ID% %ASNU%

COPY %LOG_DIR%\GetPersonForCheck_count_speed-1.txt+%LOG_DIR%\GetPersonForCheck_count_speed-2.txt+%LOG_DIR%\GetPersonForCheck_count_speed-3.txt+%LOG_DIR%\GetPersonForCheck_count_speed-4.txt+%LOG_DIR%\GetPersonForCheck_count_speed-5.txt %LOG_DIR%\GetPersonForCheck_count_speed.txt
DEL /s /q /f %LOG_DIR%\*-*.txt

PAUSE