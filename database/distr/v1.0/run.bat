@ECHO OFF
SET NDFL_USER=
SET NDFL_PASS=
SET NDFL_DBNAME=
SET AUTH=%NDFL_USER%/%NDFL_PASS%@%NDFL_DBNAME%
REM SET AUTH=NDFL_NEXT/TAX@172.16.127.16:1521/orcl.aplana.local
ECHO ## DB: %AUTH%

SET ORA_BIN=
rem SET ORA_BIN=C:\app\oracle\product\11.2.0\dbhome_1\BIN
SET LOG_DIR=logs
SET BAD_DIR=bad
SET nls_lang=AMERICAN_AMERICA.AL32UTF8

ECHO ## clean
DEL /s /q /f %LOG_DIR%\*.txt
DEL /s /q /f %BAD_DIR%\*.*
rem MKDIR %LOG_DIR%
rem MKDIR %BAD_DIR%

ECHO ## ddl: create_all_tables
"%ORA_BIN%\sqlplus" %AUTH% @"create_all_tables.sql" > "%LOG_DIR%/create_all_tables.txt"

ECHO ## dml: fill refbook
"%ORA_BIN%\sqlplus" %AUTH% @"refbook.sql" > "%LOG_DIR%/refbook.txt"
"%ORA_BIN%\sqlplus" %AUTH% @"upd_refbook316.sql" > "%LOG_DIR%/upd_refbook316.txt"
ECHO ## dml: fill oktmo
CD ldr
call load_oktmo.bat
CD ..
ECHO ## dml: fill other refbook
CD refbook
call fill.bat
CD ..
ECHO ## ddl: create all constraint
"%ORA_BIN%\sqlplus" %AUTH% @"create_all_constraints.sql" > "%LOG_DIR%/create_all_constraints.txt"


PAUSE