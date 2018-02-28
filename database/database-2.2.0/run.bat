@ECHO OFF
REM ����� ��������� ��� � ������ ��������� ����� � ����� ���� ������
REM ������: user_name/password@host:port/service_name
SET AUTH=ndfl_psi_test/ndfl_psi_test@sbrf_un
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

ECHO ## ddl
"%ORA_BIN%\sqlplus" %AUTH% @"update_ddl.sql" > "%LOG_DIR%/update_ddl.txt"

ECHO ## dml
"%ORA_BIN%\sqlplus" %AUTH% @"update_dml.sql" > "%LOG_DIR%/update_dml.txt"


ECHO ## dml: templates
"%ORA_BIN%\sqlplus" %AUTH% @"templates.sql" %LOG_DIR%/templates.txt %ORA_BIN% %AUTH% %LOG_DIR% %BAD_DIR%

rem �������� ������ ���������� �������������������
PAUSE