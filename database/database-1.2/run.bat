﻿@ECHO OFF
REM url for connect to oracle database
REM for example: user_name/password@host:port/service_name
SET AUTH=ndfl_psi/ndfl_psi@172.19.214.46:1521/orcl.aplana.local
REM path to oracle bin folder: ORACLE_HOME\BIN
SET ORA_BIN=D:\dev\instantclient_12_2
SET LOG_DIR=_logs
SET BAD_DIR=_bad
SET nls_lang=AMERICAN_AMERICA.AL32UTF8

ECHO ## DB: %AUTH%

SET MODE=1
MKDIR %LOG_DIR%
MKDIR %BAD_DIR%

DEL /s /q /f %LOG_DIR%\*.txt
DEL /s /q /f %BAD_DIR%\*.*

ECHO ## dml: templates
CD templates/script
CALL templates.bat
CD ../..
PAUSE