@ECHO OFF
REM Нужно заполнить имя и пароль владельца схемы и алиас базы данных
REM Формат: user_name/password@host:port/service_name
SET AUTH=NDFL/NDFL@ORCL
REM SET AUTH=ndfl_test/ndfl_test@172.19.214.46:1521/orcl.aplana.local
REM Нужно прописать путь к папке ORACLE_HOME\BIN
SET ORA_BIN=C:\Oracle\product\11.2.0\dbhome_2\BIN
SET LOG_DIR=logs
REM SET BAD_DIR=bad
SET nls_lang=AMERICAN_AMERICA.AL32UTF8

ECHO ## DB: %AUTH%
ECHO ## clean

MKDIR %LOG_DIR%
REM MKDIR %BAD_DIR%

DEL /s /q /f %LOG_DIR%\*.txt
REM DEL /s /q /f %BAD_DIR%\*.*

ECHO ## 1: patch to v1.0.1
"%ORA_BIN%\sqlplus" %AUTH% @"update_1_0_1.sql" > "%LOG_DIR%/update_1_0_1.txt"
PAUSE