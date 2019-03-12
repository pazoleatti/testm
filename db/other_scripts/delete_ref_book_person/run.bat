@ECHO OFF
SET LOG_TAX_FORM=Y
SET ROW_LIMIT=1000000
REM ������: user_name/password@host:port/service_name
SET AUTH=ndfl_schema/schema_password@host:port/service_name
REM ����� ��������� ���� � ����� ORACLE_HOME\BIN
SET ORA_BIN=c:\app\client\user\product\12.2.0\client_1\BIN
SET LOG_DIR=_logs
SET nls_lang=AMERICAN_AMERICA.AL32UTF8

ECHO ## DB: %AUTH%

SET MODE=1
MKDIR %LOG_DIR%

DEL /s /q /f %LOG_DIR%\*.txt

IF %LOG_TAX_FORM% EQU N GOTO DELETE

ECHO ## log
"%ORA_BIN%\sqlplus" %AUTH% @"log.sql" %LOG_DIR%/1_not_deleted.txt

:DELETE
ECHO ## delete
"%ORA_BIN%\sqlplus" %AUTH% @"delete.sql" %LOG_DIR%/2_delete.txt %ROW_LIMIT%

ECHO ## update_hanging_duplicates
"%ORA_BIN%\sqlplus" %AUTH% @"update_hanging_duplicates.sql" %LOG_DIR%/3_update_hanging_duplicates.txt

PAUSE