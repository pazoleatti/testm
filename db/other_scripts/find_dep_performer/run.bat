@ECHO OFF
REM ����� ��������� ��� � ������ ��������� ����� � ����� ���� ������
REM ������: user_name/password@host:port/service_name
SET AUTH=ndfl_psi/ndfl_psi@172.19.214.45:1521/orcl.aplana.local
REM ID родительского банка исполнителя
SET DEP_ID=4
REM ����� ��������� ���� � ����� ORACLE_HOME\BIN
SET ORA_BIN=c:\app\client\akorobko\product\12.1.0\client_1\BIN
SET LOG_DIR=_logs
REM SET BAD_DIR=_bad
SET nls_lang=AMERICAN_AMERICA.AL32UTF8

ECHO ## DB: %AUTH%

SET MODE=1
MKDIR %LOG_DIR%
REM MKDIR %BAD_DIR%

DEL /s /q /f %LOG_DIR%\*.csv
REM DEL /s /q /f %BAD_DIR%\*.*

ECHO ## log
"%ORA_BIN%\sqlplus" %AUTH% @"find_dep_performer.sql" %LOG_DIR%/dep_performer.csv %DEP_ID%

rem �������� ������ ���������� �������������������
PAUSE