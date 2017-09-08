@ECHO OFF
IF "%MODE%"=="" (
REM url for connect to oracle database
REM for example: user_name/password@host:port/service_name
SET AUTH=ndfl_psi/ndfl_psi@172.19.214.45:1521/orcl.aplana.local
rem SET AUTH=ndfl_prod/P2pzLA35@172.19.214.45:1521/orcl.aplana.local
REM path to oracle bin folder: ORACLE_HOME\BIN
REM SET ORA_BIN=C:\app\oracle\product\11.2.0\dbhome_1\BIN
SET ORA_BIN=c:\tools\oracle\product\11.2.0\client_1\BIN\
SET LOG_DIR=_logs
SET BAD_DIR=_bad
SET nls_lang=AMERICAN_AMERICA.AL32UTF8
)

"%ORA_BIN%\sqlplus" %AUTH% @"clean.sql" > "../../%LOG_DIR%/clean.txt"

"%ORA_BIN%\sqlplus" %AUTH% @"before_update_template.sql" > "../../%LOG_DIR%/before_update_template.txt"

ECHO #################################
ECHO ## template: refbook
ECHO #################################
CD refbook
CALL update.bat
CD ..
ECHO #################################
ECHO ## template: ndfl
ECHO #################################
CD ndfl
CALL update.bat
CD ..

"%ORA_BIN%\sqlplus" %AUTH% @"after_update_template.sql" > "../../%LOG_DIR%/after_update_template.txt"

IF "%MODE%"=="" (PAUSE)