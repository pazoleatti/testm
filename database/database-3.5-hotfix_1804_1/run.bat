@ECHO OFF
REM ����� ��������� ��� � ������ ��������� ����� � ����� ���� ������
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

ECHO ## create_trigger
"%ORA_BIN%\sqlplus" %AUTH% @"create_trigger.sql" %LOG_DIR%/create_trigger.txt

rem �������� ������ ���������� �������������������
PAUSE