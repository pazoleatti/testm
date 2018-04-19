@ECHO OFF
REM ����� ��������� ��� � ������ ��������� ����� � ����� ���� ������
REM ������: user_name/password@host:port/service_name
SET AUTH=ndfl_psi_test2/ndfl_psi_test2@sbrf_un
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

ECHO ## load ref_book_ndfl
"%ORA_BIN%\sqlplus" %AUTH% @"load_ref_book_ndfl.sql" %LOG_DIR%/load_ref_book_ndfl.txt %ORA_BIN% %AUTH% %LOG_DIR% %BAD_DIR% %LOG_DIR%/null_values.csv %LOG_DIR%/not_unique_kpp_oktmo_tax.csv

ECHO ## dml: templates
"%ORA_BIN%\sqlplus" %AUTH% @"templates.sql" %LOG_DIR%/templates.txt %ORA_BIN% %AUTH% %LOG_DIR% %BAD_DIR%

rem �������� ������ ���������� �������������������
PAUSE