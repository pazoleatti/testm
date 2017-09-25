﻿@ECHO OFF
REM url for connect to oracle database
REM for example: user_name/password@host:port/service_name
SET AUTH=ndfl_psi/ndfl_psi@172.19.214.45:1521/orcl.aplana.local
REM system url for connect to oracle database
SET DBA_AUTH=SYSTEM/oracle@172.19.214.45:1521/orcl.aplana.local
REM path to oracle bin folder: ORACLE_HOME\BIN
SET ORA_BIN=D:\dev\instantclient_12_2
SET LOG_DIR=_logs
SET BAD_DIR=_bad
SET nls_lang=AMERICAN_AMERICA.AL32UTF8
SET NDFL_SCHEMA=NDFL_PSI

ECHO ## DB: %AUTH%

SET MODE=1
MKDIR %LOG_DIR%
MKDIR %BAD_DIR%

DEL /s /q /f %LOG_DIR%\*.txt
DEL /s /q /f %BAD_DIR%\*.*

ECHO ## grants
"%ORA_BIN%\sqlplus" %DBA_AUTH% @"grants.sql" > "%LOG_DIR%/grants.txt" %NDFL_SCHEMA%

ECHO ## ddl
"%ORA_BIN%\sqlplus" %AUTH% @"update_ddl.sql" > "%LOG_DIR%/update_ddl.txt"

ECHO ## dml: templates
CD templates/script
CALL templates.bat
CD ../..
PAUSE