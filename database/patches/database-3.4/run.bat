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

ECHO ## Beginning Installing Patch

ECHO ## check
 "%ORA_BIN%\sqlplus" %AUTH% @"01_check.sql" %LOG_DIR%/01_check_rb_ndfl_detail.csv %LOG_DIR%/01_check_rb_id_doc.csv %LOG_DIR%/01_check_rb_person.csv

ECHO ## templates
 "%ORA_BIN%\sqlplus" %AUTH% @"02_templates.sql" "%LOG_DIR%/02_templates.txt" %ORA_BIN% %AUTH% %LOG_DIR% %BAD_DIR%

ECHO ## 03_delete_rb_id_doc
"%ORA_BIN%\sqlplus" %AUTH% @"03_delete_rb_id_doc.sql" > "%LOG_DIR%/03_delete_rb_id_doc.txt"

ECHO ## 03_update_rb_ndfl_detail
"%ORA_BIN%\sqlplus" %AUTH% @"03_update_rb_ndfl_detail.sql" > "%LOG_DIR%/03_update_rb_ndfl_detail.txt"
 
ECHO ## 03_update_rb_person
"%ORA_BIN%\sqlplus" %AUTH% @"03_update_rb_person.sql" > "%LOG_DIR%/03_update_rb_person.txt"
 
ECHO ## 04_alter_tables
"%ORA_BIN%\sqlplus" %AUTH% @"04_alter_tables.sql" > "%LOG_DIR%/04_alter_tables.txt"
 
ECHO ## 05_update_dml
"%ORA_BIN%\sqlplus" %AUTH% @"05_update_dml.sql" > "%LOG_DIR%/05_update_dml.txt"
 
ECHO ## 06_replace_packages
"%ORA_BIN%\sqlplus" %AUTH% @"06_replace_packages.sql" > "%LOG_DIR%/06_replace_packages.txt"
 
ECHO ## gather statistics
"%ORA_BIN%\sqlplus" %AUTH% @"07_gather_statistics.sql" > "%LOG_DIR%/07_gather_statistics.txt"

rem �������� ������ ���������� �������������������
PAUSE