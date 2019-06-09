@ECHO OFF
REM Нужно заполнить имя и пароль владельца схемы и алиас базы данных
REM Формат: user_name/password@host:port/service_name
SET AUTH=ndfl_next2/ndfl_next2@172.19.214.46:1521/orcl.aplana.local
REM Нужно прописать путь к папке ORACLE_HOME\BIN
SET ORA_BIN=C:\app\oracle\product\11.2.0\dbhome_1\BIN
SET LOG_DIR=_logs
SET BAD_DIR=_bad
SET nls_lang=AMERICAN_AMERICA.AL32UTF8

ECHO ## DB: %AUTH%

SET MODE=1
MKDIR %LOG_DIR%
MKDIR %BAD_DIR%

DEL /s /q /f %LOG_DIR%\*.txt
DEL /s /q /f %BAD_DIR%\*.*

ECHO ## ddl
"%ORA_BIN%\sqlplus" %AUTH% @"update_ddl.sql" > "%LOG_DIR%/update_ddl.txt"

ECHO ## dml
"%ORA_BIN%\sqlplus" %AUTH% @"update_dml.sql" > "%LOG_DIR%/update_dml.txt"

ECHO ## dml: templates
CD templates/script
CALL templates.bat
CD ../..

rem добавить скрипт обновления последовательностей
PAUSE