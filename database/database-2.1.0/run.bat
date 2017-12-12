@ECHO OFF
REM ����� ��������� ��� � ������ ��������� ����� � ����� ���� ������
REM ������: user_name/password@host:port/service_name
SET AUTH=NDFL_UNSTABLE/ndfl_unstable@sbrf_un
REM ����� ��������� ���� � ����� ORACLE_HOME\BIN
SET ORA_BIN=D:\app\client\RNurgaleev\product\12.1.0\client_1\BIN
SET LOG_DIR=_logs
REM SET BAD_DIR=_bad
SET nls_lang=AMERICAN_AMERICA.AL32UTF8
SET TAX_SCHEME=TAX_1_5_1

ECHO ## DB: %AUTH%

SET MODE=1
MKDIR %LOG_DIR%
REM MKDIR %BAD_DIR%

DEL /s /q /f %LOG_DIR%\*.txt
REM DEL /s /q /f %BAD_DIR%\*.*

ECHO ## ddl
"%ORA_BIN%\sqlplus" %AUTH% @"update_ddl.sql" > "%LOG_DIR%/update_ddl.txt" %TAX_SCHEME%

ECHO ## dml
"%ORA_BIN%\sqlplus" %AUTH% @"update_dml.sql" > "%LOG_DIR%/update_dml.txt"

ECHO ## dml: templates
CD templates/script
CALL templates.bat
CD ../..

rem �������� ������ ���������� �������������������
PAUSE