@ECHO OFF
REM Нужно указать имя,пароль владельца схемы и алиас базы
SET NDFL_USER=
SET NDFL_PASS=
SET NDFL_DBNAME=
REM SET NDFL_USER=NDFL
REM SET NDFL_PASS=NDFL
REM SET NDFL_DBNAME=orcl
SET AUTH=%NDFL_USER%/%NDFL_PASS%@%NDFL_DBNAME%
REM SET AUTH=NDFL_NEXT/TAX@172.16.127.16:1521/orcl.aplana.local
ECHO ## DB: %AUTH%

REM Нужно указать путь к папке %ORA_HOME%\BIN
SET ORA_BIN=
REM SET ORA_BIN=C:\app\oracle\product\11.2.0\dbhome_1\BIN
SET LOG_DIR=logs
SET nls_lang=AMERICAN_AMERICA.AL32UTF8

ECHO ## clean
REM Оставить либо создание, либо очистку папки для лог-файлов
REM DEL /s /q /f %LOG_DIR%\*.txt
MKDIR %LOG_DIR%

ECHO ## ddl: create_main
"%ORA_BIN%\sqlplus" %AUTH% @"create_main.sql" > "%LOG_DIR%/create_main.txt"

ECHO ## ddl: create_ref_book_tables
"%ORA_BIN%\sqlplus" %AUTH% @"create_ref_book_tables.sql" > "%LOG_DIR%/create_ref_book_tables.txt"

ECHO ## dml: fill refbook
"%ORA_BIN%\sqlplus" %AUTH% @"refbook.sql" > "%LOG_DIR%/refbook.txt"
CD refbook
call fill.bat
CD ..
ECHO ## ddl: create constraint
"%ORA_BIN%\sqlplus" %AUTH% @"create_constraint.sql" > "%LOG_DIR%/create_constraint.txt"
ECHO ## ddl: create ref book constraint
"%ORA_BIN%\sqlplus" %AUTH% @"create_ref_book_constraints.sql" > "%LOG_DIR%/create_ref_book_constraints.txt"



PAUSE