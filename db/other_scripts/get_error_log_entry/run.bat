@ECHO OFF
REM строка подключения к бд
SET AUTH=ndfl_schema/schema_password@host:port/service_name
REM путь к папке BIN клиента oracle
SET ORA_BIN=c:\app\client\user\product\12.2.0\client_1\BIN
REM имя каталога для вывода результата выполнения
SET LOG_DIR=_logs

SET nls_lang=AMERICAN_AMERICA.AL32UTF8

ECHO ## DB: %AUTH%

IF NOT EXIST %LOG_DIR% MKDIR %LOG_DIR%

IF EXIST %LOG_DIR%\*.log DEL /s /q /f %LOG_DIR%\*.log

ECHO ## Get error log.  Please wait...
"%ORA_BIN%\sqlplus" %AUTH% @"get_error_log_entry.sql" %LOG_DIR%/get_error_log_entry.log

PAUSE