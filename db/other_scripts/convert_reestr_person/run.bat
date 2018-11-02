@ECHO OFF
REM ����� ��������� ��� � ������ ��������� ����� � ����� ���� ������
REM ������: user_name/password@host:port/service_name
SET AUTH=ndfl_schema/schema_password@host:port/service_name
REM ����� ��������� ���� � ����� ORACLE_HOME\BIN
SET ORA_BIN=c:\app\client\user\product\12.1.0\client_1\BIN
SET LOG_DIR=_logs
SET nls_lang=AMERICAN_AMERICA.AL32UTF8

ECHO ## DB: %AUTH%

SET MODE=1
MKDIR %LOG_DIR%

DEL /s /q /f %LOG_DIR%\*.txt
DEL /s /q /f %LOG_DIR%\*.csv

ECHO ## 01_alter_table
"%ORA_BIN%\sqlplus" %AUTH% @"01_alter_table.sql" > "%LOG_DIR%/01_alter_table.txt"

ECHO ## 02_update_start_date
"%ORA_BIN%\sqlplus" %AUTH% @"02_update_start_date.sql" > "%LOG_DIR%/02_update_start_date.txt"

ECHO ## 03_fill_end_date
"%ORA_BIN%\sqlplus" %AUTH% @"03_1_fill_end_date.sql" > "%LOG_DIR%/03_1_fill_end_date.txt"

ECHO ## 03_2_check_end_date
"%ORA_BIN%\sqlplus" %AUTH% @"03_2_check_end_date.sql" %LOG_DIR%/03_REF_BOOK_PERSON_end_date.csv

ECHO ## 03_3_update_end_date
"%ORA_BIN%\sqlplus" %AUTH% @"03_3_update_end_date.sql" > "%LOG_DIR%/03_3_update_end_date.txt"

ECHO ## 04_check_before_delete
"%ORA_BIN%\sqlplus" %AUTH% @"04_check_before_delete.sql" %LOG_DIR%/04_REF_BOOK_ID_DOC_person_links.csv %LOG_DIR%/04_DECLARATION_DATA_PERSON_person_links.csv %LOG_DIR%/04_NDFL_PERSON_person_links.csv %LOG_DIR%/04_NDFL_REFERENCES_person_links.csv %LOG_DIR%/04_REF_BOOK_ID_TAX_PAYER_person_links.csv %LOG_DIR%/04_REF_BOOK_PERSON_TB_person_links.csv %LOG_DIR%/04_REF_BOOK_PERSON_id_doc_links.csv

ECHO ## 05_1_update
"%ORA_BIN%\sqlplus" %AUTH% @"05_1_update.sql" > "%LOG_DIR%/05_1_update.txt"

ECHO ## 05_2_delete
"%ORA_BIN%\sqlplus" %AUTH% @"05_2_delete.sql" > "%LOG_DIR%/05_2_delete.txt"

ECHO ## 06_1_check_null_field
"%ORA_BIN%\sqlplus" %AUTH% @"06_1_check_null_field.sql" %LOG_DIR%/06_1_check_null_field.txt %LOG_DIR%/06_NDFL_REFERENCES_person_id_null.csv %LOG_DIR%/06_REF_BOOK_ID_TAX_PAYER_person_id_null.csv %LOG_DIR%/06_REF_BOOK_PERSON_TB_person_id_null.csv %LOG_DIR%/06_REF_BOOK_PERSON_start_date_null.csv %LOG_DIR%/06_REF_BOOK_ID_DOC_person_id_null.csv

ECHO ## 06_2_alter_table_not_null
"%ORA_BIN%\sqlplus" %AUTH% @"06_2_alter_table_not_null.sql" > "%LOG_DIR%/06_2_alter_table_not_null.txt"

ECHO ## 07_drop_columns
"%ORA_BIN%\sqlplus" %AUTH% @"07_drop_columns.sql" > "%LOG_DIR%/07_drop_columns.txt"

ECHO ## 08_ref_book_address
"%ORA_BIN%\sqlplus" %AUTH% @"08_ref_book_address.sql" > "%LOG_DIR%/08_ref_book_address.txt"

rem �������� ������ ���������� �������������������
PAUSE