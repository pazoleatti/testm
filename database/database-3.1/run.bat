@ECHO OFF
REM ����� ��������� ��� � ������ ��������� ����� � ����� ���� ������
REM ������: user_name/password@host:port/service_name
SET AUTH=korobko_test/qqq@172.19.214.45:1521/orcl.aplana.local
REM ����� ��������� ���� � ����� ORACLE_HOME\BIN
SET ORA_BIN=c:\app\client\akorobko\product\12.1.0\client_1\BIN
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

ECHO ## load ref_book_ndfl
"%ORA_BIN%\sqlplus" %AUTH% @"00_load_ref_book_ndfl.sql" %LOG_DIR%/load_ref_book_ndfl.txt %ORA_BIN% %AUTH% %LOG_DIR% %BAD_DIR% %LOG_DIR%/null_values.csv %LOG_DIR%/not_unique_kpp_oktmo_tax.csv

ECHO ## ddl
"%ORA_BIN%\sqlplus" %AUTH% @"01_update_ddl.sql" > "%LOG_DIR%/01_update_ddl.txt"

ECHO ## templates
"%ORA_BIN%\sqlplus" %AUTH% @"02_templates.sql" %LOG_DIR%/02_templates.txt %ORA_BIN% %AUTH% %LOG_DIR% %BAD_DIR%

ECHO ## dml
"%ORA_BIN%\sqlplus" %AUTH% @"03_update_dml.sql" > "%LOG_DIR%/03_update_dml.txt"

ECHO ## packages
"%ORA_BIN%\sqlplus" %AUTH% @"04_replace_packages" > "%LOG_DIR%/04_replace_packages.txt"

rem �������� ������ ���������� �������������������
PAUSE