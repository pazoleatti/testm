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

ECHO ## set fatal for decl_template_checks
"%ORA_BIN%\sqlplus" %AUTH% @"set_fatal_for_decl_template.sql" %LOG_DIR%/set_fatal_for_decl_template_checks.log

PAUSE