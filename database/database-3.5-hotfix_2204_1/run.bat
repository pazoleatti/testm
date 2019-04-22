@ECHO OFF
REM ����� ��������� ��� � ������ ��������� ����� � ����� ���� ������
REM ������: user_name/password@host:port/service_name
SET AUTH=ndfl_schema/schema_password@host:port/service_name
REM ����� ��������� ���� � ����� ORACLE_HOME\BIN
SET ORA_BIN=c:\app\client\user\product\12.2.0\client_1\BIN
SET LOG_DIR=_logs
SET BAD_DIR=_bad
SET nls_lang=AMERICAN_AMERICA.AL32UTF8

ECHO ## DB: %AUTH%

SET MODE=1
MKDIR %LOG_DIR%
MKDIR %BAD_DIR%

DEL /s /q /f %LOG_DIR%\*.txt
DEL /s /q /f %LOG_DIR%\*.csv
DEL /s /q /f %BAD_DIR%\*.*

ECHO ## templates
"%ORA_BIN%\sqlplus" %AUTH% @"02_templates.sql" %LOG_DIR%/02_templates.txt %ORA_BIN% %AUTH% %LOG_DIR% %BAD_DIR%

ECHO ## 03_update_dml
"%ORA_BIN%\sqlplus" %AUTH% @"03_update_dml.sql" > "%LOG_DIR%/03_update_dml.txt"

rem �������� ������ ���������� �������������������
PAUSE