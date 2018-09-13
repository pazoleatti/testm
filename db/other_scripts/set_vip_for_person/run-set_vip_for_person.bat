@ECHO OFF
REM строка подключения к бд
SET AUTH=ndfl_schema/schema_password@host:port/service_name
REM путь к папке BIN клиента oracle
SET ORA_BIN=c:\app\client\user\product\12.2.0\client_1\BIN
REM имя каталога для вывода результата выполнения
SET LOG_DIR=_logs
REM ИД ФЛ
SET RECORD_ID=1234567
REM признак Важность ФЛ
SET VIP_FLAG=0

SET nls_lang=AMERICAN_AMERICA.AL32UTF8

ECHO ## DB: %AUTH%

MKDIR %LOG_DIR%

DEL /s /q /f %LOG_DIR%\*.log

ECHO ## set VIP flag for person RECORD_ID
"%ORA_BIN%\sqlplus" %AUTH% @"set_vip_for_person.sql" %LOG_DIR%/set_vip_for_person.log %RECORD_ID% %VIP_FLAG%

PAUSE