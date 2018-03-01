@ECHO OFF
REM ����� ��������� ��� � ������ ��������� ����� � ����� ���� ������
REM ������: user_name/password@host:port/service_name
SET AUTH=ndfl/ndfl@sbrf_un
REM ����� ��������� ���� � ����� ORACLE_HOME\BIN
SET ORA_BIN=D:\app\client\RNurgaleev\product\12.1.0\client_1\BIN
SET LOG_DIR=_logs
REM SET BAD_DIR=_bad
SET nls_lang=AMERICAN_AMERICA.AL32UTF8

ECHO ## DB: %AUTH%

SET MODE=1
MKDIR %LOG_DIR%
REM MKDIR %BAD_DIR%

DEL /s /q /f %LOG_DIR%\*.txt
REM DEL /s /q /f %BAD_DIR%\*.*

ECHO ## log
"%ORA_BIN%\sqlplus" %AUTH% @"log.sql" %LOG_DIR%/log.csv

ECHO ## delete
"%ORA_BIN%\sqlplus" %AUTH% @"delete.sql" > "%LOG_DIR%/delete.txt"


rem �������� ������ ���������� �������������������
PAUSE