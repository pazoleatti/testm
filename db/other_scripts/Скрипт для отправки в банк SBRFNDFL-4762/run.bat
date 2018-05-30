@ECHO OFF
REM ����� ��������� ��� � ������ ��������� ����� � ����� ���� ������
REM ������: user_name/password@host:port/service_name
SET TAX_AUTH=tax/tax@172.19.214.45:1521/orcl.aplana.local

SET NDFL_AUTH=ndfl/ndfl@172.19.214.45:1521/orcl.aplana.local
SET NDFL_SCHEME=ndfl
REM ����� ��������� ���� � ����� ORACLE_HOME\BIN
SET ORA_BIN=D:\app\client\RNurgaleev\product\12.1.0\client_1\BIN
SET LOG_DIR=_logs
SET BAD_DIR=_bad
SET nls_lang=AMERICAN_AMERICA.AL32UTF8


ECHO ## DB: %AUTH%

SET MODE=1
MKDIR %LOG_DIR%
MKDIR %BAD_DIR%

DEL /s /q /f %LOG_DIR%\*.txt
DEL /s /q /f %BAD_DIR%\*.*


ECHO ## grant privileges and compile views
"%ORA_BIN%\sqlplus" %TAX_AUTH% @"grant.sql" > "%LOG_DIR%/grant.txt" %NDFL_SCHEME%

"%ORA_BIN%\sqlplus" %NDFL_AUTH% @"compile.sql" > "%LOG_DIR%/compile.txt"


rem �������� ������ ���������� �������������������
PAUSE