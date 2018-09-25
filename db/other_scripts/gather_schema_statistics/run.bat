@ECHO OFF
REM строка подключения к бд
SET AUTH=ndfl_schema/schema_password@host:port/service_name
REM путь к папке BIN клиента oracle
SET ORA_BIN=c:\app\client\user\product\12.2.0\client_1\BIN
REM имя каталога для вывода результата выполнения
SET LOG_DIR=_logs

SET nls_lang=AMERICAN_AMERICA.AL32UTF8

ECHO ## DB: %AUTH%

MKDIR %LOG_DIR%

DEL /s /q /f %LOG_DIR%\*.log

ECHO ## gather schema statistics
"%ORA_BIN%\sqlplus" %AUTH% @"gather_schema_statistics.sql" %LOG_DIR%/gather_schema_statistics.log

PAUSE