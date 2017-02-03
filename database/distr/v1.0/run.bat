@ECHO OFF
REM указать имя/пароль владельца схемы и алиас базы данных
SET NDFL_USER=
SET NDFL_USER=
SET NDFL_USER=
REM SET NDFL_USER=NDFL
REM SET NDFL_PASS=NDFL
REM SET NDFL_DBNAME=orcl
SET AUTH=%NDFL_USER%/%NDFL_PASS%@%NDFL_DBNAME%
REM SET AUTH=NDFL_NEXT/TAX@172.16.127.16:1521/orcl.aplana.local
ECHO ## DB: %AUTH%

REM указать путь к папке bin в ORA_HOME
REM SET ORA_BIN=C:\app\oracle\product\11.2.0\dbhome_1\BIN
SET LOG_DIR=logs
SET nls_lang=AMERICAN_AMERICA.AL32UTF8

ECHO ## clean
DEL /s /q /f %LOG_DIR%\*.txt
rem MKDIR %LOG_DIR%

ECHO ## ddl: create_all_tables
"%ORA_BIN%\sqlplus" %AUTH% @"create_all_tables.sql" > "%LOG_DIR%/create_all_tables.txt"

ECHO ## dml: fill refbook
"%ORA_BIN%\sqlplus" %AUTH% @"refbook.sql" > "%LOG_DIR%/refbook.txt"
CD refbook
call fill.bat
CD ..
ECHO ## ddl: create all constraint
"%ORA_BIN%\sqlplus" %AUTH% @"create_all_constraints.sql" > "%LOG_DIR%/create_all_constraints.txt"


PAUSE