@ECHO OFF
REM ����� ��������� ��� � ������ ��������� ����� � ����� ���� ������
REM ������: user_name/password@host:port/service_name
SET AUTH=ndfl_unstable/ndfl_unstable@sbrf_un
REM ����� ��������� ���� � ����� ORACLE_HOME\BIN
SET ORA_BIN=D:\app\client\RNurgaleev\product\12.1.0\client_1\BIN
SET LOG_DIR=_logs
SET nls_lang=AMERICAN_AMERICA.AL32UTF8

ECHO ## DB: %AUTH%

SET MODE=1
MKDIR %LOG_DIR%

DEL /s /q /f %LOG_DIR%\*.txt

ECHO ## add_asnu
"%ORA_BIN%\sqlplus" %AUTH% @"add_asnu.sql" > "%LOG_DIR%/add_asnu.txt"


PAUSE