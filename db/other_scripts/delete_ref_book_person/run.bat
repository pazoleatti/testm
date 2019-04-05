@ECHO OFF
SET TAX_FORM_LOG=Y
SET ROW_LIMIT=1000000
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

IF %TAX_FORM_LOG% EQU N GOTO DELETE

ECHO ## 01_tax_forms_log
"%ORA_BIN%\sqlplus" %AUTH% @"01_tax_forms_log.sql" %LOG_DIR%/01_tax_forms_log.txt

:DELETE
ECHO ## 02_create_tmp_table
"%ORA_BIN%\sqlplus" %AUTH% @"02_create_tmp_table.sql" %LOG_DIR%/02_create_tmp_table.txt %ROW_LIMIT%

ECHO ## 03_disable_indexes_and_constraints
"%ORA_BIN%\sqlplus" %AUTH% @"03_disable_indexes_and_constraints.sql" > "%LOG_DIR%/03_disable_indexes_and_constraints.txt"

ECHO ## 04_delete
"%ORA_BIN%\sqlplus" %AUTH% @"04_delete.sql" %LOG_DIR%/04_delete.txt

ECHO ## 05_enable_indexes_and_constraints
"%ORA_BIN%\sqlplus" %AUTH% @"05_enable_indexes_and_constraints.sql" > "%LOG_DIR%/05_enable_indexes_and_constraints.txt"

ECHO ## 06_drop_tmp_table
"%ORA_BIN%\sqlplus" %AUTH% @"06_drop_tmp_table.sql" > "%LOG_DIR%/06_drop_tmp_table.txt"

ECHO ## 07_update_hanging_duplicates
"%ORA_BIN%\sqlplus" %AUTH% @"07_update_hanging_duplicates.sql" %LOG_DIR%/07_update_hanging_duplicates.txt

ECHO ## 08_check_index_constraints
 "%ORA_BIN%\sqlplus" %AUTH% @"08_check_index_constraints.sql" %LOG_DIR%/08_check_index.txt "%LOG_DIR%/08_check_constraints.txt

PAUSE